package com.otherworldinn.item;

import com.otherworldinn.entity.base.StoreEntity;
import com.otherworldinn.world.dimension.TownDimensions;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class TownRosterItem extends Item {
    private static final int GLOW_DURATION_TICKS = 20 * 10;
    private static final AABB TOWN_RESIDENT_SCAN_BOX = new AABB(-1024, -64, -1024, 1024, 384, 1024);

    public TownRosterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.fail(stack);
        }
        if (serverLevel.dimension() != TownDimensions.TOWN_LEVEL) {
            return InteractionResultHolder.fail(stack);
        }

        List<StoreEntity> residents =
                serverLevel.getEntitiesOfClass(StoreEntity.class, TOWN_RESIDENT_SCAN_BOX);
        for (StoreEntity resident : residents) {
            resident.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOW_DURATION_TICKS, 0, false, false));
        }

        serverLevel.playSound(
                null,
                serverPlayer.blockPosition(),
                SoundEvents.BOOK_PAGE_TURN,
                SoundSource.PLAYERS,
                0.8F,
                1.0F);
        return InteractionResultHolder.success(stack);
    }
}
