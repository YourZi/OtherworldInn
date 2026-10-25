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
                                "路上攒了不少歌，纸快写满了，墨也快没了。能借我五张纸和一个墨囊吗？下次来给你唱首新写的。",
                                "Got a bunch of songs from the road, but I'm almost out of paper and ink. Can you spare five sheets and an ink sac? I'll sing you something new next time."),
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
                                                LocalizedText.of("抱歉，我手头也紧", "Sorry, I'm running short too"),
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
                                "太好了。歌这种东西，不赶紧写下来的话，转头就忘了。",
                                "Perfect. Songs — if you don't write 'em down quick, they're gone by the time you turn around."),
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
                                "没事，没纸我也能编几句。就是不方便记，背一下也是不错的。",
                                "It's fine, I can still make up a few lines without paper. Just awkward to keep — I'll go over them in my head a few times."),
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
                                "这趟见识了好多风景，就差一个能定下副歌的画面了。你要是路过平原，帮我拍张开阔点的照片呗？说不定这首就能收尾了。",
                                "Saw so much this trip — just missing one image to fix the chorus. If you pass the plains, take a wide open shot for me? Might finally finish this one."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_minstrel_accept_plains_photo_task",
                                                LocalizedText.of("好，我下次替你看看", "Sure, I'll keep an eye out next time"),
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
                                "不用刻意找。看着开阔舒服的地方就行。",
                                "Don't overthink it. Somewhere open. Somewhere that feels like it has room left."),
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
                                "没关系，有些歌放久了反而更有意思了，下次再说吧。",
                                "No matter. Some songs only work after they sit a while. Next time."),
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
                                "一路上你帮过的忙，借我的纸墨也好，拍回来的平原也好，我都记着呢。",
                                "All the help you've given me on the road — the paper and ink, the plains photo — I remember it."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_minstrel_reward_settle",
                                                LocalizedText.of(
                                                        "等你唱完再说留下的事",
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
                                                        "至少你写下来了",
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
                                                        "至少你找到了灵感",
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
                                                        "我们这回都没赶上",
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
                                "你说得对。我飘太久了，都快忘了好歌不一定非要在路上唱完。这张唱片你先拿着，等我把后面也理顺了，再回来慢慢唱给你听。",
                                "You're right. I've been drifting so long I almost forgot — a good song doesn't have to be finished on the road. Keep this disc for now. Once I get the rest sorted, I'll come back and sing it for you, no rush."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_minstrel_leave_after_settle_reward",
                                        LocalizedText.of("那我等你唱完", "Then I'll wait to hear the full song"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                traveling,
                new DialogueNodeDef(
                        traveling,
                        LocalizedText.of(
                                "歌是写完了，但是我的旅程还不想停。给你个唱片，就当我把还没唱完的那段先放你这儿了。等我别的地方也转一圈，补上后半段，说不定哪天就顺路绕回来了。",
                                "Song's written, but my feet aren't done. Take the disc — the unsung half, left with you for now. Once I've wandered more and filled it in, maybe I'll circle back this way."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_minstrel_leave_after_travel_reward",
                                        LocalizedText.of("愿有人听你唱歌", "May the next road still give you an audience"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                farewell,
                new DialogueNodeDef(
                        farewell,
                        LocalizedText.of(
                                "这回还是差了口气。没关系，人有碰不上的时候，歌也一样。我先接着赶路，下次再说吧。",
                                "Still a breath short. That's all right — people miss each other, songs too. I'll keep on the road; we'll talk next time."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_minstrel_leave_after_farewell",
                                        LocalizedText.of("那祝你下回顺一点", "Hope next time goes smoother"),
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
                                "我现在挺愿意把脚步放慢的。旅社里的人声、杯子碰来碰去、还有晚风，比赶路的时候更惬意休闲。",
                                "I'm happy to slow down now. The voices here, cups clinking, the evening wind — easier to keep time for a chorus than when you're rushing along."),
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
                                "我在给前几天写的那几句找调子。这里的环境特别惬意，适合慢慢打磨作品。",
                                "Trying to find a tune for the lines I wrote the other day. The light here's easy on the eyes — good for slowly polishing a song."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_minstrel_leave_chatter",
                                        LocalizedText.of("那你接着慢慢磨", "Go on, keep polishing"),
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
                                "我在试下一季的新菜品，总觉得还差一点本地特色口味。你帮我凑一顿家常饭吧：面包、熟牛肉、烤土豆、南瓜派，再来瓶蜂蜜。",
                                "Testing next season's menu, but it still lacks something local. Help me put together a home-style meal: bread, cooked beef, baked potato, pumpkin pie, and a bottle of honey."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_chef_offer_supplies",
                                                LocalizedText.of("行，我给你凑一份", "Alright, I'll put it together for you"),
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
                                                LocalizedText.of("抱歉，我这回凑不齐", "Sorry, I can't gather all that this time"),
                                                DialogueOptionType.BRANCH,
                                                refused,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                        "story_chef_leave_intro",
                                        LocalizedText.of("我先去给你备着", "Let me go prepare them for you"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                helped,
                new DialogueNodeDef(
                        helped,
                        LocalizedText.of(
                                "对，就是这样。做菜嘛都要先尝尝的。",
                                "That's it. Food has to land in a belly first — then the feel comes back."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_chef_leave_after_help",
                                                LocalizedText.of("那我等你把菜做出来", "Then I'll wait to see what you cook"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        nodes.put(
                refused,
                new DialogueNodeDef(
                        refused,
                        LocalizedText.of(
                                "那我先拿别的顶着好了。",
                                "I'll make do with something else for now."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_chef_leave_after_refuse",
                                                LocalizedText.of("那你先慢慢试着", "Take your time figuring it out"),
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
                                "这几天我在这儿前前后后看了几回，心里大概有点数了。现在我还想看看，这地方平时是怎么养活人的。你方便的话，帮我拍张农场动物的照片回来。鸡猪牛羊都行，有生活气息就行。",
                                "Looked around a few times these days; I have a rough sense now. Next I want to see how this place feeds itself. If you can, photograph the farm animals — chickens, pigs, sheep, cows. Just needs to look lived in."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_chef_accept_farm_photo_task",
                                                LocalizedText.of("行，我帮你拍回来", "Sure, I'll bring back a photo for you"),
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
                                                LocalizedText.of("抱歉，这回我去不了", "Sorry, I can't make it this time"),
                                                DialogueOptionType.BRANCH,
                                                declined,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(2)))));
        nodes.put(
                accepted,
                new DialogueNodeDef(
                        accepted,
                        LocalizedText.of(
                                "厨房也好农场也好，有够味的烟火气的地方就可以了。",
                                "Doesn't need to look tidy. Kitchens, farms — places that are really lived in never are."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_chef_leave_after_accept_photo",
                                                LocalizedText.of("好，我知道该拍什么了", "Got it, I know what to look for"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 4)))));
        nodes.put(
                declined,
                new DialogueNodeDef(
                        declined,
                        LocalizedText.of(
                                "没事，忙人也得先顾忙人的事。等你哪天有空，再替我看一眼。",
                                "No problem. Busy hands have their own business. Whenever you find time, take that look for me."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_chef_leave_after_decline_photo",
                                                LocalizedText.of("那就下次再说", "Then save it for next time"),
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
                                "不管怎么样，手感总归是找回来一些。趁着这股热乎劲儿，我也该好好盘算下一季的新菜品了。",
                                "However it went, I've got some of my feel back. While it's still warm, time to plan next season's dishes."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_chef_reward_full",
                                                LocalizedText.of(
                                                        "要不你就留下开灶",
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
                                                        "至少你已经把味道尝出来了",
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
                                                        "至少你看见了",
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
                                                        "你要不再多试几回",
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
                                "那我还会常来的。这饭袋你拿着。等我把新菜品研究好了再叫你来吃热的。",
                                "Then I'll be back often. Take this lunch bag. Once I've sorted the new dishes, I'll call you over for something hot."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_chef_leave_after_full_reward",
                                        LocalizedText.of("那你得给我留个位子", "Save me a seat then"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                partial,
                new DialogueNodeDef(
                        partial,
                        LocalizedText.of(
                                "火候还差点，但我已经不想走了。饭袋你先拿去用，等我研究一下本地口味，再试试新的菜品。",
                                "Heat's still off, but I don't feel like leaving. Take the lunch bag for now — once I really know this place, I'll bring out my best recipes."),
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
                                "这回你没怎么搭上手，可我还是想在这儿多待一阵。",
                                "You didn't get much done this time, but I still want to stay a while."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_chef_leave_after_none_reward",
                                        LocalizedText.of("那你先待一阵", "Then keep the fire going for now"),
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
                                "我最近在琢磨，怎么把这儿的家常味和各处来的旅人的口味揉到一块儿。到时候你记得来吃第一口。",
                                "Lately I'm working out how to blend local home cooking with travellers' tastes. When it's ready, come be the first to try it."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_chef_leave_final",
                                        LocalizedText.of("做好了记得先留我一口", "Save me the first bite when it's ready"),
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
                                "我在你这研究出了好多新菜品，这里人还挺会吃的。",
                                "I've worked out a lot of new dishes here. Folks around here really know how to eat."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_chef_leave_chatter",
                                        LocalizedText.of("那你接着试", "Go on, keep testing"),
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
                                "先说清楚，我不是在求你施舍，我只是想把样子收拾一下看得过去。你要是肯给我两块金锭和十六团白羊毛，至少能让我别寒酸得太难看。",
                                "Let me be clear — this isn't charity, I just want to look presentable. Two gold ingots and sixteen white wool, and I won't look so shabby."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_noble_offer_attire",
                                                LocalizedText.of("行，我帮你", "Fine, I'll help you"),
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
                                                LocalizedText.of("抱歉，我现在拿不出这么多", "Sorry, I can't spare that much right now"),
                                                DialogueOptionType.BRANCH,
                                                refused,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                        "story_noble_leave_intro",
                                        LocalizedText.of("我先去替你找找", "Let me go look for them"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                helped,
                new DialogueNodeDef(
                        helped,
                        LocalizedText.of(
                                "总算不用像刚从泥地里爬出来了。这份人情我记住了。",
                                "Finally I don't look like I crawled out of the mud. I'll remember this."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_noble_leave_after_attire_help",
                                                LocalizedText.of("那你先把这身穿稳了", "Then keep this outfit steady"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        nodes.put(
                refused,
                new DialogueNodeDef(
                        refused,
                        LocalizedText.of(
                                "也罢。要是一个姓氏只剩衣料和镀金撑场面，那本来也没剩多少了。",
                                "Very well. If a name is held together by nothing but cloth and gilt, then perhaps there was little of it left to hold."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_noble_leave_after_attire_refuse",
                                                LocalizedText.of("你这嘴还是够硬", "Your tongue's still sharp"),
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
                                "这大陆上像样的宅子已经没剩几个了。你要是路过林地府邸，替我拍张照片。我想看看，这世上还留着几分旧日门面。",
                                "There aren't many proper estates left on this continent. If you pass a woodland mansion, snap a photo for me. I want to see how much of the old grandeur still stands."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_noble_accept_mansion_photo_task",
                                                LocalizedText.of("行，我替你去看看", "Fine, I'll go look for you"),
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
                                                LocalizedText.of("抱歉，这种地方我未必去得成", "Sorry, I may never make it to such a place"),
                                                DialogueOptionType.BRANCH,
                                                declined,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(2)))));
        nodes.put(
                accepted,
                new DialogueNodeDef(
                        accepted,
                        LocalizedText.of(
                                "我就想看看窗啊墙啊，还有那点门面在就行。",
                                "No need for a good angle. I just want the windows, the walls, whatever face it still has."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_noble_leave_after_accept_photo",
                                                LocalizedText.of("好，我记下了", "Alright, I'll remember"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 4)))));
        nodes.put(
                declined,
                new DialogueNodeDef(
                        declined,
                        LocalizedText.of(
                                "也是。不是谁都愿意专程跑去看一栋还没塌完的旧房子。",
                                "Fair enough. Not everyone wants to go out of their way to look at a ruin that still hasn't finished falling."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_noble_leave_after_decline_photo",
                                                LocalizedText.of("那这回就先算了", "Let's leave it for now"),
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
                                "这几天我想了不少。不管以前那点排场还能剩几分，总得找个地方重新安稳一下。你能借我四颗绿宝石和一颗钻石吗？这笔账我会记着。",
                                "I've thought a lot these days. Whatever pomp is left, this name needs a place to settle again. Can you lend me four emeralds and a diamond? I'll remember it."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_noble_offer_fund",
                                                LocalizedText.of(
                                                        "给，你先拿这笔周转一下",
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
                                                        "现在这样也够你过一阵了",
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
                                                        "至少你心里的东西还在",
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
                                                        "要不你就先留下吧",
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
                                "很好。既然连最后这点周转也有了，我也该承认，这旅社比我当初想的像样得多。这只旧怀表给你吧，我没什么可报答你的了。",
                                "Good. With even this last bit of capital settled, I'll admit it: this inn is far more decent than I first judged. Take this old pocket watch — it's watched too much pointless ceremony. Now you keep it."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_noble_leave_after_full_reward",
                                        LocalizedText.of("那你就在这儿住下吧", "Then settle in here"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                partial,
                new DialogueNodeDef(
                        partial,
                        LocalizedText.of(
                                "我本来以为只是暂时呆一会。可是一旦过惯了有人点灯开门的日子，很难再回到流亡了。没什么可以报答你的，把这只旧怀表拿着吧。",
                                "I thought I'd only stay a while. But once you're used to lamps lit and a door opened for you, it's hard to go back to exile. Nothing else to repay you with — take this old watch."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_noble_leave_after_partial_reward",
                                        LocalizedText.of("那你就安心住下吧", "Then stay and make peace with it"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                none,
                new DialogueNodeDef(
                        none,
                        LocalizedText.of(
                                "别误会，我不是舍不得走。懒得再对另一群陌生人解释我是谁了。这里至少有住的地方，餐点也不算丢人。",
                                "Don't get me wrong — I'm not sorry to leave. I'm just tired of explaining myself to another room of strangers. Here there's a roof, meals that aren't embarrassing."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_noble_leave_after_none_reward",
                                        LocalizedText.of("那你就别再折腾着走了", "Then stop dragging yourself around"),
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
                                "我最近在学着把日子过得像个住客，而不是等着过去回头认错的人。说实话，这地方还真算待得住。",
                                "Lately I've been learning to live like a resident, not someone waiting for the past to come back and apologize. Honestly, this place is surprisingly livable."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_noble_leave_final",
                                        LocalizedText.of("看来你是慢慢适应了", "Looks like you're settling in"),
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
                                "在你这住的多了我也放下了不少，感觉总算清净了。",
                                "Having stayed here so much, I've let go of a lot. It's finally quiet."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_noble_leave_chatter",
                                        LocalizedText.of("你就别再嘴硬了", "You can drop the tough act now"),
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
                                "我路上采的样本，一到地方药性就散掉了。你要是能给我两份下界疣、一份烈焰粉和一个玻璃瓶就好了，我先把这瓶药调了。",
                                "Samples I gather on the road lose their potency the moment I arrive. Two nether wart, a blaze powder, a glass bottle, and I can brew this batch."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_alchemist_offer_supplies",
                                                LocalizedText.of("给，你先拿去用着", "Here, take these for now"),
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
                                                LocalizedText.of("抱歉，我这会儿没有这些", "Sorry, don't have those right now"),
                                                DialogueOptionType.BRANCH,
                                                refused,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                        "story_alchemist_leave_intro",
                                        LocalizedText.of("我先去替你找找", "Let me go look for them"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                helped,
                new DialogueNodeDef(
                        helped,
                        LocalizedText.of(
                                "好，至少这回容器溶剂和材料都齐了。接下来就差个火候的参照。",
                                "Good — vessel, solvent, materials, all here this time. Just need a reference to judge the heat by."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_alchemist_leave_after_help",
                                                LocalizedText.of("那我等你下回结果", "Then I'll wait for your next result"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        nodes.put(
                refused,
                new DialogueNodeDef(
                        refused,
                        LocalizedText.of(
                                "行，那我只能拿剩下的药先硬撑。能不能成，就得看运气了。",
                                "Then I'll push on with what's left of the ingredients. Whether it works is down to luck."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_alchemist_leave_after_refuse",
                                                LocalizedText.of("你自己多小心点", "Stay careful out there"),
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
                                "我这几天先拿手头这些东西反复试了试，现在还缺个靠谱参照。你要是路过下界堡垒，帮我拍张照片回来。我想看看那边的火和石头，现在还够不够我的需求。",
                                "I've been testing with what I have these past few days. Now I need a solid reference. If you pass a Nether fortress, bring back a photo. I want to see if the fire and stone there can still meet my needs."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_alchemist_accept_fortress_photo_task",
                                                LocalizedText.of("行，我替你拍回来", "Sure, I'll bring back a photo"),
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
                                                LocalizedText.of("抱歉，我这回去不了", "Sorry, I can't make it this time"),
                                                DialogueOptionType.BRANCH,
                                                declined,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(2)))));
        nodes.put(
                accepted,
                new DialogueNodeDef(
                        accepted,
                        LocalizedText.of(
                                "拍个大概，能看出来是什么地方就够了。",
                                "Rough shot's fine. As long as I can tell what kind of place it is."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_alchemist_leave_after_accept_photo",
                                                LocalizedText.of("好，我记住了", "Got it, I'll remember"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 4)))));
        nodes.put(
                declined,
                new DialogueNodeDef(
                        declined,
                        LocalizedText.of(
                                "那算了，我找找别的材料也可以，炼金材料都是相通的。",
                                "Then never mind — I can look for other materials. Alchemy ingredients all connect in the end."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_alchemist_leave_after_decline_photo",
                                                LocalizedText.of("那就留到下次吧", "Save it for next time then"),
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
                                "这几天的实验弄得我满手是灰，总算是熬出了一瓶像样的成品。手感找得差不多了，想想这瓶药水能用在哪儿吧。",
                                "Days of experiments left my hands grey, but I finally brewed a decent batch. Got the feel back — now, where would this potion be useful?"),
                        List.of(
                                new DialogueOptionDef(
                                                "story_alchemist_reward_settle",
                                                LocalizedText.of(
                                                        "要不你和药都先留这儿",
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
                                                        "至少你已经调出成品了",
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
                                                        "至少你把火候找回来了",
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
                                                        "你要不再试一轮",
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
                                "行。这瓶好点的就先留给你了。",
                                "Fine. I'll leave you the better bottle."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_alchemist_leave_after_settle_reward",
                                        LocalizedText.of("那我等你下次调得更稳", "Then I'll wait for your next improved batch"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                traveling,
                new DialogueNodeDef(
                        traveling,
                        LocalizedText.of(
                                "我现在是调出了能喝的东西，但是不是我要的最好的结果。这瓶你拿着，算谢你的帮助了。我还得去找下一组材料。",
                                "Close. I've brewed something drinkable, but not a reason to stay. Take the draught — best of this leg. I still need the next set of materials."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_alchemist_leave_after_travel_reward",
                                        LocalizedText.of("你别把自己先炸了", "Don't blow yourself up first"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                farewell,
                new DialogueNodeDef(
                        farewell,
                        LocalizedText.of(
                                "这回取到的数据还不够，暂时下不了结论。没关系，炼金学嘛本来就得多犯错，我先去别处把缺的东西补上。",
                                "Not enough data yet to conclude anything. That's fine — alchemy lets you be wrong a few times. I'll go fill in what's missing."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_alchemist_leave_after_farewell",
                                        LocalizedText.of("那我等你的好消息", "Then I'll wait for your good news"),
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
                                "我最近在研究新的配方。以前的老是失败不成预期，我就想着万一是初始配方错了呢。",
                                "Lately I'm working on new formulas. The old ones kept failing — maybe the base recipe was wrong all along."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_alchemist_leave_final",
                                        LocalizedText.of("那你就慢慢熬着看", "Then take your time and see"),
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
                                "我今天在调一份不靠烈焰起效的药。希望它在该管用的时候别掉链子。",
                                "Today I'm brewing something that doesn't need blaze to work. What I want is for it not to fail when it matters."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_alchemist_leave_chatter",
                                        LocalizedText.of("调好了记得让我看看", "Let me see it when it's ready"),
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
                                "我基本能肯定这大陆下面埋着一层被人忘掉的古文明。但要挖出来，还得要准备准备。借我一把铁镐和十六支火把呗，下次我带真正的线索回来。",
                                "I'm fairly sure a forgotten civilization lies under this land. Digging it out takes preparation — lend me an iron pickaxe and sixteen torches, and next time I'll bring real leads."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_archaeologist_offer_tools",
                                                LocalizedText.of("工具你先拿去用", "Take these tools and start digging"),
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
                                                LocalizedText.of("抱歉，我现在没有这些", "Sorry, don't have these right now"),
                                                DialogueOptionType.BRANCH,
                                                refused,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                        "story_archaeologist_leave_intro",
                                        LocalizedText.of("我先去给你找找", "Let me see if I can find some"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                helped,
                new DialogueNodeDef(
                        helped,
                        LocalizedText.of(
                                "太好了。你帮我省了不少麻烦，没有工具可难受了。",
                                "Right. What ruins archaeology isn't dirt — it's a find turning up and having nothing to hand. You've saved me a lot of grief."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_archaeologist_leave_after_tools_help",
                                                LocalizedText.of("那你记得带证据回来", "Then come back with evidence"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        nodes.put(
                refused,
                new DialogueNodeDef(
                        refused,
                        LocalizedText.of(
                                "那我就继续拿老办法慢慢挖。也不是不能挖，就是慢点。",
                                "Guess I'll keep digging the old way then. Not that I can't — just slower."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_archaeologist_leave_after_tools_refuse",
                                                LocalizedText.of("你下去的时候小心点", "Be careful down there"),
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
                                "我这几天先把手边能看的东西都看了一遍，现在还差一张能说服人的现场照片。你要是路过沙漠神殿或丛林神庙，替我拍一张。入口石头什么的我都想看看。",
                                "Been through everything at hand these past days; what I need is one convincing field photo. If you pass a desert temple or jungle temple, take one for me — the entrance, the stone."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_archaeologist_accept_temple_photo_task",
                                                LocalizedText.of("行，我替你留意一下", "Alright, I'll keep an eye out"),
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
                                                LocalizedText.of("抱歉，这种地方我未必碰得上", "Sorry, I may never run across such a place"),
                                                DialogueOptionType.BRANCH,
                                                declined,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(2)))));
        nodes.put(
                accepted,
                new DialogueNodeDef(
                        accepted,
                        LocalizedText.of(
                                "不用拍得多精致，只要看得出入口之类的结构就够我写不少东西了。",
                                "Doesn't need to be pretty. If I can see the entrance and the lay of the land, that tells me plenty."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_archaeologist_leave_after_accept_photo",
                                                LocalizedText.of("好，我知道该看什么了", "Got it, I know what to look for"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 4)))));
        nodes.put(
                declined,
                new DialogueNodeDef(
                        declined,
                        LocalizedText.of(
                                "也行，遗迹也不会跑，有的是办法看到。那我先把现有笔记理一下。",
                                "Fine. Ruins aren't in a rush to be seen. I'll tidy up the notes I have."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_archaeologist_leave_after_decline_photo",
                                                LocalizedText.of("那我等你下回的发现", "Then I'll wait for your next find"),
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
                                "我这几天研究的差不多了，已经呼之欲出了。再来一块回响碎片和一块远古残骸当样本，这研究就能差不多走通了。你愿意帮我出把力吗？",
                                "Digging up the past is piecing a mirror from broken glass. I've fitted what I can these past days. Bring me an echo shard and ancient debris as samples, and this argument's closed. Care to help?"),
                        List.of(
                                new DialogueOptionDef(
                                                "story_archaeologist_reward_full",
                                                LocalizedText.of(
                                                        "样本我给你带来了",
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
                                                        "样本我带来了",
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
                                                        "听起来你已经查得够深了",
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
                                                        "那你就继续挖吧",
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
                                "太好了，这下就不缺什么了，一切资料都连上了。这把旧刷子你拿着吧，真的很好用。",
                                "Good — no more guessing. Tools, photos, samples all connect. The research can move. Take this old brush; it's been with me a long while and works better than it looks."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_archaeologist_leave_after_full_reward",
                                        LocalizedText.of("那你就把它查透吧", "Then research it through to the end"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                relics,
                new DialogueNodeDef(
                        relics,
                        LocalizedText.of(
                                "这两个样本很有分量。前面的证据虽然还不够整齐，但研究先往下推完全够了。这把刷子送你，老好用了，怎么都用不坏。",
                                "These two samples carry weight. The rest isn't tidy, but it's plenty to push on. Take this brush — old, but it handles far better than new ones."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_archaeologist_leave_after_relic_reward",
                                        LocalizedText.of("那这把刷子我收下了", "Then I'll gladly keep this brush"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                evidence,
                new DialogueNodeDef(
                        evidence,
                        LocalizedText.of(
                                "关键样本还差一点，我不走了。我会继续待在这儿挖到水落石出为止。",
                                "Still missing the key sample, but I don't want to leave now. Twisted clues mean something's still buried. I'll stay and dig till it gives."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_archaeologist_leave_after_evidence_only",
                                        LocalizedText.of("那你加油", "Keep at it"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                none,
                new DialogueNodeDef(
                        none,
                        LocalizedText.of(
                                "这回你帮我补上的不多，可我反倒更确定这地方值得住下。好啃的线索到处都是，难啃的才有意思。",
                                "You didn't fill in much this time, but it makes me more sure this place is worth staying in. Easy leads are everywhere — the stubborn ones are what's interesting."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_archaeologist_leave_after_none",
                                        LocalizedText.of("期待你的成果", "Looking forward to your results"),
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
                                "我最近在把附近的地层结构和风化痕迹重新整理一遍。这里的环境还挺适合琢磨这些东西的。",
                                "Lately I'm redoing the nearby strata and weathering records. This place is actually good for thinking about this sort of thing."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_archaeologist_leave_final",
                                        LocalizedText.of("等你把新笔记整理好", "Let me read the next notes when ready"),
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
                                "最近又有新发现，你这附近好东西还挺多嘛。",
                                "Fresh finds lately — plenty of good things near this place."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_archaeologist_leave_chatter",
                                        LocalizedText.of("期待你的成果", "Looking forward to your results"),
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
                                "路上的劫匪没要我的命，但把货车和样品箱砸得稀巴烂。你要是能给我八块皮革、十二根线和六块铁锭，我稍微还能补一补抢救一下。",
                                "Bandits didn't kill me, but they smashed the wagon and sample chests to bits. Eight leather, twelve string, six iron ingots and I can at least patch the worst of it."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_gem_merchant_offer_wagon_supplies",
                                                LocalizedText.of("行，我帮你把这些装起来", "Alright, let me help you pack those"),
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
                                                LocalizedText.of("抱歉，我现在凑不出这么多", "Sorry, I can't gather that much right now"),
                                                DialogueOptionType.BRANCH,
                                                refused,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                        "story_gem_merchant_leave_intro",
                                        LocalizedText.of("我先去给你找找", "Let me go look for them"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                helped,
                new DialogueNodeDef(
                        helped,
                        LocalizedText.of(
                                "多谢，至少还有保住的货物和我的命，留得青山不怕没柴烧。",
                                "Thanks. At least I still have the cargo that survived — and my life. Where there's a hill, there's firewood."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_gem_merchant_leave_after_wagon_help",
                                                LocalizedText.of("那你可得把剩下的护好了", "You'd better guard the rest well"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        nodes.put(
                refused,
                new DialogueNodeDef(
                        refused,
                        LocalizedText.of(
                                "行吧，商人也不是只靠一辆好车活着。剩下这些样品，还够我撑一阵场面。",
                                "Fine. A merchant doesn't live or die by one good wagon. What's left of these samples is still enough to hold a shop for a while."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_gem_merchant_leave_after_wagon_refuse",
                                                LocalizedText.of("那你接下来多小心", "Stay careful out there"),
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
                                "我把手上还能看的样品和旧账理了理，光看这些也不够，我还得知道这地方值不值得长期投资。你要是下过矿洞，帮我拍张照片回来。我想看看这边的矿脉怎么样，能不能做大生意。",
                                "Sorted the samples and old accounts I have, but that's not enough. I need to know if this region is worth long-term investment. If you've been down a mine, bring me a photo — I want to see whether these veins are luck or real business."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_gem_merchant_accept_ore_photo_task",
                                                LocalizedText.of("行，我替你看一眼", "Sure, I'll take a look for you"),
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
                                                LocalizedText.of("抱歉，我这回不下矿洞", "Sorry, not visiting any caves this time"),
                                                DialogueOptionType.BRANCH,
                                                declined,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(2)))));
        nodes.put(
                accepted,
                new DialogueNodeDef(
                        accepted,
                        LocalizedText.of(
                                "差不多有点规模的矿洞给我看就行，我没什么要求。",
                                "Don't need much ore. If it looks like a real operation, I'll get the idea."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_gem_merchant_leave_after_accept_photo",
                                                LocalizedText.of("好，我记住了", "Got it, I'll remember"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 4)))));
        nodes.put(
                declined,
                new DialogueNodeDef(
                        declined,
                        LocalizedText.of(
                                "无妨。谨慎本来就是生意的一部分，我先把手上这些东西理干净。",
                                "No harm. Caution is part of trade. I'll clear what I have on hand."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_gem_merchant_leave_after_decline_photo",
                                                LocalizedText.of("那就留到下次吧", "Save it for next time then"),
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
                                "账本和样品箱都重新理过一遍了，盘缠也算点清楚了。接下来这生意该怎么做确实该仔细筹划一下。",
                                "Ledger and sample cases sorted again, and the funds are counted. How this business goes forward — time to plan that carefully."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_gem_merchant_reward_settle",
                                                LocalizedText.of(
                                                        "我看这地方值得你押货",
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
                                                        "我看你先别把本钱全押上",
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
                                                        "我看这地方还差点意思",
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
                                                        "你不如再去别处看看",
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
                                "太好了！货箱修回来了，矿脉看着也不差。这颗星核你拿着，本来不是会摆上柜台的东西。算额外谢你的救命之恩了。",
                                "That'll do. Cases fixed, veins look decent. Take this star core — not the sort of thing I'd put on a counter. Thanks, and a bit of trust I'm staking here."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_gem_merchant_leave_after_settle_reward",
                                        LocalizedText.of("那你就在这儿开张吧", "Then go ahead and open shop"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                traveling,
                new DialogueNodeDef(
                        traveling,
                        LocalizedText.of(
                                "还差一点。你帮我保住了部分本钱，也让我看见这地方有点样子，但还没到让我把所有精力都压上的地步。这星核你拿着，我继续去别处再看看。",
                                "Close, but not quite. You saved part of my capital and the place shows promise — not enough to stake everything. Take the star core; I'll compare prices elsewhere."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_gem_merchant_leave_after_travel_reward",
                                        LocalizedText.of("那你就去别处再看看", "Then go look around elsewhere"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                farewell,
                new DialogueNodeDef(
                        farewell,
                        LocalizedText.of(
                                "这回你没帮多少，但是也是缘分一场。这个星核给你了。识货的人不一定总能成交，但我至少认得出谁配看到我压箱底的东西。我继续走走，直到找到更合适的地方。",
                                "You didn't help much this time, but call it fate. Take the star core. Knowing goods doesn't always close a deal — but I can tell who deserves to see my reserves. I'll keep moving till somewhere fits."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_gem_merchant_leave_after_farewell",
                                        LocalizedText.of("那就祝你下笔买卖顺利", "May your next deal go smoothly"),
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
                                "我最近在整理一批以前不会轻易亮出来的货。这里的人看东西不只盯着价，这点挺难得。",
                                "Been reorganizing stock I'd never have displayed lightly before. People here don't just look at price tags — that's rare enough to appreciate."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_gem_merchant_leave_final",
                                        LocalizedText.of("等你把好货摆出来", "Let me see when you put out the good stuff"),
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
                                "我今天在对旧账。做生意最难是认出谁和什么地方值得交货，值钱的东西反而是最次要的了。",
                                "Going through old accounts today. The hard part of trade is knowing who and where deserves the goods — the valuables are the least of it."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_gem_merchant_leave_chatter",
                                        LocalizedText.of("听你这么说，你心里挺有数", "Sounds like you've got it figured out"),
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
                                "我的盾还顶得住，但是身上盔甲绑带都快散了。你借我六块铁锭、四块皮革和八团红羊毛怎么样，我收拾修理一下。",
                                "Shield still holds, but the straps on this armor are coming apart. Lend me six iron ingots, four leather, eight red wool, and I'll patch it up."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_old_knight_offer_repairs",
                                                LocalizedText.of("给，你先收好", "Here, take these first"),
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
                                                LocalizedText.of("抱歉，我现在没有这些材料", "Sorry, don't have those materials right now"),
                                                DialogueOptionType.BRANCH,
                                                refused,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                        "story_old_knight_leave_intro",
                                        LocalizedText.of("我先去替你找找", "Let me go look for those"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                helped,
                new DialogueNodeDef(
                        helped,
                        LocalizedText.of(
                                "好了。至少重新把旧装备穿稳了。",
                                "There. At least the old kit's strapped on straight again."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_old_knight_leave_after_help",
                                                LocalizedText.of("那你先把行头收拾好了", "Then get your kit settled first"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(2, 5)))));
        nodes.put(
                refused,
                new DialogueNodeDef(
                        refused,
                        LocalizedText.of(
                                "也罢。老兵总得学会带着缺口继续站着。",
                                "So be it. An old soldier learns to stand even with pieces missing."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_old_knight_leave_after_refuse",
                                                LocalizedText.of("你自己先站稳了", "Keep your footing first"),
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
                                "感谢你帮我把旧行头重新看了一遍。能收拾的先收拾，收拾不齐的也只能先背着。人一缓下来就会开始琢磨自己后面还想怎么走。",
                                "Thanks for going through my old kit with me. Fixed what I could, carried the rest. Once you slow down, you start wondering where you want to go next."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_old_knight_reward_settle",
                                                LocalizedText.of(
                                                        "要不你就先歇下吧",
                                                        "Why don't you rest for now"),
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
                                                        "你要走，我也不拦你",
                                                        "If you want to leave, I won't stop you"),
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
                                "也好。这面旧盾你拿着，它替我挡过不少事。",
                                "Very well. Take this old shield — it's taken plenty of hits for me."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_old_knight_leave_after_settle_reward",
                                        LocalizedText.of("那你就安心歇着吧", "Then rest easy here"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                traveling,
                new DialogueNodeDef(
                        traveling,
                        LocalizedText.of(
                                "还没到卸甲的时候。没事，老兵本来就该继续往前走。哪天走累了我再回来敲你的门。",
                                "Not time to hang up the armor. That's fine — old soldiers keep walking. When I'm truly tired, I'll knock on your door again."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_old_knight_leave_after_travel",
                                        LocalizedText.of("别再走太远了", "Just don't wander too far"),
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
                                "在这儿歇了几天，老骨头总算没那么痛了。人一上了年纪，就不想总在路上瞎折腾。过去那些沉甸甸的念想，今天也该有个着落了。",
                                "Few days of rest and these old bones finally ache a little less. When you get old, you stop wanting to wear yourself out on the road. All those heavy thoughts from before — today feels right to put them down."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_old_knight_leave_final",
                                        LocalizedText.of("那就让待在这儿吧", "Then let it stay here with you"),
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
                                "最近在学着习惯没有号角叫你起床的早晨，旅社里这种慢日子比打仗舒服多了。",
                                "Getting used to mornings without a horn. Slow days at an inn are more comfortable than fighting, honestly."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_old_knight_leave_chatter",
                                        LocalizedText.of("你也该享几天福了", "About time you enjoyed a few days"),
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
                                "嘿，正好碰见你。我在海边跑了两天，纸快用光了，羽毛笔也折了一根。你要是手头有三张纸和一根羽毛，先借我顶一顶？下回我拿点好东西给你看。",
                                "Hey, just the person. I've been running along the coast for two days — almost out of paper and snapped a quill too. If you've got three sheets and a feather on you, can you spot me? I'll show you something good next time."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cartographer_offer_supplies",
                                                LocalizedText.of(
                                                        "给，拿去用吧",
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
                                                        "我这没有，你另想办法",
                                                        "Don't have any, figure it out"),
                                                DialogueOptionType.BRANCH,
                                                refused,
                                                null)
                                        .withEffects(DialogueEffectDef.advanceStoryStage(1)),
                                new DialogueOptionDef(
                                        "story_cartographer_leave_intro",
                                        LocalizedText.of("我先去给你找找", "Let me see if I can find some"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                helped,
                new DialogueNodeDef(
                        helped,
                        LocalizedText.of(
                                "帮大忙了！等我把这张图画完，回来一定好好谢你。",
                                "Huge help! Once I finish this map, I'll come back and thank you properly."),
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
                                "没事，我再凑合凑合。反正线索还在，人没丢就行。",
                                "No worries, I'll make do. The clues are still there, and I haven't gotten lost — that's what counts."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cartographer_end_intro_refused",
                                                LocalizedText.of("那祝你接下来顺利", "Good luck out there"),
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
                                "上次那些纸笔帮了大忙，海图差不多补完了。还差一张海洋的照片，我想跟潮汐记录对一对。你下次出门，帮我拍张海边的照片呗？",
                                "Those supplies really helped — chart's almost done. Just need an ocean photo to cross-check my tide notes. Next time you're out, can you snap one of the sea for me?"),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cartographer_accept_ocean_photo_task",
                                                LocalizedText.of(
                                                        "我替你留意海上",
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
                                "随便拍就行，只要是海上的景色都行。海浪、雾气、海平线，拍到了就能帮我校地图。",
                                "Just snap whatever. Any ocean view works — waves, fog, the horizon. Whatever you get helps me check the map."),
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
                                "行，那我先把别的空白补上。海边等下回再慢慢对好了。",
                                "All right, I'll fill the other blanks first. The coast can wait till next time."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cartographer_leave_after_decline_ocean_photo_task",
                                                LocalizedText.of("那就留到下次吧", "Save it for next time then"),
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
                                "最近跑了不少地方，包里的草图也攒了一摞，休息整理一下吧。来，看看有没有你用得上的。",
                                "Been to a lot of places lately; sketches are piling up. Time to sit down and sort them. Have a look — anything you can use?"),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cartographer_reward_both_tasks",
                                                LocalizedText.of(
                                                        "这回我确实帮上你了",
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
                                                        "至少我还带回了线索",
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
                                        LocalizedText.of("我没帮上什么", "Looks like I didn't help you much this time"),
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
                                "本来只想把罗盘给真正帮过我的人。现在看来光给个罗盘还差点意思。这金币也拿着，算我谢你了。",
                                "I only meant to give this compass to someone who really helped. A compass alone feels thin now. Take these coins too — from me, and from the sea."),
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
                                "感谢你帮我找这么多东西了，这个罗盘给你，往后你自己出门说不定也用得上。",
                                "Supplies or that seascape — either way I owe you. Take this compass; might come in handy when you go roaming."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_cartographer_leave_after_one_reward",
                                        LocalizedText.of("谢谢，这份礼我收着", "Thanks, I'll keep this gift safe"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                noneCompleted,
                new DialogueNodeDef(
                        noneCompleted,
                        LocalizedText.of(
                                "路上总有些事赶不上。没事，没画完的我先记着。下回见面聊点轻松的。",
                                "Some things on the road just don't come together. No hard feelings. I'll keep the loose ends in mind — next time let's just chat about something easy."),
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
                                "这阵子总算能安安稳稳把草图摊开了。以前匆忙赶路的时候老怕丢东西了。",
                                "Finally I can spread the sketches out in peace. On the road I was always afraid of losing things. Now I can draw slowly."),
                        List.of(
                                new DialogueOptionDef(
                                        "story_cartographer_leave_final",
                                        LocalizedText.of("看来这里挺适合你", "Seems like this place suits you"),
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
                                "趁还记得清楚，我得赶紧画到图上。下回我再来的时候再给你讲段更有意思的啦。",
                                "Better get these wind and tide marks down while I still remember. Next time I'm through, I'll have a better story."),
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

