package com.otherworldinn.world.event.runtime;

import com.otherworldinn.OtherworldInn;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** 实体消失管理器：处理实体的延迟移除任务。 */
@EventBusSubscriber(modid = OtherworldInn.MODID)
public class DisappearManager {

    private record DisappearTask(UUID uuid, ResourceKey<Level> levelKey, long targetTime) {}

    private static final List<DisappearTask> tasks = new ArrayList<>();
    private static final List<DisappearTask> pendingTasks = new ArrayList<>();

    /** 安排实体消失任务。 */
    public static void schedule(Entity entity, int delayTicks) {
        if (entity == null || entity.level().isClientSide) return;
        // 使用临时列表避免并发修改异常
        pendingTasks.add(
                new DisappearTask(
                        entity.getUUID(),
                        entity.level().dimension(),
                        entity.level().getGameTime() + delayTicks));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!pendingTasks.isEmpty()) {
            tasks.addAll(pendingTasks);
            pendingTasks.clear();
        }

        if (tasks.isEmpty()) return;

        Iterator<DisappearTask> iterator = tasks.iterator();
        while (iterator.hasNext()) {
            DisappearTask task = iterator.next();

            ServerLevel level = event.getServer().getLevel(task.levelKey);
            if (level == null) {
                iterator.remove();
                continue;
            }

            if (level.getGameTime() >= task.targetTime) {
                Entity entity = level.getEntity(task.uuid);
                if (entity != null) {
                    level.sendParticles(
                            ParticleTypes.POOF,
                            entity.getX(),
                            entity.getY() + entity.getBbHeight() / 2.0,
                            entity.getZ(),
                            20,
                            0.5,
                            0.5,
                            0.5,
                            0.1);

                    // 用 discard 而非 kill，避免触发死亡掉落等逻辑
                    entity.discard();
                }
                // 无论实体是否存在（可能已被移除），任务都算完成
                iterator.remove();
            }
        }
    }
}
