package com.otherworldinn.client.control;

import com.otherworldinn.OtherworldInn;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Applies map-view visual style: palette+dither post effect and higher gamma for flatter lighting.
 */
public final class MapViewVisualEffects {
    private static final ResourceLocation MAP_POST_EFFECT =
            ResourceLocation.fromNamespaceAndPath(
                    OtherworldInn.MODID, "shaders/post/map_view_dither.json");

    private static final double MAP_GAMMA = 1.8D;
    private static boolean active = false;
    private static Double previousGamma = null;

    private MapViewVisualEffects() {}

    public static void enable() {
        if (active) {
            return;
        }

        final Minecraft mc = Minecraft.getInstance();
        applyGamma(mc.options);
        loadPostEffectReflective(mc.gameRenderer, MAP_POST_EFFECT);
        active = true;
    }

    public static void disable() {
        if (!active) {
            return;
        }

        final Minecraft mc = Minecraft.getInstance();
        unloadPostEffectReflective(mc.gameRenderer);
        restoreGamma(mc.options);
        active = false;
    }

    public static boolean isActive() {
        return active;
    }

    private static void applyGamma(final Options options) {
        if (previousGamma != null) {
            return;
        }
        previousGamma = options.gamma().get();
        options.gamma().set(MAP_GAMMA);
    }

    private static void restoreGamma(final Options options) {
        if (previousGamma == null) {
            return;
        }
        options.gamma().set(previousGamma);
        previousGamma = null;
    }

    private static void loadPostEffectReflective(final Object gameRenderer, final ResourceLocation effect) {
        if (invokeIfPresent(gameRenderer, "loadEffect", effect)) {
            return;
        }
        invokeIfPresent(gameRenderer, "loadPostEffect", effect);
    }

    private static void unloadPostEffectReflective(final Object gameRenderer) {
        if (invokeIfPresent(gameRenderer, "shutdownEffect")) {
            return;
        }
        invokeIfPresent(gameRenderer, "shutdownPostEffect");
    }

    private static boolean invokeIfPresent(
            final Object receiver, final String methodName, @Nullable final Object arg) {
        final Class<?> type = receiver.getClass();
        try {
            final Method method =
                    arg == null ? type.getMethod(methodName) : type.getMethod(methodName, arg.getClass());
            method.setAccessible(true);
            if (arg == null) {
                method.invoke(receiver);
            } else {
                method.invoke(receiver, arg);
            }
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static boolean invokeIfPresent(final Object receiver, final String methodName) {
        final Class<?> type = receiver.getClass();
        try {
            final Method method = type.getMethod(methodName);
            method.setAccessible(true);
            method.invoke(receiver);
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }
}
