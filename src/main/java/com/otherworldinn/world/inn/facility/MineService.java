package com.otherworldinn.world.inn.facility;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.util.WorldDayUtils;
import com.otherworldinn.world.data.TownSavedData;
import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.festival.FestivalEffect;
import com.otherworldinn.world.festival.FestivalService;
import com.otherworldinn.world.inn.facility.FacilityRegistry.FacilityDefinition;
import com.otherworldinn.world.inn.facility.FacilityRegistry.FacilityRange;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import com.simibubi.create.AllItems;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 矿井服务
 *
 * <p>矿井设施每日按等级向设施范围内的原版木桶填充随机矿物
 * 日期统一按世界时间（DayTime）计算，与商店补货、游商周期等共享同一时间源，
 * 玩家睡觉跳过时间会同步推进产出周期。
 */
@EventBusSubscriber(modid = OtherworldInn.MODID)
public final class MineService {

    /** 矿井设施 ID */
    public static final String MINE_FACILITY_ID = "mine";

    /** 矿井结构内预设木桶坐标：优先检查该坐标，不是木桶再遍历设施范围 */
    private static final BlockPos MINE_BARREL_DESIGN_POS = new BlockPos(4, 72, 81);

    /** 已定位的木桶坐标缓存：修复/升级时定位一次，日常产出直接复用，失效后重新定位 */
    @Nullable
    private static BlockPos cachedBarrelPos;

    /**
     * 保底产物池：每天必出，"达到该等级"即包含，高级别池包含低级别全部条目。
     */
    private record GuaranteedDrop(Item item, int minCount, int maxCount, int minLevel) {}

    private static List<GuaranteedDrop> guaranteedDrops() {
        return List.of(
                new GuaranteedDrop(Items.IRON_NUGGET, 6, 12, 1),
                new GuaranteedDrop(Items.RAW_IRON, 1, 3, 1),
                new GuaranteedDrop(Items.RAW_COPPER, 1, 3, 1),
                new GuaranteedDrop(AllItems.RAW_ZINC.get(), 1, 3, 2),
                new GuaranteedDrop(AllItems.CRUSHED_IRON.get(), 1, 3, 2),
                new GuaranteedDrop(AllItems.CRUSHED_COPPER.get(), 1, 3, 2),
                new GuaranteedDrop(AllItems.CRUSHED_ZINC.get(), 1, 3, 3),
                new GuaranteedDrop(AllItems.CRUSHED_GOLD.get(), 1, 3, 3),
                new GuaranteedDrop(Items.GOLD_INGOT, 1, 2, 3));
    }

    /** 稀有产物池：按概率额外掉落 */
    private record RareDrop(Item item, int minCount, int maxCount, int minLevel, double chance) {}

    private static List<RareDrop> rareDrops() {
        return List.of(
                new RareDrop(Items.EMERALD, 1, 1, 1, 0.05D),
                new RareDrop(Items.GOLD_NUGGET, 8, 16, 2, 0.15D),
                new RareDrop(Items.RAW_GOLD, 1, 3, 2, 0.08D),
                new RareDrop(Items.QUARTZ, 4, 8, 3, 0.10D),
                new RareDrop(Items.REDSTONE, 8, 16, 3, 0.12D),
                new RareDrop(Items.LAPIS_LAZULI, 4, 8, 3, 0.08D),
                new RareDrop(Items.DIAMOND, 1, 1, 3, 0.03D),
                new RareDrop(Items.ANCIENT_DEBRIS, 1, 1, 3, 0.01D));
    }

    private MineService() {}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel townLevel = event.getServer().getLevel(TownDimensions.TOWN_LEVEL);
        if (townLevel == null) {
            return;
        }

        long currentDay = WorldDayUtils.currentDay(townLevel);
        TownSavedData data = TownSavedData.get(townLevel);
        if (data.getLastMineFillDay() == currentDay) {
            return;
        }

        int facilityLevel = getMineLevel(event.getServer());
        if (facilityLevel <= 0) {
            return;
        }
        FacilityDefinition facility = FacilityRegistry.get(MINE_FACILITY_ID);
        if (facility == null) {
            return;
        }

        fillBarrel(townLevel, facility, facilityLevel);
        data.setLastMineFillDay(currentDay);
    }

    /** 矿井设施等级（多队伍共用同一城镇维度时取最高等级，保证只产出一份） */
    private static int getMineLevel(MinecraftServer server) {
        int level = 0;
        for (TeamData team : TeamManager.getInstance().getAllTeams(server)) {
            int teamLevel = team.getInnData().getFacilityLevel(MINE_FACILITY_ID);
            if (teamLevel > level) {
                level = teamLevel;
            }
        }
        return level;
    }

    /** 在设施范围内查找木桶并填充当日产物 */
    private static void fillBarrel(
            ServerLevel townLevel, FacilityDefinition facility, int facilityLevel) {
        Container barrel = getBarrel(townLevel, facility);
        if (barrel == null) {
            return;
        }
        double boost =
                FestivalService.queryValue(
                        townLevel, FestivalEffect.KEY_FACILITY_YIELD_BOOST, facility.id());
        for (GuaranteedDrop drop : guaranteedDrops()) {
            if (drop.minLevel() > facilityLevel) {
                continue;
            }
            int count =
                    drop.minCount()
                            + townLevel.random.nextInt(drop.maxCount() - drop.minCount() + 1);
            addToBarrel(barrel, new ItemStack(drop.item(), applyYieldBoost(count, boost)));
        }
        for (RareDrop drop : rareDrops()) {
            if (drop.minLevel() > facilityLevel || townLevel.random.nextDouble() >= drop.chance()) {
                continue;
            }
            int count =
                    drop.minCount()
                            + townLevel.random.nextInt(drop.maxCount() - drop.minCount() + 1);
            addToBarrel(barrel, new ItemStack(drop.item(), applyYieldBoost(count, boost)));
        }
    }

    /** 应用节日产出加成（加成后不少于原数量） */
    private static int applyYieldBoost(int count, double boost) {
        if (boost <= 0.0D) {
            return count;
        }
        return Math.max(count, (int) Math.round(count * (1.0D + boost)));
    }

    /** 修复/升级矿井后刷新木桶坐标缓存（结构已重新放置，重新定位一次） */
    public static void refreshBarrelCache(ServerLevel townLevel, FacilityDefinition facility) {
        cachedBarrelPos = findBarrelPos(townLevel, facility);
    }

    /** 取当前木桶：缓存有效则直接复用，否则（首次/失效/缺失）重新定位并缓存 */
    private static Container getBarrel(ServerLevel townLevel, FacilityDefinition facility) {
        if (cachedBarrelPos != null && isBarrelContainer(townLevel, cachedBarrelPos)) {
            return townLevel.getBlockEntity(cachedBarrelPos) instanceof Container container
                    ? container
                    : null;
        }
        refreshBarrelCache(townLevel, facility);
        if (cachedBarrelPos == null) {
            return null;
        }
        return townLevel.getBlockEntity(cachedBarrelPos) instanceof Container container
                ? container
                : null;
    }

    /** 定位木桶：先检查预设坐标，不是木桶再遍历设施范围（设计上范围内只放置一个） */
    private static BlockPos findBarrelPos(ServerLevel townLevel, FacilityDefinition facility) {
        if (isBarrelContainer(townLevel, MINE_BARREL_DESIGN_POS)) {
            return MINE_BARREL_DESIGN_POS;
        }
        FacilityRange range = facility.facilityRange().normalize();
        BlockPos from = range.from();
        BlockPos to = range.to();
        for (int x = from.getX(); x <= to.getX(); x++) {
            for (int y = from.getY(); y <= to.getY(); y++) {
                for (int z = from.getZ(); z <= to.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (isBarrelContainer(townLevel, pos)) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    /** 判断指定坐标是否为原版木桶容器 */
    private static boolean isBarrelContainer(ServerLevel townLevel, BlockPos pos) {
        return townLevel.getBlockState(pos).is(Blocks.BARREL)
                && townLevel.getBlockEntity(pos) instanceof Container;
    }

    /** 向容器"新增"物品：先堆叠到已有的相同物品槽，再放入空槽；桶满则丢弃 */
    private static void addToBarrel(Container barrel, ItemStack stack) {
        for (int i = 0; i < barrel.getContainerSize() && !stack.isEmpty(); i++) {
            ItemStack slot = barrel.getItem(i);
            if (!slot.isEmpty()
                    && ItemStack.isSameItemSameComponents(slot, stack)
                    && slot.getCount() < slot.getMaxStackSize()) {
                int space = slot.getMaxStackSize() - slot.getCount();
                int move = Math.min(space, stack.getCount());
                slot.grow(move);
                stack.shrink(move);
            }
        }
        if (stack.isEmpty()) {
            barrel.setChanged();
            return;
        }
        for (int i = 0; i < barrel.getContainerSize() && !stack.isEmpty(); i++) {
            if (barrel.getItem(i).isEmpty()) {
                barrel.setItem(i, stack.copy());
                stack.setCount(0);
            }
        }
        barrel.setChanged();
    }
}
