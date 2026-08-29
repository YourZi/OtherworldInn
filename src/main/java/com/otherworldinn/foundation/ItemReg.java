package com.otherworldinn.foundation;

import com.otherworldinn.init.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;

/** 物品注册构建器，链式配置属性与 DataGen 信息。 */
public class ItemReg<T extends Item> {
    private final String name;
    private final Function<Item.Properties, T> itemFactory;
    private Item.Properties properties = new Item.Properties();

    private boolean generateModel = true;
    private String modelType = "generated"; // 默认为 "generated"，也可选 "handheld"

    private String enName = "";
    private String cnName = "";
    private final List<String> enTooltips = new ArrayList<>();
    private final List<String> cnTooltips = new ArrayList<>();

    public ItemReg(String name, Function<Item.Properties, T> itemFactory) {
        this.name = name;
        this.itemFactory = itemFactory;
    }

    // --- 属性配置 (Properties) ---

    public ItemReg<T> properties(UnaryOperator<Item.Properties> operator) {
        this.properties = operator.apply(this.properties);
        return this;
    }

    public ItemReg<T> stacksTo(int count) {
        this.properties.stacksTo(count);
        return this;
    }

    public ItemReg<T> durability(int durability) {
        this.properties.durability(durability);
        return this;
    }

    public ItemReg<T> rarity(Rarity rarity) {
        this.properties.rarity(rarity);
        return this;
    }

    public ItemReg<T> fireResistant() {
        this.properties.fireResistant();
        return this;
    }

    public <V> ItemReg<T> component(DataComponentType<V> type, V value) {
        this.properties.component(type, value);
        return this;
    }

    // --- DataGen配置 (DataGen) ---

    public ItemReg<T> handheld() {
        this.modelType = "handheld";
        return this;
    }

    public ItemReg<T> noModel() {
        this.generateModel = false;
        return this;
    }

    // --- 语言与工具提示 (Language & Tooltips) ---

    public ItemReg<T> lang(String enName) {
        this.enName = enName;
        if (this.cnName.isEmpty()) {
            this.cnName = enName;
        }
        return this;
    }

    public ItemReg<T> lang(String enName, String cnName) {
        this.enName = enName;
        this.cnName = cnName;
        return this;
    }

    public ItemReg<T> tooltip(String enTooltip) {
        this.enTooltips.add(enTooltip);
        this.cnTooltips.add(enTooltip); // 如果未指定中文Tooltip，默认使用英文
        return this;
    }

    public ItemReg<T> tooltip(String enTooltip, String cnTooltip) {
        this.enTooltips.add(enTooltip);
        this.cnTooltips.add(cnTooltip);
        return this;
    }

    /** 注册物品，必须调用此方法以完成注册。 */
    public DeferredItem<T> register() {
        Supplier<T> itemSupplier = () -> this.itemFactory.apply(this.properties);
        DeferredItem<T> item = ModItems.ITEMS.register(name, itemSupplier);

        ModItems.ITEM_INFOS.put(
                item,
                new ItemDataGenInfo(
                        generateModel, modelType, enName, cnName, enTooltips, cnTooltips));

        return item;
    }
}
