package com.otherworldinn.init;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.base.GuestEntity;
import com.otherworldinn.entity.store.BlacksmithEntity;
import com.otherworldinn.entity.store.BuilderEntity;
import com.otherworldinn.entity.store.ButcherEntity;
import com.otherworldinn.entity.store.FarmerEntity;
import com.otherworldinn.entity.store.FishermanEntity;
import com.otherworldinn.entity.store.GrocerEntity;
import com.otherworldinn.entity.store.MagicianEntity;
import com.otherworldinn.entity.store.WanderingTraderEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

/** 模组事件总线事件处理器 */
@EventBusSubscriber(modid = OtherworldInn.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {

    @SubscribeEvent
    public static void onAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.ORDINARY_GUEST.get(), GuestEntity.createAttributes().build());
        event.put(ModEntities.RICH_GUEST.get(), GuestEntity.createAttributes().build());
        event.put(ModEntities.HEAVY_PACK_GUEST.get(), GuestEntity.createAttributes().build());
        event.put(ModEntities.ULTRA_RICH_GUEST.get(), GuestEntity.createAttributes().build());
        event.put(ModEntities.ORDINARY_VIP_GUEST.get(), GuestEntity.createAttributes().build());
        event.put(ModEntities.ADVANCED_VIP_GUEST.get(), GuestEntity.createAttributes().build());
        event.put(ModEntities.SPONSOR_GUEST.get(), GuestEntity.createAttributes().build());
        event.put(ModEntities.STORY_GUEST.get(), GuestEntity.createAttributes().build());
        event.put(ModEntities.BLACKSMITH.get(), BlacksmithEntity.createAttributes().build());
        event.put(ModEntities.MAGICIAN.get(), MagicianEntity.createAttributes().build());
        event.put(ModEntities.FARMER.get(), FarmerEntity.createAttributes().build());
        event.put(ModEntities.GROCER.get(), GrocerEntity.createAttributes().build());
        event.put(ModEntities.BUTCHER.get(), ButcherEntity.createAttributes().build());
        event.put(ModEntities.BUILDER.get(), BuilderEntity.createAttributes().build());
        event.put(ModEntities.FISHERMAN.get(), FishermanEntity.createAttributes().build());
        event.put(ModEntities.WANDERING_TRADER.get(), WanderingTraderEntity.createAttributes().build());
    }
}
