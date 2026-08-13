package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = "examplemod", value = Dist.CLIENT)
public class ClientModEvents {

    // Trigger lock-in when player joins world
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.getEntity() == mc.player && event.getLevel().isClientSide()) {
            mc.execute(() -> {
                mc.getTutorial().setStep(TutorialSteps.NONE);
                if (!(mc.screen instanceof LockIn2DScreen)) {
                    mc.setScreen(new LockIn2DScreen());
                }
            });
        }
    }

    // Enforce minigame screen every tick (unless in Pause Menu)
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            mc.getTutorial().setStep(TutorialSteps.NONE);

            // Re-open 2D screen if not currently in PauseScreen or LockIn2DScreen
            if (!(mc.screen instanceof LockIn2DScreen) && !(mc.screen instanceof PauseScreen)) {
                mc.setScreen(new LockIn2DScreen());
            }

            // Make mobs in the 3D world stop targeting client player
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity instanceof Mob mob && mob.getTarget() == mc.player) {
                    mob.setTarget(null);
                }
            }
        }
    }

    // Intercept inventory/external screen openings
    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (!(event.getNewScreen() instanceof LockIn2DScreen) && !(event.getNewScreen() instanceof PauseScreen)) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.level != null) {
                event.setNewScreen(new LockIn2DScreen());
            }
        }
    }

    // MUTE SOUNDS: Silence real world audio while in 2D minigame
    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof LockIn2DScreen) {
            event.setSound(null);
        }
    }

    // DAMAGE IMMUNITY: Cancel all incoming damage
    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && event.getEntity() == mc.player) {
            event.setCanceled(true);
        }
    }
}
