package com.otherworldinn.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.otherworldinn.client.snow.SnowCoverController;
import com.otherworldinn.client.snow.SnowCoverRenderer;
import com.otherworldinn.world.snow.SnowCoverState;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 客户端命令 {@code /innsnow [on|off|toggle|force|status|debug]}。
 *
 * <p>{@code status} 打印开关状态与客户端镜像里有多少区块带数据；
 * {@code debug} 把准星所指那一列的判定链打到聊天栏，用于排查"这里为什么没雪"。
 */
public final class SnowCoverCommand {
    private SnowCoverCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("innsnow")
                .executes(SnowCoverCommand::toggle)
                .then(Commands.literal("on").executes(context -> apply(context, true)))
                .then(Commands.literal("off").executes(context -> apply(context, false)))
                .then(Commands.literal("toggle").executes(SnowCoverCommand::toggle))
                .then(Commands.literal("force")
                        .executes(context -> force(context, !SnowCoverController.isForced()))
                        .then(Commands.literal("on").executes(context -> force(context, true)))
                        .then(Commands.literal("off").executes(context -> force(context, false))))
                .then(Commands.literal("status").executes(SnowCoverCommand::status))
                .then(Commands.literal("debug").executes(SnowCoverCommand::debug)));
    }

    private static int toggle(CommandContext<CommandSourceStack> context) {
        return apply(context, !SnowCoverController.isEnabled());
    }

    private static int apply(CommandContext<CommandSourceStack> context, boolean enabled) {
        SnowCoverController.setEnabled(enabled);
        context.getSource()
                .sendSuccess(() -> Component.literal("[Otherworld Inn] 真实覆雪：" + (enabled ? "开启" : "关闭")), false);
        return 1;
    }

    private static int force(CommandContext<CommandSourceStack> context, boolean force) {
        SnowCoverController.setForced(force);
        context.getSource().sendSuccess(
                () -> Component.literal("[Otherworld Inn] 强制覆雪：" + (force ? "开启（无视天气）" : "关闭（按天气/温度/光照）")),
                false);
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            context.getSource().sendFailure(Component.literal("不在世界里"));
            return 0;
        }
        BlockPos at = minecraft.player.blockPosition();
        BlockPos target = minecraft.hitResult instanceof BlockHitResult hit ? hit.getBlockPos() : at;
        String[] lines = {
            "[Otherworld Inn] 真实覆雪 " + (SnowCoverController.isEnabled() ? "开" : "关")
                    + "／强制覆雪 " + (SnowCoverController.isForced() ? "开（无视天气）" : "关（按天气＋服务端数据）"),
            "客户端镜像：有数据的区块 " + SnowCoverState.coveredChunks()
                    + " 个；玩家所在块 " + SnowCoverState.coveredCount(at.getX() >> 4, at.getZ() >> 4) + " 列",
            "准星所在列 (" + target.getX() + ", " + target.getZ() + ") 覆盖=" + SnowCoverState.isCovered(target.getX(), target.getZ()),
        };
        for (String line : lines) {
            context.getSource().sendSuccess(() -> Component.literal(line), false);
        }
        return 1;
    }

    private static int debug(CommandContext<CommandSourceStack> context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            context.getSource().sendFailure(Component.literal("不在世界里"));
            return 0;
        }
        BlockPos target;
        if (minecraft.hitResult instanceof BlockHitResult blockHit) {
            target = blockHit.getBlockPos();
        } else {
            target = minecraft.player.blockPosition().below();
        }
        String report = SnowCoverRenderer.debugColumn(minecraft.level, target);
        for (String line : report.split("\\n")) {
            context.getSource().sendSuccess(() -> Component.literal(line), false);
        }
        return 1;
    }
}
