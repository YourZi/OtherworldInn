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
                        "瞧这田里的花开得多好！春祭到了，不挑些种子和花苗把自家院子也打扮打扮？",
                        "The fields are in bloom! Come pick some seeds and saplings — a spring festival deserves a garden in full dress."));
        register(
                "farmer",
                "midsummer_night",
                LocalizedText.of(
                        "这阵子庄稼长得飞快，夜风也凉快，正适合去田埂上溜达溜达。",
                        "Crops grow quickest around now, and the nights are mild. A fine time to walk the fields."));
        register(
                "farmer",
                "harvest_festival",
                LocalizedText.of(
                        "今年可是个大丰收！你瞅瞅，仓库都快堆不下了。随便挑，全都是刚打下来的新粮！",
                        "What a harvest! The storehouse is overflowing — take your pick, all freshly gathered."));
        register(
                "farmer",
                "deep_winter_festival",
                LocalizedText.of(
                        "地都冻得硬邦邦的，总算能歇口气了。放心，热乎的粮食和地窖里的存菜管够！",
                        "Ground's frozen, not much work. Still got warm grain and stored greens enough."));

        // ── 铁匠 ──
        register(
                "blacksmith",
                "spring_festival",
                LocalizedText.of(
                        "节里这阵子订单全排满了，全是赶着修农具的。想打点什么趁早说，我好给你排上！",
                        "The order book is completely full for the festival, all rushing to repair farm tools. If you want something forged, speak up early so I can put you in the queue!"));
        register(
                "blacksmith",
                "midsummer_night",
                LocalizedText.of(
                        "瞧见庆典上的灯架和铁花没？全是我一锤一锤敲出来的！随便看，需要什么别客气。",
                        "See the lantern frames and ironworks at the festival? I hammered them out one by one! Take a look around, and don't hesitate if you need anything."));
        register(
                "blacksmith",
                "harvest_festival",
                LocalizedText.of(
                        "宴会要用的长刀、大烤架，全是我连夜赶出来的。现在炉火正旺，还要打点啥？",
                        "The longswords and massive roasting racks for the banquet—I hammered them all out overnight. The forge fire is roaring, need anything else forged?"));
        register(
                "blacksmith",
                "deep_winter_festival",
                LocalizedText.of(
                        "嘶……天真冷，还是我这炉子边上最暖和。要打什么赶紧的，趁热打铁，铁可不等人。",
                        "Hiss... It's freezing out there. It's warmest right here by my forge. Speak up if you need something forged, strike while the iron is hot, for iron waits for no one."));

        // ── 魔女 ──
        register(
                "magician",
                "spring_festival",
                LocalizedText.of(
                        "万物复苏的气息，用来发酵药剂真是再完美不过了……哦？有客人？要买点什么随便看吧。",
                        "The air of returning life does wonders for potion brewing... Oh, shopping? Browse freely."));
        register(
                "magician",
                "midsummer_night",
                LocalizedText.of(
                        "夜晚的光辉真是迷人……我正在试着调配一种会发光的花酿。你要买点什么吗？",
                        "The night's glow is captivating — I'm studying a luminous brew... Was there something you needed?"));
        register(
                "magician",
                "harvest_festival",
                LocalizedText.of(
                        "连空气里都飘着丰收的甜香，真是个慷慨的季节。要看看我最近的新存货吗？",
                        "Even the scent of harvest finds its way into medicine. A generous season. Have a look at my stock."));
        register(
                "magician",
                "deep_winter_festival",
                LocalizedText.of(
                        "漫长的冬夜最适合安静地看书和做实验了，星象也格外清晰……需要什么药剂直接说。",
                        "Long nights are made for reading and experiments, and the winter stars are sharp... Just say what you need."));

        // ── 杂货商 ──
        register(
                "grocer",
                "spring_festival",
                LocalizedText.of(
                        "春祭特供新鲜上架啦！花苗、肥料应有尽有，想给家里装点门面的可千万别错过！",
                        "Spring festival stock's out! Saplings, fertilizer — don't miss them if you're dressing the place up."));
        register(
                "grocer",
                "midsummer_night",
                LocalizedText.of(
                        "庆典夜里要用的唱片和零食我这儿都备齐了。等外乡的旅客一到，手慢可就没啦！",
                        "Records and treats for the night are all stocked. Guests are pouring in — first come, first served."));
        register(
                "grocer",
                "harvest_festival",
                LocalizedText.of(
                        "哎哟，今年的货架都快被塞爆了！南瓜派、大蛋糕，还有各种节日食材，绝对管够！",
                        "The shelves can barely hold the bounty! Pumpkin pies, cakes, festival fare — plenty for all."));
        register(
                "grocer",
                "deep_winter_festival",
                LocalizedText.of(
                        "新鲜的年货和烟花全到货了！守岁嘛，就得热热闹闹的。快进来挑点好东西吧！",
                        "New Year goods and fireworks have arrived! A vigil should be loud and bright — come pick some out."));

        // ── 屠夫 ──
        register(
                "butcher",
                "spring_festival",
                LocalizedText.of(
                        "春天的小羔羊肉最是鲜嫩！要是准备做节日大餐，我这儿现切现剁，包你满意！",
                        "Spring lamb's at its tenderest. Cooking a feast? I'll cut it fresh."));
        register(
                "butcher",
                "midsummer_night",
                LocalizedText.of(
                        "晚上的烧烤摊全是在我这儿进的货，放心吧，好肉管够！",
                        "The night barbecue stalls all buy from me — meat aplenty!"));
        register(
                "butcher",
                "harvest_festival",
                LocalizedText.of(
                        "宴会要用的大件烤肉包在我身上！你瞧瞧这五花肉的纹理，丰收年养出来的猪就是不一样！",
                        "Leave the roast to me. Look at that marbling — harvest-year hogs are something else."));
        register(
                "butcher",
                "deep_winter_festival",
                LocalizedText.of(
                        "天一冷就得大口吃肉好囤膘啊！守岁那晚的桌上，怎么能少了一锅热气腾腾的肉丸子炖锅呢？",
                        "Cold days call for meat to keep warm. No vigil table should lack a pot of meatball stew."));

        // ── 建筑工 ──
        register(
                "builder",
                "spring_festival",
                LocalizedText.of(
                        "节后就要开工修整镇子了，趁着假期我得好好歇几天。怎么，要买建材吗？",
                        "Repairs on the town start after the festival — resting while I can. Need building supplies?"));
        register(
                "builder",
                "midsummer_night",
                LocalizedText.of(
                        "庆典上那大木台子可是我亲手搭的，结实着呢！你们在上面随便蹦跶，塌不了！",
                        "I built the festival stage myself. Solid as a rock — dance all you like."));
        register(
                "builder",
                "harvest_festival",
                LocalizedText.of(
                        "巡游的花车要是散架了可千万别来找我……哈哈，开玩笑的，需要修补随时来找我！",
                        "If the parade floats fall apart, don't come crying to me... Haha, just kidding, come find me anytime if you need repairs!"));
        register(
                "builder",
                "deep_winter_festival",
                LocalizedText.of(
                        "入冬前我刚把大伙的屋顶都检修了一遍，再大的雪也压不坏。你需要备些木料过冬吗？",
                        "I just inspected everyone's roofs before winter set in, so no amount of snow will crush them now. Do you need to stock up on some timber for the winter?"));

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
                        "这种夜晚最适合钓夜行鱼了。水面上映着庆典的灯影，连鱼儿都变得不怕人了呢。",
                        "Night-caught fish bite well with festival lanterns on the water — they're not shy tonight."));
        register(
                "fisherman",
                "harvest_festival",
                LocalizedText.of(
                        "丰收的可不止是地里的庄稼，海里也是！这几天的渔获那是相当丰盛！",
                        "The harvest isn't only in the fields — the sea's been generous too. Quite the haul lately!"));
        register(
                "fisherman",
                "deep_winter_festival",
                LocalizedText.of(
                        "嘶，海风真够冷的，港口怕是要冻上了。想熬口热鱼汤暖暖身子的话，我这儿还留了些好鱼。",
                        "Sea wind's turned cold, harbor's freezing. Fancy hot fish soup? I've got the makings."));

        // ── 游商 ──
        register(
                "wandering_trader",
                "spring_festival",
                LocalizedText.of(
                        "难得赶上春祭，靠岸停船歇两天，你们镇上可真热闹啊。来看看我从外地带来的新奇玩意儿？",
                        "It's rare to catch the Spring Festival. I've docked my ship to rest for a couple of days, and your town is bustling! Want to see the exotic trinkets I brought from afar?"));
        register(
                "wandering_trader",
                "midsummer_night",
                LocalizedText.of(
                        "我走南闯北，到处都在办夜市，还是你们镇上最热闹。来挑挑货吧，都是别处见不到的。",
                        "I've traveled far and wide, and night markets are everywhere, but your town's is the liveliest. Come pick out some wares—you won't find these anywhere else."));
        register(
                "wandering_trader",
                "harvest_festival",
                LocalizedText.of(
                        "港口停满了运粮船，到处都是丰收的景象。老板，要买点什么稀罕货吗？",
                        "The harbor is full of grain ships, a true scene of bountiful harvest. Innkeeper, would you care to buy some rare goods?"));
        register(
                "wandering_trader",
                "deep_winter_festival",
                LocalizedText.of(
                        "这么冷的天气，你们这儿的守岁倒是热火朝天的。要来点外邦的特产吗？",
                        "Such freezing weather, yet your winter vigil here is burning with life! Care for some specialties from foreign lands?"));
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
