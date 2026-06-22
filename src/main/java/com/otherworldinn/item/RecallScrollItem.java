package com.otherworldinn.item;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.map.MapPoint;
import com.otherworldinn.world.map.TownDataProvider;
import com.otherworldinn.world.teleport.TeleportUtils;
import java.util.Optional;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RecallScrollItem extends Item {
    private static final ResourceLocation INN_POINT_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "inn");
    private static final String FAIL_IN_TOWN_KEY = "item.otherworldinn.recall_scroll.fail_in_town";
    private static final Vec3 FALLBACK_TOWN_POS = new Vec3(51.5D, 71.0D, 0.5D);

    public RecallScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.dimension() == TownDimensions.TOWN_LEVEL) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.translatable(FAIL_IN_TOWN_KEY)
                                .withStyle(style -> style.withColor(ModColors.ERROR)),
                        true);
            }
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(usedHand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide && livingEntity instanceof ServerPlayer player) {
            ServerLevel townLevel = player.getServer().getLevel(TownDimensions.TOWN_LEVEL);
            if (townLevel != null) {
                Vec3 target = resolveTargetPosition();

                level.playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.ENDERMAN_TELEPORT,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F);

                TeleportUtils.changeDimensionTo(player, townLevel, target);

                townLevel.playSound(
                        null,
                        target.x,
                        target.y,
                        target.z,
                        SoundEvents.ENDERMAN_TELEPORT,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F);
                townLevel.sendParticles(
                        ParticleTypes.PORTAL,
                        target.x,
                        target.y + 1.0D,
                        target.z,
                        32,
                        0.5D,
                        1.0D,
                        0.5D,
                        0.1D);
            }
        }

        if (livingEntity instanceof Player player && !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 60;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    private static Vec3 resolveTargetPosition() {
        Optional<MapPoint> innPoint = TownDataProvider.getPoint(INN_POINT_ID);
        return innPoint.map(MapPoint::worldPosition).orElse(FALLBACK_TOWN_POS);
    }
}
