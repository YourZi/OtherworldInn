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
        Map<String, StoryGuestDefinition> byId = new LinkedHashMap<>();
        byId.put(wanderingCartographer.id(), wanderingCartographer);
        byId.put(wanderingMinstrel.id(), wanderingMinstrel);
        byId.put(wanderingChef.id(), wanderingChef);
        byId.put(fallenNoble.id(), fallenNoble);
        byId.put(wanderingAlchemist.id(), wanderingAlchemist);
        byId.put(archaeologist.id(), archaeologist);
        byId.put(gemMerchant.id(), gemMerchant);
        byId.put(oldKnight.id(), oldKnight);
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
                                "这一路走来攒下不少曲子，羊皮纸快写满了，墨水也快见底了。能借我五张纸和一个墨囊吗？等下次见面，我给你唱首刚写好的新歌！",
                                "I've gathered quite a few tunes on this journey, but my parchment is almost full and my ink is running dry. Could you lend me five pieces of Paper and an Ink Sac? Next time we meet, I'll sing you a brand-new song!"),
                        List.of(
                                new DialogueOptionDef(
                                                "story_minstrel_offer_supplies",
                                                LocalizedText.of("这些东西你先拿着应急吧。", "Take these and make good use of them"),
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
                                                LocalizedText.of("实在不好意思，我最近手头也紧。", "Sorry, I'm running short too"),
                                                DialogueOptionType.BRANCH,
                                                refused,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                        "story_minstrel_leave_intro",
                                        LocalizedText.of("我去准备一下，回头再来找你。", "Come back when I've prepared"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                helped,
                new DialogueNodeDef(
                        helped,
                        LocalizedText.of(
                                "太好了！灵感这东西，就像林间的风，不赶紧记下来，一转头可就溜走啦。",
                                "Wonderful! Inspiration is like the wind through the forest—if you don't jot it down quickly, it slips away the moment you turn your head."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_minstrel_leave_after_help",
                                                LocalizedText.of("那我就等你下次回来再聊。", "Then I'll wait for your return"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        nodes.put(
                refused,
                new DialogueNodeDef(
                        refused,
                        LocalizedText.of(
                                "没关系，就算没纸我也能哼出几段旋律。大不了就记在脑子里，多唱几遍也就背下来了。",
                                "That's alright, I can still hum a few melodies without paper. At worst, I'll just memorize them in my head and sing them a few times to make them stick."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_minstrel_leave_after_refuse",
                                                LocalizedText.of("愿你的旅途总有歌声相伴。", "May the road still give you songs"),
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
                                "这趟旅行见识了不少美景，可惜副歌部分总觉得还差一点画面感。你要是路过平原，能不能帮我带一张视野开阔的照片？说不定那正是这首歌完美的收尾呢。",
                                "I've seen many beautiful sights on this journey, but the chorus still lacks a certain imagery. If you pass by a plains biome, could you bring back a photo with a wide, open view? It might be the perfect finale for this song."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_minstrel_accept_plains_photo_task",
                                                LocalizedText.of("没问题，下次我替你去看看。", "Sure, I'll keep an eye out next time"),
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
                                                LocalizedText.of("抱歉，这次我实在抽不出空。", "I may not have the time this round"),
                                                DialogueOptionType.BRANCH,
                                                declined,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(2)))));
        nodes.put(
                accepted,
                new DialogueNodeDef(
                        accepted,
                        LocalizedText.of(
                                "不用刻意去寻，只要看着视野开阔、让人心情舒畅的地方就好啦。",
                                "No need to go out of your way. As long as it's a place with a wide, sweeping view that lifts the spirit, it will be perfect."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_minstrel_leave_after_accept_photo",
                                                LocalizedText.of("行，这事儿我记在心上了。", "I'll remember that"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 4)))));
        nodes.put(
                declined,
                new DialogueNodeDef(
                        declined,
                        LocalizedText.of(
                                "没事儿，有些旋律在心里沉淀久了，反而更有韵味。下次有机会再说吧。",
                                "No worries. Sometimes, melodies gain more charm when they settle in the heart for a while. Let's wait for another chance."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_minstrel_leave_after_decline_photo",
                                                LocalizedText.of("没事，那就等下次再说吧。", "Then save it for next time"),
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
                                "这一路多亏了你的帮忙，无论是借我的纸笔，还是带回来的平原风光，我都记在心里呢。",
                                "I owe so much to your help along the way. Whether it was the paper and ink you lent me, or the scenery of the plains you brought back, I've kept it all in my heart."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_minstrel_reward_settle",
                                                LocalizedText.of(
                                                        "等你把这曲子唱完，再决定去留吧。",
                                                        "Finish your song, then we'll talk about staying"),
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
                                                        "能把这些写下来，就已经很不错了。",
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
                                                        "不管怎样，至少你找到了写歌的灵感。",
                                                        "At least you've found the inspiration"),
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
                                                        "看来咱们这回都没碰上好时候。",
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
                                "你说得对。我四处漂泊太久，差点忘了好歌不一定非要在路上唱完。这张唱片你先收着，等我把后面的曲调理顺了，再回来慢慢唱给你听。",
                                "You're right. I've been wandering for so long that I almost forgot: a good song doesn't have to be finished on the road. Keep this music disc for now. Once I smooth out the rest of the melody, I'll come back and sing it to you."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_minstrel_leave_after_settle_reward",
                                        LocalizedText.of("那我洗耳恭听你的新作。", "Then I'll wait to hear the full song"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                traveling,
                new DialogueNodeDef(
                        traveling,
                        LocalizedText.of(
                                "曲子是写完了，但我的旅程还远未结束。这张唱片送你，就当是我把还没唱完的故事先寄存在这儿了。等我去更多的地方转转，补齐了后半段，说不定哪天就又绕回这家旅社啦。",
                                "The song is finished, but my journey is far from over. I'm giving you this music disc as a token of the unfinished stories I'm leaving here. After I travel to more places and fill in the rest of the tale, maybe one day I'll circle back to this inn."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_minstrel_leave_after_travel_reward",
                                        LocalizedText.of("愿你的歌声走到哪儿都有知音。", "May the next road still give you an audience"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                farewell,
                new DialogueNodeDef(
                        farewell,
                        LocalizedText.of(
                                "看来这次还是差了点缘分。无妨，人有错失之时，歌亦有未完之曲。我要继续赶路了，咱们后会有期。",
                                "It seems we lacked a bit of luck this time. No matter; people miss their moments, and songs have their unfinished verses. I must continue my journey. Until we meet again."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_minstrel_leave_after_farewell",
                                        LocalizedText.of("祝你接下来的路顺风顺水。", "Hope next time goes smoother"),
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
                                "我现在倒是挺愿意放慢脚步的。旅社里的嘈杂人声、酒杯的碰撞，还有夜晚的微风，可比匆匆赶路时惬意多啦。",
                                "I'm actually quite willing to slow my pace now. The lively chatter in the inn, the clinking of tankards, and the evening breeze are far more pleasant than rushing endlessly on the road."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_minstrel_leave_final",
                                        LocalizedText.of("期待下次能听到你的新段落。", "Save the next verse for me"),
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
                                "我正在给前几天写的那几句词找合适的调子。这里的氛围真的太棒了，特别适合慢下来打磨作品。",
                                "I'm trying to find the right melody for those lyrics I wrote a few days ago. The atmosphere here is truly wonderful, perfectly suited for slowing down and polishing my work."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_minstrel_leave_chatter",
                                        LocalizedText.of("你慢慢找灵感，我不打扰了。", "Go on, keep polishing"),
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
                                "我正在研发下一季的新菜，总觉得还差点本地的风味。你能帮我凑一顿地道的家常饭吗？面包、熟牛肉、烤土豆、南瓜派，最好再来瓶蜂蜜！",
                                "I'm developing new dishes for next season, but I feel they're lacking a bit of local flavor. Could you help me put together an authentic home-style meal? Bread, Steak, Baked Potato, Pumpkin Pie, and preferably a Honey Bottle!"),
                        List.of(
                                new DialogueOptionDef(
                                                "story_chef_offer_supplies",
                                                LocalizedText.of("没问题，我帮你凑齐。", "Alright, I'll put it together for you"),
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
                                                LocalizedText.of("抱歉，你要的食材我这会儿实在是凑不齐。", "Sorry, I can't gather all that this time"),
                                                DialogueOptionType.BRANCH,
                                                refused,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                        "story_chef_leave_intro",
                                        LocalizedText.of("行，我这就去帮你准备。", "Let me go prepare them for you"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                helped,
                new DialogueNodeDef(
                        helped,
                        LocalizedText.of(
                                "对，就是这个味儿！做菜嘛，总得先尝过百家饭才能有灵感。",
                                "Yes, that's the exact flavor! You know, to cook well, you have to taste a hundred different home-cooked meals to find real inspiration."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_chef_leave_after_help",
                                                LocalizedText.of("好，我等着尝你的新菜。", "Then I'll wait to see what you cook"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        nodes.put(
                refused,
                new DialogueNodeDef(
                        refused,
                        LocalizedText.of(
                                "行吧，那我先拿别的食材凑合一下试试。",
                                "Alright then, I'll just try to make do with some other ingredients for now."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_chef_leave_after_refuse",
                                                LocalizedText.of("那你先慢慢试，不着急。", "Take your time figuring it out"),
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
                                "这几天我在附近转悠了几圈，心里大概有谱了。现在我想看看这地方平时是怎么产出食材的。要是方便的话，帮我拍张农场动物的照片吧。鸡猪牛羊都行，只要有那种鲜活的烟火气就行！",
                                "I've wandered around the area for a few days, and I have a rough idea now. Next, I want to see how this place produces its ingredients. If it's convenient, could you take a photo of some farm animals? Chickens, pigs, cows, or sheep—any will do, as long as it captures that lively rustic charm!"),
                        List.of(
                                new DialogueOptionDef(
                                                "story_chef_accept_farm_photo_task",
                                                LocalizedText.of("没问题，我这就去农场帮你把照片拍回来。", "Sure, I'll bring back a photo for you"),
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
                                                LocalizedText.of("不好意思啊，这趟我实在抽不出空去农场。", "Sorry, I can't make it this time"),
                                                DialogueOptionType.BRANCH,
                                                declined,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(2)))));
        nodes.put(
                accepted,
                new DialogueNodeDef(
                        accepted,
                        LocalizedText.of(
                                "不管是厨房还是农场，只要是充满市井烟火气的地方就足够了。",
                                "Whether it's a kitchen or a farm, as long as it's a place full of rustic charm and everyday life, it will do perfectly."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_chef_leave_after_accept_photo",
                                                LocalizedText.of("放心，我知道该重点拍哪些食材了。", "Got it, I know what to look for"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 4)))));
        nodes.put(
                declined,
                new DialogueNodeDef(
                        declined,
                        LocalizedText.of(
                                "没事，忙的话你先顾自己的事。等哪天有空了，再替我去瞅一眼就行。",
                                "No worries, if you're busy, take care of your own matters first. Just take a look for me whenever you have some free time."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_chef_leave_after_decline_photo",
                                                LocalizedText.of("那只能等下次有机会再去拍了。", "Then save it for next time"),
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
                                "无论如何，做菜的手感总算是找回来一些了。趁着这股热乎劲儿，我得好好琢磨琢磨下一季的新菜单了。",
                                "Regardless, I've finally gotten some of my cooking touch back. While the inspiration is still hot, I need to carefully ponder over next season's new menu."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_chef_reward_full",
                                                LocalizedText.of(
                                                        "要不你干脆就留在这儿掌勺吧？",
                                                        "Why not stay and fire up the kitchen"),
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
                                                        "至少你已经摸到门道了。",
                                                        "At least you've found the flavor"),
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
                                                        "至少你亲眼见过了，也不算白跑。",
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
                                                        "别灰心，要不再多试几次？",
                                                        "Maybe try a few more rounds"),
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
                                "那我以后肯定常来。这饭袋你留着用吧，等我把新菜彻底研究透了，一定请你吃口热乎的！",
                                "Then I'll definitely be a regular here. You keep this lunch bag. Once I've completely perfected my new recipes, I'll be sure to treat you to a hot meal!"),
                        List.of(
                                new DialogueOptionDef(
                                        "story_chef_leave_after_full_reward",
                                        LocalizedText.of("那你开席的时候，可得给我留个好位子尝鲜啊。", "Save me a seat then"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                partial,
                new DialogueNodeDef(
                        partial,
                        LocalizedText.of(
                                "火候虽说还差那么点意思，但我已经舍不得离开这儿了。饭袋你先拿着，等我彻底摸透了本地人的口味，再端出我的新招牌菜。",
                                "The heat is still a little off, but I'm already reluctant to leave this place. Take this lunch bag for now. Once I've thoroughly figured out the locals' tastes, I'll serve up my new signature dish."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_chef_leave_after_partial_reward",
                                        LocalizedText.of("那你可别让我等太久啊。", "Don't keep me waiting too long"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                none,
                new DialogueNodeDef(
                        none,
                        LocalizedText.of(
                                "虽说这次你没帮上什么大忙，但我还挺喜欢这儿的，打算再多留一阵子。",
                                "Even though you couldn't help much this time, I actually quite like it here and plan to stay a bit longer."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_chef_leave_after_none_reward",
                                        LocalizedText.of("那你先在这儿安顿一阵。", "Then keep the fire going for now"),
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
                                "我最近一直在琢磨，怎么把本地的家常风味和各地旅人的口味完美地揉到一块儿。等做出来了，你可得来尝第一口！",
                                "Lately, I've been pondering how to perfectly blend local home-style flavors with the tastes of travelers from all over. When I finally make it, you have to come take the first bite!"),
                        List.of(
                                new DialogueOptionDef(
                                        "story_chef_leave_final",
                                        LocalizedText.of("做好了可得先让我尝尝鲜。", "Save me the first bite when it's ready"),
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
                                "我在你们这儿研究出了不少新花样，不得不说，这里的客人都挺懂吃的。",
                                "I've developed quite a few new dishes while staying here. I must say, the patrons at this inn really know their food."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_chef_leave_chatter",
                                        LocalizedText.of("你先接着试，我回头再来。", "Go on, keep testing"),
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
                                "提前声明，我可不是在乞求施舍，我只是想把这身行头收拾得体面些。如果你能提供两块金锭和十六团白羊毛，至少能让我看起来不至于如此落魄。",
                                "Let me be clear, I am not begging for charity. I simply wish to make my attire presentable. If you could provide two Gold Ingots and sixteen White Wool, it would at least keep me from looking so destitute."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_noble_offer_attire",
                                                LocalizedText.of("没问题，包在我身上。", "Fine, I'll help you"),
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
                                                LocalizedText.of("实在抱歉，这行头我暂时也凑不齐。", "Sorry, I can't spare that much right now"),
                                                DialogueOptionType.BRANCH,
                                                refused,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                        "story_noble_leave_intro",
                                        LocalizedText.of("你歇着，我去帮你找找看。", "Let me go look for them"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                helped,
                new DialogueNodeDef(
                        helped,
                        LocalizedText.of(
                                "终于不用像刚从泥坑里爬出来那样难堪了。这份人情，我艾什福德记下了。",
                                "Finally, I no longer have to look as though I just crawled out of a mud pit. I, Ashford, shall remember this favor."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_noble_leave_after_attire_help",
                                                LocalizedText.of("那你先把这身体面行头穿稳当了吧。", "Then keep this outfit steady"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        nodes.put(
                refused,
                new DialogueNodeDef(
                        refused,
                        LocalizedText.of(
                                "也罢。如果一个古老的姓氏只能靠几尺衣料和镀金来死撑门面，那它本身也没剩下多少荣光了。",
                                "Very well. If an ancient name must rely solely on a few yards of fabric and gilding to keep up appearances, then there wasn't much glory left to it anyway."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_noble_leave_after_attire_refuse",
                                                LocalizedText.of("你的脾气倒是一点没变，还是这么嘴硬。", "Your tongue's still sharp"),
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
                                "如今这片大陆上，像样的宅邸已经屈指可数了。要是你偶然路过林地府邸，替我留个影吧。我想确认一下，这世上究竟还保留着几分旧日的体面。",
                                "There are precious few proper estates left on this continent now. If you happen to pass by a Woodland Mansion, please take a picture for me. I want to see just how much of the old world's dignity remains."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_noble_accept_mansion_photo_task",
                                                LocalizedText.of("没问题，我替你去宅子那边走一趟。", "Fine, I'll go look for you"),
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
                                                LocalizedText.of("抱歉，那种大宅子我恐怕进不去。", "Sorry, I may never make it to such a place"),
                                                DialogueOptionType.BRANCH,
                                                declined,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(2)))));
        nodes.put(
                accepted,
                new DialogueNodeDef(
                        accepted,
                        LocalizedText.of(
                                "哪怕只是拍些残窗破墙也行，我只希望能看到昔日门面的几分影子。",
                                "Even a photo of broken windows and ruined walls will do. I only wish to catch a glimpse of its former grandeur."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_noble_leave_after_accept_photo",
                                                LocalizedText.of("好，这事儿我记下了。", "Alright, I'll remember"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 4)))));
        nodes.put(
                declined,
                new DialogueNodeDef(
                        declined,
                        LocalizedText.of(
                                "也是。毕竟不是谁都有这份闲心，跑去瞻仰一栋摇摇欲坠的旧宅。",
                                "I suppose so. After all, not everyone has the leisure to go pay respects to a crumbling old mansion."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_noble_leave_after_decline_photo",
                                                LocalizedText.of("既然这样，那这回就算了吧。", "Let's leave it for now"),
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
                                "这几天我想了许多。不管过往的荣光还剩几分，我总得找个地方重新立足。能借我四颗绿宝石和一颗钻石作为起步资金吗？这笔账，我会铭记于心。",
                                "I have thought a great deal these past few days. Regardless of how much past glory remains, I must find a place to re-establish myself. Could you lend me four Emeralds and one Diamond as starting capital? I shall keep this debt firmly in mind."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_noble_offer_fund",
                                                LocalizedText.of(
                                                        "这笔钱你先拿着周转，别客气。",
                                                        "Here, take this to keep going"),
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
                                                        "凭你现在的底子，对付一阵子不成问题。",
                                                        "What you have now should last you a while"),
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
                                                        "哪怕物是人非，至少你心里的念想还在。",
                                                        "At least what you carry inside is still there"),
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
                                                        "没别的地方去，要不你就在这儿先落脚吧。",
                                                        "Why not just stay for now"),
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
                                "很好。既然有了这笔周转资金，我也必须承认，这家旅社比我最初预想的要体面得多。这只旧怀表你收下吧，这是我目前唯一拿得出手的谢礼了。",
                                "Excellent. With this capital secured, I must admit that this inn is far more respectable than I initially presumed. Please accept this old pocket watch; it is the only decent token of gratitude I can currently offer."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_noble_leave_after_full_reward",
                                        LocalizedText.of("既然回不去了，不如就在这儿长住吧。", "Then settle in here"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                partial,
                new DialogueNodeDef(
                        partial,
                        LocalizedText.of(
                                "我原本以为这只是短暂的落脚。可一旦重新习惯了有人点灯、有人开门的安稳日子，就很难再回到颠沛流离之中了。无以为报，这只旧怀表请你务必收下。",
                                "I thought this would be but a brief stay. Yet, once you grow accustomed to the comfort of lit lamps and opened doors again, returning to a life of exile is difficult. Having nothing else to repay you, please, you must take this old pocket watch."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_noble_leave_after_partial_reward",
                                        LocalizedText.of("那你就在这儿安心住下吧，没人赶你。", "Then stay and make peace with it"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                none,
                new DialogueNodeDef(
                        none,
                        LocalizedText.of(
                                "别误会，我可不是舍不得走，只是懒得再去向另一群陌生人解释我的身份罢了。这里至少有个落脚处，饭菜也勉强能入口。",
                                "Don't misunderstand, it's not that I'm reluctant to leave. I simply cannot be bothered to explain my lineage to yet another group of strangers. At least there's a place to rest here, and the food is somewhat palatable."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_noble_leave_after_none_reward",
                                        LocalizedText.of("既然如此，就别再瞎折腾了，安心待着吧。", "Then stop dragging yourself around"),
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
                                "我最近正在学习如何像个普通的住客那样生活，而不是像个怨妇一样等着过去来向我认错。老实说，这地方还真挺让人留恋的。",
                                "Lately I've been learning how to live like an ordinary guest, rather than sulking like a bitter soul waiting for the past to apologize to me. To be honest, this place has quite a charm to it."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_noble_leave_final",
                                        LocalizedText.of("瞧你这样子，倒是渐渐习惯这里了。", "Looks like you're settling in"),
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
                                "在你这里住久了，很多过往我也渐渐放下了。现在的心境，总算是清净了不少。",
                                "Having stayed here so long, I've gradually let go of much of my past. My mind is finally at peace."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_noble_leave_chatter",
                                        LocalizedText.of("嘴硬心软，说的就是你吧。", "You can drop the tough act now"),
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
                                "我在路上采集的样本，还没到地方药性就挥发光了！真是气人。你能帮我找两份下界疣、一份烈焰粉和一个玻璃瓶吗？我想先把这瓶急需的药剂调配出来。",
                                "The samples I gathered on the road lost their potency before I even arrived! How infuriating. Could you find me two Nether Warts, one Blaze Powder, and a Glass Bottle? I need to brew this urgent potion right away."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_alchemist_offer_supplies",
                                                LocalizedText.of("给，你先拿去应急吧。", "Here, take these for now"),
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
                                                LocalizedText.of("抱歉啊，我手头暂时没你要的这些材料。", "Sorry, don't have those right now"),
                                                DialogueOptionType.BRANCH,
                                                refused,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                        "story_alchemist_leave_intro",
                                        LocalizedText.of("你歇着，我去帮你找找看。", "Let me go look for them"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                helped,
                new DialogueNodeDef(
                        helped,
                        LocalizedText.of(
                                "太好了，容器、溶剂和材料总算是凑齐了。接下来只差掌握正确的火候了。",
                                "Excellent! The vessels, solvents, and materials are all finally here. Now I just need to master the proper heat."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_alchemist_leave_after_help",
                                                LocalizedText.of("那我就期待你下次的实验结果了。", "Then I'll wait for your next result"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        nodes.put(
                refused,
                new DialogueNodeDef(
                        refused,
                        LocalizedText.of(
                                "唉，那我只能拿剩下的残次品硬撑一下了。能不能成，全看运气咯。",
                                "Alas, I'll just have to make do with these leftover dregs. Whether the brew succeeds is entirely up to luck."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_alchemist_leave_after_refuse",
                                                LocalizedText.of("你自己多加小心，别勉强。", "Stay careful out there"),
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
                                "这几天我用手头现有的材料反复试错，发现还是缺一个靠谱的火候参照。要是你顺路去下界堡垒，帮我拍张那里的照片吧。我想观察一下那边的火焰温度和地狱岩的质地，看看符不符合我的实验需求。",
                                "I've been repeatedly testing the materials I have on hand, but I still lack a reliable reference for the heat. If you happen to pass by a Nether Fortress, please take a photo for me. I want to observe the temperature of the flames and the texture of the netherrack to see if they meet my experimental needs."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_alchemist_accept_fortress_photo_task",
                                                LocalizedText.of("交给我吧，我去给你拍回来。", "Sure, I'll bring back a photo"),
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
                                                LocalizedText.of("不好意思，这趟我恐怕抽不开身。", "Sorry, I can't make it this time"),
                                                DialogueOptionType.BRANCH,
                                                declined,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(2)))));
        nodes.put(
                accepted,
                new DialogueNodeDef(
                        accepted,
                        LocalizedText.of(
                                "拍个大概就行，只要能认出是下界堡垒的地貌就可以啦。",
                                "A rough shot is fine, as long as I can recognize the terrain of a Nether Fortress."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_alchemist_leave_after_accept_photo",
                                                LocalizedText.of("好，这事儿我记下了。", "Got it, I'll remember"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 4)))));
        nodes.put(
                declined,
                new DialogueNodeDef(
                        declined,
                        LocalizedText.of(
                                "那就算了，我找找别的替代材料吧，毕竟炼金术的原理都是相通的。",
                                "Never mind then, I'll look for substitute materials. After all, the principles of alchemy are all interconnected."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_alchemist_leave_after_decline_photo",
                                                LocalizedText.of("没事，那就等下次再说吧。", "Save it for next time then"),
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
                                "这几天的实验弄得我灰头土脸的，不过总算是熬出了一瓶完美的成品！手感全找回来了，现在得好好想想这瓶药水该用在什么地方。",
                                "These past few days of experiments have left me covered in soot, but I've finally brewed a perfect batch! I've completely gotten my touch back. Now I must carefully consider where to use this potion."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_alchemist_reward_settle",
                                                LocalizedText.of(
                                                        "我看你和这些药剂还是都先留在这儿吧。",
                                                        "Why don't you and the draught both stay"),
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
                                                        "往好处想，至少你已经能配出成品了。",
                                                        "At least you've brewed a finished batch"),
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
                                                        "不管怎么说，至少你找回掌控火候的感觉了。",
                                                        "At least you've recovered your heat control"),
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
                                                        "要不……你再重新试一锅？",
                                                        "Maybe run one more round of tests"),
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
                                "行吧，那这瓶品质最好的就先留给你防身啦。",
                                "Very well. I'll leave this highest-quality potion with you for protection."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_alchemist_leave_after_settle_reward",
                                        LocalizedText.of("那我等你下次配出更稳定的药剂。", "Then I'll wait for your next improved batch"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                traveling,
                new DialogueNodeDef(
                        traveling,
                        LocalizedText.of(
                                "虽然现在勉强调出了能喝的药剂，但离我心目中的完美品质还差得远。这瓶你先拿着，权当谢你的帮忙了。我还得继续去寻找下一组实验材料。",
                                "Although I've managed to brew a drinkable potion, it's still far from the perfection I seek. Take this bottle as thanks for your help. I must continue my search for the next set of experimental materials."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_alchemist_leave_after_travel_reward",
                                        LocalizedText.of("悠着点，别先把你自己给炸飞了。", "Don't blow yourself up first"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                farewell,
                new DialogueNodeDef(
                        farewell,
                        LocalizedText.of(
                                "这次收集的数据还是太单薄了，做不出什么像样的结论。不过没关系，炼金术嘛，哪有不经历失败的？我准备去别处转转，把缺的材料都补齐。",
                                "The data gathered this time is too sparse to draw any decent conclusions. But no matter, what is alchemy without a few failures? I'm going to travel elsewhere to gather the missing materials."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_alchemist_leave_after_farewell",
                                        LocalizedText.of("行，那我就静候佳音了。", "Then I'll wait for your good news"),
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
                                "我最近正在埋头研究新配方。以前的实验老是达不到预期，我就在想，万一是一开始的理论基础就错了呢？",
                                "I've been burying myself in researching new formulas lately. My past experiments never met expectations, so I started wondering: what if my foundational theories were wrong all along?"),
                        List.of(
                                new DialogueOptionDef(
                                        "story_alchemist_leave_final",
                                        LocalizedText.of("那你就守着那口锅慢慢熬吧。", "Then take your time and see"),
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
                                "我今天在尝试调配一份不需要烈焰粉就能起效的药剂。希望它在关键时刻可千万别掉链子。",
                                "Today I'm trying to brew a potion that works without Blaze Powder. I just hope it doesn't fail me at a critical moment."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_alchemist_leave_chatter",
                                        LocalizedText.of("新药剂调好了记得给我长长见识。", "Let me see it when it's ready"),
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
                                "我有九成的把握，这片大陆的地层深处埋藏着一个被遗忘的古老文明！但要让它重见天日，我还得做些准备。能借我一把铁镐和十六支火把吗？下次我一定带回决定性的线索！",
                                "I am ninety percent certain that a forgotten ancient civilization lies buried deep within the strata of this continent! But to bring it to light, I need to make preparations. Could you lend me an Iron Pickaxe and sixteen Torches? Next time, I will surely bring back decisive clues!"),
                        List.of(
                                new DialogueOptionDef(
                                                "story_archaeologist_offer_tools",
                                                LocalizedText.of("这些工具你先拿去应付一下吧。", "Take these tools and start digging"),
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
                                                LocalizedText.of("真不好意思，我手头正好缺这些工具。", "Sorry, don't have these right now"),
                                                DialogueOptionType.BRANCH,
                                                refused,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                        "story_archaeologist_leave_intro",
                                        LocalizedText.of("你等着，我这就去帮你找找。", "Let me see if I can find some"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                helped,
                new DialogueNodeDef(
                        helped,
                        LocalizedText.of(
                                "太感谢了！你可帮我省了大麻烦，一个考古学家要是没有趁手的工具，简直比没带脑子还难受。",
                                "Thank you so much! You've saved me a great deal of trouble. For an archaeologist, being without proper tools is almost worse than being without a brain."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_archaeologist_leave_after_tools_help",
                                                LocalizedText.of("那你可得好好干，别忘了把证据带回来。", "Then come back with evidence"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        nodes.put(
                refused,
                new DialogueNodeDef(
                        refused,
                        LocalizedText.of(
                                "那我只能用老办法徒手慢慢刨了。也不是不行，就是效率实在太低了。",
                                "Then I'll just have to dig slowly by hand, the old-fashioned way. It's not impossible, just terribly inefficient."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_archaeologist_leave_after_tools_refuse",
                                                LocalizedText.of("下遗迹的时候自己多加小心，别逞强。", "Be careful down there"),
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
                                "这几天我已经把手头的文献和破片都研究透了，现在就缺一张具有说服力的现场照片。你要是路过沙漠神殿或是丛林神庙，务必替我拍张照！入口的结构、石砖的风化程度，这些都是极其宝贵的资料。",
                                "I've thoroughly studied all the documents and fragments I have, and now all I lack is a convincing field photograph. If you pass by a Desert Pyramid or a Jungle Temple, be sure to take a picture for me! The entrance structure, the weathering of the stone bricks—these are incredibly precious data."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_archaeologist_accept_temple_photo_task",
                                                LocalizedText.of("行，我到了那边会帮你多留心拍几张的。", "Alright, I'll keep an eye out"),
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
                                                LocalizedText.of("抱歉，那种遗迹我这趟未必能碰得着。", "Sorry, I may never run across such a place"),
                                                DialogueOptionType.BRANCH,
                                                declined,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(2)))));
        nodes.put(
                accepted,
                new DialogueNodeDef(
                        accepted,
                        LocalizedText.of(
                                "不用拍得多么精美，只要能清晰辨认出入口的建筑结构，就足够我写出两篇论文了！",
                                "It doesn't need to be perfectly composed. As long as the architectural structure of the entrance is clearly recognizable, that's enough for me to write two whole papers!"),
                        List.of(
                                new DialogueOptionDef(
                                                "story_archaeologist_leave_after_accept_photo",
                                                LocalizedText.of("明白，我知道该重点拍些什么了。", "Got it, I know what to look for"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 4)))));
        nodes.put(
                declined,
                new DialogueNodeDef(
                        declined,
                        LocalizedText.of(
                                "也是，反正遗迹在那里又不会长腿跑掉，总有机会看到的。那我先理一理现有的考察笔记吧。",
                                "Fair enough. The ruins aren't going to grow legs and run away; there will always be another chance. I'll just organize my current field notes for now."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_archaeologist_leave_after_decline_photo",
                                                LocalizedText.of("那我就期待你下次能有什么大发现了。", "Then I'll wait for your next find"),
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
                                "经过这几天的日夜钻研，真相已经呼之欲出了！只要再有一块回响碎片和一块远古残骸作为对照样本，这个课题就能彻底走通了！你愿意再帮我出把力吗？",
                                "After days and nights of intense study, the truth is on the verge of being revealed! With just one Echo Shard and one Ancient Debris as control samples, this research will be completely validated! Would you be willing to lend me a hand once more?"),
                        List.of(
                                new DialogueOptionDef(
                                                "story_archaeologist_reward_full",
                                                LocalizedText.of(
                                                        "你要的遗迹样本我给你带回来了。",
                                                        "I've brought the samples for you"),
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
                                                        "你要的样本我带回来了，看看合不合用。",
                                                        "I've brought the samples."),
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
                                                        "听你的意思，这背后的水已经被你蹚得够深了。",
                                                        "Sounds like you've dug deep enough"),
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
                                                        "既然如此，那你就继续在这里挖挖看吧。",
                                                        "Then keep digging"),
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
                                "完美！这下拼图彻底完整了，所有的线索都串联起来了！这把跟随我多年的考古刷送你吧，清理文物的时候它可是帮了大忙。",
                                "Perfect! The puzzle is finally complete, and all the clues connect! Take this archaeological brush that has accompanied me for years; it's been an immense help in cleaning artifacts."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_archaeologist_leave_after_full_reward",
                                        LocalizedText.of("那你就趁热打铁，把这背后的秘密彻底挖出来吧。", "Then research it through to the end"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                relics,
                new DialogueNodeDef(
                        relics,
                        LocalizedText.of(
                                "这两个样本的分量简直不可估量！虽然之前的证据链还有些零碎，但有了它们，研究完全可以继续往下推进了。这把刷子送你，手感极佳，可是我用过最结实的工具了。",
                                "The value of these two samples is simply immeasurable! Although the previous chain of evidence was a bit fragmented, with these, the research can fully advance. Take this brush—it handles excellently and is the sturdiest tool I've ever used."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_archaeologist_leave_after_relic_reward",
                                        LocalizedText.of("多谢，那这把考古刷我就不客气收下了。", "Then I'll gladly keep this brush"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                evidence,
                new DialogueNodeDef(
                        evidence,
                        LocalizedText.of(
                                "还差最后一点关键样本，我不打算走了。我就在这儿耗着，直到把所有的秘密都挖出来为止。",
                                "I'm still missing the final key sample, so I don't plan on leaving. I'll stay right here and dig until every last secret is unearthed."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_archaeologist_leave_after_evidence_only",
                                        LocalizedText.of("那你继续努力，祝你早日查明真相。", "Keep at it"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                none,
                new DialogueNodeDef(
                        none,
                        LocalizedText.of(
                                "虽然这次你帮我找到的线索不多，但我反而更确信这地方大有文章。毕竟那些一眼就能看透的遗迹太无趣，像这种硬骨头啃起来才带劲！",
                                "Even though you didn't find many clues for me this time, it only makes me more certain that there's something huge hidden here. After all, ruins that can be figured out at a glance are too boring; these tough mysteries are the real thrill!"),
                        List.of(
                                new DialogueOptionDef(
                                        "story_archaeologist_leave_after_none",
                                        LocalizedText.of("期待你早日有新发现。", "Looking forward to your results"),
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
                                "我最近正在把附近的地层结构和风化痕迹重新做一次系统性的整理。这里的地理环境，真的是个天然的研究室。",
                                "I've recently been doing a systematic reorganization of the nearby strata structures and weathering traces. The geographical environment here truly is a natural laboratory."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_archaeologist_leave_final",
                                        LocalizedText.of("我等着拜读你整理好的新笔记。", "Let me read the next notes when ready"),
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
                                "最近又有了不少新发现，你这附近埋着的“宝贝”可比我想象的要多得多啊！",
                                "I've made quite a few new discoveries lately. There are far more treasures buried around here than I ever imagined!"),
                        List.of(
                                new DialogueOptionDef(
                                        "story_archaeologist_leave_chatter",
                                        LocalizedText.of("期待你早日有新发现。", "Looking forward to your results"),
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
                                "路上的那帮强盗虽然没要了我的命，但把我的货车和样品箱砸了个稀巴烂！你要是能支援我八块皮革、十二根线和六块铁锭，我还能勉强抢救一下剩下的货物。",
                                "Those bandits on the road didn't take my life, but they smashed my wagon and sample chests to splinters! If you could spare eight Leather, twelve String, and six Iron Ingots, I might just be able to salvage the remaining goods."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_gem_merchant_offer_wagon_supplies",
                                                LocalizedText.of("没问题，我帮你把货装好。", "Alright, let me help you pack those"),
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
                                                LocalizedText.of("抱歉，我这会儿也拿不出那么多物资。", "Sorry, I can't gather that much right now"),
                                                DialogueOptionType.BRANCH,
                                                refused,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                        "story_gem_merchant_leave_intro",
                                        LocalizedText.of("你等着，我这就去帮你找找。", "Let me go look for them"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                helped,
                new DialogueNodeDef(
                        helped,
                        LocalizedText.of(
                                "多谢了。好歹命保住了，货物也抢救回一部分，留得青山在，不怕没柴烧！",
                                "Many thanks. At least my life is spared, and I salvaged part of the cargo. Where there's a mountain, there's firewood to be found!"),
                        List.of(
                                new DialogueOptionDef(
                                                "story_gem_merchant_leave_after_wagon_help",
                                                LocalizedText.of("剩下的货你可得看好了。", "You'd better guard the rest well"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        nodes.put(
                refused,
                new DialogueNodeDef(
                        refused,
                        LocalizedText.of(
                                "也罢，真正的商人可不是只靠一辆漂亮的马车吃饭。就凭剩下的这些样品，也足够我重新打开局面了。",
                                "Very well. A true merchant doesn't rely solely on a fancy wagon to make a living. Even with just these remaining samples, it's enough for me to turn things around."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_gem_merchant_leave_after_wagon_refuse",
                                                LocalizedText.of("那你接下来的路可得多加小心。", "Stay careful out there"),
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
                                "我把手头还能见人的样品和旧账本理了一遍，光看这些还不够，我得确认这地方到底值不值得我长期砸钱。你要是下过矿洞，帮我拍张矿脉的照片回来。我倒要看看这边的矿产成色如何，能不能做笔大买卖。",
                                "I've sorted through the presentable samples and old ledgers I have left, but it's not enough. I need to confirm if this place is worth long-term investment. If you ever go down a mine, snap a photo of an ore vein for me. I want to see the quality of the minerals here and if there's a big deal to be made."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_gem_merchant_accept_ore_photo_task",
                                                LocalizedText.of("包在我身上，我替你去探探路。", "Sure, I'll take a look for you"),
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
                                                LocalizedText.of("抱歉，我这趟不去矿洞那边。", "Sorry, not visiting any caves this time"),
                                                DialogueOptionType.BRANCH,
                                                declined,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(2)))));
        nodes.put(
                accepted,
                new DialogueNodeDef(
                        accepted,
                        LocalizedText.of(
                                "只要是有点规模的矿洞就行，能看出矿脉走向就可以，我不挑。",
                                "Any decent-sized mine will do, as long as I can see the direction of the ore veins. I'm not picky."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_gem_merchant_leave_after_accept_photo",
                                                LocalizedText.of("好，这事儿我记下了。", "Got it, I'll remember"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 4)))));
        nodes.put(
                declined,
                new DialogueNodeDef(
                        declined,
                        LocalizedText.of(
                                "无妨。做生意本来就该谨慎行事，我先把自己手头的烂摊子理清楚再说。",
                                "No harm done. Caution is essential in business anyway. I'll just focus on sorting out the mess I have on hand first."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_gem_merchant_leave_after_decline_photo",
                                                LocalizedText.of("没事，那就等下次再说吧。", "Save it for next time then"),
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
                                "账本和样品箱都重新清点过了，剩下的盘缠也核算清楚了。接下来这笔买卖该怎么做大，确实得好好盘算盘算。",
                                "The ledgers and sample chests have been recounted, and my remaining travel funds tallied. Now, I really need to carefully scheme how to turn this into a massive enterprise."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_gem_merchant_reward_settle",
                                                LocalizedText.of(
                                                        "我看这地方风水不错，值得你押一把。",
                                                        "I'd say this place is worth your goods"),
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
                                                        "稳妥起见，还是别把本钱全押上。",
                                                        "I'd say don't stake all your capital yet"),
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
                                                        "我觉得这儿的生意恐怕不太好做。",
                                                        "I don't think this place is quite there yet"),
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
                                                        "觉得这儿不行，就去别处碰碰运气吧。",
                                                        "Maybe you should try elsewhere"),
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
                                "太棒了！货箱修好了，矿脉的成色也远超我的预期。这颗星核你收下，这可是非卖品，就当是额外答谢你的救命之恩了！",
                                "Fantastic! The cargo chests are fixed, and the ore quality far exceeds my expectations. Take this Nether Star—it's strictly not for sale, but consider it an extra reward for saving my life!"),
                        List.of(
                                new DialogueOptionDef(
                                        "story_gem_merchant_leave_after_settle_reward",
                                        LocalizedText.of("那你安心在这儿做生意吧。", "Then go ahead and open shop"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                traveling,
                new DialogueNodeDef(
                        traveling,
                        LocalizedText.of(
                                "还是差了点意思。虽然你帮我保住了部分本钱，这地方也确实有几分潜力，但还没到让我孤注一掷的地步。这星核你拿着，我打算再去别处考察考察。",
                                "Still missing a little something. Though you helped me secure some of my capital, and this place has potential, it's not enough for me to wager everything. Take this Nether Star; I plan to scout for opportunities elsewhere."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_gem_merchant_leave_after_travel_reward",
                                        LocalizedText.of("那你再去别的地方碰碰运气。", "Then go look around elsewhere"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                farewell,
                new DialogueNodeDef(
                        farewell,
                        LocalizedText.of(
                                "虽说这次没帮上什么忙，但也算相识一场，这枚星核就送你了。懂行的人未必每次都能做成买卖，但我看得出谁配得上我压箱底的宝贝。我得继续上路了，去寻觅个更合适的落脚处。",
                                "Though you couldn't help much this time, we did cross paths. Have this Nether Star. A shrewd merchant doesn't close a deal every time, but I know who's worthy of my most prized treasures. I must hit the road again to find a more suitable place to settle."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_gem_merchant_leave_after_farewell",
                                        LocalizedText.of("那就祝你下一单生意兴隆。", "May your next deal go smoothly"),
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
                                "我最近在盘点一批平时绝不轻易示人的尖货。这里的客人品味不错，买东西不只盯着价签看，这在如今可太难得了。",
                                "Lately, I've been inventorying a batch of top-tier goods I rarely show to anyone. The patrons here have good taste; they don't just stare at the price tags when buying, which is incredibly rare these days."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_gem_merchant_leave_final",
                                        LocalizedText.of("等着看你把好东西摆出来。", "Let me see when you put out the good stuff"),
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
                                "我今天在核对旧账。其实做生意最难的，是眼光——看准什么人、什么地方值得你托付心血。相比之下，那些值钱的货物反而都是次要的了。",
                                "I'm checking old ledgers today. Truthfully, the hardest part of business is having an eye for it—knowing exactly who and what places are worth your dedication. Compared to that, the valuable goods are secondary."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_gem_merchant_leave_chatter",
                                        LocalizedText.of("看来你心里已经有底了。", "Sounds like you've got it figured out"),
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
                                "我这面老盾牌倒是还顶得住，但身上的盔甲绑带快要散架了。能不能借我六块铁锭、四块皮革和八团红羊毛？我得把这身行头好好修理一番。",
                                "My old shield can still take a beating, but the straps on my armor are falling apart. Could you lend me six Iron Ingots, four Leather, and eight Red Wool? I need to give this gear a proper repair."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_old_knight_offer_repairs",
                                                LocalizedText.of("给你，先把这些收好。", "Here, take these supplies for now."),
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
                                                LocalizedText.of("抱歉，我这会儿凑不齐你要的材料。", "Sorry, don't have those materials right now"),
                                                DialogueOptionType.BRANCH,
                                                refused,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                        "story_old_knight_leave_intro",
                                        LocalizedText.of("你歇着，我去帮你找找看。", "You rest. I'll go see if I can find them for you."),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                helped,
                new DialogueNodeDef(
                        helped,
                        LocalizedText.of(
                                "呼，总算弄好了。至少这身旧盔甲又能稳稳地穿在身上了。",
                                "Phew, finally done. At least this old set of armor will sit securely on my shoulders once more."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_old_knight_leave_after_help",
                                                LocalizedText.of("先把你的装备保养好吧。", "Then get your kit settled first"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        nodes.put(
                refused,
                new DialogueNodeDef(
                        refused,
                        LocalizedText.of(
                                "也罢。真正的老兵，总得学会带着残破的盔甲继续站得笔挺。",
                                "So be it. A true veteran must learn to stand tall, even in broken armor."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_old_knight_leave_after_refuse",
                                                LocalizedText.of("别逞强了，你自己先站稳脚跟再说。", "Keep your footing first"),
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
                                "感谢你帮我把这身老行头重新拾掇了一遍。能修补的都修补了，实在破得没法看的也只能将就着穿。人啊，一旦停下来歇口气，就会忍不住琢磨以后到底该往哪儿走。",
                                "Thank you for helping me get this old gear back in shape. I fixed what could be fixed, and I'll just have to make do with the rest. You know, once a man stops to catch his breath, he can't help but wonder where his path leads next."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_old_knight_reward_settle",
                                                LocalizedText.of(
                                                        "别硬撑了，干脆就在这儿歇下吧。",
                                                        "Don't push yourself so hard. Why not just rest here?"),
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
                                                        "腿长在你身上，你要走我绝不拦你。",
                                                        "They're your own two feet. If you want to leave, I won't stop you."),
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
                                "这样也好。这面旧盾牌你收下吧，它曾替我挡下过不少灾厄。",
                                "Perhaps that is for the best. Take this old shield; it has warded off many disasters for me over the years."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_old_knight_leave_after_settle_reward",
                                        LocalizedText.of("既然累了，就安心在这儿歇歇脚吧。", "Since you're weary, rest your feet here for a while."),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                traveling,
                new DialogueNodeDef(
                        traveling,
                        LocalizedText.of(
                                "还没到卸甲归田的时候。无妨，老兵的宿命本就是不断前行。等哪天真走累了，我再来敲你这客栈的门。",
                                "It is not yet time to hang up my armor. No matter, an old soldier's fate is to keep marching forward. When I finally tire of the road, I'll come knocking on your inn's door again."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_old_knight_leave_after_travel",
                                        LocalizedText.of("听我一句劝，以后别再往远处折腾了。", "Take my advice, don't wander so far afield anymore."),
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
                                "在这儿歇了几天，这把老骨头总算是没那么酸痛了。人一上了年纪，就不想再成天在风雨里瞎折腾。过去那些沉甸甸的念想和荣耀，如今也该有个安放的地方了。",
                                "After resting here for a few days, these old bones finally ache a little less. When you get on in years, you no longer want to brave the wind and rain every day. Those heavy memories and past glories... it is time they found a place to rest."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_old_knight_leave_final",
                                        LocalizedText.of("那就踏踏实实在这儿养老吧。", "Then settle down and retire here in peace."),
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
                                "我最近正在努力习惯那些没有冲锋号角的清晨。老实说，旅社里这种悠哉的日子，可比在刀尖上舔血舒服多了。",
                                "Lately, I've been trying to get used to mornings without the sound of a charging horn. Truth be told, these leisurely days at the inn are far more comfortable than living on the edge of a blade."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_old_knight_leave_chatter",
                                        LocalizedText.of("奔波大半辈子，你也该享享清福了。", "After half a lifetime on the road, it's time you enjoyed some peace."),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition(OLD_KNIGHT_CHATTER_DIALOGUE_ID, root, nodes);
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
                                "嘿，正好碰见你！我在海边吹了两天海风，画纸快用光了，羽毛笔也画折了一根。你要是手头有三张纸和一根羽毛，能不能先借我顶一顶？等下次见面，我绝对拿点稀罕东西给你开开眼！",
                                "Hey, just the person I wanted to see! I've been braving the sea breeze for two days; I'm almost out of paper, and I even snapped a quill. If you have three pieces of Paper and a Feather on hand, could you lend them to me? Next time we meet, I'll show you something truly rare!"),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cartographer_offer_supplies",
                                                LocalizedText.of(
                                                        "给，这些物资你先拿去对付一下。",
                                                        "Here, take these"),
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
                                                        "我这儿没有多余的物资，你可能得另想办法了。",
                                                        "Don't have any, figure it out"),
                                                DialogueOptionType.BRANCH,
                                                refused,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                        "story_cartographer_leave_intro",
                                        LocalizedText.of("你等着，我这就去帮你找找。", "Let me see if I can find some"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                helped,
                new DialogueNodeDef(
                        helped,
                        LocalizedText.of(
                                "帮大忙了！等我把这张海图绘制完毕，回来一定好好请你喝一杯！",
                                "You're a lifesaver! Once I finish drawing up this nautical chart, I'll definitely come back and buy you a drink!"),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cartographer_end_intro_helped",
                                                LocalizedText.of("路上多加小心，别迷路了。", "Travel safely"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        nodes.put(
                refused,
                new DialogueNodeDef(
                        refused,
                        LocalizedText.of(
                                "没事，我再想想办法凑合一下。反正关键的线索还在脑子里，人没丢就行。",
                                "No worries, I'll figure out a way to make do. The key landmarks are still in my head anyway; as long as I don't get lost, I'll be fine."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cartographer_end_intro_refused",
                                                LocalizedText.of("那祝你接下来的测绘一切顺利。", "Good luck out there"),
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
                                "上次你借我的纸笔可是帮了大忙，海图已经补得七七八八了。现在就差一张海洋的照片，我想跟潮汐的记录再核对一下。你下次出门，能不能顺手帮我拍张海边的风景？",
                                "The paper and pen you lent me last time were a huge help; the nautical chart is mostly filled in. Now I just need a photo of the ocean to cross-reference with my tide records. Next time you head out, could you snap a picture of the seascape for me?"),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cartographer_accept_ocean_photo_task",
                                                LocalizedText.of(
                                                        "放心，出海的时候我会帮你留意这片海域的。",
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
                                                        "实在抱歉，这趟出海我恐怕是帮不上什么忙了。",
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
                                "随便拍就行，只要是海上的景色我都收！海浪、晨雾、海平线，只要拍到了就能帮我校对这该死的地图坐标。",
                                "Just snap anything, I'll take any view of the sea! Waves, morning fog, the horizon—anything you capture will help me calibrate these blasted map coordinates."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cartographer_leave_after_accept_ocean_photo_task",
                                                LocalizedText.of("行，这事儿我记在心上了。", "I'll remember that"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 4)))));
        nodes.put(
                declined,
                new DialogueNodeDef(
                        declined,
                        LocalizedText.of(
                                "行吧，那我先把地图上其他的空白区域填补上。海边的细节等下次有机会再慢慢核对好了。",
                                "Alright then, I'll just fill in the other blank areas on the map first. I can slowly verify the coastal details the next time I get a chance."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cartographer_leave_after_decline_ocean_photo_task",
                                                LocalizedText.of("没事，那就等下次再说吧。", "Save it for next time then"),
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
                                "最近风里雨里跑了不少地方，背包里的草图都快塞不下了，是时候坐下来好好整理一番了。来，你看看这些废稿里有没有你能用得上的？",
                                "I've braved the wind and rain across many lands lately, and my pack is overflowing with sketches. It's time to sit down and sort through them properly. Here, take a look and see if there are any drafts you can use."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cartographer_reward_both_tasks",
                                                LocalizedText.of(
                                                        "看来这回我是真帮上你的大忙了。",
                                                        "I really helped you out this time"),
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
                                                        "能解你的燃眉之急就好。",
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
                                                        "不管怎么说，至少我帮你把关键线索带回来了。",
                                                        "At least I brought back a clue"),
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
                                        LocalizedText.of("惭愧，这次好像没帮上你什么实质性的忙。", "Looks like I didn't help you much this time"),
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
                                "原本我这罗盘，只打算送给真正帮过我大忙的人。但现在看来，光送个罗盘还不足以表达我的谢意。这些金币你也收下吧，算是我的一点心意。",
                                "I originally planned to give this compass only to someone who truly helped me. But now it seems a compass alone isn't enough to express my gratitude. Please take these gold coins as well, as a token of my appreciation."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cartographer_leave_after_both_rewards",
                                                LocalizedText.of("既然如此，那这份谢礼我就不客气地收下了。", "Then I'll gladly take them"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null))));
        nodes.put(
                oneCompleted,
                new DialogueNodeDef(
                        oneCompleted,
                        LocalizedText.of(
                                "谢谢你帮我找齐了这些物件。这个旧罗盘送给你吧，以后出门远行，说不定能派上用场。",
                                "Thank you for finding these items for me. I want you to have this old compass; it might just come in handy on your future travels."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_cartographer_leave_after_one_reward",
                                        LocalizedText.of("多谢，那这份心意我就收下了。", "Thanks, I'll keep this gift safe"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                noneCompleted,
                new DialogueNodeDef(
                        noneCompleted,
                        LocalizedText.of(
                                "旅途中总有错过的风景。没关系，缺的这几笔我先在心里记下。下次见面，咱们聊点开心的。",
                                "There are always missed sights on a journey. No matter, I'll just keep these missing details in mind for now. Let's chat about happier things next time we meet."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cartographer_leave_after_no_reward",
                                                LocalizedText.of("行吧，那咱们下次再聊。", "We'll talk again next time"),
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
                                "这阵子总算能找个平坦的桌子，安安稳稳地把草图摊开来了。以前匆匆忙忙赶路的时候，我老提心吊胆怕图纸被风吹跑了。",
                                "I've finally found a flat table where I can peacefully spread out my sketches. When I was rushing along the road, I was always terrified my maps would blow away in the wind."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_cartographer_leave_final",
                                        LocalizedText.of("看来这地方还挺对你的胃口。", "Seems like this place suits you"),
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
                                "趁着脑子里的画面还清晰，我得赶紧把它们落在纸上。等下回我再来的时候，保证给你讲一段更刺激的探险故事！",
                                "While the images are still fresh in my mind, I need to get them down on paper. When I come back next time, I promise to tell you an even more thrilling tale of adventure!"),
                        List.of(
                                new DialogueOptionDef(
                                        "story_cartographer_leave_chatter",
                                        LocalizedText.of("那等你完成这部分地图我们再聊。", "Then tell me next time"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition(CARTOGRAPHER_CHATTER_DIALOGUE_ID, root, nodes);
    }
}

