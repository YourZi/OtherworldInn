package com.otherworldinn.world.quest;

/**
 * 任务目标类型（首批全部为状态判定型）。
 *
 * <ul>
 *   <li>{@link #HAS_ITEM} 任一队员背包持有 ≥ count
 *   <li>{@link #INTERACT_ENTITY} 右键交互指定实体（按类型或特定实例二选一）
 *   <li>{@link #INTERACT_BLOCK} 右键交互指定方块（按方块 ID/标签或特定坐标二选一）
 *   <li>{@link #ADVANCEMENT} 已达成指定原版进度（接取前已达成也算）
 *   <li>{@link #ENTER_REGION} 任一队员进入 AABB 范围
 * </ul>
 */
public enum QuestObjectiveType {
    HAS_ITEM,
    INTERACT_ENTITY,
    INTERACT_BLOCK,
    ADVANCEMENT,
    ENTER_REGION
}
