package com.otherworldinn.item;

import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** 地契物品：圈选并购买区域以扩展旅社范围。 */
public class LandDeedItem extends Item {

    public static final int MAX_REGION_MIN_X = 30;
    public static final int MAX_REGION_MIN_Z = -28;
    public static final int MAX_REGION_MAX_X = 80;
    public static final int MAX_REGION_MAX_Z = 27;
    private static final int MAX_INN_RATING = 5;
    private static final String RATING_ICON = "§f\uE005§r";
    private static final int PRICE_PER_BLOCK = 2;
    private static final int MAX_EXPANDABLE_AREA =
            (MAX_REGION_MAX_X - MAX_REGION_MIN_X + 1) * (MAX_REGION_MAX_Z - MAX_REGION_MIN_Z + 1);
    private static final int BASE_INN_AREA = TeamData.getDefaultInnArea();

    public LandDeedItem(Properties properties) {
        super(properties);
    }

    public static boolean isWithinBounds(BlockPos pos1, BlockPos pos2) {
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        return minX >= MAX_REGION_MIN_X
                && maxX <= MAX_REGION_MAX_X
                && minZ >= MAX_REGION_MIN_Z
                && maxZ <= MAX_REGION_MAX_Z;
    }

    public static int getMaxAllowedAreaByRating(int rating) {
        int clampedRating = Math.max(0, Math.min(MAX_INN_RATING, rating));
        int expandableRange = Math.max(0, MAX_EXPANDABLE_AREA - BASE_INN_AREA);
        double progress = clampedRating / (double) MAX_INN_RATING;
        return BASE_INN_AREA + (int) Math.round(expandableRange * progress);
    }

    public static int calculateCurrentInnArea(TeamData team) {
        if (team == null) {
            return 0;
        }
        int area = 0;
        for (TeamData.InnRegion region : team.getInnRegions()) {
            area += (region.maxX() - region.minX() + 1) * (region.maxZ() - region.minZ() + 1);
        }
        return area;
    }

    public static boolean isWithinRatingAreaLimit(TeamData team, BlockPos pos1, BlockPos pos2) {
        if (team == null) {
            return true;
        }
        if (!isWithinBounds(pos1, pos2)) {
            return false;
        }
        int currentArea = calculateCurrentInnArea(team);
        int additionalArea = calculatePrice(team, pos1, pos2) / PRICE_PER_BLOCK;
        int projectedArea = currentArea + additionalArea;
        int maxAllowedArea = getMaxAllowedAreaByRating(team.getInnData().getRating());
        return projectedArea <= maxAllowedArea;
    }

    public static int getExpandedAreaByTeam(TeamData team) {
        return Math.max(0, calculateCurrentInnArea(team) - BASE_INN_AREA);
    }

    public static int getRemainingExpandableAreaByTeam(TeamData team) {
        if (team == null) {
            return 0;
        }
        int rating = team.getInnData().getRating();
        int maxAllowedArea = getMaxAllowedAreaByRating(rating);
        int maxExpandableArea = Math.max(0, maxAllowedArea - BASE_INN_AREA);
        int expandedArea = getExpandedAreaByTeam(team);
        return Math.max(0, maxExpandableArea - expandedArea);
    }

    private static String formatRatingDisplay(int rating) {
        int clampedRating = Math.max(0, Math.min(MAX_INN_RATING, rating));
        if (clampedRating == 0) {
            return "0";
        }
        return RATING_ICON.repeat(clampedRating);
    }

    /** 价格 = 有效面积（圈选面积减去已被旅社区域覆盖的部分）× 单价 PRICE_PER_BLOCK。 */
    public static int calculatePrice(TeamData team, BlockPos pos1, BlockPos pos2) {
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        // 如果没有队伍，则全额计算
        if (team == null) {
            return (maxX - minX + 1) * (maxZ - minZ + 1) * PRICE_PER_BLOCK;
        }

        int validCount = 0;
        List<TeamData.InnRegion> regions = team.getInnRegions();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                boolean overlapped = false;
                for (TeamData.InnRegion region : regions) {
                    if (region.contains(x, z)) {
                        overlapped = true;
                        break;
                    }
                }

                if (!overlapped) {
                    validCount++;
                }
            }
        }

        return validCount * PRICE_PER_BLOCK;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        InteractionHand hand = context.getHand();

        if (level.isClientSide || player == null) {
            return InteractionResult.PASS;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            TeamData team = TeamManager.getInstance().getPlayerTeam(serverPlayer);
            if (team == null) {
                player.displayClientMessage(
                        Component.translatable("command.otherworldinn.team.not_in_team"), true);
                return InteractionResult.FAIL;
            }

            ItemStack stack = player.getItemInHand(hand);
            CustomData customData =
                    stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag tag = customData.copyTag();

            if (!tag.contains("Pos1")) {
                tag.putLong("Pos1", pos.asLong());
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                player.displayClientMessage(
                        Component.translatable(
                                        "message.otherworldinn.land_deed.pos1_set",
                                        pos.toShortString())
                                .withStyle(style -> style.withColor(ModColors.INFO)),
                        true);
            } else if (!tag.contains("Pos2")) {
                // 记录 Pos2 并预览价格
                tag.putLong("Pos2", pos.asLong());
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                BlockPos pos1 = BlockPos.of(tag.getLong("Pos1"));

                if (!isWithinBounds(pos1, pos)) {
                    player.displayClientMessage(
                            Component.translatable(
                                            "message.otherworldinn.land_deed.fail_out_of_bounds")
                                    .withStyle(style -> style.withColor(ModColors.ERROR)),
                            true);
                    // 清除 Pos2，允许重新圈选
                    tag.remove("Pos2");
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    return InteractionResult.SUCCESS;
                }
                if (!isWithinRatingAreaLimit(team, pos1, pos)) {
                    player.displayClientMessage(
                            Component.translatable(
                                            "message.otherworldinn.land_deed.fail_rating_limit")
                                    .withStyle(style -> style.withColor(ModColors.ERROR)),
                            true);
                    tag.remove("Pos2");
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    return InteractionResult.SUCCESS;
                }

                int price = calculatePrice(team, pos1, pos);

                if (team.getCoins() >= price) {
                    player.displayClientMessage(
                            Component.translatable(
                                            "message.otherworldinn.land_deed.pos2_set_with_cost",
                                            price)
                                    .withStyle(style -> style.withColor(ModColors.INFO)),
                            true);
                } else {
                    player.displayClientMessage(
                            Component.translatable(
                                            "message.otherworldinn.land_deed.pos2_set_with_cost_fail",
                                            price,
                                            team.getCoins())
                                    .withStyle(style -> style.withColor(ModColors.ERROR)),
                            true);
                }
            } else {
                // 确认添加
                BlockPos pos1 = BlockPos.of(tag.getLong("Pos1"));
                BlockPos pos2 = BlockPos.of(tag.getLong("Pos2"));

                if (!isWithinBounds(pos1, pos2)) {
                    player.displayClientMessage(
                            Component.translatable(
                                            "message.otherworldinn.land_deed.fail_out_of_bounds")
                                    .withStyle(style -> style.withColor(ModColors.ERROR)),
                            true);
                    // 清除标记，允许重新圈选
                    tag.remove("Pos1");
                    tag.remove("Pos2");
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    return InteractionResult.FAIL;
                }
                if (!isWithinRatingAreaLimit(team, pos1, pos2)) {
                    player.displayClientMessage(
                            Component.translatable(
                                            "message.otherworldinn.land_deed.fail_rating_limit")
                                    .withStyle(style -> style.withColor(ModColors.ERROR)),
                            true);
                    tag.remove("Pos1");
                    tag.remove("Pos2");
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    return InteractionResult.FAIL;
                }

                int minX = Math.min(pos1.getX(), pos2.getX());
                int minZ = Math.min(pos1.getZ(), pos2.getZ());
                int maxX = Math.max(pos1.getX(), pos2.getX());
                int maxZ = Math.max(pos1.getZ(), pos2.getZ());

                int price = calculatePrice(team, pos1, pos2);

                if (TeamManager.getInstance().removeCoins(team, price, serverPlayer.getServer())) {
                    TeamData.InnRegion newRegion = new TeamData.InnRegion(minX, minZ, maxX, maxZ);

                    team.addRegion(newRegion);

                    TeamManager.getInstance().syncTeam(team, serverPlayer.getServer());

                    player.displayClientMessage(
                            Component.translatable("message.otherworldinn.land_deed.success", price)
                                    .withStyle(style -> style.withColor(ModColors.SUCCESS)),
                            true);

                    level.playSound(
                            null,
                            pos,
                            SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT,
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F);

                    tag.remove("Pos1");
                    tag.remove("Pos2");
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                    if (!player.getAbilities().instabuild && price > 0) {
                        stack.shrink(1);
                    }
                } else {
                    player.displayClientMessage(
                            Component.translatable(
                                            "message.otherworldinn.land_deed.fail_no_money",
                                            price,
                                            team.getCoins())
                                    .withStyle(style -> style.withColor(ModColors.ERROR)),
                            true);
                }
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level, Player player, InteractionHand usedHand) {
        return InteractionResultHolder.pass(player.getItemInHand(usedHand));
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag) {
        TeamData team = TeamManager.getInstance().getClientPlayerTeam();
        if (team != null) {
            int rating = team.getInnData().getRating();
            int expandedArea = getExpandedAreaByTeam(team);
            int remainingArea = getRemainingExpandableAreaByTeam(team);
            tooltipComponents.add(
                    Component.translatable(
                                    "tooltip.otherworldinn.land_deed.rating",
                                    formatRatingDisplay(rating))
                            .withStyle(style -> style.withColor(ModColors.INFO)));
            tooltipComponents.add(
                    Component.translatable(
                                    "tooltip.otherworldinn.land_deed.area_status",
                                    expandedArea,
                                    remainingArea)
                            .withStyle(style -> style.withColor(ModColors.LIGHT)));
        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
