package com.otherworldinn.compat.exposure.client;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.api.OtherworldInnHudSnapshotApi;
import com.otherworldinn.world.photo.PhotoObjective;
import com.otherworldinn.world.photo.PhotoObjectiveEvaluator;
import com.otherworldinn.world.photo.PhotoObjectiveRegistry;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.util.PointOfView;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.camera.frame.EntitiesInFrame;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 取景器拍照任务目标提示（仅客户端）。
 *
 * <p>玩家通过 Exposure 取景器观察时，若画面满足当前激活拍照任务（剧情/委托）的目标条件，
 * 在 actionbar 显示"发现拍照任务目标"。判定复用 Exposure 拍照时的同一套算法
 * （getPointOfView + getViewfinderFov + EntitiesInFrame），保证提示与最终完成判定口径一致；
 * 任务目标来自 HUD 快照，无专用网络包。
 *
 * <p>本类只应经 {@link PhotoTargetHintClientHandler} 在确认 Exposure 已加载后调用，
 * 避免软依赖下的类加载问题。
 */
public final class PhotoTargetHintService {
    private static final String KEY_TASKS = "Tasks";
    private static final String KEY_REQUIREMENTS = "Requirements";
    private static final String TYPE_PHOTO = "photo";
    private static final String HINT_KEY = "message.otherworldinn.photo.hint.spotted";
    private static final int TICK_INTERVAL = 5;
    private static final int SUSTAIN_REFRESH_TICKS = 40;

    private enum State {
        INACTIVE,
        AIMING,
        SPOTTED
    }

    private static State state = State.INACTIVE;
    private static int tickCounter;
    private static int evaluationsSinceAnnounce;
    private static boolean disabled;

    private PhotoTargetHintService() {}

    public static void tick() {
        if (disabled) {
            return;
        }
        tickCounter++;
        if (tickCounter % TICK_INTERVAL != 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            reset();
            return;
        }

        List<PhotoObjective> objectives = readActiveObjectives();
        if (objectives.isEmpty()) {
            reset();
            return;
        }

        if (!CameraClient.isActive() || !CameraClient.viewfinder().isLookingThrough()) {
            reset();
            return;
        }
        Camera camera = CameraClient.getActive().orElse(null);
        if (camera == null) {
            reset();
            return;
        }
        ItemStack stack = camera.getItemStack();
        if (!(stack.getItem() instanceof CameraItem cameraItem)) {
            reset();
            return;
        }

        if (evaluate(mc, objectives, camera, cameraItem, stack)) {
            if (state != State.SPOTTED) {
                state = State.SPOTTED;
                evaluationsSinceAnnounce = 0;
                announce(mc);
            } else if (++evaluationsSinceAnnounce >= SUSTAIN_REFRESH_TICKS / TICK_INTERVAL) {
                evaluationsSinceAnnounce = 0;
                announce(mc);
            }
        } else {
            state = State.AIMING;
        }
    }

    private static boolean evaluate(
            Minecraft mc,
            List<PhotoObjective> objectives,
            Camera camera,
            CameraItem cameraItem,
            ItemStack stack) {
        try {
            // 自拍模式的反向与回拉由 getPointOfView 内部处理
            PointOfView pov = cameraItem.getPointOfView(camera.getHolder(), stack);
            double fov = cameraItem.getViewfinderFov(mc.level, stack);
            List<LivingEntity> entitiesInFrame = EntitiesInFrame.get(camera.getHolder(), pov, fov);
            for (PhotoObjective objective : objectives) {
                if (PhotoObjectiveEvaluator.matchesClientView(
                        objective, mc.level, pov.pos(), pov.dir(), entitiesInFrame)) {
                    return true;
                }
            }
            return false;
        } catch (Throwable t) {
            // EntitiesInFrame 名义上是服务端工具，出现未知 dist 问题时降级为本会话禁用
            OtherworldInn.LOGGER.warn(
                    "Failed to evaluate photo target hint, disabling for this session", t);
            disabled = true;
            reset();
            return false;
        }
    }

    private static List<PhotoObjective> readActiveObjectives() {
        List<PhotoObjective> objectives = new ArrayList<>();
        CompoundTag snapshot = OtherworldInnHudSnapshotApi.getTaskHudSnapshot();
        ListTag tasks = snapshot.getList(KEY_TASKS, Tag.TAG_COMPOUND);
        for (int i = 0; i < tasks.size(); i++) {
            ListTag requirements = tasks.getCompound(i).getList(KEY_REQUIREMENTS, Tag.TAG_COMPOUND);
            for (int j = 0; j < requirements.size(); j++) {
                CompoundTag requirement = requirements.getCompound(j);
                if (!TYPE_PHOTO.equals(requirement.getString("Type"))
                        || requirement.getBoolean("Complete")) {
                    continue;
                }
                ResourceLocation objectiveId =
                        ResourceLocation.tryParse(requirement.getString("TargetId"));
                PhotoObjective objective = objectiveId == null ? null : PhotoObjectiveRegistry.get(objectiveId);
                if (objective != null && PhotoObjectiveEvaluator.isClientEvaluatable(objective)) {
                    objectives.add(objective);
                }
            }
        }
        return objectives;
    }

    private static void announce(Minecraft mc) {
        mc.gui.setOverlayMessage(Component.translatable(HINT_KEY), false);
    }

    private static void reset() {
        state = State.INACTIVE;
        evaluationsSinceAnnounce = 0;
    }
}
