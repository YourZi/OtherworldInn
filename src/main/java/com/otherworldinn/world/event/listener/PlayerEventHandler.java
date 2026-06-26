package com.otherworldinn.world.event.listener;

import com.otherworldinn.block.CrystalBallBlock;
import com.otherworldinn.OtherworldInn;
import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.init.ModAttachments;
import com.otherworldinn.init.ModBlocks;
import com.otherworldinn.init.ModItems;
import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import com.otherworldinn.world.teleport.DeathExploreAnchorSyncHelper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.otherworldinn.world.teleport.TeleportUtils;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSetSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** 玩家事件处理器 */
@EventBusSubscriber(modid = OtherworldInn.MODID)
public class PlayerEventHandler {
    private static final BlockPos TOWN_SPAWN_POS = new BlockPos(51, 71, 0);
    private static final double TOWN_BOUNDARY_CENTER_X = -19.0D;
    private static final double TOWN_BOUNDARY_CENTER_Z = 0.0D;
    private static final double TOWN_BOUNDARY_WARNING_DISTANCE = 150.0D;
    private static final double TOWN_BOUNDARY_WARNING_DISTANCE_SQR =
            TOWN_BOUNDARY_WARNING_DISTANCE * TOWN_BOUNDARY_WARNING_DISTANCE;
    private static final double TOWN_BOUNDARY_TELEPORT_DISTANCE = 170.0D;
    private static final double TOWN_BOUNDARY_TELEPORT_DISTANCE_SQR =
            TOWN_BOUNDARY_TELEPORT_DISTANCE * TOWN_BOUNDARY_TELEPORT_DISTANCE;
    private static final double TOWN_RELOCATE_X = 7.0D;
    private static final double TOWN_RELOCATE_Y = 71.0D;
    private static final double TOWN_RELOCATE_Z = 0.0D;
    private static final String TOWN_BOUNDARY_WARNING_KEY =
            "message.otherworldinn.town.boundary_warning";
    private static final Component TOWN_BOUNDARY_WARNING_TEXT =
            Component.translatable(TOWN_BOUNDARY_WARNING_KEY)
                    .withStyle(style -> style.withColor(ModColors.ERROR));

    private static final String WILD_SPAWN_DENIED_KEY =
            "message.otherworldinn.exploration.wild_spawn_denied";
    private static final String WILD_DEATH_RETURN_KEY =
            "message.otherworldinn.exploration.wild_death_return";
    private static final String WILD_DEATH_BROADCAST_KEY =
            "message.otherworldinn.exploration.wild_death_broadcast";
    private static final int DEATH_PENALTY_MIN_PERCENT = 5;
    private static final int DEATH_PENALTY_MAX_PERCENT = 10;
    private static final int DEATH_PENALTY_MAX_AMOUNT = 500;
    private static final Set<UUID> FORCED_TOWN_RESPAWNS = new HashSet<>();
    private static final Set<UUID> PENDING_RECALL_SCROLLS = new HashSet<>();


    /**
     * 处理玩家死亡事件
     *
     * <p>在非城镇维度死亡时强制回城且给予惩罚。
     */
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ServerLevel level = player.serverLevel();
        if (isPenaltyDimension(level)) {
            player.getData(ModAttachments.PLAYER_DEATH_EXPLORE_ANCHOR)
                    .setPendingDeathAnchor(level.dimension(), player.blockPosition());
        } else {
            player.getData(ModAttachments.PLAYER_DEATH_EXPLORE_ANCHOR).clearPendingDeathPos();
        }
        DeathExploreAnchorSyncHelper.sync(player);
        if (isTownDimension(level)) return;

        FORCED_TOWN_RESPAWNS.add(player.getUUID());

        if (!isPenaltyDimension(level)) return;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        if (level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            dropRandomInventorySlots(player, level);
        }
        int penalty = applyMedicalFee(player, server);

        player.sendSystemMessage(Component.translatable(WILD_DEATH_RETURN_KEY)
                .withStyle(style -> style.withColor(ModColors.ERROR)));

        Component broadcast = Component.translatable(
                        WILD_DEATH_BROADCAST_KEY,
                        player.getName().copy().withStyle(style -> style.withColor(ModColors.WHITE)),
                        formatCoinAmount(penalty))
                .withStyle(style -> style.withColor(ModColors.ERROR));
        server.getPlayerList().broadcastSystemMessage(broadcast, false);
    }

    /**
     * 处理玩家维度切换事件
     */
    @SubscribeEvent
    public static void onDimensionChange(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.level().dimension() == TownDimensions.TOWN_LEVEL
                    && event.getDimension() != TownDimensions.TOWN_LEVEL) {
                PENDING_RECALL_SCROLLS.add(player.getUUID());
            }

            // 魔法空间进出追踪
            boolean leavingMagic = player.level().dimension() == TownDimensions.MAGIC_SPACE_LEVEL;
            boolean enteringMagic = event.getDimension() == TownDimensions.MAGIC_SPACE_LEVEL;
            if (leavingMagic && !enteringMagic) {
                CrystalBallBlock.PLAYERS_IN_MAGIC_SPACE.remove(player.getUUID());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!PENDING_RECALL_SCROLLS.remove(player.getUUID())) {
            return;
        }
        giveOrDropRecallScroll(player);
    }

    /**
     * 处理玩家登录事件
     *
     * <p>玩家首次加入时，将其传送到旅社并设置重生点。 同时也负责初始化玩家的队伍信息。
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {

            MinecraftServer server = player.getServer();

            // 确保玩家加入队伍
            if (server != null) {
                TeamManager.getInstance().onPlayerJoin(player, server);
            }

            // 首次加入逻辑
            if (!player.getTags().contains("otherworldinn.joined")) {
                teleportToTownSpawn(player);
                setTownRespawn(player);
                player.addTag("otherworldinn.joined");
            }

            DeathExploreAnchorSyncHelper.sync(player);
        }
    }

    /**
     * 处理玩家重生事件
     *
     * <p>如果玩家没有重生点，则将其传送到旅社。
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (FORCED_TOWN_RESPAWNS.remove(player.getUUID())) {
                teleportToTownSpawn(player);
                setTownRespawn(player);
                DeathExploreAnchorSyncHelper.sync(player);
                return;
            }

            if (player.getRespawnDimension() == Level.OVERWORLD
                    || player.getRespawnPosition() == null) {
                teleportToTownSpawn(player);
            }
            DeathExploreAnchorSyncHelper.sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerSetSpawn(PlayerSetSpawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (event.getNewSpawn() == null) {
            return;
        }
        if (event.getSpawnLevel() == TownDimensions.TOWN_LEVEL) {
            return;
        }

        event.setCanceled(true);
        player.sendSystemMessage(Component.translatable(WILD_SPAWN_DENIED_KEY)
                .withStyle(style -> style.withColor(ModColors.ERROR)));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % 20 != 0) {
            return;
        }

        // 魔法空间虚空坠落保护
        if (player.level().dimension() == TownDimensions.MAGIC_SPACE_LEVEL) {
            if (player.getY() < -10) {
                BlockPos returnPos = CrystalBallBlock.RETURN_POSITIONS.get(player.getUUID());
                if (returnPos != null) {
                    ServerLevel townLevel = player.getServer().getLevel(TownDimensions.TOWN_LEVEL);
                    if (townLevel != null) {
                        TeleportUtils.changeDimensionTo(player, townLevel,
                                new BlockPos(returnPos.getX(), returnPos.getY() + 1, returnPos.getZ()));
                    }
                } else {
                    ServerLevel townLevel = player.getServer().getLevel(TownDimensions.TOWN_LEVEL);
                    if (townLevel != null) {
                        TeleportUtils.changeDimensionTo(player, townLevel,
                                new BlockPos(51, 71, 0));
                    }
                }
            }
            return;
        }

        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        if (player.level().dimension() != TownDimensions.TOWN_LEVEL) {
            return;
        }

        double dx = player.getX() - TOWN_BOUNDARY_CENTER_X;
        double dz = player.getZ() - TOWN_BOUNDARY_CENTER_Z;
        double distanceSqr = dx * dx + dz * dz;
        if (distanceSqr <= TOWN_BOUNDARY_WARNING_DISTANCE_SQR) {
            return;
        }

        player.displayClientMessage(TOWN_BOUNDARY_WARNING_TEXT, true);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1, false, true));
        if (distanceSqr <= TOWN_BOUNDARY_TELEPORT_DISTANCE_SQR) {
            return;
        }

        player.teleportTo(
                player.serverLevel(),
                TOWN_RELOCATE_X,
                TOWN_RELOCATE_Y,
                TOWN_RELOCATE_Z,
                player.getYRot(),
                player.getXRot());
        player.displayClientMessage(TOWN_BOUNDARY_WARNING_TEXT, true);
    }


    //水晶球保护
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel)) return;
        if (!event.getState().is(ModBlocks.CRYSTAL_BALL.get())) return;

        if (!CrystalBallBlock.PLAYERS_IN_MAGIC_SPACE.isEmpty()) {
            event.setCanceled(true);
            if (event.getPlayer() instanceof ServerPlayer sp) {
                sp.displayClientMessage(
                        Component.translatable("message.otherworldinn.crystal_ball.cannot_break_in_use")
                                .withStyle(style -> style.withColor(ModColors.ERROR)),
                        true);
            }
        }
    }

    /** 玩家退出时清理魔法空间追踪 */
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CrystalBallBlock.PLAYERS_IN_MAGIC_SPACE.remove(player.getUUID());
            CrystalBallBlock.RETURN_POSITIONS.remove(player.getUUID());
            FORCED_TOWN_RESPAWNS.remove(player.getUUID());
        }
    }

    private static boolean isTownDimension(Level level) {
        return level.dimension() == TownDimensions.TOWN_LEVEL;
    }

    private static boolean isPenaltyDimension(Level level) {
        return level.dimension() == Level.OVERWORLD
                || level.dimension() == Level.NETHER
                || level.dimension() == Level.END;
    }

    private static void teleportToTownSpawn(ServerPlayer player) {
        ServerLevel townLevel = player.getServer() != null
                ? player.getServer().getLevel(TownDimensions.TOWN_LEVEL)
                : null;
        if (townLevel == null) {
            return;
        }
        player.teleportTo(
                townLevel,
                TOWN_SPAWN_POS.getX() + 0.5,
                TOWN_SPAWN_POS.getY() + 1,
                TOWN_SPAWN_POS.getZ() + 0.5,
                player.getYRot(),
                player.getXRot());
    }

    private static void setTownRespawn(ServerPlayer player) {
        player.setRespawnPosition(TownDimensions.TOWN_LEVEL, TOWN_SPAWN_POS, 0, true, false);
    }

    private static void giveOrDropRecallScroll(ServerPlayer player) {
        ItemStack scroll = new ItemStack(ModItems.RECALL_SCROLL.get());
        if (!player.getInventory().contains(scroll) && !player.getInventory().add(scroll)) {
            player.drop(scroll, false);
        }
    }


    private static int applyMedicalFee(ServerPlayer player, MinecraftServer server) {
        TeamData team = TeamManager.getInstance().getPlayerTeam(player);
        if (team == null || team.getCoins() <= 0) {
            return 0;
        }

        int percent =
                DEATH_PENALTY_MIN_PERCENT
                        + player.serverLevel()
                                .random
                                .nextInt(DEATH_PENALTY_MAX_PERCENT - DEATH_PENALTY_MIN_PERCENT + 1);
        int penalty = Math.max(1, (int) Math.ceil(team.getCoins() * (percent / 100.0D)));
        penalty = Math.min(penalty, Math.min(DEATH_PENALTY_MAX_AMOUNT, team.getCoins()));

        if (penalty > 0 && TeamManager.getInstance().removeCoins(team, penalty, server)) {
            return penalty;
        }
        return 0;
    }

    private static void dropRandomInventorySlots(ServerPlayer player, ServerLevel level) {
        List<InventorySlotRef> candidates = collectDropCandidates(player);
        if (candidates.isEmpty()) {
            return;
        }

        int dropCount = Math.min(candidates.size(), 1 + level.random.nextInt(3));
        for (int i = 0; i < dropCount; i++) {
            int pickedIndex = level.random.nextInt(candidates.size());
            InventorySlotRef target = candidates.remove(pickedIndex);
            ItemStack stack = target.getStack();
            if (stack.isEmpty()) {
                continue;
            }

            ItemStack dropped = stack.copy();
            target.clear();

            ItemEntity itemEntity = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), dropped);
            itemEntity.setDeltaMovement(
                    (level.random.nextDouble() - 0.5D) * 0.3D,
                    0.2D + level.random.nextDouble() * 0.15D,
                    (level.random.nextDouble() - 0.5D) * 0.3D);
            itemEntity.setGlowingTag(true);
            itemEntity.setInvulnerable(true);
            itemEntity.setUnlimitedLifetime();
            level.addFreshEntity(itemEntity);
        }

        player.getInventory().setChanged();
    }

    private static List<InventorySlotRef> collectDropCandidates(ServerPlayer player) {
        List<InventorySlotRef> candidates = new ArrayList<>();
        collectDropCandidates(candidates, player.getInventory().items);
        collectDropCandidates(candidates, player.getInventory().armor);
        collectDropCandidates(candidates, player.getInventory().offhand);
        return candidates;
    }

    private static void collectDropCandidates(
            List<InventorySlotRef> output, List<ItemStack> container) {
        for (int i = 0; i < container.size(); i++) {
            if (!container.get(i).isEmpty()) {
                output.add(new InventorySlotRef(container, i));
            }
        }
    }

    private static Component formatCoinAmount(int amount) {
        return Component.literal("\uE001")
                .withStyle(style -> style.withColor(ModColors.WHITE))
                .append(Component.literal(String.valueOf(amount))
                        .withStyle(style -> style.withColor(ModColors.YELLOW)));
    }

    private record InventorySlotRef(List<ItemStack> container, int index) {
        private ItemStack getStack() {
            return this.container.get(this.index);
        }

        private void clear() {
            this.container.set(this.index, ItemStack.EMPTY);
        }
    }
}
