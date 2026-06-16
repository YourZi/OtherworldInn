package com.otherworldinn.init;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.guest.AdvancedVipGuestEntity;
import com.otherworldinn.entity.guest.HeavyPackGuestEntity;
import com.otherworldinn.entity.guest.OrdinaryGuestEntity;
import com.otherworldinn.entity.guest.OrdinaryVipGuestEntity;
import com.otherworldinn.entity.guest.RichGuestEntity;
import com.otherworldinn.entity.guest.SponsorGuestEntity;
import com.otherworldinn.entity.guest.UltraRichGuestEntity;
import com.otherworldinn.entity.store.BlacksmithEntity;
import com.otherworldinn.entity.store.BuilderEntity;
import com.otherworldinn.entity.store.ButcherEntity;
import com.otherworldinn.entity.store.FarmerEntity;
import com.otherworldinn.entity.store.FishermanEntity;
import com.otherworldinn.entity.store.GrocerEntity;
import com.otherworldinn.entity.store.MagicianEntity;
import com.otherworldinn.world.entity.projectile.CoinProjectileEntity;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

/** 实体注册中心 */
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, OtherworldInn.MODID);

    public static final Supplier<EntityType<OrdinaryGuestEntity>> ORDINARY_GUEST =
            ENTITY_TYPES.register(
                    "ordinary_guest",
                    () ->
                            EntityType.Builder.of(OrdinaryGuestEntity::new, MobCategory.CREATURE)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(80)
                                    .updateInterval(2)
                                    .setShouldReceiveVelocityUpdates(true)
                                    .build("ordinary_guest"));

    public static final Supplier<EntityType<RichGuestEntity>> RICH_GUEST =
            ENTITY_TYPES.register(
                    "rich_guest",
                    () ->
                            EntityType.Builder.of(RichGuestEntity::new, MobCategory.CREATURE)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(80)
                                    .updateInterval(2)
                                    .setShouldReceiveVelocityUpdates(true)
                                    .build("rich_guest"));

    public static final Supplier<EntityType<HeavyPackGuestEntity>> HEAVY_PACK_GUEST =
            ENTITY_TYPES.register(
                    "heavy_pack_guest",
                    () ->
                            EntityType.Builder.of(HeavyPackGuestEntity::new, MobCategory.CREATURE)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(80)
                                    .updateInterval(2)
                                    .setShouldReceiveVelocityUpdates(true)
                                    .build("heavy_pack_guest"));

    public static final Supplier<EntityType<UltraRichGuestEntity>> ULTRA_RICH_GUEST =
            ENTITY_TYPES.register(
                    "ultra_rich_guest",
                    () ->
                            EntityType.Builder.of(UltraRichGuestEntity::new, MobCategory.CREATURE)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(80)
                                    .updateInterval(2)
                                    .setShouldReceiveVelocityUpdates(true)
                                    .build("ultra_rich_guest"));

    public static final Supplier<EntityType<OrdinaryVipGuestEntity>> ORDINARY_VIP_GUEST =
            ENTITY_TYPES.register(
                    "ordinary_vip_guest",
                    () ->
                            EntityType.Builder.of(OrdinaryVipGuestEntity::new, MobCategory.CREATURE)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(80)
                                    .updateInterval(2)
                                    .setShouldReceiveVelocityUpdates(true)
                                    .build("ordinary_vip_guest"));

    public static final Supplier<EntityType<AdvancedVipGuestEntity>> ADVANCED_VIP_GUEST =
            ENTITY_TYPES.register(
                    "advanced_vip_guest",
                    () ->
                            EntityType.Builder.of(AdvancedVipGuestEntity::new, MobCategory.CREATURE)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(80)
                                    .updateInterval(2)
                                    .setShouldReceiveVelocityUpdates(true)
                                    .build("advanced_vip_guest"));

    public static final Supplier<EntityType<SponsorGuestEntity>> SPONSOR_GUEST =
            ENTITY_TYPES.register(
                    "sponsor_guest",
                    () ->
                            EntityType.Builder.of(SponsorGuestEntity::new, MobCategory.CREATURE)
                                    .sized(0.6F, 1.8F)
                                    .clientTrackingRange(80)
                                    .updateInterval(2)
                                    .setShouldReceiveVelocityUpdates(true)
                                    .build("sponsor_guest"));

    public static final Supplier<EntityType<BlacksmithEntity>> BLACKSMITH =
            ENTITY_TYPES.register(
                    "blacksmith",
                    () ->
                            EntityType.Builder.of(
                                            BlacksmithEntity::new,
                                            MobCategory.MISC) // 使用 MISC 分类，因为不是生物
                                    .sized(0.6F, 1.95F) // 村民大小
                                    .clientTrackingRange(80)
                                    .updateInterval(2)
                                    .setShouldReceiveVelocityUpdates(true)
                                    .build("blacksmith"));

    public static final Supplier<EntityType<MagicianEntity>> MAGICIAN =
            ENTITY_TYPES.register(
                    "magician",
                    () ->
                            EntityType.Builder.of(
                                            MagicianEntity::new,
                                            MobCategory.MISC)
                                    .sized(0.6F, 1.95F)
                                    .clientTrackingRange(80)
                                    .updateInterval(2)
                                    .setShouldReceiveVelocityUpdates(true)
                                    .build("magician"));

    public static final Supplier<EntityType<FarmerEntity>> FARMER =
            ENTITY_TYPES.register(
                    "farmer",
                    () ->
                            EntityType.Builder.of(
                                            FarmerEntity::new,
                                            MobCategory.MISC)
                                    .sized(0.6F, 1.95F)
                                    .clientTrackingRange(80)
                                    .updateInterval(2)
                                    .setShouldReceiveVelocityUpdates(true)
                                    .build("farmer"));

    public static final Supplier<EntityType<GrocerEntity>> GROCER =
            ENTITY_TYPES.register(
                    "grocer",
                    () ->
                            EntityType.Builder.of(
                                            GrocerEntity::new,
                                            MobCategory.MISC)
                                    .sized(0.6F, 1.95F)
                                    .clientTrackingRange(80)
                                    .updateInterval(2)
                                    .setShouldReceiveVelocityUpdates(true)
                                    .build("grocer"));

    public static final Supplier<EntityType<ButcherEntity>> BUTCHER =
            ENTITY_TYPES.register(
                    "butcher",
                    () ->
                            EntityType.Builder.of(
                                            ButcherEntity::new,
                                            MobCategory.MISC)
                                    .sized(0.6F, 1.95F)
                                    .clientTrackingRange(80)
                                    .updateInterval(2)
                                    .setShouldReceiveVelocityUpdates(true)
                                    .build("butcher"));

    public static final Supplier<EntityType<BuilderEntity>> BUILDER =
            ENTITY_TYPES.register(
                    "builder",
                    () ->
                            EntityType.Builder.of(
                                            BuilderEntity::new,
                                            MobCategory.MISC)
                                    .sized(0.6F, 1.95F)
                                    .clientTrackingRange(80)
                                    .updateInterval(2)
                                    .setShouldReceiveVelocityUpdates(true)
                                    .build("builder"));

    public static final Supplier<EntityType<FishermanEntity>> FISHERMAN =
            ENTITY_TYPES.register(
                    "fisherman",
                    () ->
                            EntityType.Builder.of(
                                            FishermanEntity::new,
                                            MobCategory.MISC)
                                    .sized(0.6F, 1.95F)
                                    .clientTrackingRange(80)
                                    .updateInterval(2)
                                    .setShouldReceiveVelocityUpdates(true)
                                    .build("fisherman"));

    public static final Supplier<EntityType<CoinProjectileEntity>> COIN_PROJECTILE =
            ENTITY_TYPES.register(
                    "coin_projectile",
                    () ->
                            EntityType.Builder.<CoinProjectileEntity>of(
                                            CoinProjectileEntity::new, MobCategory.MISC)
                                    .sized(0.25F, 0.25F)
                                    .clientTrackingRange(32)
                                    .updateInterval(2)
                                    .setShouldReceiveVelocityUpdates(true)
                                    .build("coin_projectile"));
}
