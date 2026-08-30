package com.otherworldinn.compat;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import java.lang.reflect.Method;

/**
 * 女仆工作效率辅助类，根据好感度计算效率倍率。
 * 满好感 = 1.0，最低好感 = 0.4，线性映射。
 */
public final class MaidEfficiencyHelper {
    private static final float MIN_EFFICIENCY = 0.4F;
    private static final float MAX_EFFICIENCY = 1.0F;
    private static final int DEFAULT_FAVORABILITY = 100;
    private static Method FAVORABILITY_METHOD;
    private static boolean FAVORABILITY_CHECKED;

    private MaidEfficiencyHelper() {}

    /**
     * 获取女仆当前的工作效率倍率 [0.4, 1.0]。
     */
    public static float getEfficiency(EntityMaid maid) {
        int favor = getFavorability(maid);
        float ratio = (float) favor / (float) DEFAULT_FAVORABILITY;
        return MIN_EFFICIENCY + ratio * (MAX_EFFICIENCY - MIN_EFFICIENCY);
    }

    /**
     * 效率越低，工作冷却/延迟越长。
     */
    public static long adjustCooldown(long baseCooldown, float efficiency) {
        return Math.max(1, Math.round(baseCooldown / Math.max(0.4F, efficiency)));
    }

    /**
     * 效率越低，移动速度越慢。
     */
    public static float adjustMoveSpeed(float baseSpeed, float efficiency) {
        return Math.max(0.32F, baseSpeed * Math.max(0.4F, efficiency));
    }

    private static int getFavorability(EntityMaid maid) {
        if (!FAVORABILITY_CHECKED) {
            FAVORABILITY_CHECKED = true;
            try {
                FAVORABILITY_METHOD = EntityMaid.class.getMethod("getFavorability");
            } catch (NoSuchMethodException ignored) {
                // 回退到旧方法名
                try {
                    FAVORABILITY_METHOD = EntityMaid.class.getMethod("getFavorabilityLevel");
                } catch (NoSuchMethodException ignored2) {
                    FAVORABILITY_METHOD = null;
                }
            }
        }
        if (FAVORABILITY_METHOD != null) {
            try {
                Object result = FAVORABILITY_METHOD.invoke(maid);
                if (result instanceof Number) {
                    return Math.max(0, Math.min(DEFAULT_FAVORABILITY, ((Number) result).intValue()));
                }
            } catch (Exception ignored) {
            }
        }
        return DEFAULT_FAVORABILITY;
    }
}
