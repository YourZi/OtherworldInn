package com.otherworldinn.init;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.world.inventory.StoreMenu;
import com.otherworldinn.world.inventory.WanderingTraderRecycleMenu;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(BuiltInRegistries.MENU, OtherworldInn.MODID);

    public static final Supplier<MenuType<StoreMenu>> STORE_MENU =
            MENU_TYPES.register("store_menu", () -> IMenuTypeExtension.create(StoreMenu::new));

    public static final Supplier<MenuType<com.otherworldinn.world.inventory.CommissionBoardMenu>> COMMISSION_BOARD_MENU =
            MENU_TYPES.register(
                    "commission_board_menu",
                    () ->
                            IMenuTypeExtension.create(
                                    (windowId, inv, data) ->
                                            new com.otherworldinn.world.inventory.CommissionBoardMenu(windowId, inv)));

    public static final Supplier<MenuType<WanderingTraderRecycleMenu>> WANDERING_TRADER_RECYCLE =
            MENU_TYPES.register(
                    "wandering_trader_recycle",
                    () -> IMenuTypeExtension.create(WanderingTraderRecycleMenu::new));
}
