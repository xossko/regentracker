package com.xossko.regentracker.event;

import com.xossko.regentracker.network.PacketHandler;
import com.xossko.regentracker.network.RegenDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class RegenTrackingHandler {
    
    // Хранение данных о регенерации для каждого игрока
    private static final Map<UUID, RegenData> PLAYER_REGEN_DATA = new HashMap<>();
    
    // Отслеживание последнего HP для расчета естественной регенерации
    private static final Map<UUID, Float> LAST_HEALTH = new HashMap<>();
    
    /**
     * Отслеживание всех событий лечения с низким приоритетом
     * чтобы перехватить модификации после их применения
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        
        UUID playerId = player.getUUID();
        RegenData data = PLAYER_REGEN_DATA.computeIfAbsent(playerId, k -> new RegenData());
        
        // Сохраняем оригинальное и финальное значение лечения
        float originalAmount = event.getAmount();
        
        // Регистрируем событие лечения для вычисления модификатора
        data.lastOriginalHeal = originalAmount;
        data.lastModifiedHeal = event.getAmount(); // После всех модификаций
        data.lastHealTime = System.currentTimeMillis();
        
        // Вычисляем текущий модификатор лечения
        if (originalAmount > 0) {
            data.currentHealingMultiplier = event.getAmount() / originalAmount;
        }
    }
    
    /**
     * Обновление данных каждый тик
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide) return;
        
        Player player = event.player;
        UUID playerId = player.getUUID();
        RegenData data = PLAYER_REGEN_DATA.computeIfAbsent(playerId, k -> new RegenData());
        
        // Отслеживание изменения здоровья для вычисления регенерации
        float currentHealth = player.getHealth();
        Float lastHealth = LAST_HEALTH.get(playerId);
        
        if (lastHealth != null) {
            float healthDiff = currentHealth - lastHealth;
            
            // Если здоровье увеличилось и это не было событие лечения
            long timeSinceLastHeal = System.currentTimeMillis() - data.lastHealTime;
            
            if (healthDiff > 0 && timeSinceLastHeal > 100) {
                // Это естественная регенерация (например, от насыщения или эффектов)
                data.naturalRegenPerTick = healthDiff;
                data.lastNaturalRegenTime = System.currentTimeMillis();
            } else if (timeSinceLastHeal < 50) {
                // Это было событие лечения, используем данные из LivingHealEvent
                data.regenPerSecond = data.lastModifiedHeal;
            }
        }
        
        LAST_HEALTH.put(playerId, currentHealth);
        
        // Вычисляем общую регенерацию в секунду
        calculateTotalRegen(player, data);
        
        // Отправляем данные клиенту каждые 10 тиков (0.5 сек)
        if (player.tickCount % 10 == 0 && player instanceof ServerPlayer serverPlayer) {
            PacketHandler.sendToClient(new RegenDataPacket(
                data.regenPerSecond,
                data.currentHealingMultiplier,
                data.naturalRegenPerTick * 20 // Конвертируем в секунду
            ), serverPlayer);
        }
    }
    
    /**
     * Вычисление общей регенерации с учетом всех источников
     */
    private static void calculateTotalRegen(Player player, RegenData data) {
        float totalRegen = 0;
        
        // 1. Естественная регенерация от насыщения
        long timeSinceNaturalRegen = System.currentTimeMillis() - data.lastNaturalRegenTime;
        if (timeSinceNaturalRegen < 1000) { // Если была регенерация в последнюю секунду
            totalRegen += data.naturalRegenPerTick * 20; // * 20 тиков = секунда
        }
        
        // 2. Регенерация от эффектов зелий
        if (player.hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION)) {
            int amplifier = player.getEffect(net.minecraft.world.effect.MobEffects.REGENERATION).getAmplifier();
            // Регенерация работает каждые 50/(amplifier+1) тиков
            float effectRegenPerSec = 20.0f / (50.0f / (amplifier + 1));
            totalRegen += effectRegenPerSec * data.currentHealingMultiplier;
        }
        
        // 3. Регенерация от релик (Heart of Tarrasque и других)
        // Эта регенерация уже учтена через LivingHealEvent
        long timeSinceLastHeal = System.currentTimeMillis() - data.lastHealTime;
        if (timeSinceLastHeal < 1000) {
            totalRegen += data.regenPerSecond;
        }
        
        // Применяем модификатор лечения ко всей регенерации
        data.regenPerSecond = totalRegen;
    }
    
    /**
     * Класс для хранения данных о регенерации игрока
     */
    private static class RegenData {
        float regenPerSecond = 0;
        float currentHealingMultiplier = 1.0f;
        float naturalRegenPerTick = 0;
        float lastOriginalHeal = 0;
        float lastModifiedHeal = 0;
        long lastHealTime = 0;
        long lastNaturalRegenTime = 0;
    }
}