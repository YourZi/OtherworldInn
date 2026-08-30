package com.otherworldinn.client.quest;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

/**
 * 客户端任务 HUD 数据缓存：解析 S2CQuestHudPacket 快照，服务端已将 quest/委托/故事委托归一化为统一条目。
 */
public final class QuestHudState {
    /** 单个目标条目；required > 1 时渲染计数 current/required。 */
    public record HudRequirement(String type, Component text, boolean complete, int current, int required) {}

    /**
     * 一个任务条目：稳定 key（快照 Id）+ 阶段签名（活跃节点集合，用于节点推进交叉淡变）
     * + 分类（main/side，标题前缀用）+ 标题 + 目标行列表 + 指示点（世界坐标，xyz）。
     */
    public record HudTask(
            String key,
            String stage,
            String category,
            Component title,
            List<HudRequirement> requirements,
            List<int[]> markers) {}

    private static volatile List<HudTask> tasks = List.of();

    private QuestHudState() {}

    public static void updateFromSnapshot(@Nullable CompoundTag snapshot) {
        if (snapshot == null || !snapshot.contains("Tasks", Tag.TAG_LIST)) {
            tasks = List.of();
            return;
        }
        List<HudTask> parsed = new ArrayList<>();
        ListTag taskList = snapshot.getList("Tasks", Tag.TAG_COMPOUND);
        for (Tag t : taskList) {
            if (!(t instanceof CompoundTag taskTag)) {
                continue;
            }
            Component title = resolveText(taskTag.getString("TitleKey"), taskTag.getString("TitleText"));
            if (title.getString().isBlank()) {
                continue;
            }
            String key = taskTag.getString("Id");
            if (key.isBlank()) {
                key = title.getString();
            }
            List<HudRequirement> requirements = new ArrayList<>();
            ListTag requirementList = taskTag.getList("Requirements", Tag.TAG_COMPOUND);
            for (Tag r : requirementList) {
                if (!(r instanceof CompoundTag requirementTag)) {
                    continue;
                }
                Component text =
                        resolveText(requirementTag.getString("DisplayKey"), requirementTag.getString("DisplayText"));
                if (text.getString().isBlank()) {
                    continue;
                }
                requirements.add(new HudRequirement(
                        requirementTag.getString("Type"),
                        text,
                        requirementTag.getBoolean("Complete"),
                        Math.max(0, requirementTag.getInt("Current")),
                        Math.max(1, requirementTag.getInt("Required"))));
            }
            List<int[]> markers = new ArrayList<>();
            ListTag markerList = taskTag.getList("Markers", Tag.TAG_COMPOUND);
            for (Tag m : markerList) {
                if (!(m instanceof CompoundTag markerTag)) {
                    continue;
                }
                markers.add(new int[] {
                    markerTag.getInt("X"), markerTag.getInt("Y"), markerTag.getInt("Z")
                });
            }
            parsed.add(new HudTask(
                    key,
                    taskTag.getString("Stage"),
                    taskTag.getString("Category"),
                    title,
                    List.copyOf(requirements),
                    List.copyOf(markers)));
        }
        tasks = List.copyOf(parsed);
    }

    public static List<HudTask> getTasks() {
        return tasks;
    }

    public static void clear() {
        tasks = List.of();
    }

    /** 与服务端 builder 的约定一致：翻译键有效则本地化，否则用服务端下发的明文兜底。 */
    private static Component resolveText(String key, String fallbackText) {
        if (key != null && !key.isBlank() && Language.getInstance().has(key)) {
            return Component.translatable(key);
        }
        return Component.literal(fallbackText == null ? "" : fallbackText);
    }
}
