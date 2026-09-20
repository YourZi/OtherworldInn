package com.otherworldinn.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.otherworldinn.OtherworldInn;
import com.otherworldinn.item.MarshmallowOnStickItem;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/** 一串棉花糖的手持渲染：手部用五阶段 3D 模型，其余上下文回退 2D 图标。 */
public final class MarshmallowStickHandRenderer extends BlockEntityWithoutLevelRenderer {

    /** 手持阶段数，对应 {@link MarshmallowOnStickItem} 的五个烤制阶段。 */
    public static final int STAGE_COUNT = 5;

    /** 各阶段手持模型，索引即阶段，需经 RegisterAdditional 注册。 */
    public static final List<ModelResourceLocation> HAND_MODELS =
            IntStream.range(0, STAGE_COUNT)
                    .mapToObj(stage -> standalone("item/marshmallow_stick_hand_" + stage))
                    .toList();

    /** 非手持上下文的 2D 图标模型。 */
    public static final ModelResourceLocation ICON_MODEL = standalone("item/marshmallow_stick_icon");

    /** 烤制时前伸距离（格）。 */
    private static final float ROAST_EXTEND = 0.1F;
    /** 烤制时的倾斜角 */
    private static final float ROAST_TILT_DEGREES = -50.0F;
    /** 摇晃幅度（度）：Y 轴左右 / X 轴上下。 */
    private static final float ROAST_SWAY_YAW = 5.0F;
    private static final float ROAST_SWAY_PITCH = 3.0F;
    /** 摇晃周期 */
    private static final float ROAST_SWAY_PERIOD = 60.0F;
    /** 进度逼近比例，抬起/放下约 0.35 秒过渡。 */
    private static final float ROAST_SMOOTHING = 0.3F;
    /** 营火交互距离，与服务端判定一致。 */
    private static final double ROAST_RANGE = 2.0D;

    /** 举起进度 0-1：0 竖举，1 横伸。 */
    private static float roastProgress;

    /** 注册给物品的客户端扩展，渲染器延迟创建。 */
    public static final IClientItemExtensions EXTENSIONS =
            new IClientItemExtensions() {
                private BlockEntityWithoutLevelRenderer renderer;

                @Override
                public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                    if (renderer == null) {
                        renderer = new MarshmallowStickHandRenderer();
                    }
                    return renderer;
                }
            };

    private MarshmallowStickHandRenderer() {
        super(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext context,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay) {
        BakedModel model =
                Minecraft.getInstance()
                        .getModelManager()
                        .getModel(isHand(context) ? HAND_MODELS.get(MarshmallowOnStickItem.getHandStage(stack)) : ICON_MODEL);
        // 抵消外层 builtin/entity 的 (-0.5,-0.5,-0.5) 平移，再由原版流程应用目标模型的 display。
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        if (context.firstPerson()) {
            applyRoastAnimation(poseStack);
        }
        Minecraft.getInstance()
                .getItemRenderer()
                .render(stack, context, isLeftHand(context), poseStack, buffer, light, overlay, model);
        poseStack.popPose();
    }

    /** 第一人称烤制时前伸并缓慢摇晃，判定同服务端 {@code MarshmallowRoastHandler}。 */
    private static void applyRoastAnimation(PoseStack poseStack) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }
        // 按渲染帧推进进度，deltaTicks 为本帧流逝 tick 数。
        float deltaTicks = minecraft.getTimer().getGameTimeDeltaTicks();
        float target = isRoasting(minecraft, player) ? 1.0F : 0.0F;
        roastProgress += (target - roastProgress) * Mth.clamp(ROAST_SMOOTHING * deltaTicks, 0.0F, 1.0F);
        if (roastProgress < 0.01F) {
            roastProgress = 0.0F;
            return;
        }
        // 连续时间（tick + 帧内插值），避免 20 Hz 跳变。
        float time = player.tickCount + minecraft.getTimer().getGameTimeDeltaPartialTick(false);
        float phase = time / ROAST_SWAY_PERIOD * (float) (Math.PI * 2.0);
        float amount = roastProgress;
        // 前伸，手部 -Z 为前方。
        poseStack.translate(0.0F, 0.0F, -ROAST_EXTEND * amount);
        // 竖举转横伸（绕 X 轴，+Y 转向 -Z）。
        poseStack.mulPose(Axis.XP.rotationDegrees(ROAST_TILT_DEGREES * amount));
        poseStack.mulPose(Axis.YP.rotationDegrees(ROAST_SWAY_YAW * amount * Mth.sin(phase)));
        poseStack.mulPose(Axis.XP.rotationDegrees(ROAST_SWAY_PITCH * amount * Mth.cos(phase)));
    }

    /** 按住右键且准星对准点燃的营火。 */
    private static boolean isRoasting(Minecraft minecraft, LocalPlayer player) {
        if (!minecraft.options.keyUse.isDown()) {
            return false;
        }
        HitResult hit = player.pick(ROAST_RANGE, 1.0F, false);
        if (!(hit instanceof BlockHitResult blockHit)) {
            return false;
        }
        BlockState state = player.level().getBlockState(blockHit.getBlockPos());
        return state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT);
    }

    private static boolean isHand(ItemDisplayContext context) {
        return context.firstPerson()
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    private static boolean isLeftHand(ItemDisplayContext context) {
        return context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
    }

    private static ModelResourceLocation standalone(String path) {
        return ModelResourceLocation.standalone(
                ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, path));
    }
}
