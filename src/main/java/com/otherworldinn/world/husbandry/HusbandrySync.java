package com.otherworldinn.world.husbandry;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.init.ModAttachments;
import com.otherworldinn.network.packet.S2CHusbandrySyncPacket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 好感度的服务端 -> 客户端同步。
 *
 * <p>发送时机：① 好感度变化（喂食/抚摸…）；② 玩家开始追踪该动物（初次进入视野）。
 * 客户端收到后写入客户端实体上的附件，读取走 {@link HusbandryService}。
 * 若同步包比实体的生成包先到（客户端还没有该实体），值先挂起，等 {@link EntityJoinLevelEvent} 补写。
 */
@EventBusSubscriber(modid = OtherworldInn.MODID)
public final class HusbandrySync {
    /** 客户端专用：实体尚未创建时暂存的好感度。 */
    private static final Map<Integer, Integer> PENDING = new ConcurrentHashMap<>();

    private HusbandrySync() {}

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getTarget() instanceof Animal animal)) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (animal.level() instanceof ServerLevel serverLevel) {
            // 玩家靠近时顺手结算衰减，这里发出的就是最新数值
            HusbandryService.settleDecay(animal, serverLevel);
        }
        PacketDistributor.sendToPlayer(player, snapshot(animal));
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide()) {
            return;
        }
        Integer pending = PENDING.remove(event.getEntity().getId());
        if (pending != null) {
            applyTo(event.getEntity(), pending);
        }
    }

    /** 好感度变化后推给所有正在追踪该动物的玩家。 */
    public static void broadcast(Animal animal) {
        if (!(animal.level() instanceof ServerLevel)) {
            return;
        }
        PacketDistributor.sendToPlayersTrackingEntity(animal, snapshot(animal));
    }

    /** 客户端入口：写入实体附件；实体还没到就挂起。 */
    public static void applyOnClient(int entityId, int affection, @Nullable Level level) {
        if (level == null) {
            return;
        }
        Entity entity = level.getEntity(entityId);
        if (entity == null) {
            // 正常情况下下一秒就会被 EntityJoinLevelEvent 消费；上限只是防止号码复用导致无限堆积。
            if (PENDING.size() > 128) {
                PENDING.clear();
            }
            PENDING.put(entityId, affection);
            return;
        }
        applyTo(entity, affection);
    }

    private static void applyTo(Entity entity, int affection) {
        entity.getData(ModAttachments.HUSBANDRY).setAffection(affection);
    }

    private static S2CHusbandrySyncPacket snapshot(Animal animal) {
        return new S2CHusbandrySyncPacket(animal.getId(), HusbandryService.getAffection(animal));
    }
}
