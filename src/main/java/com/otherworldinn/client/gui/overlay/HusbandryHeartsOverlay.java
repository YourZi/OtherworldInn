package com.otherworldinn.client.gui.overlay;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.client.gui.SmoothGuiAnimations;
import com.otherworldinn.world.husbandry.HusbandryData;
import com.otherworldinn.world.husbandry.HusbandryService;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * 好感度心形 HUD：右键动物后，在准星下方居中显示一行 {@link HusbandryData#MAX_HEARTS} 颗心，
 * 好感度有多少就亮多少；不满一颗心的部分沿用商店面板的画法（垂直裁剪填充，见 StoreScreen）。
 */
@EventBusSubscriber(
        modid = OtherworldInn.MODID,
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.MOD)
public final class HusbandryHeartsOverlay {
    private static final ResourceLocation LAYER_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "husbandry_hearts");
    private static final ResourceLocation HEART_EMPTY_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "minecraft", "textures/gui/sprites/hud/heart/container.png");
    private static final ResourceLocation HEART_FULL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "minecraft", "textures/gui/sprites/hud/heart/full.png");

    /** 单颗心的边长（原版心形贴图是 9×9，取 10 基本等于原尺寸，不会糊）。 */
    private static final int ICON_SIZE = 10;
    /** 两颗心之间的空隙。 */
    private static final int ICON_SPACING = 4;
    /** 相对屏幕中心（准星）的下移距离。 */
    private static final int CROSSHAIR_OFFSET_Y = 10;

    /** 准星离开目标后，HUD 在 1 秒（20 tick）后完全消失。 */
    private static final float LOOK_AWAY_DISMISS_TICKS = 20.0F;
    /** 消失前最后这 10 tick 用来淡出，所以"移开 0.5 秒后开始变淡，1 秒时刚好没影"。 */
    private static final float FADE_TICKS = 10.0F;

    /** 相邻两颗心开始冒出的间隔（毫秒）—— 与单颗时长重叠，形成一道"波"而不是逐个停等。 */
    private static final long REVEAL_STAGGER_MS = 55L;
    /** 单颗心从"只露一点"到"完全冒出"的时长（毫秒）。 */
    private static final long REVEAL_DURATION_MS = 180L;
    /** 冒出时从下方滑上来的距离（像素）；用 easeOutBack 会略微冲过终点再回落。 */
    private static final int REVEAL_RISE_PX = 6;
    /** 每颗心落定后的额外"弹一下"时长（毫秒）。 */
    private static final long SETTLE_PULSE_MS = 170L;
    /** 落定脉冲的最大放大比例（1.0 = 不放大）。 */
    private static final float SETTLE_PULSE_SCALE = 0.12F;

    private static int targetEntityId = -1;
    /** 准星已经离开目标多少 tick（重新看向目标时归零）。 */
    private static float awayTicks;
    /** 本轮显示的起始时刻（{@link net.minecraft.Util#getMillis()}），用于计算冒出动画进度。 */
    private static long shownAtMillis;

    private HusbandryHeartsOverlay() {}

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, LAYER_ID, HusbandryHeartsOverlay::render);
    }

    /** 由交互监听器调用：记住目标动物并重置显示时长；换了动物或已消失时重新播放冒出动画。 */
    public static void show(Animal animal) {
        boolean restartAnimation =
                targetEntityId != animal.getId() || awayTicks >= LOOK_AWAY_DISMISS_TICKS;
        targetEntityId = animal.getId();
        awayTicks = 0.0F;
        if (restartAnimation) {
            shownAtMillis = Util.getMillis();
        }
    }

    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (awayTicks >= LOOK_AWAY_DISMISS_TICKS) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        Entity entity = minecraft.level.getEntity(targetEntityId);
        if (!(entity instanceof Animal animal) || !animal.isAlive()) {
            awayTicks = LOOK_AWAY_DISMISS_TICKS;
            return;
        }

        // 准星仍指着这只动物就保持常亮；一旦移开就开始累计，满 1 秒彻底消失
        Entity lookedAt = minecraft.crosshairPickEntity;
        if (lookedAt != null && lookedAt.getId() == targetEntityId) {
            awayTicks = 0.0F;
        } else {
            awayTicks += deltaTracker.getGameTimeDeltaTicks();
            if (awayTicks >= LOOK_AWAY_DISMISS_TICKS) {
                awayTicks = LOOK_AWAY_DISMISS_TICKS;
                return;
            }
        }
        float alpha =
                Mth.clamp((LOOK_AWAY_DISMISS_TICKS - awayTicks) / FADE_TICKS, 0.0F, 1.0F);
        if (alpha <= 0.0F) {
            return;
        }

        float hearts =
                HusbandryService.getAffection(animal) / (float) HusbandryData.AFFECTION_PER_HEART;
        int totalWidth =
                HusbandryData.MAX_HEARTS * ICON_SIZE
                        + (HusbandryData.MAX_HEARTS - 1) * ICON_SPACING;
        int startX = (graphics.guiWidth() - totalWidth) / 2;
        int y = graphics.guiHeight() / 2 + CROSSHAIR_OFFSET_Y;

        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        long now = Util.getMillis();
        for (int i = 0; i < HusbandryData.MAX_HEARTS; i++) {
            int x = startX + i * (ICON_SIZE + ICON_SPACING);

            long elapsed = now - shownAtMillis - i * REVEAL_STAGGER_MS;
            float t = Mth.clamp((float) elapsed / (float) REVEAL_DURATION_MS, 0.0F, 1.0F);
            if (t <= 0.0F) {
                continue;
            }
            // 裁剪用单调的 easeOutCubic（不能过冲），位移用 easeOutBack（会冲过终点再回落）
            int revealHeight = Math.round(ICON_SIZE * SmoothGuiAnimations.easeOutCubic(t));
            if (revealHeight <= 0) {
                continue;
            }
            int drawY =
                    y + Math.round((1.0F - SmoothGuiAnimations.easeOutBack(t)) * REVEAL_RISE_PX);

            // 落定后的小弹跳：此时心已完整，可以安全地用 pose 缩放（scissor 是屏幕坐标，不能与缩放叠加）
            float settleScale = 1.0F;
            if (t >= 1.0F) {
                long settleAge = elapsed - REVEAL_DURATION_MS;
                if (settleAge < SETTLE_PULSE_MS) {
                    float u = settleAge / (float) SETTLE_PULSE_MS;
                    settleScale = 1.0F + SETTLE_PULSE_SCALE * SmoothGuiAnimations.punch(u);
                }
            }
            boolean pulsed = settleScale != 1.0F;
            if (pulsed) {
                SmoothGuiAnimations.pushCenteredScale(
                        graphics, x + ICON_SIZE / 2.0F, drawY + ICON_SIZE / 2.0F, settleScale);
            }

            float ratio = Mth.clamp(hearts - i, 0.0F, 1.0F);
            int fillHeight = Math.round(ICON_SIZE * ratio);

            if (t < 1.0F) {
                // 冒出中：空心底与填充都只露出"已冒出"的那一段（两者贴底，取交集高度）
                graphics.enableScissor(
                        x, drawY + ICON_SIZE - revealHeight, x + ICON_SIZE, drawY + ICON_SIZE);
                drawHeart(graphics, HEART_EMPTY_TEXTURE, x, drawY);
                graphics.disableScissor();

                int visibleFill = Math.min(fillHeight, revealHeight);
                if (visibleFill > 0) {
                    graphics.enableScissor(
                            x,
                            drawY + ICON_SIZE - visibleFill,
                            x + ICON_SIZE,
                            drawY + ICON_SIZE);
                    drawHeart(graphics, HEART_FULL_TEXTURE, x, drawY);
                    graphics.disableScissor();
                }
            } else {
                drawHeart(graphics, HEART_EMPTY_TEXTURE, x, drawY);
                if (fillHeight > 0) {
                    graphics.enableScissor(
                            x,
                            drawY + ICON_SIZE - fillHeight,
                            x + ICON_SIZE,
                            drawY + ICON_SIZE);
                    drawHeart(graphics, HEART_FULL_TEXTURE, x, drawY);
                    graphics.disableScissor();
                }
            }

            if (pulsed) {
                graphics.pose().popPose();
            }
        }
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void drawHeart(GuiGraphics graphics, ResourceLocation texture, int x, int y) {
        graphics.blit(texture, x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
    }
}
