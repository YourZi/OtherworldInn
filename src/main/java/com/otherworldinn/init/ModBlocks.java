package com.otherworldinn.init;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.block.CommissionBoardBlock;
import com.otherworldinn.block.CrystalBallBlock;
import com.otherworldinn.foundation.BlockDataGenInfo;
import com.otherworldinn.foundation.BlockReg;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 方块注册中心
 *
 * <p>负责注册模组中的所有方块。 同时注册对应的方块物品（如果启用）。
 */
public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(OtherworldInn.MODID);

    /** 存储方块的数据生成信息，用于 DataGen */
    public static final Map<DeferredBlock<?>, BlockDataGenInfo> BLOCK_INFOS = new HashMap<>();

    // --- 方块注册 ---

    public static final BlockReg<CommissionBoardBlock> COMMISSION_BOARD_REG =
            register("commission_board", CommissionBoardBlock::new)
                    .properties(props -> props.strength(114514.0F).noOcclusion())
                    .cutout()
                    .noModel()
                    .lang("Commission Board", "委托板");
    public static final DeferredBlock<CommissionBoardBlock> COMMISSION_BOARD =
            COMMISSION_BOARD_REG.register();

    public static final BlockReg<CrystalBallBlock> CRYSTAL_BALL_REG =
            register("crystal_ball", CrystalBallBlock::new)
                    .properties(props -> props.strength(2.0F).noOcclusion())
                    .cutout()
                    .lang("Crystal Ball", "水晶球")
                    .tooltip("Right-click to enter the Magic Space", "右键进入魔法空间")
                    .tooltip("Only one Crystal Ball can exist in the world at a time", "同一存档只能放置一个水晶球");
    public static final DeferredBlock<CrystalBallBlock> CRYSTAL_BALL =
            CRYSTAL_BALL_REG.register();

    // --- 辅助方法 ---

    /**
     * 开始一个方块的链式注册
     *
     * @param name 方块注册名
     * @param factory 方块工厂方法 (例如 Block::new)
     * @param <T> 方块类型
     * @return BlockReg 构建器
     */
    public static <T extends Block> BlockReg<T> register(
            String name, Function<BlockBehaviour.Properties, T> factory) {
        return new BlockReg<>(name, factory);
    }
}
