package com.otherworldinn.mixin;

import com.otherworldinn.world.snow.SnowCoverServer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 把真实覆雪的融化接到 Serene Seasons 自己的融雪流程上。
 *
 * <p>SS 的 {@code RandomUpdateHandler#meltInChunk} 每个区块每次按配置（{@code melt_rolls} /
 * {@code melt_percent}）掷骰，掷中后取 {@code getHeightmapPos(MOTION_BLOCKING, 随机列)}
 * 作为要融化的方块位置。我们在这一步把它算出来的那一列也交给覆雪系统融化——
 * 于是：
 * <ul>
 *   <li>融化节奏、概率、季节开关全都由 SS 的配置决定，我们不再自己判断温度/季节；</li>
 *   <li>SS 某个季节 {@code melt_rolls = 0}（比如冬天）时，我们的雪也不会化；</li>
 *   <li>只化它选中的那一列，天然是零散、渐进的。</li>
 * </ul>
 * 没装 SS 时这个 mixin 会被 {@code @Pseudo} 跳过，覆雪只保留原版"方块光 &gt; 11 才化"的规则。
 */
@Pseudo
@Mixin(targets = "sereneseasons.season.RandomUpdateHandler", remap = false)
public class MixinRandomUpdateHandlerSnowMelt {

    @Redirect(
            method = "meltInChunk",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/server/level/ServerLevel;getHeightmapPos(Lnet/minecraft/world/level/levelgen/Heightmap$Types;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;"),
            remap = false)
    private static BlockPos otherworldinn$meltSnowCoverToo(
            ServerLevel level, Heightmap.Types type, BlockPos column) {
        BlockPos snowPos = level.getHeightmapPos(type, column);
        SnowCoverServer.meltColumn(level, snowPos);
        return snowPos;
    }
}
