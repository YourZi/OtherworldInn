package com.otherworldinn.client;

import com.otherworldinn.client.commission.CommissionClientManager;
import com.otherworldinn.client.dialogue.DialogueClientManager;
import com.otherworldinn.client.renderer.RoomOutlineRenderer;
import com.otherworldinn.client.util.TextureUtils;
import com.otherworldinn.util.ClientServices;
import com.otherworldinn.world.dialogue.DialogueNodeView;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public final class ClientServicesImpl implements ClientServices.Hooks {
    @Override
    public void activateRoomOutline(int ticks) {
        RoomOutlineRenderer.activateTimedRoomOutline(ticks);
    }

    @Override
    @Nullable
    public Player getClientPlayer() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft == null ? null : minecraft.player;
    }

    @Override
    public boolean isChineseLocale() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return true;
        }
        String locale = minecraft.getLanguageManager().getSelected();
        return locale != null && locale.toLowerCase(Locale.ROOT).startsWith("zh");
    }

    @Override
    public boolean isShiftDown() {
        return Screen.hasShiftDown();
    }

    @Override
    public List<ResourceLocation> findTexturesInFolder(String namespace, String path) {
        return TextureUtils.findTexturesInFolder(namespace, path);
    }

    @Override
    public ResourceLocation getMojangSkinTexture(String playerName, ResourceLocation fallback) {
        return TextureUtils.getMojangSkinTexture(playerName, fallback);
    }

    @Override
    public void handleDialogueNode(DialogueNodeView view) {
        DialogueClientManager.handleNode(view);
    }

    @Override
    public void handleDialogueClose() {
        DialogueClientManager.handleClose();
    }

    @Override
    public void handleCommissionBoard(CompoundTag data, boolean openScreen) {
        CommissionClientManager.handleBoardData(data, openScreen);
    }
}
