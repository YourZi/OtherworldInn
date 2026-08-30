package com.otherworldinn.world.map;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/**
 * 地图点（POI）数据结构。
 * {@code screenOffset} 为相对世界坐标映射后屏幕位置的微调偏移；{@code atlasSlot} 为图集槽位（每槽 16x48，垂直三态）。
 */
public record MapPoint(
        ResourceLocation id,
        Vec3 worldPosition,
        Vec2 screenOffset,
        ResourceLocation iconTexture,
        int atlasSlot,
        Component displayName,
        MapPointType type,
        String unlockCondition) {
    public enum MapPointType {
        SHOP, // 商店/功能点
        EXIT_GATE, // 离开城镇的出口
        LANDMARK // 地标（仅展示）
    }
}
