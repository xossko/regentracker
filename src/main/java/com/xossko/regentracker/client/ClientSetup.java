package com.xossko.regentracker.client;

import net.minecraftforge.common.MinecraftForge;

public class ClientSetup {
    public static void init() {
        MinecraftForge.EVENT_BUS.register(RegenDisplayRenderer.class);
    }
}