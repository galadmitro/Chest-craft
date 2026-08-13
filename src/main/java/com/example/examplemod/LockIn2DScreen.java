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

    // Exact Vanilla Double Chest Dimensions (1:1 Unscaled)
    private static final int CHEST_W = 176;
    private static final int CHEST_H = 222;

    // 2D Physics & Player Coordinates
    private float playerX = 0;
    private float playerY = 0;
    private float velocityX = 0;
    private float velocityY = 0;
    private boolean isOnGround = false;

    // Movement Physics (Tuned for 1x Pixel Grid)
    private static final float GRAVITY = 0.6f;
    private static final float JUMP_STRENGTH = -8.5f;
    private static final float MOVE_SPEED = 2.5f;
    private static final int PLAYER_SCALE = 15;

    // Key States
    private boolean keyLeft = false;
    private boolean keyRight = false;
    private boolean keyJump = false;

    public LockIn2DScreen() {
        super(Component.literal("2D Chest World"));
    }

    @Override
    protected void init() {
        super.init();

        // 1. Disable Tutorial Toasts
        if (this.minecraft != null) {
            this.minecraft.getTutorial().setStep(TutorialSteps.NONE);
            // 2. Mute world background sound
            this.minecraft.getSoundManager().pause();
        }

        // Center player inside the TOP CHEST GRID initially
        int guiX = (this.width - CHEST_W) / 2;
        int guiY = (this.height - CHEST_H) / 2;

        if (this.playerX == 0 && this.playerY == 0) {
            this.playerX = guiX + (CHEST_W / 2.0f);
            this.playerY = guiY + 125.0f; // Bottom of top chest grid
        }
    }

    @Override
    public void removed() {
        super.removed();
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().resume();
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false; // Prevent ESC from closing screen
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        // Keep player invulnerable & fed in background world
        if (this.minecraft != null && this.minecraft.player != null) {
            LocalPlayer p = this.minecraft.player;
            p.getAbilities().invulnerable = true;
            p.setHealth(p.getMaxHealth());
            p.getFoodData().setFoodLevel(20);
        }

        updatePhysics();
    }

    private void updatePhysics() {
        int guiX = (this.width - CHEST_W) / 2;
        int guiY = (this.height - CHEST_H) / 2;

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

        // Strict constraints: Left and Right walls of top chest grid
        float minX = guiX + 16;
        float maxX = guiX + CHEST_W - 16;
        if (playerX < minX) playerX = minX;
        if (playerX > maxX) playerX = maxX;

        // Floor: Bottom boundary of the TOP chest grid (Row 6)
        float floorY = guiY + 125;
        if (playerY >= floorY) {
            playerY = floorY;
            velocityY = 0;
            isOnGround = true;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Prevent closing via ESC / E
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
        // 1. Background Dirt
        renderDirtBackground(guiGraphics);

        int guiX = (this.width - CHEST_W) / 2;
        int guiY = (this.height - CHEST_H) / 2;

        // 2. Pixel-Perfect Crisp Vanilla Chest GUI (1:1 Unscaled)
        guiGraphics.blit(CHEST_GUI_TEXTURE, guiX, guiY, 0, 0, CHEST_W, CHEST_H, 256, 256);

        // 3. Grass Blocks at the bottom of TOP CHEST GRID
        renderGrassFloorInTopGrid(guiGraphics, guiX, guiY);

        // 4. Player inside TOP CHEST GRID
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            int intX = Math.round(playerX);
            int intY = Math.round(playerY);

            int x1 = intX - 12;
            int y1 = intY - 28;
            int x2 = intX + 12;
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

    private void renderGrassFloorInTopGrid(GuiGraphics guiGraphics, int guiX, int guiY) {
        ItemStack grassStack = new ItemStack(Items.GRASS_BLOCK);
        int startX = guiX + 8;
        int floorY = guiY + 109; // Row 6 of top grid

        for (int i = 0; i < 9; i++) {
            guiGraphics.renderItem(grassStack, startX + (i * 18), floorY);
        }
    }
}
