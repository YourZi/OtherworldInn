package com.otherworldinn.network.packet;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.init.ModAttachments;
import com.otherworldinn.world.map.MapPoint;
import com.otherworldinn.world.map.TownDataProvider;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import com.otherworldinn.world.teleport.DeathExploreAnchorSyncHelper;
import com.otherworldinn.world.teleport.TeleportUtils;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** 客户端 -> 服务端传送请求包，携带目标地图点的 ResourceLocation ID。 */
public record C2STeleportPacket(ResourceLocation pointId) implements CustomPacketPayload {
    private static final ResourceLocation TOWN_GATE_POINT_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "town_gate");
    private static final BlockPos TOWN_SPAWN_POS = new BlockPos(51, 71, 0);
    private static final int OVERWORLD_EXIT_RADIUS = 2048;
    private static final int OVERWORLD_EXIT_ATTEMPTS = 24;
    private static final int OVERWORLD_DEATH_RETURN_RADIUS = 128;
    private static final int NETHER_DEATH_RETURN_RADIUS = 48;
    private static final int END_DEATH_RETURN_RADIUS = 16;


    public static final Type<C2STeleportPacket> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "teleport_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2STeleportPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC,
                    C2STeleportPacket::pointId,
                    C2STeleportPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(
                () -> {
                    if (context.player() instanceof ServerPlayer player) {
                        TeamData team = TeamManager.getInstance().getPlayerTeam(player);
                        if (team == null) {
                            return;
                        }

                        if (TOWN_GATE_POINT_ID.equals(pointId)) {
                            teleportToExploreSpawn(player);
                            return;
                        }

                        if (!team.isMapPointUnlocked(pointId)) {
                            return;
                        }

                        Optional<MapPoint> pointOpt = TownDataProvider.getPoint(pointId);
                        if (pointOpt.isPresent()) {
                            // 先退出地图模式（保留当前位置，不恢复到进入前的位置），
                            // 避免 maintainMapHover 在客户端关闭动画期间把玩家拉回地图虚拟位置。
                            C2SMapModeSyncPacket.exitMapMode(player, false);

                            Vec3 target = pointOpt.get().worldPosition();
                            player.teleportTo(
                                    player.serverLevel(),
                                    target.x,
                                    target.y,
                                    target.z,
                                    player.getYRot(),
                                    player.getXRot());

                            player.serverLevel()
                                    .playSound(
                                            null,
                                            player.getX(),
                                            player.getY(),
                                            player.getZ(),
                                            SoundEvents.ENDERMAN_TELEPORT,
                                            SoundSource.PLAYERS,
                                            8.0F,
                                            1.0F);

                            player.serverLevel()
                                    .sendParticles(
                                            ParticleTypes.PORTAL,
                                            player.getX(),
                                            player.getY() + 1.0,
                                            player.getZ(),
                                            32,
                                            0.5,
                                            1.0,
                                            0.5,
                                            0.1);
                        }
                    }
                });
    }

    private static void teleportToExploreSpawn(ServerPlayer player) {
        // 先退出地图模式（保留当前位置），避免 maintainMapHover 拉回玩家。
        C2SMapModeSyncPacket.exitMapMode(player, false);

        // 先传送到旅社出生点
        player.teleportTo(
                player.serverLevel(),
                TOWN_SPAWN_POS.getX() + 0.5,
                TOWN_SPAWN_POS.getY(),
                TOWN_SPAWN_POS.getZ() + 0.5,
                player.getYRot(),
                player.getXRot());

        // 再传送到默认探索维度或跑尸保护记录的死亡维度
        ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }

        ServerLevel targetLevel = overworld;
        BlockPos centerPos = overworld.getSharedSpawnPos();
        int searchRadius = OVERWORLD_EXIT_RADIUS;
        var deathAnchorData = player.getData(ModAttachments.PLAYER_DEATH_EXPLORE_ANCHOR);
        boolean hadPendingDeathAnchor = deathAnchorData.hasPendingDeathPos();
        if (hadPendingDeathAnchor) {
            ResourceKey<Level> pendingDeathDimension = deathAnchorData.getPendingDeathDimension();
            BlockPos pendingDeathPos = deathAnchorData.getPendingDeathPos();
            if (pendingDeathDimension != null
                    && pendingDeathPos != null
                    && isSupportedDeathReturnDimension(pendingDeathDimension)) {
                ServerLevel pendingDeathLevel = player.server.getLevel(pendingDeathDimension);
                if (pendingDeathLevel != null) {
                    targetLevel = pendingDeathLevel;
                    centerPos = pendingDeathPos;
                    searchRadius = getDeathReturnRadius(pendingDeathDimension);
                }
            }
        }

        BlockPos spawnPos =
                TeleportUtils.findRandomSafeSpawnPos(
                        targetLevel,
                        centerPos,
                        searchRadius,
                        OVERWORLD_EXIT_ATTEMPTS);
        TeleportUtils.changeDimensionTo(player, targetLevel, spawnPos);
        if (hadPendingDeathAnchor) {
            deathAnchorData.clearPendingDeathPos();
            DeathExploreAnchorSyncHelper.sync(player);
        }
        playTeleportEffects(player);
    }

    private static boolean isSupportedDeathReturnDimension(ResourceKey<Level> dimension) {
        return Level.OVERWORLD.equals(dimension)
                || Level.NETHER.equals(dimension)
                || Level.END.equals(dimension);
    }

    private static int getDeathReturnRadius(ResourceKey<Level> dimension) {
        if (Level.NETHER.equals(dimension)) {
            return NETHER_DEATH_RETURN_RADIUS;
        }
        if (Level.END.equals(dimension)) {
            return END_DEATH_RETURN_RADIUS;
        }
        return OVERWORLD_DEATH_RETURN_RADIUS;
    }

    private static void playTeleportEffects(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS,
                8.0F,
                1.0F);
        level.sendParticles(
                ParticleTypes.PORTAL,
                player.getX(),
                player.getY() + 1.0D,
                player.getZ(),
                32,
                0.5D,
                1.0D,
                0.5D,
                0.1D);
    }
}
