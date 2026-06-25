package com.otherworldinn.init;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.compat.waystones.WaystonesClientCompat;
import com.otherworldinn.client.gui.screen.StoreScreen;
import com.otherworldinn.client.gui.screen.WanderingTraderRecycleScreen;
import com.otherworldinn.client.renderer.BlacksmithModel;
import com.otherworldinn.client.renderer.BlacksmithRenderer;
import com.otherworldinn.client.renderer.BuilderModel;
import com.otherworldinn.client.renderer.BuilderRenderer;
import com.otherworldinn.client.renderer.ButcherModel;
import com.otherworldinn.client.renderer.ButcherRenderer;
import com.otherworldinn.client.renderer.FarmerModel;
import com.otherworldinn.client.renderer.FarmerRenderer;
import com.otherworldinn.client.renderer.FishermanModel;
import com.otherworldinn.client.renderer.FishermanRenderer;
import com.otherworldinn.client.renderer.GrocerModel;
import com.otherworldinn.client.renderer.GrocerRenderer;
import com.otherworldinn.client.renderer.GuestRenderer;
import com.otherworldinn.client.renderer.MagicianModel;
import com.otherworldinn.client.renderer.MagicianRenderer;
import com.otherworldinn.client.renderer.WanderingTraderModel;
import com.otherworldinn.client.renderer.WanderingTraderRenderer;
import com.otherworldinn.item.RoomKeyItem;
import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import com.otherworldinn.client.render.SlotIconRenderer;

/**
 * 模组客户端事件处理器
 *
 * <p>处理仅限客户端的事件，例如按键绑定注册。
 */
@EventBusSubscriber(
        modid = OtherworldInn.MODID,
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.MOD)
public class ModClientEvents {
    private static final ModelResourceLocation COLD_CUT_HAM_SLICES_FORCED_MODEL_PATH =
            ModelResourceLocation.standalone(
                    ResourceLocation.fromNamespaceAndPath(
                            "kaleidoscope_cookery", "item/cold_cut_ham_slices_block"));

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(
                () -> {
                    ItemProperties.register(
                            ModItems.ROOM_REGISTER.get(),
                            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "offhand"),
                            (stack, level, entity, seed) -> {
                                if (entity == null) return 0.0F;
                                return entity.getOffhandItem() == stack ? 1.0F : 0.0F;
                            });
                    if (ModList.get().isLoaded("waystones")) {
                        WaystonesClientCompat.init();
                    }
                });
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ORDINARY_GUEST.get(), GuestRenderer::new);
        event.registerEntityRenderer(ModEntities.RICH_GUEST.get(), GuestRenderer::new);
        event.registerEntityRenderer(ModEntities.HEAVY_PACK_GUEST.get(), GuestRenderer::new);
        event.registerEntityRenderer(ModEntities.ULTRA_RICH_GUEST.get(), GuestRenderer::new);
        event.registerEntityRenderer(ModEntities.ORDINARY_VIP_GUEST.get(), GuestRenderer::new);
        event.registerEntityRenderer(ModEntities.ADVANCED_VIP_GUEST.get(), GuestRenderer::new);
        event.registerEntityRenderer(ModEntities.SPONSOR_GUEST.get(), GuestRenderer::new);
        event.registerEntityRenderer(ModEntities.STORY_GUEST.get(), GuestRenderer::new);
        event.registerEntityRenderer(ModEntities.BLACKSMITH.get(), BlacksmithRenderer::new);
        event.registerEntityRenderer(ModEntities.MAGICIAN.get(), MagicianRenderer::new);
        event.registerEntityRenderer(ModEntities.FARMER.get(), FarmerRenderer::new);
        event.registerEntityRenderer(ModEntities.GROCER.get(), GrocerRenderer::new);
        event.registerEntityRenderer(ModEntities.BUTCHER.get(), ButcherRenderer::new);
        event.registerEntityRenderer(ModEntities.BUILDER.get(), BuilderRenderer::new);
        event.registerEntityRenderer(ModEntities.FISHERMAN.get(), FishermanRenderer::new);
        event.registerEntityRenderer(ModEntities.WANDERING_TRADER.get(), WanderingTraderRenderer::new);
        event.registerEntityRenderer(ModEntities.COIN_PROJECTILE.get(), ThrownItemRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(
            EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
                BlacksmithModel.LAYER_LOCATION, BlacksmithModel::createBodyLayer);
        event.registerLayerDefinition(
                MagicianModel.LAYER_LOCATION, MagicianModel::createBodyLayer);
        event.registerLayerDefinition(FarmerModel.LAYER_LOCATION, FarmerModel::createBodyLayer);
        event.registerLayerDefinition(GrocerModel.LAYER_LOCATION, GrocerModel::createBodyLayer);
        event.registerLayerDefinition(ButcherModel.LAYER_LOCATION, ButcherModel::createBodyLayer);
        event.registerLayerDefinition(BuilderModel.LAYER_LOCATION, BuilderModel::createBodyLayer);
        event.registerLayerDefinition(FishermanModel.LAYER_LOCATION, FishermanModel::createBodyLayer);
        event.registerLayerDefinition(WanderingTraderModel.LAYER_LOCATION, WanderingTraderModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.STORE_MENU.get(), StoreScreen::new);
        event.register(ModMenuTypes.WANDERING_TRADER_RECYCLE.get(), WanderingTraderRecycleScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(
            net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent event) {
        event.register(ModKeyBindings.TOGGLE_MAP_MODE);
    }

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(COLD_CUT_HAM_SLICES_FORCED_MODEL_PATH);
    }

    @SubscribeEvent
    public static void onRegisterItemDecorations(RegisterItemDecorationsEvent event) {
        event.register(
                ModItems.ROOM_KEY.get(),
                (guiGraphics, font, stack, x, y) -> {
                    if (!RoomKeyItem.isBoundRoomFull(stack)) {
                        return false;
                    }
                    SlotIconRenderer.renderCentered(guiGraphics,
                            new ItemStack(Items.BARRIER), x, y, 0, 1.0f);
                    return false;
                });
    }
}
