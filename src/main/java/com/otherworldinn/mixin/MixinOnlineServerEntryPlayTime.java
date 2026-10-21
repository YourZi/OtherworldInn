package com.otherworldinn.mixin;

import com.otherworldinn.client.playtime.PlayTimeDisplay;
import com.otherworldinn.client.playtime.ServerPlayTimeTracker;
import com.otherworldinn.foundation.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在多人游戏服务器列表的条目里显示累计游玩时长：注入 {@code OnlineServerEntry.render} 的 TAIL，
 * 绘制于 {@code x + entryWidth - 24 - statusTextWidth - textWidth, y + 1}（给右侧在线人数状态留位）。
 */
@Mixin(ServerSelectionList.OnlineServerEntry.class)
public abstract class MixinOnlineServerEntryPlayTime {

    @Inject(method = "render", at = @At("TAIL"), require = 0)
    private void otherworldinn$renderPlayTime(
            GuiGraphics guiGraphics,
            int index,
            int y,
            int x,
            int entryWidth,
            int entryHeight,
            int mouseX,
            int mouseY,
            boolean hovered,
            float partialTick,
            CallbackInfo ci) {
        if (!ClientConfig.showServerPlayTime()) {
            return;
        }
        ServerData serverData = ((ServerSelectionList.OnlineServerEntry) (Object) this).getServerData();
        if (serverData == null) {
            return;
        }
        String text = PlayTimeDisplay.format(ServerPlayTimeTracker.get(serverData.ip));
        if (text == null) {
            return;
        }
        int statusWidth = serverData.status == null
                ? 0
                : Minecraft.getInstance().font.width(serverData.status);
        PlayTimeDisplay.draw(
                guiGraphics,
                text,
                x + entryWidth - 24 - statusWidth - PlayTimeDisplay.width(text),
                y + 1);
    }
}
