package com.otherworldinn.world.event.listener;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.item.MarshmallowOnStickItem;
import com.otherworldinn.util.AdvancementUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** 棉花糖烤制：主/副手持有时持续右键点燃的营火累积烤熟度，烤满后继续烤则碳化成火把。 */
@EventBusSubscriber(modid = OtherworldInn.MODID)
public final class MarshmallowRoastHandler {

    private static final double CAMPFIRE_RANGE = 2.0D;
    /** 每 10 tick +1 烤熟度，0→100 共 50 秒。 */
    private static final int ROAST_INTERVAL_TICKS = 10;
    /** 客户端按住右键每 4 tick 重发交互包，留 1 tick 余量判定。 */
    private static final int USE_TIMEOUT_TICKS = 5;
    /** 最近一次右键营火的 tick，仅服务端读写。 */
    private static final String LAST_USE_TICK_KEY = "otherworldinn:marshmallow_use_tick";
    /** "焦香的棉花糖"阶段，首次烤到该阶段解锁成就。 */
    private static final int FRAGRANT_STAGE = 1;

    private MarshmallowRoastHandler() {}

    /** 消费右键营火交互以避免进入进食流程（保持按住并重发），并记录最近右键 tick 供 tick 判定。 */
    @SubscribeEvent
    public static void onUseItemOnBlock(UseItemOnBlockEvent event) {
        if (event.getUsePhase() != UseItemOnBlockEvent.UsePhase.ITEM_BEFORE_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        if (player == null || !(event.getItemStack().getItem() instanceof MarshmallowOnStickItem)) {
            return;
        }
        if (!isLitCampfire(event.getLevel(), event.getPos())) {
            return;
        }
        if (!event.getLevel().isClientSide) {
            player.getPersistentData().putInt(LAST_USE_TICK_KEY, player.tickCount);
        }
        event.cancelWithResult(ItemInteractionResult.CONSUME);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % ROAST_INTERVAL_TICKS != 0) {
            return;
        }
        int sinceUse = player.tickCount - player.getPersistentData().getInt(LAST_USE_TICK_KEY);
        if (sinceUse < 0 || sinceUse > USE_TIMEOUT_TICKS) {
            return;
        }

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!(stack.getItem() instanceof MarshmallowOnStickItem)) {
                continue;
            }
            if (!isLookingAtLitCampfire(player)) {
                continue;
            }

            int roast = MarshmallowOnStickItem.getRoast(stack);
            if (roast >= MarshmallowOnStickItem.MAX_ROAST) {
                // 烤到顶仍在烤：变为火把
                player.setItemInHand(hand, new ItemStack(Items.TORCH));
                player.level()
                        .playSound(
                                null,
                                player.blockPosition(),
                                SoundEvents.FIRE_EXTINGUISH,
                                SoundSource.PLAYERS,
                                0.7F,
                                0.8F);
                continue;
            }
            int stageBefore = MarshmallowOnStickItem.getHandStage(stack);
            MarshmallowOnStickItem.setRoast(stack, roast + 1);
            // 首次进入焦香阶段时解锁成就
            if (stageBefore < FRAGRANT_STAGE && MarshmallowOnStickItem.getHandStage(stack) == FRAGRANT_STAGE) {
                AdvancementUtils.award(player, AdvancementUtils.OUTER_WILDS);
            }
        }
    }

    private static boolean isLookingAtLitCampfire(ServerPlayer player) {
        HitResult hit = player.pick(CAMPFIRE_RANGE, 1.0F, false);
        return hit instanceof BlockHitResult blockHit && isLitCampfire(player.level(), blockHit.getBlockPos());
    }

    private static boolean isLitCampfire(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT);
    }
}
