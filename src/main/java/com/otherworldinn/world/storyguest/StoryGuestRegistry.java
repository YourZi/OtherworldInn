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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class StoryGuestRegistry {
    private static final ResourceLocation TEXTURE_WANDERING_CARTOGRAPHER =
            ResourceLocation.fromNamespaceAndPath(
                    OtherworldInn.MODID, "textures/entity/guest/ordinary_guest/1.png");
    private static final ResourceLocation PAPER =
            ResourceLocation.fromNamespaceAndPath("minecraft", "paper");
    private static final ResourceLocation FEATHER =
            ResourceLocation.fromNamespaceAndPath("minecraft", "feather");
    private static final ResourceLocation NATURES_COMPASS =
            ResourceLocation.fromNamespaceAndPath("naturescompass", "naturescompass");
    private static final String CARTOGRAPHER_HELPED_FLAG = "cartographer_helped";
    private static final String CARTOGRAPHER_REFUSED_FLAG = "cartographer_refused";
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
                        buildCartographerVisitChatterDialogue());
        return new StoryGuestDefinition(
                id,
                LocalizedText.of("流浪绘图师 伊莱", "Eli the Wandering Cartographer"),
                TEXTURE_WANDERING_CARTOGRAPHER,
                "slim",
                0,
                profile,
                2,
                5,
                1,
                1,
                Map.of(
                        0, "story_cartographer_stage0",
                        1, "story_cartographer_stage1",
                        2, "story_cartographer_stage2"),
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
                                        .withEffects(
                                                DialogueEffectDef.setStoryFlag(CARTOGRAPHER_REFUSED_FLAG),
                                                DialogueEffectDef.advanceStoryStage(1)),
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
        String repaid = "repaid";
        String noHelp = "no_help";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "我认得这家旅社的灯。上次的海图已经补好了，刚好能来兑现一句承诺。",
                                "I recognized the lights of this inn. I finished that coastal chart, so I came back to make good on a promise."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cartographer_collect_thanks",
                                                LocalizedText.of(
                                                        "上次的旅途后来怎样了？",
                                                        "How did that trip turn out?"),
                                                DialogueOptionType.BRANCH,
                                                repaid,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasStoryFlag(
                                                        CARTOGRAPHER_HELPED_FLAG))
                                        .withEffects(
                                                DialogueEffectDef.giveItem(NATURES_COMPASS, 1),
                                                DialogueEffectDef.advanceStoryStage(2)),
                                new DialogueOptionDef(
                                                "story_cartographer_hear_story",
                                                LocalizedText.of(
                                                        "后来你还是把地图画完了？",
                                                        "So you still finished the map?"),
                                                DialogueOptionType.BRANCH,
                                                noHelp,
                                                null)
                                        .withRequirements(
                                                DialogueRequirementDef.hasStoryFlag(
                                                        CARTOGRAPHER_REFUSED_FLAG))
                                        .withEffects(DialogueEffectDef.advanceStoryStage(2)),
                                new DialogueOptionDef(
                                        "story_cartographer_leave_return",
                                        LocalizedText.of("改天再细聊", "Let's talk another time"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                repaid,
                new DialogueNodeDef(
                        repaid,
                        LocalizedText.of(
                                "多亏你那几样补给，我没在海雾里把方向丢掉。这只指南针被我调过，送给你，愿它以后总能把人带回这间旅社。",
                                "Thanks to those supplies, I never lost my bearings in the sea fog. I tuned this compass myself. Keep it, and may it always lead people back to this inn."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cartographer_finish_helped_arc",
                                                LocalizedText.of("我会收好的", "I'll treasure it"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(4, 7)))));
        nodes.put(
                noHelp,
                new DialogueNodeDef(
                        noHelp,
                        LocalizedText.of(
                                "最后我把地图也画完了，只是慢了些。说来也怪，路上总觉得要是当时在这儿多坐一会儿，也许会更安心。",
                                "I finished the map in the end, just a little slower. Funny thing is, on the road I kept thinking that if I'd lingered here a little longer, I might've felt steadier."),
                        List.of(
                                new DialogueOptionDef(
                                                "story_cartographer_finish_refused_arc",
                                                LocalizedText.of("欢迎你以后常来", "You're always welcome here"),
                                                DialogueOptionType.BRANCH,
                                                null,
                                                null)
                                        .withEffects(DialogueEffectDef.setNextVisitRange(4, 7)))));
        return new DialogueDefinition("story_cartographer_stage1", root, nodes);
    }

    private static DialogueDefinition buildCartographerStage2Dialogue() {
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
        return new DialogueDefinition("story_cartographer_stage2", root, nodes);
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
