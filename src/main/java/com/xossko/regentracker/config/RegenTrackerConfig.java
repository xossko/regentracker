package com.xossko.regentracker.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class RegenTrackerConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.IntValue X_POSITION;
    public static final ForgeConfigSpec.IntValue Y_POSITION;
    public static final ForgeConfigSpec.BooleanValue SHOW_DETAILED;
    public static final ForgeConfigSpec.DoubleValue UPDATE_FREQUENCY;
    
    static {
        BUILDER.push("Regen Tracker Settings");
        
        ENABLED = BUILDER
                .comment("Включить/выключить отображение регенерации")
                .define("enabled", true);
        
        X_POSITION = BUILDER
                .comment("Позиция X на экране (отрицательные значения считаются от правого края)")
                .defineInRange("xPosition", 10, -1000, 1000);
        
        Y_POSITION = BUILDER
                .comment("Позиция Y на экране (отрицательные значения считаются от нижнего края)")
                .defineInRange("yPosition", 10, -1000, 1000);
        
        SHOW_DETAILED = BUILDER
                .comment("Показывать детальную информацию (множители лечения)")
                .define("showDetailed", true);
        
        UPDATE_FREQUENCY = BUILDER
                .comment("Частота обновления в секундах")
                .defineInRange("updateFrequency", 0.5, 0.1, 2.0);
        
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}