package com.otherworldinn.world.commission;

import com.otherworldinn.world.dialogue.LocalizedText;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public final class CommissionRegistry {
    private static final List<CommissionTemplate> TEMPLATES = new ArrayList<>();

    static {
        register("farm_supply")
                .stars(1, 2)
                .weight(12)
                .description(
                        LocalizedText.of(
                                "农场的粮食储备不够了，请带一点过来。",
                                "The farm is short on staple supplies. Please restock soon."))
                .submit("minecraft:wheat", 16)
                .submit("minecraft:bread", 8)
                .rewardCoins(40)
                .rewardFavor("otherworldinn:farmer", 60)
                .build();

        register("forge_maintenance")
                .stars(1, 2)
                .weight(10)
                .description(
                        LocalizedText.of(
                                "铁匠铺需要备用金属件，请先送一批材料过来。",
                                "The forge needs spare metal parts. Deliver a batch of materials."))
                .submit("minecraft:iron_ingot", 12)
                .submit("minecraft:gold_ingot", 4)
                .rewardCoins(40)
                .rewardFavor("otherworldinn:blacksmith", 90)
                .build();

        register("town_patrol_zombie")
                .stars(1, 3)
                .weight(14)
                .description(
                        LocalizedText.of(
                                "最近城镇外夜间不是很太平，请清理游荡的僵尸。",
                                "Nights beyond town are unsafe. Clear out wandering zombies."))
                .kill("minecraft:zombie", 10)
                .rewardCoins(80)
                .build();

        register("road_patrol_mixed")
                .stars(3, 3)
                .weight(9)
                .description(
                        LocalizedText.of(
                                "城镇的巡逻队请求支援：沿路清理骷髅并补给食物和工具。",
                                "Patrol requests support: clear skeletons and deliver field rations and tools."))
                .kill("minecraft:skeleton", 12)
                .submit("minecraft:bread", 12)
                .submit("minecraft:diamond_sword", 1)
                .rewardCoins(120)
                .build();

        register("magic_study_materials")
                .stars(2, 3)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "魔女需要一些稀有的魔法研究材料，帮忙收集一下。",
                                "The magician needs rare materials for magical studies. Help gather them."))
                .submit("minecraft:lapis_lazuli", 16)
                .submit("minecraft:ender_pearl", 4)
                .submit("minecraft:glowstone_dust", 8)
                .rewardCoins(70)
                .rewardFavor("otherworldinn:magician", 80)
                .build();

        register("farm_animal_feed")
                .stars(1, 2)
                .weight(10)
                .description(
                        LocalizedText.of(
                                "农场需要额外的动物饲料，带些小麦和胡萝卜过来。",
                                "The farm needs extra animal feed. Bring wheat and carrots."))
                .submit("minecraft:wheat", 24)
                .submit("minecraft:carrot", 16)
                .rewardCoins(45)
                .rewardFavor("otherworldinn:farmer", 70)
                .build();

        register("forge_tool_repair")
                .stars(2, 4)
                .weight(7)
                .description(
                        LocalizedText.of(
                                "城外有需要修理的设施，需要材料和帮手。",
                                "The forge has backlogged tool repairs. Needs materials and assistance."))
                .submit("minecraft:iron_ingot", 8)
                .submit("minecraft:stick", 16)
                .kill("minecraft:zombie", 5)
                .rewardCoins(95)
                .rewardFavor("otherworldinn:blacksmith", 85)
                .build();

        register("magic_potion_supply")
                .stars(3, 4)
                .weight(6)
                .description(
                        LocalizedText.of(
                                "魔女的药水库存不足了，需要一些基础药材补充。",
                                "The magician's potion supplies are low. Needs basic ingredients."))
                .submit("minecraft:nether_wart", 12)
                .submit("minecraft:spider_eye", 8)
                .submit("minecraft:ghast_tear", 2)
                .rewardCoins(110)
                .rewardFavor("otherworldinn:magician", 75)
                .build();

        register("forge_weapon_order")
                .stars(4, 5)
                .weight(5)
                .description(
                        LocalizedText.of(
                                "铁匠接到一批武器订单，需要高级金属材料。",
                                "The blacksmith received weapon orders. Needs premium metals."))
                .submit("minecraft:diamond", 3)
                .submit("minecraft:emerald", 6)
                .submit("minecraft:netherite_ingot", 1)
                .rewardCoins(280)
                .rewardFavor("otherworldinn:blacksmith", 95)
                .build();

        register("magic_ritual_prep")
                .stars(4, 5)
                .weight(4)
                .description(
                        LocalizedText.of(
                                "魔女有一个重要仪式要做，需要极其稀有的材料。",
                                "The magician prepares for an important ritual. Needs extremely rare materials."))
                .submit("minecraft:blaze_rod", 4)
                .submit("minecraft:shulker_shell", 2)
                .submit("minecraft:dragon_breath", 1)
                .rewardCoins(250)
                .rewardFavor("otherworldinn:magician", 100)
                .build();

        register("town_cleanup_spiders")
                .stars(2, 3)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "仓库里出现了蜘蛛，需要清理。",
                                "Spiders have infested the town warehouse. Needs cleaning and drying."))
                .kill("minecraft:spider", 8)
                .submit("minecraft:string", 12)
                .rewardCoins(65)
                .build();

        register("farm_seed_supply")
                .stars(1, 2)
                .weight(11)
                .description(
                        LocalizedText.of(
                                "农场需要各种作物种子进行轮作种植。",
                                "The farm needs various crop seeds for rotation planting."))
                .submit("minecraft:wheat_seeds", 20)
                .submit("minecraft:melon_seeds", 12)
                .submit("minecraft:pumpkin_seeds", 12)
                .rewardCoins(40)
                .rewardFavor("otherworldinn:farmer", 60)
                .build();

        register("forge_fuel_supply")
                .stars(1, 2)
                .weight(10)
                .description(
                        LocalizedText.of(
                                "铁匠铺的燃料快用完了，需要补充煤炭。",
                                "The forge is running low on fuel. Needs coal replenishment."))
                .submit("minecraft:coal", 32)
                .rewardCoins(50)
                .rewardFavor("otherworldinn:blacksmith", 55)
                .build();

        register("magic_library_organize")
                .stars(2, 3)
                .weight(7)
                .description(
                        LocalizedText.of(
                                "魔女的图书馆需要整理，顺便收集一些古籍。",
                                "The magician's library needs organizing. Also collect some ancient texts."))
                .submit("minecraft:book", 8)
                .submit("minecraft:paper", 24)
                .rewardCoins(85)
                .rewardFavor("otherworldinn:magician", 70)
                .build();

        register("forge_armor_order")
                .stars(3, 4)
                .weight(6)
                .description(
                        LocalizedText.of(
                                "城镇卫队需要一批新护甲，铁匠需要材料。",
                                "The town guard needs new armor. Blacksmith requires materials."))
                .submit("minecraft:iron_ingot", 16)
                .submit("minecraft:leather", 24)
                .submit("minecraft:gold_ingot", 8)
                .rewardCoins(130)
                .rewardFavor("otherworldinn:blacksmith", 80)
                .build();

        register("town_festival_prep")
                .stars(2, 3)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "城镇准备举办庆典，需要食物和装饰材料。",
                                "The town prepares for a festival. Needs food and decoration materials."))
                .submit("minecraft:cake", 3)
                .submit("minecraft:cookie", 24)
                .rewardCoins(90)
                .build();

        register("town_road_repair")
                .stars(1, 2)
                .weight(10)
                .description(
                        LocalizedText.of(
                                "城镇道路需要维修，收集些石材和木材。",
                                "Town roads need repair. Collect some stone and wood."))
                .submit("minecraft:cobblestone", 32)
                .submit("minecraft:oak_planks", 24)
                .rewardCoins(50)
                .build();

        register("farm_protection")
                .stars(2, 3)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "农场需要加强防护，清理周围的敌对生物。",
                                "The farm needs better protection. Clear surrounding hostile mobs."))
                .kill("minecraft:creeper", 4)
                .kill("minecraft:skeleton", 6)
                .kill("minecraft:zombie", 8)
                .rewardCoins(85)
                .rewardFavor("otherworldinn:farmer", 75)
                .build();

        register("forge_masterpiece")
                .stars(5, 5)
                .weight(3)
                .description(
                        LocalizedText.of(
                                "铁匠要打造一件传世之作，需要最顶级的材料。",
                                "The blacksmith aims to create a masterpiece. Needs the finest materials."))
                .submit("minecraft:netherite_ingot", 2)
                .submit("minecraft:diamond_block", 1)
                .submit("minecraft:emerald_block", 1)
                .rewardCoins(300)
                .rewardFavor("otherworldinn:blacksmith", 100)
                .build();

        register("town_emergency_supplies")
                .stars(1, 3)
                .weight(9)
                .description(
                        LocalizedText.of(
                                "城镇储备应急物资，需要食物和应急用品。",
                                "Town stockpiles emergency supplies. Needs food and medical items."))
                .submit("minecraft:bread", 20)
                .submit("minecraft:golden_apple", 2)
                .rewardCoins(70)
                .build();

        register("grocer_shelf_restock")
                .stars(1, 2)
                .weight(10)
                .description(
                        LocalizedText.of(
                                "杂货铺木材见底了，先送一批常用木板过来补货。",
                                "The grocer is running low on lumber. Deliver common planks for shelf restock."))
                .submit("minecraft:oak_planks", 24)
                .submit("minecraft:spruce_planks", 24)
                .rewardCoins(55)
                .rewardFavor("otherworldinn:grocer", 65)
                .build();

        register("grocer_masonry_supply")
                .stars(2, 3)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "杂货铺收一批石材。",
                                "The grocer is expanding the masonry aisle. Bring cobblestone, diorite, andesite, and granite."))
                .submit("minecraft:cobblestone", 32)
                .submit("minecraft:diorite", 16)
                .submit("minecraft:andesite", 16)
                .submit("minecraft:granite", 16)
                .rewardCoins(90)
                .rewardFavor("otherworldinn:grocer", 85)
                .build();

        register("grocer_stone_assortment")
                .stars(3, 4)
                .weight(6)
                .description(
                        LocalizedText.of(
                                "有建筑队要一套杂石样品，帮杂货铺备齐凝灰岩和深板岩石料。",
                                "A building crew requested mixed stone samples. Help the grocer prepare tuff and deepslate stock."))
                .submit("minecraft:tuff", 24)
                .submit("minecraft:cobbled_deepslate", 24)
                .submit("minecraft:calcite", 12)
                .rewardCoins(130)
                .rewardFavor("otherworldinn:grocer", 105)
                .build();

        register("adventurers_guild_warden_hunt")
                .stars(5, 5)
                .weight(2)
                .description(
                        LocalizedText.of(
                                "冒险家协会发布高危委托：前往深暗之域并击杀 1 只监守者。",
                                "Adventurers Guild high-risk request: enter the Deep Dark and kill 1 Warden."))
                .kill("minecraft:warden", 1)
                .rewardCoins(280)
                .rewardItem("minecraft:echo_shard", 2)
                .build();

        register("adventurers_guild_enderman_patrol")
                .stars(4, 5)
                .weight(4)
                .description(
                        LocalizedText.of(
                                "冒险家协会发布巡查委托：清理游荡末影人并回收样本。",
                                "Adventurers Guild patrol request: clear roaming Endermen and recover samples."))
                .kill("minecraft:enderman", 20)
                .rewardCoins(260)
                .rewardItem("minecraft:ender_pearl", 8)
                .build();

        register("adventurers_guild_nether_suppression")
                .stars(5, 5)
                .weight(3)
                .description(
                        LocalizedText.of(
                                "冒险家协会发布下界清剿委托：击杀烈焰人并顺带清理凋灵骷髅。",
                                "Adventurers Guild Nether operation: defeat Blazes and clear Wither Skeletons."))
                .kill("minecraft:blaze", 12)
                .kill("minecraft:wither_skeleton", 16)
                .rewardCoins(360)
                .rewardItem("minecraft:blaze_rod", 10)
                .build();

        register("adventurers_guild_raid_breaker")
                .stars(4, 5)
                .weight(3)
                .description(
                        LocalizedText.of(
                                "冒险家协会发布防务委托：击杀袭击者与唤魔者，降低村庄威胁。",
                                "Adventurers Guild defense request: eliminate Raiders and Evokers to reduce village threats."))
                .kill("minecraft:pillager", 18)
                .kill("minecraft:evoker", 2)
                .rewardCoins(320)
                .rewardItem("minecraft:totem_of_undying", 1)
                .build();

        // ── 屠夫委托 ──

        register("butcher_meat_supply")
                .stars(1, 2)
                .weight(10)
                .description(
                        LocalizedText.of(
                                "屠夫收一批生肉补货，带些猪肉和牛肉过来。",
                                "The butcher needs raw meat to restock. Bring porkchops and beef."))
                .submit("minecraft:porkchop", 16)
                .submit("minecraft:beef", 16)
                .rewardCoins(50)
                .rewardFavor("otherworldinn:butcher", 65)
                .build();

        register("butcher_poultry_order")
                .stars(1, 2)
                .weight(10)
                .description(
                        LocalizedText.of(
                                "屠夫收一批禽肉，带些鸡肉和鸡蛋过来。",
                                "The butcher needs poultry. Bring chicken and eggs."))
                .submit("minecraft:chicken", 20)
                .submit("minecraft:egg", 16)
                .rewardCoins(45)
                .rewardFavor("otherworldinn:butcher", 60)
                .build();

        register("butcher_seafood_order")
                .stars(2, 3)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "屠夫想拓展水产生意，带些鱼过来看看反应。",
                                "The butcher wants to try seafood. Bring cod and salmon for a trial."))
                .submit("minecraft:cod", 20)
                .submit("minecraft:salmon", 20)
                .rewardCoins(60)
                .rewardFavor("otherworldinn:butcher", 70)
                .build();

        register("butcher_leather_order")
                .stars(2, 3)
                .weight(7)
                .description(
                        LocalizedText.of(
                                "屠夫接了一批皮革，需要帮忙凑足皮革和骨粉原料。",
                                "The butcher took a leather commission. Help gather leather and bones."))
                .submit("minecraft:leather", 16)
                .submit("minecraft:bone", 24)
                .rewardCoins(70)
                .rewardFavor("otherworldinn:butcher", 75)
                .build();

        register("butcher_exotic_hunt")
                .stars(4, 5)
                .weight(4)
                .description(
                        LocalizedText.of(
                                "屠夫想要些稀罕货，弄点兔子脚和墨囊来。",
                                "The butcher wants exotic goods. Fetch some rabbit feet and ink sacs."))
                .submit("minecraft:rabbit_foot", 4)
                .submit("minecraft:ink_sac", 8)
                .submit("minecraft:glow_ink_sac", 2)
                .rewardCoins(110)
                .rewardFavor("otherworldinn:butcher", 85)
                .build();

        // ── 建筑工委托 ──

        register("builder_timber_order")
                .stars(1, 2)
                .weight(10)
                .description(
                        LocalizedText.of(
                                "建筑工缺木材，送一批原木和木板过来。",
                                "The builder is short on timber. Deliver logs and planks."))
                .submit("minecraft:oak_log", 32)
                .submit("minecraft:oak_planks", 32)
                .rewardCoins(50)
                .rewardFavor("otherworldinn:builder", 65)
                .build();

        register("builder_stone_order")
                .stars(2, 3)
                .weight(9)
                .description(
                        LocalizedText.of(
                                "建筑工有地基要打，需要大量石料。",
                                "The builder needs stone for a foundation. Bring cobblestone and stone."))
                .submit("minecraft:cobblestone", 48)
                .submit("minecraft:stone", 32)
                .rewardCoins(65)
                .rewardFavor("otherworldinn:builder", 70)
                .build();

        register("builder_sand_gravel_order")
                .stars(1, 2)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "建筑工需要沙子和砾石配砂浆。",
                                "The builder needs sand and gravel for mortar mixing."))
                .submit("minecraft:sand", 32)
                .submit("minecraft:gravel", 32)
                .rewardCoins(55)
                .rewardFavor("otherworldinn:builder", 60)
                .build();

        register("builder_decorative_order")
                .stars(3, 4)
                .weight(6)
                .description(
                        LocalizedText.of(
                                "建筑工承接了一栋精装修，需要砖块和玻璃。",
                                "The builder is working on a fine interior. Needs bricks and glass."))
                .submit("minecraft:bricks", 32)
                .submit("minecraft:glass", 16)
                .submit("minecraft:terracotta", 16)
                .rewardCoins(100)
                .rewardFavor("otherworldinn:builder", 80)
                .build();

        register(FishingCommissionGenerator.TEMPLATE_ID)
                .stars(1, 5)
                .weight(7)
                .description(
                        LocalizedText.of(
                                "渔夫正在收购一条指定的鲜鱼。带回委托要求的目标鱼即可完成这份钓鱼委托。",
                                "The fisherman is buying a specific fresh catch. Bring back the requested fish to finish this fishing commission."))
                .build();
    }

    private CommissionRegistry() {}

    public static Builder register(String id) {
        return new Builder(id);
    }

    public static List<CommissionTemplate> allTemplates() {
        return Collections.unmodifiableList(TEMPLATES);
    }

    public static CommissionTemplate pickRandomTemplate(
            RandomSource random, List<String> excludedTemplateIds) {
        List<CommissionTemplate> pool = new ArrayList<>();
        for (CommissionTemplate template : TEMPLATES) {
            if (!excludedTemplateIds.contains(template.id())) {
                pool.add(template);
            }
        }
        if (pool.isEmpty()) {
            pool = TEMPLATES;
        }
        if (pool.isEmpty()) {
            return null;
        }
        int totalWeight = 0;
        for (CommissionTemplate template : pool) {
            totalWeight += Math.max(1, template.weight());
        }
        int roll = random.nextInt(Math.max(1, totalWeight));
        int acc = 0;
        for (CommissionTemplate template : pool) {
            acc += Math.max(1, template.weight());
            if (roll < acc) {
                return template;
            }
        }
        return pool.get(random.nextInt(pool.size()));
    }

    public record CommissionTemplate(
            String id,
            int minStars,
            int maxStars,
            int weight,
            LocalizedText description,
            List<CommissionEntry.ItemRequirement> submitRequirements,
            List<CommissionEntry.KillRequirement> killRequirements,
            List<CommissionEntry.ItemReward> itemRewards,
            int coinReward,
            List<CommissionEntry.NpcFavorReward> npcFavorRewards) {
        public String descriptionKey() {
            return "commission.otherworldinn.description." + id;
        }
    }

    public static final class Builder {
        private final String id;
        private int minStars = 1;
        private int maxStars = 5;
        private int weight = 1;
        private LocalizedText description = LocalizedText.of("暂无描述", "No description");
        private final List<CommissionEntry.ItemRequirement> submitRequirements = new ArrayList<>();
        private final List<CommissionEntry.KillRequirement> killRequirements = new ArrayList<>();
        private final List<CommissionEntry.ItemReward> itemRewards = new ArrayList<>();
        private int coinReward = 0;
        private final List<CommissionEntry.NpcFavorReward> npcFavorRewards = new ArrayList<>();

        private Builder(String id) {
            this.id = id;
        }

        public Builder stars(int minStars, int maxStars) {
            this.minStars = Math.max(1, Math.min(5, minStars));
            this.maxStars = Math.max(this.minStars, Math.max(1, Math.min(5, maxStars)));
            return this;
        }

        public Builder weight(int weight) {
            this.weight = Math.max(1, weight);
            return this;
        }

        public Builder description(LocalizedText description) {
            if (description != null) {
                this.description = description;
            }
            return this;
        }

        public Builder submit(String itemId, int count) {
            this.submitRequirements.add(new CommissionEntry.ItemRequirement(itemId, count));
            return this;
        }

        public Builder submit(String itemId, int count, CompoundTag nbt) {
            this.submitRequirements.add(new CommissionEntry.ItemRequirement(itemId, count, nbt));
            return this;
        }

        public Builder kill(String entityTypeId, int count) {
            this.killRequirements.add(new CommissionEntry.KillRequirement(entityTypeId, count));
            return this;
        }

        public Builder rewardItem(String itemId, int count) {
            this.itemRewards.add(new CommissionEntry.ItemReward(itemId, count));
            return this;
        }

        public Builder rewardCoins(int coinReward) {
            this.coinReward = Math.max(0, coinReward);
            return this;
        }

        public Builder rewardFavor(String npcEntityTypeId, int favorProgress) {
            this.npcFavorRewards.add(
                    new CommissionEntry.NpcFavorReward(npcEntityTypeId, favorProgress));
            return this;
        }

        public CommissionTemplate build() {
            CommissionTemplate template =
                    new CommissionTemplate(
                            id,
                            minStars,
                            maxStars,
                            weight,
                            description,
                            List.copyOf(submitRequirements),
                            List.copyOf(killRequirements),
                            List.copyOf(itemRewards),
                            coinReward,
                            List.copyOf(npcFavorRewards));
            TEMPLATES.add(template);
            return template;
        }
    }
}
