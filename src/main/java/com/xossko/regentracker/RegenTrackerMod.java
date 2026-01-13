package com.xossko.regentracker;

import com.mojang.logging.LogUtils;
import com.xossko.regentracker.client.ClientSetup;
import com.xossko.regentracker.config.RegenTrackerConfig;
import com.xossko.regentracker.event.RegenTrackingHandler;
import com.xossko.regentracker.network.PacketHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod("regentracker")
public class RegenTrackerMod {
    public static final String MODID = "regentracker";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RegenTrackerMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // Регистрация конфига
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, RegenTrackerConfig.SPEC);
        
        // События инициализации
        modEventBus.addListener(this::commonSetup);
        
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::clientSetup);
        }
        
        // Регистрация обработчиков событий
        MinecraftForge.EVENT_BUS.register(new RegenTrackingHandler());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(PacketHandler::register);
        LOGGER.info("Regen Tracker Mod initialized!");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ClientSetup.init();
    }
}