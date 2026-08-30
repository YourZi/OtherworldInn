package com.otherworldinn.item;

import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.util.AdvancementUtils;
import com.otherworldinn.world.inn.InnData;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import com.simibubi.create.content.redstone.deskBell.DeskBellBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class InnKeyItem extends Item {
    public InnKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof DeskBellBlockEntity) {
            Player player = context.getPlayer();

            // 客户端直接返回成功，以便触发手部动画
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }

            if (player instanceof ServerPlayer serverPlayer) {
                TeamData team = TeamManager.getInstance().getTeamAt(pos, level.getServer());
                if (team != null) {
                    // 权限模型：队伍成员（含地契持有者）可开关旅社
                    if (team.hasMember(player.getUUID())) {
                        InnData innData = team.getInnData();
                        InnData.InnState currentState = innData.getState();
                        InnData.InnState newState;
                        SoundEvent sound = null;
                        MutableComponent message;
                        int color;

                        if (currentState == InnData.InnState.OPEN) {
                            sound = SoundEvents.WOODEN_DOOR_OPEN;
                            newState = InnData.InnState.CLOSED;
                            message =
                                    Component.translatable("message.otherworldinn.inn_key.closed");
                            color = ModColors.RED;
                        } else {
                            sound = SoundEvents.WOODEN_DOOR_CLOSE;
                            newState = InnData.InnState.OPEN;
                            message = Component.translatable("message.otherworldinn.inn_key.open");
                            color = ModColors.GREEN;
                        }

                        innData.setState(newState);
                        if (newState == InnData.InnState.OPEN) {
                            AdvancementUtils.award(serverPlayer, AdvancementUtils.OPEN_FIRST_INN);
                        }
                        level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
                        TeamManager.getInstance().syncTeam(team, level.getServer());
                        serverPlayer.displayClientMessage(
                                message.withStyle(style -> style.withColor(color)), true);
                    } else {
                        player.displayClientMessage(
                                Component.translatable(
                                        "message.otherworldinn.inn_key.no_permission"),
                                true);
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.useOn(context);
    }
}
