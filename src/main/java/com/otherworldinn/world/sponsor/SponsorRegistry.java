package com.otherworldinn.world.sponsor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

public final class SponsorRegistry {
    private static final List<SponsorDefinition> SPONSORS =
            List.of(
                    SponsorDefinition.of("MOQing"),
                    SponsorDefinition.of("YeLuoYueShang"),
                    SponsorDefinition.of("Ailisi415"),
                    SponsorDefinition.of("PCLuige"),
                    SponsorDefinition.of("SPDish"),
                    SponsorDefinition.of("KnightKite"),
                    SponsorDefinition.of("YOYIMIYASAMA"),
                    SponsorDefinition.of("Yiyi"),
                    SponsorDefinition.of("Mangomineralspr"),
                    SponsorDefinition.of("AngryB"),
                    SponsorDefinition.of("Xiwanzi"),
                    SponsorDefinition.of("Hkszk"),
                    SponsorDefinition.of("QvQi"),
                    SponsorDefinition.of("GimmickyHare220"),
                    SponsorDefinition.of("YoungAubthur"),
                    SponsorDefinition.of("Kevinoer"),
                    SponsorDefinition.of("AoRan"),
                    SponsorDefinition.of("Lazypeople"),
                    SponsorDefinition.of("RIHE122"),
                    SponsorDefinition.of("NekotuanQAQ"),
                    SponsorDefinition.of("HuLi_Tn"),
                    SponsorDefinition.of("LinLei_Baruch"),
                    SponsorDefinition.of("DanxiaoHanser"),
                    SponsorDefinition.of("TheFlareStar"),
                    SponsorDefinition.of("Biantwin"),
                    SponsorDefinition.of("Striveturtle"),
                    SponsorDefinition.of("XHeYa_3u3"),
                    SponsorDefinition.of("Wuyu_OWO"),
                    SponsorDefinition.of("chihuo_QWQ"),
                    SponsorDefinition.of("B_eibao"),
                    SponsorDefinition.of("Mr_eyes_5"),
                    SponsorDefinition.of("JustKayina"),
                    SponsorDefinition.of("IsoiaYUME"),
                    SponsorDefinition.of("Cadivy"),
                    SponsorDefinition.of("Yumicuibb"),
                    SponsorDefinition.of("KeyxelDesu"),
                    SponsorDefinition.of("Caoning"),
                    SponsorDefinition.of("NomeSun"),
                    SponsorDefinition.of("xiao_zhan"),
                    SponsorDefinition.of("Shuo_Mo"),
                    SponsorDefinition.of("yszx_"),
                    SponsorDefinition.of("zzniania"),
                    SponsorDefinition.of("Ms_Springfield"),
                    SponsorDefinition.of("YAKUMODESU"),
                    SponsorDefinition.of("cabll"),
                    SponsorDefinition.of("Cillian_master"),
                    SponsorDefinition.of("Crowtyard"),
                    SponsorDefinition.of("sadgwd"),
                    SponsorDefinition.of("Cillian_master"),
                    SponsorDefinition.of("lao__xong")
                );

    private SponsorRegistry() {}

    public static List<SponsorDefinition> all() {
        return SPONSORS;
    }

    public static SponsorDefinition pickRandom(RandomSource random, @Nullable ServerLevel level) {
        if (level == null) {
            return pickUniformly(random, SPONSORS);
        }

        Set<String> onlineNames = new HashSet<>();
        for (ServerPlayer player : level.players()) {
            String name = player.getGameProfile().getName();
            if (name != null && !name.isBlank()) {
                onlineNames.add(name.toLowerCase(Locale.ROOT));
            }
        }
        if (onlineNames.isEmpty()) {
            return pickUniformly(random, SPONSORS);
        }

        List<SponsorDefinition> matched = new ArrayList<>();
        List<SponsorDefinition> unmatched = new ArrayList<>();
        for (SponsorDefinition sponsor : SPONSORS) {
            if (onlineNames.contains(sponsor.normalizedKey())) {
                matched.add(sponsor);
            } else {
                unmatched.add(sponsor);
            }
        }

        if (matched.isEmpty()) {
            return pickUniformly(random, SPONSORS);
        }
        if (unmatched.isEmpty()) {
            return pickUniformly(random, matched);
        }
        return random.nextFloat() < 0.5F ? pickUniformly(random, matched) : pickUniformly(random, unmatched);
    }

    public static SponsorDefinition resolveByName(String playerName) {
        String normalized = normalize(playerName);
        if (normalized == null) {
            return SponsorDefinition.of("");
        }
        for (SponsorDefinition sponsor : SPONSORS) {
            if (sponsor.normalizedKey().equals(normalized)) {
                return sponsor;
            }
        }
        return SponsorDefinition.of(playerName);
    }

    private static SponsorDefinition pickUniformly(RandomSource random, List<SponsorDefinition> sponsors) {
        return sponsors.get(random.nextInt(sponsors.size()));
    }

    @Nullable
    private static String normalize(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return null;
        }
        return playerName.trim().toLowerCase(Locale.ROOT);
    }
}
