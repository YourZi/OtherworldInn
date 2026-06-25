package com.otherworldinn.world.storyguest;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.base.GuestEntity;
import com.otherworldinn.world.dialogue.DialogueDefinition;
import com.otherworldinn.world.dialogue.DialogueEffectDef;
import com.otherworldinn.world.dialogue.DialogueNodeDef;
import com.otherworldinn.world.dialogue.DialogueOptionDef;
import com.otherworldinn.world.dialogue.DialogueOptionType;
import com.otherworldinn.world.dialogue.DialogueRequirementDef;
import com.otherworldinn.world.dialogue.LocalizedText;
import com.otherworldinn.world.inn.GuestData;
import com.otherworldinn.world.photo.StoryGuestPhotoTaskRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class StoryGuestRegistry {
    private static final ResourceLocation TEXTURE_WANDERING_CARTOGRAPHER =
            storyGuestTexture("wandering_cartographer");
    private static final ResourceLocation TEXTURE_WANDERING_MINSTREL =
            storyGuestTexture("wandering_minstrel");
    private static final ResourceLocation TEXTURE_WANDERING_CHEF =
            storyGuestTexture("wandering_chef");
    private static final ResourceLocation TEXTURE_FALLEN_NOBLE =
            storyGuestTexture("fallen_noble");
    private static final ResourceLocation TEXTURE_WANDERING_ALCHEMIST =
            storyGuestTexture("wandering_alchemist");
    private static final ResourceLocation TEXTURE_ARCHAEOLOGIST =
            storyGuestTexture("archaeologist");
    private static final ResourceLocation TEXTURE_GEM_MERCHANT =
            storyGuestTexture("gem_merchant");
    private static final ResourceLocation TEXTURE_OLD_KNIGHT =
            storyGuestTexture("old_knight");
    private static final ResourceLocation TEXTURE_CURSED_ADVENTURER =
            storyGuestTexture("cursed_adventurer");
    private static final ResourceLocation TEXTURE_OLD_ANGLER =
            storyGuestTexture("old_angler");
    private static final ResourceLocation PAPER =
            ResourceLocation.fromNamespaceAndPath("minecraft", "paper");
    private static final ResourceLocation FEATHER =
            ResourceLocation.fromNamespaceAndPath("minecraft", "feather");
    private static final ResourceLocation INK_SAC =
            ResourceLocation.fromNamespaceAndPath("minecraft", "ink_sac");
    private static final ResourceLocation BREAD =
            ResourceLocation.fromNamespaceAndPath("minecraft", "bread");
    private static final ResourceLocation COOKED_BEEF =
            ResourceLocation.fromNamespaceAndPath("minecraft", "cooked_beef");
    private static final ResourceLocation BAKED_POTATO =
            ResourceLocation.fromNamespaceAndPath("minecraft", "baked_potato");
    private static final ResourceLocation PUMPKIN_PIE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "pumpkin_pie");
    private static final ResourceLocation HONEY_BOTTLE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "honey_bottle");
    private static final ResourceLocation GOLD_INGOT =
            ResourceLocation.fromNamespaceAndPath("minecraft", "gold_ingot");
    private static final ResourceLocation WHITE_WOOL =
            ResourceLocation.fromNamespaceAndPath("minecraft", "white_wool");
    private static final ResourceLocation EMERALD =
            ResourceLocation.fromNamespaceAndPath("minecraft", "emerald");
    private static final ResourceLocation DIAMOND =
            ResourceLocation.fromNamespaceAndPath("minecraft", "diamond");
    private static final ResourceLocation NETHER_WART =
            ResourceLocation.fromNamespaceAndPath("minecraft", "nether_wart");
    private static final ResourceLocation BLAZE_POWDER =
            ResourceLocation.fromNamespaceAndPath("minecraft", "blaze_powder");
    private static final ResourceLocation GLASS_BOTTLE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "glass_bottle");
    private static final ResourceLocation IRON_PICKAXE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "iron_pickaxe");
    private static final ResourceLocation TORCH =
            ResourceLocation.fromNamespaceAndPath("minecraft", "torch");
    private static final ResourceLocation ECHO_SHARD =
            ResourceLocation.fromNamespaceAndPath("minecraft", "echo_shard");
    private static final ResourceLocation ANCIENT_DEBRIS =
            ResourceLocation.fromNamespaceAndPath("minecraft", "ancient_debris");
    private static final ResourceLocation LEATHER =
            ResourceLocation.fromNamespaceAndPath("minecraft", "leather");
    private static final ResourceLocation STRING_ITEM =
            ResourceLocation.fromNamespaceAndPath("minecraft", "string");
    private static final ResourceLocation IRON_INGOT =
            ResourceLocation.fromNamespaceAndPath("minecraft", "iron_ingot");
    private static final ResourceLocation RED_WOOL =
            ResourceLocation.fromNamespaceAndPath("minecraft", "red_wool");
    private static final ResourceLocation MILK_BUCKET =
            ResourceLocation.fromNamespaceAndPath("minecraft", "milk_bucket");
    private static final ResourceLocation GOLDEN_APPLE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "golden_apple");
    private static final ResourceLocation FERMENTED_SPIDER_EYE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "fermented_spider_eye");
    private static final ResourceLocation COD =
            ResourceLocation.fromNamespaceAndPath("minecraft", "cod");
    private static final ResourceLocation SALMON =
            ResourceLocation.fromNamespaceAndPath("minecraft", "salmon");
    private static final ResourceLocation PUFFERFISH =
            ResourceLocation.fromNamespaceAndPath("minecraft", "pufferfish");
    private static final ResourceLocation NATURES_COMPASS =
            ResourceLocation.fromNamespaceAndPath("naturescompass", "naturescompass");
    private static final ResourceLocation MINSTREL_DISC =
            ResourceLocation.fromNamespaceAndPath("minecraft", "music_disc_mellohi");
    private static final ResourceLocation CHEF_LUNCH_BAG =
            ResourceLocation.fromNamespaceAndPath(
                    "kaleidoscope_cookery", "transmutation_lunch_bag");
    private static final ResourceLocation NOBLE_CLOCK =
            ResourceLocation.fromNamespaceAndPath("minecraft", "clock");
    private static final ResourceLocation ALCHEMIST_POTION =
            ResourceLocation.fromNamespaceAndPath("minecraft", "potion");
    private static final ResourceLocation ARCHAEOLOGIST_BRUSH =
            ResourceLocation.fromNamespaceAndPath("minecraft", "brush");
    private static final ResourceLocation GEM_MERCHANT_STAR =
            ResourceLocation.fromNamespaceAndPath("minecraft", "nether_star");
    private static final ResourceLocation OLD_KNIGHT_SHIELD =
            ResourceLocation.fromNamespaceAndPath("minecraft", "shield");
    private static final ResourceLocation CURSED_ADVENTURER_APPLE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "golden_apple");
    private static final ResourceLocation OLD_ANGLER_ROD =
            ResourceLocation.fromNamespaceAndPath("minecraft", "fishing_rod");
    private static final String CARTOGRAPHER_HELPED_FLAG = "cartographer_helped";
    private static final String CARTOGRAPHER_CHATTER_DIALOGUE_ID = "story_cartographer_visit_chatter";
    private static final String MINSTREL_SUPPLIED_FLAG = "minstrel_supplied";
    private static final String MINSTREL_SETTLED_IN_INN_FLAG = "minstrel_settled_in_inn";
    private static final String MINSTREL_KEEPS_TRAVELING_FLAG = "minstrel_keeps_traveling";
    private static final String MINSTREL_CHATTER_DIALOGUE_ID = "story_minstrel_visit_chatter";
    private static final String CHEF_SUPPLIED_FLAG = "chef_supplied";
    private static final String CHEF_CHATTER_DIALOGUE_ID = "story_chef_visit_chatter";
    private static final String NOBLE_ATTIRE_FLAG = "noble_attire";
    private static final String NOBLE_FUND_FLAG = "noble_fund";
    private static final String NOBLE_CHATTER_DIALOGUE_ID = "story_noble_visit_chatter";
    private static final String ALCHEMIST_SUPPLIED_FLAG = "alchemist_supplied";
    private static final String ALCHEMIST_SETTLED_IN_INN_FLAG = "alchemist_settled_in_inn";
    private static final String ALCHEMIST_KEEPS_TRAVELING_FLAG = "alchemist_keeps_traveling";
    private static final String ALCHEMIST_CHATTER_DIALOGUE_ID = "story_alchemist_visit_chatter";
    private static final String ARCHAEOLOGIST_TOOLS_FLAG = "archaeologist_tools";
    private static final String ARCHAEOLOGIST_RELICS_FLAG = "archaeologist_relics";
    private static final String ARCHAEOLOGIST_CHATTER_DIALOGUE_ID =
            "story_archaeologist_visit_chatter";
    private static final String GEM_MERCHANT_WAGON_FLAG = "gem_merchant_wagon";
    private static final String GEM_MERCHANT_SETTLED_IN_INN_FLAG =
            "gem_merchant_settled_in_inn";
    private static final String GEM_MERCHANT_KEEPS_TRAVELING_FLAG =
            "gem_merchant_keeps_traveling";
    private static final String GEM_MERCHANT_CHATTER_DIALOGUE_ID =
            "story_gem_merchant_visit_chatter";
    private static final String OLD_KNIGHT_REARMED_FLAG = "old_knight_rearmed";
    private static final String OLD_KNIGHT_SETTLED_IN_INN_FLAG = "old_knight_settled_in_inn";
    private static final String OLD_KNIGHT_MOVED_ON_FLAG = "old_knight_moved_on";
    private static final String OLD_KNIGHT_CHATTER_DIALOGUE_ID = "story_old_knight_visit_chatter";
    private static final String CURSED_ADVENTURER_CURE_FLAG = "cursed_adventurer_cure";
    private static final String CURSED_ADVENTURER_STAYED_FLAG = "cursed_adventurer_stayed";
    private static final String CURSED_ADVENTURER_LEFT_FLAG = "cursed_adventurer_left";
    private static final String CURSED_ADVENTURER_CHATTER_DIALOGUE_ID =
            "story_cursed_adventurer_visit_chatter";
    private static final String OLD_ANGLER_CATCH_FLAG = "old_angler_catch";
    private static final String OLD_ANGLER_CHATTER_DIALOGUE_ID = "story_old_angler_visit_chatter";

    private static final Map<String, StoryGuestDefinition> DEFINITIONS_BY_ID;
    private static final List<DialogueDefinition> ALL_DIALOGUES;

    static {
        StoryGuestDefinition wanderingCartographer = buildWanderingCartographer();
        StoryGuestDefinition wanderingMinstrel = buildWanderingMinstrel();
        StoryGuestDefinition wanderingChef = buildWanderingChef();
        StoryGuestDefinition fallenNoble = buildFallenNoble();
        StoryGuestDefinition wanderingAlchemist = buildWanderingAlchemist();
        StoryGuestDefinition archaeologist = buildArchaeologist();
        StoryGuestDefinition gemMerchant = buildGemMerchant();
        StoryGuestDefinition oldKnight = buildOldKnight();
        StoryGuestDefinition cursedAdventurer = buildCursedAdventurer();
        StoryGuestDefinition oldAngler = buildOldAngler();
        Map<String, StoryGuestDefinition> byId = new LinkedHashMap<>();
        byId.put(wanderingCartographer.id(), wanderingCartographer);
        byId.put(wanderingMinstrel.id(), wanderingMinstrel);
        byId.put(wanderingChef.id(), wanderingChef);
        byId.put(fallenNoble.id(), fallenNoble);
        byId.put(wanderingAlchemist.id(), wanderingAlchemist);
        byId.put(archaeologist.id(), archaeologist);
        byId.put(gemMerchant.id(), gemMerchant);
        byId.put(oldKnight.id(), oldKnight);
        byId.put(cursedAdventurer.id(), cursedAdventurer);
        byId.put(oldAngler.id(), oldAngler);
        DEFINITIONS_BY_ID = Collections.unmodifiableMap(byId);

        List<DialogueDefinition> dialogues = new ArrayList<>();
        for (StoryGuestDefinition definition : DEFINITIONS_BY_ID.values()) {
            dialogues.addAll(definition.dialogues());
        }
        ALL_DIALOGUES = Collections.unmodifiableList(dialogues);
    }

    private StoryGuestRegistry() {}

    @Nullable
    public static StoryGuestDefinition get(String storyGuestId) {
        return DEFINITIONS_BY_ID.get(storyGuestId);
    }

    public static List<StoryGuestDefinition> allDefinitions() {
        return DEFINITIONS_BY_ID.values().stream().toList();
    }

    public static List<DialogueDefinition> allDialogues() {
        return ALL_DIALOGUES;
    }

    private static ResourceLocation storyGuestTexture(String storyGuestId) {
        return ResourceLocation.fromNamespaceAndPath(
                OtherworldInn.MODID, "textures/entity/guest/story_guest/" + storyGuestId + ".png");
    }

    private static StoryGuestDefinition buildWanderingCartographer() {
        String id = "wandering_cartographer";
        GuestEntity.GuestProfile profile =
                new GuestEntity.GuestProfile(
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(18, 26), new GuestData.IntRange(72, 88)),
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(20, 28), new GuestData.IntRange(82, 96)),
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(5, 15), new GuestData.IntRange(68, 82)),
                        new GuestData.IntRange(18, 18),
                        1.15D);
        List<DialogueDefinition> dialogues =
                List.of(
                        buildCartographerStage0Dialogue(),
                        buildCartographerStage1Dialogue(),
                        buildCartographerStage2Dialogue(),
                        buildCartographerStage3Dialogue(),
                        buildCartographerVisitChatterDialogue());
        return new StoryGuestDefinition(
                id,
                LocalizedText.of("流浪绘图师 伊莱", "Eli the Wandering Cartographer"),
                TEXTURE_WANDERING_CARTOGRAPHER,
                "default",
                0,
                profile,
                2,
                5,
                1,
                1,
                3,
                List.of(),
                Map.of(
                        0, "story_cartographer_stage0",
                        1, "story_cartographer_stage1",
                        2, "story_cartographer_stage2",
                        3, "story_cartographer_stage3"),
                CARTOGRAPHER_CHATTER_DIALOGUE_ID,
                dialogues);
    }

    private static StoryGuestDefinition buildWanderingMinstrel() {
        String id = "wandering_minstrel";
        GuestEntity.GuestProfile profile =
                new GuestEntity.GuestProfile(
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(16, 24), new GuestData.IntRange(70, 88)),
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(14, 22), new GuestData.IntRange(64, 82)),
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(8, 18), new GuestData.IntRange(58, 74)),
                        new GuestData.IntRange(14, 16),
                        1.1D);
        List<DialogueDefinition> dialogues =
                List.of(
                        buildMinstrelStage0Dialogue(),
                        buildMinstrelStage1Dialogue(),
                        buildMinstrelStage2Dialogue(),
                        buildMinstrelStage3Dialogue(),
                        buildMinstrelVisitChatterDialogue());
        return new StoryGuestDefinition(
                id,
                LocalizedText.of("流浪吟游诗人 赛琳", "Selene the Wandering Minstrel"),
                TEXTURE_WANDERING_MINSTREL,
                "slim",
                0,
                profile,
                2,
                5,
                1,
                1,
                3,
                List.of(
                        new StoryGuestVisitOutcomeRule(MINSTREL_KEEPS_TRAVELING_FLAG, false),
                        new StoryGuestVisitOutcomeRule(MINSTREL_SETTLED_IN_INN_FLAG, true)),
                Map.of(
                        0, "story_minstrel_stage0",
                        1, "story_minstrel_stage1",
                        2, "story_minstrel_stage2",
                        3, "story_minstrel_stage3"),
                MINSTREL_CHATTER_DIALOGUE_ID,
                dialogues);
    }

    private static DialogueDefinition buildMinstrelStage0Dialogue() {
        String root = "root";
        String helped = "helped";
        String refused = "refused";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我一路记了不少歌谣，可纸页快写满了，墨也快见底。要是你愿意借我五张纸和一枚墨囊，下回我就唱一段新写好的给你听。",
                                "I've gathered a fair number of songs on the road, but I'm nearly out of paper and ink. If you can spare five sheets of paper and an ink sac, I'll sing you something new the next time I return."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_minstrel_offer_supplies",
                                                LocalizedText.of("这些你先拿去用吧", "Take these and make good use of them"),
                                                DialogueOptionType.BRANCH,
                                                helped,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasItem(PAPER, 5),
                                                DialogueRequirementDef.hasItem(INK_SAC, 1))
                                        .withEffects(
                                                DialogueEffectDef.takeItem(PAPER, 5),
                                                DialogueEffectDef.takeItem(INK_SAC, 1),
                                                DialogueEffectDef.setStoryFlag(MINSTREL_SUPPLIED_FLAG),
                                                DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                                "story_minstrel_decline_supplies",
                                                LocalizedText.of("我手头也不宽裕", "I'm a little short myself"),
                                                DialogueOptionType.BRANCH,
                                                refused,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                        "story_minstrel_leave_intro",
                                        LocalizedText.of("等我准备好了再来找你", "Come back when I've prepared"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                helped,
                new DialogueNodeDef(
                        helped,
                        LocalizedText.of(
                                "这就对了。好歌总要先被好好写下来，才不至于半路散进风里。",
                                "That's the spirit. A good song should be written down properly before the wind steals half of it away."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_minstrel_leave_after_help",
                                                LocalizedText.of("那我等你下次回来", "Then I'll wait for your return"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        nodes.put(
                refused,
                new DialogueNodeDef(
                        refused,
                        LocalizedText.of(
                                "也罢，诗人总能从窘迫里翻出几句能唱的词。只是这次要写得慢些了。",
                                "So be it. A minstrel can always wring a few verses out of hardship. I'll just have to write more slowly this time."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_minstrel_leave_after_refuse",
                                                LocalizedText.of("愿你路上仍有歌可唱", "May the road still give you songs"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        return new DialogueDefinition("story_minstrel_stage0", root, nodes);
    }

    private static DialogueDefinition buildMinstrelStage1Dialogue() {
        String root = "root";
        String accepted = "accepted";
        String declined = "declined";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我这一路听了不少风声，但总差一张能让我定下副歌的景色。若你路过平原，替我拍一张开阔的地平线吧，也许我能把这首歌真正写完。",
                                "I've listened to plenty of winds along the road, but I'm still missing the view that can settle the chorus. If you pass through the plains, bring me a photograph of that wide horizon. It might finally let me finish this song."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_minstrel_accept_plains_photo_task",
                                                LocalizedText.of("可以，我会替你留意平原的风景", "I can do that. I'll watch for a plains horizon"),
                                                DialogueOptionType.BRANCH,
                                                accepted,
                                                null)
                                        .withEffects(
                                                DialogueEffectDef.setStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .MINSTREL_PLAINS_PHOTO_REQUESTED_FLAG),
                                                DialogueEffectDef.advanceStoryStage(2)),
                                new DialogueOptionDef(
                                                "story_minstrel_decline_plains_photo_task",
                                                LocalizedText.of("这次恐怕没有空闲", "I may not have the time this round"),
                                                DialogueOptionType.BRANCH,
                                                declined,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(2)))));
        nodes.put(
                accepted,
                new DialogueNodeDef(
                        accepted,
                        LocalizedText.of(
                                "不用找什么奇景，只要是一望过去像还能容得下新故事的地方就行。",
                                "It doesn't need to be some marvel. Any place that looks like it still has room for a new story will do."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_minstrel_leave_after_accept_photo",
                                                LocalizedText.of("我记下了", "I'll remember that"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 4)))));
        nodes.put(
                declined,
                new DialogueNodeDef(
                        declined,
                        LocalizedText.of(
                                "没关系。有些歌本就该在心里多搁一阵，等下回风向合适再唱。",
                                "That's alright. Some songs are meant to rest in the heart a while longer, until the wind turns the right way."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_minstrel_leave_after_decline_photo",
                                                LocalizedText.of("那就留到下次吧", "Then save it for next time"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 4)))));
        return new DialogueDefinition("story_minstrel_stage1", root, nodes);
    }

    private static DialogueDefinition buildMinstrelStage2Dialogue() {
        String root = "root";
        String settled = "settled";
        String traveling = "traveling";
        String farewell = "farewell";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "这一路欠下的曲谱和风景，我都一一记着。你给过我的纸墨也好，替我带回来的平原天色也好，都让我得把这首歌写到最后一页了。",
                                "I've kept track of every borrowed sheet and every horizon along this road. Whether it was the paper and ink you shared or the plains sky you brought back for me, it's all pushed this song toward its final page."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_minstrel_reward_settle",
                                                LocalizedText.of(
                                                        "那就把它唱完，也留在这里吧",
                                                        "Then finish it, and stay here a while"),
                                                DialogueOptionType.BRANCH,
                                                settled,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasStoryFlag(MINSTREL_SUPPLIED_FLAG),
                                                DialogueRequirementDef.hasStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .MINSTREL_PLAINS_PHOTO_COMPLETED_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.giveItem(MINSTREL_DISC, 1),
                                                DialogueEffectDef.giveCoins(12),
                                                DialogueEffectDef.setStoryFlag(
                                                        MINSTREL_SETTLED_IN_INN_FLAG),
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(3, 6)),
                                new DialogueOptionDef(
                                                "story_minstrel_reward_travel_after_supplies",
                                                LocalizedText.of(
                                                        "至少你已经把想唱的话写下来了",
                                                        "At least you've written down what you meant to sing"),
                                                DialogueOptionType.BRANCH,
                                                traveling,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasStoryFlag(MINSTREL_SUPPLIED_FLAG),
                                                DialogueRequirementDef.missingStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .MINSTREL_PLAINS_PHOTO_COMPLETED_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.giveItem(MINSTREL_DISC, 1),
                                                DialogueEffectDef.setStoryFlag(
                                                        MINSTREL_KEEPS_TRAVELING_FLAG),
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(4, 8)),
                                new DialogueOptionDef(
                                                "story_minstrel_reward_travel_after_photo",
                                                LocalizedText.of(
                                                        "至少你已经找到那片适合副歌的天色",
                                                        "At least you've found the sky that fits the chorus"),
                                                DialogueOptionType.BRANCH,
                                                traveling,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.missingStoryFlag(MINSTREL_SUPPLIED_FLAG),
                                                DialogueRequirementDef.hasStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .MINSTREL_PLAINS_PHOTO_COMPLETED_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.giveItem(MINSTREL_DISC, 1),
                                                DialogueEffectDef.setStoryFlag(
                                                        MINSTREL_KEEPS_TRAVELING_FLAG),
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(4, 8)),
                                new DialogueOptionDef(
                                                "story_minstrel_reward_farewell",
                                                LocalizedText.of(
                                                        "这回我们大概都没赶上好时机",
                                                        "Perhaps neither of us caught the right moment this time"),
                                                DialogueOptionType.BRANCH,
                                                farewell,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.missingStoryFlag(MINSTREL_SUPPLIED_FLAG),
                                                DialogueRequirementDef.missingStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .MINSTREL_PLAINS_PHOTO_COMPLETED_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.setStoryFlag(
                                                        MINSTREL_KEEPS_TRAVELING_FLAG),
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(4, 8)))));
        nodes.put(
                settled,
                new DialogueNodeDef(
                        settled,
                        LocalizedText.of(
                                "也许你说得对。我已经漂了太久，久到忘了好歌未必非得在路上唱完。这张唱片你收着吧，等我把剩下的段落也唱稳了，再回来慢慢给你听。",
                                "Maybe you're right. I've drifted for so long I forgot a good song doesn't have to be finished on the road. Keep this disc for me. Once I've steadied the rest of the verses, I'll come back and sing them properly."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_minstrel_leave_after_settle_reward",
                                        LocalizedText.of("那我就等你把整首歌唱完", "Then I'll wait to hear the full song"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                traveling,
                new DialogueNodeDef(
                        traveling,
                        LocalizedText.of(
                                "歌是写下来了，可我的脚步还没肯停。把这张唱片收好吧，算是我把没唱完的副歌先留给你。等我在别处把后半段补齐，也许风会替我把名字吹回来。",
                                "The song is written, but my feet still refuse to stop. Keep this disc for me. Think of it as the unfinished chorus I've left in your care. If I finish the rest somewhere else, perhaps the wind will carry my name back here."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_minstrel_leave_after_travel_reward",
                                        LocalizedText.of("愿你下一段路也有人听你唱", "May the next road still give you an audience"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                farewell,
                new DialogueNodeDef(
                        farewell,
                        LocalizedText.of(
                                "有些歌这次终究没能写成。没关系，旅人和诗句一样，总有错过的时候。我该继续上路了。",
                                "Some songs simply weren't meant to be finished this time. That's alright. Travelers and verses both know what it means to miss their moment. I should keep moving."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_minstrel_leave_after_farewell",
                                        LocalizedText.of("愿你在路上找到下一段旋律", "May the road hand you your next melody"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition("story_minstrel_stage2", root, nodes);
    }

    private static DialogueDefinition buildMinstrelStage3Dialogue() {
        String root = "root";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我现在愿意把脚步慢下来一些了。旅社里的人声、杯盏和晚风，比赶路时更适合给副歌定拍子。",
                                "I'm willing to slow my steps a little now. The voices, cups, and evening breeze of this inn keep better time for a chorus than the road ever did."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_minstrel_leave_final",
                                        LocalizedText.of("等你唱新段落给我听", "Save the next verse for me"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition("story_minstrel_stage3", root, nodes);
    }

    private static DialogueDefinition buildMinstrelVisitChatterDialogue() {
        String root = "root";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我正把前些天写下的句子重新谱成调子。旅社里的灯火真奇怪，总让人愿意把漂泊唱得轻一点。",
                                "I'm setting the lines I wrote the other day back to melody. There's something strange about this inn's lamplight. It makes wandering sound softer than it used to."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_minstrel_leave_chatter",
                                        LocalizedText.of("写好了就唱给我听", "Sing it for me when it's done"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition(MINSTREL_CHATTER_DIALOGUE_ID, root, nodes);
    }

    private static StoryGuestDefinition buildWanderingChef() {
        String id = "wandering_chef";
        GuestEntity.GuestProfile profile =
                new GuestEntity.GuestProfile(
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(18, 28), new GuestData.IntRange(74, 92)),
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(16, 24), new GuestData.IntRange(66, 82)),
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(10, 20), new GuestData.IntRange(54, 70)),
                        new GuestData.IntRange(16, 18),
                        1.08D);
        List<DialogueDefinition> dialogues =
                List.of(
                        buildChefStage0Dialogue(),
                        buildChefStage1Dialogue(),
                        buildChefStage2Dialogue(),
                        buildChefStage3Dialogue(),
                        buildChefVisitChatterDialogue());
        return new StoryGuestDefinition(
                id,
                LocalizedText.of("流浪厨师 古斯塔沃", "Gustavo the Wandering Chef"),
                TEXTURE_WANDERING_CHEF,
                "default",
                0,
                profile,
                2,
                5,
                2,
                1,
                3,
                List.of(),
                Map.of(
                        0, "story_chef_stage0",
                        1, "story_chef_stage1",
                        2, "story_chef_stage2",
                        3, "story_chef_stage3"),
                CHEF_CHATTER_DIALOGUE_ID,
                dialogues);
    }

    private static DialogueDefinition buildChefStage0Dialogue() {
        String root = "root";
        String helped = "helped";
        String refused = "refused";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我在给下一季菜单试味，可总觉得还差一点当地人的烟火气。要是你愿意，给我带些真正能上桌的家常货：面包、熟牛肉、烤土豆、南瓜派，再来一瓶蜂蜜。",
                                "I'm tasting ideas for my next season's menu, but it still lacks the feel of a local table. If you're willing, bring me the sort of things that truly belong on a supper spread: bread, cooked beef, baked potato, pumpkin pie, and a bottle of honey."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_chef_offer_supplies",
                                                LocalizedText.of("行，我给你凑一份正经晚餐", "Alright, I'll put together a proper supper for you"),
                                                DialogueOptionType.BRANCH,
                                                helped,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasItem(BREAD, 1),
                                                DialogueRequirementDef.hasItem(COOKED_BEEF, 1),
                                                DialogueRequirementDef.hasItem(BAKED_POTATO, 1),
                                                DialogueRequirementDef.hasItem(PUMPKIN_PIE, 1),
                                                DialogueRequirementDef.hasItem(HONEY_BOTTLE, 1))
                                        .withEffects(
                                                DialogueEffectDef.takeItem(BREAD, 1),
                                                DialogueEffectDef.takeItem(COOKED_BEEF, 1),
                                                DialogueEffectDef.takeItem(BAKED_POTATO, 1),
                                                DialogueEffectDef.takeItem(PUMPKIN_PIE, 1),
                                                DialogueEffectDef.takeItem(HONEY_BOTTLE, 1),
                                                DialogueEffectDef.setStoryFlag(CHEF_SUPPLIED_FLAG),
                                                DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                                "story_chef_decline_supplies",
                                                LocalizedText.of("我这回怕是凑不齐", "I probably can't gather all that this time"),
                                                DialogueOptionType.BRANCH,
                                                refused,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                        "story_chef_leave_intro",
                                        LocalizedText.of("等我备好了再来", "I'll come back when I'm ready"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                helped,
                new DialogueNodeDef(
                        helped,
                        LocalizedText.of(
                                "对，就是这种搭配。不是奢侈，是一桌人坐下以后会安静两秒的那种满足感。",
                                "Yes, that's the balance I wanted. Not luxury, just the kind of spread that makes a whole table fall quiet for two seconds."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_chef_leave_after_help",
                                                LocalizedText.of("那就看你能做出什么来", "Then let's see what you can make of it"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        nodes.put(
                refused,
                new DialogueNodeDef(
                        refused,
                        LocalizedText.of(
                                "没事，锅里总能先煨点别的。我只是想知道，这地方值不值得让我把火生久一点。",
                                "That's alright. I can always let something else simmer first. I'm only trying to learn whether this place deserves a longer fire."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_chef_leave_after_refuse",
                                                LocalizedText.of("那你先慢慢想", "Then take your time"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        return new DialogueDefinition("story_chef_stage0", root, nodes);
    }

    private static DialogueDefinition buildChefStage1Dialogue() {
        String root = "root";
        String accepted = "accepted";
        String declined = "declined";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "食材尝过以后，我还缺一张像样的出处照片。要是你方便，替我拍一张农场动物的画面吧。鸡、猪、羊、牛都行，只要看着像这地方真能养活人。",
                                "Now that I've tasted the ingredients, I still need a proper picture of where they come from. If you can, bring me a photograph of some farm animals. Chicken, pig, sheep, cow, any of them will do, so long as the place looks like it can truly feed people."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_chef_accept_farm_photo_task",
                                                LocalizedText.of("可以，我替你拍一张像样的农场照", "Sure, I'll get you a proper farm photograph"),
                                                DialogueOptionType.BRANCH,
                                                accepted,
                                                null)
                                        .withEffects(
                                                DialogueEffectDef.setStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .CHEF_FARM_PHOTO_REQUESTED_FLAG),
                                                DialogueEffectDef.advanceStoryStage(2)),
                                new DialogueOptionDef(
                                                "story_chef_decline_farm_photo_task",
                                                LocalizedText.of("这回我顾不上去农场", "I can't make it to the farm this time"),
                                                DialogueOptionType.BRANCH,
                                                declined,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(2)))));
        nodes.put(
                accepted,
                new DialogueNodeDef(
                        accepted,
                        LocalizedText.of(
                                "不用摆得多整齐，越像真在过日子越好。好厨房从来不是只靠菜谱撑起来的。",
                                "It doesn't need to look tidy. The more it resembles real life, the better. A good kitchen is never held up by recipes alone."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_chef_leave_after_accept_photo",
                                                LocalizedText.of("行，我知道你想要什么了", "Alright, I know what you're after"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 4)))));
        nodes.put(
                declined,
                new DialogueNodeDef(
                        declined,
                        LocalizedText.of(
                                "也罢，真正的厨房总得给忙人留余地。等你哪天有空，再替我看一眼也不迟。",
                                "Fair enough. A real kitchen always leaves room for busy hands. When you have the time, you'll bring me that glimpse."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_chef_leave_after_decline_photo",
                                                LocalizedText.of("那就留到下次", "Then save it for next time"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 4)))));
        return new DialogueDefinition("story_chef_stage1", root, nodes);
    }

    private static DialogueDefinition buildChefStage2Dialogue() {
        String root = "root";
        String full = "full";
        String partial = "partial";
        String none = "none";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我把这几天试出来的味道都记进去了。食材、景色、火候，还有这里的人声。看样子，我差不多能决定要不要把下一季的锅架在这儿了。",
                                "I've written down the tastes of the last few days. Ingredients, scenery, heat, even the sound of the people here. Looks like I can finally decide whether the next season's pots belong here."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_chef_reward_full",
                                                LocalizedText.of(
                                                        "那就把你的招牌手艺留在这里吧",
                                                        "Then leave your signature craft here"),
                                                DialogueOptionType.BRANCH,
                                                full,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasStoryFlag(CHEF_SUPPLIED_FLAG),
                                                DialogueRequirementDef.hasStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .CHEF_FARM_PHOTO_COMPLETED_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.giveItem(CHEF_LUNCH_BAG, 1),
                                                DialogueEffectDef.giveCoins(16),
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(3, 6)),
                                new DialogueOptionDef(
                                                "story_chef_reward_partial_after_food",
                                                LocalizedText.of(
                                                        "至少你已经吃透了这地方的味道",
                                                        "At least you've learned the taste of this place"),
                                                DialogueOptionType.BRANCH,
                                                partial,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasStoryFlag(CHEF_SUPPLIED_FLAG),
                                                DialogueRequirementDef.missingStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .CHEF_FARM_PHOTO_COMPLETED_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.giveItem(CHEF_LUNCH_BAG, 1),
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(3, 6)),
                                new DialogueOptionDef(
                                                "story_chef_reward_partial_after_photo",
                                                LocalizedText.of(
                                                        "至少你已经看见这地方能养活多少张嘴",
                                                        "At least you've seen how many mouths this place can feed"),
                                                DialogueOptionType.BRANCH,
                                                partial,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.missingStoryFlag(CHEF_SUPPLIED_FLAG),
                                                DialogueRequirementDef.hasStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .CHEF_FARM_PHOTO_COMPLETED_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.giveItem(CHEF_LUNCH_BAG, 1),
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(3, 6)),
                                new DialogueOptionDef(
                                                "story_chef_reward_none",
                                                LocalizedText.of(
                                                        "看来你还是得自己再多试几锅",
                                                        "Looks like you'll need to keep testing a few more pots yourself"),
                                                DialogueOptionType.BRANCH,
                                                none,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.missingStoryFlag(CHEF_SUPPLIED_FLAG),
                                                DialogueRequirementDef.missingStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .CHEF_FARM_PHOTO_COMPLETED_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(3, 6)))));
        nodes.put(
                full,
                new DialogueNodeDef(
                        full,
                        LocalizedText.of(
                                "行，那我就把锅留下。这个饭袋你拿着，里头装的是我最常带在身边的一套便携手艺。等我把新菜单写完，再回来跟你分一桌热乎的。",
                                "Alright then, I'll leave the pots here. Take this lunch bag. It carries the portable tricks I rely on most. Once I've finished the new menu, I'll come back and share a hot meal with you."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_chef_leave_after_full_reward",
                                        LocalizedText.of("那我就等你下一桌菜", "Then I'll wait for your next table"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                partial,
                new DialogueNodeDef(
                        partial,
                        LocalizedText.of(
                                "火候还差一点，但我已经不想走了。先把这个饭袋拿去试着用吧，等我把这地方真正摸透，再把压箱底的菜谱也端出来。",
                                "The heat still needs work, but I don't feel like leaving anymore. Take this lunch bag and try it for now. Once I've truly learned this place, I'll start bringing out the recipes I keep deepest in reserve."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_chef_leave_after_partial_reward",
                                        LocalizedText.of("那你可别让我等太久", "Don't keep me waiting too long"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                none,
                new DialogueNodeDef(
                        none,
                        LocalizedText.of(
                                "你这回没帮上太多，但我还是想在这儿多待一阵。毕竟有些厨房的火，不是靠材料点起来的，是靠人留下来的。",
                                "You didn't help much this time, but I still want to stay here a while longer. Some kitchens aren't lit by ingredients. They're lit by the people who remain."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_chef_leave_after_none_reward",
                                        LocalizedText.of("那就先把炉火守住吧", "Then keep the fire going for now"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition("story_chef_stage2", root, nodes);
    }

    private static DialogueDefinition buildChefStage3Dialogue() {
        String root = "root";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我最近正琢磨把这里的家常味道和旅人的口味揉在一起。等哪天成了，你来替我尝第一口。",
                                "Lately I've been figuring out how to fold this place's homely flavors together with a traveler's appetite. When I finally get it right, you'll be the first to taste it."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_chef_leave_final",
                                        LocalizedText.of("记得给我留最热的那一份", "Save me the hottest serving"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition("story_chef_stage3", root, nodes);
    }

    private static DialogueDefinition buildChefVisitChatterDialogue() {
        String root = "root";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我今天在试一道适合赶路人吃完也不觉得负担的菜。好吃只是底线，真正难的是让人吃完以后还想回来。",
                                "Today I'm testing a dish for travelers who want to leave the table satisfied but not weighed down. Tasty is the baseline. The hard part is making something that still calls them back."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_chef_leave_chatter",
                                        LocalizedText.of("做好了记得先让我尝", "Let me taste it first when it's ready"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition(CHEF_CHATTER_DIALOGUE_ID, root, nodes);
    }

    private static StoryGuestDefinition buildFallenNoble() {
        String id = "fallen_noble";
        GuestEntity.GuestProfile profile =
                new GuestEntity.GuestProfile(
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(20, 30), new GuestData.IntRange(82, 96)),
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(18, 26), new GuestData.IntRange(70, 86)),
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(8, 16), new GuestData.IntRange(56, 70)),
                        new GuestData.IntRange(20, 22),
                        1.12D);
        List<DialogueDefinition> dialogues =
                List.of(
                        buildNobleStage0Dialogue(),
                        buildNobleStage1Dialogue(),
                        buildNobleStage2Dialogue(),
                        buildNobleStage3Dialogue(),
                        buildNobleVisitChatterDialogue());
        return new StoryGuestDefinition(
                id,
                LocalizedText.of("落魄贵族 艾什福德", "Lord Ashford, the Fallen Noble"),
                TEXTURE_FALLEN_NOBLE,
                "default",
                0,
                profile,
                2,
                5,
                3,
                1,
                3,
                List.of(),
                Map.of(
                        0, "story_noble_stage0",
                        1, "story_noble_stage1",
                        2, "story_noble_stage2",
                        3, "story_noble_stage3"),
                NOBLE_CHATTER_DIALOGUE_ID,
                dialogues);
    }

    private static DialogueDefinition buildNobleStage0Dialogue() {
        String root = "root";
        String helped = "helped";
        String refused = "refused";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我可不是在乞求施舍，只是如今要见人，总得先把体面拼回去一点。若你愿意，给我两块金锭和十六团白羊毛，至少能让我把这副样子整理得像回事。",
                                "I am not begging for charity. But if one is expected to be seen, one must first stitch some dignity back together. If you're willing, bring me two gold ingots and sixteen bundles of white wool. It would at least let me look presentable again."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_noble_offer_attire",
                                                LocalizedText.of("行，我帮你把场面撑起来", "Fine, I'll help you restore some appearances"),
                                                DialogueOptionType.BRANCH,
                                                helped,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasItem(GOLD_INGOT, 2),
                                                DialogueRequirementDef.hasItem(WHITE_WOOL, 16))
                                        .withEffects(
                                                DialogueEffectDef.takeItem(GOLD_INGOT, 2),
                                                DialogueEffectDef.takeItem(WHITE_WOOL, 16),
                                                DialogueEffectDef.setStoryFlag(NOBLE_ATTIRE_FLAG),
                                                DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                                "story_noble_decline_attire",
                                                LocalizedText.of("这回我帮不了这么多", "I can't spare that much this time"),
                                                DialogueOptionType.BRANCH,
                                                refused,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                        "story_noble_leave_intro",
                                        LocalizedText.of("等我有余力再说", "We'll speak again when I have the means"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                helped,
                new DialogueNodeDef(
                        helped,
                        LocalizedText.of(
                                "总算不至于像被命运从楼梯上踹下来之后，顺着泥地滚进旅社的样子了。你这份眼光，我记住了。",
                                "At last, I needn't look like fate kicked me down a staircase and rolled me through the mud into this inn. I will remember that you had the decency to notice."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_noble_leave_after_attire_help",
                                                LocalizedText.of("先把这身行头穿稳吧", "Then wear it with some confidence"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        nodes.put(
                refused,
                new DialogueNodeDef(
                        refused,
                        LocalizedText.of(
                                "也罢。一个家族若真只剩下衣料和镀金撑场面，那本来也没剩下多少东西了。",
                                "Very well. If a house can be held together only by cloth and gilt, then perhaps there was little of it left to save in the first place."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_noble_leave_after_attire_refuse",
                                                LocalizedText.of("至少你还剩下点自尊", "At least you still have your pride"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        return new DialogueDefinition("story_noble_stage0", root, nodes);
    }

    private static DialogueDefinition buildNobleStage1Dialogue() {
        String root = "root";
        String accepted = "accepted";
        String declined = "declined";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "如今这片大陆上，仍算得上像样门面的地方已经不多了。要是你恰好路过林地府邸，替我拍一张照片吧。我想看看，这世上是不是还剩几分旧日排场。",
                                "There are precious few places left on this continent that can still be called proper seats of stature. If you ever pass a woodland mansion, bring me a photograph. I would like to know whether the world still remembers how to carry itself."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_noble_accept_mansion_photo_task",
                                                LocalizedText.of("可以，我替你看看还剩几分门面", "Very well, I'll see how much grandeur remains"),
                                                DialogueOptionType.BRANCH,
                                                accepted,
                                                null)
                                        .withEffects(
                                                DialogueEffectDef.setStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .NOBLE_MANSION_PHOTO_REQUESTED_FLAG),
                                                DialogueEffectDef.advanceStoryStage(2)),
                                new DialogueOptionDef(
                                                "story_noble_decline_mansion_photo_task",
                                                LocalizedText.of("这种地方我未必去得成", "I may never make it to such a place"),
                                                DialogueOptionType.BRANCH,
                                                declined,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(2)))));
        nodes.put(
                accepted,
                new DialogueNodeDef(
                        accepted,
                        LocalizedText.of(
                                "不必取最壮阔的角度。只要能让我看见，那些高窗与立面是否还撑得住过去的名字就够了。",
                                "No need to chase the grandest angle. I only need to see whether those high windows and facades still have the strength to bear their old names."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_noble_leave_after_accept_photo",
                                                LocalizedText.of("那我记下了", "Then I'll remember what to look for"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 4)))));
        nodes.put(
                declined,
                new DialogueNodeDef(
                        declined,
                        LocalizedText.of(
                                "也正常。不是谁都愿意专程去看一栋还没倒塌的旧体面。",
                                "Naturally. Not everyone wishes to travel merely to look upon a ruin that still has the manners not to collapse."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_noble_leave_after_decline_photo",
                                                LocalizedText.of("那就当你还没准备好看见它", "Then perhaps you weren't ready to see it"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 4)))));
        return new DialogueDefinition("story_noble_stage1", root, nodes);
    }

    private static DialogueDefinition buildNobleStage2Dialogue() {
        String root = "root";
        String full = "full";
        String partial = "partial";
        String none = "none";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "体面总算拾回了一点，旧门第的影子我也算看过了。如今剩下的，不过是让一个还没彻底倒下的姓氏，再多撑过些时日。若你能拿出四颗绿宝石和一颗钻石，我会把这份情记得比账本还清楚。",
                                "A little dignity has been gathered back up, and I have at least seen the shadow of what old houses once were. What remains now is simply helping a name that has not quite fallen endure a little longer. If you can spare four emeralds and a diamond, I will remember the favor more clearly than any ledger."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_noble_offer_fund",
                                                LocalizedText.of(
                                                        "拿去吧，至少别让你连最后的体面都输光",
                                                        "Take it. At least don't lose the last of your dignity"),
                                                DialogueOptionType.BRANCH,
                                                full,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasItem(EMERALD, 4),
                                                DialogueRequirementDef.hasItem(DIAMOND, 1))
                                        .withEffects(
                                                DialogueEffectDef.takeItem(EMERALD, 4),
                                                DialogueEffectDef.takeItem(DIAMOND, 1),
                                                DialogueEffectDef.setStoryFlag(NOBLE_FUND_FLAG),
                                                DialogueEffectDef.giveItem(NOBLE_CLOCK, 1),
                                                DialogueEffectDef.giveCoins(18),
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(3, 6)),
                                new DialogueOptionDef(
                                                "story_noble_settle_after_some_help",
                                                LocalizedText.of(
                                                        "你现在这样，也算勉强能在这里过下去",
                                                        "As you are now, you can at least manage a life here"),
                                                DialogueOptionType.BRANCH,
                                                partial,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasStoryFlag(NOBLE_ATTIRE_FLAG),
                                                DialogueRequirementDef.missingStoryFlag(NOBLE_FUND_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.giveItem(NOBLE_CLOCK, 1),
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(3, 6)),
                                new DialogueOptionDef(
                                                "story_noble_settle_after_photo_only",
                                                LocalizedText.of(
                                                        "至少你已经看见，这世上还留着些旧梦",
                                                        "At least you've seen that some old dreams still remain"),
                                                DialogueOptionType.BRANCH,
                                                partial,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.missingStoryFlag(NOBLE_ATTIRE_FLAG),
                                                DialogueRequirementDef.hasStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .NOBLE_MANSION_PHOTO_COMPLETED_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.giveItem(NOBLE_CLOCK, 1),
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(3, 6)),
                                new DialogueOptionDef(
                                                "story_noble_settle_without_help",
                                                LocalizedText.of(
                                                        "这地方大概比别处更适合你留下嘴硬",
                                                        "This place may suit your stubborn pride better than most"),
                                                DialogueOptionType.BRANCH,
                                                none,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.missingStoryFlag(NOBLE_ATTIRE_FLAG),
                                                DialogueRequirementDef.missingStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .NOBLE_MANSION_PHOTO_COMPLETED_FLAG),
                                                DialogueRequirementDef.missingStoryFlag(NOBLE_FUND_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(3, 6)))));
        nodes.put(
                full,
                new DialogueNodeDef(
                        full,
                        LocalizedText.of(
                                "很好。既然连最后那点周转也有了，我大概可以承认一件事：这家旅社比我原先看得起它。把这只旧怀表收好吧，它过去替我守过不少无谓的排场，如今留在你手里，反倒更像是时间终于花在了值得的人身上。",
                                "Good. Now that even the last of my working capital is secured, I can admit something: this inn has turned out to be worth more than I first allowed. Keep this old watch. It once kept time for a great deal of pointless ceremony. In your hands, perhaps time will finally be spent on someone worthy of it."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_noble_leave_after_full_reward",
                                        LocalizedText.of("那就别再拿这地方当临时落脚处了", "Then stop pretending this is only temporary"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                partial,
                new DialogueNodeDef(
                        partial,
                        LocalizedText.of(
                                "我本来以为自己只是暂住，可人一旦过惯了有人点灯、有人开门的日子，就很难继续把每个清晨都当作流亡。把这只旧怀表拿着吧，它在我身边太久了，如今倒像该换个人继续替我记着时辰。",
                                "I thought I was only staying temporarily, but once a person grows used to a place where someone lights the lamps and opens the door, it's hard to call every morning an exile. Take this old watch. It has stayed with me too long already. Perhaps it should let someone else keep the hour for me."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_noble_leave_after_partial_reward",
                                        LocalizedText.of("那就安心留下吧", "Then stay and make peace with it"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                none,
                new DialogueNodeDef(
                        none,
                        LocalizedText.of(
                                "别误会，我留下不是因为舍不得什么，只是懒得再去向另一群陌生人解释我是谁了。这地方至少还有椅子、灯火和不至于太失礼的晚餐。",
                                "Do not misunderstand. I am not staying because I feel sentiment for anything. I am merely too tired to explain myself to yet another room of strangers. This place at least has chairs, lamplight, and dinners that are not completely insulting."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_noble_leave_after_none_reward",
                                        LocalizedText.of("那就当你终于认命了", "Then let's call it acceptance at last"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition("story_noble_stage2", root, nodes);
    }

    private static DialogueDefinition buildNobleStage3Dialogue() {
        String root = "root";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我最近正重新学着把一天过得像个住客，而不是一位等着过去上门赔罪的人。说来可笑，这地方竟真有点让人待得住。",
                                "Lately I have been relearning how to live through a day like a resident, rather than a man waiting for the past to arrive and apologize. Ridiculous as it sounds, this place is becoming unexpectedly livable."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_noble_leave_final",
                                        LocalizedText.of("那你就继续把姿态端稳吧", "Then keep carrying yourself with what remains"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition("story_noble_stage3", root, nodes);
    }

    private static DialogueDefinition buildNobleVisitChatterDialogue() {
        String root = "root";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我今天路过前厅时，忽然觉得这里的钟声比许多旧宅还更像日子该有的样子。倒不是因为怀念，只是终于没那么吵了。",
                                "As I passed the front hall today, it struck me that the chime here sounds more like a proper day than many old estates ever did. Not because I am sentimental, of course. Merely because things have finally grown quieter."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_noble_leave_chatter",
                                        LocalizedText.of("少嘴硬两句会更像在过日子", "You'd sound even more at home if you argued less"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition(NOBLE_CHATTER_DIALOGUE_ID, root, nodes);
    }

    private static StoryGuestDefinition buildWanderingAlchemist() {
        String id = "wandering_alchemist";
        GuestEntity.GuestProfile profile =
                new GuestEntity.GuestProfile(
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(16, 24), new GuestData.IntRange(68, 84)),
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(12, 20), new GuestData.IntRange(56, 74)),
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(14, 24), new GuestData.IntRange(64, 82)),
                        new GuestData.IntRange(16, 18),
                        1.06D);
        List<DialogueDefinition> dialogues =
                List.of(
                        buildAlchemistStage0Dialogue(),
                        buildAlchemistStage1Dialogue(),
                        buildAlchemistStage2Dialogue(),
                        buildAlchemistStage3Dialogue(),
                        buildAlchemistVisitChatterDialogue());
        return new StoryGuestDefinition(
                id,
                LocalizedText.of("流浪炼金术师 米蕾雅", "Mireya the Wandering Alchemist"),
                TEXTURE_WANDERING_ALCHEMIST,
                "slim",
                0,
                profile,
                2,
                5,
                3,
                1,
                3,
                List.of(
                        new StoryGuestVisitOutcomeRule(ALCHEMIST_KEEPS_TRAVELING_FLAG, false),
                        new StoryGuestVisitOutcomeRule(ALCHEMIST_SETTLED_IN_INN_FLAG, true)),
                Map.of(
                        0, "story_alchemist_stage0",
                        1, "story_alchemist_stage1",
                        2, "story_alchemist_stage2",
                        3, "story_alchemist_stage3"),
                ALCHEMIST_CHATTER_DIALOGUE_ID,
                dialogues);
    }

    private static DialogueDefinition buildAlchemistStage0Dialogue() {
        String root = "root";
        String helped = "helped";
        String refused = "refused";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我这一路采来的样本，总在快抵达旅社前失去活性。若你愿意，给我两份下界疣、一份烈焰粉，再加一只玻璃瓶。我得先把配方重新稳定下来，才知道这地方值不值得我停火驻留。",
                                "The samples I gather on the road keep losing their potency just before I reach the inn. If you're willing, bring me two nether wart, a blaze powder, and a glass bottle. I need to stabilize the formula again before I can tell whether this place is worth banking my fire for." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_alchemist_offer_supplies",
                                                LocalizedText.of("拿去吧，先把你的配方稳住", "Take them, and steady your formula first"),
                                                DialogueOptionType.BRANCH,
                                                helped,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasItem(NETHER_WART, 2),
                                                DialogueRequirementDef.hasItem(BLAZE_POWDER, 1),
                                                DialogueRequirementDef.hasItem(GLASS_BOTTLE, 1))
                                        .withEffects(
                                                DialogueEffectDef.takeItem(NETHER_WART, 2),
                                                DialogueEffectDef.takeItem(BLAZE_POWDER, 1),
                                                DialogueEffectDef.takeItem(GLASS_BOTTLE, 1),
                                                DialogueEffectDef.setStoryFlag(ALCHEMIST_SUPPLIED_FLAG),
                                                DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                                "story_alchemist_decline_supplies",
                                                LocalizedText.of("这些材料我这回拿不出来", "I can't spare those materials this time"),
                                                DialogueOptionType.BRANCH,
                                                refused,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                        "story_alchemist_leave_intro",
                                        LocalizedText.of("等我备好材料再来", "Come back when I've gathered them"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                helped,
                new DialogueNodeDef(
                        helped,
                        LocalizedText.of(
                                "很好。火候、溶剂、容器，总算又凑齐了。接下来只差一个能让我校准热度的参照物。",
                                "Good. Heat, solvent, vessel. The balance is complete again. Now I only lack a reference point strong enough to calibrate the temperature." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_alchemist_leave_after_help",
                                                LocalizedText.of("那就等你下一次试验", "Then I'll wait for your next trial"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        nodes.put(
                refused,
                new DialogueNodeDef(
                        refused,
                        LocalizedText.of(
                                "也罢，我先拿残存的药性硬撑着看。只是这样调出来的结果，多半还不够让我安心。",
                                "So be it. I'll make do with whatever potency remains. But anything brewed under those conditions is unlikely to satisfy me." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_alchemist_leave_after_refuse",
                                                LocalizedText.of("那你自己小心些", "Then take care of yourself"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        return new DialogueDefinition("story_alchemist_stage0", root, nodes);
    }

    private static DialogueDefinition buildAlchemistStage1Dialogue() {
        String root = "root";
        String accepted = "accepted";
        String declined = "declined";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "材料的问题解决以后，我还差一张真正可靠的参照。若你路过下界堡垒，替我拍一张照片吧。我想看看那里的火与石，如今还能不能维持我需要的烈度。",
                                "With the materials sorted, I'm still missing a trustworthy reference. If you pass a Nether fortress, bring me a photograph. I want to see whether the fire and stone there can still hold the intensity I need." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_alchemist_accept_fortress_photo_task",
                                                LocalizedText.of("可以，我替你带回一张下界堡垒的照片", "I can do that. I'll bring you a photograph of a Nether fortress"),
                                                DialogueOptionType.BRANCH,
                                                accepted,
                                                null)
                                        .withEffects(
                                                DialogueEffectDef.setStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .ALCHEMIST_FORTRESS_PHOTO_REQUESTED_FLAG),
                                                DialogueEffectDef.advanceStoryStage(2)),
                                new DialogueOptionDef(
                                                "story_alchemist_decline_fortress_photo_task",
                                                LocalizedText.of("这趟下界之行我未必顾得上", "I may not manage a Nether journey this time"),
                                                DialogueOptionType.BRANCH,
                                                declined,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(2)))));
        nodes.put(
                accepted,
                new DialogueNodeDef(
                        accepted,
                        LocalizedText.of(
                                "不必拍得多完整。我只需要看见那种会让空气发烫的轮廓，以及火焰落在砖石上的颜色。",
                                "It doesn't need to be comprehensive. I only need the outline that makes the air seem to burn, and the color fire takes when it strikes those old bricks." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_alchemist_leave_after_accept_photo",
                                                LocalizedText.of("我记下了", "I'll remember that"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 4)))));
        nodes.put(
                declined,
                new DialogueNodeDef(
                        declined,
                        LocalizedText.of(
                                "没关系。我可以先把配方压着不出手，只是答案就得晚些才能见分晓。",
                                "That's alright. I can keep the formula corked for now. It just means the answer will take longer to reveal itself." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_alchemist_leave_after_decline_photo",
                                                LocalizedText.of("那就留到下次", "Then leave it for next time"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 4)))));
        return new DialogueDefinition("story_alchemist_stage1", root, nodes);
    }

    private static DialogueDefinition buildAlchemistStage2Dialogue() {
        String root = "root";
        String settled = "settled";
        String traveling = "traveling";
        String farewell = "farewell";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我已经把材料、火候和那张下界堡垒的参照都摆在桌上了。现在只差最后一步: 决定这瓶成品是该留在这里慢慢改良，还是继续跟着我去别处冒险。",
                                "I've laid out the materials, the heat curves, and that Nether fortress reference on the table. Only one step remains: deciding whether this finished draught should stay here for refinement, or keep wandering with me into the next danger." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_alchemist_reward_settle",
                                                LocalizedText.of(
                                                        "把它留在这里吧，你也留下",
                                                        "Leave it here, and stay with it"),
                                                DialogueOptionType.BRANCH,
                                                settled,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasStoryFlag(ALCHEMIST_SUPPLIED_FLAG),
                                                DialogueRequirementDef.hasStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .ALCHEMIST_FORTRESS_PHOTO_COMPLETED_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.giveItem(ALCHEMIST_POTION, 1),
                                                DialogueEffectDef.giveCoins(20),
                                                DialogueEffectDef.setStoryFlag(
                                                        ALCHEMIST_SETTLED_IN_INN_FLAG),
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(3, 6)),
                                new DialogueOptionDef(
                                                "story_alchemist_reward_travel_after_supplies",
                                                LocalizedText.of(
                                                        "至少你的药剂已经有了稳定的底子",
                                                        "At least your draught now has a stable foundation"),
                                                DialogueOptionType.BRANCH,
                                                traveling,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasStoryFlag(ALCHEMIST_SUPPLIED_FLAG),
                                                DialogueRequirementDef.missingStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .ALCHEMIST_FORTRESS_PHOTO_COMPLETED_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.giveItem(ALCHEMIST_POTION, 1),
                                                DialogueEffectDef.setStoryFlag(
                                                        ALCHEMIST_KEEPS_TRAVELING_FLAG),
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(4, 8)),
                                new DialogueOptionDef(
                                                "story_alchemist_reward_travel_after_photo",
                                                LocalizedText.of(
                                                        "至少你已经找回了想要的火候参照",
                                                        "At least you've recovered the heat reference you needed"),
                                                DialogueOptionType.BRANCH,
                                                traveling,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.missingStoryFlag(ALCHEMIST_SUPPLIED_FLAG),
                                                DialogueRequirementDef.hasStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .ALCHEMIST_FORTRESS_PHOTO_COMPLETED_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.giveItem(ALCHEMIST_POTION, 1),
                                                DialogueEffectDef.setStoryFlag(
                                                        ALCHEMIST_KEEPS_TRAVELING_FLAG),
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(4, 8)),
                                new DialogueOptionDef(
                                                "story_alchemist_reward_farewell",
                                                LocalizedText.of(
                                                        "看来这回还不足以让你停下实验",
                                                        "Looks like this still isn't enough to make you stop experimenting"),
                                                DialogueOptionType.BRANCH,
                                                farewell,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.missingStoryFlag(ALCHEMIST_SUPPLIED_FLAG),
                                                DialogueRequirementDef.missingStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .ALCHEMIST_FORTRESS_PHOTO_COMPLETED_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.setStoryFlag(
                                                        ALCHEMIST_KEEPS_TRAVELING_FLAG),
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(4, 8)))));
        nodes.put(
                settled,
                new DialogueNodeDef(
                        settled,
                        LocalizedText.of(
                                "那就这样吧。我把这瓶最稳定的成品先留给你，它足够证明这地方的火并不比下界差。至于我，也许该试试把实验台固定在一个不会每天换窗景的地方了。",
                                "Then so be it. I'll leave you the most stable batch I've made. It's proof enough that the fire here is no less dependable than the Nether's. As for me, perhaps it's time I tried keeping a laboratory where the view beyond the window doesn't change every day." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_alchemist_leave_after_settle_reward",
                                        LocalizedText.of("那我就等你下一次配出更好的成品", "Then I'll wait for your next improved batch"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                traveling,
                new DialogueNodeDef(
                        traveling,
                        LocalizedText.of(
                                "还差一点。我已经调出了能喝的答案，却还没调出让我安心停下的理由。把这瓶药剂收着吧，算是我把这一程最成功的结果留给了你。至于我，恐怕还得继续追着下一处火源走。",
                                "It's close, but not complete. I've brewed an answer worth drinking, yet not a reason strong enough to stay. Keep this draught. Consider it the finest result of this leg of my journey. As for me, I still need to chase the next source of fire." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_alchemist_leave_after_travel_reward",
                                        LocalizedText.of("愿你下一次找到真正想停下的地方", "May your next stop be the one you choose to keep"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                farewell,
                new DialogueNodeDef(
                        farewell,
                        LocalizedText.of(
                                "这回我没能从你这里取到足够的变量，也就暂时得不出结论。没关系，炼金术总会容许几次失败。我先继续上路，去别处把答案烧出来。",
                                "This time, I couldn't gather enough variables here to draw a conclusion. That's alright. Alchemy always allows for a few failed runs. I'll stay on the road a while longer and burn the answer out elsewhere." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_alchemist_leave_after_farewell",
                                        LocalizedText.of("愿你下次带着更好的结果回来", "May you return with a finer result next time"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition("story_alchemist_stage2", root, nodes);
    }

    private static DialogueDefinition buildAlchemistStage3Dialogue() {
        String root = "root";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我最近正试着把旅社里的炉火调成一种更适合长期观察的温度。这里没有下界那样暴躁，却意外地适合把复杂的结果慢慢熬出来。",
                                "Lately I've been tuning the inn's fire to a temperature better suited for long observation. It lacks the Nether's violence, but that turns out to be perfect for drawing out more complicated results." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_alchemist_leave_final",
                                        LocalizedText.of("那就把下一份成品也留在这里吧", "Then leave your next finished batch here too"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition("story_alchemist_stage3", root, nodes);
    }

    private static DialogueDefinition buildAlchemistVisitChatterDialogue() {
        String root = "root";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我今天在调一份不靠爆裂和灼烧见效的药。真正难的不是让它立刻有用，而是让它在该起作用的时候，永远都稳得住。",
                                "Today I'm tuning a draught that doesn't rely on burst or blaze to prove its worth. The hard part isn't making it useful immediately. It's making sure it stays reliable exactly when it needs to." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_alchemist_leave_chatter",
                                        LocalizedText.of("等你调好了让我也见识一下", "Let me see it when you've finished tuning it"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition(ALCHEMIST_CHATTER_DIALOGUE_ID, root, nodes);
    }

    private static StoryGuestDefinition buildArchaeologist() {
        String id = "archaeologist";
        GuestEntity.GuestProfile profile =
                new GuestEntity.GuestProfile(
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(16, 24), new GuestData.IntRange(70, 86)),
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(14, 22), new GuestData.IntRange(58, 74)),
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(10, 18), new GuestData.IntRange(52, 68)),
                        new GuestData.IntRange(15, 17),
                        1.04D);
        List<DialogueDefinition> dialogues =
                List.of(
                        buildArchaeologistStage0Dialogue(),
                        buildArchaeologistStage1Dialogue(),
                        buildArchaeologistStage2Dialogue(),
                        buildArchaeologistStage3Dialogue(),
                        buildArchaeologistVisitChatterDialogue());
        return new StoryGuestDefinition(
                id,
                LocalizedText.of("考古学者 索恩教授", "Professor Thorne the Archaeologist"),
                TEXTURE_ARCHAEOLOGIST,
                "default",
                0,
                profile,
                2,
                5,
                4,
                1,
                3,
                List.of(),
                Map.of(
                        0, "story_archaeologist_stage0",
                        1, "story_archaeologist_stage1",
                        2, "story_archaeologist_stage2",
                        3, "story_archaeologist_stage3"),
                ARCHAEOLOGIST_CHATTER_DIALOGUE_ID,
                dialogues);
    }

    private static DialogueDefinition buildArchaeologistStage0Dialogue() {
        String root = "root";
        String helped = "helped";
        String refused = "refused";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我几乎可以肯定，这片大陆埋着一层被人刻意遗忘的旧文明。可要把它们从土里请出来，我还差一把铁镐和十六支火把。若你肯借我这些工具，下一次我就能带着真正的线索回来。",
                                "I'm nearly certain this land hides a civilization that was deliberately forgotten. But if I'm to invite it back out of the soil, I still need an iron pickaxe and sixteen torches. Lend me those tools, and next time I'll return with something more than a theory." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_archaeologist_offer_tools",
                                                LocalizedText.of("这些工具你先拿去挖", "Take these tools and start digging"),
                                                DialogueOptionType.BRANCH,
                                                helped,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasItem(IRON_PICKAXE, 1),
                                                DialogueRequirementDef.hasItem(TORCH, 16))
                                        .withEffects(
                                                DialogueEffectDef.takeItem(IRON_PICKAXE, 1),
                                                DialogueEffectDef.takeItem(TORCH, 16),
                                                DialogueEffectDef.setStoryFlag(ARCHAEOLOGIST_TOOLS_FLAG),
                                                DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                                "story_archaeologist_decline_tools",
                                                LocalizedText.of("这套工具我暂时凑不出来", "I can't spare a set of tools right now"),
                                                DialogueOptionType.BRANCH,
                                                refused,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                        "story_archaeologist_leave_intro",
                                        LocalizedText.of("等我备好工具再来", "Come back when I've gathered them"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                helped,
                new DialogueNodeDef(
                        helped,
                        LocalizedText.of(
                                "很好。考古最怕的从来不是泥土，而是证据刚露头时手边什么都没有。你替我省下了不少愚蠢的耽搁。",
                                "Good. Archaeology rarely loses to dirt. It loses to the moment evidence breaks the surface and one has nothing in hand. You've spared me a great deal of foolish delay." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_archaeologist_leave_after_tools_help",
                                                LocalizedText.of("那就带着证据回来", "Then come back with evidence"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        nodes.put(
                refused,
                new DialogueNodeDef(
                        refused,
                        LocalizedText.of(
                                "也罢。我还能继续用旧办法慢慢挖，只是这样发现的东西，多半会先被黑暗和时间磨掉一层真相。",
                                "So be it. I can continue with the old methods, slowly. It only means whatever I find will lose another layer of truth to darkness and time before I reach it." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_archaeologist_leave_after_tools_refuse",
                                                LocalizedText.of("那你自己多加小心", "Then be careful down there"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        return new DialogueDefinition("story_archaeologist_stage0", root, nodes);
    }

    private static DialogueDefinition buildArchaeologistStage1Dialogue() {
        String root = "root";
        String accepted = "accepted";
        String declined = "declined";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "工具的事解决了，我还差一张足够服人的现场影像。若你路过沙漠神殿或丛林神庙，替我拍一张照片吧。很多时候，一处入口的朝向就足够推翻几十年的猜测。",
                                "Now that the tool problem is solved, I'm still missing field evidence convincing enough to show others. If you pass a desert pyramid or jungle temple, bring me a photograph. More than once, the orientation of an entrance has been enough to overturn decades of assumptions." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_archaeologist_accept_temple_photo_task",
                                                LocalizedText.of("可以，我替你带回一张遗迹照片", "I can do that. I'll bring back a ruin photograph"),
                                                DialogueOptionType.BRANCH,
                                                accepted,
                                                null)
                                        .withEffects(
                                                DialogueEffectDef.setStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .ARCHAEOLOGIST_TEMPLE_PHOTO_REQUESTED_FLAG),
                                                DialogueEffectDef.advanceStoryStage(2)),
                                new DialogueOptionDef(
                                                "story_archaeologist_decline_temple_photo_task",
                                                LocalizedText.of("这种遗迹我未必遇得上", "I may not run across such ruins"),
                                                DialogueOptionType.BRANCH,
                                                declined,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(2)))));
        nodes.put(
                accepted,
                new DialogueNodeDef(
                        accepted,
                        LocalizedText.of(
                                "不必追求完美角度。只要能看见那些石阶、入口或残垣如何埋进地势，我就能从里头读出不少东西。",
                                "No need to chase a perfect angle. If I can see how the steps, entrance, or broken walls settle into the terrain, I'll read quite a lot from that alone." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_archaeologist_leave_after_accept_photo",
                                                LocalizedText.of("我记下了", "I'll remember that"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 4)))));
        nodes.put(
                declined,
                new DialogueNodeDef(
                        declined,
                        LocalizedText.of(
                                "没关系，遗迹从不急着向人显露自己。我可以先把现有笔记整理完，等下一次机会真正开口。",
                                "That's alright. Ruins are never in a hurry to reveal themselves. I can finish arranging the notes I already have and wait until the next opportunity speaks more clearly." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_archaeologist_leave_after_decline_photo",
                                                LocalizedText.of("那就等下次发现", "Then wait for the next discovery"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 4)))));
        return new DialogueDefinition("story_archaeologist_stage1", root, nodes);
    }

    private static DialogueDefinition buildArchaeologistStage2Dialogue() {
        String root = "root";
        String full = "full";
        String relics = "relics";
        String evidence = "evidence";
        String none = "none";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我已经把图纸、勘测记录和你带回来的线索摊开了。如今若能再有一块回响碎片和一块远古残骸作样本，我就能把整套论证彻底钉死。即便你这回拿不来，我也不会停手，这地方已经让我闻到了历史翻身时的尘土味。",
                                "I've spread out the charts, survey notes, and whatever clues you've brought back. If I can add an echo shard and a piece of ancient debris as samples, I can finally pin the whole argument in place. Even if you cannot bring them this time, I won't stop. This place already smells like history turning in its sleep." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_archaeologist_reward_full",
                                                LocalizedText.of(
                                                        "最后的样本也在这里，你可以正式定论了",
                                                        "The final samples are here. You can make your case properly now"),
                                                DialogueOptionType.BRANCH,
                                                full,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasItem(ECHO_SHARD, 1),
                                                DialogueRequirementDef.hasItem(ANCIENT_DEBRIS, 1),
                                                DialogueRequirementDef.hasStoryFlag(ARCHAEOLOGIST_TOOLS_FLAG),
                                                DialogueRequirementDef.hasStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .ARCHAEOLOGIST_TEMPLE_PHOTO_COMPLETED_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.takeItem(ECHO_SHARD, 1),
                                                DialogueEffectDef.takeItem(ANCIENT_DEBRIS, 1),
                                                DialogueEffectDef.setStoryFlag(ARCHAEOLOGIST_RELICS_FLAG),
                                                DialogueEffectDef.giveItem(ARCHAEOLOGIST_BRUSH, 1),
                                                DialogueEffectDef.giveCoins(20),
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(3, 6)),
                                new DialogueOptionDef(
                                                "story_archaeologist_reward_relics_only",
                                                LocalizedText.of(
                                                        "样本我带来了，剩下的证据你慢慢补齐",
                                                        "I've brought the samples. You can fill in the rest of the evidence"),
                                                DialogueOptionType.BRANCH,
                                                relics,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasItem(ECHO_SHARD, 1),
                                                DialogueRequirementDef.hasItem(ANCIENT_DEBRIS, 1))
                                        .withEffects(
                                                DialogueEffectDef.takeItem(ECHO_SHARD, 1),
                                                DialogueEffectDef.takeItem(ANCIENT_DEBRIS, 1),
                                                DialogueEffectDef.setStoryFlag(ARCHAEOLOGIST_RELICS_FLAG),
                                                DialogueEffectDef.giveItem(ARCHAEOLOGIST_BRUSH, 1),
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(3, 6)),
                                new DialogueOptionDef(
                                                "story_archaeologist_reward_evidence_only",
                                                LocalizedText.of(
                                                        "你前面的研究已经够让我相信这里有东西",
                                                        "Your earlier research is enough to convince me there's something here"),
                                                DialogueOptionType.BRANCH,
                                                evidence,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasStoryFlag(ARCHAEOLOGIST_TOOLS_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(3, 6)),
                                new DialogueOptionDef(
                                                "story_archaeologist_reward_none",
                                                LocalizedText.of(
                                                        "看来这回还得你继续自己往下挖",
                                                        "Looks like you'll have to keep digging on your own this time"),
                                                DialogueOptionType.BRANCH,
                                                none,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.missingStoryFlag(
                                                        ARCHAEOLOGIST_TOOLS_FLAG),
                                                DialogueRequirementDef.missingStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .ARCHAEOLOGIST_TEMPLE_PHOTO_COMPLETED_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(3, 6)))));
        nodes.put(
                full,
                new DialogueNodeDef(
                        full,
                        LocalizedText.of(
                                "很好，这下就不是猜想了。工具、遗迹影像、还有真正从旧时代身上剥离下来的样本，已经足够让我把这份研究继续做下去。把这把旧刷子拿着吧，它陪我刷开过不少半真半假的历史，如今轮到你亲手去把别的答案从尘土里请出来了。",
                                "Excellent. This is no longer conjecture. Tools, ruin imagery, and samples stripped from the old age itself are enough to let me carry this work much further. Take this old brush. It has brushed aside many half-truths for me already. Now it's your turn to coax other answers out of the dust with your own hand." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_archaeologist_leave_after_full_reward",
                                        LocalizedText.of("那你就继续把这地方挖透吧", "Then keep digging this place all the way through"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                relics,
                new DialogueNodeDef(
                        relics,
                        LocalizedText.of(
                                "这两件样本已经很有分量了。虽然前面的证据还不够完整，但我可以先把研究继续下去。把这把旧刷子拿着，它比多数新工具更耐得住耐心，也更适合那些不肯轻易露面的历史。",
                                "These two samples already carry real weight. Even if the earlier evidence is incomplete, I can continue the research from here. Take this old brush. It endures patience better than most new tools, and it suits histories that refuse to reveal themselves too easily." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_archaeologist_leave_after_relic_reward",
                                        LocalizedText.of("那我就收下这份研究工具", "Then I'll keep this research tool safe"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                evidence,
                new DialogueNodeDef(
                        evidence,
                        LocalizedText.of(
                                "还差关键样本，但我已经没法说服自己离开了。越是证据不完整，越说明这里还埋着值得继续往下挖的东西。我会留下，直到把这片土翻出真正肯开口的历史为止。",
                                "The crucial samples are still missing, but I can no longer persuade myself to leave. The more incomplete the evidence remains, the more this place insists there is still something worth uncovering beneath it. I will stay until the soil gives up a history willing to speak plainly." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_archaeologist_leave_after_evidence_only",
                                        LocalizedText.of("那就别停手", "Then don't stop now"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                none,
                new DialogueNodeDef(
                        none,
                        LocalizedText.of(
                                "你这回没替我补上多少证据，但我反而更确定这里值得继续待下去。一个地方若连线索都这么难啃，往往正说明它埋的东西足够古，也足够真。",
                                "You didn't help me gather much evidence this time, but if anything, that only convinces me more strongly that this place is worth staying in. Whenever clues are this stubborn, it usually means whatever is buried here is both old enough and real enough to matter." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_archaeologist_leave_after_none",
                                        LocalizedText.of("那就继续跟这片土较劲吧", "Then keep wrestling answers out of this soil"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition("story_archaeologist_stage2", root, nodes);
    }

    private static DialogueDefinition buildArchaeologistStage3Dialogue() {
        String root = "root";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我最近在把旅社附近的地层、遗迹朝向和风化痕迹重新誊成一份更像样的记录。真正的历史从不怕被看得慢，它只怕没人肯一直看下去。",
                                "Lately I've been recopying the nearby strata, ruin orientations, and weathering marks into something more orderly. Real history never fears being read slowly. It only fears that no one will keep reading long enough." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_archaeologist_leave_final",
                                        LocalizedText.of("等你下一份笔记整理好", "Let me read the next set of notes when it's ready"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition("story_archaeologist_stage3", root, nodes);
    }

    private static DialogueDefinition buildArchaeologistVisitChatterDialogue() {
        String root = "root";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我今天又刷出了一层新的痕迹。大多数人只看见灰尘，可在我眼里，那是年代、工艺和某种还没来得及被忘干净的秩序。",
                                "Today I brushed free another layer of traces. Most people see only dust. I see age, workmanship, and a kind of order that hasn't quite finished being forgotten yet." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_archaeologist_leave_chatter",
                                        LocalizedText.of("那就继续把它们读出来", "Then keep reading them back into the light"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition(ARCHAEOLOGIST_CHATTER_DIALOGUE_ID, root, nodes);
    }

    private static StoryGuestDefinition buildGemMerchant() {
        String id = "gem_merchant";
        GuestEntity.GuestProfile profile =
                new GuestEntity.GuestProfile(
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(18, 26), new GuestData.IntRange(74, 90)),
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(16, 24), new GuestData.IntRange(64, 80)),
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(10, 20), new GuestData.IntRange(56, 72)),
                        new GuestData.IntRange(18, 20),
                        1.1D);
        List<DialogueDefinition> dialogues =
                List.of(
                        buildGemMerchantStage0Dialogue(),
                        buildGemMerchantStage1Dialogue(),
                        buildGemMerchantStage2Dialogue(),
                        buildGemMerchantStage3Dialogue(),
                        buildGemMerchantVisitChatterDialogue());
        return new StoryGuestDefinition(
                id,
                LocalizedText.of("宝石商人 杰玛", "Gemma the Gem Merchant"),
                TEXTURE_GEM_MERCHANT,
                "slim",
                0,
                profile,
                2,
                5,
                4,
                1,
                3,
                List.of(
                        new StoryGuestVisitOutcomeRule(GEM_MERCHANT_KEEPS_TRAVELING_FLAG, false),
                        new StoryGuestVisitOutcomeRule(GEM_MERCHANT_SETTLED_IN_INN_FLAG, true)),
                Map.of(
                        0, "story_gem_merchant_stage0",
                        1, "story_gem_merchant_stage1",
                        2, "story_gem_merchant_stage2",
                        3, "story_gem_merchant_stage3"),
                GEM_MERCHANT_CHATTER_DIALOGUE_ID,
                dialogues);
    }

    private static DialogueDefinition buildGemMerchantStage0Dialogue() {
        String root = "root";
        String helped = "helped";
        String refused = "refused";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "劫匪没能把我的命拿走，却把我压箱底的货车和样品箱拆得七零八落。若你肯给我八块皮革、十二根线和六块铁锭，我至少能把最重要的那几格货重新装回去。",
                                "The bandits failed to take my life, but they left my wagon and specimen chests in pieces. If you can spare eight leather, twelve string, and six iron ingots, I can at least rebuild the compartments that matter most." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_gem_merchant_offer_wagon_supplies",
                                                LocalizedText.of("拿去吧，先把你的货箱修起来", "Take them and rebuild your cases first"),
                                                DialogueOptionType.BRANCH,
                                                helped,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasItem(LEATHER, 8),
                                                DialogueRequirementDef.hasItem(STRING_ITEM, 12),
                                                DialogueRequirementDef.hasItem(IRON_INGOT, 6))
                                        .withEffects(
                                                DialogueEffectDef.takeItem(LEATHER, 8),
                                                DialogueEffectDef.takeItem(STRING_ITEM, 12),
                                                DialogueEffectDef.takeItem(IRON_INGOT, 6),
                                                DialogueEffectDef.setStoryFlag(GEM_MERCHANT_WAGON_FLAG),
                                                DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                                "story_gem_merchant_decline_wagon_supplies",
                                                LocalizedText.of("这些材料我现在拿不出来", "I can't spare those materials right now"),
                                                DialogueOptionType.BRANCH,
                                                refused,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                        "story_gem_merchant_leave_intro",
                                        LocalizedText.of("等我把材料凑齐再说", "I'll return when I can gather them"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                helped,
                new DialogueNodeDef(
                        helped,
                        LocalizedText.of(
                                "这就对了。货物真正值钱的部分，从来不是摆出来给人看的那一面，而是那只还没被摔坏的箱子里剩下了什么。",
                                "That's more like it. The truly valuable part of any cargo is never what sits in full display. It's what survived in the chest that wasn't smashed open." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_gem_merchant_leave_after_wagon_help",
                                                LocalizedText.of("那你最好把剩下的也护好", "Then you had better guard the rest well"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        nodes.put(
                refused,
                new DialogueNodeDef(
                        refused,
                        LocalizedText.of(
                                "也罢，商人从不只靠一架完好的车活下去。我还能先用残存的样品撑场面，只是这会让我更谨慎地挑选该在哪落脚。",
                                "So be it. A merchant never survives on the promise of a single intact wagon alone. I can make do with the samples I have left, but it will make me that much more careful about where I decide to settle." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_gem_merchant_leave_after_wagon_refuse",
                                                LocalizedText.of("那你就继续算清楚风险", "Then keep your risks measured"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        return new DialogueDefinition("story_gem_merchant_stage0", root, nodes);
    }

    private static DialogueDefinition buildGemMerchantStage1Dialogue() {
        String root = "root";
        String accepted = "accepted";
        String declined = "declined";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "光把货箱修好还不够，我还得知道这地方值不值得长期押货。若你进过地下洞窟，替我拍一张照片吧。我想看看这里的矿脉气息，是零星的小运气，还是足够支撑一门长久生意的底子。",
                                "Repairing my cases isn't enough. I still need to know whether this region is worth staking goods on for the long term. If you've been down into the caves, bring me a photograph. I want to see whether the mineral character of this land is merely a streak of luck, or the kind of foundation that can support real trade." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_gem_merchant_accept_ore_photo_task",
                                                LocalizedText.of("可以，我替你带回一张矿洞照片", "I can do that. I'll bring you a cave photograph"),
                                                DialogueOptionType.BRANCH,
                                                accepted,
                                                null)
                                        .withEffects(
                                                DialogueEffectDef.setStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .GEM_MERCHANT_ORE_PHOTO_REQUESTED_FLAG),
                                                DialogueEffectDef.advanceStoryStage(2)),
                                new DialogueOptionDef(
                                                "story_gem_merchant_decline_ore_photo_task",
                                                LocalizedText.of("这趟我未必能下到地下去", "I may not make it underground this trip"),
                                                DialogueOptionType.BRANCH,
                                                declined,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(2)))));
        nodes.put(
                accepted,
                new DialogueNodeDef(
                        accepted,
                        LocalizedText.of(
                                "不用刻意找最亮的矿脉。只要那地方看上去像矿石和风险都愿意往深处继续延伸，我就能从里头看出值不值得下注。",
                                "No need to seek the brightest seam. If the place looks like both ore and risk are willing to keep descending deeper, I'll be able to tell whether it's worth betting on." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_gem_merchant_leave_after_accept_photo",
                                                LocalizedText.of("我记下了", "I'll remember that"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 4)))));
        nodes.put(
                declined,
                new DialogueNodeDef(
                        declined,
                        LocalizedText.of(
                                "无妨。谨慎本就是做生意的一部分。我可以先把账本摊开，等下次再决定这地方究竟值不值得让我停留得更久。",
                                "No matter. Caution is part of trade. I can open my ledger for now and decide next time whether this place truly deserves a longer stay from me." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_gem_merchant_leave_after_decline_photo",
                                                LocalizedText.of("那就等你下次再算", "Then do the accounting next time"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 4)))));
        return new DialogueDefinition("story_gem_merchant_stage1", root, nodes);
    }

    private static DialogueDefinition buildGemMerchantStage2Dialogue() {
        String root = "root";
        String settled = "settled";
        String traveling = "traveling";
        String farewell = "farewell";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我已经把账本、样品箱和你的那份地下影像对在一起了。现在的问题只剩一个: 这里究竟配不配让我把真正压箱底的货留下来，顺便也把我自己留下来。",
                                "I've set my ledger, specimen cases, and the underground image you brought me beside one another. Only one question remains now: is this place worthy of the finest cargo I keep in reserve, and perhaps worthy of keeping me with it." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_gem_merchant_reward_settle",
                                                LocalizedText.of(
                                                        "这里值得你长期押货，也值得你留下",
                                                        "This place is worth staking goods on, and worth your staying"),
                                                DialogueOptionType.BRANCH,
                                                settled,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasStoryFlag(GEM_MERCHANT_WAGON_FLAG),
                                                DialogueRequirementDef.hasStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .GEM_MERCHANT_ORE_PHOTO_COMPLETED_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.giveItem(GEM_MERCHANT_STAR, 1),
                                                DialogueEffectDef.giveCoins(24),
                                                DialogueEffectDef.setStoryFlag(
                                                        GEM_MERCHANT_SETTLED_IN_INN_FLAG),
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(4, 7)),
                                new DialogueOptionDef(
                                                "story_gem_merchant_reward_travel_after_wagon",
                                                LocalizedText.of(
                                                        "你的货已经能重新上路了，但我还没替你看出更稳的矿源",
                                                        "Your goods can travel again, but I haven't proven a steadier mineral source for you"),
                                                DialogueOptionType.BRANCH,
                                                traveling,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasStoryFlag(GEM_MERCHANT_WAGON_FLAG),
                                                DialogueRequirementDef.missingStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .GEM_MERCHANT_ORE_PHOTO_COMPLETED_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.giveItem(GEM_MERCHANT_STAR, 1),
                                                DialogueEffectDef.setStoryFlag(
                                                        GEM_MERCHANT_KEEPS_TRAVELING_FLAG),
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(5, 9)),
                                new DialogueOptionDef(
                                                "story_gem_merchant_reward_travel_after_photo",
                                                LocalizedText.of(
                                                        "你已经看见这地方的矿脉底子，但行商的根基还不够稳",
                                                        "You've seen the mineral promise here, but the merchant's footing is still unsteady"),
                                                DialogueOptionType.BRANCH,
                                                traveling,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.missingStoryFlag(GEM_MERCHANT_WAGON_FLAG),
                                                DialogueRequirementDef.hasStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .GEM_MERCHANT_ORE_PHOTO_COMPLETED_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.giveItem(GEM_MERCHANT_STAR, 1),
                                                DialogueEffectDef.setStoryFlag(
                                                        GEM_MERCHANT_KEEPS_TRAVELING_FLAG),
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(5, 9)),
                                new DialogueOptionDef(
                                                "story_gem_merchant_reward_farewell",
                                                LocalizedText.of(
                                                        "看来你还得继续替自己寻找下一处落脚点",
                                                        "Looks like you'll need to keep looking for your next foothold"),
                                                DialogueOptionType.BRANCH,
                                                farewell,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.missingStoryFlag(GEM_MERCHANT_WAGON_FLAG),
                                                DialogueRequirementDef.missingStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .GEM_MERCHANT_ORE_PHOTO_COMPLETED_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.giveItem(GEM_MERCHANT_STAR, 1),
                                                DialogueEffectDef.setStoryFlag(
                                                        GEM_MERCHANT_KEEPS_TRAVELING_FLAG),
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(5, 9)))));
        nodes.put(
                settled,
                new DialogueNodeDef(
                        settled,
                        LocalizedText.of(
                                "这就够了。货箱重新立起来了，矿脉的气息也没让我失望。把这颗星核收好吧，它本来是我绝不会轻易摆上柜台的压箱货。既然我决定在这里久留，它留在你手里反而像是一种比交易更正式的信任。",
                                "That will do. My cases stand upright again, and the mineral promise here hasn't disappointed me. Take this star core. It's the sort of reserve piece I would never ordinarily place on a counter. If I am staying here for the long haul, then leaving it in your hands feels like a trust more formal than any trade." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_gem_merchant_leave_after_settle_reward",
                                        LocalizedText.of("那就把你真正的生意也开在这里吧", "Then open your true business here as well"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                traveling,
                new DialogueNodeDef(
                        traveling,
                        LocalizedText.of(
                                "还差一点。你帮我保住了一部分本钱，也让我看见了这里的潜力，可还不足以让我把全部账本都押在这间旅社上。把这颗星核收着吧，算是我对你眼光的认可。至于我，还得继续去别处比较更好的报价与落脚点。",
                                "It's close, but not enough. You've helped me preserve part of my capital, and you've shown me this place has promise, but not enough to make me stake every ledger I own on this inn. Keep this star core. Consider it my acknowledgment of your eye for value. As for me, I still need to compare better offers and footholds elsewhere." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_gem_merchant_leave_after_travel_reward",
                                        LocalizedText.of("愿你下一笔交易更合算", "May your next deal prove the better one"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                farewell,
                new DialogueNodeDef(
                        farewell,
                        LocalizedText.of(
                                "你这回没替我补上多少筹码，但我并不打算空手道别。把这颗星核拿去吧。真正识货的人不一定总能成交，可我至少能认出谁有资格看见我压箱底的东西。至于我，还得继续走，直到找到更适合长住的柜台与灯火。",
                                "You didn't add much to my hand this time, but I don't intend to leave empty of courtesy. Take this star core. A discerning eye does not always close the deal, but I can at least recognize who deserves to see what I keep in reserve. As for me, I must keep moving until I find a counter and lamplight better suited to a longer stay." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_gem_merchant_leave_after_farewell",
                                        LocalizedText.of("愿你下一站真能让你停得住", "May your next stop be one that finally holds you"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition("story_gem_merchant_stage2", root, nodes);
    }

    private static DialogueDefinition buildGemMerchantStage3Dialogue() {
        String root = "root";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我最近在重新整理一批以前绝不会轻易亮出来的货。这里的人看东西不像只看价格，这倒让我愿意把一些真正值钱的东西拿出来慢慢谈。",
                                "Lately I've been reorganizing a portion of my stock I would never once have displayed lightly. People here don't seem to look only at price, and that makes me more willing to bring out goods that are actually worth discussing." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_gem_merchant_leave_final",
                                        LocalizedText.of("那我等你把真正的好货摆上来", "Then I'll wait to see the true quality on display"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition("story_gem_merchant_stage3", root, nodes);
    }

    private static DialogueDefinition buildGemMerchantVisitChatterDialogue() {
        String root = "root";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我今天在核对几笔旧账。做生意最难的从来不是认出值钱的东西，而是认出什么人和什么地方，值得把值钱的东西交出去。",
                                "Today I'm reconciling a few old accounts. The hardest part of trade is never recognizing what is valuable. It's recognizing which people and places are worth entrusting valuable things to." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_gem_merchant_leave_chatter",
                                        LocalizedText.of("看来你已经开始把这里算进账里了", "Sounds like you've started entering this place into your books"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition(GEM_MERCHANT_CHATTER_DIALOGUE_ID, root, nodes);
    }

    private static StoryGuestDefinition buildOldKnight() {
        String id = "old_knight";
        GuestEntity.GuestProfile profile =
                new GuestEntity.GuestProfile(
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(18, 26), new GuestData.IntRange(72, 88)),
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(14, 22), new GuestData.IntRange(60, 76)),
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(8, 16), new GuestData.IntRange(52, 68)),
                        new GuestData.IntRange(16, 18),
                        1.08D);
        List<DialogueDefinition> dialogues =
                List.of(
                        buildOldKnightStage0Dialogue(),
                        buildOldKnightStage1Dialogue(),
                        buildOldKnightStage2Dialogue(),
                        buildOldKnightVisitChatterDialogue());
        return new StoryGuestDefinition(
                id,
                LocalizedText.of("老骑士 奥德里克", "Sir Aldric, the Old Knight"),
                TEXTURE_OLD_KNIGHT,
                "default",
                0,
                profile,
                2,
                5,
                2,
                1,
                2,
                List.of(
                        new StoryGuestVisitOutcomeRule(OLD_KNIGHT_MOVED_ON_FLAG, false),
                        new StoryGuestVisitOutcomeRule(OLD_KNIGHT_SETTLED_IN_INN_FLAG, true)),
                Map.of(
                        0, "story_old_knight_stage0",
                        1, "story_old_knight_stage1",
                        2, "story_old_knight_stage2"),
                OLD_KNIGHT_CHATTER_DIALOGUE_ID,
                dialogues);
    }

    private static DialogueDefinition buildOldKnightStage0Dialogue() {
        String root = "root";
        String helped = "helped";
        String refused = "refused";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我的旧盾还撑得住，但绑带、护缘和披挂都快散了。若你愿意给我六块铁锭、四块皮革和八团红羊毛，我至少还能把这副老样子重新拾起来。",
                                "My old shield still holds, but the straps, edging, and surcoat are all coming apart. If you can spare six iron ingots, four leather, and eight bundles of red wool, I can at least gather the old shape of myself together again." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_old_knight_offer_repairs",
                                                LocalizedText.of("拿去吧，把你的旧行头收拾起来", "Take them and set your old kit in order"),
                                                DialogueOptionType.BRANCH,
                                                helped,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasItem(IRON_INGOT, 6),
                                                DialogueRequirementDef.hasItem(LEATHER, 4),
                                                DialogueRequirementDef.hasItem(RED_WOOL, 8))
                                        .withEffects(
                                                DialogueEffectDef.takeItem(IRON_INGOT, 6),
                                                DialogueEffectDef.takeItem(LEATHER, 4),
                                                DialogueEffectDef.takeItem(RED_WOOL, 8),
                                                DialogueEffectDef.setStoryFlag(OLD_KNIGHT_REARMED_FLAG),
                                                DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                                "story_old_knight_decline_repairs",
                                                LocalizedText.of("这些材料我现在拿不出来", "I can't spare those materials right now"),
                                                DialogueOptionType.BRANCH,
                                                refused,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                        "story_old_knight_leave_intro",
                                        LocalizedText.of("等我准备好了再来", "Come back when I've prepared them"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                helped,
                new DialogueNodeDef(
                        helped,
                        LocalizedText.of(
                                "这就够了。人老了以后，能把旧装备重新穿稳，有时比赢下一场仗更难。",
                                "That will do. When a man grows old, settling his old equipment properly can be harder than winning a battle ever was." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_old_knight_leave_after_help",
                                                LocalizedText.of("那就等你整理完回来", "Then return once you've set yourself in order"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        nodes.put(
                refused,
                new DialogueNodeDef(
                        refused,
                        LocalizedText.of(
                                "也罢。老兵总得学会在缺口里继续站着，只是这样的人，通常不会停留太久。",
                                "So be it. An old soldier must learn to stand even while things are missing. Men who live that way rarely linger anywhere for long." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_old_knight_leave_after_refuse",
                                                LocalizedText.of("愿你还能站得稳", "May you keep your footing"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        return new DialogueDefinition("story_old_knight_stage0", root, nodes);
    }

    private static DialogueDefinition buildOldKnightStage1Dialogue() {
        String root = "root";
        String settled = "settled";
        String traveling = "traveling";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "如今这副旧装备总算又能见人了。剩下的问题只在于，我该不该继续把它背去下一段路，还是把它和我自己一并留在一个还有灯火、还有门会为旅人打开的地方。",
                                "Now this old set of gear is fit to be seen again. The only question left is whether I should carry it into another stretch of road, or leave it and myself in a place that still keeps lamplight and doors open for travelers." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_old_knight_reward_settle",
                                                LocalizedText.of(
                                                        "这里已经够你歇脚了，把盾留在这里吧",
                                                        "This place is enough for your rest. Leave the shield here"),
                                                DialogueOptionType.BRANCH,
                                                settled,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasStoryFlag(OLD_KNIGHT_REARMED_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.giveItem(OLD_KNIGHT_SHIELD, 1),
                                                DialogueEffectDef.giveCoins(14),
                                                DialogueEffectDef.setStoryFlag(
                                                        OLD_KNIGHT_SETTLED_IN_INN_FLAG),
                                                DialogueEffectDef.advanceStoryStage(2),
                                                DialogueEffectDef.setNextVisitRange(3, 6)),
                                new DialogueOptionDef(
                                                "story_old_knight_reward_travel",
                                                LocalizedText.of(
                                                        "看来你还是得继续走下去",
                                                        "It seems you still need to keep walking"),
                                                DialogueOptionType.BRANCH,
                                                traveling,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.missingStoryFlag(
                                                        OLD_KNIGHT_REARMED_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.setStoryFlag(OLD_KNIGHT_MOVED_ON_FLAG),
                                                DialogueEffectDef.advanceStoryStage(2),
                                                DialogueEffectDef.setNextVisitRange(4, 8)))));
        nodes.put(
                settled,
                new DialogueNodeDef(
                        settled,
                        LocalizedText.of(
                                "也好。把这面旧徽盾拿着吧，它陪我挡过太多早该结束的风浪。若它今后还要继续起作用，我倒宁可它守着一个值得我回来的地方。",
                                "Very well. Take this old crested shield. It has weathered too many storms that should have ended long ago. If it is to keep serving any purpose, I'd rather it guard a place worth returning to." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_old_knight_leave_after_settle_reward",
                                        LocalizedText.of("那你就安心在这里歇一阵", "Then stay and rest here a while"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                traveling,
                new DialogueNodeDef(
                        traveling,
                        LocalizedText.of(
                                "我还没到能卸甲的时候。没关系，老兵本就该继续和路上的风一起耗下去。若哪天我真厌倦了，再回来敲你的门。",
                                "I'm not yet ready to set my armor aside. That's alright. Old soldiers are meant to wear themselves down alongside the road's wind. If the day comes when I truly tire of it, I'll knock on your door again." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_old_knight_leave_after_travel",
                                        LocalizedText.of("愿你下次敲门时不用再流浪", "May you not still be wandering next time you knock"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition("story_old_knight_stage1", root, nodes);
    }

    private static DialogueDefinition buildOldKnightStage2Dialogue() {
        String root = "root";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我今天把盾牌立在门边时，忽然觉得它看上去更像守卫，而不是负担。看来人到了一定年纪，终于会明白该把哪些东西放下，哪些东西留下。",
                                "When I set the shield by the door today, it struck me that it looked more like a guard than a burden. Perhaps at a certain age, a man finally learns what he should put down and what he should leave in place." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_old_knight_leave_final",
                                        LocalizedText.of("那就让它在这里继续守着吧", "Then let it keep watch here"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition("story_old_knight_stage2", root, nodes);
    }

    private static DialogueDefinition buildOldKnightVisitChatterDialogue() {
        String root = "root";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我最近在教自己重新习惯没有军号催促的早晨。说来奇怪，旅社里这种慢下来的日子，反倒比很多胜仗都更难得。",
                                "Lately I've been teaching myself to accept mornings without a horn calling me on. Strange thing, these slower days in the inn feel rarer than many victories ever did." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_old_knight_leave_chatter",
                                        LocalizedText.of("那就把今天当作赢下来的安稳", "Then count today as a peace you've earned"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition(OLD_KNIGHT_CHATTER_DIALOGUE_ID, root, nodes);
    }

    private static StoryGuestDefinition buildCursedAdventurer() {
        String id = "cursed_adventurer";
        GuestEntity.GuestProfile profile =
                new GuestEntity.GuestProfile(
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(14, 22), new GuestData.IntRange(66, 82)),
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(12, 20), new GuestData.IntRange(58, 74)),
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(8, 18), new GuestData.IntRange(50, 66)),
                        new GuestData.IntRange(14, 16),
                        1.05D);
        List<DialogueDefinition> dialogues =
                List.of(
                        buildCursedAdventurerStage0Dialogue(),
                        buildCursedAdventurerStage1Dialogue(),
                        buildCursedAdventurerStage2Dialogue(),
                        buildCursedAdventurerVisitChatterDialogue());
        return new StoryGuestDefinition(
                id,
                LocalizedText.of("被诅咒的冒险者 凯尔", "Kael the Cursed Adventurer"),
                TEXTURE_CURSED_ADVENTURER,
                "default",
                0,
                profile,
                2,
                5,
                2,
                1,
                2,
                List.of(
                        new StoryGuestVisitOutcomeRule(CURSED_ADVENTURER_LEFT_FLAG, false),
                        new StoryGuestVisitOutcomeRule(CURSED_ADVENTURER_STAYED_FLAG, true)),
                Map.of(
                        0, "story_cursed_adventurer_stage0",
                        1, "story_cursed_adventurer_stage1",
                        2, "story_cursed_adventurer_stage2"),
                CURSED_ADVENTURER_CHATTER_DIALOGUE_ID,
                dialogues);
    }

    private static DialogueDefinition buildCursedAdventurerStage0Dialogue() {
        String root = "root";
        String helped = "helped";
        String refused = "refused";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我身上的诅咒最近又开始发作了。牛奶能暂时压下去，金苹果能把命续住，发酵蛛眼则能让我把那股反咬回来的药性重新调平。若你愿意，把这三样给我，我至少还能撑到下次太阳出来。",
                                "The curse on me has started flaring up again. Milk suppresses it for a while, a golden apple keeps my body from giving out, and a fermented spider eye lets me rebalance the backlash in the mixture. If you can spare all three, I can at least hold together until the next sunrise." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cursed_adventurer_offer_cure",
                                                LocalizedText.of("拿去吧，先把命保住", "Take them and keep yourself alive first"),
                                                DialogueOptionType.BRANCH,
                                                helped,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasItem(MILK_BUCKET, 1),
                                                DialogueRequirementDef.hasItem(GOLDEN_APPLE, 1),
                                                DialogueRequirementDef.hasItem(FERMENTED_SPIDER_EYE, 1))
                                        .withEffects(
                                                DialogueEffectDef.takeItem(MILK_BUCKET, 1),
                                                DialogueEffectDef.takeItem(GOLDEN_APPLE, 1),
                                                DialogueEffectDef.takeItem(FERMENTED_SPIDER_EYE, 1),
                                                DialogueEffectDef.setStoryFlag(CURSED_ADVENTURER_CURE_FLAG),
                                                DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                                "story_cursed_adventurer_decline_cure",
                                                LocalizedText.of("这些东西我现在凑不齐", "I can't gather all that right now"),
                                                DialogueOptionType.BRANCH,
                                                refused,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                        "story_cursed_adventurer_leave_intro",
                                        LocalizedText.of("等我找齐了再来", "Come back when I've found them"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                helped,
                new DialogueNodeDef(
                        helped,
                        LocalizedText.of(
                                "够了，至少今晚那东西不会在我骨头里继续啃。等我把这口气喘匀，再决定自己究竟该留下还是继续逃。",
                                "That's enough. At least tonight that thing won't keep gnawing through my bones. Once I can breathe steadily again, I'll decide whether I should stay or keep running." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cursed_adventurer_leave_after_help",
                                                LocalizedText.of("那就先别死在路上", "Then don't go dying on the road"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        nodes.put(
                refused,
                new DialogueNodeDef(
                        refused,
                        LocalizedText.of(
                                "明白了。我还能再扛一阵，只是每多撑一天，下一段路就更像是在和自己赌命。",
                                "I understand. I can endure a little longer, but every extra day I force through makes the next stretch of road feel more like a wager against my own life." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cursed_adventurer_leave_after_refuse",
                                                LocalizedText.of("愿你还能等到下一次机会", "May you last until your next chance"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        return new DialogueDefinition("story_cursed_adventurer_stage0", root, nodes);
    }

    private static DialogueDefinition buildCursedAdventurerStage1Dialogue() {
        String root = "root";
        String stayed = "stayed";
        String left = "left";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我把你给我的东西都用上了，至少短时间里那诅咒没能继续把我往地里拖。现在我能选了: 留在一个总算有机会让人喘口气的地方，还是继续把自己扔给下一段不知会不会要命的路。",
                                "I've used everything you gave me, and for the moment the curse can no longer drag me toward the grave. Now I have a choice: remain in a place that finally lets a man catch his breath, or throw myself into another road that may yet kill me." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cursed_adventurer_reward_stay",
                                                LocalizedText.of(
                                                        "先留下吧，把命保住比赶路更重要",
                                                        "Stay for now. Keeping your life matters more than the road"),
                                                DialogueOptionType.BRANCH,
                                                stayed,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasStoryFlag(
                                                        CURSED_ADVENTURER_CURE_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.giveItem(CURSED_ADVENTURER_APPLE, 1),
                                                DialogueEffectDef.giveCoins(10),
                                                DialogueEffectDef.setStoryFlag(
                                                        CURSED_ADVENTURER_STAYED_FLAG),
                                                DialogueEffectDef.advanceStoryStage(2),
                                                DialogueEffectDef.setNextVisitRange(3, 6)),
                                new DialogueOptionDef(
                                                "story_cursed_adventurer_reward_leave",
                                                LocalizedText.of(
                                                        "看来你还是得继续把解法往远处找",
                                                        "Looks like you'll still need to chase your cure elsewhere"),
                                                DialogueOptionType.BRANCH,
                                                left,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.missingStoryFlag(
                                                        CURSED_ADVENTURER_CURE_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.setStoryFlag(
                                                        CURSED_ADVENTURER_LEFT_FLAG),
                                                DialogueEffectDef.advanceStoryStage(2),
                                                DialogueEffectDef.setNextVisitRange(4, 8)))));
        nodes.put(
                stayed,
                new DialogueNodeDef(
                        stayed,
                        LocalizedText.of(
                                "也许你说得对。把这颗金苹果拿着吧，它是我这一路一直舍不得真正吃掉的那一颗。若我真决定留下，总该有件东西替我记住，是谁让我第一次觉得自己不必再一味往前逃。",
                                "Perhaps you're right. Take this golden apple. It's the one I never quite allowed myself to consume on the road. If I truly stay, then something should remember who first made me feel I didn't need to keep fleeing forever." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_cursed_adventurer_leave_after_stay_reward",
                                        LocalizedText.of("那就先活下来，再谈别的", "Then survive first and worry about the rest later"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                left,
                new DialogueNodeDef(
                        left,
                        LocalizedText.of(
                                "我还没资格在这种地方停下。若哪天我真找到了压住这诅咒的办法，再回来告诉你，这一路到底是怎么熬过来的。",
                                "I haven't yet earned the right to stop in a place like this. If the day comes when I truly find a way to hold this curse down, I'll return and tell you how I survived the road in between." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_cursed_adventurer_leave_after_leave",
                                        LocalizedText.of("愿你别再被它追上", "May it never catch up to you again"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition("story_cursed_adventurer_stage1", root, nodes);
    }

    private static DialogueDefinition buildCursedAdventurerStage2Dialogue() {
        String root = "root";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我最近终于能在夜里睡得更久一点了。诅咒没消失，只是它第一次没能比我的意志更响。对现在的我来说，这已经像是捡回半条命。",
                                "Lately I've finally managed to sleep a little longer through the night. The curse hasn't vanished, but for the first time it hasn't spoken louder than my own will. For someone like me, that already feels like recovering half a life." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_cursed_adventurer_leave_final",
                                        LocalizedText.of("那就慢慢把剩下半条也活回来", "Then take your time and live the other half back too"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition("story_cursed_adventurer_stage2", root, nodes);
    }

    private static DialogueDefinition buildCursedAdventurerVisitChatterDialogue() {
        String root = "root";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我今天状态还算稳。对别人来说，这大概只是普通的一天；对我来说，却已经像命运终于肯松开一点手。",
                                "I'm holding steady today. To most people that would only mean an ordinary day. To me, it feels like fate finally loosened its grip a little." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_cursed_adventurer_leave_chatter",
                                        LocalizedText.of("那就把这种普通日子多过几天", "Then live through a few more ordinary days like this"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition(CURSED_ADVENTURER_CHATTER_DIALOGUE_ID, root, nodes);
    }

    private static StoryGuestDefinition buildOldAngler() {
        String id = "old_angler";
        GuestEntity.GuestProfile profile =
                new GuestEntity.GuestProfile(
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(16, 24), new GuestData.IntRange(68, 84)),
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(14, 22), new GuestData.IntRange(60, 76)),
                        new GuestEntity.PreferenceRangeProfile(
                                new GuestData.IntRange(10, 18), new GuestData.IntRange(56, 70)),
                        new GuestData.IntRange(15, 17),
                        1.03D);
        List<DialogueDefinition> dialogues =
                List.of(
                        buildOldAnglerStage0Dialogue(),
                        buildOldAnglerStage1Dialogue(),
                        buildOldAnglerStage2Dialogue(),
                        buildOldAnglerVisitChatterDialogue());
        return new StoryGuestDefinition(
                id,
                LocalizedText.of("钓鱼大师 老许", "Old Xu the Angler"),
                TEXTURE_OLD_ANGLER,
                "default",
                0,
                profile,
                2,
                5,
                1,
                1,
                2,
                List.of(),
                Map.of(
                        0, "story_old_angler_stage0",
                        1, "story_old_angler_stage1",
                        2, "story_old_angler_stage2"),
                OLD_ANGLER_CHATTER_DIALOGUE_ID,
                dialogues);
    }

    private static DialogueDefinition buildOldAnglerStage0Dialogue() {
        String root = "root";
        String helped = "helped";
        String refused = "refused";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我想试一锅真正配得上传说之鱼名头的杂鱼汤，还差些像样的底子。给我八条鳕鱼、八条鲑鱼，再加一条河豚，我就能看看这地方到底值不值得我把鱼竿久挂在岸边。",
                                "I'm trying to work up a proper broth worthy of legendary fish, but the base is still lacking. Bring me eight cod, eight salmon, and a single pufferfish, and I'll see whether this place is worth hanging my rod beside for longer than a night." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_old_angler_offer_catch",
                                                LocalizedText.of("拿去吧，看看能不能熬出你想要的味道", "Take them and see if you can boil the flavor you're after"),
                                                DialogueOptionType.BRANCH,
                                                helped,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasItem(COD, 8),
                                                DialogueRequirementDef.hasItem(SALMON, 8),
                                                DialogueRequirementDef.hasItem(PUFFERFISH, 1))
                                        .withEffects(
                                                DialogueEffectDef.takeItem(COD, 8),
                                                DialogueEffectDef.takeItem(SALMON, 8),
                                                DialogueEffectDef.takeItem(PUFFERFISH, 1),
                                                DialogueEffectDef.setStoryFlag(OLD_ANGLER_CATCH_FLAG),
                                                DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                                "story_old_angler_decline_catch",
                                                LocalizedText.of("这些鱼我这回凑不齐", "I can't gather that catch this time"),
                                                DialogueOptionType.BRANCH,
                                                refused,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                        "story_old_angler_leave_intro",
                                        LocalizedText.of("等我下次带鱼来", "I'll return with the fish next time"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                helped,
                new DialogueNodeDef(
                        helped,
                        LocalizedText.of(
                                "这才像样。鱼汤和钓鱼一样，最难的从来不是钓上来什么，而是懂得什么该留下、什么该慢慢熬。",
                                "Now that's proper. Broth is like fishing. The hard part is never what you pull up, but knowing what ought to be kept and what ought to be simmered slowly." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_old_angler_leave_after_help",
                                                LocalizedText.of("那我等你下一锅开盖", "Then I'll wait for the next pot to open"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        nodes.put(
                refused,
                new DialogueNodeDef(
                        refused,
                        LocalizedText.of(
                                "没事，老钓手总得学会空手回岸。我还会在这儿待一阵，看看下一次水面愿不愿意给我更像样的答案。",
                                "No matter. An old angler must learn to return to shore empty-handed now and then. I'll stay here a while longer and see whether the water offers a better answer next time." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_old_angler_leave_after_refuse",
                                                LocalizedText.of("那就再等等下一口鱼讯", "Then wait for the next pull on the line"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        return new DialogueDefinition("story_old_angler_stage0", root, nodes);
    }

    private static DialogueDefinition buildOldAnglerStage1Dialogue() {
        String root = "root";
        String full = "full";
        String none = "none";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "锅我已经熬上了，水汽、鱼香和岸边的风正好都凑在一起。看样子，我差不多也该决定，是继续把鱼竿背去下一个码头，还是就在这儿慢慢守一片熟悉的水面。",
                                "The pot is already on, and the steam, fish scent, and wind off the bank have all come together properly. It seems I'm about ready to decide whether to carry my rod to the next pier, or stay here and keep watch over a stretch of water that has begun to feel familiar." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_old_angler_reward_full",
                                                LocalizedText.of(
                                                        "你的鱼竿就留在这里吧，这地方已经够你守一阵了",
                                                        "Leave your rod here. This place is enough to keep for a while"),
                                                DialogueOptionType.BRANCH,
                                                full,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasStoryFlag(OLD_ANGLER_CATCH_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.giveItem(OLD_ANGLER_ROD, 1),
                                                DialogueEffectDef.giveCoins(12),
                                                DialogueEffectDef.advanceStoryStage(2),
                                                DialogueEffectDef.setNextVisitRange(3, 6)),
                                new DialogueOptionDef(
                                                "story_old_angler_reward_none",
                                                LocalizedText.of(
                                                        "看来你还是得自己守着这片水",
                                                        "Looks like you'll have to keep watching these waters yourself"),
                                                DialogueOptionType.BRANCH,
                                                none,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.missingStoryFlag(OLD_ANGLER_CATCH_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.advanceStoryStage(2),
                                                DialogueEffectDef.setNextVisitRange(3, 6)))));
        nodes.put(
                full,
                new DialogueNodeDef(
                        full,
                        LocalizedText.of(
                                "行，那我就把这根老钓竿留给你。它跟了我很久，知道什么时候该等，什么时候该收线。若你真想学会钓那些不肯轻易上钩的东西，它比很多废话都更有用。",
                                "Very well. Then I'll leave you this old rod. It has been with me long enough to know when to wait and when to draw the line in. If you truly mean to learn how to catch what refuses the hook, it will serve you better than a great deal of advice." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_old_angler_leave_after_full_reward",
                                        LocalizedText.of("那我就替你把这份手艺接着钓下去", "Then I'll keep casting forward with the craft you've left me"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                none,
                new DialogueNodeDef(
                        none,
                        LocalizedText.of(
                                "你这回没替我带回多少鱼，不过我还是愿意在这儿再住上一阵。真正的钓手守着一片水面，不是因为每次都有鱼，而是因为他知道总会有下一次浮漂轻轻沉下去的时候。",
                                "You didn't bring me much catch this time, but I'm willing to stay here a while longer all the same. A true angler watches a stretch of water not because every cast succeeds, but because he knows there will always be another moment when the float finally dips." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_old_angler_leave_after_none",
                                        LocalizedText.of("那就继续等你的下一口鱼讯", "Then keep waiting for the next pull"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition("story_old_angler_stage1", root, nodes);
    }

    private static DialogueDefinition buildOldAnglerStage2Dialogue() {
        String root = "root";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我最近在琢磨，一条好鱼究竟是靠运气，还是靠人肯在不出声的水边坐多久。想来想去，多半还是后者。所以我暂时不打算再换地方了。",
                                "Lately I've been wondering whether a truly fine catch comes from luck, or from how long a person is willing to sit beside quiet water. The answer is probably the latter. So for now, I don't intend to move on." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_old_angler_leave_final",
                                        LocalizedText.of("那就继续在这里守着吧", "Then keep watch here a while longer"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition("story_old_angler_stage2", root, nodes);
    }

    private static DialogueDefinition buildOldAnglerVisitChatterDialogue() {
        String root = "root";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我今天没打算钓什么大鱼，只想看看水面什么时候会先动。很多时候，真正值钱的不是鱼，而是你在等它时学会的耐心。",
                                "I wasn't planning to chase any great fish today. I only wanted to see when the water would move first. More often than not, what proves valuable isn't the fish, but the patience you learn while waiting for it." ),
                        List.of(
                                new DialogueOptionDef(
                                        "story_old_angler_leave_chatter",
                                        LocalizedText.of("那就继续把耐心练下去", "Then keep sharpening that patience"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition(OLD_ANGLER_CHATTER_DIALOGUE_ID, root, nodes);
    }

    private static DialogueDefinition buildCartographerStage0Dialogue() {
        String root = "root";
        String helped = "helped";
        String refused = "refused";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我在沿海画图，偏偏把记事纸和羽毛笔用光了。你要是愿意借我三张纸和一根羽毛，下次来时我给你看一件好东西。",
                                "I'm charting the coast, but I've run out of paper and quills. If you can spare three sheets of paper and a feather, I'll bring you something worthwhile next time."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cartographer_offer_supplies",
                                                LocalizedText.of(
                                                        "给你，这些拿去用吧",
                                                        "Here, take these and use them"),
                                                DialogueOptionType.BRANCH,
                                                helped,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasItem(PAPER, 3),
                                                DialogueRequirementDef.hasItem(FEATHER, 1))
                                        .withEffects(
                                                DialogueEffectDef.takeItem(PAPER, 3),
                                                DialogueEffectDef.takeItem(FEATHER, 1),
                                                DialogueEffectDef.setStoryFlag(CARTOGRAPHER_HELPED_FLAG),
                                                DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                                "story_cartographer_decline_supplies",
                                                LocalizedText.of(
                                                        "我这里没有，你自己想想办法吧",
                                                        "I don't have any. You'll have to figure something out yourself"),
                                                DialogueOptionType.BRANCH,
                                                refused,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                        "story_cartographer_leave_intro",
                                        LocalizedText.of("我给你找找，稍后再来", "Let me look for them and come back later"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                helped,
                new DialogueNodeDef(
                        helped,
                        LocalizedText.of(
                                "帮大忙了。等我把图补完，回旅社时一定亲手谢你。",
                                "That helps a lot. Once I've finished the map, I'll return to thank you properly."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cartographer_end_intro_helped",
                                                LocalizedText.of("路上小心", "Travel safely"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        nodes.put(
                refused,
                new DialogueNodeDef(
                        refused,
                        LocalizedText.of(
                                "没事，我总会想办法的。等地图线索多些，我还是会回来坐坐。",
                                "That's all right. I'll manage somehow. Once I have more map leads, I'll still come back for a stay."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cartographer_end_intro_refused",
                                                LocalizedText.of("祝你顺利", "Good luck out there"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        return new DialogueDefinition("story_cartographer_stage0", root, nodes);
    }

    private static DialogueDefinition buildCartographerStage1Dialogue() {
        String root = "root";
        String accepted = "accepted";
        String declined = "declined";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "上回那点补给让我把海图补得顺利多了。现在我还缺一张海洋群系的取景照片，想拿它和潮线笔记一起比对。你若愿意，下次出门时替我拍一张海边或海上的景色吧。",
                                "Those supplies made it much easier to finish my coastal chart. What I'm missing now is a photograph from any ocean biome so I can compare it against my tide notes. If you're willing, take one for me the next time you travel by the sea." ),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cartographer_accept_ocean_photo_task",
                                                LocalizedText.of(
                                                        "可以，我会替你留意海上的景色",
                                                        "Sure. I'll keep an eye out for a seascape for you"),
                                                DialogueOptionType.BRANCH,
                                                accepted,
                                                null)
                                        .withEffects(
                                                DialogueEffectDef.setStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .CARTOGRAPHER_OCEAN_PHOTO_REQUESTED_FLAG),
                                                DialogueEffectDef.advanceStoryStage(2)),
                                new DialogueOptionDef(
                                                "story_cartographer_decline_ocean_photo_task",
                                                LocalizedText.of(
                                                        "这次我恐怕帮不上忙",
                                                        "I don't think I can help with that this time"),
                                                DialogueOptionType.BRANCH,
                                                declined,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(2)))));
        nodes.put(
                accepted,
                new DialogueNodeDef(
                        accepted,
                        LocalizedText.of(
                                "不用挑得太苛刻，只要是在海洋群系拍下的景色就够了。浪头、海雾、海平线，哪一样都能帮我校对地图。",
                                "It doesn't need to be anything fancy. Any scene taken in an ocean biome will do. Waves, sea fog, the horizon. Any of it can help me correct the map."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cartographer_leave_after_accept_ocean_photo_task",
                                                LocalizedText.of("我记下了", "I'll remember that"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 4)))));
        nodes.put(
                declined,
                new DialogueNodeDef(
                        declined,
                        LocalizedText.of(
                                "没关系，我会先把其余空白补上。等我下回回来，再看看这些零散线索究竟能拼成什么样。",
                                "That's alright. I'll fill in the other blanks first. By the time I return, we'll see what shape these scattered clues can make."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cartographer_leave_after_decline_ocean_photo_task",
                                                LocalizedText.of("祝你顺利", "Good luck with it"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 4)))));
        return new DialogueDefinition("story_cartographer_stage1", root, nodes);
    }

    private static DialogueDefinition buildCartographerStage2Dialogue() {
        String root = "root";
        String bothCompleted = "both_completed";
        String oneCompleted = "one_completed";
        String noneCompleted = "none_completed";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "这些日子我一直在整理旅途中欠下的人情和线索。你之前帮过我的事，我都记着。让我看看，今天能给你带点什么回礼。",
                                "These past days I've been sorting through the favors and clues I owe from my travels. I still remember what you've done for me. Let's see what I can give you in return today."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cartographer_reward_both_tasks",
                                                LocalizedText.of(
                                                        "看来我帮上的忙比你预想的还多",
                                                        "Looks like I helped more than you expected"),
                                                DialogueOptionType.BRANCH,
                                                bothCompleted,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasStoryFlag(CARTOGRAPHER_HELPED_FLAG),
                                                DialogueRequirementDef.hasStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .CARTOGRAPHER_OCEAN_PHOTO_COMPLETED_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.giveItem(NATURES_COMPASS, 1),
                                                DialogueEffectDef.giveCoins(16),
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(4, 7)),
                                new DialogueOptionDef(
                                                "story_cartographer_reward_one_task_supplies",
                                                LocalizedText.of(
                                                        "能帮到你就好",
                                                        "I'm glad I could help"),
                                                DialogueOptionType.BRANCH,
                                                oneCompleted,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.missingStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .CARTOGRAPHER_OCEAN_PHOTO_COMPLETED_FLAG),
                                                DialogueRequirementDef.hasStoryFlag(CARTOGRAPHER_HELPED_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.giveItem(NATURES_COMPASS, 1),
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(4, 7)),
                                new DialogueOptionDef(
                                                "story_cartographer_reward_one_task_photo",
                                                LocalizedText.of(
                                                        "至少我替你带回了一份线索",
                                                        "At least I brought back one solid lead for you"),
                                                DialogueOptionType.BRANCH,
                                                oneCompleted,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.missingStoryFlag(CARTOGRAPHER_HELPED_FLAG),
                                                DialogueRequirementDef.hasStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .CARTOGRAPHER_OCEAN_PHOTO_COMPLETED_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.giveItem(NATURES_COMPASS, 1),
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(4, 7)),
                                new DialogueOptionDef(
                                        "story_cartographer_reward_no_tasks",
                                        LocalizedText.of("这次看来我没帮上你什么", "Looks like I didn't help you much this time"),
                                        DialogueOptionType.BRANCH,
                                        noneCompleted,
                                        null)
                                        .withRequirements(
                                                DialogueRequirementDef.missingStoryFlag(CARTOGRAPHER_HELPED_FLAG),
                                                DialogueRequirementDef.missingStoryFlag(
                                                        StoryGuestPhotoTaskRegistry
                                                                .CARTOGRAPHER_OCEAN_PHOTO_COMPLETED_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.advanceStoryStage(3),
                                                DialogueEffectDef.setNextVisitRange(4, 7)))));
        nodes.put(
                bothCompleted,
                new DialogueNodeDef(
                        bothCompleted,
                        LocalizedText.of(
                                "我原本只想把这枚罗盘交给真正帮过我的人。现在看来，一枚罗盘还不够表达谢意。这十六枚金币也收下吧，算我替海风和潮声一起谢你。",
                                "I meant to give this compass only to someone who had truly helped me. Now it seems a compass alone isn't enough. Take these sixteen coins as well, with my thanks and the sea's."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cartographer_leave_after_both_rewards",
                                                LocalizedText.of("那我就不客气了", "Then I'll gladly take them"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null))));
        nodes.put(
                oneCompleted,
                new DialogueNodeDef(
                        oneCompleted,
                        LocalizedText.of(
                                "不管是那几张纸和羽毛，还是你替我带回来的海上景色，都足够让我记住这份情。我把这枚自然罗盘留给你，愿它今后也能替你指路。",
                                "Whether it was the paper and feather or the seascape you brought back, it was enough for me to remember the favor. Keep this Nature's Compass, and may it guide you in return."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_cartographer_leave_after_one_reward",
                                        LocalizedText.of("这份礼物我会收好", "I'll keep this gift safe"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                noneCompleted,
                new DialogueNodeDef(
                        noneCompleted,
                        LocalizedText.of(
                                "旅途上总会有些事来不及完成。我不怪你，只是把这些未画完的边角继续记在心里。等下回见面，我们就聊些轻松的吧。",
                                "There are always things a traveler doesn't manage to finish. I don't blame you. I'll just keep these unfinished edges in mind and save lighter talk for the next time we meet."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cartographer_leave_after_no_reward",
                                                LocalizedText.of("下次再聊", "We'll talk again next time"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null))));
        return new DialogueDefinition("story_cartographer_stage2", root, nodes);
    }

    private static DialogueDefinition buildCartographerStage3Dialogue() {
        String root = "root";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "海线会变，旅人的路也会变，但我大概会一直记得这家旅社的灯火。",
                                "Coastlines shift, and so do a traveler's roads, but I think I'll always remember the lights of this inn."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_cartographer_leave_final",
                                        LocalizedText.of("下次见", "See you next time"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition("story_cartographer_stage3", root, nodes);
    }

    private static DialogueDefinition buildCartographerVisitChatterDialogue() {
        String root = "root";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我得趁记忆还热，把刚记下来的风向和潮痕誊到图纸上。等我下次再来，应该能讲给你听一段更像样的见闻。",
                                "I should copy these fresh notes about wind and tide onto the chart while they're still vivid. By the time I return, I'll probably have a better story to tell."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_cartographer_leave_chatter",
                                        LocalizedText.of("那就等你下次再说", "Then tell me next time"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition(CARTOGRAPHER_CHATTER_DIALOGUE_ID, root, nodes);
    }
}
