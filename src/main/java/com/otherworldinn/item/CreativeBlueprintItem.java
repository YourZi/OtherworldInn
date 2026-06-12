package com.otherworldinn.item;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.schematics.SchematicItem;
import com.simibubi.create.content.schematics.SchematicProcessor;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class CreativeBlueprintItem extends Item {

    public CreativeBlueprintItem(Properties properties) {
        super(properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context,
            List<Component> tooltip, TooltipFlag flag) {
        if (stack.has(AllDataComponents.SCHEMATIC_FILE)) {
            tooltip.add(Component.literal(
                    ChatFormatting.GOLD + stack.get(AllDataComponents.SCHEMATIC_FILE)));
        } else {
            tooltip.add(Component.literal("("
                    + CreateLang.translateDirect("schematic.invalid").getString()
                    + ")").withStyle(ChatFormatting.RED));
        }
        tooltip.add(Component.translatable(
                "tooltip.otherworldinn.creative_blueprint.survival_place")
                .withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.FAIL;

        ItemStack stack = context.getItemInHand();

        if (!stack.has(AllDataComponents.SCHEMATIC_FILE)) {
            return InteractionResult.FAIL;
        }

        if (!player.isCreative()) {
            // 生存/冒险模式：直接放置结构
            if (context.getLevel().isClientSide) {
                return InteractionResult.SUCCESS;
            }

            StructureTemplate template = SchematicItem.loadSchematic(
                    context.getLevel(), stack);
            if (template.getSize().equals(BlockPos.ZERO)) {
                return InteractionResult.FAIL;
            }

            BlockPos anchor = stack.getOrDefault(
                    AllDataComponents.SCHEMATIC_ANCHOR, BlockPos.ZERO);
            StructurePlaceSettings settings = SchematicItem.getSettings(stack);
            settings.setIgnoreEntities(false);
            settings.clearProcessors();
            settings.addProcessor(SchematicProcessor.INSTANCE);

            BlockPos placePos = context.getClickedPos().relative(
                    context.getClickedFace());
            BlockPos offset = placePos.subtract(anchor);

            template.placeInWorld((ServerLevel) context.getLevel(),
                    offset, offset, settings,
                    context.getLevel().getRandom(), 2);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }

        // 创造模式：与普通蓝图行为一致，交给 Create 处理
        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn,
            InteractionHand handIn) {
        if (!playerIn.isShiftKeyDown() || handIn != InteractionHand.MAIN_HAND)
            return super.use(worldIn, playerIn, handIn);
        if (!playerIn.getItemInHand(handIn)
                .has(AllDataComponents.SCHEMATIC_FILE))
            return super.use(worldIn, playerIn, handIn);
        if (!playerIn.level().isClientSide())
            return new InteractionResultHolder<>(InteractionResult.SUCCESS,
                    playerIn.getItemInHand(handIn));
        CatnipServices.PLATFORM.executeOnClientOnly(() -> this::displayBlueprintScreen);
        return new InteractionResultHolder<>(InteractionResult.SUCCESS,
                playerIn.getItemInHand(handIn));
    }

    @OnlyIn(Dist.CLIENT)
    protected void displayBlueprintScreen() {
        ScreenOpener.open(new com.simibubi.create.content.schematics.client.SchematicEditScreen());
    }
}
