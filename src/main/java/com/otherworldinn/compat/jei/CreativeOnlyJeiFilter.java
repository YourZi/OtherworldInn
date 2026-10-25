package com.otherworldinn.compat.jei;

import com.otherworldinn.OtherworldInn;
import java.util.List;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * 生存模式下从 JEI 里隐藏"创造专属"物品（目前是刷怪蛋）。
 *
 * <p>为什么只做刷怪蛋：命令方块、屏障、结构方块、调试棒这类调试物品本身没有配方，
 * JEI 不会给它们配方页，整合包通常也已经过滤掉；真正会出现在列表里、生存又拿不到的只有刷怪蛋。
 *
 * <p>实现方式是 JEI 19 的运行时增删接口：
 * {@code IIngredientManager#removeIngredientsAtRuntime} / {@code addIngredientsAtRuntime}，
 * 在玩家切换游戏模式时同步一次（不是每帧）。
 *
 * <p>四种游戏模式都显式处理：只有 {@link GameType#CREATIVE} 显示这些物品，
 * 生存 / 冒险 / 旁观一律隐藏（这三者在原版都拿不到刷怪蛋）。
 *
 * <p>要隐藏别的物品，在 {@link #isHiddenFromSurvival(ItemStack)} 里加判断即可。
 */
@EventBusSubscriber(modid = OtherworldInn.MODID, value = Dist.CLIENT)
public final class CreativeOnlyJeiFilter {
    private static IIngredientManager ingredientManager;
    private static List<ItemStack> hiddenStacks = List.of();
    /** null 表示还没同步过（例如 JEI 尚未就绪或玩家还没进世界）。 */
    private static GameType lastGameType;

    private CreativeOnlyJeiFilter() {}

    /** 由 JEI 插件在注册阶段与运行时就绪时调用。 */
    public static void attach(IIngredientManager manager) {
        if (manager == null) {
            return;
        }
        ingredientManager = manager;
        collectHiddenStacks();
        Minecraft minecraft = Minecraft.getInstance();
        lastGameType = currentGameType(minecraft);
        applyForCurrentMode();
    }

    private static void collectHiddenStacks() {
        if (ingredientManager == null) {
            hiddenStacks = List.of();
            return;
        }
        // 必须用 JEI 里真实存在的栈，不能 new ItemStack(item)：带组件的物品会被判为不同原料而移除失败
        hiddenStacks =
                ingredientManager.getAllItemStacks().stream()
                        .filter(CreativeOnlyJeiFilter::isHiddenFromSurvival)
                        .toList();
    }

    private static boolean isHiddenFromSurvival(ItemStack stack) {
        return stack.getItem() instanceof SpawnEggItem;
    }

    /** 当前客户端的游戏模式；还没进世界时返回 null。 */
    private static GameType currentGameType(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.gameMode == null) {
            return null;
        }
        return minecraft.gameMode.getPlayerMode();
    }

    /**
     * 哪些游戏模式需要隐藏这些物品。
     *
     * <p>只有创造模式能真正拿到刷怪蛋；生存、冒险、旁观在原版都拿不到，
     * 所以三种模式一律隐藏。将来若要给某个模式开例外，只改这里。
     */
    private static boolean shouldHideIn(GameType gameType) {
        return switch (gameType) {
            case CREATIVE -> false;
            case SURVIVAL, ADVENTURE, SPECTATOR -> true;
        };
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            lastGameType = null;
            return;
        }
        GameType gameType = currentGameType(minecraft);
        if (gameType == null) {
            lastGameType = null;
            return;
        }
        if (lastGameType == null) {
            lastGameType = gameType;
            return;
        }
        if (lastGameType == gameType) {
            return;
        }
        lastGameType = gameType;
        applyForCurrentMode();
    }

    private static void applyForCurrentMode() {
        if (ingredientManager == null || hiddenStacks.isEmpty() || lastGameType == null) {
            return;
        }
        if (shouldHideIn(lastGameType)) {
            ingredientManager.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, hiddenStacks);
        } else {
            ingredientManager.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, hiddenStacks);
        }
    }
}
