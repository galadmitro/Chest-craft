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

    // Exact Vanilla Double Chest Dimensions
    public static final int CHEST_W = 176;
    public static final int CHEST_H = 222;

    // Grid Metrics (1 Section / Block Slot = 18 Pixels)
    public static final float SECTION_SIZE = 18.0f;

    // 2D Physics State (Current and Previous for High-FPS Interpolation)
    public static float playerX = 0;
    public static float playerY = 0;
    public static float prevPlayerX = 0;
    public static float prevPlayerY = 0;

    private float velocityX = 0;
    private float velocityY = 0;
    private boolean isOnGround = false;

    // Movement Physics
    private static final float GRAVITY = 0.45f;
    private static final float JUMP_STRENGTH = -4.5f; // Max jump height: 1.25 blocks
    private static final float ACCELERATION = 0.65f;
    private static final float FRICTION = 0.72f;
    private static final float MAX_SPEED = 2.15f;
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

        if (playerX == 0 && playerY == 0) {
            playerX = guiX + (CHEST_W / 2.0f);
            playerY = guiY + 108.0f + (SECTION_SIZE * 0.5f);
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
        // Save previous tick coordinates for frame interpolation
        prevPlayerX = playerX;
        prevPlayerY = playerY;

        int guiX = (this.width - CHEST_W) / 2;
        int guiY = (this.height - CHEST_H) / 2;

        if (keyLeft) velocityX -= ACCELERATION;
        if (keyRight) velocityX += ACCELERATION;
        velocityX *= FRICTION;

        velocityX = Mth.clamp(velocityX, -MAX_SPEED, MAX_SPEED);

        if (keyJump && isOnGround) {
            velocityY = JUMP_STRENGTH;
            isOnGround = false;
        }

        velocityY += GRAVITY;

        playerX += velocityX;
        playerY += velocityY;

        // Boundaries
        float minX = guiX + 16;
        float maxX = guiX + CHEST_W - 16;
        if (playerX < minX) playerX = minX;
        if (playerX > maxX) playerX = maxX;

        float floorY = guiY + 108.0f + (SECTION_SIZE * 0.5f);
        if (playerY >= floorY) {
            playerY = floorY;
            velocityY = 0;
            isOnGround = true;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new PauseScreen(true));
            }
            return true;
        }

        // Toggle nametag keybind (O key)
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
        if (this.minecraft != null && this.minecraft.gameRenderer != null) {
            this.minecraft.gameRenderer.shutdownEffect();
        }

        render2DScene(guiGraphics, this.width, this.height, mouseX, mouseY, partialTick);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public static void render2DScene(GuiGraphics guiGraphics, int screenWidth, int screenHeight, int mouseX, int mouseY, float partialTick) {
        // 1. Crisp Dirt Background
        int tileSize = 16;
        for (int x = 0; x < screenWidth; x += tileSize) {
            for (int y = 0; y < screenHeight; y += tileSize) {
                guiGraphics.blit(DIRT_TEXTURE, x, y, 0, 0, tileSize, tileSize, tileSize, tileSize);
            }
        }

        int guiX = (screenWidth - CHEST_W) / 2;
        int guiY = (screenHeight - CHEST_H) / 2;

        // 2. Chest GUI
        guiGraphics.blit(CHEST_GUI_TEXTURE, guiX, guiY, 0, 0, CHEST_W, CHEST_H, 256, 256);

        // 3. Row 6 Grass Blocks
        ItemStack grassStack = new ItemStack(Items.GRASS_BLOCK);
        int startX = guiX + 8;
        int floorY = guiY + 108;
        for (int i = 0; i < 9; i++) {
            guiGraphics.renderItem(grassStack, startX + (i * 18), floorY);
        }

        // 4. Smooth Frame-Interpolated Player Render Position
        float smoothX = Mth.lerp(partialTick, prevPlayerX, playerX);
        float smoothY = Mth.lerp(partialTick, prevPlayerY, playerY);

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            int intX = Math.round(smoothX);
            int intY = Math.round(smoothY);

            float oldYRot = player.getYRot();
            float oldXRot = player.getXRot();
            float oldYHeadRot = player.yHeadRot;
            float oldYBodyRot = player.yBodyRotO;

            float dx = mouseX - intX;
            float dy = mouseY - (intY - 20);
            float targetHeadYaw = (float) Math.toDegrees(Math.atan2(dx, 40));
            float targetPitch = (float) Math.toDegrees(Math.atan2(-dy, 40));
            float bodyYaw = Mth.clamp(targetHeadYaw, -35.0f, 35.0f);

            player.setYRot(targetHeadYaw);
            player.setXRot(-targetPitch);
            player.yHeadRot = targetHeadYaw;
            player.yBodyRot = bodyYaw;

            int x1 = intX - 25;
            int y1 = intY - 42;
            int x2 = intX + 25;
            int y2 = intY;

            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    guiGraphics,
                    x1, y1, x2, y2,
                    PLAYER_SCALE,
                    0.0625f,
                    (float) mouseX, (float) mouseY,
                    player
            );

            player.setYRot(oldYRot);
            player.setXRot(oldXRot);
            player.yHeadRot = oldYHeadRot;
            player.yBodyRot = oldYBodyRot;

            // 5. Compact Scaled Player Nametag
            if (Config.SHOW_NAMETAG.get()) {
                Component name = player.getDisplayName();
                int textWidth = Minecraft.getInstance().font.width(name);

                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate((float) intX, (float) (intY - 44), 0.0f);
                guiGraphics.pose().scale(0.65f, 0.65f, 1.0f);

                int scaledX = -(textWidth / 2);
                int scaledY = 0;

                guiGraphics.fill(scaledX - 2, scaledY - 2, scaledX + textWidth + 2, scaledY + 9, 0x80000000);
                guiGraphics.drawString(Minecraft.getInstance().font, name, scaledX, scaledY, 0xFFFFFFFF, false);

                guiGraphics.pose().popPose();
            }
        }
    }
}