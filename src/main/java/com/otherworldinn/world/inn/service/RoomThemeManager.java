package com.otherworldinn.world.inn.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RoomThemeManager {

    public enum ThemePart {
        SHELL,
        INTERIOR,
        BOTH
    }

    public record ThemeMembership(RoomTheme theme, ThemePart part) {}

    public record BlockMatcher(
            @Nullable String tagKeyword,
            @Nullable String idKeyword,
            @Nullable String exactId) {

        public static BlockMatcher byTag(String tagKeyword) {
            return new BlockMatcher(tagKeyword, null, null);
        }

        public static BlockMatcher byIdKeyword(String idKeyword) {
            return new BlockMatcher(null, idKeyword, null);
        }

        public static BlockMatcher byExactId(String exactId) {
            return new BlockMatcher(null, null, exactId);
        }

        public boolean matches(Block block) {
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
            if (blockId == null) {
                return false;
            }

            if (tagKeyword != null && !tagKeyword.isEmpty()) {
                for (TagKey<Block> tag : BuiltInRegistries.BLOCK.getTagNames().toList()) {
                    if (tag.location().getPath().contains(tagKeyword)
                            && block.defaultBlockState().is(tag)) {
                        return true;
                    }
                }
            }

            if (idKeyword != null && !idKeyword.isEmpty()) {
                if (blockId.getPath().contains(idKeyword)) {
                    return true;
                }
            }

            if (exactId != null && !exactId.isEmpty()) {
                if (blockId.equals(ResourceLocation.parse(exactId))) {
                    return true;
                }
            }

            return false;
        }
    }

    public record StatModifiers(int comfort, int light, int humidity) {
        public static final StatModifiers NONE = new StatModifiers(0, 0, 0);
    }

    /** 主题定义：外壳组与内部组均按"组内 OR、组间 AND"判定，两者需同时满足；statModifiers 为属性加成。 */
    public record RoomTheme(
            String id,
            String zhName,
            String enName,
            List<List<BlockMatcher>> shellGroups,
            List<List<BlockMatcher>> interiorGroups,
            StatModifiers statModifiers) {}

    private static final Map<String, RoomTheme> THEMES = new LinkedHashMap<>();

    static {
        initDefaultThemes();
    }

    private static void initDefaultThemes() {
        // ── 末地主题 ──
        List<List<BlockMatcher>> endShell = List.of(List.of(
                BlockMatcher.byIdKeyword("end_stone"),
                BlockMatcher.byIdKeyword("purpur")));
        // 内部组: 有末地烛/龙头/潜影盒
        List<List<BlockMatcher>> endInterior = List.of(List.of(
                BlockMatcher.byExactId("minecraft:end_rod"),
                BlockMatcher.byExactId("minecraft:dragon_head"),
                BlockMatcher.byIdKeyword("shulker_box")));

        register(new RoomTheme("end", "末地", "End", endShell, endInterior, new StatModifiers(10, 0, 0)));

        // ── 下界主题 ──
        List<List<BlockMatcher>> netherShell = List.of(List.of(
                BlockMatcher.byExactId("minecraft:netherrack"),
                BlockMatcher.byExactId("minecraft:obsidian"),
                BlockMatcher.byExactId("minecraft:crying_obsidian"),
                BlockMatcher.byExactId("minecraft:soul_sand"),
                BlockMatcher.byExactId("minecraft:soul_soil"),
                BlockMatcher.byIdKeyword("nether_brick"),
                BlockMatcher.byIdKeyword("blackstone"),
                BlockMatcher.byIdKeyword("basalt")));
        List<List<BlockMatcher>> netherInterior = List.of(List.of(
                BlockMatcher.byExactId("minecraft:soul_torch"),
                BlockMatcher.byExactId("minecraft:soul_lantern"),
                BlockMatcher.byExactId("minecraft:soul_wall_torch")));

        register(new RoomTheme("nether", "下界", "Nether", netherShell, netherInterior, new StatModifiers(0, 10, -20)));

        // ── 海洋主题 ──
        List<List<BlockMatcher>> oceanShell = List.of(List.of(
                BlockMatcher.byIdKeyword("prismarine")));
        List<List<BlockMatcher>> oceanInterior = List.of(List.of(
                BlockMatcher.byExactId("minecraft:sea_lantern"),
                BlockMatcher.byExactId("minecraft:conduit"),
                BlockMatcher.byIdKeyword("coral")));

        register(new RoomTheme("ocean", "海洋", "Ocean", oceanShell, oceanInterior, new StatModifiers(0, 0, 15)));
    }

    public static void register(RoomTheme theme) {
        THEMES.put(theme.id(), theme);
    }

    @Nullable
    public static ThemeMembership findThemeMembership(Block block) {
        if (block == null) {
            return null;
        }
        for (RoomTheme theme : THEMES.values()) {
            boolean shellMatch = anyMatcherMatches(theme.shellGroups(), block);
            boolean interiorMatch = anyMatcherMatches(theme.interiorGroups(), block);
            if (!shellMatch && !interiorMatch) {
                continue;
            }
            ThemePart part =
                    shellMatch && interiorMatch
                            ? ThemePart.BOTH
                            : (shellMatch ? ThemePart.SHELL : ThemePart.INTERIOR);
            return new ThemeMembership(theme, part);
        }
        return null;
    }

    @Nullable
    public static String detectTheme(ServerLevel level, BlockPos minPos, BlockPos maxPos) {
        int minX = minPos.getX();
        int minY = minPos.getY();
        int minZ = minPos.getZ();
        int maxX = maxPos.getX();
        int maxY = maxPos.getY();
        int maxZ = maxPos.getZ();

        List<RoomTheme> themes = List.copyOf(THEMES.values());
        int n = themes.size();
        int[] shellTotal = new int[n];
        int[] shellMatch = new int[n];
        boolean[][] interiorDoneArr = new boolean[n][];
        for (int i = 0; i < n; i++) {
            interiorDoneArr[i] = new boolean[themes.get(i).interiorGroups().size()];
        }

        for (BlockPos pos : BlockPos.betweenClosed(
                minX - 1, minY - 1, minZ - 1,
                maxX + 1, maxY + 1, maxZ + 1)) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            boolean isShell = x < minX || x > maxX || y < minY || y > maxY || z < minZ || z > maxZ;

            Block block = state.getBlock();

            for (int i = 0; i < n; i++) {
                RoomTheme theme = themes.get(i);
                if (isShell) {
                    if (theme.shellGroups().isEmpty()) {
                        continue;
                    }
                    shellTotal[i]++;
                    if (anyMatcherMatches(theme.shellGroups(), block)) {
                        shellMatch[i]++;
                    }
                } else {
                    if (theme.interiorGroups().isEmpty()) {
                        continue;
                    }
                    tryMatchGroups(interiorDoneArr[i], theme.interiorGroups(), block);
                }
            }
        }

        // 判定：外壳占比 ≥ 60% 且所有内部需求组满足
        for (int i = 0; i < n; i++) {
            RoomTheme theme = themes.get(i);
            boolean shellOk = theme.shellGroups().isEmpty()
                    || (shellTotal[i] > 0 && (double) shellMatch[i] / shellTotal[i] >= 0.6);
            boolean interiorOk = theme.interiorGroups().isEmpty()
                    || allGroupsDone(interiorDoneArr[i]);
            if (shellOk && interiorOk) {
                return theme.id();
            }
        }

        return null;
    }

    private static boolean anyMatcherMatches(List<List<BlockMatcher>> groups, Block block) {
        for (List<BlockMatcher> group : groups) {
            for (BlockMatcher m : group) {
                if (m.matches(block)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean tryMatchGroups(
            boolean[] groupDone, List<List<BlockMatcher>> groups, Block block) {
        for (int g = 0; g < groups.size(); g++) {
            if (groupDone[g]) {
                continue;
            }
            for (BlockMatcher matcher : groups.get(g)) {
                if (matcher.matches(block)) {
                    groupDone[g] = true;
                    break;
                }
            }
        }
        return false;
    }

    private static boolean allGroupsDone(boolean[] groupDone) {
        for (boolean done : groupDone) {
            if (!done) {
                return false;
            }
        }
        return true;
    }

    public static RoomTheme getTheme(String id) {
        return THEMES.get(id);
    }

    public static String getDisplayName(String id, boolean zh) {
        RoomTheme theme = THEMES.get(id);
        if (theme == null) {
            return id;
        }
        return zh ? theme.zhName() : theme.enName();
    }

    public static StatModifiers getThemeModifiers(String id) {
        RoomTheme theme = THEMES.get(id);
        if (theme == null) {
            return StatModifiers.NONE;
        }
        return theme.statModifiers();
    }
}
