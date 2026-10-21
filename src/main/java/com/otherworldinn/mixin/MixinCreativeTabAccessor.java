package com.otherworldinn.mixin;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 读取创造模式物品栏当前选中的标签（{@code selectedTab} 是 private static 字段）。
 * 移植自 Subtle GUI Tweaks（MIT），署名见 {@code NOTICE-smooth-gui.md}。
 */
@Mixin(CreativeModeInventoryScreen.class)
public interface MixinCreativeTabAccessor {

    @Accessor("selectedTab")
    static CreativeModeTab otherworldinn$getSelectedTab() {
        throw new AssertionError("mixin accessor not applied");
    }
}
