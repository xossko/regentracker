package com.xossko.regentracker.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xossko.regentracker.config.RegenTrackerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class RegenDisplayRenderer {
    
    // Данные регенерации, обновляемые из пакетов
    private static float currentRegenPerSecond = 0;
    private static float healingMultiplier = 1.0f;
    private static float naturalRegen = 0;
    
    // Для плавного отображения
    private static float displayedRegen = 0;
    private static long lastUpdateTime = 0;
    
    public static void updateRegenData(float regenPerSec, float healMult, float natRegen) {
        currentRegenPerSecond = regenPerSec;
        healingMultiplier = healMult;
        naturalRegen = natRegen;
        lastUpdateTime = System.currentTimeMillis();
    }
    
    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (!RegenTrackerConfig.ENABLED.get()) return;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        
        // Плавное изменение отображаемого значения
        float target = currentRegenPerSecond;
        float diff = target - displayedRegen;
        displayedRegen += diff * 0.2f; // Плавная интерполяция
        
        // Если прошло больше 2 секунд с последнего обновления, обнуляем
        if (System.currentTimeMillis() - lastUpdateTime > 2000) {
            displayedRegen *= 0.95f;
            if (Math.abs(displayedRegen) < 0.01f) {
                displayedRegen = 0;
            }
        }
        
        GuiGraphics guiGraphics = event.getGuiGraphics();
        
        // Позиция отображения
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        
        int xPos = RegenTrackerConfig.X_POSITION.get();
        int yPos = RegenTrackerConfig.Y_POSITION.get();
        
        // Если позиция отрицательная, считаем от правого/нижнего края
        if (xPos < 0) xPos = screenWidth + xPos;
        if (yPos < 0) yPos = screenHeight + yPos;
        
        // Формирование текста
        String regenText = formatRegenText(displayedRegen, healingMultiplier);
        
        // Цвет в зависимости от значения
        int color = getColorForRegen(displayedRegen, healingMultiplier);
        
        // Отрисовка с тенью
        RenderSystem.enableBlend();
        guiGraphics.drawString(mc.font, regenText, xPos, yPos, color, true);
        RenderSystem.disableBlend();
        
        // Дополнительная информация (если включена в конфиге)
        if (RegenTrackerConfig.SHOW_DETAILED.get() && displayedRegen > 0.01f) {
            String detailText = String.format("§7Множитель: §f%.1f%%", healingMultiplier * 100);
            guiGraphics.drawString(mc.font, detailText, xPos, yPos + 10, 0xAAAAAA, true);
        }
    }
    
    /**
     * Форматирование текста регенерации
     */
    private static String formatRegenText(float regenPerSec, float multiplier) {
        if (Math.abs(regenPerSec) < 0.01f) {
            return "§7HP: §f0.0/сек";
        }
        
        // Конвертация в сердца (1 сердце = 2 HP)
        float hearts = regenPerSec / 2.0f;
        
        String sign = regenPerSec > 0 ? "§a+" : "§c";
        
        if (Math.abs(hearts) >= 1.0f) {
            return String.format("%s%.1f §f❤/сек", sign, hearts);
        } else {
            return String.format("%s%.2f §f❤/сек", sign, hearts);
        }
    }
    
    /**
     * Получение цвета в зависимости от регенерации
     */
    private static int getColorForRegen(float regen, float multiplier) {
        if (regen <= 0) return 0xFF5555; // Красный
        if (regen < 0.5f) return 0xFFFF55; // Желтый
        if (regen < 2.0f) return 0x55FF55; // Зеленый
        return 0x55FFFF; // Голубой (сильная регенерация)
    }
}