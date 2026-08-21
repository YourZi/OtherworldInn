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
                                "农场的粮食储备不够了，请带一点过来。",
                                "The farm is short on staple supplies. Please restock soon."))
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
                                "铁匠铺需要备用金属件，请先送一批材料过来。",
                                "The forge needs spare metal parts. Deliver a batch of materials."))
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
                                "最近城镇外夜间不是很太平，请清理游荡的僵尸。",
                                "Nights beyond town are unsafe. Clear out wandering zombies."))
                .kill("minecraft:zombie", 10)
                .rewardCoins(80)
                .build();

        register("road_patrol_mixed")
                .stars(3, 3)
                .weight(9)
                .description(
                        LocalizedText.of(
                                "城镇的巡逻队请求支援：沿路清理骷髅并补给食物和工具。",
                                "Patrol requests support: clear skeletons and deliver field rations and tools."))
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
                                "魔女需要一些稀有的魔法研究材料，帮忙收集一下。",
                                "The magician needs rare materials for magical studies. Help gather them."))
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
                                "农场需要额外的动物饲料，带些小麦和胡萝卜过来。",
                                "The farm needs extra animal feed. Bring wheat and carrots."))
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
                                "城外有需要修理的设施，需要材料和帮手。",
                                "The forge has backlogged tool repairs. Needs materials and assistance."))
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
                                "魔女的药水库存不足了，需要一些基础药材补充。",
                                "The magician's potion supplies are low. Needs basic ingredients."))
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
                                "铁匠接到一批武器订单，需要高级金属材料。",
                                "The blacksmith received weapon orders. Needs premium metals."))
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
                                "魔女有一个重要仪式要做，需要极其稀有的材料。",
                                "The magician prepares for an important ritual. Needs extremely rare materials."))
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
                                "仓库里出现了蜘蛛，需要清理。",
                                "Spiders have infested the town warehouse. Needs cleaning and drying."))
                .kill("minecraft:spider", 8)
                .submit("minecraft:string", 12)
                .rewardCoins(65)
                .build();

        register("farm_seed_supply")
                .stars(1, 2)
                .weight(11)
                .description(
                        LocalizedText.of(
                                "农场需要各种作物种子进行轮作种植。",
                                "The farm needs various crop seeds for rotation planting."))
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
                                "铁匠铺的燃料快用完了，需要补充煤炭。",
                                "The forge is running low on fuel. Needs coal replenishment."))
                .submit("minecraft:coal", 32)
                .rewardCoins(50)
                .rewardFavor("otherworldinn:blacksmith", 55)
                .build();

        register("magic_library_organize")
                .stars(2, 3)
                .weight(7)
                .description(
                        LocalizedText.of(
                                "魔女的图书馆需要整理，顺便收集一些古籍。",
                                "The magician's library needs organizing. Also collect some ancient texts."))
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
                                "城镇卫队需要一批新护甲，铁匠需要材料。",
                                "The town guard needs new armor. Blacksmith requires materials."))
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
                                "秋收祭的庆典餐桌还缺些点心，送来蛋糕、曲奇和南瓜派。",
                                "The Harvest Festival table still needs sweets. Deliver cakes, cookies, and pumpkin pies."))
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
                                "杂货铺想引进一批异域食材试卖，带些青团和炸春卷来看看。",
                                "The grocer wants to trial some exotic fare. Bring qingtuan and fried spring rolls."))
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
                                "铁匠想试试机械动力的机件，收一批齿轮和传动杆。",
                                "The blacksmith wants to try kinetic mechanisms. Bring cogwheels and shafts."))
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
                                "铁匠想拆开精密构件研究其结构，带两枚来交换黄铜。",
                                "The blacksmith wants to take precision mechanisms apart. Trade two for brass."))
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
                                "农夫想给雇工们添几顶草帽，带些草帽和花饰草帽来。",
                                "The farmer wants sun hats for the hired hands. Bring straw hats and a flowered one."))
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
                                "杂货铺要进一批酒水，带些梅酒和冰酒来试试行情。",
                                "The grocer is stocking drinks. Bring plum wine and ice wine to test the market."))
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
                                "魔女迷恋上了异国茶饮，收集一些花茶和大麦茶来。",
                                "The magician has taken to exotic teas. Gather flower tea and barley tea."))
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
                                "建筑工要给铺面做铜壳装饰，带几个铜机壳过来。",
                                "The builder is cladding a storefront in copper. Bring some copper casings."))
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
                                "珊瑚礁间游动着色彩各异的海马，帮渔夫拍张照片回来。",
                                "Colorful seahorses drift among the coral reefs. Take a photo for the fisherman."))
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
                                "海里的鲱鱼开始成群游动了，帮渔夫拍摄一张三条鲱鱼同框的照片。",
                                "The herring have started schooling. Catch three of them in a single frame for the fisherman."))
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
                                "温和的海牛栖息在海草床群系，替渔夫拍一张它们与其栖息地的合影。",
                                "Gentle sea cows dwell in the seagrass meadows. Photograph one together with its habitat."))
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
                                "渡渡鸟岛屿上生活着不会飞的渡渡鸟，带回一张它们在岛上的照片。",
                                "Flightless dodos roam their island. Bring back a photo of one on its home turf."))
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
                                "渔夫说深水中总是看见一点幽幽的亮光，去拍清楚那是什么。",
                                "The fisherman keeps spotting a faint glow in the deep. Go find out what it is on film."))
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
                                "菜园边上常能捡到蜗牛壳，帮农夫拍一张活蜗牛的照片。",
                                "Empty snail shells turn up by the garden beds. Photograph a living snail for the farmer."))
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
                                "开阔的草地上住着会囤粮的仓鼠，帮农夫拍一张它们的照片。",
                                "Hamsters hoard seeds in the open grasslands. Photograph one for the farmer."))
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
                                "蚂蚁在蚁丘间进进出出地搬运，帮农夫拍一张它们在蚂蚁之园劳作的照片。",
                                "Ants march to and from their hills, hauling supplies. Photograph them at work in the ant gardens."))
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
                                "魔女听闻海里有种会藏进壳里的聪明生物，拍一张章鱼的照片给她。",
                                "The magician has heard of a clever creature that hides in shells. Bring her a photo of an octopus."))
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
                                "魔女对会自己攻击怪物的涂蜡构装体很感兴趣，拍一张滞儡的照片。",
                                "The magician is curious about the waxed constructs that fight monsters on their own. Photograph a stranded."))
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
                                "火山岛屿上游荡着由熔岩组成的守卫，替魔女远远拍一张，注意安全。",
                                "Guards of lava roam the volcanic island. Keep your distance and photograph a firekeeper for the magician."))
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
                                "退潮后的海岸上螃蟹横行，帮屠夫拍一张海岸蟹的照片。",
                                "Crabs scuttle along the shore at low tide. Photograph a coastal crab for the butcher."))
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
                                "海里游着个头很大的金枪鱼，屠夫想看看它们的样貌，拍一张回来。",
                                "Great tuna cruise the open sea. The butcher wants to see one — bring back a photo."))
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
                                "冷水海域藏着会吐冰泡的蜘蛛蟹，替屠夫拍一张它们的水下照片。",
                                "Spider crabs lurk in cold waters, spitting frozen bubbles. Photograph one underwater for the butcher."))
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
                                "农夫想引进一批新作物，带些向日葵种子和椰枣来试种。",
                                "The farmer wants to trial new crops. Bring sunflower seeds and dates to plant."))
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
                                "农场要修一条壳瓦小径再围一圈花坛，带些蜗牛壳瓦和向日葵来。",
                                "The farm wants a shell-tile path and a flower bed. Bring snail shell tiles and sunflowers."))
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
                                "魔女的药水实验需要蜗牛的粘痕作原料，收集一些来。",
                                "The magician's potion experiments need snail mucus as an ingredient. Collect some."))
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
                                "魔女想研究掺了幻翼膜的苍白粘痕块，制作两块送来。",
                                "The magician wants to study ghostly mucus infused with phantom membrane. Craft two blocks and deliver."))
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
                                "屠夫的水产柜台要上贝类，带些活蚌和熟蚌肉来。",
                                "The butcher's seafood counter needs shellfish. Bring live clams and cooked clam meat."))
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
                                "屠夫想加一栏即食海味，带些鲱鱼罐头和金枪鱼三明治来试卖。",
                                "The butcher wants a ready-to-eat shelf. Bring canned herring and tuna sandwiches to trial."))
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
                                "铁匠的锻造炉要升级火力，需要两台烈焰人燃烧室。",
                                "The forge needs more heat. Deliver two blaze burners."))
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
                                "铁匠要用黄铜打造一批新机件，带些黄铜锭和黄铜板来。",
                                "The blacksmith is crafting new brass fittings. Bring brass ingots and sheets."))
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
                                "铁匠在研究新式机械的心脏，收一批电子管。",
                                "The blacksmith is studying the heart of newer machines. Bring a batch of electron tubes."))
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
                                "有农场主订购了一批传送带，帮杂货铺备货。",
                                "A farmer ordered a bulk set of belts. Help the grocer fill the order."))
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
                                "杂货铺的五金货架要补漏斗，安山漏斗和黄铜漏斗各来一批。",
                                "The hardware shelf needs funnels. Bring andesite funnels and a batch of brass ones."))
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
                                "杂货铺要进一批铜管道卖，带些流体管道来。",
                                "The grocer is stocking copper plumbing. Bring a bundle of fluid pipes."))
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
                                "果园的苹果丰收了，农夫想做几份苹果拼盘，先带些成品来参考。",
                                "The orchard had a bumper crop. Bring some apple platters for reference."))
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
                                "农户们想学做糯米糕点，带一批糯米糕来当样品。",
                                "The farmhands want to learn sticky rice cakes. Bring a batch as samples."))
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
                                "杂货铺要进一批速食饺子，带一屉来验货。",
                                "The grocer is stocking quick dumplings. Bring a tray for inspection."))
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
                                "杂货铺的熟食柜要上新，带几份东坡肉来试卖。",
                                "The deli counter is adding new dishes. Bring some Dongpo pork to trial."))
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
                                "魔女喝惯了花茶，想再比一比铁观音和碧螺春的高下。",
                                "Used to flower teas, the magician wants to compare tieguanyin against biluochun."))
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
                                "夏天的田边要备些解渴的西瓜汁，带一批来。",
                                "Summer fields need something to drink. Bring a batch of watermelon juice."))
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
                                "蜂农酿的蜂蜜酒快卖断货了，带几瓶来补货。",
                                "The apiary's honey wine is nearly sold out. Bring a few bottles to restock."))
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
                                "杂货铺要为庆典备货，先收几瓶香槟。",
                                "The grocer is stocking up for a celebration. Bring a few bottles of champagne."))
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
                                "魔女听说有一种会发光的花酿，带一瓶来给她看看。",
                                "The magician heard of a brew that glows. Bring her a bottle to see."))
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
                                "魔女想在宴会上摆两款特别的酒，光辉新娘和雪母各来两瓶。",
                                "The magician wants two special drinks for a banquet. Bring luminous bride and mother snow, two each."))
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
                                "城镇道路需要维修，收集些石材和木材。",
                                "Town roads need repair. Collect some stone and wood."))
                .submit("minecraft:cobblestone", 32)
                .submit("minecraft:oak_planks", 24)
                .rewardCoins(50)
                .build();

        register("farm_protection")
                .stars(2, 3)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "农场需要加强防护，清理周围的敌对生物。",
                                "The farm needs better protection. Clear surrounding hostile mobs."))
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
                                "铁匠要打造一件传世之作，需要最顶级的材料。",
                                "The blacksmith aims to create a masterpiece. Needs the finest materials."))
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
                                "城镇储备应急物资，需要食物和应急用品。",
                                "Town stockpiles emergency supplies. Needs food and medical items."))
                .submit("minecraft:bread", 20)
                .submit("minecraft:golden_apple", 2)
                .rewardCoins(70)
                .build();

        register("grocer_shelf_restock")
                .stars(1, 2)
                .weight(10)
                .description(
                        LocalizedText.of(
                                "杂货铺木材见底了，先送一批常用木板过来补货。",
                                "The grocer is running low on lumber. Deliver common planks for shelf restock."))
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
                                "杂货铺收一批石材。",
                                "The grocer is expanding the masonry aisle. Bring cobblestone, diorite, andesite, and granite."))
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
                                "有建筑队要一套杂石样品，帮杂货铺备齐凝灰岩和深板岩石料。",
                                "A building crew requested mixed stone samples. Help the grocer prepare tuff and deepslate stock."))
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
                                "冒险家协会发布高危委托：前往深暗之域并击杀 1 只监守者。",
                                "Adventurers Guild high-risk request: enter the Deep Dark and kill 1 Warden."))
                .kill("minecraft:warden", 1)
                .rewardCoins(280)
                .rewardItem("minecraft:echo_shard", 2)
                .build();

        register("adventurers_guild_enderman_patrol")
                .stars(4, 5)
                .weight(4)
                .description(
                        LocalizedText.of(
                                "冒险家协会发布巡查委托：清理游荡末影人并回收样本。",
                                "Adventurers Guild patrol request: clear roaming Endermen and recover samples."))
                .kill("minecraft:enderman", 20)
                .rewardCoins(260)
                .rewardItem("minecraft:ender_pearl", 8)
                .build();

        register("adventurers_guild_nether_suppression")
                .stars(5, 5)
                .weight(3)
                .description(
                        LocalizedText.of(
                                "冒险家协会发布下界清剿委托：击杀烈焰人并顺带清理凋灵骷髅。",
                                "Adventurers Guild Nether operation: defeat Blazes and clear Wither Skeletons."))
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
                                "冒险家协会发布防务委托：击杀袭击者与唤魔者，降低村庄威胁。",
                                "Adventurers Guild defense request: eliminate Raiders and Evokers to reduce village threats."))
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
                                "屠夫收一批生肉补货，带些猪肉和牛肉过来。",
                                "The butcher needs raw meat to restock. Bring porkchops and beef."))
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
                                "屠夫收一批禽肉，带些鸡肉和鸡蛋过来。",
                                "The butcher needs poultry. Bring chicken and eggs."))
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
                                "屠夫想拓展水产生意，带些鱼过来看看反应。",
                                "The butcher wants to try seafood. Bring cod and salmon for a trial."))
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
                                "屠夫接了一批皮革，需要帮忙凑足皮革和骨粉原料。",
                                "The butcher took a leather commission. Help gather leather and bones."))
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
                                "屠夫想要些稀罕货，弄点兔子脚和墨囊来。",
                                "The butcher wants exotic goods. Fetch some rabbit feet and ink sacs."))
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
                                "建筑工缺木材，送一批原木和木板过来。",
                                "The builder is short on timber. Deliver logs and planks."))
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
                                "建筑工有地基要打，需要大量石料。",
                                "The builder needs stone for a foundation. Bring cobblestone and stone."))
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
                                "建筑工需要沙子和砾石配砂浆。",
                                "The builder needs sand and gravel for mortar mixing."))
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
                                "建筑工承接了一栋精装修，需要砖块和玻璃。",
                                "The builder is working on a fine interior. Needs bricks and glass."))
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
                                "巡逻队需要一些僵尸出没的画面素材来补充记录。请在主世界拍到至少一只僵尸。",
                                "The patrol needs footage of zombie sightings for its records. Photograph at least one zombie in the Overworld."))
                .photo(PhotoObjectiveRegistry.ZOMBIE_SNAPSHOT_ID.toString())
                .rewardCoins(12)
                .rewardItem("minecraft:diamond_sword", 1)
                .build();

        register("plains_scene_snapshot")
                .stars(1, 1)
                .weight(9)
                .description(
                        LocalizedText.of(
                                "农务记录员想补一张平原环境照，方便整理本季的耕作笔记。请拍一张平原群系的景色。",
                                "The farm recorder needs a plains landscape for this season's cultivation notes. Take a photograph in a plains biome."))
                .photo(PhotoObjectiveRegistry.PLAINS_SCENE_ID.toString())
                .rewardCoins(18)
                .build();

        register("pasture_cow_snapshot")
                .stars(1, 2)
                .weight(8)
                .description(
                        LocalizedText.of(
                                "牧场要补一份牲畜档案，拍到至少一头牛就行，不必特意清场。",
                                "The ranch needs a livestock archive entry. Photograph at least one cow; no need to clear the scene."))
                .photo(PhotoObjectiveRegistry.COW_SNAPSHOT_ID.toString())
                .rewardCoins(22)
                .rewardItem("minecraft:milk_bucket", 1)
                .build();

        register("apiary_bee_snapshot")
                .stars(1, 2)
                .weight(7)
                .description(
                        LocalizedText.of(
                                "园艺师想收集一张蜜蜂活动的照片，替她在主世界拍到至少一只蜜蜂。",
                                "The gardener wants a photograph of bee activity. Capture at least one bee in the Overworld."))
                .photo(PhotoObjectiveRegistry.BEE_SNAPSHOT_ID.toString())
                .rewardCoins(24)
                .rewardItem("minecraft:honey_bottle", 2)
                .build();

        register("snowfield_survey_snapshot")
                .stars(2, 3)
                .weight(5)
                .description(
                        LocalizedText.of(
                                "补给队想先看看雪原的路况，带回一张雪原群系的景色照片。",
                                "The supply caravan wants an early look at the route conditions. Bring back a photograph from a snowy plains biome."))
                .photo(PhotoObjectiveRegistry.SNOWY_PLAINS_SCENE_ID.toString())
                .rewardCoins(42)
                .rewardItem("minecraft:leather_boots", 1)
                .build();

        register("nether_ghast_snapshot")
                .stars(3, 4)
                .weight(3)
                .description(
                        LocalizedText.of(
                                "冒险协会需要恶魂目击素材来补充下界航路档案。请在下界拍到至少一只恶魂。",
                                "The Adventurers Guild needs ghast sighting footage for its Nether route archive. Photograph at least one ghast in the Nether."))
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
                                "春祭的街道要布置花饰，请送来一批春花、粉色花瓣和花盆。",
                                "The Spring Festival streets need floral decor. Deliver spring flowers, pink petals, and flower pots."))
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
                                "养蜂人想趁着春祭清点蜂群，替他拍到一只蜜蜂，再带几瓶蜂蜜回来。",
                                "The beekeeper is counting hives during the festival. Photograph a bee and bring back some honey."))
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
                                "趁着春祭把镇上的庭院翻修一遍，需要骨粉、苔藓和杜鹃花丛。",
                                "The town courtyard needs a spring renewal. Bring bone meal, moss, and azalea bushes."))
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
                                "孩子们想做春祭的纸鸢。清理扰人的幻翼，取回薄膜再备些纸来。",
                                "The children want festival kites. Clear the bothersome phantoms, gather their membranes, and bring paper."))
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
                                "仲夏夜的庆典需要安宁，清理夜间出没的骷髅和蜘蛛。",
                                "The midsummer celebration needs peace. Clear skeletons and spiders stalking the night."))
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
                                "篝火晚会想换个海边的场地，先拍一张海洋群系的照片，再带些西瓜来。",
                                "The bonfire party wants a beach venue. Photograph an ocean scene and bring watermelons."))
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
                                "仲夏夜的庆典引来了大批旅客，为涌入的客人们备好床铺和口粮。",
                                "The celebration draws crowds of travelers. Prepare bedding and rations for the incoming guests."))
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
                                "园游会的灯彩与烟花还差一批材料：荧石、紫水晶和火药。",
                                "The garden lantern night still needs materials: glowstone, amethyst, and gunpowder."))
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
                                "丰收巡游需要满载的花车，收集小麦、南瓜和苹果来装点。",
                                "The harvest parade needs full carts. Gather wheat, pumpkins, and apples for the floats."))
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
                                "丰宴的主厨需要人手备菜：南瓜派、金胡萝卜和一大批熟肉。",
                                "The feast chef needs a prep crew: pumpkin pies, golden carrots, and a batch of cooked beef."))
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
                                "把今年的收成封进地窖酿成冬日的甜酒，需要木桶、苹果和蜂蜜。",
                                "Seal this year's harvest into the cellar for winter cider. Bring barrels, apples, and honey."))
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
                                "隆冬节为留宿的旅人募集御寒物资，需要羊毛、皮革和煤炭。",
                                "The Deep Winter drive collects warm supplies for lodging guests. Bring wool, leather, and coal."))
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
                                "补给队想在节前确认雪原路况，带回一张雪原照片，顺便堆些雪球备用。",
                                "The supply crew wants a road check before the festival. Bring a snowy plains photo and some snowballs."))
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
                                "守岁长宴要一直开到新年天亮，备足热食和甜浆果热饮。",
                                "The vigil feast runs until dawn of the new year. Stock up on hot food and sweet berry drinks."))
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
                                "冰雕嘉年华开工了，需要冰、雪块和一点浮冰做压轴雕塑。",
                                "The ice sculpture carnival begins. Bring ice, snow blocks, and some packed ice for the centerpiece."))
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
                                "渔夫正在收购一条指定的鲜鱼。带回委托要求的目标鱼即可完成这份钓鱼委托。",
                                "The fisherman is buying a specific fresh catch. Bring back the requested fish to finish this fishing commission."))
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

        /**
         * 模组门控：仅当指定 modid 已加载时，模板才有机会被刷出到委托板。
         * 用于需求/奖励引用其它模组内容（物品、实体、拍照目标等）的联动委托，
         * 模组缺失时模板整体不出池，避免刷出无法完成的委托。
         */
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
