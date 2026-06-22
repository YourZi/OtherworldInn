package com.otherworldinn.world.event.listener;

import com.otherworldinn.block.CrystalBallBlock;
import com.otherworldinn.OtherworldInn;
import com.otherworldinn.init.ModBlocks;
import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import com.otherworldinn.world.teleport.TeleportUtils;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** 玩家事件处理器 */
@EventBusSubscriber(modid = OtherworldInn.MODID)
public class PlayerEventHandler {
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
            Component.translatable(TOWN_BOUNDARY_WARNING_KEY).withStyle(ChatFormatting.RED);

    private static final double DEATH_PENALTY_MIN_RATIO = 0.05;
    private static final double DEATH_PENALTY_MAX_RATIO = 0.10;
    private static final int DEATH_PENALTY_MAX_AMOUNT = 500;


    /**
     * 处理玩家死亡事件
     *
     * <p>在非城镇维度死亡时扣除队伍余额的5%-10%（上限500），并通知全队。
     */
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        Level level = player.level();
        if (level.dimension() == TownDimensions.TOWN_LEVEL) return;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        TeamData team = TeamManager.getInstance().getPlayerTeam(player);
        if (team == null || team.getCoins() <= 0) return;

        double ratio = DEATH_PENALTY_MIN_RATIO
                + (DEATH_PENALTY_MAX_RATIO - DEATH_PENALTY_MIN_RATIO) * level.random.nextDouble();
        int penalty = Math.max(1, (int) Math.round(team.getCoins() * ratio));
        penalty = Math.min(penalty, DEATH_PENALTY_MAX_AMOUNT);
        penalty = Math.min(penalty, team.getCoins());

        team.removeCoins(penalty, server);
        TeamManager.getInstance().syncTeam(team, server);

        Component coinIcon = Component.literal("\uE001").withStyle(ChatFormatting.WHITE);
        Component msg = Component.translatable("message.otherworldinn.death_penalty",
                player.getName().copy().withStyle(ChatFormatting.YELLOW),
                coinIcon.copy().append(Component.literal(String.valueOf(penalty)).withStyle(ChatFormatting.GOLD)))
                .withStyle(ChatFormatting.RED);

        for (UUID memberId : team.getMembers()) {
            ServerPlayer member = server.getPlayerList().getPlayer(memberId);
            if (member != null) {
                member.sendSystemMessage(msg);
            }
        }
    }

    /**
     * 处理玩家维度切换事件
     */
    @SubscribeEvent
    public static void onDimensionChange(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 魔法空间进出追踪
            boolean leavingMagic = player.level().dimension() == TownDimensions.MAGIC_SPACE_LEVEL;
            boolean enteringMagic = event.getDimension() == TownDimensions.MAGIC_SPACE_LEVEL;
            if (leavingMagic && !enteringMagic) {
                CrystalBallBlock.PLAYERS_IN_MAGIC_SPACE.remove(player.getUUID());
            }
        }
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
                ServerLevel townLevel = player.getServer().getLevel(TownDimensions.TOWN_LEVEL);
                if (townLevel != null) {
                    BlockPos spawnPos = new BlockPos(51, 71, 0);
                    player.teleportTo(
                            townLevel,
                            spawnPos.getX() + 0.5,
                            spawnPos.getY() + 1,
                            spawnPos.getZ() + 0.5,
                            player.getYRot(),
                            player.getXRot());
                    player.setRespawnPosition(TownDimensions.TOWN_LEVEL, spawnPos, 0, true, false);
                    player.addTag("otherworldinn.joined");
                }
            }
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

            if (player.getRespawnDimension() == Level.OVERWORLD
                    || player.getRespawnPosition() == null) {
                ServerLevel townLevel = player.getServer().getLevel(TownDimensions.TOWN_LEVEL);
                if (townLevel != null) {
                    BlockPos spawnPos = new BlockPos(51, 71, 0);
                    player.teleportTo(
                            townLevel,
                            spawnPos.getX() + 0.5,
                            spawnPos.getY() + 1,
                            spawnPos.getZ() + 0.5,
                            player.getYRot(),
                            player.getXRot());
                }
            }
        }
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
                                .withStyle(ChatFormatting.RED), true);
            }
        }
    }

    /** 玩家退出时清理魔法空间追踪 */
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CrystalBallBlock.PLAYERS_IN_MAGIC_SPACE.remove(player.getUUID());
            CrystalBallBlock.RETURN_POSITIONS.remove(player.getUUID());
        }
    }
}
