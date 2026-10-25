package com.otherworldinn.world.husbandry;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.util.WorldDayUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;

/**
 * 喂食入口。原版"用手持繁殖食物右键成年动物"的行为被 {@link HusbandryInteractionHandler}
 * 接管后走这里：消耗 1 个食物、播放喂食表现，但**不会让动物进入发情**。
 *
 * <p>好感度数值尚未实现，通过 {@link FeedListener} 留出接入点：默认实现来者不拒。
 * 将来好感度系统落地时，只需在模组初始化里
 * {@code HusbandryFeeding.setListener(...)} 注入一个实现（读附件里的好感度、做每日冷却、
 * 返回 false 表示"今天已经吃过了"），本类与监听器都不用改。
 */
public final class HusbandryFeeding {
    /** 一次喂食的上下文。{@code day} 是世界日，供实现方做"每天一次"之类的冷却。 */
    public record FeedContext(
            ServerLevel level, Animal animal, ServerPlayer player, ItemStack food, long day) {}

    /** 好感度系统的接入点：返回 false 表示这次喂食被拒绝（调用方不消耗物品）。 */
    @FunctionalInterface
    public interface FeedListener {
        boolean onFed(FeedContext context);
    }

    private static volatile FeedListener listener = context -> true;

    private HusbandryFeeding() {}

    /** 注入好感度实现；传 {@code null} 恢复为"来者不拒"的默认实现。 */
    public static void setListener(FeedListener newListener) {
        listener = newListener == null ? context -> true : newListener;
    }

    /**
     * 执行一次喂食。
     *
     * @return {@code true} 表示食物已被吃下（物品已消耗）；{@code false} 表示被监听器拒绝（未消耗）。
     */
    public static boolean feed(ServerLevel level, Animal animal, ServerPlayer player, ItemStack food) {
        long day = WorldDayUtils.currentDay(level);
        boolean accepted;
        try {
            accepted = listener.onFed(new FeedContext(level, animal, player, food, day));
        } catch (RuntimeException exception) {
            OtherworldInn.LOGGER.error("喂食回调异常，按已接受处理", exception);
            accepted = true;
        }
        if (!accepted) {
            return false;
        }
        food.consume(1, player);
        // 原版 Animal#setInLove 用的就是 18 号实体事件（7 颗爱心），这里只借表现、不改发情状态。
        level.broadcastEntityEvent(animal, (byte) 18);
        level.playSound(
                null,
                animal.blockPosition(),
                SoundEvents.GENERIC_EAT,
                SoundSource.NEUTRAL,
                0.6F,
                1.1F);
        return true;
    }
}
