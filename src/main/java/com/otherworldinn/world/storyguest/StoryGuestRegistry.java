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
    private static final ResourceLocation PAPER =
            ResourceLocation.fromNamespaceAndPath("minecraft", "paper");
    private static final ResourceLocation FEATHER =
            ResourceLocation.fromNamespaceAndPath("minecraft", "feather");
    private static final ResourceLocation NATURES_COMPASS =
            ResourceLocation.fromNamespaceAndPath("naturescompass", "naturescompass");
    private static final String CARTOGRAPHER_HELPED_FLAG = "cartographer_helped";
    private static final String CARTOGRAPHER_CHATTER_DIALOGUE_ID = "story_cartographer_visit_chatter";

    private static final Map<String, StoryGuestDefinition> DEFINITIONS_BY_ID;
    private static final List<DialogueDefinition> ALL_DIALOGUES;

    static {
        StoryGuestDefinition wanderingCartographer = buildWanderingCartographer();
        Map<String, StoryGuestDefinition> byId = new LinkedHashMap<>();
        byId.put(wanderingCartographer.id(), wanderingCartographer);
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
