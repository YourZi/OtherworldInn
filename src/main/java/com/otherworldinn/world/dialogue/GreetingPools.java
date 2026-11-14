package com.otherworldinn.world.dialogue;

import java.util.List;
import java.util.Map;

/** 城镇 NPC 每次打开对话时随机抽取的问候语池；键为 {@code dialogue.otherworldinn.<npc>.greeting.<i>}。 */
public final class GreetingPools {

    private static final Map<String, List<LocalizedText>> POOLS = Map.of(
            "blacksmith",
            List.of(
                    new LocalizedText("要买什么？东西都在架子上，别乱动我刚打好的护甲。", "What are you buying? The goods are on the rack, just don't touch the armor I just forged."),
                    new LocalizedText("又是你。这次是想修你的镐子，还是来找点结实的金属材料？", "You again. Are you here to fix your pickaxe this time, or looking for some sturdy metal materials?"),
                    new LocalizedText("别站在门口挡光，有什么需要快说，我后面还有几件活要赶。", "Don't stand in the doorway blocking the light. Speak up if you need something, I've got a few more commissions to finish."),
                    new LocalizedText(
                            "你又来了？看来我打的工具还算合你的手。",
                            "Back again? Looks like the tools I forge suit your hands well enough.")),
            "farmer",
            List.of(
                    new LocalizedText("来得正好，帮我把这袋种子搬到那边去。", "Oh good, give me a hand with this."),
                    new LocalizedText("今年天气不太对，我得赶紧回田里看看。", "Weather's been bad this year."),
                    new LocalizedText("尝尝这个？刚摘下来的，味道挺足。", "Tried these? Sweet-sour. ...Fine, they're a bit sour."),
                    new LocalizedText("这天看着又要下雨了，庄稼可受不了连阴雨。", "Reckon it's going to rain again?")),
            "magician",
            List.of(
                    new LocalizedText("哦，是你啊。等我把这瓶药剂处理完——", "Oh. You're here. Hang on a second—"),
                    new LocalizedText("小心点，那边的架子上放着不稳定的东西，别乱碰。", "Don't touch that. Really."),
                    new LocalizedText("我就知道你会来。这次是想要药水，还是在找什么附魔？", "I knew you'd come. Impressive, right?"),
                    new LocalizedText(
                            "欢迎光临。是准备好来买单了，还是只是随便看看？",
                            "There you are! How much money you got for me? ...No — what are you buying?")),
            "grocer",
            List.of(
                    new LocalizedText("来啦！刚到的货，还热乎着呢。", "There you are! Fresh stock."),
                    new LocalizedText("明码标价，要什么自己挑。", "No haggling. Pick what you want."),
                    new LocalizedText("不能再便宜了，这价格我已经很照顾老熟人了。", "Can't go lower, friend. Any less and I'm losing money."),
                    new LocalizedText(
                            "你需要点什么？",
                            "That blacksmith can't tell good goods from junk! Last time he came in— forget it. What're you buying?")),
            "butcher",
            List.of(
                    new LocalizedText(
                            "嘿！来看看这批刚处理好的肉，新鲜得很。",
                            "Now that's a fat pig... Oh, not you of course. What do you need?"),
                    new LocalizedText("今天的肉排成色不错，要带两块回去尝尝吗？", "Nice steak today. Want it?"),
                    new LocalizedText("脚下看着点，刚清理过地面，还没干透。", "Mind the floor, all wet. Knocked the bucket over just now."),
                    new LocalizedText("上好的生肉！", "Fresh meat!"),
                    new LocalizedText("看中哪块跟我说，我给你切，别光在上面拨拉。", "Buy it or don't. Stop poking at it.")),
            "builder",
            List.of(
                    new LocalizedText("旅社的结构看着还算稳固，需要扩建或者翻修吗？", "The inn's structure looks solid enough. Do you need an expansion or a renovation?"),
                    new LocalizedText("大房子住起来确实舒服，但也得地基打得够深才行。", "A large house is definitely comfortable to live in, but the foundation needs to be dug deep enough."),
                    new LocalizedText("来看看这些蓝图？只要材料备齐，剩下的交给我就行。", "Want to take a look at these blueprints? As long as you have the materials ready, leave the rest to me."),
                    new LocalizedText("想当年我也参与过不少大工程，你这点小活儿找我准没错。", "I've been a part of many massive projects back in the day, you can definitely count on me for a small job like this.")),
            "fisherman",
            List.of(
                    new LocalizedText("今天水里的动静不错，你有钓到什么吗？", "Catch anything today, friend?"),
                    new LocalizedText("来卖鱼的话，都放在旁边的箱子里就行，我一会来收。", "What I catch goes in that box over there."),
                    new LocalizedText("我这儿还有些备用的鱼饵和渔具，需要吗？", "I ought to sell you some bait and tackle."),
                    new LocalizedText("在这儿坐一上午了，水里的鱼可真有耐心。", "Been sitting here all morning."),
                    new LocalizedText("要买点鲜鱼吗？", "What'll it be?")),
            "wandering_trader",
            List.of(
                    new LocalizedText("哟，这次我带了不少从远方运来的稀奇货。", "Oho, I've brought quite a few rare goods from distant lands this time."),
                    new LocalizedText("这个价只卖给你，去别处可找不着第二家。", "This price is for you alone. You won't find a deal like this anywhere else."),
                    new LocalizedText(
                            "看看吧，我这儿的货绝对让你大开眼界。",
                            "Take a look! My wares will surely open your eyes to the wonders of the world."),
                    new LocalizedText("都是些市面上少见的新鲜玩意儿，不打算带走一件吗？", "These are fresh novelties rarely seen on the market. Won't you take one with you?")));

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
