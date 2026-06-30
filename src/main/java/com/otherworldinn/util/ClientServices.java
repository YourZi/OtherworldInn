package com.otherworldinn.util;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.world.dialogue.DialogueNodeView;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public final class ClientServices {
    private static final String CLIENT_HOOKS_CLASS = "com.otherworldinn.client.ClientServicesImpl";
    private static final Hooks HOOKS = createHooks();

    private ClientServices() {}

    public static void activateRoomOutline(int ticks) {
        HOOKS.activateRoomOutline(ticks);
    }

    @Nullable
    public static Player getClientPlayer() {
        return HOOKS.getClientPlayer();
    }

    public static boolean isChineseLocale() {
        return HOOKS.isChineseLocale();
    }

    public static boolean isShiftDown() {
        return HOOKS.isShiftDown();
    }

    public static List<ResourceLocation> findTexturesInFolder(String namespace, String path) {
        return HOOKS.findTexturesInFolder(namespace, path);
    }

    public static ResourceLocation getMojangSkinTexture(
            String playerName, ResourceLocation fallback) {
        return HOOKS.getMojangSkinTexture(playerName, fallback);
    }

    public static void handleDialogueNode(DialogueNodeView view) {
        HOOKS.handleDialogueNode(view);
    }

    public static void handleDialogueClose() {
        HOOKS.handleDialogueClose();
    }

    public static void handleCommissionBoard(CompoundTag data, boolean openScreen) {
        HOOKS.handleCommissionBoard(data, openScreen);
    }

    private static Hooks createHooks() {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return NoopHooks.INSTANCE;
        }
        try {
            Class<?> hooksClass = Class.forName(CLIENT_HOOKS_CLASS);
            Constructor<?> constructor = hooksClass.getDeclaredConstructor();
            return (Hooks) constructor.newInstance();
        } catch (ReflectiveOperationException | LinkageError exception) {
            OtherworldInn.LOGGER.warn("Failed to initialize Otherworld Inn client services", exception);
            return NoopHooks.INSTANCE;
        }
    }

    public interface Hooks {
        default void activateRoomOutline(int ticks) {}

        @Nullable
        default Player getClientPlayer() {
            return null;
        }

        default boolean isChineseLocale() {
            return true;
        }

        default boolean isShiftDown() {
            return false;
        }

        default List<ResourceLocation> findTexturesInFolder(String namespace, String path) {
            return Collections.emptyList();
        }

        default ResourceLocation getMojangSkinTexture(String playerName, ResourceLocation fallback) {
            return fallback;
        }

        default void handleDialogueNode(DialogueNodeView view) {}

        default void handleDialogueClose() {}

        default void handleCommissionBoard(CompoundTag data, boolean openScreen) {}
    }

    private enum NoopHooks implements Hooks {
        INSTANCE
    }
}
