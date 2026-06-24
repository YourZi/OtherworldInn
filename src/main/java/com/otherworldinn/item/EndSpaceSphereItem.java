package com.otherworldinn.item;

import com.otherworldinn.world.teleport.TeleportUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;

public class EndSpaceSphereItem extends Item {
    private static final BlockPos END_PLATFORM_CENTER = new BlockPos(100, 49, 0);
    private static final Vec3 END_PLATFORM_TARGET = new Vec3(100.5D, 50.0D, 0.5D);
    private static final int PLATFORM_RADIUS = 2;

    public EndSpaceSphereItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.fail(stack);
        }

        ServerLevel endLevel = serverPlayer.server.getLevel(Level.END);
        if (endLevel == null) {
            return InteractionResultHolder.fail(stack);
        }

        ensureEndPlatform(endLevel);

        level.playSound(
                null,
                serverPlayer.getX(),
                serverPlayer.getY(),
                serverPlayer.getZ(),
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F);

        TeleportUtils.changeDimensionTo(serverPlayer, endLevel, END_PLATFORM_TARGET);

        endLevel.playSound(
                null,
                END_PLATFORM_TARGET.x,
                END_PLATFORM_TARGET.y,
                END_PLATFORM_TARGET.z,
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F);
        endLevel.sendParticles(
                ParticleTypes.PORTAL,
                END_PLATFORM_TARGET.x,
                END_PLATFORM_TARGET.y + 1.0D,
                END_PLATFORM_TARGET.z,
                32,
                0.5D,
                1.0D,
                0.5D,
                0.1D);

        if (!serverPlayer.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.success(stack);
    }

    private static void ensureEndPlatform(ServerLevel endLevel) {
        int chunkX = SectionPos.blockToSectionCoord(END_PLATFORM_CENTER.getX());
        int chunkZ = SectionPos.blockToSectionCoord(END_PLATFORM_CENTER.getZ());
        endLevel.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, true);

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -PLATFORM_RADIUS; x <= PLATFORM_RADIUS; x++) {
            for (int z = -PLATFORM_RADIUS; z <= PLATFORM_RADIUS; z++) {
                cursor.set(
                        END_PLATFORM_CENTER.getX() + x,
                        END_PLATFORM_CENTER.getY(),
                        END_PLATFORM_CENTER.getZ() + z);
                endLevel.setBlockAndUpdate(cursor, Blocks.OBSIDIAN.defaultBlockState());

                for (int y = 1; y <= 3; y++) {
                    cursor.set(
                            END_PLATFORM_CENTER.getX() + x,
                            END_PLATFORM_CENTER.getY() + y,
                            END_PLATFORM_CENTER.getZ() + z);
                    endLevel.setBlockAndUpdate(cursor, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }
}
