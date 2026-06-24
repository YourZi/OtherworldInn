package com.otherworldinn.client.schematic;

import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.event.listener.TownProtectionHandler;
import com.simibubi.create.content.schematics.client.SchematicHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/** 客户端蓝图预览合法性缓存。 */
public final class SchematicPreviewProtectionHelper {
    public static final int VALID_OUTLINE_COLOR = 0x6886c5;
    public static final int INVALID_OUTLINE_COLOR = 0xff6b6b;

    private static net.minecraft.resources.ResourceKey<Level> cachedDimension;
    private static BlockPos cachedAnchor;
    private static Vec3i cachedSize;
    private static Rotation cachedRotation = Rotation.NONE;
    private static Mirror cachedMirror = Mirror.NONE;
    private static long cachedGameTime = Long.MIN_VALUE;
    private static boolean cachedInvalid;

    private SchematicPreviewProtectionHelper() {}

    public static int getOutlineColor(SchematicHandler handler, @Nullable BlockPos anchorOverride) {
        return isPreviewOutsideLegalRange(handler, anchorOverride)
                ? INVALID_OUTLINE_COLOR
                : VALID_OUTLINE_COLOR;
    }

    public static boolean isPreviewOutsideLegalRange(
            SchematicHandler handler, @Nullable BlockPos anchorOverride) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null
                || handler == null
                || !handler.isActive()
                || handler.getBounds() == null
                || handler.getTransformation() == null
                || level.dimension() != TownDimensions.TOWN_LEVEL) {
            return false;
        }

        AABB bounds = handler.getBounds();
        Vec3i size = new Vec3i(
                Math.max(0, (int) Math.round(bounds.getXsize())),
                Math.max(0, (int) Math.round(bounds.getYsize())),
                Math.max(0, (int) Math.round(bounds.getZsize())));
        if (size.getX() <= 0 || size.getY() <= 0 || size.getZ() <= 0) {
            return false;
        }

        StructurePlaceSettings settings = handler.getTransformation().toSettings();
        BlockPos anchor =
                anchorOverride != null ? anchorOverride : handler.getTransformation().getAnchor();
        Rotation rotation = settings.getRotation();
        Mirror mirror = settings.getMirror();
        long gameTime = level.getGameTime();
        if (level.dimension().equals(cachedDimension)
                && anchor.equals(cachedAnchor)
                && size.equals(cachedSize)
                && rotation == cachedRotation
                && mirror == cachedMirror
                && gameTime == cachedGameTime) {
            return cachedInvalid;
        }

        BlockPos extent = new BlockPos(size).offset(-1, -1, -1);
        BlockPos transformedExtent = StructureTemplate.calculateRelativePosition(settings, extent);
        int minX = Math.min(0, transformedExtent.getX());
        int minY = Math.min(0, transformedExtent.getY());
        int minZ = Math.min(0, transformedExtent.getZ());
        int maxX = Math.max(0, transformedExtent.getX());
        int maxY = Math.max(0, transformedExtent.getY());
        int maxZ = Math.max(0, transformedExtent.getZ());

        boolean invalid = false;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        outer:
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    cursor.set(anchor.getX() + x, anchor.getY() + y, anchor.getZ() + z);
                    if (!TownProtectionHandler.isInnRestrictionLiftedAt(level, cursor)) {
                        invalid = true;
                        break outer;
                    }
                }
            }
        }

        cachedDimension = level.dimension();
        cachedAnchor = anchor.immutable();
        cachedSize = size;
        cachedRotation = rotation;
        cachedMirror = mirror;
        cachedGameTime = gameTime;
        cachedInvalid = invalid;
        return invalid;
    }
}
