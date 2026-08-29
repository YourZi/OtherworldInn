package com.otherworldinn.network.packet;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.base.StoreEntity;
import com.otherworldinn.entity.store.BuilderBlueprintManager;
import com.otherworldinn.util.AdvancementUtils;
import com.otherworldinn.world.inventory.StoreMenu;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** 商店购买数据包 (Client -> Server) */
public record C2SStorePurchasePacket(int entityId, List<PurchaseItem> items)
        implements CustomPacketPayload {
    private static final int MAX_REQUEST_ITEMS = 54;
    private static final double MAX_PURCHASE_DISTANCE_SQR = 64.0D;

    public record PurchaseItem(ItemStack stack, int quantity) {
        public static final StreamCodec<RegistryFriendlyByteBuf, PurchaseItem> STREAM_CODEC =
                StreamCodec.composite(
                        ItemStack.STREAM_CODEC,
                        PurchaseItem::stack,
                        ByteBufCodecs.INT,
                        PurchaseItem::quantity,
                        PurchaseItem::new);
    }

    public static final Type<C2SStorePurchasePacket> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "store_purchase"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SStorePurchasePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    C2SStorePurchasePacket::entityId,
                    PurchaseItem.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    C2SStorePurchasePacket::items,
                    C2SStorePurchasePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SStorePurchasePacket packet, IPayloadContext context) {
        context.enqueueWork(
                () -> {
                    if (context.player() instanceof ServerPlayer player) {
                        Entity entity = player.level().getEntity(packet.entityId);
                        if (entity instanceof StoreEntity storeEntity) {
                            if (!(player.containerMenu instanceof StoreMenu storeMenu)) {
                                return;
                            }
                            if (storeMenu.getStoreEntity() != storeEntity) {
                                return;
                            }
                            if (player.distanceToSqr(storeEntity) > MAX_PURCHASE_DISTANCE_SQR) {
                                return;
                            }
                            if (packet.items == null
                                    || packet.items.isEmpty()
                                    || packet.items.size() > MAX_REQUEST_ITEMS) {
                                return;
                            }
                            TeamManager manager = TeamManager.getInstance();
                            TeamData team = manager.getPlayerTeam(player);

                            if (team == null) {
                                return;
                            }

                            int totalPrice = 0;
                            List<ItemStack> toGive = new ArrayList<>();
                            List<StoreEntity.StoreItem> toDeductStock = new ArrayList<>();
                            List<Integer> deductQuantities = new ArrayList<>();
                            // 按库存对象聚合数量，处理重复购买
                            Map<StoreEntity.StoreItem, Integer> requestedByStock =
                                    new IdentityHashMap<>();

                            for (PurchaseItem request : packet.items) {
                                if (request == null
                                        || request.stack == null
                                        || request.stack.isEmpty()) {
                                    return;
                                }
                                if (request.quantity <= 0) {
                                    return;
                                }
                                boolean found = false;
                                for (StoreEntity.StoreItem stockItem :
                                        storeEntity.getStoreItems()) {
                                    if (ItemStack.isSameItemSameComponents(
                                            stockItem.getItemStack(), request.stack)) {
                                        if (!storeEntity.canPurchase(player, stockItem)) {
                                            return;
                                        }
                                        int aggregatedQuantity =
                                                requestedByStock.getOrDefault(stockItem, 0)
                                                        + request.quantity;
                                        if (aggregatedQuantity <= 0) {
                                            return;
                                        }
                                        // 库存不足时直接判定交易失败，不支持部分购买
                                        if (stockItem.getMaxStock() != -1
                                                && stockItem.getCurrentStock()
                                                        < aggregatedQuantity) {
                                            return;
                                        }
                                        requestedByStock.put(stockItem, aggregatedQuantity);

                                        totalPrice +=
                                                storeEntity.getPurchasePrice(stockItem)
                                                        * request.quantity;
                                        if (totalPrice <= 0) {
                                            return;
                                        }
                                        int remaining = request.quantity;
                                        int maxStackSize =
                                                Math.max(1, stockItem.getItemStack().getMaxStackSize());
                                        while (remaining > 0) {
                                            int splitCount = Math.min(remaining, maxStackSize);
                                            ItemStack stack =
                                                    BuilderBlueprintManager.createPurchasedStackFromPreview(
                                                            player.level(),
                                                            stockItem.getItemStack());
                                            stack.setCount(splitCount);
                                            toGive.add(stack);
                                            remaining -= splitCount;
                                        }

                                        toDeductStock.add(stockItem);
                                        deductQuantities.add(request.quantity);
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    return;
                                }
                            }

                            if (totalPrice <= 0) {
                                return;
                            }
                            if (team.getCoins() >= totalPrice) {
                                manager.removeCoins(team, totalPrice, player.getServer());
                                storeEntity.addSpentCoins(totalPrice);
                                if (storeEntity.level() instanceof ServerLevel serverLevel) {
                                    AdvancementUtils.awardStoreFavorProgress(
                                            player, storeEntity, serverLevel);
                                }

                                for (int i = 0; i < toDeductStock.size(); i++) {
                                    StoreEntity.StoreItem stockItem = toDeductStock.get(i);
                                    int quantity = deductQuantities.get(i);
                                    if (stockItem.getMaxStock() != -1) {
                                        stockItem.setCurrentStock(
                                                stockItem.getCurrentStock() - quantity);
                                    }
                                }

                                for (ItemStack stack : toGive) {
                                    if (!player.getInventory().add(stack)) {
                                        player.drop(stack, false);
                                    }
                                }

                                // 库存刷新依赖后续同步
                            }
                        }
                    }
                });
    }
}
