package com.otherworldinn.init;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.foundation.BlockDataGenInfo;
import com.otherworldinn.foundation.ItemDataGenInfo;
import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.economy.service.ItemSellPriceManager;
import com.otherworldinn.world.inn.InnData;
import com.otherworldinn.world.inn.decoration.InnDecorationBuffType;
import com.otherworldinn.world.inn.decoration.InnDecorationRegistry;
import com.otherworldinn.world.inn.decoration.InnDecorationStats;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * 客户端工具提示处理器
 *
 * <p>自动为注册的物品和方块添加工具提示
 */
@EventBusSubscriber(modid = OtherworldInn.MODID, value = Dist.CLIENT)
public class ModTooltips {
    private static final ResourceLocation CREATE_CLIPBOARD_ID =
            ResourceLocation.fromNamespaceAndPath("create", "clipboard");

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();
        Item item = itemStack.getItem();
        Level level = Minecraft.getInstance().level;
        TeamData clientTeam = TeamManager.getInstance().getClientPlayerTeam();

        // 显示出售价格
        int sellPrice = ItemSellPriceManager.getPrice(itemStack);
        if (sellPrice > 0 && clientTeam != null) {
            sellPrice =
                    clientTeam.getInnData()
                            .applyPositiveDecorationBuff(
                                    sellPrice, InnDecorationBuffType.DINING_INCOME_MULTIPLIER);
        }
        if (sellPrice > 0) {
            event.getToolTip()
                    .add(
                            Component.translatable("tooltip.otherworldinn.sell_price", sellPrice)
                                    .withStyle(ChatFormatting.YELLOW));
        }

        if (BuiltInRegistries.ITEM.getKey(item).equals(CREATE_CLIPBOARD_ID)) {
            event.getToolTip()
                    .add(
                            Component.translatable("tooltip.otherworldinn.create_clipboard_hint")
                                    .withStyle(style -> style.withColor(ModColors.INFO)));
        }

        if (itemStack.is(net.minecraft.world.item.Items.BOOK)
                || itemStack.is(net.minecraft.world.item.Items.WRITABLE_BOOK)) {
            event.getToolTip()
                    .add(
                            Component.translatable("tooltip.otherworldinn.book.roster")
                                    .withStyle(style -> style.withColor(ModColors.INFO)));
        }

        // 检查是否在城镇维度且物品被禁用
        if (level != null) {
            boolean inTown = level.dimension() == TownDimensions.TOWN_LEVEL;

            if (inTown && itemStack.is(OtherworldInn.BANNED_IN_TOWN)) {
                event.getToolTip()
                        .add(Component.translatable("tooltip.otherworldinn.banned_in_town"));
            }

            // 检查是否在非城镇维度且物品仅限城镇使用
            if (!inTown && itemStack.is(OtherworldInn.ONLY_IN_TOWN)) {
                event.getToolTip()
                        .add(Component.translatable("tooltip.otherworldinn.only_in_town"));
            }

            // 为旅社钥匙添加状态提示
            if (itemStack.is(ModItems.INN_KEY.get())) {
                if (clientTeam != null) {
                    InnData.InnState state = clientTeam.getInnData().getState();
                    Component stateText;
                    int color;

                    switch (state) {
                        case OPEN:
                            stateText =
                                    Component.translatable(
                                            "message.otherworldinn.desk_bell.status.open");
                            color = ModColors.GREEN;
                            break;
                        case CLOSED:
                        default:
                            stateText =
                                    Component.translatable(
                                            "message.otherworldinn.desk_bell.status.closed");
                            color = ModColors.RED;
                            break;
                    }

                    event.getToolTip()
                            .add(
                                    Component.translatable(
                                                    "message.otherworldinn.inn_key.status",
                                                    stateText)
                                            .withStyle(style -> style.withColor(color)));
                }
            }
        }

        // 检查物品注册表
        for (Map.Entry<DeferredItem<?>, ItemDataGenInfo> entry : ModItems.ITEM_INFOS.entrySet()) {
            if (entry.getKey().get() == item) {
                int count = entry.getValue().enTooltips().size();
                for (int i = 0; i < count; i++) {
                    event.getToolTip()
                            .add(
                                    Component.translatable(
                                                    item.getDescriptionId() + ".tooltip." + i)
                                            .withStyle(style -> style.withColor(ModColors.INFO)));
                }
                return;
            }
        }

        // 检查方块物品注册表
        if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();

            InnDecorationRegistry.getStats(block)
                    .ifPresent(stats -> addDecorationTooltips(event.getToolTip(), stats));

            for (Map.Entry<DeferredBlock<?>, BlockDataGenInfo> entry :
                    ModBlocks.BLOCK_INFOS.entrySet()) {
                if (entry.getKey().get() == block) {
                    int count = entry.getValue().enTooltips().size();
                    for (int i = 0; i < count; i++) {
                        event.getToolTip()
                                .add(
                                        Component.translatable(
                                                        block.getDescriptionId() + ".tooltip." + i)
                                                .withStyle(style -> style.withColor(ModColors.INFO)));
                    }
                    return;
                }
            }
        }
    }

    private static void addDecorationTooltips(List<Component> tooltip, InnDecorationStats stats) {
        addDecorationTooltip(
                tooltip,
                InnDecorationBuffType.LODGING_INCOME_MULTIPLIER,
                stats.lodgingIncomeMultiplier());
        addDecorationTooltip(
                tooltip, InnDecorationBuffType.DINING_INCOME_MULTIPLIER, stats.diningIncomeMultiplier());
        addDecorationTooltip(
                tooltip,
                InnDecorationBuffType.GUEST_ARRIVAL_SPEED_MULTIPLIER,
                stats.guestArrivalSpeedMultiplier());
        addDecorationTooltip(
                tooltip,
                InnDecorationBuffType.REPUTATION_GAIN_MULTIPLIER,
                stats.reputationGainMultiplier());
    }

    private static void addDecorationTooltip(
            List<Component> tooltip, InnDecorationBuffType type, double value) {
        if (value == 0.0D) {
            return;
        }
        String amount = formatDecorationPercentage(value);
        tooltip.add(
                switch (type) {
            case LODGING_INCOME_MULTIPLIER ->
                    Component.translatable("tooltip.otherworldinn.decoration.lodging_income", amount)
                            .withStyle(style -> style.withColor(ModColors.YELLOW));
            case DINING_INCOME_MULTIPLIER ->
                    Component.translatable("tooltip.otherworldinn.decoration.dining_income", amount)
                            .withStyle(style -> style.withColor(ModColors.YELLOW));
            case GUEST_ARRIVAL_SPEED_MULTIPLIER ->
                    Component.translatable(
                                    "tooltip.otherworldinn.decoration.guest_arrival_speed", amount)
                            .withStyle(style -> style.withColor(ModColors.INFO));
            case REPUTATION_GAIN_MULTIPLIER ->
                    Component.translatable(
                                    "tooltip.otherworldinn.decoration.reputation_gain", amount)
                            .withStyle(style -> style.withColor(ModColors.SUCCESS));
                });
    }

    private static String formatDecorationPercentage(double value) {
        double percent = value * 100.0D;
        if (Math.abs(percent - Math.rint(percent)) < 0.0001D) {
            return String.format("%+.0f%%", percent);
        }
        return String.format("%+.1f%%", percent);
    }
}
