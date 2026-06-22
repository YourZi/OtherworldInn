package com.otherworldinn.world.commission;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;

final class FishingCommissionGenerator {
    static final String TEMPLATE_ID = "fishing_request";
    private static final String DESCRIPTION_KEY =
            "commission.otherworldinn.description." + TEMPLATE_ID;
    private static final String FISHERMAN_ENTITY_ID = "otherworldinn:fisherman";
    private static final TagKey<Item> RAW_FISHES =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "raw_fishes"));

    private FishingCommissionGenerator() {}

    static CommissionEntry generate(ServerLevel level, RandomSource random, long day, int slot) {
        List<FishingTarget> targets = loadTargets(level.getServer().getResourceManager());
        if (targets.isEmpty()) {
            return null;
        }

        FishingTarget target = targets.get(random.nextInt(targets.size()));
        RewardProfile profile = RewardProfile.forRarity(target.rarity());
        return new CommissionEntry(
                "commission_" + TEMPLATE_ID + "_" + day + "_" + slot,
                DESCRIPTION_KEY,
                profile.stars(),
                profile.durationDays(),
                List.of(new CommissionEntry.ItemRequirement(target.itemId().toString(), 1)),
                List.of(),
                List.of(),
                profile.coinReward(),
                List.of(new CommissionEntry.NpcFavorReward(FISHERMAN_ENTITY_ID, profile.favorReward())));
    }

    private static List<FishingTarget> loadTargets(ResourceManager resourceManager) {
        Map<ResourceLocation, FishingTarget> targets = new LinkedHashMap<>();
        Map<ResourceLocation, Resource> resources =
                resourceManager.listResources(
                        "starcatcher/fish", location -> location.getPath().endsWith(".json"));

        resources.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> addTarget(entry.getValue(), targets));

        return new ArrayList<>(targets.values());
    }

    private static void addTarget(Resource resource, Map<ResourceLocation, FishingTarget> targets) {
        try (Reader reader = resource.openAsReader()) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (!root.has("rarity") || !root.has("catch_info")) {
                return;
            }

            FishingRarity rarity = FishingRarity.fromString(root.get("rarity").getAsString());
            if (rarity == null) {
                return;
            }

            JsonObject catchInfo = root.getAsJsonObject("catch_info");
            if (catchInfo == null || !catchInfo.has("item")) {
                return;
            }

            ResourceLocation itemId = ResourceLocation.tryParse(catchInfo.get("item").getAsString());
            if (itemId == null) {
                return;
            }

            Item item = BuiltInRegistries.ITEM.get(itemId);
            if (item == null || !item.builtInRegistryHolder().is(RAW_FISHES)) {
                return;
            }

            targets.putIfAbsent(itemId, new FishingTarget(itemId, rarity));
        } catch (Exception ignored) {
        }
    }

    private record FishingTarget(ResourceLocation itemId, FishingRarity rarity) {}

    private enum FishingRarity {
        COMMON,
        UNCOMMON,
        RARE,
        EPIC,
        LEGENDARY;

        private static FishingRarity fromString(String value) {
            if (value == null || value.isBlank()) {
                return null;
            }
            return switch (value.toLowerCase(Locale.ROOT)) {
                case "common" -> COMMON;
                case "uncommon" -> UNCOMMON;
                case "rare" -> RARE;
                case "epic" -> EPIC;
                case "legendary" -> LEGENDARY;
                default -> null;
            };
        }
    }

    private record RewardProfile(int stars, long durationDays, int coinReward, int favorReward) {
        private static RewardProfile forRarity(FishingRarity rarity) {
            return switch (rarity) {
                case COMMON -> new RewardProfile(1, 2L, 45, 50);
                case UNCOMMON -> new RewardProfile(2, 3L, 70, 70);
                case RARE -> new RewardProfile(3, 4L, 100, 95);
                case EPIC -> new RewardProfile(4, 5L, 150, 130);
                case LEGENDARY -> new RewardProfile(5, 6L, 220, 180);
            };
        }
    }
}
