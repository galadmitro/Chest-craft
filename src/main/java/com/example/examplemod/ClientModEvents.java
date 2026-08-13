package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = ExampleMod.MOD_ID, value = Dist.CLIENT)
public class ClientModEvents {

    // Trigger lock-in as soon as world loads
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.getEntity() == mc.player && event.getLevel().isClientSide()) {
            mc.execute(() -> {
                if (!(mc.screen instanceof LockIn2DScreen)) {
                    mc.setScreen(new LockIn2DScreen());
                }
            });
        }
    }

    // Continuously enforce screen lock every tick (0-tick escape gap)
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            if (!(mc.screen instanceof LockIn2DScreen)) {
                mc.setScreen(new LockIn2DScreen());
            }
        }
    }

    // Intercept any attempt to open external menus
    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (!(event.getNewScreen() instanceof LockIn2DScreen)) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.level != null) {
                event.setNewScreen(new LockIn2DScreen());
            }
        }
    }
}