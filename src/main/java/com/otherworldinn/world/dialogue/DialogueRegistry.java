package com.otherworldinn.world.dialogue;

import com.otherworldinn.entity.base.GuestEntity;
import com.otherworldinn.entity.store.BlacksmithEntity;
import com.otherworldinn.entity.store.BuilderEntity;
import com.otherworldinn.entity.store.ButcherEntity;
import com.otherworldinn.entity.store.FarmerEntity;
import com.otherworldinn.entity.store.FishermanEntity;
import com.otherworldinn.entity.store.GrocerEntity;
import com.otherworldinn.entity.store.MagicianEntity;
import com.otherworldinn.entity.store.WanderingTraderEntity;
import com.otherworldinn.world.storyguest.StoryGuestRegistry;
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
    public static final String FUNCTION_OPEN_RECYCLE = "open_recycle";


    private static final DialogueDefinition BLACKSMITH_DIALOGUE =
            buildStoreDialogueWithFacilityTopics(
                    "blacksmith",
                    LocalizedText.of(
                        "炉火正旺，想打点什么趁早说。", 
                        "The forge fire is roaring. If you want something forged, speak up now."),
                    LocalizedText.of(
                        "矿石、铸锭、工具和材料全在这儿了，看中什么自己挑。", 
                        "Ores, ingots, tools, and materials are all right here. Pick whatever catches your eye."),
                    null,
                    LocalizedText.of("关于锅炉房……", "About the Boiler Room"),
                    null,
                    "fix_boiler_room",
                    null,
                    null,
                    LocalizedText.of("地下那个老锅炉房都闲置多久了，也不知道还能不能打起火来。", 
                                     "That old boiler room underground has been sitting idle for ages. I don't even know if it can still spark a fire."),
                    LocalizedText.of("你居然把它修好了？干得漂亮！这下咱们的机器又能转起来了。",
                                     "You actually fixed it? Beautiful work! Now our machines can get spinning again."),
                    LocalizedText.of("关于矿井……", "About the Mine"),
                    LocalizedText.of("那边那废矿井塌了好一阵子了。要是有人能去修一修，我打铁用的矿石就不愁了。",
                                     "That abandoned mineshaft over there collapsed a while ago. If someone could go fix it, I wouldn't have to worry about ores for my forging."),
                    LocalizedText.of("多亏了那座矿井，现在好材料源源不断，可帮了我大忙了。",
                                     "Thanks to that mine, the good materials are flowing in endlessly now. You really did me a huge favor."),
                    "fix_mine",
                    LocalizedText.of("那地方具体在哪儿？", "Where is it?"),
                    LocalizedText.of(
                            "往前走右转，穿过集市再往右拐，那条小巷子走到底就是了。",
                            "Go straight, turn right, pass through the market, and take a right again. It's at the very end of that small alleyway."),
                    LocalizedText.of("借你的铁砧用用", "Borrow the Anvil"),
                    FUNCTION_OPEN_VIRTUAL_ANVIL);
    private static final DialogueDefinition FARMER_DIALOGUE =
            buildStoreDialogueWithFacilityTopics(
                    "farmer",
                    LocalizedText.of(
                        "田里刚收上来一批好货，想来点什么？", 
                        "Fresh harvest just came in. Need anything?"),
                    LocalizedText.of(
                        "种子、作物还有农具全都有，喏，都在这儿了。", 
                        "Seeds, crops, tools — take your time."),
                    LocalizedText.of("关于温室……", "About the Greenhouse"),
                    null,
                    "fix_greenhouse",
                    null,
                    LocalizedText.of("对面那温室荒废好些年头啦……想当年它还好好的时候，种出来的菜可水灵了。",
                                     "That greenhouse across the way has been abandoned for ages... it used to be so useful."),
                    LocalizedText.of("你居然真把它给修好了？太感谢了，这下种地可方便太多了！", 
                                     "You actually got it repaired. Thank you for putting in the work."),
                    null,
                    null,
                    null,
                    null,
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
                        "站在你面前这位迷人的魔女是谁呢？没错，就是我~ 想要点什么神奇的小玩意儿？", 
                        "The arcane stirs. What do you seek?"),
                    LocalizedText.of(
                        "材料和魔法书卷都在这儿了，看上哪个自己挑吧。",
                        "Materials and tomes are ready. Mind the explosive stuff."));
    private static final DialogueDefinition GROCER_DIALOGUE =
            buildGrocerDialogue(
                    "grocer",
                    LocalizedText.of(
                            "要买什么？我这里什么都有",
                            "What do you want to buy? I have everything."),
                    LocalizedText.of(
                            "都是些稀奇古怪的小玩意儿，随便看看吧。",
                            "A bunch of odds and ends. Feel free to browse."));

    private static final DialogueDefinition BUTCHER_DIALOGUE =
            buildSimpleStoreDialogueWithChat(
                    "butcher",
                    LocalizedText.of(
                            "新鲜的鲜肉，刚宰的！来点什么部位？",
                            "Fresh meat, just butchered! What do you need?"),
                    LocalizedText.of(
                            "生肉、皮革、骨头应有尽有，缺好食材了随时来找我！",
                            "Raw meat, leather, bones — come by when you run short."),
                    LocalizedText.of("最近店里什么肉最好卖？",
                            "What\'s been selling well lately?"),
                    LocalizedText.of(
                            "牛肉和猪肉卖得最快，不过也得看是什么季节。",
                            "Beef and pork sell best, though it depends on the season."));

    private static final DialogueDefinition BUILDER_DIALOGUE =
            buildSimpleStoreDialogueWithChat(
                    "builder",
                    LocalizedText.of(
                            "想盖房子？找我准没错！",
                            "Building something? You\'ve come to the right person!"),
                    LocalizedText.of(
                            "木材、石料、砖瓦我这儿管够，看上哪种建材了？",
                            "Timber, stone, bricks — all here. What were you after?"),
                    LocalizedText.of("能不能帮我搭把手建房子？",
                            "Can you help me build something?"),
                    LocalizedText.of(
                            "包在我身上，先挑挑蓝图，看中哪个款式了？",
                            "I can. Pick some blueprints."));

    private static final DialogueDefinition FISHERMAN_DIALOGUE =
            buildSimpleStoreDialogueWithChat(
                    "fisherman",
                    LocalizedText.of(
                            "今天运气不错，刚捞上来的好货，来看看？",
                            "Today\'s catch is decent. Take a look?"),
                    LocalizedText.of(
                            "鲜鱼、水产，还有从海里捞上来的些稀奇古怪的玩意儿。",
                            "Fish, seafood, and all sorts of junk pulled out of the sea."),
                    LocalizedText.of("能传授点钓鱼的秘诀吗？",
                            "Any tips for fishing?"),
                    LocalizedText.of(
                            "稳住气，有耐心就行。雨天鱼儿咬钩更勤，当然也得看季节和时辰。",
                            "Patience is enough. Rain helps, and so does the season and time of day."));
    private static final DialogueDefinition WANDERING_TRADER_DIALOGUE =
            buildWanderingTraderDialogue();
    private static final List<DialogueDefinition> GUEST_DIALOGUES =
            List.of(
                    buildGuestLineDialogue(
                            "guest_ordinary_welcome",
                            LocalizedText.of(
                                    "呼……这儿比我想象的还要暖和，今晚总算能睡个安稳觉了。",
                                    "This place feels warmer than I expected. I might sleep well tonight.")),
                    buildGuestLineDialogue(
                            "guest_ordinary_weather",
                            LocalizedText.of(
                                    "外头风刮得真邪乎，幸亏旅馆里够暖和安静。",
                                    "The wind outside is harsh. Glad it's calm in the inn.")),
                    buildGuestLineDialogue(
                            "guest_ordinary_checkout",
                            LocalizedText.of(
                                    "明天一早就得赶路，今晚可得好好歇歇脚。",
                                    "I leave at dawn tomorrow. Tonight, I just need good rest.")),
                    buildGuestLineDialogue(
                            "guest_ordinary_bed",
                            LocalizedText.of(
                                    "这床看着挺软和的，希望被子别太薄。",
                                    "This bed looks comfortable. Hope the blankets are warm enough.")),
                    buildGuestLineDialogue(
                            "guest_ordinary_food",
                            LocalizedText.of(
                                    "听人说这儿的招牌菜是一绝，晚上可得好好尝尝。",
                                    "I heard the home-style cooking here is good. Should try it tonight.")),
                    buildGuestLineDialogue(
                            "guest_ordinary_bath",
                            LocalizedText.of(
                                    "浑身骨头都快散架了，能泡个热水澡简直是诸神保佑。",
                                    "After a long day's travel, a hot bath is exactly what I need.")),
                    buildGuestLineDialogue(
                            "guest_ordinary_fireplace",
                            LocalizedText.of(
                                    "壁炉烧得真旺，坐在这儿翻翻书倒也惬意。",
                                    "The fireplace is so warm. Would be nice to read a book here.")),
                    buildGuestLineDialogue(
                            "guest_ordinary_morning",
                            LocalizedText.of(
                                    "晨风真清爽，正好出去溜达溜达。",
                                    "The morning air is refreshing. Good for a walk.")),
                    buildGuestLineDialogue(
                            "guest_ordinary_rain",
                            LocalizedText.of(
                                    "这种阴雨天，还是窝在旅馆里最舒坦。",
                                    "Staying at the inn during rain was a wise choice.")),
                    buildGuestLineDialogue(
                            "guest_ordinary_memory",
                            LocalizedText.of(
                                    "啧，这地方真让人怀念，跟我小时候住过的那家老客栈一模一样。",
                                    "This place reminds me of an old inn from my childhood.")),
                    buildGuestLineDialogue(
                            "guest_ordinary_recommend",
                            LocalizedText.of(
                                    "朋友极力推荐我来这儿落脚，果然名不虚传。",
                                    "A friend recommended this place. It hasn't disappointed.")),
                    buildGuestLineDialogue(
                            "guest_ordinary_return",
                            LocalizedText.of(
                                    "下次再路过这片地方，我肯定还来这儿住。",
                                    "I'll stay here again next time I pass through.")),


                    buildGuestLineDialogue(
                            "guest_rich_service",
                            LocalizedText.of(
                                    "只要服务能保持这个水准，我以后就是常客了。",
                                    "If the service stays this refined, I'll return often.")),
                    buildGuestLineDialogue(
                            "guest_rich_wine",
                            LocalizedText.of(
                                    "饭菜是不错，要是晚餐能再满上一杯好酒，那就绝了。",
                                    "A fine wine with dinner would make this place perfect.")),
                    buildGuestLineDialogue(
                            "guest_rich_tip",
                            LocalizedText.of(
                                    "招待得很周到，退房时我会多留点小费的。",
                                    "Good care deserves a tip when I check out.")),
                    buildGuestLineDialogue(
                            "guest_rich_room",
                            LocalizedText.of(
                                    "房间布置得倒还算雅致，就是差个挂大衣的地方。",
                                    "The room is elegantly decorated, though it lacks a walk-in closet.")),
                    buildGuestLineDialogue(
                            "guest_rich_view",
                            LocalizedText.of(
                                    "窗外风景挺别致，可惜看不见海。",
                                    "The view from the window is nice, though not oceanfront.")),
                    buildGuestLineDialogue(
                            "guest_rich_business",
                            LocalizedText.of(
                                    "这地方够清静，挺适合谈点正经买卖。",
                                    "The atmosphere here is suitable for business discussions.")),
                    buildGuestLineDialogue(
                            "guest_rich_linen",
                            LocalizedText.of(
                                    "床单摸着还算舒服，是纯棉的吧？",
                                    "The bedding has decent texture. Is it cotton?")),
                    buildGuestLineDialogue(
                            "guest_rich_quiet",
                            LocalizedText.of(
                                    "我可是付了足额的金币，就是图个清静，你们可别让我失望。",
                                    "I'm paying premium for peace and quiet. Don't disappoint.")),
                    buildGuestLineDialogue(
                            "guest_rich_referral",
                            LocalizedText.of(
                                    "不错，我会把这家店推荐给我那些挑剔的朋友们。",
                                    "I'll recommend this place to other discerning friends.")),
                    buildGuestLineDialogue(
                            "guest_rich_standard",
                            LocalizedText.of(
                                    "拿这个价钱来说，你们的服务也算是物有所值了。",
                                    "For this price range, the service standard is acceptable.")),

                    buildGuestLineDialogue(
                            "guest_heavy_pack_route",
                            LocalizedText.of(
                                    "扛着这么一大批货，能找个安稳的落脚点可真不容易。",
                                    "Traveling with this much cargo, a safe stop is priceless.")),
                    buildGuestLineDialogue(
                            "guest_heavy_pack_storage",
                            LocalizedText.of(
                                    "这些货箱先搁这儿，可千万别受潮了。",
                                    "Let me stash these crates here. Keep them out of the rain.")),
                    buildGuestLineDialogue(
                            "guest_heavy_pack_food",
                            LocalizedText.of(
                                    "跑了一整天的商，先给我端碗热乎的浓汤来！",
                                    "Run around all day — first thing you want is something hot.")),
                    buildGuestLineDialogue(
                            "guest_heavy_pack_road",
                            LocalizedText.of(
                                    "前头的山路可不好走，我得养足精神再上路。",
                                    "The road ahead is rough. Need to rest well before continuing.")),
                    buildGuestLineDialogue(
                            "guest_heavy_pack_repair",
                            LocalizedText.of(
                                    "马车的轮毂有点松动，这附近有铁匠铺吗？",
                                    "The wheel's loose. Is there a blacksmith nearby?")),
                    buildGuestLineDialogue(
                            "guest_heavy_pack_early",
                            LocalizedText.of(
                                    "明天天不亮我就得走，麻烦后厨早点备好干粮。",
                                    "I leave before dawn tomorrow. Please prepare breakfast early.")),
                    buildGuestLineDialogue(
                            "guest_heavy_pack_weather",
                            LocalizedText.of(
                                    "看这天色像是要起风暴，万幸找到了落脚的地方。",
                                    "Sky looks bad. Good thing I found somewhere to stay.")),
                    buildGuestLineDialogue(
                            "guest_heavy_pack_rest",
                            LocalizedText.of(
                                    "包袱压得我肩膀都麻了，今晚得好好睡一觉。",
                                    "My shoulders are numb from carrying. Need a good night's rest.")),
                    buildGuestLineDialogue(
                            "guest_heavy_pack_return",
                            LocalizedText.of(
                                    "等我跑完这趟回程还会路过，记得给我留间上房。",
                                    "I'll pass through on the return trip. Save me a room.")),

                    // Ultra Rich Guest Dialogues
                    buildGuestLineDialogue(
                            "guest_ultra_rich_suite",
                            LocalizedText.of(
                                    "这间套房勉强入得了我的眼，希望晚上不会有乱七八糟的噪音。",
                                    "This suite is acceptable. I expect absolute quiet at night.")),
                    buildGuestLineDialogue(
                            "guest_ultra_rich_privacy",
                            LocalizedText.of(
                                    "我这人最讨厌被打扰，没事别让下人来烦我。",
                                    "I dislike interruptions. Make sure your staff knows that.")),
                    buildGuestLineDialogue(
                            "guest_ultra_rich_guard",
                            LocalizedText.of(
                                    "我的行李可都价值连城，你们的安保最好别出什么岔子。",
                                    "My luggage is valuable. Security must not fail.")),
                    buildGuestLineDialogue(
                            "guest_ultra_rich_staff",
                            LocalizedText.of(
                                    "叫你们这儿最机灵的侍者来伺候，别拿笨手笨脚的新人敷衍我。",
                                    "Assign your most experienced staff. No trainees.")),
                    buildGuestLineDialogue(
                            "guest_ultra_rich_perfection",
                            LocalizedText.of(
                                    "细节才能彰显品位，比如这枕头的高度就得恰到好处。",
                                    "Details define quality. The pillow height must be perfect.")),
                    buildGuestLineDialogue(
                            "guest_ultra_rich_expectation",
                            LocalizedText.of(
                                    "我平时出行的排场可不小，希望你们这儿的服务能跟得上。",
                                    "My standards are exceptionally high. I hope you can meet them.")),
                    buildGuestLineDialogue(
                            "guest_ultra_rich_discretion",
                            LocalizedText.of(
                                    "我的行程绝对保密，要是走漏了风声，唯你是问。",
                                    "My itinerary is confidential. Do not disclose it.")),
                    buildGuestLineDialogue(
                            "guest_ultra_rich_taste",
                            LocalizedText.of(
                                    "墙上这装饰品看着还有点品味，不会是赝品吧？",
                                    "The decor shows some taste. Are these pieces authentic?")),
                    buildGuestLineDialogue(
                            "guest_ultra_rich_investment",
                            LocalizedText.of(
                                    "这家旅馆倒有点投资的潜力，我会留意留意的。",
                                    "This inn has investment potential. I'll keep an eye on it.")),
                    buildGuestLineDialogue(
                            "guest_ultra_rich_legacy",
                            LocalizedText.of(
                                    "到底是老牌旅馆，这陈设、这底蕴，就是跟那些新店不一样。",
                                    "An old inn is something else. Real depth to it, hey.")),

                    // VIP Guest Dialogues
                    buildGuestLineDialogue(
                            "guest_vip_ordinary_schedule",
                            LocalizedText.of(
                                    "我的日程排得很满，明早千万记得按时叫醒我。",
                                    "My schedule is tight. Wake me on time, please.")),
                    buildGuestLineDialogue(
                            "guest_vip_ordinary_tea",
                            LocalizedText.of(
                                    "要是能给我屋里送壶刚泡好的热茶，那就再好不过了。",
                                    "A pot of hot tea to the room would be lovely.")),
                    buildGuestLineDialogue(
                            "guest_vip_ordinary_review",
                            LocalizedText.of(
                                    "招待得很用心，我会给你们留个好名声的。",
                                    "The service is excellent. I'll leave a positive review.")),
                    buildGuestLineDialogue(
                            "guest_vip_advanced_security",
                            LocalizedText.of(
                                    "我的随从稍后就到，先把他们的铺盖都准备妥当。",
                                    "My attendants arrive later. Get things ready first.")),
                    buildGuestLineDialogue(
                            "guest_vip_advanced_order",
                            LocalizedText.of(
                                    "晚些时候我会点餐，记得用最好的食材。",
                                    "I'll place an order later. Use your finest ingredients.")),
                    buildGuestLineDialogue(
                            "guest_vip_advanced_reward",
                            LocalizedText.of(
                                    "只要把本老爷伺候好了，少不了你的赏钱。",
                                    "Get it done right, and your cut won't be forgotten.")),
                    buildGuestLineDialogue(
                            "guest_vip_feedback",
                            LocalizedText.of(
                                    "这里的服务流程非常严谨，我会如实记录下来的。",
                                    "The service process is well-standardized. I'll report upward.")),

                    // Sponsor Guest Dialogues
                    buildGuestLineDialogue(
                            "guest_sponsor_photo",
                            LocalizedText.of(
                                    "这里的氛围简直绝了，我得用留影机拍张照留个纪念。",
                                    "The vibe here is great. I want to take a commemorative photo.")),
                    buildGuestLineDialogue(
                            "guest_sponsor_renovation",
                            LocalizedText.of(
                                    "旅馆翻新之后住着舒服多了，真是辛苦你们了。",
                                    "The renovations made this place so much nicer. Well done.")),
                    buildGuestLineDialogue(
                            "guest_sponsor_support",
                            LocalizedText.of(
                                    "保持这个势头，我会一直光顾你们生意的！",
                                    "Keep it up. I'll keep supporting this inn.")),
                    buildGuestLineDialogue(
                            "guest_sponsor_growth",
                            LocalizedText.of(
                                    "看着这家旅馆一点点变得热闹起来，我这心里也挺高兴。",
                                    "It's heartwarming to see the inn improving.")),
                    buildGuestLineDialogue(
                            "guest_sponsor_community",
                            LocalizedText.of(
                                    "这儿真有一种家的温馨感，大伙儿也都挺和善的。",
                                    "This place feels like home. The guests are all friendly.")),
                    buildGuestLineDialogue(
                            "guest_sponsor_tradition",
                            LocalizedText.of(
                                    "老旅馆的韵味保留得非常完美，真是难得的情怀。",
                                    "The old inn charm is well preserved. It has character.")),
                    buildGuestLineDialogue(
                            "guest_sponsor_recommend",
                            LocalizedText.of(
                                    "我回去一定要把这儿推荐给朋友们，这么好的地方可不能藏私。",
                                    "I'll recommend this place to more friends. Good places should be shared.")),
                    buildGuestLineDialogue(
                            "guest_sponsor_improvement",
                            LocalizedText.of(
                                    "如果需要什么关于经营的建议，随时可以来问我。",
                                    "What else needs improvement? I can offer suggestions.")),
                    buildGuestLineDialogue(
                            "guest_sponsor_future",
                            LocalizedText.of(
                                    "真期待这家旅馆以后会变成什么样，我会一直关注的。",
                                    "Looking forward to the inn's future development. I'll keep watching.")),
                    buildGuestLineDialogue(
                            "guest_sponsor_appreciation",
                            LocalizedText.of(
                                    "谢谢你们把这里的原汁原味保留了下来，在如今这世道可太难得了。",
                                    "Thank you for maintaining the authentic feel. It's rare.")));

    private static final List<DialogueDefinition> ALL_DIALOGUES;

    private static final Map<String, DialogueDefinition> DIALOGUE_BY_ID;

    static {
        List<DialogueDefinition> all =
                new ArrayList<>(
                        List.of(BLACKSMITH_DIALOGUE, FARMER_DIALOGUE, MAGICIAN_DIALOGUE, GROCER_DIALOGUE,
                                BUTCHER_DIALOGUE, BUILDER_DIALOGUE, FISHERMAN_DIALOGUE, WANDERING_TRADER_DIALOGUE));
        all.addAll(GUEST_DIALOGUES);
        all.addAll(StoryGuestRegistry.allDialogues());
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
        if (entity instanceof ButcherEntity) {
            return BUTCHER_DIALOGUE;
        }
        if (entity instanceof BuilderEntity) {
            return BUILDER_DIALOGUE;
        }
        if (entity instanceof FishermanEntity) {
            return FISHERMAN_DIALOGUE;
        }
        if (entity instanceof WanderingTraderEntity) {
            return WANDERING_TRADER_DIALOGUE;
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
                                        LocalizedText.of("你这儿都卖些什么", "What Do You Sell?"),
                                        DialogueOptionType.BRANCH,
                                        askGoods,
                                        null),
                                new DialogueOptionDef(
                                        "leave",
                                        LocalizedText.of("先告辞了", "Leave"),
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
                                        LocalizedText.of("先告辞了", "Leave"),
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
                                        LocalizedText.of("你这儿都卖些什么", "What Do You Sell?"),
                                        DialogueOptionType.BRANCH,
                                        askGoods,
                                        null),
                                new DialogueOptionDef(
                                        "leave",
                                        LocalizedText.of("先告辞了", "Leave"),
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
                                        LocalizedText.of("先告辞了", "Leave"),
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
            @Nullable String greenhouseQuestId,
            @Nullable String boilerRoomQuestId,
            @Nullable LocalizedText greenhouseUnrepairedText,
            @Nullable LocalizedText greenhouseRepairedText,
            @Nullable LocalizedText boilerRoomUnrepairedText,
            @Nullable LocalizedText boilerRoomRepairedText,
            @Nullable LocalizedText aboutMineLabel,
            @Nullable LocalizedText mineText,
            @Nullable LocalizedText mineRepairedText,
            @Nullable String mineQuestId,
            @Nullable LocalizedText boilerRoomLocationLabel,
            @Nullable LocalizedText boilerRoomLocationText,
            @Nullable LocalizedText extraFunctionLabel,
            @Nullable String extraFunctionId) {
        String root = "root";
        String askGoods = "ask_goods";
        String greenhouseTopic = "topic_greenhouse";
        String boilerTopic = "topic_boiler_room";
        String boilerLocationTopic = "topic_boiler_room_location";
        String mineTopic = "topic_mine";
        boolean hasGreenhouseTopic =
                aboutGreenhouseLabel != null
                        && greenhouseUnrepairedText != null
                        && greenhouseRepairedText != null;
        boolean hasBoilerTopic =
                aboutBoilerRoomLabel != null
                        && boilerRoomUnrepairedText != null
                        && boilerRoomRepairedText != null;
        boolean hasMineTopic =
                aboutMineLabel != null && mineText != null && mineRepairedText != null;
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
                        LocalizedText.of("你这儿都卖些什么", "What Do You Sell?"),
                        DialogueOptionType.BRANCH,
                        askGoods,
                        null));
        if (hasGreenhouseTopic) {
            DialogueOptionDef aboutGreenhouseOption =
                    new DialogueOptionDef(
                            "about_greenhouse",
                            aboutGreenhouseLabel,
                            DialogueOptionType.BRANCH,
                            greenhouseTopic,
                            null);
            if (greenhouseQuestId != null && !greenhouseQuestId.isBlank()) {
                aboutGreenhouseOption = aboutGreenhouseOption.withEffects(DialogueEffectDef.acceptQuest(greenhouseQuestId));
            }
            rootOptions.add(aboutGreenhouseOption);
        }
        if (hasBoilerTopic) {
            DialogueOptionDef aboutBoilerRoomOption =
                    new DialogueOptionDef(
                            "about_boiler_room",
                            aboutBoilerRoomLabel,
                            DialogueOptionType.BRANCH,
                            boilerTopic,
                            null);
            if (boilerRoomQuestId != null && !boilerRoomQuestId.isBlank()) {
                aboutBoilerRoomOption = aboutBoilerRoomOption.withEffects(DialogueEffectDef.acceptQuest(boilerRoomQuestId));
            }
            rootOptions.add(aboutBoilerRoomOption);
            if (hasMineTopic) {
                DialogueOptionDef aboutMineOption =
                        new DialogueOptionDef(
                                "about_mine",
                                aboutMineLabel,
                                DialogueOptionType.BRANCH,
                                mineTopic,
                                null);
                if (mineQuestId != null && !mineQuestId.isBlank()) {
                    aboutMineOption = aboutMineOption.withEffects(DialogueEffectDef.acceptQuest(mineQuestId));
                }
                rootOptions.add(aboutMineOption);
            }
        }
        rootOptions.add(
                new DialogueOptionDef(
                        "leave",
                        LocalizedText.of("先告辞了", "Leave"),
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
            DialogueOptionDef aboutGreenhouseOption =
                    new DialogueOptionDef(
                            "about_greenhouse",
                            aboutGreenhouseLabel,
                            DialogueOptionType.BRANCH,
                            greenhouseTopic,
                            null);
            if (greenhouseQuestId != null && !greenhouseQuestId.isBlank()) {
                aboutGreenhouseOption = aboutGreenhouseOption.withEffects(DialogueEffectDef.acceptQuest(greenhouseQuestId));
            }
            askGoodsOptions.add(aboutGreenhouseOption);
        }
        if (hasBoilerTopic) {
            DialogueOptionDef aboutBoilerRoomOption =
                    new DialogueOptionDef(
                            "about_boiler_room",
                            aboutBoilerRoomLabel,
                            DialogueOptionType.BRANCH,
                            boilerTopic,
                            null);
            if (boilerRoomQuestId != null && !boilerRoomQuestId.isBlank()) {
                aboutBoilerRoomOption = aboutBoilerRoomOption.withEffects(DialogueEffectDef.acceptQuest(boilerRoomQuestId));
            }
            askGoodsOptions.add(aboutBoilerRoomOption);
            if (hasMineTopic) {
                DialogueOptionDef aboutMineOption =
                        new DialogueOptionDef(
                                "about_mine",
                                aboutMineLabel,
                                DialogueOptionType.BRANCH,
                                mineTopic,
                                null);
                if (mineQuestId != null && !mineQuestId.isBlank()) {
                    aboutMineOption = aboutMineOption.withEffects(DialogueEffectDef.acceptQuest(mineQuestId));
                }
                askGoodsOptions.add(aboutMineOption);
            }
        }
        askGoodsOptions.add(
                new DialogueOptionDef(
                        "leave",
                        LocalizedText.of("先告辞了", "Leave"),
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
                            LocalizedText.of("先告辞了", "Leave"),
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
                            LocalizedText.of("先告辞了", "Leave"),
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
                                LocalizedText.of("先告辞了", "Leave"),
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
        if (hasMineTopic) {
            List<DialogueOptionDef> mineOptions = new ArrayList<>();
            mineOptions.add(
                    new DialogueOptionDef(
                            "open_store",
                            LocalizedText.of("打开商店", "Open Shop"),
                            DialogueOptionType.FUNCTION,
                            null,
                            FUNCTION_OPEN_STORE));
            if (extraFunctionLabel != null && extraFunctionId != null && !extraFunctionId.isBlank()) {
                mineOptions.add(
                        new DialogueOptionDef(
                                "extra_function",
                                extraFunctionLabel,
                                DialogueOptionType.FUNCTION,
                                null,
                                extraFunctionId));
            }
            mineOptions.add(
                    new DialogueOptionDef(
                            "leave",
                            LocalizedText.of("先告辞了", "Leave"),
                            DialogueOptionType.BRANCH,
                            null,
                            null));
            nodes.put(
                    mineTopic,
                    new DialogueNodeDef(
                            mineTopic,
                            mineText,
                            List.copyOf(mineOptions),
                            new DialogueNodeConditionalText("mine", mineText, mineRepairedText)));
        }
        return new DialogueDefinition(npcId, root, nodes);
    }

    private static DialogueDefinition buildSimpleStoreDialogueWithChat(
            String npcId, LocalizedText rootText, LocalizedText askGoodsText,
            LocalizedText chatLabel, LocalizedText chatText) {
        String root = "root";
        String askGoods = "ask_goods";
        String chatNode = "small_talk_" + npcId;
        String chatOptionId = "small_talk_" + npcId;
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
                                        LocalizedText.of("你这儿都卖些什么", "What Do You Sell?"),
                                        DialogueOptionType.BRANCH,
                                        askGoods,
                                        null),
                                new DialogueOptionDef(
                                        chatOptionId,
                                        chatLabel,
                                        DialogueOptionType.BRANCH,
                                        chatNode,
                                        null),
                                new DialogueOptionDef(
                                        "leave",
                                        LocalizedText.of("先告辞了", "Leave"),
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
                                        chatOptionId,
                                        chatLabel,
                                        DialogueOptionType.BRANCH,
                                        chatNode,
                                        null),
                                new DialogueOptionDef(
                                        "leave",
                                        LocalizedText.of("先告辞了", "Leave"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                chatNode,
                new DialogueNodeDef(
                        chatNode,
                        chatText,
                        List.of(
                                new DialogueOptionDef(
                                        "open_store",
                                        LocalizedText.of("打开商店", "Open Shop"),
                                        DialogueOptionType.FUNCTION,
                                        null,
                                        FUNCTION_OPEN_STORE),
                                new DialogueOptionDef(
                                        "leave",
                                        LocalizedText.of("先告辞了", "Leave"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition(npcId, root, nodes);
    }

    private static DialogueDefinition buildWanderingTraderDialogue() {
        String root = "root";
        String askGoods = "ask_goods";
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        nodes.put(
                root,
                new DialogueNodeDef(
                        root,
                        LocalizedText.of(
                                "从远方运来的稀奇货物，看看有没有你需要的？",
                                "Rare goods from distant lands — see anything you like?"),
                        List.of(
                                new DialogueOptionDef(
                                        "open_store",
                                        LocalizedText.of("打开商店", "Open Shop"),
                                        DialogueOptionType.FUNCTION,
                                        null,
                                        FUNCTION_OPEN_STORE),
                                new DialogueOptionDef(
                                        "open_recycle",
                                        LocalizedText.of("出售物品", "Sell Items"),
                                        DialogueOptionType.FUNCTION,
                                        null,
                                        FUNCTION_OPEN_RECYCLE),
                                new DialogueOptionDef(
                                        "ask_goods",
                                        LocalizedText.of("你这儿都卖些什么", "What Do You Sell?"),
                                        DialogueOptionType.BRANCH,
                                        askGoods,
                                        null),
                                new DialogueOptionDef(
                                        "leave",
                                        LocalizedText.of("先告辞了", "Leave"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        nodes.put(
                askGoods,
                new DialogueNodeDef(
                        askGoods,
                        LocalizedText.of(
                                "这次我带的可都是市面上难得一见的新鲜货，想寻点什么？",
                                "This time I've brought fresh novelties rarely seen on the market. What might you be looking for?"),
                        List.of(
                                new DialogueOptionDef(
                                        "open_store",
                                        LocalizedText.of("打开商店", "Open Shop"),
                                        DialogueOptionType.FUNCTION,
                                        null,
                                        FUNCTION_OPEN_STORE),
                                new DialogueOptionDef(
                                        "open_recycle",
                                        LocalizedText.of("出售物品", "Sell Items"),
                                        DialogueOptionType.FUNCTION,
                                        null,
                                        FUNCTION_OPEN_RECYCLE),
                                new DialogueOptionDef(
                                        "leave",
                                        LocalizedText.of("先告辞了", "Leave"),
                                        DialogueOptionType.BRANCH,
                                        null,
                                        null))));
        return new DialogueDefinition("wandering_trader", root, nodes);
    }

}
