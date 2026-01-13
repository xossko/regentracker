package com.xossko.regentracker.network;

import com.xossko.regentracker.client.RegenDisplayRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RegenDataPacket {
    private final float regenPerSecond;
    private final float healingMultiplier;
    private final float naturalRegen;
    
    public RegenDataPacket(float regenPerSecond, float healingMultiplier, float naturalRegen) {
        this.regenPerSecond = regenPerSecond;
        this.healingMultiplier = healingMultiplier;
        this.naturalRegen = naturalRegen;
    }
    
    public RegenDataPacket(FriendlyByteBuf buf) {
        this.regenPerSecond = buf.readFloat();
        this.healingMultiplier = buf.readFloat();
        this.naturalRegen = buf.readFloat();
    }
    
    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(regenPerSecond);
        buf.writeFloat(healingMultiplier);
        buf.writeFloat(naturalRegen);
    }
    
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> 
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> 
                RegenDisplayRenderer.updateRegenData(regenPerSecond, healingMultiplier, naturalRegen)
            )
        );
        ctx.get().setPacketHandled(true);
    }
}