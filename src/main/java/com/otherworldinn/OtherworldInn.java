package com.otherworldinn;

import com.mojang.logging.LogUtils;
import com.otherworldinn.block.HoneyCauldronInteractions;
import com.otherworldinn.compat.CreateCompat;
import com.otherworldinn.compat.ExposureCompat;
import com.otherworldinn.compat.HoneyCauldronCompat;
import com.otherworldinn.compat.KaleidoscopeCompat;
import com.otherworldinn.compat.ReskillableCompat;
import com.otherworldinn.compat.WaystonesCompat;
import com.otherworldinn.foundation.ClientConfig;
import com.otherworldinn.init.ModBlocks;
import com.otherworldinn.init.ModAttachments;
import com.otherworldinn.init.ModCreativeModeTabs;
import com.otherworldinn.init.ModDataComponents;
import com.otherworldinn.init.ModDimensions;
import com.otherworldinn.init.ModEntities;
import com.otherworldinn.init.ModGameRules;
import com.otherworldinn.init.ModItems;
import com.otherworldinn.init.ModLootModifiers;
import com.otherworldinn.init.ModMenuTypes;
import com.otherworldinn.init.ModSounds;
import com.otherworldinn.init.ModStats;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

/** 模组主类：入口点，负责各类注册表、客户端配置与生命周期事件的注册。 */
@Mod(OtherworldInn.MODID)
public class OtherworldInn {
    public static final String MODID = "otherworldinn";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final TagKey<Item> BANNED_IN_TOWN =
            TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(MODID, "banned_in_town"));
    public static final TagKey<Item> ONLY_IN_TOWN =
            TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(MODID, "only_in_town"));
    public static final TagKey<Block> INN_FREE_INTERACT =
            TagKey.create(
                    Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(MODID, "inn_free_interact"));
    public static final TagKey<Block> PROTECTED_ZONE_NATURAL_CONVERSIONS =
            TagKey.create(
                    Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(MODID, "protected_zone_natural_conversions"));
    /**
     * 保护区内允许玩家/女仆清除的方块（房间整洁类，如 {@code otherworldinn:clutter}）：命中该标签的方块
     * 变成空气时底层结构性写入闸门一律放行，避免"事件层放行、写入层静默拒绝"导致清不掉。
     */
    public static final TagKey<Block> PROTECTED_ZONE_PLAYER_CLEANABLE =
            TagKey.create(
                    Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(MODID, "protected_zone_player_cleanable"));
    /**
     * 保护区内禁止玩家右键交互的方块（默认空）。用于第三方方块：其 {@code useItemOn} 用
     * {@code setBlock(..., AIR)} 删除自己却忽略返回值，保护区拒绝删除后方块仍在、产物照发，可被无限刷取。
     */
    public static final TagKey<Block> PROTECTED_ZONE_DENY_INTERACT =
            TagKey.create(
                    Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(MODID, "protected_zone_deny_interact"));

    public OtherworldInn(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModDataComponents.DATA_COMPONENT_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);
        ModCreativeModeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModDimensions.register(modEventBus);
        ModLootModifiers.LOOT_MODIFIER_SERIALIZERS.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModStats.CUSTOM_STATS.register(modEventBus);
        ModGameRules.init();

        // 注册配置
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        // 模组列表里的"配置"按钮 → GUI 动效开关（仅客户端）
        if (net.neoforged.fml.loading.FMLEnvironment.dist
                == net.neoforged.api.distmarker.Dist.CLIENT) {
            modContainer.registerExtensionPoint(
                    net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,
                    (net.neoforged.neoforge.client.gui.IConfigScreenFactory)
                            (container, parent) ->
                                    new com.otherworldinn.client.gui.screen.GuiAnimationConfigScreen(parent));
        }

        // 注册生命周期事件
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(HoneyCauldronInteractions::bootstrap);
        event.enqueueWork(HoneyCauldronCompat::init);

        // Create 兼容性初始化
        if (ModList.get().isLoaded("create")) {
            event.enqueueWork(CreateCompat::init);
        }
        if (ExposureCompat.isLoaded()) {
            event.enqueueWork(ExposureCompat::init);
        }
        event.enqueueWork(KaleidoscopeCompat::init);
        event.enqueueWork(ReskillableCompat::init);
        if (ModList.get().isLoaded("waystones")) {
            event.enqueueWork(WaystonesCompat::init);
        }
    }
}
