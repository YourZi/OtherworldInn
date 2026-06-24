package com.otherworldinn.world.inn.decoration;

import com.otherworldinn.OtherworldInn;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

/** 从结构 NBT 资源解析装饰模板。 */
public final class InnDecorationTemplateLoader {
    private static final Map<String, Optional<InnDecorationTemplate>> CACHE = new HashMap<>();

    private InnDecorationTemplateLoader() {}

    public static Optional<InnDecorationTemplate> getTemplate(InnDecorationDefinition definition) {
        return CACHE.computeIfAbsent(definition.id(), key -> load(definition));
    }

    private static Optional<InnDecorationTemplate> load(InnDecorationDefinition definition) {
        try (InputStream in =
                InnDecorationTemplateLoader.class
                        .getClassLoader()
                        .getResourceAsStream(definition.templateResourcePath())) {
            if (in == null) {
                OtherworldInn.LOGGER.warn(
                        "Decoration template not found: {}", definition.templateResourcePath());
                return Optional.empty();
            }
            CompoundTag root = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
            return parseTemplate(root);
        } catch (Exception e) {
            OtherworldInn.LOGGER.warn(
                    "Failed to load decoration template: {}", definition.templateResourcePath(), e);
            return Optional.empty();
        }
    }

    private static Optional<InnDecorationTemplate> parseTemplate(CompoundTag root) {
        if (!root.contains("palette", Tag.TAG_LIST) || !root.contains("blocks", Tag.TAG_LIST)) {
            return Optional.empty();
        }

        ListTag paletteTag = root.getList("palette", Tag.TAG_COMPOUND);
        List<BlockState> palette = new ArrayList<>(paletteTag.size());
        for (Tag tag : paletteTag) {
            if (!(tag instanceof CompoundTag paletteEntry)) {
                continue;
            }
            palette.add(parseBlockState(paletteEntry));
        }

        ListTag blocksTag = root.getList("blocks", Tag.TAG_COMPOUND);
        List<InnDecorationTemplate.TemplateBlock> blocks = new ArrayList<>(blocksTag.size());
        for (Tag tag : blocksTag) {
            if (!(tag instanceof CompoundTag blockTag)) {
                continue;
            }
            int stateIndex = blockTag.getInt("state");
            if (stateIndex < 0 || stateIndex >= palette.size()) {
                continue;
            }
            ListTag posTag = blockTag.getList("pos", Tag.TAG_INT);
            if (posTag.size() < 3) {
                continue;
            }
            BlockPos relativePos = new BlockPos(posTag.getInt(0), posTag.getInt(1), posTag.getInt(2));
            BlockState state = palette.get(stateIndex);
            if (state.isAir()) {
                continue;
            }
            blocks.add(new InnDecorationTemplate.TemplateBlock(relativePos, state));
        }

        if (blocks.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new InnDecorationTemplate(blocks));
    }

    private static BlockState parseBlockState(CompoundTag paletteEntry) {
        ResourceLocation blockId = ResourceLocation.tryParse(paletteEntry.getString("Name"));
        if (blockId == null) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        Block block = BuiltInRegistries.BLOCK.get(blockId);
        if (block == null || block == net.minecraft.world.level.block.Blocks.AIR) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        BlockState state = block.defaultBlockState();
        if (!paletteEntry.contains("Properties", Tag.TAG_COMPOUND)) {
            return state;
        }
        CompoundTag propertiesTag = paletteEntry.getCompound("Properties");
        for (String key : propertiesTag.getAllKeys()) {
            Property<?> property = block.getStateDefinition().getProperty(key);
            if (property == null) {
                continue;
            }
            state = applyProperty(state, property, propertiesTag.getString(key));
        }
        return state;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static BlockState applyProperty(BlockState state, Property property, String valueName) {
        Optional<?> parsed = property.getValue(valueName);
        if (parsed.isEmpty()) {
            return state;
        }
        return state.setValue(property, (Comparable) parsed.get());
    }
}
