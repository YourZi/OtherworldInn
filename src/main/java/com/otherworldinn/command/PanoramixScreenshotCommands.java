package com.otherworldinn.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.otherworldinn.OtherworldInn;
import com.otherworldinn.client.PanoramixScreenshotController;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class PanoramixScreenshotCommands {
    private static final DateTimeFormatter FOLDER_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private PanoramixScreenshotCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("panoramixScreenshot")
                        .executes(PanoramixScreenshotCommands::capturePanorama)
                        .then(
                                Commands.argument("delayFrames", IntegerArgumentType.integer(0))
                                        .executes(PanoramixScreenshotCommands::capturePanoramaWithDelay)
                                        .then(
                                                Commands.argument("fov", IntegerArgumentType.integer(1, 179))
                                                        .executes(
                                                                PanoramixScreenshotCommands
                                                                        ::capturePanoramaWithDelayAndFov))));
    }

    private static int capturePanorama(CommandContext<CommandSourceStack> context) {
        return capturePanorama(context, 20, 90);
    }

    private static int capturePanoramaWithDelay(CommandContext<CommandSourceStack> context) {
        return capturePanorama(context, IntegerArgumentType.getInteger(context, "delayFrames"), 90);
    }

    private static int capturePanoramaWithDelayAndFov(CommandContext<CommandSourceStack> context) {
        return capturePanorama(
                context,
                IntegerArgumentType.getInteger(context, "delayFrames"),
                IntegerArgumentType.getInteger(context, "fov"));
    }

    private static int capturePanorama(CommandContext<CommandSourceStack> context, int delayFrames, int fov) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            context.getSource()
                    .sendFailure(Component.translatable("command.otherworldinn.panoramix.unavailable"));
            return 0;
        }

        File outputDir =
                new File(
                        new File(minecraft.gameDirectory, "screenshots/panoramix"),
                        FOLDER_TIME_FORMAT.format(LocalDateTime.now()));
        try {
            Files.createDirectories(outputDir.toPath());
        } catch (IOException exception) {
            OtherworldInn.LOGGER.error("Failed to create panoramix screenshot directory", exception);
            context.getSource()
                    .sendFailure(Component.translatable("command.otherworldinn.panoramix.create_dir_failed"));
            return 0;
        }

        return PanoramixScreenshotController.start(context.getSource(), outputDir, delayFrames, fov);
    }
}
