package com.otherworldinn.world.husbandry;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.init.ModGameRules;
import com.otherworldinn.init.ModEnchantments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import javax.annotation.Nullable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * 动物交互接管：把原版"用繁殖食物右键成年动物 = 进入发情"改造成"喂食"，
 * 并在幼崽生成层兜底拦截任何来源的繁殖。
 *
 * <p>不受影响：幼崽喂食（加速成长）、未驯服的猫/马等（驯服优先）、村民繁殖。
 * 开关为 {@code /gamerule allowVanillaBreeding true}（默认关闭，开启后完全恢复原版行为）。
 */
@EventBusSubscriber(modid = OtherworldInn.MODID)
public final class HusbandryInteractionHandler {
    private HusbandryInteractionHandler() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Animal animal)) {
            return;
        }

        // ── 命名：首次用命名牌给动物起名字，一次性 +NAMING_AFFECTION ──
        // 幂等靠"动物原本没有自定义名"；不取消交互，原版命名流程照常执行。
        ItemStack heldStack = event.getItemStack();
        if (heldStack.is(Items.NAME_TAG)
                && heldStack.has(DataComponents.CUSTOM_NAME)
                && !animal.hasCustomName()) {
            if (event.getLevel() instanceof ServerLevel namingLevel) {
                HusbandryService.grantNamingBonus(animal, namingLevel);
            }
            return;
        }

        // ── 收获附魔：手持带"收获"的剪刀右键，采集一份该动物的击杀掉落，每只每天一次 ──
        // 不取消交互：羊的普通剪毛等原版行为照常发生。
        if (event.getLevel() instanceof ServerLevel harvestLevel) {
            int harvestEnchant = ModEnchantments.harvestLevel(harvestLevel, heldStack);
            if (harvestEnchant > 0) {
                if (animal.isBaby()) {
                    return;
                }
                // 原版优先：羊、蘑菇牛这类"这次本来就能剪"的动物交给原版处理。
                // 否则一次右键会既剪毛又采集——玩家只想剪羊毛时会被扣好感、占用当天次数。
                if (animal instanceof Shearable shearable && shearable.readyForShearing()) {
                    return;
                }
                if (event.getEntity() instanceof ServerPlayer harvester) {
                    HusbandryService.tryHarvest(
                            animal, harvestLevel, harvester, heldStack, harvestEnchant);
                }
                return;
            }
        }

        // 抚摸只由 PatPat 的摸摸动作触发（见 MixinPatPatServerPacketManager），
        // 这里不再用"潜行+右键"的手势兜底，避免同一个动作有两条来源。

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }
        if (animal.isBaby()) {
            return;
        }
        if (!animal.isFood(stack)) {
            return;
        }
        if (animal instanceof OwnableEntity ownable && ownable.getOwnerUUID() == null) {
            return;
        }

        Level level = event.getLevel();
        boolean serverSide = level instanceof ServerLevel;
        if (serverSide
                && ((ServerLevel) level)
                        .getGameRules()
                        .getBoolean(ModGameRules.RULE_ALLOW_VANILLA_BREEDING)) {
            return;
        }

        // 客户端也取消：不预测发情，爱心表现统一由服务端广播。
        // 取消结果用 SUCCESS：shouldSwing() 为 true —— 右键动物一律有挥手反馈（设计如此）。
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        if (!serverSide || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!HusbandryFeeding.feed((ServerLevel) level, animal, player, stack)) {
            player.displayClientMessage(
                    Component.translatable("message.otherworldinn.feed_refused")
                            .withStyle(ChatFormatting.GRAY),
                    true);
        }
    }

    /** 玩家打伤动物扣好感（怪物/环境伤害不扣，避免玩家无法控制的损失）。 */
    @SubscribeEvent
    public static void onAnimalHurt(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof Animal animal)) {
            return;
        }
        if (!(animal.level() instanceof ServerLevel level)) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof Player)) {
            return;
        }
        HusbandryService.penalizeHurt(animal, level);
    }

    @Nullable
    private static Animal parentBAnimal(BabyEntitySpawnEvent event) {
        return event.getParentB() instanceof Animal second ? second : null;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBabySpawn(BabyEntitySpawnEvent event) {
        if (!(event.getParentA() instanceof Animal)) {
            return;
        }
        Level level = event.getParentA().level();
        if (level instanceof ServerLevel serverLevel
                && serverLevel
                        .getGameRules()
                        .getBoolean(ModGameRules.RULE_ALLOW_VANILLA_BREEDING)) {
            return;
        }
        // 发情只可能来自奇迹药水（玩家喂食与自动喂食都被拦下，不会 setInLove），
        // 所以"双方都在发情"本身就等于"药水放行"，不需要额外的标记或按天过期机制。
        Animal parentA = (Animal) event.getParentA();
        Animal parentB = parentBAnimal(event);
        boolean potionInduced =
                parentA.isInLove() || (parentB != null && parentB.isInLove());
        if (potionInduced) {
            // 幼崽继承双亲好感平均值（上限 2 心）
            if (event.getChild() instanceof Animal baby && level instanceof ServerLevel serverLevel) {
                HusbandryService.applyNewbornAffection(baby, parentA, parentB, serverLevel);
            }
            return;
        }
        event.setCanceled(true);
    }
}
