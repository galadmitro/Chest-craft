package com.example.examplemod;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SHOW_NAMETAG = BUILDER
            .comment("Toggle rendering of the player nametag above the 2D model.")
            .define("showNametag", true);

    public static final ModConfigSpec SPEC = BUILDER.build();
}