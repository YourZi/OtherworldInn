package com.otherworldinn.world.dimension;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNull;

/**
 * 城镇区块生成器：虚空世界（地形/洞穴/地表/生物/结构均不生成），预留接口生成固定的城镇结构。
 */
public class TownChunkGenerator extends ChunkGenerator {
    public static final MapCodec<TownChunkGenerator> CODEC =
            RecordCodecBuilder.mapCodec(
                    instance ->
                            instance.group(
                                            BiomeSource.CODEC
                                                    .fieldOf("biome_source")
                                                    .forGetter(TownChunkGenerator::getBiomeSource))
                                    .apply(instance, TownChunkGenerator::new));

    public TownChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected @NotNull MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(
            @NotNull WorldGenRegion region,
            long seed,
            @NotNull RandomState random,
            @NotNull BiomeManager biomeManager,
            @NotNull StructureManager structureManager,
            @NotNull ChunkAccess chunk,
            GenerationStep.@NotNull Carving step) {
    }

    @Override
    public void buildSurface(
            @NotNull WorldGenRegion region,
            @NotNull StructureManager structureManager,
            @NotNull RandomState random,
            @NotNull ChunkAccess chunk) {
    }

    @Override
    public void spawnOriginalMobs(@NotNull WorldGenRegion region) {
    }

    @Override
    public int getGenDepth() {
        return 384; // 标准高度 -64 到 320
    }

    @Override
    public @NotNull CompletableFuture<ChunkAccess> fillFromNoise(
            @NotNull Blender blender,
            @NotNull RandomState state,
            @NotNull StructureManager manager,
            @NotNull ChunkAccess chunk) {
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public void createStructures(
            @NotNull RegistryAccess registryAccess,
            @NotNull ChunkGeneratorStructureState chunkGeneratorStructureState,
            @NotNull StructureManager structureManager,
            @NotNull ChunkAccess chunkAccess,
            @NotNull StructureTemplateManager structureTemplateManager) {
    }

    @Override
    public int getSeaLevel() {
        return 63;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getBaseHeight(
            int x,
            int z,
            Heightmap.@NotNull Types type,
            @NotNull LevelHeightAccessor level,
            @NotNull RandomState random) {
        return 0;
    }

    @Override
    public void addDebugScreenInfo(
            @NotNull List<String> info, @NotNull RandomState random, @NotNull BlockPos pos) {}

    @Override
    public @NotNull NoiseColumn getBaseColumn(
            int arg0, int arg1, @NotNull LevelHeightAccessor arg2, @NotNull RandomState arg3) {
        return new NoiseColumn(0, new BlockState[0]);
    }
}
