package com.otherworldinn.world.dialogue;

import com.otherworldinn.entity.base.GuestEntity;
import com.otherworldinn.entity.store.BlacksmithEntity;
import com.otherworldinn.entity.store.FarmerEntity;
import com.otherworldinn.entity.store.GrocerEntity;
import com.otherworldinn.entity.store.MagicianEntity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public final class DialogueRegistry {
    public static final String FUNCTION_OPEN_STORE = "open_store";
    public static final String FUNCTION_OPEN_VIRTUAL_ANVIL = "open_virtual_anvil";


    private static final DialogueDefinition BLACKSMITH_DIALOGUE =
            buildStoreDialogueWithFacilityTopics(
                    "blacksmith",
                    LocalizedText.of(
                        "铁火未冷，要打什么？", 
                        "The forge is hot. What are you after?"),
                    LocalizedText.of(
                        "矿石、矿锭、工具都在这，挑你顺手的", 
                        "Ores, ingots, and tools. Pick what suits your hand."),
                    null,
                    LocalizedText.of("关于锅炉房", "About the Boiler Room"),
                    null,
                    null,
                    LocalizedText.of("地下那座锅炉房好久没人修理了，不知道还能不能用", 
                                     "No one has repaired the boiler room downstairs for a long time. I don't even know if it still works."),
                    LocalizedText.of("你把它修好了？太好了，我们的机器可以用了",
                                     "You repaired it? That's great."),
                    LocalizedText.of("它在什么位置？", "Where is it?"),
                    LocalizedText.of(
                            "在前面右转，穿过集市再右转的小通道里。",
                            "Turn right ahead, pass through the market, then take the small alley on the right."),
                    LocalizedText.of("借用一下铁砧", "Borrow the Anvil"),
                    FUNCTION_OPEN_VIRTUAL_ANVIL);
    private static final DialogueDefinition FARMER_DIALOGUE =
            buildStoreDialogueWithFacilityTopics(
                    "farmer",
                    LocalizedText.of(
                        "田里刚收了货，想买点啥？", 
                        "Fresh harvest just came in. Need anything?"),
                    LocalizedText.of(
                        "种子、作物、农具都有，慢慢挑", 
                        "Seeds, produce, and farming goods. Take your time."),
                    LocalizedText.of("关于温室", "About the Greenhouse"),
                    null,
                    LocalizedText.of("对面那座温室废弃很久了...以前可好用了",
                                     "That greenhouse across the way has been abandoned for ages... it used to be so useful."),
                    LocalizedText.of("你居然真的把它修好了，感谢你的付出，现在种地更方便了", 
                                     "You actually got it repaired. Thank you for putting in the work."),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);
    private static final DialogueDefinition MAGICIAN_DIALOGUE =
            buildSimpleStoreDialogue(
                    "magician",
                    LocalizedText.of(
                        "你面前这么可爱的魔女是谁呢？没错，就是我，你需要什么？", 
                        "The arcane stirs. What do you seek?"),
                    LocalizedText.of(
                        "材料和书卷都在这，想要什么自己挑",
                        "Materials and tomes are ready. Mind the explosive stuff."));
    private static final DialogueDefinition GROCER_DIALOGUE =
            buildGrocerDialogue(
                    "grocer",
                    LocalizedText.of(
                            "要买什么？我这里什么都有",
                            "What do you want to buy? I have everything."),
                    LocalizedText.of(
                            "一些乱七八糟的东西，随意看看吧",
                            "A bunch of odds and ends. Feel free to browse."));
    private static final List<DialogueDefinition> GUEST_DIALOGUES =
            List.of(
                    buildGuestLineDialogue(
                            "guest_ordinary_welcome",
                            LocalizedText.of(
                                    "这里比我想象中更温暖，今晚应该能睡个好觉。",
                                    "This place feels warmer than I expected. I might sleep well tonight.")),
                    buildGuestLineDialogue(
                            "guest_ordinary_weather",
                            LocalizedText.of(
                                    "外面的风好大，幸好旅馆里很安静。",
                                    "The wind outside is harsh. Glad it's calm in the inn.")),
                    buildGuestLineDialogue(
                            "guest_ordinary_checkout",
                            LocalizedText.of(
                                    "明天我就启程，今晚先好好休息。",
                                    "I leave at dawn tomorrow. Tonight, I just need good rest.")),
                    buildGuestLineDialogue(
                            "guest_ordinary_bed",
                            LocalizedText.of(
                                    "这张床看起来挺舒服的，希望被子够暖和。",
                                    "This bed looks comfortable. Hope the blankets are warm enough.")),
                    buildGuestLineDialogue(
                            "guest_ordinary_food",
                            LocalizedText.of(
                                    "听说这里的家常菜不错，晚上得尝尝。",
                                    "I heard the home-style cooking here is good. Should try it tonight.")),
                    buildGuestLineDialogue(
                            "guest_ordinary_bath",
                            LocalizedText.of(
                                    "赶了一天路，能洗个热水澡真是太好了。",
                                    "After a long day's travel, a hot bath is exactly what I need.")),
                    buildGuestLineDialogue(
                            "guest_ordinary_fireplace",
                            LocalizedText.of(
                                    "壁炉里的火真暖和，坐在这儿看书应该不错。",
                                    "The fireplace is so warm. Would be nice to read a book here.")),
                    buildGuestLineDialogue(
                            "guest_ordinary_morning",
                            LocalizedText.of(
                                    "早上空气真好，适合出去走走。",
                                    "The morning air is refreshing. Good for a walk.")),
                    buildGuestLineDialogue(
                            "guest_ordinary_rain",
                            LocalizedText.of(
                                    "下雨天待在旅馆里真是明智的选择。",
                                    "Staying at the inn during rain was a wise choice.")),
                    buildGuestLineDialogue(
                            "guest_ordinary_memory",
                            LocalizedText.of(
                                    "这地方让我想起小时候住过的老旅馆。",
                                    "This place reminds me of an old inn from my childhood.")),
                    buildGuestLineDialogue(
                            "guest_ordinary_recommend",
                            LocalizedText.of(
                                    "朋友推荐我来这里的，果然没让我失望。",
                                    "A friend recommended this place. It hasn't disappointed.")),
                    buildGuestLineDialogue(
                            "guest_ordinary_return",
                            LocalizedText.of(
                                    "下次路过这里，我还会再来住的。",
                                    "I'll stay here again next time I pass through.")),


                    buildGuestLineDialogue(
                            "guest_rich_service",
                            LocalizedText.of(
                                    "如果服务继续这么周到，我会常来。",
                                    "If the service stays this refined, I'll return often.")),
                    buildGuestLineDialogue(
                            "guest_rich_wine",
                            LocalizedText.of(
                                    "要是晚餐再配点好酒，就更完美了。",
                                    "A fine wine with dinner would make this place perfect.")),
                    buildGuestLineDialogue(
                            "guest_rich_tip",
                            LocalizedText.of(
                                    "照顾得不错，退房时我可能会留点小费。",
                                    "Good care deserves a tip when I check out.")),
                    buildGuestLineDialogue(
                            "guest_rich_room",
                            LocalizedText.of(
                                    "房间布置得还算雅致，就是缺个衣帽间。",
                                    "The room is elegantly decorated, though it lacks a walk-in closet.")),
                    buildGuestLineDialogue(
                            "guest_rich_view",
                            LocalizedText.of(
                                    "窗外的景色不错，可惜不是海景。",
                                    "The view from the window is nice, though not oceanfront.")),
                    buildGuestLineDialogue(
                            "guest_rich_business",
                            LocalizedText.of(
                                    "这里的环境适合谈生意，比较安静。",
                                    "The atmosphere here is suitable for business discussions.")),
                    buildGuestLineDialogue(
                            "guest_rich_linen",
                            LocalizedText.of(
                                    "床品的质感还可以，是棉的吗？",
                                    "The bedding has decent texture. Is it cotton?")),
                    buildGuestLineDialogue(
                            "guest_rich_quiet",
                            LocalizedText.of(
                                    "我付了高价就是图个清静，别让我失望。",
                                    "I'm paying premium for peace and quiet. Don't disappoint.")),
                    buildGuestLineDialogue(
                            "guest_rich_referral",
                            LocalizedText.of(
                                    "我会把这里推荐给其他讲究的朋友。",
                                    "I'll recommend this place to other discerning friends.")),
                    buildGuestLineDialogue(
                            "guest_rich_standard",
                            LocalizedText.of(
                                    "以这个价位来说，服务水准还算达标。",
                                    "For this price range, the service standard is acceptable.")),

                    buildGuestLineDialogue(
                            "guest_heavy_pack_route",
                            LocalizedText.of(
                                    "我背着这么多货，能有个落脚点真不容易。",
                                    "Traveling with this much cargo, a safe stop is priceless.")),
                    buildGuestLineDialogue(
                            "guest_heavy_pack_storage",
                            LocalizedText.of(
                                    "这些箱子先放这儿，别让它们淋雨了。",
                                    "Let me stash these crates here. Keep them out of the rain.")),
                    buildGuestLineDialogue(
                            "guest_heavy_pack_food",
                            LocalizedText.of(
                                    "跑了一整天，先来份热乎的饭再说。",
                                    "I've been on the road all day. A hot meal comes first.")),
                    buildGuestLineDialogue(
                            "guest_heavy_pack_road",
                            LocalizedText.of(
                                    "前面的路不好走，得养足精神再出发。",
                                    "The road ahead is rough. Need to rest well before continuing.")),
                    buildGuestLineDialogue(
                            "guest_heavy_pack_repair",
                            LocalizedText.of(
                                    "车轮有点松了，附近有铁匠铺吗？",
                                    "The wheel's loose. Is there a blacksmith nearby?")),
                    buildGuestLineDialogue(
                            "guest_heavy_pack_early",
                            LocalizedText.of(
                                    "明天天不亮就得走，麻烦早点准备早餐。",
                                    "I leave before dawn tomorrow. Please prepare breakfast early.")),
                    buildGuestLineDialogue(
                            "guest_heavy_pack_weather",
                            LocalizedText.of(
                                    "看这天色要变，幸好找到地方落脚了。",
                                    "The weather looks changing. Glad I found shelter.")),
                    buildGuestLineDialogue(
                            "guest_heavy_pack_rest",
                            LocalizedText.of(
                                    "肩膀都压麻了，得好好歇一晚。",
                                    "My shoulders are numb from carrying. Need a good night's rest.")),
                    buildGuestLineDialogue(
                            "guest_heavy_pack_return",
                            LocalizedText.of(
                                    "回程的时候还会路过，记得给我留间房。",
                                    "I'll pass through on the return trip. Save me a room.")),

                    // Ultra Rich Guest Dialogues (12 total)
                    buildGuestLineDialogue(
                            "guest_ultra_rich_suite",
                            LocalizedText.of(
                                    "这间套房勉强合格，希望夜里足够安静。",
                                    "This suite is acceptable. I expect absolute quiet at night.")),
                    buildGuestLineDialogue(
                            "guest_ultra_rich_privacy",
                            LocalizedText.of(
                                    "我不喜欢被打扰，安排人手时请注意。",
                                    "I dislike interruptions. Make sure your staff knows that.")),
                    buildGuestLineDialogue(
                            "guest_ultra_rich_guard",
                            LocalizedText.of(
                                    "我的行李很贵重，安保别出差错。",
                                    "My luggage is valuable. Security must not fail.")),
                    buildGuestLineDialogue(
                            "guest_ultra_rich_staff",
                            LocalizedText.of(
                                    "让最经验丰富的员工来服务，新手不要。",
                                    "Assign your most experienced staff. No trainees.")),
                    buildGuestLineDialogue(
                            "guest_ultra_rich_perfection",
                            LocalizedText.of(
                                    "细节决定品质，枕头的高度要刚刚好。",
                                    "Details define quality. The pillow height must be perfect.")),
                    buildGuestLineDialogue(
                            "guest_ultra_rich_expectation",
                            LocalizedText.of(
                                    "我习惯的标准很高，希望你们跟得上。",
                                    "My standards are exceptionally high. I hope you can meet them.")),
                    buildGuestLineDialogue(
                            "guest_ultra_rich_discretion",
                            LocalizedText.of(
                                    "我的行程要保密，不要对外透露。",
                                    "My itinerary is confidential. Do not disclose it.")),
                    buildGuestLineDialogue(
                            "guest_ultra_rich_taste",
                            LocalizedText.of(
                                    "装饰品的品味还可以，是真品吗？",
                                    "The decor shows some taste. Are these pieces authentic?")),
                    buildGuestLineDialogue(
                            "guest_ultra_rich_investment",
                            LocalizedText.of(
                                    "这家旅馆有投资潜力，我会关注一下。",
                                    "This inn has investment potential. I'll keep an eye on it.")),
                    buildGuestLineDialogue(
                            "guest_ultra_rich_legacy",
                            LocalizedText.of(
                                    "老牌旅馆就是不一样，底蕴深厚。",
                                    "Established inns are different. They have depth.")),

                    // VIP Guest Dialogues (12 total)
                    buildGuestLineDialogue(
                            "guest_vip_ordinary_schedule",
                            LocalizedText.of(
                                    "我的行程很紧，麻烦按时叫醒我。",
                                    "My schedule is tight. Wake me on time, please.")),
                    buildGuestLineDialogue(
                            "guest_vip_ordinary_tea",
                            LocalizedText.of(
                                    "如果能送一壶热茶到房间就更好了。",
                                    "A pot of hot tea to the room would be lovely.")),
                    buildGuestLineDialogue(
                            "guest_vip_ordinary_review",
                            LocalizedText.of(
                                    "服务不错，我会给旅馆写个好评。",
                                    "The service is excellent. I'll leave a positive review.")),
                    buildGuestLineDialogue(
                            "guest_vip_advanced_security",
                            LocalizedText.of(
                                    "我的随从稍后到，先把房门权限准备好。",
                                    "My attendants arrive later. Prepare room access in advance.")),
                    buildGuestLineDialogue(
                            "guest_vip_advanced_order",
                            LocalizedText.of(
                                    "晚些时候我会点餐，记得用最好的食材。",
                                    "I'll place an order later. Use your finest ingredients.")),
                    buildGuestLineDialogue(
                            "guest_vip_advanced_reward",
                            LocalizedText.of(
                                    "把事情办漂亮了，回头少不了赏金。",
                                    "Do this properly, and there will be a reward.")),
                    buildGuestLineDialogue(
                            "guest_vip_feedback",
                            LocalizedText.of(
                                    "服务流程很规范，我会向上级汇报。",
                                    "The service process is well-standardized. I'll report upward.")),

                    // Sponsor Guest Dialogues (12 total)
                    buildGuestLineDialogue(
                            "guest_sponsor_photo",
                            LocalizedText.of(
                                    "这里氛围真棒，我想拍张照留念。",
                                    "The vibe here is great. I want to take a commemorative photo.")),
                    buildGuestLineDialogue(
                            "guest_sponsor_renovation",
                            LocalizedText.of(
                                    "旅馆改造后舒服多了，辛苦你们了。",
                                    "The renovations made this place so much nicer. Well done.")),
                    buildGuestLineDialogue(
                            "guest_sponsor_support",
                            LocalizedText.of(
                                    "继续加油，我会一直支持这家旅馆。",
                                    "Keep it up. I'll keep supporting this inn.")),
                    buildGuestLineDialogue(
                            "guest_sponsor_growth",
                            LocalizedText.of(
                                    "看到旅馆越来越好，我很欣慰。",
                                    "It's heartwarming to see the inn improving.")),
                    buildGuestLineDialogue(
                            "guest_sponsor_community",
                            LocalizedText.of(
                                    "这里有种家的感觉，旅客们都很友善。",
                                    "This place feels like home. The guests are all friendly.")),
                    buildGuestLineDialogue(
                            "guest_sponsor_tradition",
                            LocalizedText.of(
                                    "老旅馆的味道保留得很好，有情怀。",
                                    "The old inn charm is well preserved. It has character.")),
                    buildGuestLineDialogue(
                            "guest_sponsor_recommend",
                            LocalizedText.of(
                                    "我会向更多朋友推荐这里，好地方要分享。",
                                    "I'll recommend this to more friends. Good places should be shared.")),
                    buildGuestLineDialogue(
                            "guest_sponsor_improvement",
                            LocalizedText.of(
                                    "还有什么需要改进的？我可以提供建议。",
                                    "What else needs improvement? I can offer suggestions.")),
                    buildGuestLineDialogue(
                            "guest_sponsor_future",
                            LocalizedText.of(
                                    "期待旅馆未来的发展，我会持续关注。",
                                    "Looking forward to the inn's future development. I'll keep watching.")),
                    buildGuestLineDialogue(
                            "guest_sponsor_appreciation",
                            LocalizedText.of(
                                    "感谢你们保持这里的原汁原味，很难得。",
                                    "Thank you for maintaining the authentic feel. It's rare.")));

    private static final List<DialogueDefinition> ALL_DIALOGUES;

    private static final Map<String, DialogueDefinition> DIALOGUE_BY_ID;

    static {
        List<DialogueDefinition> all =
                new ArrayList<>(
                        List.of(BLACKSMITH_DIALOGUE, FARMER_DIALOGUE, MAGICIAN_DIALOGUE, GROCER_DIALOGUE));
        all.addAll(GUEST_DIALOGUES);
        ALL_DIALOGUES = Collections.unmodifiableList(all);
        Map<String, DialogueDefinition> byId = new LinkedHashMap<>();
        for (DialogueDefinition dialogue : ALL_DIALOGUES) {
            byId.put(dialogue.id(), dialogue);
        }
        DIALOGUE_BY_ID = Collections.unmodifiableMap(byId);
    }

    private DialogueRegistry() {}

    @Nullable
    public static DialogueDefinition resolve(Entity entity) {
        if (entity instanceof GuestEntity guestEntity) {
            return resolveById(guestEntity.getAssignedDialogueId());
        }
        if (entity instanceof BlacksmithEntity) {
            return BLACKSMITH_DIALOGUE;
        }
        if (entity instanceof FarmerEntity) {
            return FARMER_DIALOGUE;
        }
        if (entity instanceof MagicianEntity) {
            return MAGICIAN_DIALOGUE;
        }
        if (entity instanceof GrocerEntity) {
            return GROCER_DIALOGUE;
        }
        return null;
    }

    @Nullable
    public static DialogueDefinition resolveById(@Nullable String dialogueId) {
        if (dialogueId == null || dialogueId.isBlank()) {
            return null;
        }
        return DIALOGUE_BY_ID.get(dialogueId);
    }

    public static List<DialogueDefinition> allDialogues() {
        return ALL_DIALOGUES;
    }

    private static DialogueDefinition buildGuestLineDialogue(String id, LocalizedText lineText) {
        String root = "root";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        lineText,
                        List.of(
                                new DialogueOptionDef(
                                        "leave",
                                        LocalizedText.of("祝您旅居愉快", "Enjoy Your Stay"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition(id, root, nodes);
    }

    private static DialogueDefinition buildSimpleStoreDialogue(
            String npcId, LocalizedText rootText, LocalizedText askGoodsText) {
        String root = "root";
        String askGoods = "ask_goods";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        rootText,
                        List.of(
                                new DialogueOptionDef(
                                        "open_store",
                                        LocalizedText.of("打开商店", "Open Shop"),
                                        DialogueOptionType.FUNCTION,
                                        null,
                                        FUNCTION_OPEN_STORE),
                                new DialogueOptionDef(
                                        "ask_goods",
                                        LocalizedText.of("这里卖什么", "What Do You Sell?"),
                                        DialogueOptionType.BRANCH,
                                        askGoods,
                                        null),
                                new DialogueOptionDef(
                                        "leave",
                                        LocalizedText.of("先告辞", "Leave"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                askGoods,
                new DialogueNodeDef(
                        askGoods,
                        askGoodsText,
                        List.of(
                                new DialogueOptionDef(
                                        "open_store",
                                        LocalizedText.of("打开商店", "Open Shop"),
                                        DialogueOptionType.FUNCTION,
                                        null,
                                        FUNCTION_OPEN_STORE),
                                new DialogueOptionDef(
                                        "leave",
                                        LocalizedText.of("先告辞", "Leave"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition(npcId, root, nodes);
    }

    private static DialogueDefinition buildGrocerDialogue(
            String npcId, LocalizedText rootText, LocalizedText askGoodsText) {
        String root = "root";
        String askGoods = "ask_goods";

        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        rootText,
                        List.of(
                                new DialogueOptionDef(
                                        "open_store",
                                        LocalizedText.of("打开商店", "Open Shop"),
                                        DialogueOptionType.FUNCTION,
                                        null,
                                        FUNCTION_OPEN_STORE),
                                new DialogueOptionDef(
                                        "ask_goods",
                                        LocalizedText.of("这里卖什么", "What Do You Sell?"),
                                        DialogueOptionType.BRANCH,
                                        askGoods,
                                        null),
                                new DialogueOptionDef(
                                        "leave",
                                        LocalizedText.of("先告辞", "Leave"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));

        nodes.put(
                askGoods,
                new DialogueNodeDef(
                        askGoods,
                        askGoodsText,
                        List.of(
                                new DialogueOptionDef(
                                        "open_store",
                                        LocalizedText.of("打开商店", "Open Shop"),
                                        DialogueOptionType.FUNCTION,
                                        null,
                                        FUNCTION_OPEN_STORE),
                                new DialogueOptionDef(
                                        "leave",
                                        LocalizedText.of("先告辞", "Leave"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition(npcId, root, nodes);
    }

    private static DialogueDefinition buildStoreDialogueWithFacilityTopics(
            String npcId,
            LocalizedText rootText,
            LocalizedText askGoodsText,
            @Nullable LocalizedText aboutGreenhouseLabel,
            @Nullable LocalizedText aboutBoilerRoomLabel,
            @Nullable LocalizedText greenhouseUnrepairedText,
            @Nullable LocalizedText greenhouseRepairedText,
            @Nullable LocalizedText boilerRoomUnrepairedText,
            @Nullable LocalizedText boilerRoomRepairedText,
            @Nullable LocalizedText boilerRoomLocationLabel,
            @Nullable LocalizedText boilerRoomLocationText,
            @Nullable LocalizedText extraFunctionLabel,
            @Nullable String extraFunctionId) {
        String root = "root";
        String askGoods = "ask_goods";
        String greenhouseTopic = "topic_greenhouse";
        String boilerTopic = "topic_boiler_room";
        String boilerLocationTopic = "topic_boiler_room_location";
        boolean hasGreenhouseTopic =
                aboutGreenhouseLabel != null
                        && greenhouseUnrepairedText != null
                        && greenhouseRepairedText != null;
        boolean hasBoilerTopic =
                aboutBoilerRoomLabel != null
                        && boilerRoomUnrepairedText != null
                        && boilerRoomRepairedText != null;
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();

        List<DialogueOptionDef> rootOptions = new ArrayList<>();
        rootOptions.add(
                new DialogueOptionDef(
                        "open_store",
                        LocalizedText.of("打开商店", "Open Shop"),
                        DialogueOptionType.FUNCTION,
                        null,
                        FUNCTION_OPEN_STORE));
        if (extraFunctionLabel != null && extraFunctionId != null && !extraFunctionId.isBlank()) {
            rootOptions.add(
                    new DialogueOptionDef(
                            "extra_function",
                            extraFunctionLabel,
                            DialogueOptionType.FUNCTION,
                            null,
                            extraFunctionId));
        }
        rootOptions.add(
                new DialogueOptionDef(
                        "ask_goods",
                        LocalizedText.of("这里卖什么", "What Do You Sell?"),
                        DialogueOptionType.BRANCH,
                        askGoods,
                        null));
        if (hasGreenhouseTopic) {
            rootOptions.add(
                    new DialogueOptionDef(
                            "about_greenhouse",
                            aboutGreenhouseLabel,
                            DialogueOptionType.BRANCH,
                            greenhouseTopic,
                            null));
        }
        if (hasBoilerTopic) {
            rootOptions.add(
                    new DialogueOptionDef(
                            "about_boiler_room",
                            aboutBoilerRoomLabel,
                            DialogueOptionType.BRANCH,
                            boilerTopic,
                            null));
        }
        rootOptions.add(
                new DialogueOptionDef(
                        "leave",
                        LocalizedText.of("先告辞", "Leave"),
                        DialogueOptionType.BRANCH,
                        null,
                        null));
        nodes.put(root, new DialogueNodeDef(root, rootText, List.copyOf(rootOptions)));

        List<DialogueOptionDef> askGoodsOptions = new ArrayList<>();
        askGoodsOptions.add(
                new DialogueOptionDef(
                        "open_store",
                        LocalizedText.of("打开商店", "Open Shop"),
                        DialogueOptionType.FUNCTION,
                        null,
                        FUNCTION_OPEN_STORE));
        if (extraFunctionLabel != null && extraFunctionId != null && !extraFunctionId.isBlank()) {
            askGoodsOptions.add(
                    new DialogueOptionDef(
                            "extra_function",
                            extraFunctionLabel,
                            DialogueOptionType.FUNCTION,
                            null,
                            extraFunctionId));
        }
        if (hasGreenhouseTopic) {
            askGoodsOptions.add(
                    new DialogueOptionDef(
                            "about_greenhouse",
                            aboutGreenhouseLabel,
                            DialogueOptionType.BRANCH,
                            greenhouseTopic,
                            null));
        }
        if (hasBoilerTopic) {
            askGoodsOptions.add(
                    new DialogueOptionDef(
                            "about_boiler_room",
                            aboutBoilerRoomLabel,
                            DialogueOptionType.BRANCH,
                            boilerTopic,
                            null));
        }
        askGoodsOptions.add(
                new DialogueOptionDef(
                        "leave",
                        LocalizedText.of("先告辞", "Leave"),
                        DialogueOptionType.BRANCH,
                        null,
                        null));
        nodes.put(askGoods, new DialogueNodeDef(askGoods, askGoodsText, List.copyOf(askGoodsOptions)));

        if (hasGreenhouseTopic) {
            List<DialogueOptionDef> greenhouseOptions = new ArrayList<>();
            greenhouseOptions.add(
                    new DialogueOptionDef(
                            "open_store",
                            LocalizedText.of("打开商店", "Open Shop"),
                            DialogueOptionType.FUNCTION,
                            null,
                            FUNCTION_OPEN_STORE));
            if (extraFunctionLabel != null && extraFunctionId != null && !extraFunctionId.isBlank()) {
                greenhouseOptions.add(
                        new DialogueOptionDef(
                                "extra_function",
                                extraFunctionLabel,
                                DialogueOptionType.FUNCTION,
                                null,
                                extraFunctionId));
            }
            greenhouseOptions.add(
                    new DialogueOptionDef(
                            "leave",
                            LocalizedText.of("先告辞", "Leave"),
                            DialogueOptionType.BRANCH,
                            null,
                            null));
            nodes.put(
                    greenhouseTopic,
                    new DialogueNodeDef(
                            greenhouseTopic,
                            greenhouseUnrepairedText,
                            List.copyOf(greenhouseOptions),
                            new DialogueNodeConditionalText(
                                    "greenhouse", greenhouseUnrepairedText, greenhouseRepairedText)));
        }
        if (hasBoilerTopic) {
            List<DialogueOptionDef> boilerOptions = new ArrayList<>();
            boilerOptions.add(
                    new DialogueOptionDef(
                            "open_store",
                            LocalizedText.of("打开商店", "Open Shop"),
                            DialogueOptionType.FUNCTION,
                            null,
                            FUNCTION_OPEN_STORE));
            if (extraFunctionLabel != null && extraFunctionId != null && !extraFunctionId.isBlank()) {
                boilerOptions.add(
                        new DialogueOptionDef(
                                "extra_function",
                                extraFunctionLabel,
                                DialogueOptionType.FUNCTION,
                                null,
                                extraFunctionId));
            }
            if (boilerRoomLocationLabel != null && boilerRoomLocationText != null) {
                boilerOptions.add(
                        new DialogueOptionDef(
                                "boiler_room_location",
                                boilerRoomLocationLabel,
                                DialogueOptionType.BRANCH,
                                boilerLocationTopic,
                                null));
            }
            boilerOptions.add(
                    new DialogueOptionDef(
                            "leave",
                            LocalizedText.of("先告辞", "Leave"),
                            DialogueOptionType.BRANCH,
                            null,
                            null));
            nodes.put(
                    boilerTopic,
                    new DialogueNodeDef(
                            boilerTopic,
                            boilerRoomUnrepairedText,
                            List.copyOf(boilerOptions),
                            new DialogueNodeConditionalText(
                                    "boiler_room", boilerRoomUnrepairedText, boilerRoomRepairedText)));
            if (boilerRoomLocationLabel != null && boilerRoomLocationText != null) {
                List<DialogueOptionDef> boilerLocationOptions = new ArrayList<>();
                boilerLocationOptions.add(
                        new DialogueOptionDef(
                                "open_store",
                                LocalizedText.of("打开商店", "Open Shop"),
                                DialogueOptionType.FUNCTION,
                                null,
                                FUNCTION_OPEN_STORE));
                if (extraFunctionLabel != null && extraFunctionId != null && !extraFunctionId.isBlank()) {
                    boilerLocationOptions.add(
                            new DialogueOptionDef(
                                    "extra_function",
                                    extraFunctionLabel,
                                    DialogueOptionType.FUNCTION,
                                    null,
                                    extraFunctionId));
                }
                boilerLocationOptions.add(
                        new DialogueOptionDef(
                                "leave",
                                LocalizedText.of("先告辞", "Leave"),
                                DialogueOptionType.BRANCH,
                                null,
                                null));
                nodes.put(
                        boilerLocationTopic,
                        new DialogueNodeDef(
                                boilerLocationTopic,
                                boilerRoomLocationText,
                                List.copyOf(boilerLocationOptions)));
            }
        }
        return new DialogueDefinition(npcId, root, nodes);
    }

}
