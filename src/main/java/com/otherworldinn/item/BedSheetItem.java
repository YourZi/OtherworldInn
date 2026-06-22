package com.otherworldinn.item;

import com.otherworldinn.foundation.ModBlockProperties;
import com.otherworldinn.init.ModItems;
import com.otherworldinn.world.inn.InnData;
import com.otherworldinn.world.inn.RoomData;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

/**
 * 床单物品
 *
 * <p>用于清理脏乱的床。 右键点击脏乱的床时，消耗耐久并将床变干净，同时给予玩家一个脏乱的床单物品。
 */
public class BedSheetItem extends Item {

    public BedSheetItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        // 检查目标是否为脏乱的床
        if (state.getBlock() instanceof BedBlock
                && state.hasProperty(ModBlockProperties.MESSY)
                && state.getValue(ModBlockProperties.MESSY)) {

            if (!level.isClientSide) {
                if (level instanceof ServerLevel serverLevel && cleanMessyBed(serverLevel, pos)) {
                    ItemStack dirtySheet = createDirtySheet(stack, 1);
                    if (stack.getCount() > 1) {
                        stack.shrink(1);
                        if (!player.getInventory().add(dirtySheet)) {
                            player.drop(dirtySheet, false);
                        }
                    } else {
                        player.setItemInHand(context.getHand(), dirtySheet);
                    }
                }
            } else {
                // 客户端效果
                level.playSound(
                        player, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                for (int i = 0; i < 5; i++) {
                    level.addParticle(
                            ParticleTypes.HAPPY_VILLAGER,
                            pos.getX() + 0.5 + (level.random.nextDouble() - 0.5),
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5),
                            0,
                            0,
                            0);
                }
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    public static boolean cleanMessyBed(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BedBlock)
                || !state.hasProperty(ModBlockProperties.MESSY)
                || !state.getValue(ModBlockProperties.MESSY)) {
            return false;
        }
        level.setBlock(pos, state.setValue(ModBlockProperties.MESSY, false), 3);
        BedPart part = state.getValue(BedBlock.PART);
        BlockPos otherPos =
                pos.relative(
                        part == BedPart.HEAD
                                ? state.getValue(BedBlock.FACING).getOpposite()
                                : state.getValue(BedBlock.FACING));
        BlockState otherState = level.getBlockState(otherPos);
        if (otherState.getBlock() instanceof BedBlock
                && otherState.hasProperty(ModBlockProperties.MESSY)
                && otherState.getValue(ModBlockProperties.MESSY)) {
            level.setBlock(otherPos, otherState.setValue(ModBlockProperties.MESSY, false), 3);
        }
        TeamData team = TeamManager.getInstance().getTeamAt(pos, level.getServer());
        if (team != null) {
            InnData innData = team.getInnData();
            RoomData room = innData.getRoomAt(pos);
            if (room != null) {
                innData.checkAndUpdateRoom(room.getId(), level, team);
                TeamManager.getInstance().syncTeam(team, level.getServer());
            }
        }
        return true;
    }

    public static ItemStack createDirtySheet(ItemStack source, int extraDamage) {
        ItemStack dirtySheet = new ItemStack(ModItems.MESSY_BED_SHEET.get());
        int damage = Math.max(0, source.getDamageValue() + Math.max(0, extraDamage));
        dirtySheet.setDamageValue(Math.min(dirtySheet.getMaxDamage(), damage));
        return dirtySheet;
    }
}
