package com.otherworldinn.init;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.foundation.ItemDataGenInfo;
import com.otherworldinn.foundation.ItemReg;
import com.otherworldinn.item.BedSheetItem;
import com.otherworldinn.item.CoinItem;
import com.otherworldinn.item.EndSpaceSphereItem;
import com.otherworldinn.item.InnKeyItem;
import com.otherworldinn.item.InnUpgradeVoucherItem;
import com.otherworldinn.item.LandDeedItem;
import com.otherworldinn.item.MessyBedSheetItem;
import com.otherworldinn.item.NetherSpaceSphereItem;
import com.otherworldinn.item.RecallScrollItem;
import com.otherworldinn.item.RoomKeyItem;
import com.otherworldinn.item.RoomRegisterItem;
import com.otherworldinn.item.SpaceSphereItem;
import com.otherworldinn.item.ChartComponentItem;
import com.otherworldinn.item.ExpeditionChartItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 物品注册中心
 *
 * <p>负责注册模组中的所有物品。
 */
public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(OtherworldInn.MODID);

    /** 存储物品的数据生成信息，用于 DataGen */
    public static final Map<DeferredItem<?>, ItemDataGenInfo> ITEM_INFOS = new HashMap<>();

    // --- 物品注册 ---

    public static final ItemReg<RecallScrollItem> RECALL_SCROLL_REG =
            new ItemReg<>("recall_scroll", RecallScrollItem::new)
                    .rarity(Rarity.EPIC)
                    .stacksTo(1)
                    .fireResistant()
                    .lang("Recall Scroll", "回程卷轴")
                    .tooltip(
                            "Teleports back to the inn after using for 3 seconds",
                            "持续使用3秒后传送回旅社位置");
    public static final DeferredItem<RecallScrollItem> RECALL_SCROLL = RECALL_SCROLL_REG.register();

    public static final ItemReg<RoomRegisterItem> ROOM_REGISTER_REG =
            new ItemReg<>("room_register", RoomRegisterItem::new)
                    .rarity(Rarity.UNCOMMON)
                    .stacksTo(1)
                    .noModel()
                    .lang("Room Register", "房间登记册")
                    .tooltip("Hold in off-hand to edit room", "副手手持来编辑房间");
    public static final DeferredItem<RoomRegisterItem> ROOM_REGISTER = ROOM_REGISTER_REG.register();

    public static final ItemReg<RoomKeyItem> ROOM_KEY_REG =
            new ItemReg<>("room_key", RoomKeyItem::new)
                    .rarity(Rarity.COMMON)
                    .stacksTo(16)
                    .lang("Room Key", "房间钥匙")
                    .tooltip("Right click room to bind", "右键房间绑定，左键解绑")
                    .component(
                            DataComponents.ATTRIBUTE_MODIFIERS,
                            new ItemAttributeModifiers(
                                    List.of(
                                            new ItemAttributeModifiers.Entry(
                                                    Attributes.ENTITY_INTERACTION_RANGE,
                                                    new AttributeModifier(
                                                            ResourceLocation.fromNamespaceAndPath(
                                                                    OtherworldInn.MODID,
                                                                    "room_key_entity_interaction_range"),
                                                            3.0,
                                                            AttributeModifier.Operation.ADD_VALUE),
                                                    EquipmentSlotGroup.MAINHAND)),
                                    true));
    public static final DeferredItem<RoomKeyItem> ROOM_KEY = ROOM_KEY_REG.register();

    public static final ItemReg<BedSheetItem> BED_SHEET_REG =
            new ItemReg<>("bed_sheet", BedSheetItem::new)
                    .rarity(Rarity.COMMON)
                    .stacksTo(16)
                    .durability(64)
                    .lang("Bed Sheet", "床单")
                    .tooltip("Use to replace bed sheets for a messy bed", "对脏乱的床铺使用以更换床单");
    public static final DeferredItem<BedSheetItem> BED_SHEET = BED_SHEET_REG.register();

    public static final ItemReg<MessyBedSheetItem> MESSY_BED_SHEET_REG =
            new ItemReg<>("messy_bed_sheet", MessyBedSheetItem::new)
                    .rarity(Rarity.COMMON)
                    .stacksTo(16)
                    .durability(64)
                    .lang("Messy Bed Sheet", "脏乱的床单")
                    .tooltip("Use it in water to clean", "在水中使用以清洗");
    public static final DeferredItem<MessyBedSheetItem> MESSY_BED_SHEET =
            MESSY_BED_SHEET_REG.register();

    public static final ItemReg<LandDeedItem> LAND_DEED_REG =
            new ItemReg<>("land_deed", LandDeedItem::new)
                    .rarity(Rarity.RARE)
                    .stacksTo(1)
                    .lang("Land Deed", "地契")
                    .tooltip("Use on two corners to expand Inn area", "在两个角落使用以扩展旅社区域");
    public static final DeferredItem<LandDeedItem> LAND_DEED = LAND_DEED_REG.register();

    public static final ItemReg<InnKeyItem> INN_KEY_REG =
            new ItemReg<>("inn_key", InnKeyItem::new)
                    .rarity(Rarity.RARE)
                    .stacksTo(1)
                    .lang("Inn Key", "旅社钥匙")
                    .tooltip("Right click Desk Bell to toggle Inn state", "潜行右键前台铃铛以切换旅社状态");
    public static final DeferredItem<InnKeyItem> INN_KEY = INN_KEY_REG.register();

    public static final ItemReg<InnUpgradeVoucherItem> INN_UPGRADE_VOUCHER_REG =
            new ItemReg<>("inn_upgrade_voucher", InnUpgradeVoucherItem::new)
                    .rarity(Rarity.RARE)
                    .stacksTo(16)
                    .lang("Inn Upgrade Voucher", "旅社升级凭证")
                    .tooltip(
                            "Right click to attempt upgrading Inn rating",
                            "右键提升旅社评级");
    public static final DeferredItem<InnUpgradeVoucherItem> INN_UPGRADE_VOUCHER =
            INN_UPGRADE_VOUCHER_REG.register();

    public static final ItemReg<Item> FACILITY_UPGRADE_TEMPLATE_REG =
            register("facility_upgrade_template")
                    .rarity(Rarity.UNCOMMON)
                    .stacksTo(16)
                    .lang("Facility Upgrade Template", "设施升级模板")
                    .tooltip(
                            "Used for repairing or upgrading facilities. The upgraded facilities will reset all blocks inside, ",
                            "用于维修或升级设施，升级后的设施会重置内部的所有方块")
                    .tooltip(
                            "so please ensure that no important blocks are placed inside the facilities.",
                            "，请确保没有重要方块放置在设施内");
    public static final DeferredItem<Item> FACILITY_UPGRADE_TEMPLATE =
            FACILITY_UPGRADE_TEMPLATE_REG.register();

    public static final ItemReg<CoinItem> COIN_REG =
            register("coin", CoinItem::new)
                    .rarity(Rarity.COMMON)
                    .stacksTo(64)
                    .lang("Coin", "金币")
                    .tooltip("Sneak + Right Click to deposit into team balance", "潜行右键将金币存入队伍余额")
                    .tooltip(
                            "§7I wonder if you want to toss it somewhere...",
                            "§7难道你想把它投到什么地方吗...");
    public static final DeferredItem<CoinItem> COIN = COIN_REG.register();

    public static final ItemReg<SpaceSphereItem> SPACE_SPHERE_REG =
            new ItemReg<>("space_sphere", SpaceSphereItem::new)
                    .rarity(Rarity.EPIC)
                    .stacksTo(1)
                    .lang("Space Sphere", "空间球")
                    .tooltip("Use to activate map teleport for your team", "使用后激活地图点传送功能");
    public static final DeferredItem<SpaceSphereItem> SPACE_SPHERE = SPACE_SPHERE_REG.register();

    public static final ItemReg<NetherSpaceSphereItem> NETHER_SPACE_SPHERE_REG =
            new ItemReg<>("nether_space_sphere", NetherSpaceSphereItem::new)
                    .rarity(Rarity.EPIC)
                    .stacksTo(16)
                    .lang("Nether Space Sphere", "下界空间球");
    public static final DeferredItem<NetherSpaceSphereItem> NETHER_SPACE_SPHERE =
            NETHER_SPACE_SPHERE_REG.register();

    public static final ItemReg<EndSpaceSphereItem> END_SPACE_SPHERE_REG =
            new ItemReg<>("end_space_sphere", EndSpaceSphereItem::new)
                    .rarity(Rarity.EPIC)
                    .stacksTo(16)
                    .lang("End Space Sphere", "末地空间球");
    public static final DeferredItem<EndSpaceSphereItem> END_SPACE_SPHERE =
            END_SPACE_SPHERE_REG.register();

    public static final ItemReg<ExpeditionChartItem> PIONEER_CHART_REG =
            new ItemReg<>("pioneer_chart", ExpeditionChartItem::new)
                    .rarity(Rarity.RARE)
                    .stacksTo(1)
                    .lang("Pioneer Chart", "开拓者星图")
                    .tooltip("Add Chart Components via crafting to customize the expedition", "通过合成附加星图组件来定制远征");
    public static final DeferredItem<ExpeditionChartItem> PIONEER_CHART =
            PIONEER_CHART_REG.register();

    public static final ItemReg<ChartComponentItem> CHART_COMPONENT_REG =
            new ItemReg<>("chart_component", ChartComponentItem::new)
                    .rarity(Rarity.COMMON)
                    .stacksTo(64)
                    .lang("Chart Component", "星图组件")
                    .tooltip("Craft with a blank component + materials to create a specific component", "使用空白组件+特定材料合成特定组件")
                    .tooltip("Attach to a Pioneer Chart to customize expedition terrain", "附加到开拓者星图以定制远征地形");
    public static final DeferredItem<ChartComponentItem> CHART_COMPONENT =
            CHART_COMPONENT_REG.register();

    // --- 辅助方法 ---

    /**
     * 开始一个物品的链式注册 (自定义物品类)
     *
     * @param name 物品注册名
     * @param factory 物品工厂方法 (例如 CustomItem::new)
     * @param <T> 物品类型
     * @return ItemReg 构建器
     */
    public static <T extends Item> ItemReg<T> register(
            String name, Function<Item.Properties, T> factory) {
        return new ItemReg<>(name, factory);
    }

    /**
     * 开始一个普通物品的链式注册 (使用默认 Item 类)
     *
     * @param name 物品注册名
     * @return ItemReg 构建器
     */
    public static ItemReg<Item> register(String name) {
        return new ItemReg<>(name, Item::new);
    }
}
