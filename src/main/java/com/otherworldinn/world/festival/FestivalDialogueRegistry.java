package com.otherworldinn.world.festival;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.world.dialogue.DialogueDefinition;
import com.otherworldinn.world.dialogue.DialogueNodeDef;
import com.otherworldinn.world.dialogue.DialogueRegistry;
import com.otherworldinn.world.dialogue.LocalizedText;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

/**
 * 节日限定 NPC 对话注册表：克隆基础商店对话并替换 root 节点问候文案，其余节点与选项结构原样保留（选项 id 全局共享、翻译自动复用）。
 * 对话打开时实时判定当前激活节日（含调试开关），节日结束或未注册组合时回落基础对话。
 */
public final class FestivalDialogueRegistry {
    /** 基础对话 id → (节日 id → 节日版对话) */
    private static final Map<String, Map<String, DialogueDefinition>> BY_BASE_DIALOGUE =
            new LinkedHashMap<>();

    static {
        // ── 农夫 ──
        register(
                "farmer",
                "spring_festival",
                LocalizedText.of(
                        "田里的花都开好了，来挑些种子和花苗吗？春祭就该把园子装点起来。",
                        "The fields are in bloom! Come pick some seeds and saplings — a spring festival deserves a garden in full dress."));
        register(
                "farmer",
                "midsummer_night",
                LocalizedText.of(
                        "这个时节庄稼长得最快，夜里也凉快，正适合去田边转转。",
                        "Crops grow quickest around now, and the nights are mild. A fine time to walk the fields."));
        register(
                "farmer",
                "harvest_festival",
                LocalizedText.of(
                        "今年是个丰收年！仓库都堆满了，随便挑，都是刚收下来的。",
                        "What a harvest! The storehouse is overflowing — take your pick, all freshly gathered."));
        register(
                "farmer",
                "deep_winter_festival",
                LocalizedText.of(
                        "地都冻上了，活儿少。不过热乎的粮食和存菜还是管够的。",
                        "The ground's frozen solid, so there's little to do — but warm grain and stored greens are plentiful."));

        // ── 铁匠 ──
        register(
                "blacksmith",
                "spring_festival",
                LocalizedText.of(
                        "节日期间订单排满了，都是修农具的。要打点什么，趁早说。",
                        "The order book is full of farm tools this week. If you need something forged, speak up early."));
        register(
                "blacksmith",
                "midsummer_night",
                LocalizedText.of(
                        "庆典的灯架和铁艺都出自我的锤子。看看需要什么，别客气。",
                        "The lantern frames and ironwork for the celebration came off my anvil. Have a look around."));
        register(
                "blacksmith",
                "harvest_festival",
                LocalizedText.of(
                        "宴会要用的刀具、烤架，全是我连夜赶出来的。炉火正旺。",
                        "Knives and roasting racks for the feast — hammered out overnight. The forge burns bright."));
        register(
                "blacksmith",
                "deep_winter_festival",
                LocalizedText.of(
                        "天冷了，炉子边上最暖和。需要什么趁热打，铁不等人。",
                        "Cold days make the forge the warmest spot in town. Strike while the iron's hot."));

        // ── 魔女 ──
        register(
                "magician",
                "spring_festival",
                LocalizedText.of(
                        "万物复苏的气息对药剂发酵最好不过了……哦，要买东西？随便看。",
                        "The air of returning life does wonders for potion brewing... Oh, shopping? Browse freely."));
        register(
                "magician",
                "midsummer_night",
                LocalizedText.of(
                        "夜晚的光辉如此迷人，我正研究会发光的花酿……你要买点什么？",
                        "The night's glow is captivating — I'm studying a luminous brew... Was there something you needed?"));
        register(
                "magician",
                "harvest_festival",
                LocalizedText.of(
                        "丰收的甜香也能入药，真是慷慨的季节。看看我的存货吧。",
                        "Even the scent of harvest finds its way into medicine. A generous season. Have a look at my stock."));
        register(
                "magician",
                "deep_winter_festival",
                LocalizedText.of(
                        "长夜里最适合读书和实验，冬夜的星象也很清楚……需要什么就说。",
                        "Long nights are made for reading and experiments, and the winter stars are sharp... Just say what you need."));

        // ── 杂货商 ──
        register(
                "grocer",
                "spring_festival",
                LocalizedText.of(
                        "春祭特供上架啦！花苗、肥料都有，要装点门面的可别错过。",
                        "Festival specials are on the shelf! Saplings and fertilizer — don't miss them while decorating for the spring."));
        register(
                "grocer",
                "midsummer_night",
                LocalizedText.of(
                        "庆典夜里用的唱片和零食都备齐了，旅客们都要来了，手快有手慢无。",
                        "Records and treats for the night are all stocked. Guests are pouring in — first come, first served."));
        register(
                "grocer",
                "harvest_festival",
                LocalizedText.of(
                        "丰收的货架都摆不下了！南瓜派、蛋糕、节日食材，管够。",
                        "The shelves can barely hold the bounty! Pumpkin pies, cakes, festival fare — plenty for all."));
        register(
                "grocer",
                "deep_winter_festival",
                LocalizedText.of(
                        "年货和烟花都到货了！守岁要热热闹闹的，进来挑点吧。",
                        "New Year goods and fireworks have arrived! A vigil should be loud and bright — come pick some out."));

        // ── 屠夫 ──
        register(
                "butcher",
                "spring_festival",
                LocalizedText.of(
                        "春天的羔羊肉最嫩了，要做节日大餐的话，我这儿现切。",
                        "Spring lamb is at its tenderest. If you're cooking a festival feast, I'll cut it fresh."));
        register(
                "butcher",
                "midsummer_night",
                LocalizedText.of(
                        "夜里的烧烤摊都来我这儿进货，肉管够！",
                        "The night barbecue stalls all buy from me — meat aplenty!"));
        register(
                "butcher",
                "harvest_festival",
                LocalizedText.of(
                        "宴会的烤肉大件就交给我吧，看看这肉质，丰收年的猪就是不一样。",
                        "Leave the roast to me. Look at that marbling — harvest-year hogs are something else."));
        register(
                "butcher",
                "deep_winter_festival",
                LocalizedText.of(
                        "天冷就得吃肉囤膘，守岁的桌上肉丸子炖锅不能少。",
                        "Cold days call for meat to keep warm. No vigil table should lack a pot of meatball stew."));

        // ── 建筑工 ──
        register(
                "builder",
                "spring_festival",
                LocalizedText.of(
                        "节后就要开工修整镇子了，先趁假期歇歇。要买建材吗？",
                        "Repairs on the town start after the festival — resting while I can. Need building supplies?"));
        register(
                "builder",
                "midsummer_night",
                LocalizedText.of(
                        "庆典的木台子是我搭的，结实得很，随便蹦跶。",
                        "I built the festival stage myself. Solid as a rock — dance all you like."));
        register(
                "builder",
                "harvest_festival",
                LocalizedText.of(
                        "巡游的花车要是散架了可别来找我……开玩笑的，要修车随时来。",
                        "If a parade float falls apart, don't come crying to me... Kidding — I'll fix any wagon, anytime."));
        register(
                "builder",
                "deep_winter_festival",
                LocalizedText.of(
                        "入冬前我把屋顶都检修了一遍，这下雪也冻不着。要备些木料吗？",
                        "I checked every roof before winter — no snow's getting in. Stocking up on timber?"));

        // ── 渔夫 ──
        register(
                "fisherman",
                "spring_festival",
                LocalizedText.of(
                        "春天的鱼最肥美，鱼汛也来了。要买点鲜鱼吗？",
                        "Spring fish run fat, and the season's shoals are in. Care for some fresh catch?"));
        register(
                "fisherman",
                "midsummer_night",
                LocalizedText.of(
                        "夜里适合钓夜行的鱼，水面上还有庆典的灯影，鱼都不怕人。",
                        "Night-caught fish bite well with festival lanterns on the water — they're not shy tonight."));
        register(
                "fisherman",
                "harvest_festival",
                LocalizedText.of(
                        "丰收不止在田里，海里也是。这几天的渔获多得很！",
                        "The harvest isn't only in the fields — the sea's been generous too. Quite the haul lately!"));
        register(
                "fisherman",
                "deep_winter_festival",
                LocalizedText.of(
                        "海风变冷了，港口冻上了，想喝口热鱼汤的话，我这儿有料。",
                        "The sea wind's turned cold and the harbor's frozen over. I've the fixings for hot fish soup, if you're keen."));

        // ── 游商 ──
        register(
                "wandering_trader",
                "spring_festival",
                LocalizedText.of(
                        "难得赶上春祭停船，镇子上真热闹。看看我带来的货？",
                        "Rare luck, docking right at the spring festival — the town's buzzing. Care to see my wares?"));
        register(
                "wandering_trader",
                "midsummer_night",
                LocalizedText.of(
                        "各处都在办夜市，就你们镇上最热闹。来看看货吧。",
                        "Night markets everywhere this season, but yours is the liveliest. Come have a look."));
        register(
                "wandering_trader",
                "harvest_festival",
                LocalizedText.of(
                        "丰收的港口运粮船真多。要买点什么吗？",
                        "So many grain ships in the harvest harbor. Anything you'd like?"));
        register(
                "wandering_trader",
                "deep_winter_festival",
                LocalizedText.of(
                        "这么冷的日子，你们这儿的守岁还挺热闹，看看什么吗？",
                        "Cold as it is, your vigil here is quite the lively one. Care to look around?"));
    }

    private FestivalDialogueRegistry() {}

    /** 节日激活且已注册该 NPC×节日组合时返回节日版对话，否则原样返回基础对话 */
    public static DialogueDefinition overrideIfFestivalActive(DialogueDefinition base, Entity entity) {
        if (base == null || entity.level().isClientSide) {
            return base;
        }
        return override(base, (ServerLevel) entity.level());
    }

    private static DialogueDefinition override(DialogueDefinition base, ServerLevel level) {
        Optional<FestivalDefinition> festival = FestivalService.getActiveFestival(level);
        if (festival.isEmpty()) {
            return base;
        }
        Map<String, DialogueDefinition> byFestival = BY_BASE_DIALOGUE.get(base.id());
        if (byFestival == null) {
            return base;
        }
        return byFestival.getOrDefault(festival.get().id(), base);
    }

    /** 全部节日版对话（供 datagen 生成文案 key；选项 key 与基础对话共享自动去重） */
    public static List<DialogueDefinition> allFestivalDialogues() {
        List<DialogueDefinition> all = new ArrayList<>();
        for (Map<String, DialogueDefinition> byFestival : BY_BASE_DIALOGUE.values()) {
            all.addAll(byFestival.values());
        }
        return all;
    }

    private static void register(String baseDialogueId, String festivalId, LocalizedText greeting) {
        DialogueDefinition base = DialogueRegistry.resolveById(baseDialogueId);
        if (base == null) {
            OtherworldInn.LOGGER.warn(
                    "Festival dialogue skipped: unknown base dialogue {}", baseDialogueId);
            return;
        }
        BY_BASE_DIALOGUE
                .computeIfAbsent(baseDialogueId, ignored -> new LinkedHashMap<>())
                .put(festivalId, withFestivalGreeting(base, festivalId, greeting));
    }

    /** 克隆基础对话并替换 root 节点问候文案，其余节点与选项结构原样保留 */
    private static DialogueDefinition withFestivalGreeting(
            DialogueDefinition base, String festivalId, LocalizedText greeting) {
        Map<String, DialogueNodeDef> nodes = new LinkedHashMap<>();
        for (Map.Entry<String, DialogueNodeDef> entry : base.nodes().entrySet()) {
            DialogueNodeDef node = entry.getValue();
            if (entry.getKey().equals(base.rootNodeId())) {
                nodes.put(entry.getKey(), new DialogueNodeDef(node.id(), greeting, node.options(), node.conditionalText()));
            } else {
                nodes.put(entry.getKey(), node);
            }
        }
        return new DialogueDefinition(base.id() + "_" + festivalId, base.rootNodeId(), nodes);
    }
}
