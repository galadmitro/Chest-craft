package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public class LockIn2DScreen extends Screen {
    // Textures
    private static final ResourceLocation DIRT_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/dirt.png");
    private static final ResourceLocation CHEST_GUI_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");

    // 2D Physics & Player Coordinates
    private float playerX = 0;
    private float playerY = 0;
    private float velocityX = 0;
    private float velocityY = 0;
    private boolean isOnGround = false;

    // Movement Physics Configuration
    private static final float GRAVITY = 0.8f;
    private static final float JUMP_STRENGTH = -11.0f;
    private static final float MOVE_SPEED = 3.5f;
    private static final int PLAYER_SCALE = 22;

    // Key States
    private boolean keyLeft = false;
    private boolean keyRight = false;
    private boolean keyJump = false;

    // Chest Frame Dimensions (2x Scaled)
    private static final int CHEST_TEX_W = 176;
    private static final int CHEST_TEX_H = 166;
    private static final float GUI_SCALE = 2.0f;

    public LockIn2DScreen() {
        super(Component.literal("2D Chest World"));
    }

    @Override
    protected void init() {
        super.init();

        // 1. Disable Tutorial Toast overlay
        if (this.minecraft != null) {
            this.minecraft.getTutorial().setStep(TutorialSteps.NONE);
            // 2. Pause background world sounds
            this.minecraft.getSoundManager().pause();
        }

        // Center player inside the chest container bounds initially
        int guiWidth = Math.round(CHEST_TEX_W * GUI_SCALE);
        int guiHeight = Math.round(CHEST_TEX_H * GUI_SCALE);
        int guiX = (this.width - guiWidth) / 2;
        int guiY = (this.height - guiHeight) / 2;

        if (this.playerX == 0 && this.playerY == 0) {
            this.playerX = guiX + (guiWidth / 2.0f);
            this.playerY = guiY + guiHeight - 30.0f;
        }
    }

    @Override
    public void removed() {
        super.removed();
        // Resume world audio if screen ever closes
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().resume();
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false; // Strictly prevents ESC key from closing screen
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        // Prevent background player death / damage / hunger
        if (this.minecraft != null && this.minecraft.player != null) {
            LocalPlayer p = this.minecraft.player;
            p.getAbilities().invulnerable = true;
            p.setHealth(p.getMaxHealth());
            p.getFoodData().setFoodLevel(20);
        }

        updatePhysics();
    }

    private void updatePhysics() {
        int guiWidth = Math.round(CHEST_TEX_W * GUI_SCALE);
        int guiHeight = Math.round(CHEST_TEX_H * GUI_SCALE);
        int guiX = (this.width - guiWidth) / 2;
        int guiY = (this.height - guiHeight) / 2;

        velocityX = 0;
        if (keyLeft) velocityX -= MOVE_SPEED;
        if (keyRight) velocityX += MOVE_SPEED;

        if (keyJump && isOnGround) {
            velocityY = JUMP_STRENGTH;
            isOnGround = false;
        }

        velocityY += GRAVITY;

        playerX += velocityX;
        playerY += velocityY;

        // Strict constraints to stay INSIDE the chest GUI frame
        float minX = guiX + 24;
        float maxX = guiX + guiWidth - 24;
        if (playerX < minX) playerX = minX;
        if (playerX > maxX) playerX = maxX;

        // Floor level inside the bottom section of the chest UI
        float floorY = guiY + guiHeight - 20;
        if (playerY >= floorY) {
            playerY = floorY;
            velocityY = 0;
            isOnGround = true;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Intercept ESC and inventory keys (E)
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_E) {
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_A || keyCode == GLFW.GLFW_KEY_LEFT) {
            keyLeft = true;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_D || keyCode == GLFW.GLFW_KEY_RIGHT) {
            keyRight = true;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_W || keyCode == GLFW.GLFW_KEY_UP) {
            keyJump = true;
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_A || keyCode == GLFW.GLFW_KEY_LEFT) {
            keyLeft = false;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_D || keyCode == GLFW.GLFW_KEY_RIGHT) {
            keyRight = false;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_W || keyCode == GLFW.GLFW_KEY_UP) {
            keyJump = false;
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 1. Tiled Dirt Background
        renderDirtBackground(guiGraphics);

        // Calculate chest GUI position
        int guiWidth = Math.round(CHEST_TEX_W * GUI_SCALE);
        int guiHeight = Math.round(CHEST_TEX_H * GUI_SCALE);
        int guiX = (this.width - guiWidth) / 2;
        int guiY = (this.height - guiHeight) / 2;

        // 2. Scaled Chest GUI Frame
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(guiX, guiY, 0);
        guiGraphics.pose().scale(GUI_SCALE, GUI_SCALE, 1.0f);
        guiGraphics.blit(CHEST_GUI_TEXTURE, 0, 0, 0, 0, CHEST_TEX_W, CHEST_TEX_H, 256, 256);
        guiGraphics.pose().popPose();

        // 3. Render Grass Floor INSIDE Chest UI
        renderGrassFloorInsideChest(guiGraphics, guiX, guiY, guiWidth, guiHeight);

        // 4. Render 3D Player Model INSIDE Chest UI
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            int intX = Math.round(playerX);
            int intY = Math.round(playerY);

            int x1 = intX - 18;
            int y1 = intY - 45;
            int x2 = intX + 18;
            int y2 = intY;

            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    guiGraphics,
                    x1, y1, x2, y2,
                    PLAYER_SCALE,
                    0.0625f,
                    mouseX, mouseY,
                    player
            );
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderDirtBackground(GuiGraphics guiGraphics) {
        int tileSize = 16;
        for (int x = 0; x < this.width; x += tileSize) {
            for (int y = 0; y < this.height; y += tileSize) {
                guiGraphics.blit(DIRT_TEXTURE, x, y, 0, 0, tileSize, tileSize, tileSize, tileSize);
            }
        }
    }

    private void renderGrassFloorInsideChest(GuiGraphics guiGraphics, int guiX, int guiY, int guiWidth, int guiHeight) {
        ItemStack grassStack = new ItemStack(Items.GRASS_BLOCK);
        int scale = 1;
        int step = 16 * scale;
        int startX = guiX + 18;
        int endX = guiX + guiWidth - 18;
        int floorY = guiY + guiHeight - 32;

        for (int x = startX; x < endX; x += step) {
            guiGraphics.renderItem(grassStack, x, floorY);
        }
    }
}
