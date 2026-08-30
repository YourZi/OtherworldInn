package com.otherworldinn.world.event.listener;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.init.ModItems;
import com.otherworldinn.util.AdvancementUtils;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * maimai 彩蛋的服务端逻辑。
 * 客户端处理器在专用服务器上不会注册，因此状态持久化与金币扣除必须放在独立的服务端监听中。
 */
@EventBusSubscriber(modid = OtherworldInn.MODID)
public class EasterEggServerHandler {
    private static final String SERVER_STATE_NBT_KEY = "otherworldinn_maimai_enabled";
    private static final ResourceLocation MAIMAI_HIDDEN_ADVANCEMENT_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "maimai_hidden");
    private static final Set<ResourceLocation> TRIGGER_BLOCK_IDS =
            Set.of(
                    ResourceLocation.fromNamespaceAndPath("yuushya", "washing_machine"),
                    ResourceLocation.fromNamespaceAndPath("yuushya", "washing_machine_sym"));

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (event.getLevel().isClientSide) {
            return;
        }
        Player player = event.getEntity();
        ResourceLocation blockId =
                BuiltInRegistries.BLOCK.getKey(event.getLevel().getBlockState(event.getPos()).getBlock());
        if (!TRIGGER_BLOCK_IDS.contains(blockId)) {
            return;
        }
        CompoundTag persistentData = player.getPersistentData();
        boolean wasEnabled = persistentData.getBoolean(SERVER_STATE_NBT_KEY);
        boolean shouldEnable = !wasEnabled;
        if (shouldEnable) {
            if (!isHoldingCoin(player)) {
                return;
            }
            consumeOneCoin(player);
            if (player instanceof ServerPlayer serverPlayer) {
                AdvancementUtils.award(serverPlayer, MAIMAI_HIDDEN_ADVANCEMENT_ID);
            }
        }
        persistentData.putBoolean(SERVER_STATE_NBT_KEY, shouldEnable);
    }

    private static boolean isHoldingCoin(Player player) {
        return player.getMainHandItem().is(ModItems.COIN.get());
    }

    private static void consumeOneCoin(Player player) {
        if (player.getAbilities().instabuild) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!stack.isEmpty()) {
            stack.shrink(1);
        }
    }
}
