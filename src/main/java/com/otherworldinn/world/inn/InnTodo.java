package com.otherworldinn.world.inn;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public record InnTodo(String id, Component component, String fallbackText) {
    private static final String LEGACY_PREFIX = "legacy:";
    private static final String TAG_ID = "Id";
    private static final String TAG_COMPONENT = "Component";
    private static final String TAG_TEXT = "Text";

    public InnTodo {
        fallbackText = fallbackText == null ? "" : fallbackText;
        component = component == null ? Component.literal(fallbackText) : component;
        if (fallbackText.isBlank()) {
            fallbackText = component.getString();
        }
        id = id == null || id.isBlank() ? LEGACY_PREFIX + fallbackText : id;
    }

    public static InnTodo legacy(String text) {
        String fallback = text == null ? "" : text;
        return new InnTodo(LEGACY_PREFIX + fallback, Component.literal(fallback), fallback);
    }

    public static InnTodo component(String id, Component component) {
        return new InnTodo(id, component, component == null ? "" : component.getString());
    }

    public static InnTodo translatable(String id, String key, Object... args) {
        return component(id, Component.translatable(key, args));
    }

    public boolean matchesId(String otherId) {
        return otherId != null && !otherId.isBlank() && id.equals(otherId);
    }

    public boolean matchesText(String text) {
        if (text == null) {
            return false;
        }
        return fallbackText.equals(text) || component.getString().equals(text);
    }

    public boolean matches(InnTodo other) {
        if (other == null) {
            return false;
        }
        return id.equals(other.id)
                || matchesText(other.fallbackText)
                || other.matchesText(fallbackText);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_ID, id);
        tag.putString(TAG_TEXT, fallbackText);
        tag.putString(TAG_COMPONENT, componentToJson(component));
        return tag;
    }

    public static InnTodo load(Tag tag) {
        if (tag instanceof StringTag stringTag) {
            return legacy(stringTag.getAsString());
        }
        if (!(tag instanceof CompoundTag compound)) {
            return legacy("");
        }
        String fallback = compound.getString(TAG_TEXT);
        Component component = componentFromJson(compound.getString(TAG_COMPONENT), fallback);
        return new InnTodo(compound.getString(TAG_ID), component, fallback);
    }

    private static String componentToJson(Component component) {
        try {
            return ComponentSerialization.CODEC
                    .encodeStart(JsonOps.INSTANCE, component)
                    .getOrThrow(JsonParseException::new)
                    .toString();
        } catch (RuntimeException exception) {
            return "";
        }
    }

    private static Component componentFromJson(String json, String fallback) {
        if (json == null || json.isBlank()) {
            return Component.literal(fallback == null ? "" : fallback);
        }
        try {
            return ComponentSerialization.CODEC
                    .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
                    .getOrThrow(JsonParseException::new);
        } catch (RuntimeException exception) {
            return Component.literal(fallback == null ? "" : fallback);
        }
    }
}
