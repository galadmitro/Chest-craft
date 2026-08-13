package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class CustomBlurScreen extends Screen {

    public CustomBlurScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();
        Minecraft mc = Minecraft.getInstance();
        ClientModEvents.applyBlur(mc);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        // Custom UI drawing goes here
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            // Close this screen and clean up blur
            this.onClose();
            
            // Open standard Minecraft pause menu
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                mc.setScreen(new PauseScreen(true));
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        // Shutdown effect on close so the screen stops being blurry
        Minecraft mc = Minecraft.getInstance();
        ClientModEvents.clearBlur(mc);
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
