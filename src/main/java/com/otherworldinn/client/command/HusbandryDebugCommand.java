package com.otherworldinn.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.otherworldinn.world.husbandry.HusbandryData;
import com.otherworldinn.world.husbandry.HusbandryService;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;

/**
 * 调试用的客户端命令 {@code /innhusbandry}：打印准星所指动物在**客户端**读到的好感度，
 * 用来确认 {@code S2CHusbandrySyncPacket} 是否真的把值送到了客户端。
 * 后续有了 HUD 就可以删掉本类。
 */
public final class HusbandryDebugCommand {
    private HusbandryDebugCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("innhusbandry").executes(HusbandryDebugCommand::report));
    }

    private static int report(CommandContext<CommandSourceStack> context) {
        Entity target = Minecraft.getInstance().crosshairPickEntity;
        if (!(target instanceof Animal animal)) {
            context.getSource().sendFailure(Component.literal("准星没有对准养殖动物"));
            return 0;
        }
        int affection = HusbandryService.getAffection(animal);
        float hearts = affection / (float) HusbandryData.AFFECTION_PER_HEART;
        context.getSource()
                .sendSuccess(
                        () ->
                                Component.literal("[客户端] ")
                                        .append(animal.getName())
                                        .append(
                                                Component.literal(
                                                                " 好感="
                                                                        + affection
                                                                        + "  心数="
                                                                        + String.format(java.util.Locale.ROOT, "%.2f", hearts)
                                                                        + "/"
                                                                        + HusbandryData.MAX_HEARTS)
                                                        .withStyle(ChatFormatting.GRAY)),
                        false);
        return 1;
    }
}
