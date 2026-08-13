package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public class LockIn2DScreen extends Screen {
    private static final ResourceLocation DIRT_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/options_background.png");

    // 2D Physics & Player Coordinates
    private float playerX = 0;
    private float playerY = 0;
    private float velocityX = 0;
    private float velocityY = 0;
    private boolean isOnGround = false;

    // Movement Physics Configuration
    private static final float GRAVITY = 0.8f;
    private static final float JUMP_STRENGTH = -12.0f;
    private static final float MOVE_SPEED = 4.0f;
    private static final int PLAYER_SCALE = 30;

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
        if (this.playerX == 0 && this.playerY == 0) {
            this.playerX = this.width / 2.0f;
            this.playerY = this.height - 60.0f;
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false; // Disables ESC closing completely
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        updatePhysics();
    }

    private void updatePhysics() {
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

        // Screen Side Constraints
        float minX = 25;
        float maxX = this.width - 25;
        if (playerX < minX) playerX = minX;
        if (playerX > maxX) playerX = maxX;

        // Floor Collision (Grass Block Level)
        float floorY = this.height - 40;
        if (playerY >= floorY) {
            playerY = floorY;
            velocityY = 0;
            isOnGround = true;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Intercept close commands (ESC / E key)
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
        // 1. Tiled Dirt Texture Background
        renderDirtBackground(guiGraphics);

        // 2. Line of Grass Blocks on Floor
        renderGrassFloor(guiGraphics);

        // 3. 3D Player Model using active player skin
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            int intX = Math.round(playerX);
            int intY = Math.round(playerY);

            int x1 = intX - 25;
            int y1 = intY - 60;
            int x2 = intX + 25;
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
        int tileSize = 32;
        for (int x = 0; x < this.width; x += tileSize) {
            for (int y = 0; y < this.height; y += tileSize) {
                guiGraphics.blit(DIRT_TEXTURE, x, y, 0, 0, tileSize, tileSize, tileSize, tileSize);
            }
        }
    }

    private void renderGrassFloor(GuiGraphics guiGraphics) {
        ItemStack grassStack = new ItemStack(Items.GRASS_BLOCK);
        int scale = 2;
        int step = 16 * scale;
        int floorY = this.height - 32;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0f);

        for (int x = 0; x < this.width; x += step) {
            guiGraphics.renderItem(grassStack, x / scale, floorY / scale);
        }

        guiGraphics.pose().popPose();
    }
}