package com.otherworldinn.world.inn.service;

import com.otherworldinn.util.BlockEntitySearchUtils;
import com.otherworldinn.world.inn.InnTodo;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.equipment.clipboard.ClipboardBlockEntity;
import com.simibubi.create.content.equipment.clipboard.ClipboardContent;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides.ClipboardType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

public class ClipboardManager {

    /** 在范围内所有墙面剪贴板添加一条待办事项，返回是否至少修改了一个剪贴板。 */
    public static boolean addTodo(Level level, AABB area, String text) {
        return addTodo(level, area, Component.literal(text));
    }

    public static boolean addTodo(Level level, AABB area, Component text) {
        return modifyClipboards(
                level,
                area,
                pages -> {
                    // 寻找第一个未满的页面（每页最多12条）
                    List<ClipboardEntry> targetPage = null;
                    for (List<ClipboardEntry> page : pages) {
                        if (page.size() < 12) {
                            targetPage = page;
                            break;
                        }
                    }

                    if (targetPage == null) {
                        targetPage = new ArrayList<>();
                        pages.add(targetPage);
                    }

                    targetPage.add(new ClipboardEntry(false, text.copy()));
                });
    }

    /** 在指定范围内删除所有匹配文本的待办事项 */
    public static void removeTodo(Level level, AABB area, String text) {
        removeTodo(level, area, InnTodo.legacy(text));
    }

    public static void removeTodo(Level level, AABB area, InnTodo todo) {
        modifyClipboards(
                level,
                area,
                pages -> {
                    for (List<ClipboardEntry> page : pages) {
                        page.removeIf(entry -> matches(entry, todo));
                    }
                    pages.removeIf(List::isEmpty);
                });
    }

    public static void setTodoStatus(Level level, AABB area, String text, boolean checked) {
        modifyClipboards(
                level,
                area,
                pages -> {
                    for (List<ClipboardEntry> page : pages) {
                        for (ClipboardEntry entry : page) {
                            if (entry.text.getString().equals(text)) {
                                entry.checked = checked;
                            }
                        }
                    }
                });
    }

    private static boolean matches(ClipboardEntry entry, InnTodo todo) {
        return entry.text.equals(todo.component()) || todo.matchesText(entry.text.getString());
    }

    /** 清空范围内所有剪贴板的内容 */
    public static void clear(Level level, AABB area) {
        modifyClipboards(level, area, List::clear);
    }

    /** 核心修改方法 */
    public static boolean modifyClipboards(
            Level level, AABB area, Consumer<List<List<ClipboardEntry>>> modifier) {
        if (level.isClientSide) return false;

        boolean[] modified = new boolean[] {false};
        BlockPos min = BlockPos.containing(area.minX, area.minY, area.minZ);
        BlockPos max = BlockPos.containing(area.maxX, area.maxY, area.maxZ);

        int minChunkX = min.getX() >> 4;
        int maxChunkX = max.getX() >> 4;
        int minChunkZ = min.getZ() >> 4;
        int maxChunkZ = max.getZ() >> 4;

        BlockEntitySearchUtils.forEachInChunkRange(
                level,
                minChunkX,
                maxChunkX,
                minChunkZ,
                maxChunkZ,
                blockEntity -> {
                    BlockPos pos = blockEntity.getBlockPos();
                    if (!area.contains(pos.getX(), pos.getY(), pos.getZ())) {
                        return;
                    }
                    if (!(blockEntity instanceof ClipboardBlockEntity cbe)) {
                        return;
                    }

                    BlockState state = cbe.getBlockState();
                    if (!AllBlocks.CLIPBOARD.has(state)) {
                        return;
                    }
                    if (state.getValue(BlockStateProperties.ATTACH_FACE) != AttachFace.WALL) {
                        return;
                    }

                    ClipboardContent content =
                            cbe.components()
                                    .getOrDefault(
                                            AllDataComponents.CLIPBOARD_CONTENT,
                                            ClipboardContent.EMPTY);
                    List<List<ClipboardEntry>> pages = ClipboardEntry.readAll(content);
                    modifier.accept(pages);
                    modified[0] = true;

                    ClipboardContent newContent =
                            content.setPages(pages)
                                    .setType(
                                            pages.isEmpty()
                                                    ? ClipboardType.EMPTY
                                                    : ClipboardType.WRITTEN);
                    PatchedDataComponentMap map = new PatchedDataComponentMap(cbe.components());
                    map.set(AllDataComponents.CLIPBOARD_CONTENT, newContent);
                    cbe.setComponents(map);
                    cbe.notifyUpdate();
                    cbe.updateWrittenState();
                });
        return modified[0];
    }
}
