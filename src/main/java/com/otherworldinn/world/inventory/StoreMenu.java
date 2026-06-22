package com.otherworldinn.world.inventory;

import com.otherworldinn.entity.base.StoreEntity;
import com.otherworldinn.init.ModMenuTypes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/** 商店菜单 */
public class StoreMenu extends AbstractContainerMenu {

    private final Player player;
    private final int storeEntityId;
    private final EntityType<?> storeEntityType;
    private final StoreEntity storeEntity;
    private final List<StoreEntity.StoreItem> storeItems;
    private final int favorLevel;
    private final int totalSpentCoins;
    private final int coinsPerFavorLevel;

    public StoreMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        // 客户端构造：从网络缓冲还原商店快照
        this(
                containerId,
                playerInventory,
                readStoreEntityId(extraData),
                readStoreEntityType(extraData),
                readFavorLevel(extraData),
                readTotalSpentCoins(extraData),
                readCoinsPerFavorLevel(extraData),
                readStoreItems(playerInventory, extraData));
    }

    private static int readStoreEntityId(FriendlyByteBuf extraData) {
        return extraData.readInt();
    }

    private static EntityType<?> readStoreEntityType(FriendlyByteBuf extraData) {
        ResourceLocation typeId = extraData.readResourceLocation();
        return BuiltInRegistries.ENTITY_TYPE.getOptional(typeId).orElse(null);
    }

    private static List<StoreEntity.StoreItem> readStoreItems(
            Inventory playerInventory, FriendlyByteBuf extraData) {
        List<StoreEntity.StoreItem> items = new ArrayList<>();
        int size = extraData.readInt();
        HolderLookup.Provider registryAccess = playerInventory.player.registryAccess();
        // 逐项反序列化，避免直接信任客户端缓存
        for (int i = 0; i < size; i++) {
            items.add(StoreEntity.StoreItem.load(registryAccess, extraData.readNbt()));
        }
        return items;
    }

    private static int readFavorLevel(FriendlyByteBuf extraData) {
        return extraData.readInt();
    }

    private static int readTotalSpentCoins(FriendlyByteBuf extraData) {
        return extraData.readInt();
    }

    private static int readCoinsPerFavorLevel(FriendlyByteBuf extraData) {
        return extraData.readInt();
    }

    public StoreMenu(int containerId, Inventory playerInventory, StoreEntity storeEntity) {
        this(
                containerId,
                playerInventory,
                storeEntity != null ? storeEntity.getId() : -1,
                storeEntity != null ? storeEntity.getType() : null,
                storeEntity != null ? storeEntity.getFavorLevel() : 1,
                storeEntity != null ? storeEntity.getTotalSpentCoins() : 0,
                storeEntity != null
                        ? storeEntity.getFavorCoinsPerLevelValue()
                        : StoreEntity.getCoinsPerFavorLevelValue(),
                storeEntity != null ? storeEntity.getStoreItems() : new ArrayList<>());
    }

    protected StoreMenu(
            int containerId,
            Inventory playerInventory,
            int storeEntityId,
            EntityType<?> storeEntityType,
            int favorLevel,
            int totalSpentCoins,
            int coinsPerFavorLevel,
            List<StoreEntity.StoreItem> storeItems) {
        super(ModMenuTypes.STORE_MENU.get(), containerId);
        this.player = playerInventory.player;
        this.storeEntityId = storeEntityId;
        this.storeEntityType = storeEntityType;
        // 尝试按实体 ID 绑定真实商店实体，失败时使用快照模式
        Entity entity =
                storeEntityId >= 0 ? playerInventory.player.level().getEntity(storeEntityId) : null;
        this.storeEntity = entity instanceof StoreEntity store ? store : null;
        this.favorLevel = favorLevel;
        this.totalSpentCoins = totalSpentCoins;
        this.coinsPerFavorLevel = coinsPerFavorLevel;
        this.storeItems = new ArrayList<>(storeItems);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        StoreEntity storeEntity = this.getStoreEntity();
        return storeEntity != null
                && storeEntity.isAlive()
                && storeEntity.distanceTo(player) < 8.0f;
    }

    public StoreEntity getStoreEntity() {
        if (this.storeEntity != null && this.storeEntity.isAlive()) {
            return this.storeEntity;
        }
        if (this.storeEntityId < 0) {
            return null;
        }
        // 兜底：当初始引用失效时再按 ID 二次查找
        Entity entity = this.player.level().getEntity(this.storeEntityId);
        return entity instanceof StoreEntity store ? store : null;
    }

    public EntityType<?> getStoreEntityType() {
        if (this.storeEntityType != null) {
            return this.storeEntityType;
        }
        StoreEntity storeEntity = this.getStoreEntity();
        return storeEntity != null ? storeEntity.getType() : null;
    }

    public List<StoreEntity.StoreItem> getStoreItems() {
        return storeItems;
    }

    public int getFavorLevel() {
        return this.favorLevel;
    }

    public int getTotalSpentCoins() {
        return this.totalSpentCoins;
    }

    public int getCoinsPerFavorLevel() {
        return this.coinsPerFavorLevel;
    }
}
