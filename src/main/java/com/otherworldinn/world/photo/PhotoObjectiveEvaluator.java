package com.otherworldinn.world.photo;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;

/**
 * 拍照目标的通用子判定（双端可用，不依赖 Exposure）。
 *
 * <p>服务端完成判定（ExposurePhotoMatcher，基于 Frame 额外数据）与客户端取景提示
 * （PhotoTargetHintService，基于本地预测的画面内容）共用同一套匹配规则，避免两份逻辑漂移。
 * 结构条件依赖服务端 Frame 数据，保留在 ExposurePhotoMatcher 中。
 */
public final class PhotoObjectiveEvaluator {
    /** 客户端群系采样沿视线的最大距离（格） */
    private static final double BIOME_SAMPLE_REACH = 96.0;
    private static final double[] BIOME_SAMPLE_DISTANCES = {8.0, 16.0, 32.0, 64.0};

    private PhotoObjectiveEvaluator() {}

    public static boolean matchesEntityTypeIds(PhotoObjective objective, List<ResourceLocation> entityTypeIds) {
        int matchedCount = 0;
        for (ResourceLocation entityTypeId : entityTypeIds) {
            if (objective.entityIds().contains(entityTypeId)) {
                matchedCount++;
                if (matchedCount >= objective.countMin()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean matchesDimension(PhotoObjective objective, @Nullable ResourceLocation dimension) {
        return objective.dimension() != null && objective.dimension().equals(dimension);
    }

    public static boolean matchesBiome(PhotoObjective objective, @Nullable ResourceLocation biome) {
        return biome != null && objective.biomeIds().contains(biome);
    }

    public static boolean combine(PhotoObjective objective, List<Boolean> results) {
        if (results.isEmpty()) {
            return false;
        }
        return objective.matchMode() == PhotoObjectiveMatchMode.ANY
                ? results.stream().anyMatch(Boolean::booleanValue)
                : results.stream().allMatch(Boolean::booleanValue);
    }

    /** 结构条件只能在服务端从 Frame 额外数据中评估 */
    public static boolean isClientEvaluatable(PhotoObjective objective) {
        return objective.structureIds().isEmpty();
    }

    /** 客户端取景提示的三态判定：区分"完全命中"与"实体命中但群系不符" */
    public enum ClientViewMatch {
        /** 实体条件未满足（或维度不符） */
        NO_MATCH,
        /** 实体在画面中，但群系条件未满足 */
        WRONG_BIOME,
        /** 全部条件满足 */
        FULL
    }

    /**
     * 取景提示用：以相机位置与朝向近似还原拍照时刻的判定上下文，
     * 返回三态结果供提示区分"发现目标"与"生物对但群系错"。
     *
     * <p>群系条件沿视线采样若干点（近似服务端从画面命中方块取群系的口径），
     * 任一采样点命中即视为满足。语义按 matchMode=ALL 展开（现有目标均为 ALL）。
     */
    public static ClientViewMatch evaluateClientView(
            PhotoObjective objective,
            Level level,
            Vec3 cameraPos,
            Vec3 cameraDir,
            List<LivingEntity> entitiesInFrame) {
        if (objective == null || level == null) {
            return ClientViewMatch.NO_MATCH;
        }
        if (objective.dimension() != null
                && !matchesDimension(objective, level.dimension().location())) {
            return ClientViewMatch.NO_MATCH;
        }
        if (!objective.entityIds().isEmpty()) {
            List<ResourceLocation> entityTypeIds = new ArrayList<>(entitiesInFrame.size());
            for (LivingEntity entity : entitiesInFrame) {
                entityTypeIds.add(EntityType.getKey(entity.getType()));
            }
            if (!matchesEntityTypeIds(objective, entityTypeIds)) {
                return ClientViewMatch.NO_MATCH;
            }
        }
        if (!objective.biomeIds().isEmpty()
                && !matchesBiomeSampled(objective, level, cameraPos, cameraDir)) {
            return ClientViewMatch.WRONG_BIOME;
        }
        return ClientViewMatch.FULL;
    }

    private static boolean matchesBiomeSampled(PhotoObjective objective, Level level, Vec3 cameraPos, Vec3 cameraDir) {
        if (matchesBiome(objective, biomeAt(level, cameraPos))) {
            return true;
        }
        BlockHitResult hit = level.clip(new ClipContext(
                cameraPos,
                cameraPos.add(cameraDir.scale(BIOME_SAMPLE_REACH)),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                CollisionContext.empty()));
        Vec3[] samples = new Vec3[BIOME_SAMPLE_DISTANCES.length + 1];
        for (int i = 0; i < BIOME_SAMPLE_DISTANCES.length; i++) {
            samples[i] = cameraPos.add(cameraDir.scale(BIOME_SAMPLE_DISTANCES[i]));
        }
        samples[BIOME_SAMPLE_DISTANCES.length] = hit.getLocation();
        for (Vec3 sample : samples) {
            if (matchesBiome(objective, biomeAt(level, sample))) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static ResourceLocation biomeAt(Level level, Vec3 pos) {
        return level.getBiome(BlockPos.containing(pos))
                .unwrapKey()
                .map(key -> key.location())
                .orElse(null);
    }
}
