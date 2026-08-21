package com.otherworldinn.compat.exposure.client;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.api.OtherworldInnHudSnapshotApi;
import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.world.photo.PhotoObjective;
import com.otherworldinn.world.photo.PhotoObjectiveEvaluator;
import com.otherworldinn.world.photo.PhotoObjectiveEvaluator.ClientViewMatch;
import com.otherworldinn.world.photo.PhotoObjectiveRegistry;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.util.PointOfView;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.camera.frame.EntitiesInFrame;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
 * 在准星下方渲染金色提示"发现拍照任务目标"。判定复用 Exposure 拍照时的同一套算法
 * （getPointOfView + getViewfinderFov + EntitiesInFrame），保证提示与最终完成判定口径一致；
 * 任务目标来自 HUD 快照，无专用网络包。
 *
 * <p>提示不走 actionbar/HUD 事件：Exposure 在 Gui.render 头部渲染取景器 overlay 并按
 * 配置取消原版 HUD，RenderGuiEvent.Post 随之不触发；因此渲染由
 * MixinExposureViewfinderOverlay 在 overlay 渲染尾部借其 GuiGraphics 调用
 * {@link #renderHint}，投影与层级天然正确。tick 判定经
 * {@link PhotoTargetHintClientHandler} 在确认 Exposure 已加载后调用；本类的所有调用方
 * 均保证 Exposure 已加载（mixin 生效即隐含），无软依赖类加载风险。
 */
public final class PhotoTargetHintService {
    private static final String KEY_TASKS = "Tasks";
    private static final String KEY_REQUIREMENTS = "Requirements";
    private static final String TYPE_PHOTO = "photo";
    private static final String HINT_KEY = "message.otherworldinn.photo.hint.spotted";
    private static final String HINT_WRONG_BIOME_KEY = "message.otherworldinn.photo.hint.wrong_biome";
    private static final int TICK_INTERVAL = 5;
    /** 准星下方偏移（GUI 缩放像素） */
    private static final int CROSSHAIR_OFFSET_Y = 20;
    private static final Component HINT_TEXT = Component.translatable(HINT_KEY);
    private static final Component HINT_WRONG_BIOME_TEXT = Component.translatable(HINT_WRONG_BIOME_KEY);

    private enum State {
        INACTIVE,
        AIMING,
        /** 实体在画面中，但目标群系不符：金色主提示下方追加红色群系警告 */
        PARTIAL,
        SPOTTED
    }

    private static State state = State.INACTIVE;
    private static int tickCounter;
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

        state = switch (evaluate(mc, objectives, camera, cameraItem, stack)) {
            case FULL -> State.SPOTTED;
            case WRONG_BIOME -> State.PARTIAL;
            default -> State.AIMING;
        };
    }

    /** 每帧渲染：金色"发现拍照任务目标"；PARTIAL 态（生物对但群系错）在其下追加红色警告行 */
    public static void renderHint(GuiGraphics guiGraphics) {
        if (state != State.SPOTTED && state != State.PARTIAL) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        int centerX = mc.getWindow().getGuiScaledWidth() / 2;
        int y = mc.getWindow().getGuiScaledHeight() / 2 + CROSSHAIR_OFFSET_Y;
        guiGraphics.drawString(
                mc.font, HINT_TEXT, centerX - mc.font.width(HINT_TEXT) / 2, y, ModColors.YELLOW, true);
        if (state == State.PARTIAL) {
            int warnY = y + mc.font.lineHeight + 2;
            guiGraphics.drawString(
                    mc.font,
                    HINT_WRONG_BIOME_TEXT,
                    centerX - mc.font.width(HINT_WRONG_BIOME_TEXT) / 2,
                    warnY,
                    ModColors.ERROR,
                    true);
        }
    }

    /** 多目标中取最优结果：任一 FULL 即 FULL，否则任一 WRONG_BIOME 即 WRONG_BIOME */
    private static ClientViewMatch evaluate(
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
            ClientViewMatch best = ClientViewMatch.NO_MATCH;
            for (PhotoObjective objective : objectives) {
                ClientViewMatch match = PhotoObjectiveEvaluator.evaluateClientView(
                        objective, mc.level, pov.pos(), pov.dir(), entitiesInFrame);
                if (match == ClientViewMatch.FULL) {
                    return ClientViewMatch.FULL;
                }
                if (match == ClientViewMatch.WRONG_BIOME) {
                    best = ClientViewMatch.WRONG_BIOME;
                }
            }
            return best;
        } catch (Throwable t) {
            // EntitiesInFrame 名义上是服务端工具，出现未知 dist 问题时降级为本会话禁用
            OtherworldInn.LOGGER.warn(
                    "Failed to evaluate photo target hint, disabling for this session", t);
            disabled = true;
            reset();
            return ClientViewMatch.NO_MATCH;
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

    private static void reset() {
        state = State.INACTIVE;
    }
}
