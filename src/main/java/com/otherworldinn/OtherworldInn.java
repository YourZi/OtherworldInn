package com.otherworldinn;

import com.mojang.logging.LogUtils;
import com.otherworldinn.compat.CreateCompat;
import com.otherworldinn.compat.KaleidoscopeCompat;
import com.otherworldinn.compat.ReskillableCompat;
import com.otherworldinn.foundation.ClientConfig;
import com.otherworldinn.init.ModBlocks;
import com.otherworldinn.init.ModCreativeModeTabs;
import com.otherworldinn.init.ModDimensions;
import com.otherworldinn.init.ModEntities;
import com.otherworldinn.init.ModGameRules;
import com.otherworldinn.init.ModItems;
import com.otherworldinn.init.ModLootModifiers;
import com.otherworldinn.init.ModMenuTypes;
import com.otherworldinn.init.ModSounds;
import com.otherworldinn.world.expedition.recipe.ExpeditionRecipeSerializers;
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

/**
 * 模组主类
 *
 * <p>模组的入口点，负责初始化和注册。
 */
@Mod(OtherworldInn.MODID)
public class OtherworldInn {
    public static final String MODID = "otherworldinn";
    public static final Logger LOGGER = LogUtils.getLogger();

    // 定义标签
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

    public OtherworldInn(IEventBus modEventBus, ModContainer modContainer) {
        // 注册物品和方块
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);
        ModCreativeModeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModDimensions.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModLootModifiers.GLM.register(modEventBus);
        ExpeditionRecipeSerializers.register(modEventBus);
        ModGameRules.init();

        // 注册配置
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

        // 注册生命周期事件
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Create 兼容性初始化
        if (ModList.get().isLoaded("create")) {
            event.enqueueWork(CreateCompat::init);
        }
        event.enqueueWork(KaleidoscopeCompat::init);
        event.enqueueWork(ReskillableCompat::init);
    }
}
