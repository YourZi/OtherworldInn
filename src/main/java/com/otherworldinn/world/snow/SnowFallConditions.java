package com.otherworldinn.world.snow;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.compat.season.SeasonalSnowCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * "这一格归谁"：原版雪层，还是我们的覆雪。客户端与服务端共用这一份判定。
 *
 * <p>原版 {@code ServerLevel#tickPrecipitation} 只有
 * {@code isRaining && gamerule > 0 && biome.shouldSnow(level, pos)} 才放雪层，而
 * {@code shouldSnow} 内部除了天气/温度/光照，还会调 {@code Blocks.SNOW.canSurvive(level, pos)}——
 * 也就是"这一格能不能托住一个完整雪层"。栅栏、半砖、台阶、墙托不住，原版就什么也不放，
 * 那正是我们接手的地方。
 *
 * <p>所以顺序是：先问原版"你会铺吗"，会铺就让给它；不会铺时再排除"不是因为托不住"的原因，
 * 剩下的才归我们。条件全部走原版/季节模组自己的方法，不复制一份。
 */
public final class SnowFallConditions {

    /** 与原版 {@code Biome#shouldSnow} 内的阈值一致。 */
    private static final int MAX_BLOCK_LIGHT = 10;

    /** 判定结果；除 {@link #COVER} 外都附带原因。 */
    public enum Decision {
        COVER,
        NO_RAIN,
        GAMERULE_OFF,
        VANILLA_HANDLES,
        TOO_WARM,
        TOO_BRIGHT,
        NOT_AIR
    }

    private SnowFallConditions() {}

    /**
     * @param pos 雪要落的那一格（列顶上方第一格），与原版 {@code getHeightmapPos(MOTION_BLOCKING, ...)} 一致
     */
    public static Decision decide(Level level, BlockPos pos) {
        if (!level.isRaining()) {
            return Decision.NO_RAIN;
        }
        if (level.getGameRules().getInt(GameRules.RULE_SNOW_ACCUMULATION_HEIGHT) <= 0) {
            return Decision.GAMERULE_OFF;
        }
        Biome biome = level.getBiome(pos).value();
        if (biome.shouldSnow(level, pos)) {
            return Decision.VANILLA_HANDLES;
        }
        // 季节模组替换的是 shouldSnow，原版 warmEnoughToRain 返回的仍是基础温度，必须走季节版
        if (SeasonalSnowCompat.warmEnoughToRain(level, pos)) {
            return Decision.TOO_WARM;
        }
        if (level.getBrightness(LightLayer.BLOCK, pos) >= MAX_BLOCK_LIGHT) {
            return Decision.TOO_BRIGHT;
        }
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.SNOW)) {
            return Decision.VANILLA_HANDLES;
        }
        if (!state.getFluidState().isEmpty()) {
            return Decision.NOT_AIR;
        }
        // 空气、没有碰撞的装饰（草/火把/按钮……）、以及标签里点名"不铺雪"的方块：
        // 都算"不占位"，雪归我们，渲染时再分配给下面真正有上表面的方块。
        if (!state.isAir()
                && !state.getCollisionShape(level, pos).isEmpty()
                && !state.is(OtherworldInn.SNOW_COVER_IGNORED)) {
            return Decision.NOT_AIR;
        }
        return Decision.COVER;
    }
}
