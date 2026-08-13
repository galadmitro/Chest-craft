package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = "examplemod", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientModEvents {

    private static boolean blurActive = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        // If player or level is null, turn off blur if active and stop
        if (mc.player == null || mc.level == null) {
            if (blurActive) {
                clearBlur(mc);
            }
            return;
        }

        // Check client-side entities to see if any mob is targeting the player
        boolean isTargeted = false;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof Mob mob) {
                if (mob.getTarget() == mc.player) {
                    isTargeted = true;
                    break;
                }
            }
        }

        // Handle blur state transitions
        if (isTargeted && !blurActive) {
            applyBlur(mc);
        } else if (!isTargeted && blurActive && mc.screen == null) {
            clearBlur(mc);
        }
    }

    public static void applyBlur(Minecraft mc) {
        if (mc.gameRenderer != null && !blurActive) {
            try {
                mc.gameRenderer.loadEffect(ResourceLocation.withDefaultNamespace("shaders/post/blur.json"));
                blurActive = true;
            } catch (Exception e) {
                // Ignore if shader fails to load
            }
        }
    }

    public static void clearBlur(Minecraft mc) {
        if (mc.gameRenderer != null) {
            mc.gameRenderer.shutdownEffect();
            blurActive = false;
        }
    }

    public static boolean isBlurActive() {
        return blurActive;
    }
}
