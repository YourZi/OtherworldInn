package com.otherworldinn.world.quest;

import com.otherworldinn.world.dialogue.LocalizedText;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

/**
 * 主线任务节点（树结构）：至多一个父节点，一个父节点可对应多个子节点。
 * 父节点完成时全部子节点同时激活并行推进；任务完成 = 全部叶子节点完成。
 *
 * <p>一个节点一个 JSON 文件（参照原版进度），文件路径即节点 id；
 * 根节点（无 parent）额外承担任务元数据（name/desc/category）。
 * 奖励挂在节点上，节点完成时发放。
 * 可选 {@code marker}（三维坐标）：任务进行中时 HUD 上在该世界位置渲染指示圆点
 * （仅位置随视角变化，尺寸恒定）；{@code relative} 为真时坐标相对城镇出生点偏移。
 */
public record QuestNodeDef(
        String id,
        @Nullable String parent,
        @Nullable LocalizedText label,
        List<QuestObjectiveDef> objectives,
        List<QuestRewardDef> rewards,
        @Nullable BlockPos marker,
        boolean markerRelative) {

    public QuestNodeDef {
        objectives = List.copyOf(objectives);
        rewards = List.copyOf(rewards);
    }
}
