package com.otherworldinn.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.otherworldinn.OtherworldInn;
import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.init.ModItems;
import com.otherworldinn.item.LandDeedItem;
import com.otherworldinn.item.RoomKeyItem;
import com.otherworldinn.init.ModKeyBindings;
import com.otherworldinn.world.inn.InnData;
import com.otherworldinn.world.inn.RoomData;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import com.simibubi.create.AllSpecialTextures;
import java.util.List;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

/**
 * 房间轮廓渲染器
 *
 * <p>使用 Create 模组的 Outliner API 渲染房间边框和预览区域。 仅在客户端运行。
 */
@EventBusSubscriber(modid = OtherworldInn.MODID, value = Dist.CLIENT)
public class RoomOutlineRenderer {

    private static final Object PREVIEW_SLOT = "room_preview";
    private static int forcedRoomOutlineTicks = 0;
    private static boolean overlayToggled = false;
    private static boolean overlayTogglePressed = false;

    public static void activateTimedRoomOutline(int ticks) {
        if (ticks > 0) {
            forcedRoomOutlineTicks = Math.max(forcedRoomOutlineTicks, ticks);
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        if (forcedRoomOutlineTicks <= 0) {
            return;
        }

        TeamData team = TeamManager.getInstance().getClientPlayerTeam();
        if (team == null || team.getInnData().getRooms().isEmpty()) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        Vec3 cameraPos = event.getCamera().getPosition();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        Tesselator tesselator = Tesselator.getInstance();
        Matrix4f matrix = poseStack.last().pose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.lineWidth(8.0F);

        BufferBuilder lineBuffer =
                tesselator.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        for (RoomData room : team.getInnData().getRooms().values()) {
            drawAABBOutline(lineBuffer, matrix, room);
        }
        BufferUploader.drawWithShader(lineBuffer.buildOrThrow());

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        // 检测按键绑定切换重叠层显示
        boolean pressed = ModKeyBindings.TOGGLE_INN_OVERLAY.isDown();
        if (pressed && !overlayTogglePressed) {
            overlayToggled = !overlayToggled;
            player.displayClientMessage(
                    Component.translatable(
                            "message.otherworldinn.inn_overlay.toggled",
                            Component.translatable(
                                    overlayToggled
                                            ? "message.otherworldinn.inn_overlay.on"
                                            : "message.otherworldinn.inn_overlay.off")
                                    .setStyle(Style.EMPTY.withColor(
                                            overlayToggled ? 0x55FF55 : 0xFF5555))),
                    true);
        }
        overlayTogglePressed = pressed;

        // 检查是否手持房间登记册或地契
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        boolean holdingRegistry = stack.is(ModItems.ROOM_REGISTER.get());
        boolean holdingLandDeed = stack.is(ModItems.LAND_DEED.get());
        boolean holdingRoomKey = stack.is(ModItems.ROOM_KEY.get());
        boolean forceRenderRooms = forcedRoomOutlineTicks > 0;

        if (!holdingRegistry && !holdingLandDeed && !holdingRoomKey) {
            stack = player.getItemInHand(InteractionHand.OFF_HAND);
            holdingRegistry = stack.is(ModItems.ROOM_REGISTER.get());
            holdingLandDeed = stack.is(ModItems.LAND_DEED.get());
            holdingRoomKey = stack.is(ModItems.ROOM_KEY.get());
        }

        if (!holdingRegistry && !holdingLandDeed && !holdingRoomKey && !forceRenderRooms && !overlayToggled) {
            return;
        }

        // 1. 渲染已有房间 (钢蓝色) - 仅当手持房间登记册时
        TeamData team = TeamManager.getInstance().getClientPlayerTeam();
        if (team != null) {
            InnData innData = team.getInnData();

            if (holdingRegistry) {
                for (RoomData room : innData.getRooms().values()) {
                    AABB box =
                            new AABB(
                                    room.getMinPos().getX(),
                                    room.getMinPos().getY(),
                                    room.getMinPos().getZ(),
                                    room.getMaxPos().getX() + 1.0,
                                    room.getMaxPos().getY() + 1.0,
                                    room.getMaxPos().getZ() + 1.0);
                    Outliner.getInstance()
                            .showAABB(room.getId(), box)
                            .colored(ModColors.BLUE)
                            .lineWidth(1 / 16f)
                            .withFaceTextures(
                                    AllSpecialTextures.CUTOUT_CHECKERED,
                                    AllSpecialTextures.CUTOUT_CHECKERED);
                }
            } else if (holdingRoomKey) {
                // 手持房间钥匙：渲染所有房间
                // 绑定的房间：蓝色 (ModColors.BLUE)
                // 其他房间：灰色 (ModColors.GRAY_DARK)
                java.util.Optional<java.util.UUID> boundRoomUUID =
                        RoomKeyItem.getBoundRoomUUID(stack);

                for (RoomData room : innData.getRooms().values()) {
                    boolean isBound =
                            boundRoomUUID.isPresent() && boundRoomUUID.get().equals(room.getUuid());
                    int color = isBound ? ModColors.BLUE : ModColors.GRAY_DARK;

                    AABB box =
                            new AABB(
                                    room.getMinPos().getX(),
                                    room.getMinPos().getY(),
                                    room.getMinPos().getZ(),
                                    room.getMaxPos().getX() + 1.0,
                                    room.getMaxPos().getY() + 1.0,
                                    room.getMaxPos().getZ() + 1.0);
                    Outliner.getInstance()
                            .showAABB(room.getId(), box)
                            .colored(color)
                            .lineWidth(1 / 16f)
                            .withFaceTextures(
                                    AllSpecialTextures.CUTOUT_CHECKERED,
                                    AllSpecialTextures.CUTOUT_CHECKERED);
                }
            }

            // 3. 渲染旅社范围 (青色) - 手持房间登记册或地契时都显示
            if (holdingLandDeed || holdingRegistry) {
                int minBuildHeight = mc.level.getMinBuildHeight();
                int maxBuildHeight = mc.level.getMaxBuildHeight();

                List<TeamData.InnRegion> regions = team.getInnRegions();
                for (int i = 0; i < regions.size(); i++) {
                    TeamData.InnRegion region = regions.get(i);
                    AABB innBox =
                            new AABB(
                                    region.minX(),
                                    minBuildHeight,
                                    region.minZ(),
                                    region.maxX() + 1.0,
                                    maxBuildHeight,
                                    region.maxZ() + 1.0);

                    Outliner.getInstance()
                            .showAABB("inn_region_" + i, innBox)
                            .colored(ModColors.BLUE)
                            .lineWidth(1 / 16f)
                            .withFaceTextures(
                                    AllSpecialTextures.CUTOUT_CHECKERED,
                                    AllSpecialTextures.CUTOUT_CHECKERED);
                }
            }

            // 4. 按键切换重叠层：显示所有房间 + 旅社范围
            if (overlayToggled && !holdingRegistry && !holdingRoomKey && !holdingLandDeed) {
                for (RoomData room : innData.getRooms().values()) {
                    AABB box =
                            new AABB(
                                    room.getMinPos().getX(),
                                    room.getMinPos().getY(),
                                    room.getMinPos().getZ(),
                                    room.getMaxPos().getX() + 1.0,
                                    room.getMaxPos().getY() + 1.0,
                                    room.getMaxPos().getZ() + 1.0);
                    Outliner.getInstance()
                            .showAABB(room.getId(), box)
                            .colored(ModColors.BLUE)
                            .lineWidth(1 / 16f)
                            .withFaceTextures(
                                    AllSpecialTextures.CUTOUT_CHECKERED,
                                    AllSpecialTextures.CUTOUT_CHECKERED);
                }

                int minBuildHeight = mc.level.getMinBuildHeight();
                int maxBuildHeight = mc.level.getMaxBuildHeight();
                List<TeamData.InnRegion> regions = team.getInnRegions();
                for (int i = 0; i < regions.size(); i++) {
                    TeamData.InnRegion region = regions.get(i);
                    AABB innBox =
                            new AABB(
                                    region.minX(),
                                    minBuildHeight,
                                    region.minZ(),
                                    region.maxX() + 1.0,
                                    maxBuildHeight,
                                    region.maxZ() + 1.0);
                    Outliner.getInstance()
                            .showAABB("inn_overlay_region_" + i, innBox)
                            .colored(ModColors.GREEN)
                            .lineWidth(1 / 16f)
                            .withFaceTextures(
                                    AllSpecialTextures.CUTOUT_CHECKERED,
                                    AllSpecialTextures.CUTOUT_CHECKERED);
                }

                // 最大可建造地皮范围 (橙色)
                AABB maxBounds =
                        new AABB(
                                LandDeedItem.MAX_REGION_MIN_X,
                                minBuildHeight,
                                LandDeedItem.MAX_REGION_MIN_Z,
                                LandDeedItem.MAX_REGION_MAX_X + 1.0,
                                maxBuildHeight,
                                LandDeedItem.MAX_REGION_MAX_Z + 1.0);
                Outliner.getInstance()
                        .showAABB("inn_max_bounds", maxBounds)
                        .colored(ModColors.YELLOW)
                        .lineWidth(1 / 16f)
                        .withFaceTextures(
                                AllSpecialTextures.CUTOUT_CHECKERED,
                                AllSpecialTextures.CUTOUT_CHECKERED);
            }
        }

        if (forcedRoomOutlineTicks > 0) {
            forcedRoomOutlineTicks--;
        }

        // 2. 渲染预览区域 (黄绿色/地契颜色) - 仅在手持相关物品时
        if (!holdingLandDeed && !holdingRegistry) {
            return;
        }
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        if (holdingLandDeed) {
            // 地契渲染逻辑 (Pos1 已定, Pos2 待定/已定)
            if (tag.contains("Pos1")) {
                BlockPos pos1 = BlockPos.of(tag.getLong("Pos1"));
                BlockPos pos2;

                if (tag.contains("Pos2")) {
                    // Pos2 已定，显示确认预览
                    pos2 = BlockPos.of(tag.getLong("Pos2"));
                } else {
                    // Pos2 未定，跟随光标
                    HitResult hitResult = mc.hitResult;
                    if (hitResult instanceof BlockHitResult blockHitResult) {
                        pos2 = blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
                    } else {
                        return; // 未指向方块时不渲染
                    }
                }

                int minBuildHeight = mc.level.getMinBuildHeight();
                int maxBuildHeight = mc.level.getMaxBuildHeight();

                BlockPos minPos =
                        new BlockPos(
                                Math.min(pos1.getX(), pos2.getX()),
                                minBuildHeight,
                                Math.min(pos1.getZ(), pos2.getZ()));
                BlockPos maxPos =
                        new BlockPos(
                                Math.max(pos1.getX(), pos2.getX()),
                                maxBuildHeight,
                                Math.max(pos1.getZ(), pos2.getZ()));

                AABB previewBox =
                        new AABB(
                                minPos.getX(),
                                minPos.getY(),
                                minPos.getZ(),
                                maxPos.getX() + 1.0,
                                maxPos.getY() + 1.0,
                                maxPos.getZ() + 1.0);

                int color;
                if (!LandDeedItem.isWithinBounds(pos1, pos2)) {
                    color = ModColors.RED;
                } else if (!LandDeedItem.isWithinRatingAreaLimit(team, pos1, pos2)) {
                    color = ModColors.RED;
                } else {
                    int price = LandDeedItem.calculatePrice(team, pos1, pos2);
                    int coins = team != null ? team.getCoins() : 0;
                    color = (coins >= price) ? ModColors.YELLOW : ModColors.RED;
                }

                Outliner.getInstance()
                        .showAABB(PREVIEW_SLOT, previewBox)
                        .colored(color)
                        .lineWidth(1 / 16f)
                        .withFaceTextures(
                                AllSpecialTextures.CUTOUT_CHECKERED,
                                AllSpecialTextures.CUTOUT_CHECKERED);
            }
        } else if (tag.contains("Pos1")) {
            // 房间登记册渲染逻辑
            BlockPos pos1 = BlockPos.of(tag.getLong("Pos1"));

            HitResult hitResult = mc.hitResult;
            if (hitResult instanceof BlockHitResult blockHitResult) {
                // 确定 Pos2 (当前所指方块的相邻面)
                BlockPos pos2 =
                        blockHitResult.getBlockPos().relative(blockHitResult.getDirection());

                BlockPos minPos =
                        new BlockPos(
                                Math.min(pos1.getX(), pos2.getX()),
                                Math.min(pos1.getY(), pos2.getY()),
                                Math.min(pos1.getZ(), pos2.getZ()));
                BlockPos maxPos =
                        new BlockPos(
                                Math.max(pos1.getX(), pos2.getX()),
                                Math.max(pos1.getY(), pos2.getY()),
                                Math.max(pos1.getZ(), pos2.getZ()));

                AABB previewBox =
                        new AABB(
                                minPos.getX(),
                                minPos.getY(),
                                minPos.getZ(),
                                maxPos.getX() + 1.0,
                                maxPos.getY() + 1.0,
                                maxPos.getZ() + 1.0);

                Outliner.getInstance()
                        .showAABB(PREVIEW_SLOT, previewBox)
                        .colored(ModColors.GREEN)
                        .lineWidth(1 / 16f)
                        .withFaceTextures(
                                AllSpecialTextures.CUTOUT_CHECKERED,
                                AllSpecialTextures.CUTOUT_CHECKERED);
            }
        }
    }

    private static void drawAABBOutline(BufferBuilder buffer, Matrix4f matrix, RoomData room) {
        float minX = room.getMinPos().getX();
        float minY = room.getMinPos().getY();
        float minZ = room.getMinPos().getZ();
        float maxX = room.getMaxPos().getX() + 1.0F;
        float maxY = room.getMaxPos().getY() + 1.0F;
        float maxZ = room.getMaxPos().getZ() + 1.0F;
        float r = 0.592F;
        float g = 0.863F;
        float b = 1.0F;
        float a = 1.0F;

        line(buffer, matrix, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
        line(buffer, matrix, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);
        line(buffer, matrix, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
        line(buffer, matrix, minX, minY, maxZ, minX, minY, minZ, r, g, b, a);

        line(buffer, matrix, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a);
        line(buffer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a);
        line(buffer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
        line(buffer, matrix, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a);

        line(buffer, matrix, minX, minY, minZ, minX, maxY, minZ, r, g, b, a);
        line(buffer, matrix, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a);
        line(buffer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a);
        line(buffer, matrix, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a);
    }

    private static void line(
            BufferBuilder buffer,
            Matrix4f matrix,
            float x1,
            float y1,
            float z1,
            float x2,
            float y2,
            float z2,
            float r,
            float g,
            float b,
            float a) {
        buffer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a);
        buffer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a);
    }
}
