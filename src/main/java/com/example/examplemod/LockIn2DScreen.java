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
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class LockIn2DScreen extends Screen {
    // Textures
    public static final ResourceLocation DIRT_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/dirt.png");
    public static final ResourceLocation CHEST_GUI_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");

    // Dimensions
    public static final int CHEST_W = 176;
    public static final int CHEST_H = 222;
    public static final float SECTION_SIZE = 18.0f;

    // Grid System (6 Rows x 9 Columns)
    public static final int ROWS = 6;
    public static final int COLS = 9;
    public static final int[][] TILE_GRID = new int[ROWS][COLS];

    static {
        for (int c = 0; c < COLS; c++) {
            TILE_GRID[5][c] = 1; // Floor row
        }
    }

    // World Spawn Anchors
    private static double startWorldX = Double.NaN;
    private static double startWorldY = Double.NaN;
    private static double startWorldZ = Double.NaN;

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

            LocalPlayer player = this.minecraft.player;
            if (player != null && Double.isNaN(startWorldX)) {
                startWorldX = player.getX();
                startWorldY = player.getY();
                startWorldZ = player.getZ();
            }
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

        if (this.minecraft != null && this.minecraft.player != null) {
            LocalPlayer player = this.minecraft.player;
            player.getAbilities().invulnerable = true;
            player.setHealth(player.getMaxHealth());
            player.getFoodData().setFoodLevel(20);

            updateRealPlayerPhysics(player);
        }
    }

    private void updateRealPlayerPhysics(LocalPlayer player) {
        if (Double.isNaN(startWorldX)) return;

        int guiX = (this.width - CHEST_W) / 2;
        float gridStartX = guiX + 8;

        // Chest grid border limits (in pixels)
        float minScreenX = gridStartX + 10.0f;
        float maxScreenX = gridStartX + (COLS * SECTION_SIZE) - 10.0f;
        float baseChestX = guiX + (CHEST_W / 2.0f);

        // Convert screen pixel limits to exact 3D world offsets
        double minWorldX = startWorldX + (minScreenX - baseChestX) / SECTION_SIZE;
        double maxWorldX = startWorldX + (maxScreenX - baseChestX) / SECTION_SIZE;

        Vec3 vel = player.getDeltaMovement();
        double moveSpeed = 0.22;
        double targetVX = 0;

        if (keyLeft) targetVX = -moveSpeed;
        if (keyRight) targetVX = moveSpeed;

        // Clamp 3D position BEFORE applying movement to prevent wall clipping
        double currentX = player.getX();
        if (currentX <= minWorldX && targetVX < 0) {
            targetVX = 0;
            player.setPos(minWorldX, player.getY(), startWorldZ);
        } else if (currentX >= maxWorldX && targetVX > 0) {
            targetVX = 0;
            player.setPos(maxWorldX, player.getY(), startWorldZ);
        }

        // Lock depth on Z axis to prevent 3D jitter
        player.setPos(Mth.clamp(player.getX(), minWorldX, maxWorldX), player.getY(), startWorldZ);
        player.setDeltaMovement(targetVX, vel.y, 0);

        // Jump physics
        if (keyJump && player.onGround()) {
            player.jumpFromGround();
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
        render2DScene(guiGraphics, this.width, this.height, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public static void render2DScene(GuiGraphics guiGraphics, int screenWidth, int screenHeight, int mouseX, int mouseY, float partialTick) {
        // Dirt Background Tiling
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

        // Double Chest GUI Background
        guiGraphics.blit(CHEST_GUI_TEXTURE, guiX, guiY, 0, 0, CHEST_W, CHEST_H, 256, 256);

        // Tile Grid Grass Blocks
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

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && !Double.isNaN(startWorldX)) {
            // Smooth Sub-pixel Interpolation via partialTick
            double lerpWorldX = Mth.lerp(partialTick, player.xo, player.getX());
            double lerpWorldY = Mth.lerp(partialTick, player.yo, player.getY());

            double deltaX = lerpWorldX - startWorldX;
            double deltaY = lerpWorldY - startWorldY;

            // Map 3D world position to 2D screen coordinates
            float baseChestX = guiX + (CHEST_W / 2.0f);
            float baseChestY = gridStartY + (5 * SECTION_SIZE);

            float smoothX = baseChestX + (float) (deltaX * SECTION_SIZE);
            float smoothY = baseChestY - (float) (deltaY * SECTION_SIZE);

            // Strict boundary clamping on screen
            float minX = gridStartX + 10.0f;
            float maxX = gridStartX + (COLS * SECTION_SIZE) - 10.0f;
            smoothX = Mth.clamp(smoothX, minX, maxX);

            // Extract integer baseline and sub-pixel float remainder
            int intX = (int) Math.floor(smoothX);
            int intY = (int) Math.floor(smoothY);
            float fracX = smoothX - intX;
            float fracY = smoothY - intY;

            // Matrix translation handles sub-pixel precision while passing int arguments to MC renderer
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(fracX, fracY, 0.0f);

            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    guiGraphics,
                    intX - 25, intY - 45,
                    intX + 25, intY,
                    PLAYER_SCALE,
                    0.0625f,
                    (float) mouseX - fracX,
                    (float) mouseY - fracY,
                    player
            );

            guiGraphics.pose().popPose();

            // Elevated Nametag
            if (Config.SHOW_NAMETAG.get()) {
                Component name = player.getDisplayName();
                int textWidth = Minecraft.getInstance().font.width(name);

                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(smoothX, smoothY - 48.0f, 100.0f);
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
