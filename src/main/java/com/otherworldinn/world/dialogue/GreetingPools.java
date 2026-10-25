package com.otherworldinn.world.dialogue;

import java.util.List;
import java.util.Map;

/** 城镇 NPC 每次打开对话时随机抽取的问候语池；键为 {@code dialogue.otherworldinn.<npc>.greeting.<i>}。 */
public final class GreetingPools {

    private static final Map<String, List<LocalizedText>> POOLS = Map.of(
            "blacksmith",
            List.of(
                    new LocalizedText("要什么自己看，我手上干活呢。", "Look around yourself, I'm working here."),
                    new LocalizedText("又是你。终于愿意修你的包浆镐子了？", "You again. Finally want that patina-caked pick fixed?"),
                    new LocalizedText("别站门口，挡着我采光了。", "Don't stand in the doorway, you're blocking my light."),
                    new LocalizedText(
                            "你又来了？这代表我打的工具不算差，对吗？",
                            "Back again? Guess my tools aren't half bad, right?")),
            "farmer",
            List.of(
                    new LocalizedText("哎哟，来得正好，帮我扶一下。", "Oh good, give me a hand with this."),
                    new LocalizedText("今年天气不太好。", "Weather's been bad this year."),
                    new LocalizedText("尝过这个没有？酸甜口的。好吧确实有点酸。", "Tried these? Sweet-sour. ...Fine, they're a bit sour."),
                    new LocalizedText("你说这天是不是又要下雨？", "Reckon it's going to rain again?")),
            "magician",
            List.of(
                    new LocalizedText("哦。你来了。等我一下——", "Oh. You're here. Hang on a second—"),
                    new LocalizedText("别碰那个。真的别碰。", "Don't touch that. Really."),
                    new LocalizedText("我就知道你会来，怎么样，厉害吧。", "I knew you'd come. Impressive, right?"),
                    new LocalizedText(
                            "你来啦！你有多少钱给我？...不是，你要买什么？",
                            "There you are! How much money you got for me? ...No — what are you buying?")),
            "grocer",
            List.of(
                    new LocalizedText("来啦！刚到的货。", "There you are! Fresh stock."),
                    new LocalizedText("不接受讨价还价，要什么自己挑。", "No haggling. Pick what you want."),
                    new LocalizedText("不能再便宜了朋友再卖亏本了。", "Can't go lower, friend. Any less and I'm losing money."),
                    new LocalizedText(
                            "那个铁匠根本就不识货！上次他来我这...不说了，买什么？",
                            "That blacksmith can't tell good goods from junk! Last time he came in— forget it. What're you buying?")),
            "butcher",
            List.of(
                    new LocalizedText(
                            "真是一头大肥猪啊...噢当然不是说你，要什么？",
                            "Now that's a fat pig... Oh, not you of course. What do you need?"),
                    new LocalizedText("今天这块肉排不错，要不要？", "Nice steak today. Want it?"),
                    new LocalizedText("小心地滑，地上全是水，刚刚水桶翻了。", "Mind the floor, all wet. Knocked the bucket over just now."),
                    new LocalizedText("新鲜的肉！", "Fresh meat!"),
                    new LocalizedText("要买好好买不买别拨拉。", "Buy it or don't. Stop poking at it.")),
            "builder",
            List.of(
                    new LocalizedText("房子不错啊朋友。", "Nice place you've got, friend."),
                    new LocalizedText("要盖就盖大点，大房子住起来不烧心。", "Build it big. A big house sits easy."),
                    new LocalizedText("图纸看看呗，看上什么我帮你造。", "Have a look at the plans. Pick something and I'll build it."),
                    new LocalizedText("我年轻的时候，盖过七层的超级大房子。", "When I was young I built a seven-storey monster.")),
            "fisherman",
            List.of(
                    new LocalizedText("今天钓上什么了吗朋友？", "Catch anything today, friend?"),
                    new LocalizedText("钓上的鱼在旁边那个箱子里卖。", "What I catch goes in that box over there."),
                    new LocalizedText("我应该卖你一点鱼饵和渔具来着。", "I ought to sell you some bait and tackle."),
                    new LocalizedText("我在这儿坐一上午了。", "Been sitting here all morning."),
                    new LocalizedText("要买什么？", "What'll it be?")),
            "wandering_trader",
            List.of(
                    new LocalizedText("哟，这次带了点真货。", "Well now — brought some real goods this time."),
                    new LocalizedText("这个价只卖给你，你去别的地方还没得买呢。", "This price is just for you. You won't find it elsewhere."),
                    new LocalizedText(
                            "要买什么？我这什么都有，真的什么都有。",
                            "What'll you have? I've got everything. Everything, really."),
                    new LocalizedText("都是新奇东西，来看看呗。", "All sorts of odd things. Come take a look.")));

    private GreetingPools() {}

    /** 该 NPC 的问候语池；没有配置时返回空表（调用方回退到固定问候）。 */
    public static List<LocalizedText> of(String npcId) {
        List<LocalizedText> pool = POOLS.get(npcId);
        return pool == null ? List.of() : pool;
    }

    /** 问候语的翻译键。 */
    public static String textKey(String npcId, int index) {
        return "dialogue.otherworldinn." + npcId + ".greeting." + index;
    }
}
