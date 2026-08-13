package com.example.examplemod;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod("examplemod")
public class ExampleMod {

    public ExampleMod(ModContainer modContainer) {
        // Register Client Config for Mods -> Configure menu
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
    }
}
