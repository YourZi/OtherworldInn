package com.wdiscute.starcatcher.registry;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class SCKeymappings
{
    public static final KeyMapping MINIGAME_HIT = new KeyMapping("key.starcatcher.minigame_hit", GLFW.GLFW_KEY_SPACE, "key.category.starcatcher.starcatcher");
    public static final KeyMapping EXPAND_TOURNAMENT = new KeyMapping("key.starcatcher.expand_tournament", GLFW.GLFW_KEY_TAB, "key.category.starcatcher.starcatcher");
}
