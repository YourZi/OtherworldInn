package com.otherworldinn.world.festival;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.network.ModMessages;
import com.otherworldinn.network.packet.S2CFestivalShopDiscountPacket;
import com.otherworldinn.util.WorldDayUtils;
import com.otherworldinn.world.data.TownSavedData;
import com.otherworldinn.world.dimension.TownDimensions;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.SeasonHelper;

/**
 * 节日服务
 *
 * <p>全局共享：整个城镇维度同步过节。每日判定当前激活的节日窗口，窗口变化时广播
 * {@link FestivalStartedEvent} / {@link FestivalEndedEvent} 并触发效果生命周期钩子。
 * 窗口为纯时间函数（季节绑定），只持久化"上次广播的窗口标识"用于事件去重。
 *
 * <p>查询入口（getActiveFestival / queryValue）供商店、客人、设施、旅社等系统在运行时
 * 主动查询节日加成，Serene Seasons 未安装时全部返回空/0。
 */
@EventBusSubscriber(modid = OtherworldInn.MODID)
public final class FestivalService {
    private static final String SERENE_SEASONS_MOD_ID = "sereneseasons";

    /** 调试用全局商店折扣率（0 = 关闭；>0 时覆盖节日折扣，对所有商店生效） */
    private static double debugGlobalDiscount;

    private FestivalService() {}

    public static double getDebugGlobalDiscount() {
        return debugGlobalDiscount;
    }

    /** 设置调试用全局商店折扣率（0~0.9，0 表示关闭） */
    public static void setDebugGlobalDiscount(double discount) {
        debugGlobalDiscount = Math.max(0.0D, Math.min(0.9D, discount));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel townLevel = event.getServer().getLevel(TownDimensions.TOWN_LEVEL);
        if (townLevel == null || !ModList.get().isLoaded(SERENE_SEASONS_MOD_ID)) {
            return;
        }
        ISeasonState state = SeasonHelper.getSeasonState(townLevel);
        Optional<FestivalDefinition> active = findActiveFestival(state);
        TownSavedData data = TownSavedData.get(townLevel);
        String lastWindow = data.getLastFestivalWindow();

        if (active.isPresent()) {
            FestivalDefinition festival = active.get();
            String windowKey = windowKey(festival, state, townLevel);
            if (!windowKey.equals(lastWindow)) {
                if (lastWindow != null) {
                    broadcastEnded(townLevel, lastWindow);
                }
                broadcastStarted(townLevel, festival, state);
                data.setLastFestivalWindow(windowKey);
            }
        } else if (lastWindow != null) {
            broadcastEnded(townLevel, lastWindow);
            data.setLastFestivalWindow(null);
        }
    }

    /** 当前城镇维度激活的节日（多个窗口重叠时取最先注册的） */
    public static Optional<FestivalDefinition> getActiveFestival(ServerLevel townLevel) {
        if (townLevel == null || townLevel.dimension() != TownDimensions.TOWN_LEVEL) {
            return Optional.empty();
        }
        if (!ModList.get().isLoaded(SERENE_SEASONS_MOD_ID)) {
            return Optional.empty();
        }
        return findActiveFestival(SeasonHelper.getSeasonState(townLevel));
    }

    /**
     * 通用数值查询：汇总当前激活节日所有效果对 key 的加成值，无节日/无匹配返回 0。
     * 参数 key 见 {@link FestivalEffect} 的查询键常量。
     */
    public static double queryValue(ServerLevel townLevel, String key, Object... args) {
        if (FestivalEffect.KEY_SHOP_DISCOUNT.equals(key) && debugGlobalDiscount > 0.0D) {
            return debugGlobalDiscount;
        }
        Optional<FestivalDefinition> festival = getActiveFestival(townLevel);
        if (festival.isEmpty()) {
            return 0.0;
        }
        double total = 0.0;
        for (FestivalEffect effect : festival.get().effects()) {
            total += effect.queryValue(key, args);
        }
        return total;
    }

    /** 当前激活节日的商店折扣表（shopType → 折扣率，含 "" 全局键），无节日返回空表 */
    public static Map<String, Double> getShopDiscounts(ServerLevel townLevel) {
        if (debugGlobalDiscount > 0.0D) {
            return Map.of("", debugGlobalDiscount);
        }
        Optional<FestivalDefinition> festival = getActiveFestival(townLevel);
        if (festival.isEmpty()) {
            return Map.of();
        }
        Map<String, Double> merged = new HashMap<>();
        for (FestivalEffect effect : festival.get().effects()) {
            if (effect instanceof ShopSaleEffect shopSale) {
                merged.putAll(shopSale.getDiscountRates());
            }
        }
        return merged;
    }

    /** 将当前节日商店折扣同步给所有在线玩家（节日开始/结束、跨窗口切换时调用） */
    public static void syncShopDiscounts(ServerLevel townLevel) {
        Map<String, Double> discounts = getShopDiscounts(townLevel);
        for (ServerPlayer player : townLevel.getServer().getPlayerList().getPlayers()) {
            ModMessages.sendToPlayer(new S2CFestivalShopDiscountPacket(discounts), player);
        }
    }

    /** 将当前节日商店折扣同步给指定玩家（登录时调用） */
    public static void syncShopDiscountsTo(ServerLevel townLevel, ServerPlayer player) {
        ModMessages.sendToPlayer(
                new S2CFestivalShopDiscountPacket(getShopDiscounts(townLevel)), player);
    }

    /** 玩家登录时同步当前节日商店折扣，保证客户端展示与结算一致 */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ServerLevel townLevel = player.getServer().getLevel(TownDimensions.TOWN_LEVEL);
        if (townLevel == null || !ModList.get().isLoaded(SERENE_SEASONS_MOD_ID)) {
            return;
        }
        syncShopDiscountsTo(townLevel, player);
    }

    private static Optional<FestivalDefinition> findActiveFestival(ISeasonState state) {
        for (FestivalDefinition festival : FestivalRegistry.getAll()) {
            if (festival.trigger().isActive(state)) {
                return Optional.of(festival);
            }
        }
        return Optional.empty();
    }

    private static String windowKey(
            FestivalDefinition festival, ISeasonState state, ServerLevel townLevel) {
        long currentDay = WorldDayUtils.currentDay(townLevel);
        return festival.id() + "@" + festival.trigger().windowStartDay(state, currentDay);
    }

    private static void broadcastStarted(
            ServerLevel townLevel, FestivalDefinition festival, ISeasonState state) {
        FestivalContext ctx =
                new FestivalContext(townLevel, festival, festival.trigger().dayIndex(state));
        NeoForge.EVENT_BUS.post(new FestivalStartedEvent(townLevel, festival));
        for (FestivalEffect effect : festival.effects()) {
            effect.onFestivalStart(townLevel, ctx);
        }
        broadcastChat(townLevel, Component.literal("【节日】").append(festival.zhName()).append(" 开始了！"));
        syncShopDiscounts(townLevel);
    }

    /** 结束广播：从持久化的窗口标识（id@起始日）解析出节日 id */
    private static void broadcastEnded(ServerLevel townLevel, String lastWindow) {
        String id = lastWindow.split("@", 2)[0];
        FestivalRegistry.get(id).ifPresent(festival -> {
            FestivalContext ctx = new FestivalContext(townLevel, festival, -1);
            NeoForge.EVENT_BUS.post(new FestivalEndedEvent(townLevel, festival));
            for (FestivalEffect effect : festival.effects()) {
                effect.onFestivalEnd(townLevel, ctx);
            }
            broadcastChat(townLevel, Component.literal("【节日】").append(festival.zhName()).append(" 结束了"));
        });
        syncShopDiscounts(townLevel);
    }

    /** 向所有在线玩家广播节日提示（节日为全局共享，任意维度玩家均可见） */
    private static void broadcastChat(ServerLevel townLevel, Component message) {
        for (ServerPlayer player : townLevel.getServer().getPlayerList().getPlayers()) {
            player.sendSystemMessage(message);
        }
    }
}
