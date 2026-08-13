package com.example.examplemod;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@Mod("examplemod")
public class ExampleMod {

    public static final KeyMapping TOGGLE_NAMETAG_KEY = new KeyMapping(
            "key.examplemod.toggle_nametag",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "key.categories.examplemod"
    );

    public ExampleMod(ModContainer modContainer, IEventBus modBus) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        modBus.addListener(this::registerKeyMappings);
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_NAMETAG_KEY);
    }
}