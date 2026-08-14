package com.example.examplemod;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
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

        if (this.minecraft != null) {
            this.minecraft.options.menuBackgroundBlurriness().set(0);
            LocalPlayer player = this.minecraft.player;
            if (player != null) {
                player.getAbilities().invulnerable = true;
                player.setHealth(player.getMaxHealth());
                player.getFoodData().setFoodLevel(20);

                updateRealPlayerPhysics(player);
            }
        }
    }

    private void updateRealPlayerPhysics(LocalPlayer player) {
        Vec3 vel = player.getDeltaMovement();
        double moveSpeed = 0.22;
        double targetVX = 0;

        if (keyLeft) targetVX = -moveSpeed;
        if (keyRight) targetVX = moveSpeed;

        // Apply real horizontal movement to actual player
        player.setDeltaMovement(targetVX, vel.y, 0);

        // Real jump using vanilla physics
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
            // Calculate interpolated real world displacement
            double lerpWorldX = Mth.lerp(partialTick, player.xo, player.getX());
            double lerpWorldY = Mth.lerp(partialTick, player.yo, player.getY());

            double deltaX = lerpWorldX - startWorldX;
            double deltaY = lerpWorldY - startWorldY;

            // Map real-world displacement to chest grid coordinates
            float baseChestX = guiX + (CHEST_W / 2.0f);
            float baseChestY = gridStartY + (5 * SECTION_SIZE);

            float smoothX = baseChestX + (float) (deltaX * SECTION_SIZE);
            float smoothY = baseChestY - (float) (deltaY * SECTION_SIZE);

            // Bounds clamping inside chest
            float minX = gridStartX + 8.0f;
            float maxX = gridStartX + (COLS * SECTION_SIZE) - 8.0f;
            smoothX = Mth.clamp(smoothX, minX, maxX);

            // Render REAL player model directly with full live walk/jump animations
            renderLivePlayerModel(guiGraphics, smoothX, smoothY, 20, player, partialTick);

            // Elevated Nametag Render
            if (Config.SHOW_NAMETAG.get()) {
                Component name = player.getDisplayName();
                int textWidth = Minecraft.getInstance().font.width(name);

                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(smoothX, smoothY - 48.0f, 200.0f);
                guiGraphics.pose().scale(0.60f, 0.60f, 1.0f);

                int scaledX = -(textWidth / 2);
                int scaledY = 0;

                guiGraphics.fill(scaledX - 2, scaledY - 2, scaledX + textWidth + 2, scaledY + 9, 0x80000000);
                guiGraphics.drawString(Minecraft.getInstance().font, name, scaledX, scaledY, 0xFFFFFFFF, false);

                guiGraphics.pose().popPose();
            }
        }
    }

    private static void renderLivePlayerModel(GuiGraphics guiGraphics, float x, float y, int scale, LocalPlayer player, float partialTick) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 150.0F);
        guiGraphics.pose().scale((float) scale, (float) scale, (float) scale);

        // Align facing angle straight toward camera in 2D chest GUI
        guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(180.0F));
        guiGraphics.pose().mulPose(Axis.YP.rotationDegrees(180.0F));

        Lighting.setupForEntityInInventory(new Quaternionf());

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        dispatcher.setRenderShadow(false);

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        // Direct Entity Render Call (preserves real leg/arm walk animations & jump physics)
        dispatcher.render(
                player,
                0.0, 0.0, 0.0,
                0.0F,
                partialTick,
                guiGraphics.pose(),
                bufferSource,
                15728880
        );

        bufferSource.endBatch();
        dispatcher.setRenderShadow(true);
        Lighting.setupFor3DItems();

        guiGraphics.pose().popPose();
    }
}
