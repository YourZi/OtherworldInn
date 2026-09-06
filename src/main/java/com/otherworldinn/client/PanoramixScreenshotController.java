package com.otherworldinn.client;

import com.otherworldinn.OtherworldInn;
import java.io.File;
import java.io.IOException;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

@EventBusSubscriber(modid = OtherworldInn.MODID, value = Dist.CLIENT)
public final class PanoramixScreenshotController {
    private static final int PANORAMA_SIZE = 1024;
    private static final int DEFAULT_CAPTURE_WARMUP_FRAMES = 20;
    private static final int DEFAULT_CAPTURE_FOV = 90;

    private static boolean active = false;
    private static int faceIndex = 0;
    private static int warmupFrames = 0;
    private static int captureWarmupFrames = DEFAULT_CAPTURE_WARMUP_FRAMES;
    private static File outputDir;

    private static int originalWindowWidth;
    private static int originalWindowHeight;
    private static int originalFov;
    private static float originalYaw;
    private static float originalPitch;
    private static float originalYawOld;
    private static float originalPitchOld;

    private PanoramixScreenshotController() {}

    public static int start(CommandSourceStack source, File directory) {
        return start(source, directory, DEFAULT_CAPTURE_WARMUP_FRAMES, DEFAULT_CAPTURE_FOV);
    }

    public static int start(CommandSourceStack source, File directory, int delayFrames, int fov) {
        Minecraft minecraft = Minecraft.getInstance();
        if (active) {
            source.sendFailure(Component.translatable("command.otherworldinn.panoramix.busy"));
            return 0;
        }
        if (minecraft.player == null || minecraft.level == null) {
            source.sendFailure(Component.translatable("command.otherworldinn.panoramix.unavailable"));
            return 0;
        }

        outputDir = directory;
        originalWindowWidth = minecraft.getWindow().getWidth();
        originalWindowHeight = minecraft.getWindow().getHeight();
        originalFov = minecraft.options.fov().get();
        originalYaw = minecraft.player.getYRot();
        originalPitch = minecraft.player.getXRot();
        originalYawOld = minecraft.player.yRotO;
        originalPitchOld = minecraft.player.xRotO;

        active = true;
        faceIndex = 0;
        captureWarmupFrames = delayFrames;
        minecraft.options.fov().set(fov);
        minecraft.getWindow().setWidth(PANORAMA_SIZE);
        minecraft.getWindow().setHeight(PANORAMA_SIZE);
        minecraft.resizeDisplay();
        applyFace(minecraft, faceIndex);
        warmupFrames = captureWarmupFrames;

        source.sendSuccess(() -> Component.translatable("command.otherworldinn.panoramix.start"), false);
        return 1;
    }

    @SubscribeEvent
    public static void onRenderFramePost(RenderFrameEvent.Post event) {
        if (!active) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || outputDir == null) {
            abort(minecraft);
            return;
        }
        if (warmupFrames > 0) {
            warmupFrames--;
            return;
        }

        captureFace(minecraft, faceIndex);
        if (faceIndex >= 5) {
            finish(minecraft);
            return;
        }

        faceIndex++;
        applyFace(minecraft, faceIndex);
        warmupFrames = captureWarmupFrames;
    }

    private static void applyFace(Minecraft minecraft, int face) {
        switch (face) {
            case 0 -> {
                minecraft.player.setYRot(originalYaw);
                minecraft.player.setXRot(0.0F);
            }
            case 1 -> {
                minecraft.player.setYRot((originalYaw + 90.0F) % 360.0F);
                minecraft.player.setXRot(0.0F);
            }
            case 2 -> {
                minecraft.player.setYRot((originalYaw + 180.0F) % 360.0F);
                minecraft.player.setXRot(0.0F);
            }
            case 3 -> {
                minecraft.player.setYRot((originalYaw - 90.0F) % 360.0F);
                minecraft.player.setXRot(0.0F);
            }
            case 4 -> {
                minecraft.player.setYRot(originalYaw);
                minecraft.player.setXRot(-90.0F);
            }
            default -> {
                minecraft.player.setYRot(originalYaw);
                minecraft.player.setXRot(90.0F);
            }
        }
        minecraft.player.yRotO = minecraft.player.getYRot();
        minecraft.player.xRotO = minecraft.player.getXRot();
    }

    private static void captureFace(Minecraft minecraft, int face) {
        var image = Screenshot.takeScreenshot(minecraft.getMainRenderTarget());
        File target = new File(outputDir, "panorama_" + face + ".png");
        Util.ioPool()
                .execute(
                        () -> {
                            try {
                                image.writeToFile(target);
                            } catch (IOException exception) {
                                OtherworldInn.LOGGER.warn("Failed to save panoramix screenshot {}", target, exception);
                            } finally {
                                image.close();
                            }
                        });
    }

    private static void finish(Minecraft minecraft) {
        restore(minecraft);
        MutableComponent folder =
                Component.literal(outputDir.getName())
                        .withStyle(ChatFormatting.UNDERLINE)
                        .withStyle(
                                style ->
                                        style.withClickEvent(
                                                new ClickEvent(
                                                        ClickEvent.Action.OPEN_FILE,
                                                        outputDir.getAbsolutePath())));
        minecraft.gui.getChat().addMessage(Component.translatable("command.otherworldinn.panoramix.finished", folder));
        active = false;
        outputDir = null;
    }

    private static void abort(Minecraft minecraft) {
        restore(minecraft);
        minecraft.gui.getChat().addMessage(Component.translatable("command.otherworldinn.panoramix.aborted"));
        active = false;
        outputDir = null;
    }

    private static void restore(Minecraft minecraft) {
        if (minecraft.player != null) {
            minecraft.player.setYRot(originalYaw);
            minecraft.player.setXRot(originalPitch);
            minecraft.player.yRotO = originalYawOld;
            minecraft.player.xRotO = originalPitchOld;
        }
        minecraft.options.fov().set(originalFov);
        minecraft.getWindow().setWidth(originalWindowWidth);
        minecraft.getWindow().setHeight(originalWindowHeight);
        minecraft.resizeDisplay();
    }
}
