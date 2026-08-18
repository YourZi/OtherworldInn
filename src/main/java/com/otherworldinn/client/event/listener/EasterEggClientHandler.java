package com.otherworldinn.client.event.listener;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.client.PlayerEasterEggFlags;
import com.otherworldinn.init.ModItems;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * maimai 彩蛋的客户端效果（音效与图集切换）
 *
 * <p>状态持久化、金币扣除与进度发放由服务端的 {@link
 * com.otherworldinn.world.event.listener.EasterEggServerHandler} 处理。
 */
@EventBusSubscriber(modid = OtherworldInn.MODID, value = Dist.CLIENT)
public class EasterEggClientHandler {
    private static final ResourceLocation MAIMAI_SOUND_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "maimai");
    private static final ResourceLocation MAIMAI_END_SOUND_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "maimai_end");
    private static final Set<ResourceLocation> TRIGGER_BLOCK_IDS =
            Set.of(
                    ResourceLocation.fromNamespaceAndPath("yuushya", "washing_machine"),
                    ResourceLocation.fromNamespaceAndPath("yuushya", "washing_machine_sym"));

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        Level level = event.getLevel();
        if (!level.isClientSide) {
            return; // 服务端逻辑由 EasterEggServerHandler 处理
        }
        Player player = event.getEntity();
        ResourceLocation blockId =
                BuiltInRegistries.BLOCK.getKey(level.getBlockState(event.getPos()).getBlock());
        if (!TRIGGER_BLOCK_IDS.contains(blockId)) {
            return;
        }
        boolean wasEnabled = PlayerEasterEggFlags.isMaimaiAtlasEnabled();
        boolean shouldEnable = !wasEnabled;
        if (shouldEnable && !isHoldingCoin(player)) {
            return;
        }
        PlayerEasterEggFlags.setMaimaiAtlasEnabled(shouldEnable);
        player.swing(event.getHand());
        level.playLocalSound(
                event.getPos().getX() + 0.5D,
                event.getPos().getY() + 0.5D,
                event.getPos().getZ() + 0.5D,
                SoundEvent.createVariableRangeEvent(shouldEnable ? MAIMAI_SOUND_ID : MAIMAI_END_SOUND_ID),
                SoundSource.PLAYERS,
                1.0F,
                1.0F,
                false);
    }

    private static boolean isHoldingCoin(Player player) {
        return player.getMainHandItem().is(ModItems.COIN.get());
    }
}
