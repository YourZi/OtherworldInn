package com.otherworldinn.item;

import com.otherworldinn.world.expedition.ChartComponentType;
import com.otherworldinn.world.expedition.ExpeditionNbtHelper;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ChartComponentItem extends Item {

    public ChartComponentItem(Properties properties) {
        super(properties);
    }

    public static String getComponentType(ItemStack stack) {
        CompoundTag tag = ExpeditionNbtHelper.readTag(stack);
        if (tag == null || !tag.contains("component_type")) return "blank";
        return tag.getString("component_type");
    }

    @Override
    public Component getName(ItemStack stack) {
        String componentType = getComponentType(stack);
        if ("blank".equals(componentType)) {
            return super.getName(stack);
        }

        ChartComponentType type = ChartComponentType.byId(componentType);
        if (type == null) {
            return super.getName(stack);
        }

        return Component.literal("")
                .append(Component.literal("[" + type.categoryDisplayName(true) + "]")
                        .withStyle(type.categoryColor()))
                .append(super.getName(stack).copy())
                .append(Component.literal("-" + type.displayName(true))
                        .withStyle(type.rarityColor()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
            List<Component> lines, TooltipFlag flag) {
        String componentType = getComponentType(stack);
        if ("blank".equals(componentType)) {
            lines.add(Component.translatable("tooltip.otherworldinn.chart_component.blank_entry")
                    .withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("tooltip.otherworldinn.chart_component.blank")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        ChartComponentType type = ChartComponentType.byId(componentType);
        if (type == null) return;

        for (String effect : type.effectLines()) {
            lines.add(Component.translatable("tooltip.otherworldinn.chart_component.effect",
                    effect).withStyle(ChatFormatting.AQUA));
        }
        for (String downside : type.downsideLines()) {
            lines.add(Component.translatable("tooltip.otherworldinn.chart_component.side_effect",
                    downside).withStyle(ChatFormatting.RED));
        }
        lines.add(Component.translatable("tooltip.otherworldinn.chart_component.fee", type.fee())
                .withStyle(ChatFormatting.YELLOW));

        ChatFormatting dimColor = switch (type.dimensionCategory()) {
            case NETHER -> ChatFormatting.RED;
            default -> ChatFormatting.GREEN;
        };
        String dimName = switch (type.dimensionCategory()) {
            case UNIVERSAL -> "通用";
            case NETHER -> "下界";
            default -> "主世界";
        };
        lines.add(Component.literal("  " + dimName).withStyle(dimColor));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        String type = getComponentType(stack);
        if ("blank".equals(type)) return false;
        ChartComponentType ct = ChartComponentType.byId(type);
        return ct != null && ct.rarity() == net.minecraft.world.item.Rarity.EPIC;
    }
}
