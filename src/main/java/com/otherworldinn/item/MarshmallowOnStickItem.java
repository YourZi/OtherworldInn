package com.otherworldinn.item;

import com.otherworldinn.init.ModDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/** 一串棉花糖：烤熟度（0-100）存于数据组件，复用耐久条显示，颜色随进度白→黄→红→黑渐变。 */
public class MarshmallowOnStickItem extends Item {

    public static final int MAX_ROAST = 100;
    private static final int STAGE_SIZE = 20;
    private static final int STAGE_COUNT = 5;

    /** 基础营养：饥饿 2、饱和 0.8。 */
    public static final FoodProperties BASE_FOOD =
            new FoodProperties.Builder().nutrition(2).saturationModifier(0.2F).build();

    /** 五阶段营养：前三段上升、后两段下降（注释为营养/饱和）。 */
    private static final FoodProperties[] STAGE_FOODS = {
        stageFood(3, 0.2F),  // 温热 3/1.2
        stageFood(4, 0.2F),  // 焦香 4/1.6
        stageFood(5, 0.25F), // 烤透 5/2.5
        stageFood(3, 0.1F),  // 烤焦 3/0.6
        stageFood(1, 0.05F), // 碳化 1/0.1
    };

    /** 耐久条颜色锚点：白→黄→红→黑。 */
    private static final int[] BAR_COLOR_STOPS = {0xFFFFFF, 0xFFFF00, 0xFF0000, 0x000000};

    public MarshmallowOnStickItem(Properties properties) {
        super(properties);
    }

    public static int getRoast(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.MARSHMALLOW_ROAST.get(), 0);
    }

    public static void setRoast(ItemStack stack, int roast) {
        int clamped = Mth.clamp(roast, 0, MAX_ROAST);
        int oldStage = stageIndex(getRoast(stack));
        stack.set(ModDataComponents.MARSHMALLOW_ROAST.get(), clamped);
        int newStage = stageIndex(clamped);
        if (newStage != oldStage) {
            stack.set(DataComponents.FOOD, newStage < 0 ? BASE_FOOD : STAGE_FOODS[newStage]);
        }
    }

    /** -1 = 未烤，0-4 = 五阶段（每 20 点一段，100 归入末段）。 */
    private static int stageIndex(int roast) {
        if (roast <= 0) {
            return -1;
        }
        return Math.min(roast / STAGE_SIZE, STAGE_COUNT - 1);
    }

    /** 手持 3D 模型阶段（0-4）：未烤时与第一阶段共用 mellow1。 */
    public static int getHandStage(ItemStack stack) {
        return Math.max(0, stageIndex(getRoast(stack)));
    }

    private static FoodProperties stageFood(int nutrition, float saturationModifier) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturationModifier).build();
    }

    @Override
    public Component getName(ItemStack stack) {
        int stage = stageIndex(getRoast(stack));
        if (stage < 0) {
            return super.getName(stack);
        }
        return Component.translatable(this.getDescriptionId() + ".stage." + stage);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getRoast(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getRoast(stack) / MAX_ROAST);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float progress = Mth.clamp(getRoast(stack) / (float) MAX_ROAST, 0.0F, 1.0F);
        float position = progress * (BAR_COLOR_STOPS.length - 1);
        int index = Math.min((int) position, BAR_COLOR_STOPS.length - 2);
        float fraction = position - index;
        return lerpColor(BAR_COLOR_STOPS[index], BAR_COLOR_STOPS[index + 1], fraction);
    }

    private static int lerpColor(int from, int to, float fraction) {
        int r = Math.round(Mth.lerp(fraction, (from >> 16) & 0xFF, (to >> 16) & 0xFF));
        int g = Math.round(Mth.lerp(fraction, (from >> 8) & 0xFF, (to >> 8) & 0xFF));
        int b = Math.round(Mth.lerp(fraction, from & 0xFF, to & 0xFF));
        return r << 16 | g << 8 | b;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        return result.isEmpty() ? new ItemStack(Items.STICK) : result;
    }

    /** 烤熟度变化只改组件，不重播装备动画；换槽位或换物品（碳化成火把）时仍播放。 */
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }
}
