package com.otherworldinn.foundation;

import com.otherworldinn.init.ModBlocks;
import com.otherworldinn.init.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;

/** 方块注册构建器，链式配置属性、物品与 DataGen 信息。 */
public class BlockReg<T extends Block> {
    private final String name;
    private final Function<BlockBehaviour.Properties, T> blockFactory;
    private BlockBehaviour.Properties properties = BlockBehaviour.Properties.of();

    private boolean hasItem = true;
    private Item.Properties itemProperties = new Item.Properties();

    private boolean generateModel = true;
    private String renderType = "solid";

    private BlockDataGenInfo.ToolType toolType = BlockDataGenInfo.ToolType.NONE;
    private BlockDataGenInfo.MiningLevel miningLevel = BlockDataGenInfo.MiningLevel.NONE;

    private LootConfig lootConfig = LootConfig.DEFAULT;

    private String enName = "";
    private String cnName = "";
    private final List<String> enTooltips = new ArrayList<>();
    private final List<String> cnTooltips = new ArrayList<>();

    public BlockReg(String name, Function<BlockBehaviour.Properties, T> blockFactory) {
        this.name = name;
        this.blockFactory = blockFactory;
    }

    // --- 属性配置 (Properties) ---

    public BlockReg<T> properties(UnaryOperator<BlockBehaviour.Properties> operator) {
        this.properties = operator.apply(this.properties);
        return this;
    }

    public BlockReg<T> copyProperties(Block block) {
        this.properties = BlockBehaviour.Properties.ofFullCopy(block);
        return this;
    }

    public BlockReg<T> hardness(float hardness) {
        this.properties.strength(hardness);
        return this;
    }

    public BlockReg<T> strength(float hardness, float resistance) {
        this.properties.strength(hardness, resistance);
        return this;
    }

    public BlockReg<T> noCollision() {
        this.properties.noCollission();
        return this;
    }

    public BlockReg<T> noOcclusion() {
        this.properties.noOcclusion();
        return this;
    }

    public BlockReg<T> sound(SoundType soundType) {
        this.properties.sound(soundType);
        return this;
    }

    // --- 语言与工具提示 (Language & Tooltips) ---

    public BlockReg<T> lang(String enName) {
        this.enName = enName;
        // 如果未指定中文名，默认使用英文名
        if (this.cnName.isEmpty()) {
            this.cnName = enName;
        }
        return this;
    }

    public BlockReg<T> lang(String enName, String cnName) {
        this.enName = enName;
        this.cnName = cnName;
        return this;
    }

    public BlockReg<T> tooltip(String enTooltip) {
        this.enTooltips.add(enTooltip);
        this.cnTooltips.add(enTooltip); // 如果未指定中文Tooltip，默认使用英文
        return this;
    }

    public BlockReg<T> tooltip(String enTooltip, String cnTooltip) {
        this.enTooltips.add(enTooltip);
        this.cnTooltips.add(cnTooltip);
        return this;
    }

    // --- 物品配置 (Item) ---

    public BlockReg<T> withItem() {
        this.hasItem = true;
        return this;
    }

    public BlockReg<T> withItem(Item.Properties properties) {
        this.hasItem = true;
        this.itemProperties = properties;
        return this;
    }

    public BlockReg<T> itemProperties(UnaryOperator<Item.Properties> operator) {
        this.hasItem = true;
        this.itemProperties = operator.apply(this.itemProperties);
        return this;
    }

    public BlockReg<T> rarity(Rarity rarity) {
        this.hasItem = true;
        this.itemProperties.rarity(rarity);
        return this;
    }

    public BlockReg<T> stacksTo(int size) {
        this.hasItem = true;
        this.itemProperties.stacksTo(size);
        return this;
    }

    public BlockReg<T> fireResistant() {
        this.hasItem = true;
        this.itemProperties.fireResistant();
        return this;
    }

    // --- DataGen配置 (DataGen) ---

    public BlockReg<T> noBlockItem() {
        this.hasItem = false;
        return this;
    }

    public BlockReg<T> translucent() {
        this.renderType = "translucent";
        return this;
    }

    public BlockReg<T> cutout() {
        this.renderType = "cutout";
        return this;
    }

    public BlockReg<T> cutoutMipped() {
        this.renderType = "cutout_mipped";
        return this;
    }

    public BlockReg<T> noModel() {
        this.generateModel = false;
        return this;
    }

    // --- 战利品表 (Loot Tables) ---

    public BlockReg<T> noLoot() {
        this.lootConfig = LootConfig.EMPTY;
        return this;
    }

    public BlockReg<T> loot(String itemId, int min, int max, boolean silkTouch) {
        return loot(itemId, 1.0f, min, max, silkTouch);
    }

    public BlockReg<T> loot(String itemId, float chance, int min, int max, boolean silkTouch) {
        if (this.lootConfig == LootConfig.DEFAULT || this.lootConfig == LootConfig.EMPTY) {
            this.lootConfig =
                    new LootConfig(LootConfig.LootType.CUSTOM, new ArrayList<>(), silkTouch);
        }

        // 如果任意一个条目启用了 silkTouch 行为（类似矿石），则更新全局配置
        if (silkTouch) {
            this.lootConfig =
                    new LootConfig(this.lootConfig.type(), this.lootConfig.entries(), true);
        }

        this.lootConfig.entries().add(new LootConfig.LootEntry(itemId, chance, min, max, false));
        return this;
    }

    public BlockReg<T> loot(String itemId, int min, int max) {
        return loot(itemId, 1.0f, min, max, false);
    }

    public BlockReg<T> loot(String itemId) {
        return loot(itemId, 1.0f, 1, 1, false);
    }

    // --- 挖掘工具与等级 (Tools & Mining) ---

    public BlockReg<T> requiresTool(
            BlockDataGenInfo.ToolType tool, BlockDataGenInfo.MiningLevel level) {
        this.toolType = tool;
        this.miningLevel = level;
        return this;
    }

    public BlockReg<T> pickaxe() {
        this.toolType = BlockDataGenInfo.ToolType.PICKAXE;
        return this;
    }

    public BlockReg<T> axe() {
        this.toolType = BlockDataGenInfo.ToolType.AXE;
        return this;
    }

    public BlockReg<T> shovel() {
        this.toolType = BlockDataGenInfo.ToolType.SHOVEL;
        return this;
    }

    public BlockReg<T> hoe() {
        this.toolType = BlockDataGenInfo.ToolType.HOE;
        return this;
    }

    public BlockReg<T> needsStone() {
        this.miningLevel = BlockDataGenInfo.MiningLevel.STONE;
        return this;
    }

    public BlockReg<T> needsIron() {
        this.miningLevel = BlockDataGenInfo.MiningLevel.IRON;
        return this;
    }

    public BlockReg<T> needsDiamond() {
        this.miningLevel = BlockDataGenInfo.MiningLevel.DIAMOND;
        return this;
    }

    public BlockReg<T> needsNetherite() {
        this.miningLevel = BlockDataGenInfo.MiningLevel.NETHERITE;
        return this;
    }

    /** 注册方块，必须调用此方法以完成注册。 */
    public DeferredBlock<T> register() {
        Supplier<T> blockSupplier = () -> this.blockFactory.apply(this.properties);

        DeferredBlock<T> block = ModBlocks.BLOCKS.register(name, blockSupplier);

        if (hasItem) {
            ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), itemProperties));
        }

        ModBlocks.BLOCK_INFOS.put(
                block,
                new BlockDataGenInfo(
                        generateModel,
                        renderType,
                        lootConfig,
                        toolType,
                        miningLevel,
                        enName,
                        cnName,
                        enTooltips,
                        cnTooltips));

        return block;
    }
}
