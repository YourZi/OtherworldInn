package com.otherworldinn.world.commission;

import com.otherworldinn.world.dialogue.LocalizedText;
import com.otherworldinn.world.photo.PhotoObjectiveRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

public final class CommissionRegistry {
    private static final List<CommissionTemplate> TEMPLATES = new ArrayList<>();

    static {
        register("farm_supply")
                .stars(1, 2)
                .weight(12)
                .description(
                        LocalizedText.of(
                                "农场的粮食储备目前告急，烦请协助运送一批物资以供补给。",
                                "The farm's food reserves are currently running critically low. Please help deliver a batch of staple supplies."))
                .submit("minecraft:wheat", 16)
                .submit("minecraft:bread", 8)
                .rewardCoins(40)
                .rewardFavor("otherworldinn:farmer", 60)
                .build();

        register("forge_maintenance")
                .stars(1, 2)
                .weight(10)
                .description(
                        LocalizedText.of(
                                "铁匠铺急需补充备用金属零件的库存。请先行运送一批基础材料以备加工之用。",
                                "The forge urgently needs to restock its spare metal parts. Deliver a batch of basic materials in advance for processing."))
                .submit("minecraft:iron_ingot", 12)
                .submit("minecraft:gold_ingot", 4)
                .rewardCoins(40)
                .rewardFavor("otherworldinn:blacksmith", 90)
                .build();

        register("town_patrol_zombie")
                .stars(1, 3)
                .weight(14)
                .description(
                        LocalizedText.of(
                                "近期城镇外围夜间治安状况堪忧。请协助巡视并清理周边游荡的僵尸，保障城镇安全。",
                                "The night security on the outskirts of town has been concerning lately. Help patrol and clear out the wandering zombies to ensure the town's safety."))
                .kill("minecraft:zombie", 10)
                .rewardCoins(80)
                .build();

        register("road_patrol_mixed")
                .stars(3, 3)
                .weight(9)
                .description(
                        LocalizedText.of(
                                "城镇巡逻队现发出支援请求。请协助沿途清理骷髅威胁，并为巡逻队员提供食物与工具的补给。",
                                "The town patrol has issued a request for support. Help clear out skeleton threats along the way and provide the patrol members with food and tool supplies."))
                .kill("minecraft:skeleton", 12)
                .submit("minecraft:bread", 12)
                .submit("minecraft:diamond_sword", 1)
                .rewardCoins(120)
                .build();

        register("magic_study_materials")
                .stars(2, 3)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "魔女的最新研究面临材料短缺的困境。请协助收集一系列稀有的魔法研究材料。",
                                "The magician's latest research is facing a shortage of materials. Help gather a series of rare magical research components."))
                .submit("minecraft:lapis_lazuli", 16)
                .submit("minecraft:ender_pearl", 4)
                .submit("minecraft:glowstone_dust", 8)
                .rewardCoins(70)
                .rewardFavor("otherworldinn:magician", 80)
                .build();

        register("farm_animal_feed")
                .stars(1, 2)
                .weight(10)
                .description(
                        LocalizedText.of(
                                "农场现需补充动物饲料，烦请协助运送部分小麦与胡萝卜。",
                                "The farm needs to replenish its animal feed. Please help deliver some wheat and carrots."))
                .submit("minecraft:wheat", 24)
                .submit("minecraft:carrot", 16)
                .rewardCoins(45)
                .rewardFavor("otherworldinn:farmer", 70)
                .build();

        register("forge_tool_repair")
                .stars(2, 4)
                .weight(7)
                .description(
                        LocalizedText.of(
                                "城外围有部分设施急需维护修缮。现招募人手并征集修理所需的各项材料。",
                                "Some facilities on the outskirts of town are in desperate need of maintenance. We are now recruiting hands and collecting various materials needed for repairs."))
                .submit("minecraft:iron_ingot", 8)
                .submit("minecraft:stick", 16)
                .kill("minecraft:zombie", 5)
                .rewardCoins(95)
                .rewardFavor("otherworldinn:blacksmith", 85)
                .build();

        register("magic_potion_supply")
                .stars(3, 4)
                .weight(6)
                .description(
                        LocalizedText.of(
                                "魔女的药水库存已显不足，需要补充部分基础药材。",
                                "The magician's potion inventory is running low and needs a restock of basic alchemical ingredients."))
                .submit("minecraft:nether_wart", 12)
                .submit("minecraft:spider_eye", 8)
                .submit("minecraft:ghast_tear", 2)
                .rewardCoins(110)
                .rewardFavor("otherworldinn:magician", 75)
                .build();

        register("forge_weapon_order")
                .stars(4, 5)
                .weight(5)
                .description(
                        LocalizedText.of(
                                "铁匠近期承接了一批武器订单，现需采购高级金属材料。",
                                "The blacksmith recently took on a batch of weapon orders and now needs to procure high-grade metal materials."))
                .submit("minecraft:diamond", 3)
                .submit("minecraft:emerald", 6)
                .submit("minecraft:netherite_ingot", 1)
                .rewardCoins(280)
                .rewardFavor("otherworldinn:blacksmith", 95)
                .build();

        register("magic_ritual_prep")
                .stars(4, 5)
                .weight(4)
                .description(
                        LocalizedText.of(
                                "魔女即将举行一项重要仪式，急需获取极其稀有的材料。",
                                "The magician is about to conduct an important ritual and urgently needs to acquire extremely rare materials."))
                .submit("minecraft:blaze_rod", 4)
                .submit("minecraft:shulker_shell", 2)
                .submit("minecraft:dragon_breath", 1)
                .rewardCoins(250)
                .rewardFavor("otherworldinn:magician", 100)
                .build();

        register("town_cleanup_spiders")
                .stars(2, 3)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "仓库内部出现了蜘蛛活动的迹象。请尽快前往清理，以确保物资存储的安全。",
                                "Traces of spider activity have appeared inside the warehouse. Head there to clear them out as soon as possible to ensure the safety of the stored goods."))
                .kill("minecraft:spider", 8)
                .submit("minecraft:string", 12)
                .rewardCoins(65)
                .build();

        register("farm_seed_supply")
                .stars(1, 2)
                .weight(11)
                .description(
                        LocalizedText.of(
                                "农场即将开展新一轮的轮作计划，急需补充各类作物的种子。请协助收集并提供。",
                                "The farm is about to start a new crop rotation cycle and urgently needs to restock various seeds. Please help collect and provide them."))
                .submit("minecraft:wheat_seeds", 20)
                .submit("minecraft:melon_seeds", 12)
                .submit("minecraft:pumpkin_seeds", 12)
                .rewardCoins(40)
                .rewardFavor("otherworldinn:farmer", 60)
                .build();

        register("forge_fuel_supply")
                .stars(1, 2)
                .weight(10)
                .description(
                        LocalizedText.of(
                                "铁匠铺的燃料储备即将耗尽。请尽快运送一批煤炭，以保障锻造工作的顺利进行。",
                                "The forge's fuel reserves are nearly depleted. Please deliver a batch of coal as soon as possible to ensure forging operations continue smoothly."))
                .submit("minecraft:coal", 32)
                .rewardCoins(50)
                .rewardFavor("otherworldinn:blacksmith", 55)
                .build();

        register("magic_library_organize")
                .stars(2, 3)
                .weight(7)
                .description(
                        LocalizedText.of(
                                "魔女的私人图书馆急需整理。请在协助整理的同时，留意并收集有价值的古籍文献。",
                                "The magician's private library is in desperate need of organization. While helping to tidy up, keep an eye out for and collect any valuable ancient texts."))
                .submit("minecraft:book", 8)
                .submit("minecraft:paper", 24)
                .rewardCoins(85)
                .rewardFavor("otherworldinn:magician", 70)
                .build();

        register("forge_armor_order")
                .stars(3, 4)
                .weight(6)
                .description(
                        LocalizedText.of(
                                "城镇卫队现需换装一批新式护甲，请为铁匠提供相应的锻造材料。",
                                "The town guard requires a new issue of armor. Please provide the blacksmith with the necessary forging materials."))
                .submit("minecraft:iron_ingot", 16)
                .submit("minecraft:leather", 24)
                .submit("minecraft:gold_ingot", 8)
                .rewardCoins(130)
                .rewardFavor("otherworldinn:blacksmith", 80)
                .build();

        register("town_festival_prep")
                .festival("harvest_festival")
                .stars(2, 3)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "秋收祭的庆典宴席尚缺部分甜点。请筹备并运送蛋糕、曲奇及南瓜派，以丰富餐桌供应。",
                                "The Harvest Festival banquet is still short on desserts. Prepare and deliver cakes, cookies, and pumpkin pies to enrich the feast."))
                .submit("minecraft:cake", 2)
                .submit("minecraft:cookie", 32)
                .submit("minecraft:pumpkin_pie", 4)
                .rewardCoins(125)
                .rewardFavor("otherworldinn:grocer", 90)
                .build();

        // ── 模组联动委托（requiresMod 门控：模组未加载时不刷出）──

        register("grocer_exotic_food_order")
                .requiresMod("kaleidoscope_cookery")
                .stars(2, 3)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "杂货铺计划引进部分异域特色食材进行试销。请带上青团与炸春卷供铺主评估。",
                                "The grocer plans to trial some exotic ingredients. Bring some qingtuan and fried spring rolls for the owner to evaluate."))
                .submit("kaleidoscope_cookery:qingtuan", 8)
                .submit("kaleidoscope_cookery:fried_spring_roll", 8)
                .rewardCoins(100)
                .rewardFavor("otherworldinn:grocer", 85)
                .build();

        register("forge_cogwheel_supply")
                .requiresMod("create")
                .stars(2, 3)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "铁匠有意研究机械动力相关的机件。请协助收集一批齿轮与传动杆以供其测试。",
                                "The blacksmith wishes to study kinetic mechanisms. Help gather a batch of cogwheels and shafts for testing."))
                .submit("create:cogwheel", 12)
                .submit("create:shaft", 8)
                .rewardCoins(110)
                .rewardFavor("otherworldinn:blacksmith", 95)
                .build();

        register("forge_precision_mechanism")
                .requiresMod("create")
                .stars(4, 5)
                .weight(4)
                .description(
                        LocalizedText.of(
                                "铁匠希望拆解精密构件以研究其内部结构，烦请提供两枚部件以换取黄铜。",
                                "The blacksmith wishes to dismantle precision mechanisms to study their internal structure. Please provide two components in exchange for brass."))
                .submit("create:precision_mechanism", 2)
                .rewardCoins(96)
                .rewardItem("create:brass_ingot", 4)
                .rewardFavor("otherworldinn:blacksmith", 100)
                .build();

        register("farm_straw_hat")
                .requiresMod("kaleidoscope_cookery")
                .stars(1, 2)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "农夫计划为雇工们购置遮阳草帽。请筹备一批普通的草帽以及带有花饰的草帽。",
                                "The farmer plans to buy sun-shading straw hats for the hired hands. Prepare a batch of plain straw hats and one with floral decorations."))
                .submit("kaleidoscope_cookery:straw_hat", 3)
                .submit("kaleidoscope_cookery:straw_hat_flower", 1)
                .rewardCoins(60)
                .rewardFavor("otherworldinn:farmer", 75)
                .build();

        register("grocer_tavern_order")
                .requiresMod("kaleidoscope_tavern")
                .stars(2, 3)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "杂货铺准备进购一批特色酒水。请提供梅酒与冰酒各数瓶，以便测试市场反响。",
                                "The grocer is preparing to stock a batch of specialty liquors. Provide several bottles of plum wine and ice wine to test the market response."))
                .submit("kaleidoscope_tavern:plum_wine", 6)
                .submit("kaleidoscope_tavern:ice_wine", 4)
                .rewardCoins(95)
                .rewardFavor("otherworldinn:grocer", 90)
                .build();

        register("magic_flower_tea")
                .requiresMod("kaleidoscope_cookery")
                .stars(2, 3)
                .weight(6)
                .description(
                        LocalizedText.of(
                                "魔女近期对异国茶饮产生了浓厚兴趣。请协助收集一些花茶与大麦茶以供其品鉴。",
                                "The magician has recently developed a keen interest in exotic teas. Help gather some flower tea and barley tea for her to taste."))
                .submit("kaleidoscope_cookery:flower_tea", 6)
                .submit("kaleidoscope_cookery:barley_tea", 8)
                .rewardCoins(90)
                .rewardFavor("otherworldinn:magician", 80)
                .build();

        register("builder_copper_casing")
                .requiresMod("create")
                .stars(2, 3)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "建筑工计划为商铺门面增加铜制装饰，烦请提供数个铜机壳。",
                                "The builder plans to add copper accents to the shopfronts. Please provide several copper casings."))
                .submit("create:copper_casing", 4)
                .rewardCoins(95)
                .rewardFavor("otherworldinn:builder", 90)
                .build();

        register("ecology_seahorse_snapshot")
                .requiresMod("spawn")
                .stars(1, 2)
                .weight(6)
                .description(
                        LocalizedText.of(
                                "珊瑚礁海域中游弋着色彩斑斓的海马。请潜入该区域，为渔夫带回一张海马的影像记录。",
                                "Colorful seahorses drift through the coral reefs. Dive into the area and bring back a photographic record of a seahorse for the fisherman."))
                .photo(PhotoObjectiveRegistry.SEAHORSE_SNAPSHOT_ID.toString())
                .rewardCoins(26)
                .rewardItem("spawn:seahorse_bucket", 1)
                .build();

        register("ecology_herring_school_snapshot")
                .requiresMod("spawn")
                .stars(2, 2)
                .weight(6)
                .description(
                        LocalizedText.of(
                                "海域中的鲱鱼已结成鱼群活动。请协助渔夫，拍摄一张至少有三条鲱鱼同框的生态照片。",
                                "The herring in the sea have formed schools. Help the fisherman by taking an ecological photograph with at least three herring in the same frame."))
                .photo(PhotoObjectiveRegistry.HERRING_SCHOOL_ID.toString())
                .rewardCoins(36)
                .rewardItem("spawn:canned_herring", 2)
                .build();

        register("ecology_sea_cow_snapshot")
                .requiresMod("spawn")
                .stars(2, 3)
                .weight(5)
                .description(
                        LocalizedText.of(
                                "温顺的海牛通常栖息于海草床群系。请前往该区域，为渔夫拍摄一张海牛与其栖息环境的合影。",
                                "Gentle sea cows typically inhabit the seagrass meadows. Head to the area and take a photograph of a sea cow in its natural habitat for the fisherman."))
                .photo(PhotoObjectiveRegistry.SEA_COW_MEADOW_ID.toString())
                .rewardCoins(55)
                .rewardItem("spawn:casting_net", 1)
                .rewardFavor("otherworldinn:fisherman", 40)
                .build();

        register("ecology_dodo_snapshot")
                .requiresMod("spawn")
                .stars(3, 4)
                .weight(4)
                .description(
                        LocalizedText.of(
                                "渡渡鸟岛屿上栖息着独特的无翼渡渡鸟。请前往该岛，拍摄并带回一张渡渡鸟的生态照片。",
                                "Flightless dodos inhabit the Dodo Island. Travel there, take a picture, and bring back an ecological photograph of a dodo."))
                .photo(PhotoObjectiveRegistry.DODO_ISLAND_ID.toString())
                .rewardCoins(72)
                .rewardItem("spawn:dodo", 2)
                .rewardFavor("otherworldinn:fisherman", 55)
                .build();

        register("ecology_angler_snapshot")
                .requiresMod("spawn")
                .stars(3, 4)
                .weight(4)
                .description(
                        LocalizedText.of(
                                "渔夫报告称深水区频现幽微亮光。请潜入深水查明情况，并拍摄清晰的照片作为调查记录。",
                                "The fisherman reports a faint glow appearing frequently in the deep waters. Dive down, investigate, and take a clear photograph as an investigative record."))
                .photo(PhotoObjectiveRegistry.ANGLER_FISH_SNAPSHOT_ID.toString())
                .rewardCoins(78)
                .rewardItem("spawn:angler_fish", 2)
                .rewardFavor("otherworldinn:fisherman", 60)
                .build();

        register("ecology_snail_snapshot")
                .requiresMod("spawn")
                .stars(1, 1)
                .weight(7)
                .description(
                        LocalizedText.of(
                                "菜园周围散落着许多蜗牛壳。请协助农夫寻找存活的蜗牛，并拍摄一张照片以作记录。",
                                "Many snail shells are scattered around the vegetable garden. Help the farmer find a living snail and take a photograph for the records."))
                .photo(PhotoObjectiveRegistry.SNAIL_SNAPSHOT_ID.toString())
                .rewardCoins(24)
                .rewardItem("spawn:snail_shell", 2)
                .rewardFavor("otherworldinn:farmer", 25)
                .build();

        register("ecology_hamster_snapshot")
                .requiresMod("spawn")
                .stars(1, 2)
                .weight(6)
                .description(
                        LocalizedText.of(
                                "开阔的草地上栖息着有囤粮习性的仓鼠。请协助农夫，拍摄一张仓鼠活动的清晰影像。",
                                "Hoarding hamsters dwell in the open grasslands. Help the farmer by taking a clear photograph of a hamster in action."))
                .photo(PhotoObjectiveRegistry.HAMSTER_SNAPSHOT_ID.toString())
                .rewardCoins(28)
                .rewardItem("spawn:sunflower_seeds", 8)
                .rewardFavor("otherworldinn:farmer", 30)
                .build();

        register("ecology_ant_garden_snapshot")
                .requiresMod("spawn")
                .stars(2, 3)
                .weight(5)
                .description(
                        LocalizedText.of(
                                "蚂蚁们正频繁穿梭于蚁丘之间搬运物资。请协助农夫拍摄一张它们在蚂蚁之园辛勤劳作的影像。",
                                "The ants are busy ferrying supplies between their hills. Help the farmer take a picture of their hard work in the ant garden."))
                .photo(PhotoObjectiveRegistry.ANT_GARDEN_ID.toString())
                .rewardCoins(52)
                .rewardItem("spawn:ant_pupa", 1)
                .rewardFavor("otherworldinn:farmer", 45)
                .build();

        register("ecology_octopus_snapshot")
                .requiresMod("spawn")
                .stars(2, 3)
                .weight(5)
                .description(
                        LocalizedText.of(
                                "魔女对海洋中能够藏身于壳内的智慧生物颇感兴趣。请寻找章鱼的踪迹，并为其拍摄一张照片。",
                                "The magician is quite interested in intelligent marine creatures that hide in shells. Track down an octopus and take a photograph of it."))
                .photo(PhotoObjectiveRegistry.OCTOPUS_SNAPSHOT_ID.toString())
                .rewardCoins(55)
                .rewardItem("spawn:captured_octopus", 1)
                .rewardFavor("otherworldinn:magician", 45)
                .build();

        register("ecology_stranded_snapshot")
                .requiresMod("spawn")
                .stars(3, 3)
                .weight(5)
                .description(
                        LocalizedText.of(
                                "魔女对能够主动攻击怪物的涂蜡构装体抱有浓厚兴趣。请寻找滞儡的踪迹，并拍摄一张照片交予她。",
                                "The magician has a keen interest in waxed constructs that actively attack monsters. Track down a Stranded and take a photograph for her."))
                .photo(PhotoObjectiveRegistry.STRANDED_SNAPSHOT_ID.toString())
                .rewardCoins(62)
                .rewardItem("spawn:mucus", 4)
                .rewardFavor("otherworldinn:magician", 50)
                .build();

        register("ecology_firekeeper_snapshot")
                .requiresMod("spawn")
                .stars(4, 5)
                .weight(3)
                .description(
                        LocalizedText.of(
                                "火山岛屿上有熔岩构成的守卫在游荡。请在确保自身安全的前提下，从远处为魔女拍摄一张守卫的照片。",
                                "Lava-formed guards roam the volcanic island. While ensuring your own safety, take a photograph of a firekeeper from a distance for the magician."))
                .photo(PhotoObjectiveRegistry.FIREKEEPER_SNAPSHOT_ID.toString())
                .rewardCoins(85)
                .rewardItem("spawn:sunstone", 2)
                .rewardFavor("otherworldinn:magician", 65)
                .build();

        register("ecology_coastal_crab_snapshot")
                .requiresMod("spawn")
                .stars(1, 2)
                .weight(6)
                .description(
                        LocalizedText.of(
                                "退潮后的海岸上出现了大量螃蟹。请前往该区域，为屠夫拍摄一张海岸蟹的清晰照片以供研究。",
                                "A large number of crabs have appeared on the shore after low tide. Head there and take a clear photograph of a coastal crab for the butcher's research."))
                .photo(PhotoObjectiveRegistry.COASTAL_CRAB_SNAPSHOT_ID.toString())
                .rewardCoins(30)
                .rewardItem("spawn:coastal_crab_claw", 1)
                .rewardFavor("otherworldinn:butcher", 30)
                .build();

        register("ecology_tuna_snapshot")
                .requiresMod("spawn")
                .stars(2, 3)
                .weight(5)
                .description(
                        LocalizedText.of(
                                "广阔的海域中生活着体型庞大的金枪鱼。请出海寻觅其踪迹，并拍摄一张金枪鱼的照片带给屠夫。",
                                "Massive tuna swim in the vast ocean. Set sail to track them down and bring a photograph of a tuna to the butcher."))
                .photo(PhotoObjectiveRegistry.TUNA_SNAPSHOT_ID.toString())
                .rewardCoins(52)
                .rewardItem("spawn:tuna_chunk", 6)
                .rewardFavor("otherworldinn:butcher", 45)
                .build();

        register("ecology_spider_crab_snapshot")
                .requiresMod("spawn")
                .stars(3, 4)
                .weight(4)
                .description(
                        LocalizedText.of(
                                "寒冷海域中潜藏着能够吐出冰泡的蜘蛛蟹。请潜入水下，为屠夫拍摄一张该生物的清晰照片。",
                                "Spider crabs that spit frozen bubbles lurk in the frigid waters. Dive underwater and take a clear photograph of this creature for the butcher."))
                .photo(PhotoObjectiveRegistry.SPIDER_CRAB_SNAPSHOT_ID.toString())
                .rewardCoins(70)
                .rewardItem("spawn:crab_boil", 2)
                .rewardFavor("otherworldinn:butcher", 55)
                .build();

        register("farm_seed_introduction")
                .requiresMod("spawn")
                .stars(1, 2)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "农夫计划引进并试种一批新型作物。请协助收集向日葵种子与椰枣，以支持农业试验。",
                                "The farmer plans to introduce and trial a new batch of crops. Help gather sunflower seeds and dates to support the agricultural experiment."))
                .submit("spawn:sunflower_seeds", 16)
                .submit("spawn:dates", 12)
                .rewardCoins(55)
                .rewardFavor("otherworldinn:farmer", 70)
                .build();

        register("farm_garden_decor")
                .requiresMod("spawn")
                .stars(2, 3)
                .weight(7)
                .description(
                        LocalizedText.of(
                                "农场计划铺设一条壳瓦小径并修筑环形花坛。请协助筹备蜗牛壳瓦与向日葵等相关材料。",
                                "The farm plans to lay a shell-tile path and build a circular flower bed. Help gather materials like snail shell tiles and sunflowers."))
                .submit("spawn:snail_shell_tiles", 8)
                .submit("spawn:sunflower", 4)
                .rewardCoins(90)
                .rewardFavor("otherworldinn:farmer", 80)
                .build();

        register("magic_mucus_sample")
                .requiresMod("spawn")
                .stars(2, 3)
                .weight(7)
                .description(
                        LocalizedText.of(
                                "魔女的魔药实验急需蜗牛的粘痕作为核心原料。请协助收集部分粘痕以推进实验进度。",
                                "The magician's potion experiments urgently require snail mucus as a core ingredient. Help collect some mucus to advance the experiment."))
                .submit("spawn:mucus", 8)
                .rewardCoins(75)
                .rewardFavor("otherworldinn:magician", 70)
                .build();

        register("magic_ghostly_mucus")
                .requiresMod("spawn")
                .stars(3, 4)
                .weight(5)
                .description(
                        LocalizedText.of(
                                "魔女计划研究掺入幻翼膜的苍白粘痕块。请协助制作两块符合要求的材料并交付于她。",
                                "The magician plans to study ghostly mucus infused with phantom membranes. Help craft two pieces of the required material and deliver them to her."))
                .submit("spawn:ghostly_mucus_block", 2)
                .rewardCoins(115)
                .rewardFavor("otherworldinn:magician", 85)
                .build();

        register("butcher_shellfish_order")
                .requiresMod("spawn")
                .stars(1, 2)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "屠夫的水产铺计划新增贝类商品。请协助提供一批活蚌与熟蚌肉以供上架。",
                                "The butcher's seafood counter is adding shellfish to the menu. Help provide a batch of live clams and cooked clam meat for the shelves."))
                .submit("spawn:clam", 12)
                .submit("spawn:cooked_clam", 8)
                .rewardCoins(60)
                .rewardFavor("otherworldinn:butcher", 70)
                .build();

        register("butcher_seafood_menu")
                .requiresMod("spawn")
                .stars(2, 3)
                .weight(7)
                .description(
                        LocalizedText.of(
                                "屠夫计划增设即食海味专区，请提供鲱鱼罐头与金枪鱼三明治以供试销。",
                                "The butcher plans to open a ready-to-eat seafood section. Provide some canned herring and tuna sandwiches for a trial run."))
                .submit("spawn:canned_herring", 4)
                .submit("spawn:tuna_sandwich", 4)
                .rewardCoins(85)
                .rewardFavor("otherworldinn:butcher", 80)
                .build();

        register("forge_blaze_burner_order")
                .requiresMod("create")
                .stars(4, 5)
                .weight(4)
                .description(
                        LocalizedText.of(
                                "铁匠计划对锻造炉进行火力升级。请协助获取两台烈焰人燃烧室以完成改造。",
                                "The blacksmith plans to upgrade the forge's firepower. Help acquire two blaze burners to complete the modification."))
                .submit("create:blaze_burner", 2)
                .rewardCoins(105)
                .rewardFavor("otherworldinn:blacksmith", 53)
                .build();

        register("forge_brass_alloy_order")
                .requiresMod("create")
                .stars(2, 3)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "铁匠准备使用黄铜铸造一批新型机件。请收集并提供所需的黄铜锭与黄铜板。",
                                "The blacksmith is preparing to cast a new batch of brass mechanisms. Collect and provide the required brass ingots and brass sheets."))
                .submit("create:brass_ingot", 8)
                .submit("create:brass_sheet", 8)
                .rewardCoins(55)
                .rewardFavor("otherworldinn:blacksmith", 48)
                .build();

        register("forge_electron_tube_order")
                .requiresMod("create")
                .stars(3, 4)
                .weight(6)
                .description(
                        LocalizedText.of(
                                "铁匠正致力于研发新型机械的核心部件。请协助收集一批电子管以推进该项研究。",
                                "The blacksmith is dedicated to researching the core components of advanced machinery. Help gather a batch of electron tubes to advance this research."))
                .submit("create:electron_tube", 6)
                .rewardCoins(65)
                .rewardFavor("otherworldinn:blacksmith", 50)
                .build();

        register("grocer_belt_order")
                .requiresMod("create")
                .stars(2, 3)
                .weight(7)
                .description(
                        LocalizedText.of(
                                "杂货铺接到了一笔农场主订购传送带的业务。请协助杂货铺筹备相关货源以完成订单。",
                                "The grocer received a bulk order for conveyor belts from a farm owner. Help the grocer prepare the stock to fulfill the order."))
                .submit("create:belt_connector", 12)
                .rewardCoins(68)
                .rewardFavor("otherworldinn:grocer", 43)
                .build();

        register("grocer_funnel_order")
                .requiresMod("create")
                .stars(2, 3)
                .weight(7)
                .description(
                        LocalizedText.of(
                                "杂货铺的五金货架急需补充漏斗库存。请分别筹备一批安山漏斗与黄铜漏斗。",
                                "The grocer's hardware shelf urgently needs a restock of funnels. Prepare a batch of andesite funnels and brass funnels."))
                .submit("create:andesite_funnel", 8)
                .submit("create:brass_funnel", 4)
                .rewardCoins(75)
                .rewardFavor("otherworldinn:grocer", 45)
                .build();

        register("grocer_pipe_supply")
                .requiresMod("create")
                .stars(1, 2)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "杂货铺计划新增铜制管道的销售业务。请提供一批流体管道以丰富货架商品。",
                                "The grocer plans to add copper plumbing to their sales catalog. Provide a batch of fluid pipes to stock the shelves."))
                .submit("create:fluid_pipe", 16)
                .rewardCoins(41)
                .rewardFavor("otherworldinn:grocer", 35)
                .build();

        register("farm_apple_platter_order")
                .requiresMod("kaleidoscope_cookery")
                .stars(1, 2)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "果园迎来了苹果的丰收季，农夫计划制作苹果拼盘。请提供几份成品拼盘作为制作参考。",
                                "The orchard has welcomed a bountiful apple harvest, and the farmer plans to make apple platters. Provide a few finished platters as a reference."))
                .submit("kaleidoscope_cookery:apple_platter", 6)
                .rewardCoins(48)
                .rewardFavor("otherworldinn:farmer", 42)
                .build();

        register("farm_rice_cake_order")
                .requiresMod("kaleidoscope_cookery")
                .stars(1, 2)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "农户们有意学习糯米糕点的制作工艺。请提供一批糯米糕作为观摩与品鉴的样品。",
                                "The farmhands are eager to learn the craft of making sticky rice cakes. Provide a batch of sticky rice cakes for them to observe and taste."))
                .submit("kaleidoscope_cookery:sticky_rice_cake", 12)
                .rewardCoins(45)
                .rewardFavor("otherworldinn:farmer", 40)
                .build();

        register("grocer_dumpling_order")
                .requiresMod("kaleidoscope_cookery")
                .stars(1, 2)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "杂货铺计划引进一批速食饺子。请先提供一屉饺子作为样品，以供品质检验。",
                                "The grocer plans to introduce a batch of quick-cook dumplings. Provide a steamer of dumplings first as a sample for quality inspection."))
                .submit("kaleidoscope_cookery:dumpling", 16)
                .rewardCoins(42)
                .rewardFavor("otherworldinn:grocer", 38)
                .build();

        register("grocer_dongpo_pork_order")
                .requiresMod("kaleidoscope_cookery")
                .stars(2, 3)
                .weight(7)
                .description(
                        LocalizedText.of(
                                "杂货铺的熟食专区计划推出新菜品。请提供几份东坡肉，作为新品试营业的样菜。",
                                "The grocer's deli section plans to launch a new dish. Provide a few portions of Dongpo pork as samples for the trial run."))
                .submit("kaleidoscope_cookery:dongpo_pork", 6)
                .rewardCoins(60)
                .rewardFavor("otherworldinn:grocer", 50)
                .build();

        register("magic_premium_tea_order")
                .requiresMod("kaleidoscope_cookery")
                .stars(2, 3)
                .weight(6)
                .description(
                        LocalizedText.of(
                                "魔女意图品鉴并对比名贵茶种的优劣。请收集上等的铁观音与碧螺春以供其品评。",
                                "The magician intends to taste and compare the qualities of premium teas. Gather top-grade Tieguanyin and Biluochun for her evaluation."))
                .submit("kaleidoscope_cookery:tieguanyin", 4)
                .submit("kaleidoscope_cookery:biluochun", 4)
                .rewardCoins(62)
                .rewardFavor("otherworldinn:magician", 48)
                .build();

        register("farm_melon_juice_order")
                .requiresMod("kaleidoscope_tavern")
                .stars(1, 1)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "炎炎夏日，田间劳作急需解渴饮品。请协助筹备一批清凉的西瓜汁以供应求。",
                                "Thirst-quenching drinks are desperately needed for the hot summer fieldwork. Help prepare a batch of refreshing melon juice to meet the demand."))
                .submit("kaleidoscope_tavern:watermelon_juice", 8)
                .rewardCoins(36)
                .rewardFavor("otherworldinn:farmer", 32)
                .build();

        register("farm_honey_wine_order")
                .requiresMod("kaleidoscope_tavern")
                .stars(1, 2)
                .weight(7)
                .description(
                        LocalizedText.of(
                                "蜂农酿制的蜂蜜酒即将售罄，市场需求旺盛。请提供几瓶优质蜂蜜酒以补充库存。",
                                "The beekeeper's mead is nearly sold out, and demand is high. Provide a few bottles of high-quality mead to restock the inventory."))
                .submit("kaleidoscope_tavern:honey_wine", 6)
                .rewardCoins(46)
                .rewardFavor("otherworldinn:farmer", 40)
                .build();

        register("grocer_champagne_order")
                .requiresMod("kaleidoscope_tavern")
                .stars(2, 3)
                .weight(6)
                .description(
                        LocalizedText.of(
                                "杂货铺正在为即将到来的庆典进行物资储备。请协助提供数瓶香槟以丰富商品种类。",
                                "The grocer is stockpiling supplies for the upcoming festival. Help provide several bottles of champagne to expand the inventory."))
                .submit("kaleidoscope_tavern:champagne", 4)
                .rewardCoins(58)
                .rewardFavor("otherworldinn:grocer", 48)
                .build();

        register("magic_glowflower_brew_order")
                .requiresMod("kaleidoscope_tavern")
                .stars(2, 3)
                .weight(6)
                .description(
                        LocalizedText.of(
                                "魔女听闻世间存在一种会发光的花酿。请寻觅并带回一瓶，以满足她的好奇心。",
                                "The magician heard of a luminous floral brew that exists in this world. Seek it out and bring back a bottle to satisfy her curiosity."))
                .submit("kaleidoscope_tavern:glowflower_brew", 4)
                .rewardCoins(65)
                .rewardFavor("otherworldinn:magician", 50)
                .build();

        register("magic_luminous_bride_order")
                .requiresMod("kaleidoscope_tavern")
                .stars(3, 4)
                .weight(5)
                .description(
                        LocalizedText.of(
                                "魔女计划在宴会上供应两款特制酒水。请筹备光辉新娘与雪母各两瓶，以作宴客之用。",
                                "The magician plans to serve two special beverages at the banquet. Prepare two bottles each of Luminous Bride and Mother Snow for the guests."))
                .submit("kaleidoscope_tavern:luminous_bride", 2)
                .submit("kaleidoscope_tavern:mother_snow", 2)
                .rewardCoins(78)
                .rewardFavor("otherworldinn:magician", 55)
                .build();

        register("town_road_repair")
                .stars(1, 2)
                .weight(10)
                .description(
                        LocalizedText.of(
                                "城镇的部分道路急需维护与修缮。请协助收集并提供维修所需的石材与木材。",
                                "Some of the town's roads are in desperate need of maintenance and repair. Help gather and provide the stone and wood needed for the repairs."))
                .submit("minecraft:cobblestone", 32)
                .submit("minecraft:oak_planks", 24)
                .rewardCoins(50)
                .build();

        register("farm_protection")
                .stars(2, 3)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "农场需要加强安全防护，烦请协助清除周边的敌对生物。",
                                "The farm needs to bolster its defenses. Please help clear out the hostile mobs in the surrounding area."))
                .kill("minecraft:creeper", 4)
                .kill("minecraft:skeleton", 6)
                .kill("minecraft:zombie", 8)
                .rewardCoins(85)
                .rewardFavor("otherworldinn:farmer", 75)
                .build();

        register("forge_masterpiece")
                .stars(5, 5)
                .weight(3)
                .description(
                        LocalizedText.of(
                                "铁匠正筹备打造一件绝世的传世佳作，对材料的品质要求极高。请协助寻觅并提供最顶级的锻造材料。",
                                "The blacksmith is preparing to forge an unparalleled masterpiece, and the material requirements are extremely high. Help seek out and provide the finest forging materials."))
                .submit("minecraft:netherite_ingot", 2)
                .submit("minecraft:diamond_block", 1)
                .submit("minecraft:emerald_block", 1)
                .rewardCoins(300)
                .rewardFavor("otherworldinn:blacksmith", 100)
                .build();

        register("town_emergency_supplies")
                .stars(1, 3)
                .weight(9)
                .description(
                        LocalizedText.of(
                                "城镇现正进行应急物资的战略储备。请协助筹集充足的食物与各类应急用品。",
                                "The town is currently building a strategic reserve of emergency supplies. Help gather ample food and various emergency items."))
                .submit("minecraft:bread", 20)
                .submit("minecraft:golden_apple", 2)
                .rewardCoins(70)
                .build();

        register("grocer_shelf_restock")
                .stars(1, 2)
                .weight(10)
                .description(
                        LocalizedText.of(
                                "杂货铺的木材库存已严重不足。请尽快运送一批常用的木板以完成补货。",
                                "The grocer's timber stock is critically low. Deliver a batch of common planks as soon as possible to restock."))
                .submit("minecraft:oak_planks", 24)
                .submit("minecraft:spruce_planks", 24)
                .rewardCoins(55)
                .rewardFavor("otherworldinn:grocer", 65)
                .build();

        register("grocer_masonry_supply")
                .stars(2, 3)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "杂货铺当前正大量收购优质石材，请协助收集并运送至铺内。",
                                "The grocer is currently buying up high-quality stone in bulk. Help collect and deliver them to the shop."))
                .submit("minecraft:cobblestone", 32)
                .submit("minecraft:diorite", 16)
                .submit("minecraft:andesite", 16)
                .submit("minecraft:granite", 16)
                .rewardCoins(90)
                .rewardFavor("otherworldinn:grocer", 85)
                .build();

        register("grocer_stone_assortment")
                .stars(3, 4)
                .weight(6)
                .description(
                        LocalizedText.of(
                                "某建筑队向杂货铺订购了一套杂石样品。请协助备齐凝灰岩与深板岩等石料以供交付。",
                                "A construction crew ordered a set of mixed stone samples from the grocer. Help gather tuff, deepslate, and other stones for delivery."))
                .submit("minecraft:tuff", 24)
                .submit("minecraft:cobbled_deepslate", 24)
                .submit("minecraft:calcite", 12)
                .rewardCoins(130)
                .rewardFavor("otherworldinn:grocer", 105)
                .build();

        register("adventurers_guild_warden_hunt")
                .stars(5, 5)
                .weight(2)
                .description(
                        LocalizedText.of(
                                "冒险家协会现发布高危委托：请前往深暗之域，并成功击杀一只监守者。",
                                "The Adventurers Guild has issued a high-risk commission: venture into the Deep Dark and slay a Warden."))
                .kill("minecraft:warden", 1)
                .rewardCoins(280)
                .rewardItem("minecraft:echo_shard", 2)
                .build();

        register("adventurers_guild_enderman_patrol")
                .stars(4, 5)
                .weight(4)
                .description(
                        LocalizedText.of(
                                "冒险家协会现发布巡查委托：请清除游荡的末影人，并妥善回收相关样本。",
                                "The Adventurers Guild has issued a patrol commission: please clear out roaming Endermen and carefully recover their samples."))
                .kill("minecraft:enderman", 20)
                .rewardCoins(260)
                .rewardItem("minecraft:ender_pearl", 8)
                .build();

        register("adventurers_guild_nether_suppression")
                .stars(5, 5)
                .weight(3)
                .description(
                        LocalizedText.of(
                                "冒险家协会现发布下界清剿委托：请击杀烈焰人，并同步清除凋灵骷髅威胁。",
                                "The Adventurers Guild has issued a Nether subjugation commission: slay the Blazes and eliminate the Wither Skeleton threat."))
                .kill("minecraft:blaze", 12)
                .kill("minecraft:wither_skeleton", 16)
                .rewardCoins(360)
                .rewardItem("minecraft:blaze_rod", 10)
                .build();

        register("adventurers_guild_raid_breaker")
                .stars(4, 5)
                .weight(3)
                .description(
                        LocalizedText.of(
                                "冒险家协会现发布防务委托：请击杀袭击者与唤魔者，以切实降低村庄所面临的安全威胁。",
                                "The Adventurers Guild has issued a defense commission: eliminate the Raiders and Evokers to secure the village from their looming threat."))
                .kill("minecraft:pillager", 18)
                .kill("minecraft:evoker", 2)
                .rewardCoins(320)
                .rewardItem("minecraft:totem_of_undying", 1)
                .build();

        // ── 屠夫委托 ──

        register("butcher_meat_supply")
                .stars(1, 2)
                .weight(10)
                .description(
                        LocalizedText.of(
                                "屠夫现需采购生肉以补充库存，烦请提供猪肉与牛肉。",
                                "The butcher needs to restock on raw meat. Please provide some pork and beef."))
                .submit("minecraft:porkchop", 16)
                .submit("minecraft:beef", 16)
                .rewardCoins(50)
                .rewardFavor("otherworldinn:butcher", 65)
                .build();

        register("butcher_poultry_order")
                .stars(1, 2)
                .weight(10)
                .description(
                        LocalizedText.of(
                                "屠夫正在大量收购禽类产品。请筹备一批新鲜的鸡肉与鸡蛋并交付至屠夫处。",
                                "The butcher is buying up poultry in bulk. Prepare a batch of fresh chicken and eggs for him."))
                .submit("minecraft:chicken", 20)
                .submit("minecraft:egg", 16)
                .rewardCoins(45)
                .rewardFavor("otherworldinn:butcher", 60)
                .build();

        register("butcher_seafood_order")
                .stars(2, 3)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "屠夫计划拓展水产业务，请提供部分鱼类产品以作市场评估。",
                                "The butcher plans to expand into the aquatic market. Provide some fish products for a market assessment."))
                .submit("minecraft:cod", 20)
                .submit("minecraft:salmon", 20)
                .rewardCoins(60)
                .rewardFavor("otherworldinn:butcher", 70)
                .build();

        register("butcher_leather_order")
                .stars(2, 3)
                .weight(7)
                .description(
                        LocalizedText.of(
                                "屠夫承接了一笔皮革订单，目前原料有所欠缺。请协助筹集充足的皮革与骨粉以完成生产。",
                                "The butcher took on a leather order but lacks raw materials. Help gather enough leather and bone meal to complete production."))
                .submit("minecraft:leather", 16)
                .submit("minecraft:bone", 24)
                .rewardCoins(70)
                .rewardFavor("otherworldinn:butcher", 75)
                .build();

        register("butcher_exotic_hunt")
                .stars(4, 5)
                .weight(4)
                .description(
                        LocalizedText.of(
                                "屠夫正在高价收购罕见的材料。请协助收集一些兔子脚与墨囊并交付于他。",
                                "The butcher is offering a high price for rare materials. Help him gather some rabbit's feet and ink sacs."))
                .submit("minecraft:rabbit_foot", 4)
                .submit("minecraft:ink_sac", 8)
                .submit("minecraft:glow_ink_sac", 2)
                .rewardCoins(110)
                .rewardFavor("otherworldinn:butcher", 85)
                .build();

        // ── 建筑工委托 ──

        register("builder_timber_order")
                .stars(1, 2)
                .weight(10)
                .description(
                        LocalizedText.of(
                                "建筑工目前缺乏木材物资，烦请提供一批原木与木板。",
                                "The builder is currently short on timber. Please deliver a batch of logs and planks."))
                .submit("minecraft:oak_log", 32)
                .submit("minecraft:oak_planks", 32)
                .rewardCoins(50)
                .rewardFavor("otherworldinn:builder", 65)
                .build();

        register("builder_stone_order")
                .stars(2, 3)
                .weight(9)
                .description(
                        LocalizedText.of(
                                "建筑工即将进行地基铺设工程，现需大量石料供应。",
                                "The builder is about to lay a new foundation and requires a massive supply of stone."))
                .submit("minecraft:cobblestone", 48)
                .submit("minecraft:stone", 32)
                .rewardCoins(65)
                .rewardFavor("otherworldinn:builder", 70)
                .build();

        register("builder_sand_gravel_order")
                .stars(1, 2)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "建筑工现需沙子与砾石以配制工程砂浆。",
                                "The builder requires sand and gravel to mix construction mortar."))
                .submit("minecraft:sand", 32)
                .submit("minecraft:gravel", 32)
                .rewardCoins(55)
                .rewardFavor("otherworldinn:builder", 60)
                .build();

        register("builder_decorative_order")
                .stars(3, 4)
                .weight(6)
                .description(
                        LocalizedText.of(
                                "建筑工近期承接了一项精装工程，现需采购砖块与玻璃。",
                                "The builder recently took on a fine interior project and needs to procure bricks and glass."))
                .submit("minecraft:bricks", 32)
                .submit("minecraft:glass", 16)
                .submit("minecraft:terracotta", 16)
                .rewardCoins(100)
                .rewardFavor("otherworldinn:builder", 80)
                .build();

        register("farmer_cow_snapshot")
                .stars(1, 1)
                .weight(6)
                .description(
                        LocalizedText.of(
                                "巡逻队需要补充僵尸活动的影像记录。请前往主世界，拍摄至少一只僵尸的出没画面并提交。",
                                "The patrol squad needs to update its records of zombie activity. Head to the Overworld, capture footage of at least one roaming zombie, and submit it."))
                .photo(PhotoObjectiveRegistry.ZOMBIE_SNAPSHOT_ID.toString())
                .rewardCoins(12)
                .rewardItem("minecraft:diamond_sword", 1)
                .build();

        register("plains_scene_snapshot")
                .stars(1, 1)
                .weight(9)
                .description(
                        LocalizedText.of(
                                "农务记录员需要平原环境的照片以辅助整理本季耕作笔记。请前往平原群系拍摄一张风景照并提交。",
                                "The farm recorder needs photographs of the plains environment to assist in compiling this season's cultivation notes. Head to a plains biome, take a landscape photo, and submit it."))
                .photo(PhotoObjectiveRegistry.PLAINS_SCENE_ID.toString())
                .rewardCoins(18)
                .build();

        register("pasture_cow_snapshot")
                .stars(1, 2)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "牧场当前需要完善牲畜档案。请拍摄一张包含至少一头牛的照片，拍摄时无需刻意清理背景。",
                                "The ranch currently needs to update its livestock records. Take a photograph containing at least one cow; there is no need to clear the background."))
                .photo(PhotoObjectiveRegistry.COW_SNAPSHOT_ID.toString())
                .rewardCoins(22)
                .rewardItem("minecraft:milk_bucket", 1)
                .build();

        register("apiary_bee_snapshot")
                .stars(1, 2)
                .weight(7)
                .description(
                        LocalizedText.of(
                                "园艺师正在筹备一份蜜蜂生态的影像记录。请前往主世界，拍摄至少一只蜜蜂的活动照片并交予她。",
                                "The gardener is putting together an ecological record of bees. Head to the Overworld, capture at least one bee in action on film, and bring it to her."))
                .photo(PhotoObjectiveRegistry.BEE_SNAPSHOT_ID.toString())
                .rewardCoins(24)
                .rewardItem("minecraft:honey_bottle", 2)
                .build();

        register("snowfield_survey_snapshot")
                .stars(2, 3)
                .weight(5)
                .description(
                        LocalizedText.of(
                                "补给队计划先行评估雪原的通行状况。请前往雪原群系拍摄一张风景照片，作为路况参考资料。",
                                "The supply caravan plans to assess the passage conditions of the snowfield in advance. Head to the snowy plains biome and take a landscape photograph as a road condition reference."))
                .photo(PhotoObjectiveRegistry.SNOWY_PLAINS_SCENE_ID.toString())
                .rewardCoins(42)
                .rewardItem("minecraft:leather_boots", 1)
                .build();

        register("nether_ghast_snapshot")
                .stars(3, 4)
                .weight(3)
                .description(
                        LocalizedText.of(
                                "冒险协会急需恶魂的目击素材以完善下界航路档案。请前往下界，拍摄至少一只恶魂的清晰照片。",
                                "The Adventurers Guild urgently needs sighting footage of Ghasts to update the Nether navigation archives. Head to the Nether and take a clear photograph of at least one Ghast."))
                .photo(PhotoObjectiveRegistry.GHAST_SNAPSHOT_ID.toString())
                .rewardCoins(68)
                .rewardItem("minecraft:ghast_tear", 2)
                .build();

        // ── 节日限定委托（仅在对应节日激活期间出现，refreshBoard 的保底槽保证可见）──

        register("spring_flower_decor")
                .festival("spring_festival")
                .stars(2, 3)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "春祭期间街道急需布置花饰。请协助筹备并运送一批春花、粉色花瓣以及花盆。",
                                "The streets are in desperate need of floral decorations for the Spring Festival. Help prepare and deliver a batch of spring flowers, pink petals, and flower pots."))
                .submit("minecraft:pink_petals", 16)
                .submit("minecraft:dandelion", 12)
                .submit("minecraft:flower_pot", 6)
                .rewardCoins(95)
                .rewardFavor("otherworldinn:farmer", 100)
                .build();

        register("spring_bee_census")
                .festival("spring_festival")
                .stars(1, 2)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "养蜂人计划借春祭之际清点蜂群。请协助拍摄一张蜜蜂的照片，并带回几瓶蜂蜜以供验收。",
                                "The beekeeper plans to use the Spring Festival to take a census of the hives. Help by taking a photograph of a bee, and bring back a few bottles of honey for inspection."))
                .photo(PhotoObjectiveRegistry.BEE_SNAPSHOT_ID.toString())
                .submit("minecraft:honey_bottle", 4)
                .rewardCoins(60)
                .rewardItem("minecraft:honey_block", 2)
                .rewardFavor("otherworldinn:farmer", 70)
                .build();

        register("spring_garden_renewal")
                .festival("spring_festival")
                .stars(3, 4)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "镇上计划趁春祭期间翻修庭院。请协助收集骨粉、苔藓与杜鹃花丛等园艺材料。",
                                "The town plans to renovate the courtyards during the Spring Festival. Help gather gardening materials like bone meal, moss, and azalea bushes."))
                .submit("minecraft:bone_meal", 24)
                .submit("minecraft:moss_block", 12)
                .submit("minecraft:azalea", 4)
                .rewardCoins(140)
                .rewardFavor("otherworldinn:farmer", 105)
                .build();

        register("spring_kite_workshop")
                .festival("spring_festival")
                .stars(3, 4)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "孩子们期望在春祭制作纸鸢。请驱逐滋扰的幻翼以收集薄膜，并准备适量的纸张供制作之用。",
                                "The children are hoping to make kites for the Spring Festival. Drive away the bothersome Phantoms to collect their membranes, and prepare enough paper for the crafting."))
                .kill("minecraft:phantom", 3)
                .submit("minecraft:phantom_membrane", 3)
                .submit("minecraft:paper", 12)
                .rewardCoins(160)
                .rewardFavor("otherworldinn:builder", 95)
                .build();

        register("midsummer_night_watch")
                .festival("midsummer_night")
                .stars(2, 3)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "为保障仲夏夜庆典的安全与宁静，请协助清理夜间出没的骷髅与蜘蛛，维护周边秩序。",
                                "To ensure the safety and peace of the Midsummer Night Festival, help clear out the skeletons and spiders that roam at night and maintain order in the area."))
                .kill("minecraft:skeleton", 8)
                .kill("minecraft:spider", 6)
                .rewardCoins(105)
                .rewardItem("minecraft:firework_rocket", 12)
                .build();

        register("midsummer_beach_bonfire")
                .festival("midsummer_night")
                .stars(1, 2)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "篝火晚会计划将场地迁移至海滨。请拍摄一张海洋群系的照片作为选址参考，并准备一些西瓜供晚会使用。",
                                "The bonfire party is planned to be relocated to the seaside. Take a photograph of an ocean biome as a venue reference, and prepare some melons for the party."))
                .photo(PhotoObjectiveRegistry.OCEAN_BIOME_SNAPSHOT_ID.toString())
                .submit("minecraft:melon_slice", 24)
                .rewardCoins(65)
                .rewardFavor("otherworldinn:grocer", 80)
                .build();

        register("midsummer_guest_rush")
                .festival("midsummer_night")
                .stars(2, 3)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "仲夏夜庆典吸引了大量外来旅客。请协助筹备充足的床铺与口粮，以妥善安置涌入的宾客。",
                                "The Midsummer Night Festival has attracted a large influx of travelers. Help prepare plenty of beds and rations to properly accommodate the arriving guests."))
                .submit("minecraft:hay_block", 16)
                .submit("minecraft:torch", 24)
                .submit("minecraft:bread", 16)
                .rewardCoins(110)
                .rewardFavor("otherworldinn:grocer", 90)
                .build();

        register("midsummer_lantern_night")
                .festival("midsummer_night")
                .stars(4, 5)
                .weight(4)
                .description(
                        LocalizedText.of(
                                "园游会的灯彩与烟花布置仍缺少关键材料。请协助收集荧石、紫水晶与火药，以确保庆典顺利进行。",
                                "The garden party's lantern and firework displays are still missing key materials. Help gather glowstone, amethyst, and gunpowder to ensure the celebration goes smoothly."))
                .submit("minecraft:glowstone", 16)
                .submit("minecraft:amethyst_shard", 12)
                .submit("minecraft:gunpowder", 12)
                .rewardCoins(190)
                .rewardFavor("otherworldinn:magician", 100)
                .build();

        register("harvest_crop_parade")
                .festival("harvest_festival")
                .stars(3, 4)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "丰收巡游庆典急需装饰精美的花车。请收集小麦、南瓜与苹果，为花车增添丰收的氛围。",
                                "The Harvest Parade urgently needs beautifully decorated floats. Gather wheat, pumpkins, and apples to add a festive harvest atmosphere to the floats."))
                .submit("minecraft:wheat", 32)
                .submit("minecraft:pumpkin", 8)
                .submit("minecraft:apple", 16)
                .rewardCoins(145)
                .rewardFavor("otherworldinn:farmer", 110)
                .build();

        register("harvest_feast_cooking")
                .festival("harvest_festival")
                .stars(3, 4)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "丰宴的主厨正急需助手协助备菜。请筹备南瓜派、金胡萝卜以及大量熟肉以供盛宴之用。",
                                "The head chef of the Harvest Feast urgently needs prep assistants. Prepare pumpkin pies, golden carrots, and a large quantity of cooked meat for the grand banquet."))
                .submit("minecraft:pumpkin_pie", 6)
                .submit("minecraft:golden_carrot", 12)
                .submit("minecraft:cooked_beef", 16)
                .rewardCoins(170)
                .rewardFavor("otherworldinn:butcher", 110)
                .build();

        register("harvest_cellar_stock")
                .festival("harvest_festival")
                .stars(4, 5)
                .weight(4)
                .description(
                        LocalizedText.of(
                                "为酿造冬日甜酒，需将今年的丰收作物妥善窖藏。请协助筹备木桶、苹果与蜂蜜等酿酒物资。",
                                "To brew the winter sweet wine, this year's bountiful harvest must be properly cellared. Help prepare brewing supplies like wooden barrels, apples, and honey."))
                .submit("minecraft:barrel", 8)
                .submit("minecraft:apple", 32)
                .submit("minecraft:honey_bottle", 12)
                .rewardCoins(220)
                .rewardFavor("otherworldinn:farmer", 120)
                .build();

        register("deep_winter_warm_drive")
                .festival("deep_winter_festival")
                .stars(2, 3)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "隆冬节正在为留宿的旅人筹集御寒物资。请协助收集羊毛、皮革与煤炭，为旅人送去温暖。",
                                "The Deep Winter Festival is gathering winter supplies for lodging travelers. Help collect wool, leather, and coal to bring warmth to the guests."))
                .submit("minecraft:wool", 16)
                .submit("minecraft:leather", 12)
                .submit("minecraft:coal", 24)
                .rewardCoins(110)
                .rewardFavor("otherworldinn:builder", 95)
                .build();

        register("deep_winter_snow_survey")
                .festival("deep_winter_festival")
                .stars(2, 3)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "补给队计划于节前勘察雪原路况。请前往雪原拍摄一张环境照片作为参考，并顺带收集一些雪球以备不时之需。",
                                "The supply crew plans to scout the snowfield roads before the festival. Take a photo of the environment for reference, and gather some snowballs just in case."))
                .photo(PhotoObjectiveRegistry.SNOWY_PLAINS_SCENE_ID.toString())
                .submit("minecraft:snowball", 16)
                .rewardCoins(75)
                .rewardItem("minecraft:campfire", 2)
                .rewardFavor("otherworldinn:grocer", 60)
                .build();

        register("deep_winter_grand_feast")
                .festival("deep_winter_festival")
                .stars(4, 5)
                .weight(4)
                .description(
                        LocalizedText.of(
                                "守岁长宴将持续至新年拂晓，亟需补充餐饮供应。请筹备充足的热食与甜浆果热饮以满足宾客所需。",
                                "The Vigil Feast will last until the dawn of the new year, and catering supplies are urgently needed. Prepare plenty of hot food and warm sweet berry drinks for the guests."))
                .submit("minecraft:baked_potato", 32)
                .submit("minecraft:cooked_mutton", 16)
                .submit("minecraft:sweet_berries", 24)
                .rewardCoins(210)
                .rewardFavor("otherworldinn:grocer", 110)
                .rewardItem("minecraft:firework_rocket", 16)
                .build();

        register("deep_winter_ice_carnival")
                .festival("deep_winter_festival")
                .stars(3, 4)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "冰雕嘉年华已正式启动，目前急需建筑材料。请收集冰、雪块，以及少量用于压轴雕塑的浮冰。",
                                "The Ice Sculpture Carnival has officially begun, and building materials are urgently needed. Gather ice, snow blocks, and a small amount of packed ice for the grand finale sculpture."))
                .submit("minecraft:ice", 32)
                .submit("minecraft:snow_block", 16)
                .submit("minecraft:packed_ice", 4)
                .rewardCoins(165)
                .rewardFavor("otherworldinn:builder", 105)
                .build();

        register(FishingCommissionGenerator.TEMPLATE_ID)
                .stars(1, 5)
                .weight(7)
                .description(
                        LocalizedText.of(
                                "渔夫现正悬赏收购一条特定的新鲜鱼类。请根据委托要求捕获目标鱼并带回，以完成此项垂钓任务。",
                                "The fisherman has posted a bounty for a specific fresh fish. Catch the target fish according to the request and bring it back to complete this angling commission."))
                .build();
    }

    private CommissionRegistry() {}

    public static Builder register(String id) {
        return new Builder(id);
    }

    public static List<CommissionTemplate> allTemplates() {
        return Collections.unmodifiableList(TEMPLATES);
    }

    public static CommissionTemplate pickRandomTemplate(
            RandomSource random, List<String> excludedTemplateIds) {
        List<CommissionTemplate> pool = buildTemplatePool(excludedTemplateIds, Integer.MAX_VALUE, false);
        if (pool.isEmpty()) {
            pool = fallbackTemplates();
        }
        return pickFromPool(random, pool);
    }

    public static CommissionTemplate pickRandomTemplate(
            RandomSource random,
            List<String> excludedTemplateIds,
            int allowedMaxStars,
            boolean restrictByStars) {
        List<CommissionTemplate> pool =
                buildTemplatePool(excludedTemplateIds, allowedMaxStars, restrictByStars);
        if (pool.isEmpty()) {
            pool = buildTemplatePool(excludedTemplateIds, Integer.MAX_VALUE, false);
        }
        if (pool.isEmpty()) {
            pool = fallbackTemplates();
        }
        return pickFromPool(random, pool);
    }

    private static List<CommissionTemplate> buildTemplatePool(
            List<String> excludedTemplateIds, int allowedMaxStars, boolean restrictByStars) {
        List<CommissionTemplate> pool = new ArrayList<>();
        for (CommissionTemplate template : TEMPLATES) {
            if (template.festivalId() != null) {
                // 节日限定模板只在对应节日激活期间经 pickFestivalTemplate 进入保底槽
                continue;
            }
            if (!isModLoaded(template)) {
                continue;
            }
            if (excludedTemplateIds.contains(template.id())) {
                continue;
            }
            if (restrictByStars && template.maxStars() > allowedMaxStars) {
                continue;
            }
            pool.add(template);
        }
        return pool;
    }

    /** 从指定节日的限定模板池中抽取（豁免星级递增过滤），无可用模板返回 null */
    public static CommissionTemplate pickFestivalTemplate(
            RandomSource random, String festivalId, List<String> excludedTemplateIds) {
        List<CommissionTemplate> pool = new ArrayList<>();
        for (CommissionTemplate template : TEMPLATES) {
            if (!festivalId.equals(template.festivalId())) {
                continue;
            }
            if (!isModLoaded(template)) {
                continue;
            }
            if (excludedTemplateIds.contains(template.id())) {
                continue;
            }
            pool.add(template);
        }
        return pickFromPool(random, pool);
    }

    /** 模组门控：requiredModId 非空且对应模组未加载时，模板不进入任何刷新池 */
    private static boolean isModLoaded(CommissionTemplate template) {
        return template.requiredModId() == null
                || ModList.get().isLoaded(template.requiredModId());
    }

    /** 兜底池：剔除节日限定与模组门控模板后的全量集合（保证兜底路径不绕过过滤语义） */
    private static List<CommissionTemplate> fallbackTemplates() {
        List<CommissionTemplate> pool = new ArrayList<>();
        for (CommissionTemplate template : TEMPLATES) {
            if (template.festivalId() != null || !isModLoaded(template)) {
                continue;
            }
            pool.add(template);
        }
        return pool;
    }

    private static CommissionTemplate pickFromPool(RandomSource random, List<CommissionTemplate> pool) {
        if (pool.isEmpty()) {
            return null;
        }
        int totalWeight = 0;
        for (CommissionTemplate template : pool) {
            totalWeight += Math.max(1, template.weight());
        }
        int roll = random.nextInt(Math.max(1, totalWeight));
        int acc = 0;
        for (CommissionTemplate template : pool) {
            acc += Math.max(1, template.weight());
            if (roll < acc) {
                return template;
            }
        }
        return pool.get(random.nextInt(pool.size()));
    }

    public record CommissionTemplate(
            String id,
            int minStars,
            int maxStars,
            int weight,
            LocalizedText description,
            List<CommissionEntry.ItemRequirement> submitRequirements,
            List<CommissionEntry.KillRequirement> killRequirements,
            List<CommissionEntry.PhotoRequirement> photoRequirements,
            List<CommissionEntry.ItemReward> itemRewards,
            int coinReward,
            List<CommissionEntry.NpcFavorReward> npcFavorRewards,
            @Nullable String festivalId,
            @Nullable String requiredModId) {
        public String descriptionKey() {
            return "commission.otherworldinn.description." + id;
        }
    }

    public static final class Builder {
        private final String id;
        private int minStars = 1;
        private int maxStars = 5;
        private int weight = 1;
        private LocalizedText description = LocalizedText.of("暂无描述", "No description");
        private final List<CommissionEntry.ItemRequirement> submitRequirements = new ArrayList<>();
        private final List<CommissionEntry.KillRequirement> killRequirements = new ArrayList<>();
        private final List<CommissionEntry.PhotoRequirement> photoRequirements = new ArrayList<>();
        private final List<CommissionEntry.ItemReward> itemRewards = new ArrayList<>();
        private int coinReward = 0;
        private final List<CommissionEntry.NpcFavorReward> npcFavorRewards = new ArrayList<>();
        @Nullable
        private String festivalId;
        @Nullable
        private String requiredModId;

        private Builder(String id) {
            this.id = id;
        }

        public Builder stars(int minStars, int maxStars) {
            this.minStars = Math.max(1, Math.min(5, minStars));
            this.maxStars = Math.max(this.minStars, Math.max(1, Math.min(5, maxStars)));
            return this;
        }

        public Builder weight(int weight) {
            this.weight = Math.max(1, weight);
            return this;
        }

        public Builder description(LocalizedText description) {
            if (description != null) {
                this.description = description;
            }
            return this;
        }

        public Builder submit(String itemId, int count) {
            this.submitRequirements.add(new CommissionEntry.ItemRequirement(itemId, count));
            return this;
        }

        public Builder submit(String itemId, int count, CompoundTag nbt) {
            this.submitRequirements.add(new CommissionEntry.ItemRequirement(itemId, count, nbt));
            return this;
        }

        public Builder kill(String entityTypeId, int count) {
            this.killRequirements.add(new CommissionEntry.KillRequirement(entityTypeId, count));
            return this;
        }

        public Builder photo(String objectiveId) {
            this.photoRequirements.add(new CommissionEntry.PhotoRequirement(objectiveId));
            return this;
        }

        public Builder rewardItem(String itemId, int count) {
            this.itemRewards.add(new CommissionEntry.ItemReward(itemId, count));
            return this;
        }

        public Builder rewardCoins(int coinReward) {
            this.coinReward = Math.max(0, coinReward);
            return this;
        }

        public Builder rewardFavor(String npcEntityTypeId, int favorProgress) {
            this.npcFavorRewards.add(
                    new CommissionEntry.NpcFavorReward(npcEntityTypeId, favorProgress));
            return this;
        }

        /** 绑定为节日限定委托：仅在对应节日激活期间出现于保底槽 */
        public Builder festival(String festivalId) {
            this.festivalId = festivalId == null || festivalId.isBlank() ? null : festivalId;
            return this;
        }

        /** 模组门控：modid 未加载时模板整体不出池，避免联动委托因缺少其它模组内容而无法完成 */
        public Builder requiresMod(String modId) {
            this.requiredModId = modId == null || modId.isBlank() ? null : modId;
            return this;
        }

        public CommissionTemplate build() {
            CommissionTemplate template =
                    new CommissionTemplate(
                            id,
                            minStars,
                            maxStars,
                            weight,
                            description,
                            List.copyOf(submitRequirements),
                            List.copyOf(killRequirements),
                            List.copyOf(photoRequirements),
                            List.copyOf(itemRewards),
                            coinReward,
                            List.copyOf(npcFavorRewards),
                            festivalId,
                            requiredModId);
            TEMPLATES.add(template);
            return template;
        }
    }
}
