package com.otherworldinn.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.otherworldinn.client.ClientFestivalData;
import com.otherworldinn.entity.base.StoreEntity;
import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.network.ModMessages;
import com.otherworldinn.network.packet.C2SStorePurchasePacket;
import com.otherworldinn.world.inventory.StoreMenu;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

/** 商店屏幕 */
public class StoreScreen extends AbstractContainerScreen<StoreMenu> {

    private static final ResourceLocation FAVOR_BAR_BG_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "minecraft", "textures/gui/sprites/hud/experience_bar_background.png");
    private static final ResourceLocation FAVOR_BAR_FILL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "minecraft", "textures/gui/sprites/hud/experience_bar_progress.png");
    private static final ResourceLocation HEART_EMPTY_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "minecraft", "textures/gui/sprites/hud/heart/container.png");
    private static final ResourceLocation HEART_RED_FULL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "minecraft", "textures/gui/sprites/hud/heart/full.png");
    private static final ResourceLocation HEART_YELLOW_FULL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "minecraft", "textures/gui/sprites/hud/heart/absorbing_full.png");
    private static final int GRID_COLS = 4;
    private static final int GRID_ROWS = 5;
    private static final int GOODS_DISPLAY_ROWS = 4;
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_SPACING = 2;
    private static final int FAVOR_BAR_WIDTH = 3 * SLOT_SIZE;
    private static final int FAVOR_BAR_HEIGHT = 5;
    private static final int HEART_ICON_SIZE = 14;

    private final ResourceLocation backgroundTexture;

    @Nullable private StoreEntity.StoreItem selectedItem = null;
    private int purchaseQuantity = 1;

    private EditBox quantityEditBox;
    private Button confirmButton;
    private Button purchaseButton;
    private final List<StoreEntity.StoreItem> cart = new ArrayList<>();
    @Nullable private LivingEntity previewStoreEntity;

    private float scrollOffs = 0.0F;
    private boolean isScrolling = false;
    private static final int CART_ITEM_HEIGHT = 20;
    private static final int CART_DISPLAY_ROWS = 5;
    private static final int SCROLL_BAR_WIDTH = 4;

    private float goodsScrollOffs = 0.0F;

    public StoreScreen(StoreMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 256; // 宽屏界面
        this.imageHeight = 166;

        if (menu.getStoreEntity() != null) {
            this.backgroundTexture = menu.getStoreEntity().getStoreBackground();
        } else {
            this.backgroundTexture =
                    ResourceLocation.fromNamespaceAndPath(
                            "otherworldinn", "textures/gui/store.png");
        }
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        this.quantityEditBox =
                new EditBox(
                        this.font,
                        this.leftPos + 40,
                        this.topPos + 110,
                        40,
                        16,
                        Component.literal("1"));
        this.quantityEditBox.setValue("1");
        this.quantityEditBox.setFilter(s -> s.matches("\\d*"));
        this.quantityEditBox.setResponder(
                s -> {
                    try {
                        int val = Integer.parseInt(s);
                        this.purchaseQuantity = Math.max(1, val);
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                });
        this.addRenderableWidget(this.quantityEditBox);

        this.addRenderableWidget(
                Button.builder(
                                Component.literal("-"),
                                (btn) -> {
                                    int change = hasShiftDown() ? 16 : 1;
                                    if (this.purchaseQuantity > 1) {
                                        this.purchaseQuantity =
                                                Math.max(1, this.purchaseQuantity - change);
                                        this.quantityEditBox.setValue(
                                                String.valueOf(this.purchaseQuantity));
                                    }
                                })
                        .bounds(this.leftPos + 20, this.topPos + 110, 16, 16)
                        .build());

        this.addRenderableWidget(
                Button.builder(
                                Component.literal("+"),
                                (btn) -> {
                                    int change = hasShiftDown() ? 16 : 1;
                                    this.purchaseQuantity += change;
                                    this.quantityEditBox.setValue(
                                            String.valueOf(this.purchaseQuantity));
                                })
                        .bounds(this.leftPos + 85, this.topPos + 110, 16, 16)
                        .build());

        this.confirmButton =
                Button.builder(
                                Component.translatable("gui.otherworldinn.store.confirm"),
                                (btn) -> {
                                    if (this.selectedItem != null) {
                                        this.addToCart(this.selectedItem, this.purchaseQuantity);
                                    }
                                })
                        .bounds(this.leftPos + 21, this.topPos + 128, 80, 20)
                        .build();
        this.addRenderableWidget(this.confirmButton);

        this.purchaseButton =
                Button.builder(
                                Component.empty(),
                                (btn) -> {
                                    if (this.menu.getStoreEntity() != null
                                            && !this.cart.isEmpty()) {
                                        List<C2SStorePurchasePacket.PurchaseItem> purchaseItems =
                                                new ArrayList<>();
                                        for (StoreEntity.StoreItem cartItem : this.cart) {
                                            purchaseItems.add(
                                                    new C2SStorePurchasePacket.PurchaseItem(
                                                            cartItem.getItemStack(),
                                                            cartItem.getCurrentStock()));
                                        }

                                        ModMessages.sendToServer(
                                                new C2SStorePurchasePacket(
                                                        this.menu.getStoreEntity().getId(),
                                                        purchaseItems));

                                        this.cart.clear();
                                        this.updateButtons();
                                        this.onClose(); // 购买成功后关闭界面，体验较好
                                    }
                                })
                        .bounds(
                                this.getRightPanelStartX()
                                        + (this.getPanelTotalWidth() - 80) / 2
                                        - 1,
                                this.topPos + 128,
                                80,
                                20)
                        .build();
        this.addRenderableWidget(this.purchaseButton);

        this.updateButtons();
    }

    private void addToCart(StoreEntity.StoreItem item, int quantity) {
        int existingQuantity = 0;
        int existingIndex = -1;

        for (int i = 0; i < this.cart.size(); i++) {
            StoreEntity.StoreItem cartItem = this.cart.get(i);
            if (ItemStack.isSameItemSameComponents(cartItem.getItemStack(), item.getItemStack())) {
                existingQuantity = cartItem.getCurrentStock();
                existingIndex = i;
                break;
            }
        }

        int newQuantity = existingQuantity + quantity;

        // maxStock 为 -1 表示无库存上限
        if (item.getMaxStock() != -1 && newQuantity > item.getCurrentStock()) {
            newQuantity = item.getCurrentStock();

            if (existingQuantity >= item.getCurrentStock()) {
                return;
            }
        }

        if (existingIndex != -1) {
            StoreEntity.StoreItem cartItem = this.cart.get(existingIndex);
            this.cart.set(
                    existingIndex,
                    new StoreEntity.StoreItem(
                            cartItem.getItemStack(), item.getPrice(), -1, newQuantity));
        } else {
            this.cart.add(
                    new StoreEntity.StoreItem(
                            item.getItemStack(), item.getPrice(), -1, newQuantity));
        }

        this.updateButtons();
    }

    private void updateButtons() {
        boolean hasSelection = this.selectedItem != null;
        this.confirmButton.active = hasSelection;
        this.quantityEditBox.setEditable(hasSelection);

        int totalPrice = 0;
        int totalDiscounted = 0;
        for (StoreEntity.StoreItem item : this.cart) {
            // 这里 currentStock 借用来存购买数量；购物车存原价，结算先套当前好感折扣，再套节日折扣
            totalPrice += item.getPrice() * item.getCurrentStock();
            totalDiscounted +=
                    this.getFestivalDiscountedPrice(this.getDisplayPrice(item))
                            * item.getCurrentStock();
        }

        int playerBalance = 0;
        if (this.minecraft != null && this.minecraft.level != null) {
            TeamData teamData = TeamManager.getInstance().getClientTeamCache();
            if (teamData != null) {
                playerBalance = teamData.getCoins();
            }
        }

        boolean canAfford = playerBalance >= totalDiscounted;

        Component priceText =
                Component.translatable(
                        "gui.otherworldinn.store.purchase",
                        totalDiscounted < totalPrice ? totalDiscounted : totalPrice);
        if (!canAfford) {
            priceText = priceText.copy().withStyle(style -> style.withColor(ModColors.ERROR));
        }

        this.purchaseButton.setMessage(priceText);
        this.purchaseButton.active = !this.cart.isEmpty() && canAfford;
    }

    private int getCurrentFavorLevel() {
        return this.menu.getFavorLevel();
    }

    private boolean isFavorLocked(StoreEntity.StoreItem item) {
        return item.getRequiredFavorLevel() > this.getCurrentFavorLevel();
    }

    private boolean isProgressLocked(StoreEntity.StoreItem item) {
        return item.isViewerLocked();
    }

    private boolean isLocked(StoreEntity.StoreItem item) {
        return this.isFavorLocked(item) || this.isProgressLocked(item);
    }

    private int getDisplayPrice(StoreEntity.StoreItem item) {
        return StoreEntity.getDiscountedPriceForFavorLevel(
                item.getPrice(), this.getCurrentFavorLevel());
    }

    /** 商店类型标识（实体注册 ID 的 path，如 blacksmith），用于匹配节日折扣 */
    private String getShopType() {
        StoreEntity entity = this.menu.getStoreEntity();
        return entity != null ? EntityType.getKey(entity.getType()).getPath() : "";
    }

    /** 节日折扣后的单件价格（叠加在好感折扣之上）；无折扣返回原价 */
    private int getFestivalDiscountedPrice(int price) {
        double discount = ClientFestivalData.getShopDiscount(this.getShopType());
        if (discount <= 0.0D) {
            return price;
        }
        return Math.max(1, (int) Math.floor(price * (1.0D - discount)));
    }

    /**
     * 打折时构造「灰色删除线原价 + 绿色新价」价格文本，未打折返回 null（由调用方按原价显示）。
     *
     * @param unitPrice 单件原价（节日折扣前的价格）
     * @param quantity 数量
     */
    @Nullable
    private Component buildSalePriceText(int unitPrice, int quantity) {
        int discounted = getFestivalDiscountedPrice(unitPrice);
        if (discounted >= unitPrice) {
            return null;
        }
        return Component.literal(
                "§f\uE001§r§7§m"
                        + unitPrice * quantity
                        + "§r §f\uE001§r§a"
                        + discounted * quantity
                        + "§r");
    }

    private Component getSelectedDisplayName(StoreEntity.StoreItem item) {
        ItemStack stack = item.getItemStack();
        if (stack.is(Items.ENCHANTED_BOOK) && this.minecraft != null) {
            List<Component> tooltip = getTooltipFromItem(this.minecraft, stack);
            if (tooltip.size() > 1) {
                return tooltip.get(1).copy().withStyle(style -> style.withColor(ModColors.WHITE));
            }
        }
        return stack.getHoverName();
    }

    private int getFavorBarX() {
        int goodsAreaWidth = GRID_COLS * (SLOT_SIZE + SLOT_SPACING) - SLOT_SPACING;
        return 20 + (goodsAreaWidth - FAVOR_BAR_WIDTH) / 2;
    }

    private int getGoodsAreaWidth() {
        return GRID_COLS * (SLOT_SIZE + SLOT_SPACING) - SLOT_SPACING;
    }

    private int getGoodsAreaHeight() {
        return GOODS_DISPLAY_ROWS * CART_ITEM_HEIGHT;
    }

    private int getPanelStartY() {
        return this.topPos + 20;
    }

    private int getLeftPanelStartX() {
        return this.leftPos + 20;
    }

    private int getPanelTotalWidth() {
        return getGoodsAreaWidth() + 2 + SCROLL_BAR_WIDTH;
    }

    private int getRightPanelStartX() {
        return this.leftPos + this.imageWidth - 20 - getPanelTotalWidth() + 2;
    }

    private int getCartAreaHeight() {
        return CART_DISPLAY_ROWS * CART_ITEM_HEIGHT;
    }

    private int getFavorBarY() {
        return this.titleLabelY + 3;
    }

    private int getStoreEntityRenderSize() {
        return Math.round(44 * 0.6F);
    }

    private int getStoreEntityRenderCenterX() {
        return this.imageWidth / 2;
    }

    private int getStoreEntityRenderBottomY() {
        return 20 + this.getCartAreaHeight() + 10;
    }

    private float getFavorFillRatio() {
        int maxLevel = StoreEntity.getMaxFavorLevelValue();
        int coinsPerLevel = this.menu.getCoinsPerFavorLevel();
        int maxSpent = (maxLevel - 1) * coinsPerLevel;
        if (maxSpent <= 0) {
            return 1.0F;
        }
        return net.minecraft.util.Mth.clamp(
                this.menu.getTotalSpentCoins() / (float) maxSpent, 0.0F, 1.0F);
    }

    @Nullable
    private LivingEntity getDisplayStoreEntity() {
        if (this.menu.getStoreEntity() instanceof LivingEntity livingEntity) {
            return livingEntity;
        }
        if (this.previewStoreEntity != null) {
            return this.previewStoreEntity;
        }
        if (this.minecraft == null || this.minecraft.level == null) {
            return null;
        }
        if (this.menu.getStoreEntityType() != null) {
            net.minecraft.world.entity.Entity entity =
                    this.menu.getStoreEntityType().create(this.minecraft.level);
            if (entity instanceof LivingEntity livingEntity) {
                this.previewStoreEntity = livingEntity;
                return this.previewStoreEntity;
            }
        }
        return null;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        LivingEntity displayStoreEntity = this.getDisplayStoreEntity();
        if (displayStoreEntity != null) {
            int centerX = this.leftPos + this.getStoreEntityRenderCenterX();
            int bottomY = this.topPos + this.getStoreEntityRenderBottomY();
            int size = this.getStoreEntityRenderSize();
            float lookX = this.width / 2.0F - 12.0F;
            float lookY = this.height / 2.0F + 12.0F;
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    guiGraphics,
                    0,
                    0,
                    this.width,
                    this.height,
                    size,
                    0.0625F,
                    lookX,
                    lookY,
                    displayStoreEntity);
            int heartX = centerX - HEART_ICON_SIZE / 2;
            int heartY = bottomY - 14;
            guiGraphics.blit(
                    HEART_EMPTY_TEXTURE,
                    heartX,
                    heartY,
                    0,
                    0,
                    HEART_ICON_SIZE,
                    HEART_ICON_SIZE,
                    HEART_ICON_SIZE,
                    HEART_ICON_SIZE);
            ResourceLocation fillTexture =
                    this.getCurrentFavorLevel() >= StoreEntity.getMaxFavorLevelValue()
                            ? HEART_YELLOW_FULL_TEXTURE
                            : HEART_RED_FULL_TEXTURE;
            int fillHeight = Math.round(HEART_ICON_SIZE * this.getFavorFillRatio());
            if (fillHeight > 0) {
                guiGraphics.enableScissor(
                        heartX,
                        heartY + HEART_ICON_SIZE - fillHeight,
                        heartX + HEART_ICON_SIZE,
                        heartY + HEART_ICON_SIZE);
                guiGraphics.blit(
                        fillTexture,
                        heartX,
                        heartY,
                        0,
                        0,
                        HEART_ICON_SIZE,
                        HEART_ICON_SIZE,
                        HEART_ICON_SIZE,
                        HEART_ICON_SIZE);
                guiGraphics.disableScissor();
            }
        }
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        int listX = this.getRightPanelStartX();
        int listY = this.getPanelStartY();
        int listWidth = this.getGoodsAreaWidth();

        int scrollBarX = listX + listWidth + 2;
        int scrollBarY = listY;
        int scrollBarHeight = this.getCartAreaHeight();
        boolean canScroll = this.cart.size() > CART_DISPLAY_ROWS;

        guiGraphics.fill(
                scrollBarX,
                scrollBarY,
                scrollBarX + SCROLL_BAR_WIDTH,
                scrollBarY + scrollBarHeight,
                ModColors.BLACK_DARK_32);

        if (canScroll) {
            int sliderHeight =
                    (int)
                            ((float) (scrollBarHeight * scrollBarHeight)
                                    / (float) (this.cart.size() * CART_ITEM_HEIGHT));
            sliderHeight = Math.max(32, sliderHeight);
            int sliderY =
                    scrollBarY + (int) ((float) (scrollBarHeight - sliderHeight) * this.scrollOffs);
            guiGraphics.fill(
                    scrollBarX,
                    sliderY,
                    scrollBarX + SCROLL_BAR_WIDTH,
                    sliderY + sliderHeight,
                    ModColors.GRAY_50);
            guiGraphics.fill(
                    scrollBarX,
                    sliderY,
                    scrollBarX + SCROLL_BAR_WIDTH - 1,
                    sliderY + sliderHeight - 1,
                    ModColors.GRAY_LIGHT);
        } else {
            guiGraphics.fill(
                    scrollBarX,
                    scrollBarY,
                    scrollBarX + SCROLL_BAR_WIDTH,
                    scrollBarY + scrollBarHeight,
                    ModColors.BLACK_DARK_64);
        }

        int startIndex = 0;
        if (canScroll) {
            startIndex = (int) (this.scrollOffs * (this.cart.size() - CART_DISPLAY_ROWS));
        }

        guiGraphics.enableScissor(listX, listY, listX + listWidth, listY + scrollBarHeight);

        for (int i = startIndex; i < this.cart.size() && i < startIndex + CART_DISPLAY_ROWS; i++) {
            StoreEntity.StoreItem item = this.cart.get(i);
            int y = listY + (i - startIndex) * CART_ITEM_HEIGHT;

            guiGraphics.fill(listX, y, listX + listWidth, y + SLOT_SIZE, ModColors.BLACK_ALPHA_20);
            guiGraphics.renderItem(item.getItemStack(), listX, y);
            guiGraphics.renderItemDecorations(this.font, item.getItemStack(), listX, y);
            guiGraphics.drawString(
                    this.font,
                    Component.literal("×" + item.getCurrentStock()),
                    listX + 20,
                    y + 5,
                    ModColors.WHITE);
            Component priceText =
                    Component.literal(
                            "§f\uE001§r"
                                    + this.getFestivalDiscountedPrice(this.getDisplayPrice(item))
                                            * item.getCurrentStock());
            guiGraphics.drawString(
                    this.font,
                    priceText,
                    listX + listWidth - this.font.width(priceText) - 1,
                    y + 5,
                    ModColors.YELLOW);
        }

        guiGraphics.disableScissor();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int playerBalance = 0;
        if (this.minecraft != null && this.minecraft.level != null) {
            TeamData teamData = TeamManager.getInstance().getClientTeamCache();
            if (teamData != null) {
                playerBalance = teamData.getCoins();
            }
        }

        Component balanceText = Component.literal("§f\uE001§r" + playerBalance);
        int balanceWidth = this.font.width(balanceText);
        guiGraphics.drawString(
                this.font,
                balanceText,
                (this.imageWidth - balanceWidth) / 2 - 1,
                20,
                ModColors.WHITE,
                true);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        guiGraphics.blit(
                this.backgroundTexture,
                this.leftPos,
                this.topPos,
                0,
                0,
                this.imageWidth,
                this.imageHeight,
                this.imageWidth,
                this.imageHeight);

        int startX = this.getLeftPanelStartX();
        int startY = this.getPanelStartY();
        int goodsAreaWidth = this.getGoodsAreaWidth();
        int goodsAreaHeight = this.getGoodsAreaHeight();
        int goodsScrollBarX = startX + goodsAreaWidth + 2;
        int goodsScrollBarY = startY;

        List<StoreEntity.StoreItem> items = this.menu.getStoreItems();
        int totalRows = (int) Math.ceil(items.size() / (float) GRID_COLS);
        boolean canScrollGoods = totalRows > GOODS_DISPLAY_ROWS;
        int goodsStartRow =
                canScrollGoods
                        ? (int) (this.goodsScrollOffs * (totalRows - GOODS_DISPLAY_ROWS))
                        : 0;
        int goodsStartIndex = goodsStartRow * GRID_COLS;

        guiGraphics.fill(
                goodsScrollBarX,
                goodsScrollBarY,
                goodsScrollBarX + SCROLL_BAR_WIDTH,
                goodsScrollBarY + goodsAreaHeight,
                ModColors.BLACK_DARK_32);
        if (canScrollGoods) {
            int sliderHeight =
                    (int)
                            ((float) (goodsAreaHeight * goodsAreaHeight)
                                    / (float) (totalRows * CART_ITEM_HEIGHT));
            sliderHeight = Math.max(32, sliderHeight);
            int sliderY =
                    goodsScrollBarY
                            + (int)
                                    ((float) (goodsAreaHeight - sliderHeight)
                                            * this.goodsScrollOffs);
            guiGraphics.fill(
                    goodsScrollBarX,
                    sliderY,
                    goodsScrollBarX + SCROLL_BAR_WIDTH,
                    sliderY + sliderHeight,
                    ModColors.GRAY_50);
            guiGraphics.fill(
                    goodsScrollBarX,
                    sliderY,
                    goodsScrollBarX + SCROLL_BAR_WIDTH - 1,
                    sliderY + sliderHeight - 1,
                    ModColors.GRAY_LIGHT);
        } else {
            guiGraphics.fill(
                    goodsScrollBarX,
                    goodsScrollBarY,
                    goodsScrollBarX + SCROLL_BAR_WIDTH,
                    goodsScrollBarY + goodsAreaHeight,
                    ModColors.BLACK_DARK_64);
        }

        guiGraphics.enableScissor(
                startX, startY, startX + goodsAreaWidth, startY + goodsAreaHeight);

        for (int i = goodsStartIndex; i < items.size(); i++) {
            if (i >= goodsStartIndex + GRID_COLS * GOODS_DISPLAY_ROWS) break;

            int relative = i - goodsStartIndex;
            int col = relative % GRID_COLS;
            int row = relative / GRID_COLS;
            int x = startX + col * (SLOT_SIZE + SLOT_SPACING);
            int y = startY + row * (SLOT_SIZE + SLOT_SPACING);

            guiGraphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, ModColors.BLACK_ALPHA_20);

            StoreEntity.StoreItem storeItem = items.get(i);
            boolean isFavorLocked = this.isFavorLocked(storeItem);
            boolean isProgressLocked = this.isProgressLocked(storeItem);
            boolean isOutOfStock =
                    storeItem.getMaxStock() != -1 && storeItem.getCurrentStock() <= 0;

            if (isOutOfStock || isFavorLocked || isProgressLocked) {
                guiGraphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, ModColors.BLACK_ALPHA_62);
            }

            guiGraphics.renderItem(storeItem.getItemStack(), x + 1, y + 1);
            guiGraphics.renderItemDecorations(this.font, storeItem.getItemStack(), x + 1, y + 1);

            int stock = storeItem.getCurrentStock();

            for (StoreEntity.StoreItem cartItem : this.cart) {
                if (ItemStack.isSameItemSameComponents(
                        cartItem.getItemStack(), storeItem.getItemStack())) {
                    stock -= cartItem.getCurrentStock();
                }
            }

            String stockStr;
            int color = ModColors.WHITE;

            if (storeItem.getMaxStock() == -1) {
                stockStr = "∞";
            } else {
                stockStr = String.valueOf(stock);
                if (stock == 0) {
                    color = ModColors.ERROR;
                } else if (stock < storeItem.getCurrentStock()) {
                    // 购物车已占用部分库存时显示黄色
                    color = ModColors.YELLOW;
                }
            }
            if (isFavorLocked || isProgressLocked) {
                color = ModColors.ERROR;
            }

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 200); // 确保在物品上方
            guiGraphics.drawString(
                    this.font,
                    stockStr,
                    x + SLOT_SIZE - this.font.width(stockStr) - 1,
                    y + SLOT_SIZE - 9,
                    color,
                    true);
            guiGraphics.pose().popPose();

            // 选中高亮：最后绘制以覆盖在物品上方
            if (items.get(i) == this.selectedItem) {
                color = ModColors.WHITE_GHOST;
                int shadowColor = ModColors.GRAY_ALPHA_LIGHT;
                guiGraphics.fill(x, y, x + SLOT_SIZE, y + 1, shadowColor); // 上
                guiGraphics.fill(x, y + SLOT_SIZE - 1, x + SLOT_SIZE, y + SLOT_SIZE, color); // 下
                guiGraphics.fill(x, y, x + 1, y + SLOT_SIZE, shadowColor); // 左
                guiGraphics.fill(x + SLOT_SIZE - 1, y, x + SLOT_SIZE, y + SLOT_SIZE, color); // 右
            }
        }
        guiGraphics.disableScissor();

        if (this.selectedItem != null) {
            Component name = this.getSelectedDisplayName(this.selectedItem);
            int nameWidth = this.font.width(name);
            int areaWidth = GRID_COLS * (SLOT_SIZE + SLOT_SPACING) - SLOT_SPACING;
            startX = this.getLeftPanelStartX();
            int x = startX + (areaWidth - nameWidth) / 2;
            guiGraphics.drawString(this.font, name, x, this.topPos + 100, ModColors.WHITE);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int goodsStartX = this.getLeftPanelStartX();
        int goodsStartY = this.getPanelStartY();
        int goodsWidth = this.getPanelTotalWidth();
        int goodsHeight = this.getGoodsAreaHeight();
        List<StoreEntity.StoreItem> items = this.menu.getStoreItems();
        int totalGoodsRows = (int) Math.ceil(items.size() / (float) GRID_COLS);
        boolean canScrollGoods = totalGoodsRows > GOODS_DISPLAY_ROWS;

        if (mouseX >= goodsStartX
                && mouseX < goodsStartX + goodsWidth
                && mouseY >= goodsStartY
                && mouseY < goodsStartY + goodsHeight
                && canScrollGoods) {
            float scrollStep = 1.0F / (float) (totalGoodsRows - GOODS_DISPLAY_ROWS);
            this.goodsScrollOffs =
                    (float) ((double) this.goodsScrollOffs - scrollY * (double) scrollStep);
            this.goodsScrollOffs = net.minecraft.util.Mth.clamp(this.goodsScrollOffs, 0.0F, 1.0F);
            return true;
        }

        int cartStartX = this.getRightPanelStartX();
        int cartStartY = this.getPanelStartY();
        int cartWidth = this.getPanelTotalWidth();
        int cartHeight = this.getCartAreaHeight();

        if (mouseX >= cartStartX
                && mouseX < cartStartX + cartWidth
                && mouseY >= cartStartY
                && mouseY < cartStartY + cartHeight
                && this.cart.size() > CART_DISPLAY_ROWS) {
            float scrollStep = 1.0F / (float) (this.cart.size() - CART_DISPLAY_ROWS);
            this.scrollOffs = (float) ((double) this.scrollOffs - scrollY * (double) scrollStep);
            this.scrollOffs = net.minecraft.util.Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            return true;
        }

        if (this.cart.size() > CART_DISPLAY_ROWS) {
            // 每个滚轮单位滚动一项
            float scrollStep = 1.0F / (float) (this.cart.size() - CART_DISPLAY_ROWS);
            this.scrollOffs = (float) ((double) this.scrollOffs - scrollY * (double) scrollStep);
            this.scrollOffs = net.minecraft.util.Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int startX = this.getLeftPanelStartX();
        int startY = this.getPanelStartY();
        List<StoreEntity.StoreItem> items = this.menu.getStoreItems();
        int totalRows = (int) Math.ceil(items.size() / (float) GRID_COLS);
        int goodsStartRow =
                totalRows > GOODS_DISPLAY_ROWS
                        ? (int) (this.goodsScrollOffs * (totalRows - GOODS_DISPLAY_ROWS))
                        : 0;
        int goodsStartIndex = goodsStartRow * GRID_COLS;

        for (int i = goodsStartIndex; i < items.size(); i++) {
            if (i >= goodsStartIndex + GRID_COLS * GOODS_DISPLAY_ROWS) break;

            int relative = i - goodsStartIndex;
            int col = relative % GRID_COLS;
            int row = relative / GRID_COLS;
            int x = startX + col * (SLOT_SIZE + SLOT_SPACING);
            int y = startY + row * (SLOT_SIZE + SLOT_SPACING);

            if (mouseX >= x && mouseX < x + SLOT_SIZE && mouseY >= y && mouseY < y + SLOT_SIZE) {
                if (this.isLocked(items.get(i))) {
                    return false;
                }
                if (items.get(i).getMaxStock() != -1 && items.get(i).getCurrentStock() <= 0) {
                    return false;
                }

                boolean clickedSelectedItem = this.selectedItem == items.get(i);
                if (!clickedSelectedItem) {
                    this.selectedItem = items.get(i);
                    this.purchaseQuantity = 1;
                    this.quantityEditBox.setValue("1");
                    this.updateButtons();
                } else if (!hasShiftDown()) {
                    this.addToCart(this.selectedItem, this.purchaseQuantity);
                }

                // Shift+点击：快速添加 64 个（或剩余库存）
                if (hasShiftDown()) {
                    int addAmount = 64;
                    if (this.selectedItem.getMaxStock() != -1) {
                        addAmount = Math.min(addAmount, this.selectedItem.getCurrentStock());
                    }
                    this.addToCart(this.selectedItem, addAmount);
                }

                Minecraft.getInstance()
                        .getSoundManager()
                        .play(
                                net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                                        net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);
        int modelCenterX = this.leftPos + this.getStoreEntityRenderCenterX();
        int modelBottomY = this.topPos + this.getStoreEntityRenderBottomY();
        int modelSize = this.getStoreEntityRenderSize();
        int modelMinX = modelCenterX - modelSize / 2;
        int modelMaxX = modelCenterX + modelSize / 2;
        int modelMinY = modelBottomY - modelSize - 10;
        int modelMaxY = modelBottomY + 10;
        if (mouseX >= modelMinX
                && mouseX < modelMaxX
                && mouseY >= modelMinY
                && mouseY < modelMaxY) {
            int favorLevel = this.menu.getFavorLevel();
            int coinsPerLevel = this.menu.getCoinsPerFavorLevel();
            int maxLevel = StoreEntity.getMaxFavorLevelValue();
            int spentInCurrentLevel =
                    this.menu.getTotalSpentCoins() - Math.max(0, (favorLevel - 1) * coinsPerLevel);
            int requiredForNext = favorLevel >= maxLevel ? coinsPerLevel : coinsPerLevel;
            if (favorLevel >= maxLevel) {
                spentInCurrentLevel = coinsPerLevel;
            } else {
                spentInCurrentLevel =
                        net.minecraft.util.Mth.clamp(spentInCurrentLevel, 0, coinsPerLevel);
            }
            List<Component> favorTooltip = new ArrayList<>();
            Component favorLevelText =
                    Component.translatable("gui.otherworldinn.store.favor.level", favorLevel);
            if (favorLevel >= maxLevel) {
                favorLevelText =
                        favorLevelText
                                .copy()
                                .append(
                                        Component.literal("  -30%off!")
                                                .withStyle(
                                                        net.minecraft.ChatFormatting.GREEN,
                                                        net.minecraft.ChatFormatting.BOLD));
            }
            favorTooltip.add(favorLevelText);
            favorTooltip.add(
                    Component.translatable(
                            "gui.otherworldinn.store.favor.progress",
                            spentInCurrentLevel,
                            requiredForNext));
            guiGraphics.renderTooltip(
                    this.font, favorTooltip, java.util.Optional.empty(), mouseX, mouseY);
            return;
        }

        int startX = this.leftPos + 20;
        int startY = this.topPos + 20;
        List<StoreEntity.StoreItem> items = this.menu.getStoreItems();
        int totalRows = (int) Math.ceil(items.size() / (float) GRID_COLS);
        int goodsStartRow =
                totalRows > GOODS_DISPLAY_ROWS
                        ? (int) (this.goodsScrollOffs * (totalRows - GOODS_DISPLAY_ROWS))
                        : 0;
        int goodsStartIndex = goodsStartRow * GRID_COLS;

        for (int i = goodsStartIndex; i < items.size(); i++) {
            if (i >= goodsStartIndex + GRID_COLS * GOODS_DISPLAY_ROWS) break;

            int relative = i - goodsStartIndex;
            int col = relative % GRID_COLS;
            int row = relative / GRID_COLS;
            int x = startX + col * (SLOT_SIZE + SLOT_SPACING);
            int y = startY + row * (SLOT_SIZE + SLOT_SPACING);

            if (mouseX >= x && mouseX < x + SLOT_SIZE && mouseY >= y && mouseY < y + SLOT_SIZE) {
                StoreEntity.StoreItem item = items.get(i);
                List<Component> tooltip = getTooltipFromItem(minecraft, item.getItemStack());

                int displayPrice = this.getDisplayPrice(item);
                Component saleText = this.buildSalePriceText(displayPrice, 1);
                if (saleText != null) {
                    tooltip.add(saleText);
                } else {
                    tooltip.add(
                            Component.translatable(
                                            "gui.otherworldinn.store.price", displayPrice)
                                    .withStyle(net.minecraft.ChatFormatting.YELLOW));
                }

                if (item.getMaxStock() != -1) {
                    if (!item.isRestockable()) {
                        tooltip.add(
                                Component.translatable(
                                                "gui.otherworldinn.store.limit_purchase",
                                                item.getMaxStock())
                                        .withStyle(net.minecraft.ChatFormatting.GRAY));
                    } else {
                        tooltip.add(
                                Component.translatable(
                                                "gui.otherworldinn.store.stock",
                                                item.getCurrentStock(),
                                                item.getMaxStock())
                                        .withStyle(net.minecraft.ChatFormatting.GRAY));
                    }
                } else {
                    tooltip.add(
                            Component.translatable("gui.otherworldinn.store.stock.infinite")
                                    .withStyle(net.minecraft.ChatFormatting.GRAY));
                }
                if (this.isFavorLocked(item)) {
                    tooltip.add(
                            Component.translatable(
                                            "gui.otherworldinn.store.favor_unlock",
                                            item.getRequiredFavorLevel())
                                    .withStyle(style -> style.withColor(ModColors.ERROR)));
                }
                if (this.isProgressLocked(item)) {
                    String advancementTitleKey = item.getRequiredAdvancementTitleKey();
                    if (item.getRequiredAdvancementId() != null
                            && !item.getRequiredAdvancementId().isBlank()) {
                        net.minecraft.resources.ResourceLocation advancementId =
                                net.minecraft.resources.ResourceLocation.tryParse(
                                        item.getRequiredAdvancementId());
                        if (advancementId != null) {
                            String namespacePrefix =
                                    "minecraft".equals(advancementId.getNamespace())
                                            ? ""
                                            : advancementId.getNamespace() + ".";
                            advancementTitleKey =
                                    "advancements."
                                            + namespacePrefix
                                            + advancementId.getPath().replace('/', '.')
                                            + ".title";
                        }
                    }
                    Component advancementTitle =
                            advancementTitleKey == null || advancementTitleKey.isBlank()
                                    ? Component.literal("????")
                                    : Component.translatable(advancementTitleKey);
                    tooltip.add(
                            Component.translatable(
                                            "gui.otherworldinn.store.progress_unlock",
                                            advancementTitle)
                                    .withStyle(style -> style.withColor(ModColors.ERROR)));
                }
                guiGraphics.renderTooltip(
                        this.font, tooltip, item.getItemStack().getTooltipImage(), mouseX, mouseY);
                return;
            }
        }

        int listX = this.getRightPanelStartX();
        int listY = this.getPanelStartY();
        int listWidth = this.getGoodsAreaWidth();
        int scrollBarHeight = this.getCartAreaHeight();
        int startIndex = 0;
        if (this.cart.size() > CART_DISPLAY_ROWS) {
            startIndex = (int) (this.scrollOffs * (this.cart.size() - CART_DISPLAY_ROWS));
        }
        for (int i = startIndex; i < this.cart.size() && i < startIndex + CART_DISPLAY_ROWS; i++) {
            int y = listY + (i - startIndex) * CART_ITEM_HEIGHT;
            if (mouseX >= listX
                    && mouseX < listX + listWidth
                    && mouseY >= y
                    && mouseY < y + SLOT_SIZE
                    && mouseY < listY + scrollBarHeight) {
                StoreEntity.StoreItem cartItem = this.cart.get(i);
                List<Component> tooltip = getTooltipFromItem(minecraft, cartItem.getItemStack());
                tooltip.add(
                        Component.literal("×" + cartItem.getCurrentStock())
                                .withStyle(net.minecraft.ChatFormatting.GRAY));
                Component saleText =
                        this.buildSalePriceText(
                                this.getDisplayPrice(cartItem), cartItem.getCurrentStock());
                if (saleText != null) {
                    tooltip.add(saleText);
                } else {
                    tooltip.add(
                            Component.translatable(
                                            "gui.otherworldinn.store.price",
                                            this.getDisplayPrice(cartItem)
                                                    * cartItem.getCurrentStock())
                                    .withStyle(net.minecraft.ChatFormatting.YELLOW));
                }
                guiGraphics.renderTooltip(
                        this.font,
                        tooltip,
                        cartItem.getItemStack().getTooltipImage(),
                        mouseX,
                        mouseY);
                return;
            }
        }
    }
}
