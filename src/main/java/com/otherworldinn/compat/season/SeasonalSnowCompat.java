package com.otherworldinn.compat.season;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.neoforged.fml.ModList;

/**
 * 季节模组的温度判定适配层。
 *
 * <p>为什么需要它：Serene Seasons 用 mixin **整个替换了 {@code Biome#shouldSnow}**，
 * 里面的"够不够冷"换成了它自己的 {@code SeasonHooks.warmEnoughToRainSeasonal(level, pos)}
 * （按季节改温度）。而 {@code Biome#warmEnoughToRain} 本身**没有被替换**，返回的还是原版温度。
 * 所以直接调它会在"基础温度偏暖、只有冬天才下雪"的维度（比如本模组的城镇）里永远得到"太暖"。
 *
 * <p>但它自己的逻辑是受配置控制的：
 * <pre>
 *   if (ModConfig.seasons.generateSnowAndIce) 用季节温度
 *   else                                      用原版温度
 * </pre>
 * 这里完全照它的分支走——配置关掉时我们也退回原版温度，保证判断始终和它一致。
 */
public final class SeasonalSnowCompat {

    /** {@code SeasonHooks#warmEnoughToRainSeasonal(LevelReader, BlockPos)}，没装就是 null。 */
    private static final MethodHandle SEASONAL_WARM_ENOUGH;
    /** {@code ModConfig.seasons} 静态字段。 */
    private static final Field CONFIG_SEASONS;
    /** {@code SeasonsConfig#generateSnowAndIce} 实例字段。 */
    private static final Field CONFIG_GENERATE_SNOW;

    static {
        MethodHandle warmEnough = null;
        Field seasons = null;
        Field generateSnow = null;
        if (ModList.get().isLoaded("sereneseasons")) {
            try {
                Class<?> hooks = Class.forName("sereneseasons.season.SeasonHooks");
                warmEnough = MethodHandles.publicLookup().findStatic(
                        hooks,
                        "warmEnoughToRainSeasonal",
                        MethodType.methodType(boolean.class, LevelReader.class, BlockPos.class));
            } catch (Throwable unavailable) {
                warmEnough = null;
            }
            try {
                Class<?> configClass = Class.forName("sereneseasons.init.ModConfig");
                seasons = configClass.getField("seasons");
                Class<?> seasonsConfig = Class.forName("sereneseasons.config.SeasonsConfig");
                generateSnow = seasonsConfig.getField("generateSnowAndIce");
            } catch (Throwable unavailable) {
                seasons = null;
                generateSnow = null;
            }
        }
        SEASONAL_WARM_ENOUGH = warmEnough;
        CONFIG_SEASONS = seasons;
        CONFIG_GENERATE_SNOW = generateSnow;
    }

    private SeasonalSnowCompat() {}

    /** 这一格按"季节模组当前的配置与季节"算，是否暖到只能下雨（= 不会下雪）。 */
    public static boolean warmEnoughToRain(Level level, BlockPos pos) {
        if (SEASONAL_WARM_ENOUGH != null && seasonalSnowEnabled()) {
            try {
                return (boolean) SEASONAL_WARM_ENOUGH.invokeExact((LevelReader) level, pos);
            } catch (Throwable ignored) {
                // 季节模组内部出错时退回原版判定
            }
        }
        return level.getBiome(pos).value().warmEnoughToRain(pos);
    }

    /** 配置值缓存：这个判定在随机刻热路径上，不必每次都做两次反射读。 */
    private static boolean cachedSnowEnabled = true;
    private static int refreshCountdown;

    /** 对应季节模组配置里的 {@code generateSnowAndIce}：关掉时它自己也不用季节温度。 */
    private static boolean seasonalSnowEnabled() {
        if (refreshCountdown-- > 0) {
            return cachedSnowEnabled;
        }
        refreshCountdown = 200;
        cachedSnowEnabled = readSnowEnabled();
        return cachedSnowEnabled;
    }

    private static boolean readSnowEnabled() {
        if (CONFIG_SEASONS == null || CONFIG_GENERATE_SNOW == null) {
            return true;
        }
        try {
            Object config = CONFIG_SEASONS.get(null);
            if (config == null) {
                // 配置还没加载（启动早期），按默认值处理
                return true;
            }
            return CONFIG_GENERATE_SNOW.getBoolean(config);
        } catch (Throwable ignored) {
            return true;
        }
    }
}
