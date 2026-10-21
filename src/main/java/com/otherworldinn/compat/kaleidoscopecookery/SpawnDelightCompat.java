package com.otherworldinn.compat.kaleidoscopecookery;

import com.otherworldinn.OtherworldInn;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * 把 Spawn 模组"农夫乐事（Farmer's Delight）专属内容"改由森罗物语：厨房承接的兼容层：
 * 没装 FD 时把 13 种菜品加回创造栏，食用后套用森罗的 {@code warmth} / {@code vigor}
 * （对应 FD 的 Comfort 6000t / Nourishment 3600t），并补上效果 tooltip。
 * 配方与标签由数据包补齐（{@code data/otherworldinn/recipe/spawn_kc/} 等）。
 *
 * <p>装了 Farmer's Delight 时本类全部不生效，交回 Spawn 自己的 FD 兼容逻辑，避免效果叠加。
 */
public final class SpawnDelightCompat {

    private static final String COOKERY_MOD_ID = "kaleidoscope_cookery";
    private static final String FARMERS_DELIGHT_MOD_ID = "farmersdelight";
    private static final String SPAWN_MOD_ID = "spawn";

    /** 需要接管"食用后效果"的 Spawn 菜品；清单见 {@code data/otherworldinn/tags/item/spawn_delight_dishes.json}。 */
    private static final TagKey<Item> SPAWN_DELIGHT_DISHES =
            TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(
                            OtherworldInn.MODID, "spawn_delight_dishes"));

    /** Spawn 的主创造栏（{@code spawn:spawn}）——没装 FD 时它会把下列菜品从栏内隐藏。 */
    private static final ResourceKey<CreativeModeTab> SPAWN_TAB =
            ResourceKey.create(
                    Registries.CREATIVE_MODE_TAB,
                    ResourceLocation.fromNamespaceAndPath(SPAWN_MOD_ID, "spawn"));

    /** 森罗厨房效果：温暖（靠近热源缓慢回血）对应 FD 的 Comfort；活力（奔跑不消耗饥饿）对应 Nourishment。 */
    private static final ResourceLocation WARMTH_EFFECT =
            ResourceLocation.fromNamespaceAndPath(COOKERY_MOD_ID, "warmth");
    private static final ResourceLocation VIGOR_EFFECT =
            ResourceLocation.fromNamespaceAndPath(COOKERY_MOD_ID, "vigor");

    /** 时长与 Spawn 给 FD 的 Comfort / Nourishment 数值对齐：6000 / 3600 tick。 */
    private static final int WARMTH_DURATION_TICKS = 6000;
    private static final int VIGOR_DURATION_TICKS = 3600;

    /** 没装 FD 时被 Spawn 从创造栏隐藏的菜品（顺序与 Spawn 字节码里的数组一致）。 */
    private static final List<ResourceLocation> HIDDEN_WITHOUT_FD_DISHES =
            List.of(
                    spawnId("tuna_roll"),
                    spawnId("tuna_slice"),
                    spawnId("cooked_tuna_slice"),
                    spawnId("steamed_clams"),
                    spawnId("herring_slice"),
                    spawnId("cooked_herring_slice"),
                    spawnId("herring_roll"),
                    spawnId("canned_herring"),
                    spawnId("crab_boil_block"),
                    spawnId("crab_boil"),
                    spawnId("bluefish_slice"),
                    spawnId("cooked_bluefish_slice"),
                    spawnId("bluefish_roll"));

    private SpawnDelightCompat() {}

    private static ResourceLocation spawnId(String path) {
        return ResourceLocation.fromNamespaceAndPath(SPAWN_MOD_ID, path);
    }

    /** 只在"装了森罗厨房且没装农夫乐事"时接管；装了 FD 就让 Spawn 自己的兼容逻辑生效。 */
    private static boolean shouldTakeOver() {
        return ModList.get().isLoaded(COOKERY_MOD_ID)
                && !ModList.get().isLoaded(FARMERS_DELIGHT_MOD_ID);
    }

    private static boolean isSpawnDelightDish(ItemStack stack) {
        return !stack.isEmpty() && stack.is(SPAWN_DELIGHT_DISHES);
    }

    /** 取森罗厨房的效果；未安装/被移除时返回 null（通过比对注册名，避开默认值注册表的兜底项）。 */
    private static MobEffect cookeryEffect(ResourceLocation effectId) {
        MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(effectId);
        if (effect == null || !effectId.equals(BuiltInRegistries.MOB_EFFECT.getKey(effect))) {
            return null;
        }
        return effect;
    }

    private static void applyCookeryEffect(
            LivingEntity entity, ResourceLocation effectId, int durationTicks) {
        MobEffect effect = cookeryEffect(effectId);
        if (effect == null) {
            return;
        }
        entity.addEffect(
                new MobEffectInstance(
                        BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect),
                        durationTicks,
                        0,
                        false,
                        true));
    }

    /** 追加一行"效果（时长）"，与 FD 的食用效果提示同构，复用原版 {@code potion.withDuration} 文案。 */
    private static void appendEffectTooltip(
            ItemTooltipEvent event, ResourceLocation effectId, int durationTicks) {
        MobEffect effect = cookeryEffect(effectId);
        if (effect == null) {
            return;
        }
        MobEffectInstance preview =
                new MobEffectInstance(
                        BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect),
                        durationTicks,
                        0,
                        false,
                        false);
        event.getToolTip()
                .add(
                        Component.translatable(
                                        "potion.withDuration",
                                        effect.getDisplayName(),
                                        MobEffectUtil.formatDuration(preview, 1.0F, 20.0F))
                                .withStyle(ChatFormatting.BLUE));
    }

    /** 按注册名取物品；不存在（未装 Spawn 等）时返回空栈。 */
    private static ItemStack spawnItem(ResourceLocation itemId) {
        Item item = BuiltInRegistries.ITEM.get(itemId);
        if (item == null || !itemId.equals(BuiltInRegistries.ITEM.getKey(item))) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item);
        return stack.isEmpty() ? ItemStack.EMPTY : stack;
    }

    /** 游戏事件总线：食用后的效果与 tooltip。 */
    @EventBusSubscriber(modid = OtherworldInn.MODID, bus = EventBusSubscriber.Bus.GAME)
    public static final class GameEvents {

        @SubscribeEvent
        public static void onUseItemFinish(LivingEntityUseItemEvent.Finish event) {
            if (!shouldTakeOver() || event.getEntity().level().isClientSide()) {
                return;
            }
            if (!isSpawnDelightDish(event.getItem())) {
                return;
            }
            applyCookeryEffect(event.getEntity(), WARMTH_EFFECT, WARMTH_DURATION_TICKS);
            applyCookeryEffect(event.getEntity(), VIGOR_EFFECT, VIGOR_DURATION_TICKS);
        }

        @SubscribeEvent
        public static void onItemTooltip(ItemTooltipEvent event) {
            if (!shouldTakeOver() || !isSpawnDelightDish(event.getItemStack())) {
                return;
            }
            appendEffectTooltip(event, WARMTH_EFFECT, WARMTH_DURATION_TICKS);
            appendEffectTooltip(event, VIGOR_EFFECT, VIGOR_DURATION_TICKS);
        }
    }

    /** 模组事件总线：把 Spawn 因缺少 FD 而隐藏的菜品加回它的创造栏。 */
    @EventBusSubscriber(modid = OtherworldInn.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static final class ModBusEvents {

        @SubscribeEvent
        public static void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
            if (!shouldTakeOver() || !SPAWN_TAB.equals(event.getTabKey())) {
                return;
            }
            for (ResourceLocation itemId : HIDDEN_WITHOUT_FD_DISHES) {
                ItemStack stack = spawnItem(itemId);
                if (!stack.isEmpty()) {
                    event.accept(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                }
            }
        }
    }
}
