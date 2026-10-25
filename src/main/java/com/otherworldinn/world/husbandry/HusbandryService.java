package com.otherworldinn.world.husbandry;

import com.otherworldinn.init.ModAttachments;
import com.otherworldinn.util.WorldDayUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * 好感度的读取与修改入口。交互层（{@link HusbandryInteractionHandler}）与将来的 HUD / 工具提示
 * 都走这里，避免到处直接摸 {@link HusbandryData}。
 */
public final class HusbandryService {
    /**
     * 喂一次食物的好感度。与 {@link #PET_AFFECTION} 一起构成"每天最多 25 点"，
     * 即 500 点满心需要约 20 个游戏日（全勤：喂一次 + 摸一次）。
     */
    public static final int FEED_AFFECTION = 20;

    /** 摸一次的好感度（= PatPat 的一次摸摸动作，每世界日一次）。 */
    public static final int PET_AFFECTION = 5;

    /** 第一次用命名牌给动物起名字的好感度（一次性）。 */
    public static final int NAMING_AFFECTION = 30;

    /** 自动喂食（饲养槽等非玩家来源）的好感度：玩家手动喂食的 1/5。 */
    public static final int AUTO_FEED_AFFECTION = FEED_AFFECTION / 5;

    /** 玩家打伤动物时扣的好感。 */
    public static final int HURT_PENALTY = 10;

    /** 连续这么多天没被照顾（喂/摸/命名）后才开始掉好感。 */
    public static final int CARE_GRACE_DAYS = 2;

    /** 超过宽限期后，每多一天掉的好感。 */
    public static final int DECAY_PER_DAY = 5;

    /** 新生幼崽继承的好感上限（2 心）。 */
    public static final int NEWBORN_MAX_AFFECTION = 200;

    /** 魔法剪刀要求的最低好感（2 心）。 */
    public static final int MAGIC_TRUST_AFFECTION = 200;

    /** 门槛折合成心数，用于计算"超出多少心 = 多抽几次"。 */
    public static final int MAGIC_TRUST_HEARTS =
            MAGIC_TRUST_AFFECTION / HusbandryData.AFFECTION_PER_HEART;

    /** 魔法剪刀每次使用掉的好感。 */
    public static final int MAGIC_HARVEST_COST = 5;

    private HusbandryService() {}

    public static HusbandryData data(Entity entity) {
        return entity.getData(ModAttachments.HUSBANDRY);
    }

    public static int getAffection(Entity entity) {
        return data(entity).getAffection();
    }

    public static int getHearts(Entity entity) {
        return data(entity).getHearts();
    }

    /**
     * 喂食：每世界日一次，成功则 +{@link #FEED_AFFECTION} 好感。
     *
     * @return {@code false} 表示今天已经喂过（调用方不消耗物品）
     */
    public static boolean tryFeed(Animal animal, ServerLevel level) {
        HusbandryData data = data(animal);
        long day = WorldDayUtils.currentDay(level);
        settleDecay(animal, level);
        if (!data.canFeedOn(day)) {
            return false;
        }
        data.markFed(day);
        data.markCare(day);
        data.addAffection(FEED_AFFECTION);
        HusbandrySync.broadcast(animal);
        return true;
    }

    /**
     * 抚摸（PatPat 的摸摸动作）：每世界日一次，成功则 +{@link #PET_AFFECTION} 好感。
     *
     * @return {@code false} 表示今天已经摸过
     */
    public static boolean tryPet(Animal animal, ServerLevel level) {
        HusbandryData data = data(animal);
        long day = WorldDayUtils.currentDay(level);
        settleDecay(animal, level);
        if (!data.canPetOn(day)) {
            return false;
        }
        data.markPet(day);
        data.markCare(day);
        data.addAffection(PET_AFFECTION);
        HusbandrySync.broadcast(animal);
        // 18 号实体事件 = 原版那 7 颗爱心，和喂食共用同一套反馈
        level.broadcastEntityEvent(animal, (byte) 18);
        return true;
    }

    /**
     * 首次命名奖励：一次性加 {@link #NAMING_AFFECTION} 好感。
     *
     * <p>幂等由"动物是否已有自定义名"保证——调用方只在 {@code !animal.hasCustomName()}
     * 且手持带名字的命名牌时调用；原版的命名流程不会被打断。
     */
    public static void grantNamingBonus(Animal animal, ServerLevel level) {
        HusbandryData data = data(animal);
        settleDecay(animal, level);
        data.markCare(WorldDayUtils.currentDay(level));
        data.addAffection(NAMING_AFFECTION);
        HusbandrySync.broadcast(animal);
        level.broadcastEntityEvent(animal, (byte) 18);
    }

    /** 今天是否已经被喂过（手动或自动，共用同一份记录）。饲养槽据此判断要不要去吃。 */
    public static boolean hasEatenToday(Animal animal, ServerLevel level) {
        return !data(animal).canFeedOn(WorldDayUtils.currentDay(level));
    }

    /**
     * 自动喂食：饲养槽等非玩家来源吃掉一份食物时调用。
     *
     * <p>与手动喂食<b>共享单日上限</b>：今天被任一种方式喂过，另一种就不再生效。
     * 只加好感并同步数值，<b>不触发心形 HUD</b>（那是玩家右键才有的反馈）。
     *
     * @return {@code false} 表示今天已经喂过（手动或自动）
     */
    public static boolean tryAutoFeed(Animal animal, ServerLevel level) {
        HusbandryData data = data(animal);
        long day = WorldDayUtils.currentDay(level);
        settleDecay(animal, level);
        if (!data.canFeedOn(day)) {
            return false;
        }
        data.markFed(day);
        data.markCare(day);
        data.addAffection(AUTO_FEED_AFFECTION);
        HusbandrySync.broadcast(animal);
        return true;
    }

    /**
     * 纯计算：截至 {@code day}，这只动物因为"多日没人管"本次应额外掉多少好感。
     *
     * <p>抽成纯函数是为了能脱离游戏环境直接验证（{@code build/_probe}）。
     * 宽限 {@link #CARE_GRACE_DAYS} 天，之后每多一天掉 {@link #DECAY_PER_DAY}；
     * 已结算过的量记录在 {@code appliedDecay} 里，这里只返回增量。
     */
    public static int pendingDecay(long day, long lastCareDay, int appliedDecay) {
        if (lastCareDay == HusbandryData.NEVER) {
            return 0;
        }
        long idleDays = day - lastCareDay;
        if (idleDays <= CARE_GRACE_DAYS) {
            return 0;
        }
        long target = (idleDays - CARE_GRACE_DAYS) * DECAY_PER_DAY;
        long delta = target - appliedDecay;
        if (delta <= 0) {
            return 0;
        }
        return (int) Math.min(delta, HusbandryData.MAX_AFFECTION);
    }

    /**
     * 惰性结算衰减：在互动或玩家开始追踪动物时调用，避免每天遍历全部实体。
     *
     * <p>新动物（还没有照顾记录）把当天记为起点，不追溯扣分。
     */
    public static void settleDecay(Animal animal, ServerLevel level) {
        HusbandryData data = data(animal);
        long day = WorldDayUtils.currentDay(level);
        if (data.getLastCareDay() == HusbandryData.NEVER) {
            data.setLastCareDay(day);
            return;
        }
        int decay = pendingDecay(day, data.getLastCareDay(), data.getAppliedDecay());
        if (decay <= 0) {
            return;
        }
        data.setAppliedDecay(data.getAppliedDecay() + decay);
        data.addAffection(-decay);
        HusbandrySync.broadcast(animal);
    }

    /** 玩家打伤动物：先结算衰减，再扣 {@link #HURT_PENALTY}，并冒生气粒子。 */
    public static void penalizeHurt(Animal animal, ServerLevel level) {
        settleDecay(animal, level);
        data(animal).addAffection(-HURT_PENALTY);
        HusbandrySync.broadcast(animal);
        level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.ANGRY_VILLAGER,
                animal.getX(),
                animal.getY() + 1.2D,
                animal.getZ(),
                3,
                0.2D,
                0.2D,
                0.2D,
                0.0D);
    }

    /**
     * 魔法剪刀采集：抽该动物的击杀掉落表，每只动物每世界日一次，代价 {@link #MAGIC_HARVEST_COST} 好感。
     *
     * <p>抢夺附魔每级多抽一次（{@code rolls = 1 + looting}）。有主人的宠物、好感不足、
     * 今天采过都直接拒绝并给出提示。
     *
     * @return {@code true} 表示采集成功
     */
    public static boolean tryHarvest(
            Animal animal, ServerLevel level, ServerPlayer player, ItemStack shears, int enchantLevel) {
        if (animal instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null) {
            sendHarvestFailure(level, animal, player, "message.otherworldinn.harvest.cannot_use");
            return false;
        }
        HusbandryData data = data(animal);
        long day = WorldDayUtils.currentDay(level);
        settleDecay(animal, level);
        if (data.getAffection() < MAGIC_TRUST_AFFECTION) {
            sendHarvestFailure(level, animal, player, "message.otherworldinn.harvest.need_trust");
            return false;
        }
        if (!data.canMagicHarvestOn(day)) {
            sendHarvestFailure(level, animal, player, "message.otherworldinn.harvest.cooldown");
            return false;
        }
        // 好感越高抽得越多：每超出门槛整整一颗心，就多抽一次（与抢夺的"多抽一次"同理）。
        int heartsAboveTrust = data.getHearts() - MAGIC_TRUST_HEARTS;
        int heartBonus = Math.max(0, heartsAboveTrust);

        // 附魔等级越高，代价越低（I:5 / II:4 / III:3），产量也越高（每次多抽一次）
        int cost = Math.max(1, MAGIC_HARVEST_COST - (enchantLevel - 1));

        data.markMagicHarvest(day);
        data.addAffection(-cost);

        int rolls = Math.max(1, enchantLevel) + heartBonus;
        for (ItemStack drop : rollEntityLoot(level, animal, rolls)) {
            if (!drop.isEmpty()) {
                Block.popResource(level, animal.blockPosition(), drop);
            }
        }

        // 挥手：这个分支不取消原版交互，而牛/鸡这类动物原版返回 PASS 不会挥手，
        // 服务端 swing 会广播 ClientboundAnimatePacket，客户端就有正常的挥臂动画。
        player.swing(InteractionHand.MAIN_HAND);

        if (shears.isDamageableItem()) {
            shears.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        }
        HusbandrySync.broadcast(animal);
        level.playSound(
                null,
                animal.blockPosition(),
                SoundEvents.SHEEP_SHEAR,
                SoundSource.NEUTRAL,
                0.8F,
                1.0F);
        level.sendParticles(
                ParticleTypes.ENCHANT,
                animal.getX(),
                animal.getY() + 1.0D,
                animal.getZ(),
                20,
                0.4D,
                0.4D,
                0.4D,
                0.5D);
        return true;
    }

    /**
     * 奇迹药水：让成年动物进入发情，并放行这一次繁殖（平时繁殖全被拦）。
     *
     * @return {@code true} 表示喝下了（药水已消耗）
     */
    public static boolean tryMiraclePotion(
            Animal animal, ServerLevel level, ServerPlayer player, ItemStack potion) {
        if (animal.isBaby()) {
            sendActionBar(player, "message.otherworldinn.miracle_potion.too_young");
            return false;
        }
        if (animal.isInLove()) {
            // 已在发情中：静默拒绝，不消耗药水也不提示
            return false;
        }
        // 不再限制"每天一次"：改由原版繁殖冷却接管（getAge() != 0 表示还在冷却里）
        if (animal.getAge() != 0) {
            // 原版繁殖冷却中：静默拒绝，不消耗药水
            return false;
        }
        // 完全接入原版：发情时长就是原版 setInLove 的 600 tick，不额外标记、不额外延长
        animal.setInLove(player);
        potion.consume(1, player);
        // 爱心（原版 entity event 18）
        level.broadcastEntityEvent(animal, (byte) 18);
        // 挥手：服务端 swing 会广播动画包，附近玩家也能看到
        player.swing(InteractionHand.MAIN_HAND);
        level.playSound(
                null,
                animal.blockPosition(),
                SoundEvents.BREWING_STAND_BREW,
                SoundSource.NEUTRAL,
                0.8F,
                1.2F);
        // 紫红色魔法粒子，围绕动物身体散开
        level.sendParticles(
                ParticleTypes.WITCH,
                animal.getX(),
                animal.getY() + animal.getBbHeight() * 0.6D,
                animal.getZ(),
                12,
                animal.getBbWidth() * 0.5D,
                animal.getBbHeight() * 0.4D,
                animal.getBbWidth() * 0.5D,
                0.02D);
        HusbandrySync.broadcast(animal);
        return true;
    }

    /**
     * 新生幼崽继承双亲好感平均值（上限 {@link #NEWBORN_MAX_AFFECTION} = 2 心）。
     *
     * <p>只由"药水祝福放行"的那一次繁殖调用；幼崽同时被记为"今天照顾过"，
     * 避免它一出生就进入衰减计时。
     */
    public static void applyNewbornAffection(
            Animal baby, Animal parentA, Animal parentB, ServerLevel level) {
        int sum = data(parentA).getAffection();
        int count = 1;
        if (parentB != null) {
            sum += data(parentB).getAffection();
            count = 2;
        }
        int initial = Math.min(NEWBORN_MAX_AFFECTION, sum / count);
        HusbandryData babyData = data(baby);
        babyData.setAffection(initial);
        babyData.markCare(WorldDayUtils.currentDay(level));
    }

    /**
     * 采集失败时的反馈：文字提示 + 挽手 + 村民生气粒子，但**不播放音效**。
     *
     * <p>挥手走服务端 {@code swing}（会广播 {@code ClientboundAnimatePacket}），
     * 附近玩家都能看到；粒子生成在动物头顶附近。
     */
    private static void sendHarvestFailure(
            ServerLevel level, Animal animal, ServerPlayer player, String key) {
        sendActionBar(player, key);
        player.swing(InteractionHand.MAIN_HAND);
        level.sendParticles(
                ParticleTypes.ANGRY_VILLAGER,
                animal.getX(),
                animal.getY() + animal.getBbHeight() * 0.85D,
                animal.getZ(),
                3,
                0.25D,
                0.25D,
                0.25D,
                0.0D);
    }

    private static void sendActionBar(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.GRAY), true);
    }

    /** 抽该实体的击杀掉落表；不传 KILLER_ENTITY，避免和我们的抢夺加成叠加。 */
    private static List<ItemStack> rollEntityLoot(ServerLevel level, Animal animal, int rolls) {
        ResourceKey<LootTable> key = animal.getType().getDefaultLootTable();
        LootTable table = level.getServer().reloadableRegistries().getLootTable(key);
        LootParams params =
                new LootParams.Builder(level)
                        .withParameter(LootContextParams.THIS_ENTITY, animal)
                        .withParameter(LootContextParams.ORIGIN, animal.position())
                        .withParameter(
                                LootContextParams.DAMAGE_SOURCE, animal.damageSources().generic())
                        .create(LootContextParamSets.ENTITY);
        List<ItemStack> drops = new ArrayList<>();
        for (int i = 0; i < rolls; i++) {
            drops.addAll(table.getRandomItems(params));
        }
        return drops;
    }

    /** 把默认实现挂到 {@link HusbandryFeeding} 上；好感度规则要改时只需要换掉这里。 */
    public static void bootstrap() {
        HusbandryFeeding.setListener(context -> tryFeed(context.animal(), context.level()));
    }
}
