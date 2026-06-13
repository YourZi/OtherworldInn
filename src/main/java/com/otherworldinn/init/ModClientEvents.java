package com.otherworldinn.init;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.client.gui.screen.StoreScreen;
import com.otherworldinn.client.renderer.BlacksmithModel;
import com.otherworldinn.client.renderer.BlacksmithRenderer;
import com.otherworldinn.client.renderer.FarmerModel;
import com.otherworldinn.client.renderer.FarmerRenderer;
import com.otherworldinn.client.renderer.GrocerModel;
import com.otherworldinn.client.renderer.GrocerRenderer;
import com.otherworldinn.client.renderer.GuestRenderer;
import com.otherworldinn.client.renderer.MagicianModel;
import com.otherworldinn.client.renderer.MagicianRenderer;
  import com.otherworldinn.init.ModItems;
import com.otherworldinn.item.RoomKeyItem;
import com.otherworldinn.item.ChartComponentItem;
import com.otherworldinn.world.expedition.ChartComponentType;
import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
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
        event.registerEntityRenderer(ModEntities.BLACKSMITH.get(), BlacksmithRenderer::new);
        event.registerEntityRenderer(ModEntities.MAGICIAN.get(), MagicianRenderer::new);
        event.registerEntityRenderer(ModEntities.FARMER.get(), FarmerRenderer::new);
        event.registerEntityRenderer(ModEntities.GROCER.get(), GrocerRenderer::new);
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
    }

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.STORE_MENU.get(), StoreScreen::new);
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

        event.register(
                ModItems.CHART_COMPONENT.get(),
                (guiGraphics, font, stack, x, y) -> {
                    String compType = ChartComponentItem.getComponentType(stack);
                    if ("blank".equals(compType)) return false;
                    ChartComponentType type = ChartComponentType.byId(compType);
                    if (type == null) return false;
                    SlotIconRenderer.renderCentered(guiGraphics,
                            new ItemStack(type.iconItem()), x, y, 0, 0.7f);
                    return false;
                });
    }
}
