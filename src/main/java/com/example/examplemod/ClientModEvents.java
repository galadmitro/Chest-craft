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

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.getEntity() == mc.player && event.getLevel().isClientSide()) {
            mc.execute(() -> {
                mc.getTutorial().setStep(TutorialSteps.NONE);
                mc.options.menuBackgroundBlurriness().set(0);
                if (!(mc.screen instanceof LockIn2DScreen)) {
                    mc.setScreen(new LockIn2DScreen());
                }
            });
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            mc.getTutorial().setStep(TutorialSteps.NONE);
            mc.options.menuBackgroundBlurriness().set(0);

            if (mc.gameRenderer != null) {
                mc.gameRenderer.shutdownEffect();
            }

            if (!(mc.screen instanceof LockIn2DScreen) && !(mc.screen instanceof PauseScreen)) {
                mc.setScreen(new LockIn2DScreen());
            }

            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity instanceof Mob mob && mob.getTarget() == mc.player) {
                    mob.setTarget(null);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (!(event.getNewScreen() instanceof LockIn2DScreen) && !(event.getNewScreen() instanceof PauseScreen)) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.level != null) {
                event.setNewScreen(new LockIn2DScreen());
            }
        }
    }

    @SubscribeEvent
    public static void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer != null) {
            mc.gameRenderer.shutdownEffect();
        }

        if (event.getScreen() instanceof PauseScreen) {
            LockIn2DScreen.render2DScene(
                    event.getGuiGraphics(),
                    event.getScreen().width,
                    event.getScreen().height,
                    event.getMouseX(),
                    event.getMouseY()
            );
        }
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof LockIn2DScreen) {
            event.setSound(null);
        }
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && event.getEntity() == mc.player) {
            event.setCanceled(true);
        }
    }
}
