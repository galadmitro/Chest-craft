package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public class LockIn2DScreen extends Screen {
    // Textures
    public static final ResourceLocation DIRT_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/dirt.png");
    public static final ResourceLocation CHEST_GUI_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");

    // Dimensions
    public static final int CHEST_W = 176;
    public static final int CHEST_H = 222;
    public static final float SECTION_SIZE = 18.0f;

    // Grid (6 Rows x 9 Columns)
    public static final int ROWS = 6;
    public static final int COLS = 9;
    public static final int[][] TILE_GRID = new int[ROWS][COLS];

    static {
        for (int c = 0; c < COLS; c++) {
            TILE_GRID[5][c] = 1;
        }
    }

    // Player AABB Bounds
    public static final float PLAYER_WIDTH = 10.0f;
    public static final float PLAYER_HEIGHT = 28.0f;

    // Movement & Subpixel Interpolation States
    public static float playerX = 0;
    public static float playerY = 0;
    public static float prevPlayerX = 0;
    public static float prevPlayerY = 0;

    private float velocityX = 0;
    private float velocityY = 0;
    private boolean isOnGround = false;

    // Torso rotation state
    private static float currentBodyYaw = 0.0f;

    // Tuned Physics Constants for Organic Smooth Movement
    private static final float GRAVITY = 0.42f;
    private static final float JUMP_STRENGTH = -4.3f;
    private static final float ACCELERATION = 0.45f; // Smooth acceleration ramp
    private static final float FRICTION = 0.80f;     // Natural deceleration inertia
    private static final float MAX_SPEED = 2.00f;     // Smooth max walk speed
    private static final int PLAYER_SCALE = 18;

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

        if (this.minecraft != null) {
            this.minecraft.getTutorial().setStep(TutorialSteps.NONE);
            this.minecraft.getSoundManager().pause();
            this.minecraft.options.menuBackgroundBlurriness().set(0);
        }

        int guiX = (this.width - CHEST_W) / 2;
        int guiY = (this.height - CHEST_H) / 2;
        float gridStartY = guiY + 18;

        if (playerX == 0 && playerY == 0) {
            playerX = guiX + (CHEST_W / 2.0f);
            playerY = gridStartY + (5 * SECTION_SIZE);
            prevPlayerX = playerX;
            prevPlayerY = playerY;
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
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.minecraft != null) {
            this.minecraft.options.menuBackgroundBlurriness().set(0);
            if (this.minecraft.player != null) {
                LocalPlayer p = this.minecraft.player;
                p.getAbilities().invulnerable = true;
                p.setHealth(p.getMaxHealth());
                p.getFoodData().setFoodLevel(20);
            }
        }

        updatePhysics();
    }

    private void updatePhysics() {
        prevPlayerX = playerX;
        prevPlayerY = playerY;

        int guiX = (this.width - CHEST_W) / 2;
        int guiY = (this.height - CHEST_H) / 2;
        float gridStartX = guiX + 8;
        float gridStartY = guiY + 18;

        // 1. Horizontal Movement & Smooth Inertia
        if (keyLeft) velocityX -= ACCELERATION;
        if (keyRight) velocityX += ACCELERATION;
        velocityX *= FRICTION;
        velocityX = Mth.clamp(velocityX, -MAX_SPEED, MAX_SPEED);

        playerX += velocityX;

        if (collides(playerX, playerY, gridStartX, gridStartY)) {
            if (velocityX > 0) {
                playerX = snapToBlockLeft(playerX, gridStartX) - (PLAYER_WIDTH / 2.0f);
            } else if (velocityX < 0) {
                playerX = snapToBlockRight(playerX, gridStartX) + (PLAYER_WIDTH / 2.0f);
            }
            velocityX = 0;
        }

        // Screen boundary walls
        float minX = gridStartX + (PLAYER_WIDTH / 2.0f);
        float maxX = gridStartX + (COLS * SECTION_SIZE) - (PLAYER_WIDTH / 2.0f);
        if (playerX < minX) { playerX = minX; velocityX = 0; }
        if (playerX > maxX) { playerX = maxX; velocityX = 0; }

        // 2. Vertical Movement & Collision
        if (keyJump && isOnGround) {
            velocityY = JUMP_STRENGTH;
            isOnGround = false;
        }

        velocityY += GRAVITY;
        playerY += velocityY;
        isOnGround = false;

        if (collides(playerX, playerY, gridStartX, gridStartY)) {
            if (velocityY > 0) {
                playerY = snapToBlockTop(playerY, gridStartY);
                velocityY = 0;
                isOnGround = true;
            } else if (velocityY < 0) {
                playerY = snapToBlockBottom(playerY, gridStartY) + PLAYER_HEIGHT;
                velocityY = 0;
            }
        }
    }

    private boolean collides(float px, float py, float gridX, float gridY) {
        float pMinX = px - (PLAYER_WIDTH / 2.0f);
        float pMaxX = px + (PLAYER_WIDTH / 2.0f);
        float pMinY = py - PLAYER_HEIGHT;
        float pMaxY = py;

        int startCol = Mth.clamp((int) Math.floor((pMinX - gridX) / SECTION_SIZE), 0, COLS - 1);
        int endCol   = Mth.clamp((int) Math.floor((pMaxX - gridX) / SECTION_SIZE), 0, COLS - 1);
        int startRow = Mth.clamp((int) Math.floor((pMinY - gridY) / SECTION_SIZE), 0, ROWS - 1);
        int endRow   = Mth.clamp((int) Math.floor((pMaxY - gridY) / SECTION_SIZE), 0, ROWS - 1);

        for (int r = startRow; r <= endRow; r++) {
            for (int c = startCol; c <= endCol; c++) {
                if (TILE_GRID[r][c] != 0) {
                    float bMinX = gridX + (c * SECTION_SIZE);
                    float bMaxX = bMinX + SECTION_SIZE;
                    float bMinY = gridY + (r * SECTION_SIZE);
                    float bMaxY = bMinY + SECTION_SIZE;

                    if (pMaxX > bMinX && pMinX < bMaxX && pMaxY > bMinY && pMinY < bMaxY) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private float snapToBlockTop(float py, float gridY) {
        int r = (int) Math.floor((py - gridY) / SECTION_SIZE);
        return gridY + (r * SECTION_SIZE);
    }

    private float snapToBlockBottom(float py, float gridY) {
        float pMinY = py - PLAYER_HEIGHT;
        int r = (int) Math.floor((pMinY - gridY) / SECTION_SIZE);
        return gridY + ((r + 1) * SECTION_SIZE);
    }

    private float snapToBlockLeft(float px, float gridX) {
        float pMaxX = px + (PLAYER_WIDTH / 2.0f);
        int c = (int) Math.floor((pMaxX - gridX) / SECTION_SIZE);
        return gridX + (c * SECTION_SIZE);
    }

    private float snapToBlockRight(float px, float gridX) {
        float pMinX = px - (PLAYER_WIDTH / 2.0f);
        int c = (int) Math.floor((pMinX - gridX) / SECTION_SIZE);
        return gridX + ((c + 1) * SECTION_SIZE);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new PauseScreen(true));
            }
            return true;
        }

        if (ExampleMod.TOGGLE_NAMETAG_KEY.matches(keyCode, scanCode)) {
            boolean current = Config.SHOW_NAMETAG.get();
            Config.SHOW_NAMETAG.set(!current);
            Config.SHOW_NAMETAG.save();
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
        // ONLY Space key triggers jump (W/Up Key disabled)
        if (keyCode == GLFW.GLFW_KEY_SPACE) {
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
        if (keyCode == GLFW.GLFW_KEY_SPACE) {
            keyJump = false;
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.minecraft != null && this.minecraft.gameRenderer != null) {
            this.minecraft.gameRenderer.shutdownEffect();
        }

        render2DScene(guiGraphics, this.width, this.height, mouseX, mouseY, partialTick);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public static void render2DScene(GuiGraphics guiGraphics, int screenWidth, int screenHeight, int mouseX, int mouseY, float partialTick) {
        // Dirt Background
        int tileSize = 16;
        for (int x = 0; x < screenWidth; x += tileSize) {
            for (int y = 0; y < screenHeight; y += tileSize) {
                guiGraphics.blit(DIRT_TEXTURE, x, y, 0, 0, tileSize, tileSize, tileSize, tileSize);
            }
        }

        int guiX = (screenWidth - CHEST_W) / 2;
        int guiY = (screenHeight - CHEST_H) / 2;
        float gridStartX = guiX + 8;
        float gridStartY = guiY + 18;

        // Chest GUI
        guiGraphics.blit(CHEST_GUI_TEXTURE, guiX, guiY, 0, 0, CHEST_W, CHEST_H, 256, 256);

        // Render Dynamic Tile Grid Blocks
        ItemStack grassStack = new ItemStack(Items.GRASS_BLOCK);
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (TILE_GRID[r][c] == 1) {
                    int blockX = Math.round(gridStartX + (c * SECTION_SIZE));
                    int blockY = Math.round(gridStartY + (r * SECTION_SIZE));
                    guiGraphics.renderItem(grassStack, blockX, blockY);
                }
            }
        }

        // Sub-Pixel Float Coordinates
        float smoothX = Mth.lerp(partialTick, prevPlayerX, playerX);
        float smoothY = Mth.lerp(partialTick, prevPlayerY, playerY);

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            // Update limb animations based on actual delta speed
            float walkDelta = Math.abs(playerX - prevPlayerX);
            player.walkAnimation.update(walkDelta * 1.6f, 0.4f);

            // Save Vanilla Rotations
            float oldYRot = player.getYRot();
            float oldXRot = player.getXRot();
            float oldYHeadRot = player.yHeadRot;
            float oldYBodyRot = player.yBodyRotO;

            // Align Head & Torso with Mouse
            float eyeY = smoothY - 21.0f;
            float dx = mouseX - smoothX;
            float dy = mouseY - eyeY;

            float targetHeadYaw = (float) Math.toDegrees(Math.atan2(dx, 40));
            float targetPitch = (float) Math.toDegrees(Math.atan2(-dy, 40));

            float headBodyDiff = Mth.wrapDegrees(targetHeadYaw - currentBodyYaw);
            if (Math.abs(headBodyDiff) > 40.0f) {
                currentBodyYaw += (headBodyDiff > 0 ? headBodyDiff - 40.0f : headBodyDiff + 40.0f);
            }
            currentBodyYaw = Mth.lerp(0.15f, currentBodyYaw, targetHeadYaw);

            player.setYRot(currentBodyYaw);
            player.setXRot(-targetPitch);
            player.yHeadRot = targetHeadYaw;
            player.yBodyRot = currentBodyYaw;

            // Render Player with positive Z offset (+100.0f) to fix invisibility
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(smoothX, smoothY, 100.0f);

            // Compute mouse coordinates relative to the translated origin
            float relMouseX = (float) mouseX - smoothX;
            float relMouseY = (float) mouseY - eyeY;

            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    guiGraphics,
                    -25, -42, 25, 0,
                    PLAYER_SCALE,
                    0.0625f,
                    relMouseX,
                    relMouseY,
                    player
            );

            guiGraphics.pose().popPose();

            // Restore Vanilla Rotations
            player.setYRot(oldYRot);
            player.setXRot(oldXRot);
            player.yHeadRot = oldYHeadRot;
            player.yBodyRot = oldYBodyRot;

            // Elevated Nametag (+100.0f Z offset to match entity)
            if (Config.SHOW_NAMETAG.get()) {
                Component name = player.getDisplayName();
                int textWidth = Minecraft.getInstance().font.width(name);

                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(smoothX, smoothY - 52.0f, 100.0f);
                guiGraphics.pose().scale(0.60f, 0.60f, 1.0f);

                int scaledX = -(textWidth / 2);
                int scaledY = 0;

                guiGraphics.fill(scaledX - 2, scaledY - 2, scaledX + textWidth + 2, scaledY + 9, 0x80000000);
                guiGraphics.drawString(Minecraft.getInstance().font, name, scaledX, scaledY, 0xFFFFFFFF, false);

                guiGraphics.pose().popPose();
            }
        }
    }
}
