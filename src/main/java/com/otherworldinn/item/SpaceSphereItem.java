package com.otherworldinn.item;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.world.expedition.ExpeditionNbtHelper;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class SpaceSphereItem extends Item {

    private static final String TAG_CAPTURED = "captured_entity";
    private static final String TAG_CAPTURED_ID = "captured_entity_id";
    private static final String TAG_CAPTURED_NAME = "captured_entity_name";

    public SpaceSphereItem(Properties properties) {
        super(properties);
    }

    //  按住实体 → 捕获

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        Level level = player.level();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (hasCapturedEntity(stack)) {
            player.displayClientMessage(
                    Component.translatable("message.otherworldinn.space_sphere.already_captured")
                            .withStyle(ChatFormatting.RED),
                    true);
            return InteractionResult.FAIL;
        }

        if (!target.getType().is(OtherworldInn.CAPTURABLE_WITH_SPACE_SPHERE)) {
            player.displayClientMessage(
                    Component.translatable("message.otherworldinn.space_sphere.not_capturable")
                            .withStyle(ChatFormatting.RED),
                    true);
            return InteractionResult.FAIL;
        }

        if (target instanceof Mob mob && mob.isLeashed()) {
            mob.dropLeash(true, true);
        }

        CompoundTag entityData = new CompoundTag();
        target.save(entityData);

        CompoundTag itemTag = ExpeditionNbtHelper.readTag(stack);
        itemTag.put(TAG_CAPTURED, entityData);
        itemTag.putString(TAG_CAPTURED_ID,
                BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString());
        itemTag.putString(TAG_CAPTURED_NAME, target.getName().getString());
        ExpeditionNbtHelper.writeTag(stack, itemTag);

        target.discard();

        level.playSound(null, player.blockPosition(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.8F);
        player.gameEvent(GameEvent.ENTITY_INTERACT, target);

        player.displayClientMessage(
                Component.translatable("message.otherworldinn.space_sphere.captured",
                        target.getType().getDescription())
                        .withStyle(ChatFormatting.GREEN),
                true);

        return InteractionResult.SUCCESS;
    }

    //  对准方块 → 释放

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (level.isClientSide) {
            return hasCapturedEntity(stack)
                    ? InteractionResult.SUCCESS
                    : InteractionResult.PASS;
        }
        if (!hasCapturedEntity(stack)) {
            return InteractionResult.PASS;
        }

        CompoundTag itemTag = ExpeditionNbtHelper.readTag(stack);
        CompoundTag entityData = itemTag.getCompound(TAG_CAPTURED);

        if (entityData.isEmpty()) {
            return InteractionResult.PASS;
        }

        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        double x = pos.getX() + face.getStepX() + 0.5;
        double y = pos.getY() + face.getStepY();
        double z = pos.getZ() + face.getStepZ() + 0.5;

        ServerLevel serverLevel = (ServerLevel) level;
        Entity spawned = EntityType.loadEntityRecursive(
                entityData, serverLevel, e -> {
                    e.moveTo(x, y, z, e.getYRot(), e.getXRot());
                    return e;
                });

        if (spawned != null) {
            //  重设 UUID 避免冲突
            spawned.setUUID(java.util.UUID.randomUUID());

            //  在保存/加载过程中实体可能丢失效果，重新应用
            if (entityData.contains("active_effects")) {
                var effectsList = entityData.getList("active_effects", CompoundTag.TAG_COMPOUND);
                if (spawned instanceof LivingEntity living) {
                    for (int i = 0; i < effectsList.size(); i++) {
                        CompoundTag effectTag = effectsList.getCompound(i);
                        var effect = BuiltInRegistries.MOB_EFFECT
                                .getOptional(ResourceLocation.tryParse(effectTag.getString("id")));
                        if (effect.isPresent()) {
                            int duration = effectTag.getInt("duration");
                            int amplifier = effectTag.getInt("amplifier");
                            boolean ambient = effectTag.getBoolean("ambient");
                            boolean showParticles = effectTag.getBoolean("show_particles");
                            boolean showIcon = effectTag.getBoolean("show_icon");
                            living.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                    BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect.get()),
                                    duration, amplifier, ambient,
                                    showParticles, showIcon));
                        }
                    }
                }
            }

            serverLevel.addFreshEntity(spawned);
            serverLevel.playSound(null, pos,
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.2F);
            serverLevel.gameEvent(GameEvent.ENTITY_PLACE, pos, GameEvent.Context.of(spawned));
        }

        itemTag.remove(TAG_CAPTURED);
        itemTag.remove(TAG_CAPTURED_ID);
        itemTag.remove(TAG_CAPTURED_NAME);
        ExpeditionNbtHelper.writeTag(stack, itemTag);

        player.displayClientMessage(
                Component.translatable("message.otherworldinn.space_sphere.released")
                        .withStyle(ChatFormatting.AQUA),
                true);

        return InteractionResult.SUCCESS;
    }

    //  右键空气 → 原有传送解锁

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        //  已捕获实体时，右键空气不做任何事
        if (hasCapturedEntity(stack)) {
            return InteractionResultHolder.pass(stack);
        }

        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.fail(stack);
        }

        TeamData team = TeamManager.getInstance().getPlayerTeam(serverPlayer);
        if (team == null) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.otherworldinn.space_sphere.no_team")
                            .withStyle(style -> style.withColor(ModColors.ERROR)),
                    true);
            return InteractionResultHolder.fail(stack);
        }

        if (team.isTeleportUnlocked()) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.otherworldinn.space_sphere.already_unlocked")
                            .withStyle(style -> style.withColor(ModColors.ERROR)),
                    true);
            return InteractionResultHolder.fail(stack);
        }

        TeamManager.getInstance().setTeamTeleportUnlocked(team, true, serverPlayer.getServer());

        Component broadcast =
                Component.translatable("message.otherworldinn.space_sphere.teleport_unlocked")
                        .withStyle(style -> style.withColor(ModColors.INFO));

        team.getMembers()
                .forEach(
                        memberId -> {
                            ServerPlayer member =
                                    serverPlayer.getServer().getPlayerList().getPlayer(memberId);
                            if (member != null) {
                                member.displayClientMessage(broadcast, false);
                                member.playNotifySound(
                                        SoundEvents.BEACON_ACTIVATE,
                                        SoundSource.PLAYERS,
                                        1.0F,
                                        1.0F);
                            }
                        });

        if (!serverPlayer.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.success(stack);
    }

    //  工具提示

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
            List<Component> lines, TooltipFlag flag) {
        if (hasCapturedEntity(stack)) {
            CompoundTag tag = ExpeditionNbtHelper.readTag(stack);
            String name = tag.getString(TAG_CAPTURED_NAME);
            String id = tag.getString(TAG_CAPTURED_ID);
            lines.add(Component.translatable(
                    "tooltip.otherworldinn.space_sphere.contains",
                    name.isEmpty() ? (id.isEmpty() ? "?" : id) : name)
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            lines.add(Component.translatable("tooltip.otherworldinn.space_sphere.release_hint")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            lines.add(Component.translatable("tooltip.otherworldinn.space_sphere.capture_hint")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return hasCapturedEntity(stack);
    }

    public static boolean hasCapturedEntity(ItemStack stack) {
        CompoundTag tag = ExpeditionNbtHelper.readTag(stack);
        return tag.contains(TAG_CAPTURED);
    }
}
