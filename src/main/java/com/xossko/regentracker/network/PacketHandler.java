package com.xossko.regentracker.network;

import com.xossko.regentracker.RegenTrackerMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    private static int packetId = 0;
    
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RegenTrackerMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    
    public static void register() {
        INSTANCE.messageBuilder(RegenDataPacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(RegenDataPacket::encode)
                .decoder(RegenDataPacket::new)
                .consumerMainThread(RegenDataPacket::handle)
                .add();
        
        RegenTrackerMod.LOGGER.info("Registered network packets");
    }
    
    private static int nextId() {
        return packetId++;
    }
    
    public static void sendToClient(RegenDataPacket packet, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}