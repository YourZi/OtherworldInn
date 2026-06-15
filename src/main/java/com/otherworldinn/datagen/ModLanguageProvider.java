package com.otherworldinn.datagen;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.foundation.BlockDataGenInfo;
import com.otherworldinn.foundation.ItemDataGenInfo;
import com.otherworldinn.init.ModBlocks;
import com.otherworldinn.init.ModEntities;
import com.otherworldinn.init.ModItems;
import com.otherworldinn.world.commission.CommissionRegistry;
import com.otherworldinn.world.dialogue.DialogueDefinition;
import com.otherworldinn.world.dialogue.DialogueNodeDef;
import com.otherworldinn.world.dialogue.DialogueOptionDef;
import com.otherworldinn.world.dialogue.DialogueRegistry;
import com.otherworldinn.world.inn.facility.FacilityRegistry;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * 语言文件生成器
 *
 * <p>负责生成 en_us.json 和 zh_cn.json 语言文件。 根据传入的 locale 参数决定生成哪种语言。
 */
public class ModLanguageProvider extends LanguageProvider {
    private final String locale;

    public ModLanguageProvider(PackOutput output, String locale) {
        super(output, OtherworldInn.MODID, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        addManualTranslations();
        addGeneratedTranslations();
    }

    private void addManualTranslations() {
        entry("itemGroup.otherworldinn").zh("旅社物语").en("Otherworld Inn");
        entry("itemGroup.otherworldinn.expedition").zh("旅社物语 · 远征").en("Otherworld Inn · Expedition");
        entry("gamerule.enableSponsorGuest").zh("启用赞助旅客").en("Enable Sponsor Guest");

        entry("key.categories.otherworldinn").zh("旅社物语").en("Otherworld Inn");

        entry("key.otherworldinn.map_mode").zh("地图视图").en("Map View");
        entry("skill.fishing").zh("钓鱼").en("Fishing");
        entry("skill.magic").zh("钓鱼").en("Fishing");

        // 地图点名称
        entry("map_point.otherworldinn.inn").zh("旅社").en("The Inn");
        entry("map_point.otherworldinn.blacksmith").zh("铁匠铺").en("Blacksmith");
        entry("map_point.otherworldinn.magician_workshop").zh("魔女工坊").en("Magician Workshop");
        entry("map_point.otherworldinn.market").zh("集市").en("Market");
        entry("map_point.otherworldinn.town_gate").zh("城镇大门").en("Town Gate");
        entry("map_point.otherworldinn.locked").zh("未解锁").en("Locked");
        entry("map_point.otherworldinn.cant_teleport").zh("无法传送至").en("Cannot teleport to");
        entry("advancements.otherworldinn.root.title").zh("旅社物语").en("Otherworld Inn");
        entry("advancements.otherworldinn.root.description")
                .zh("获得旅社钥匙，开启你的经营之旅")
                .en("Obtain the Inn Key and begin your management journey");
        entry("advancements.otherworldinn.too_many_beds.title")
                .zh("你要干什么")
                .en("What Are You Doing");
        entry("advancements.otherworldinn.too_many_beds.description")
                .zh("在一间房里放致死量的床")
                .en("Put a lethal amount of beds in one room");
        entry("advancements.otherworldinn.maimai_hidden.title")
                .zh("这不是洗衣机")
                .en("This Is Not a Washing Machine");
        entry("advancements.otherworldinn.maimai_hidden.description")
                .zh("将金币投入某个地方")
                .en("Put a coin into somewhere");
        entry("advancements.otherworldinn.repair_boiler_room.title")
                .zh("修复锅炉房")
                .en("Repair Boiler Room");
        entry("advancements.otherworldinn.repair_boiler_room.description")
                .zh("成功修复锅炉房设施")
                .en("Successfully repair the boiler room facility");
        entry("advancements.otherworldinn.repair_greenhouse.title")
                .zh("修复温室")
                .en("Repair Greenhouse");
        entry("advancements.otherworldinn.repair_greenhouse.description")
                .zh("成功修复温室设施")
                .en("Successfully repair the greenhouse facility");
        entry("advancements.otherworldinn.inn_rating_1.title").zh("新的开端").en("A New Beginning");
        entry("advancements.otherworldinn.inn_rating_1.description")
                .zh("将旅社星级提升至 1 级")
                .en("Upgrade inn rating to level 1");
        entry("advancements.otherworldinn.inn_rating_2.title").zh("初具规模").en("Taking Shape");
        entry("advancements.otherworldinn.inn_rating_2.description")
                .zh("将旅社星级提升至 2 级")
                .en("Upgrade inn rating to level 2");
        entry("advancements.otherworldinn.inn_rating_3.title").zh("小有成就").en("Making Progress");
        entry("advancements.otherworldinn.inn_rating_3.description")
                .zh("将旅社星级提升至 3 级")
                .en("Upgrade inn rating to level 3");
        entry("advancements.otherworldinn.inn_rating_4.title").zh("门庭若市").en("Bustling Inn");
        entry("advancements.otherworldinn.inn_rating_4.description")
                .zh("将旅社星级提升至 4 级")
                .en("Upgrade inn rating to level 4");
        entry("advancements.otherworldinn.inn_rating_5.title").zh("名扬天下").en("Renowned Everywhere");
        entry("advancements.otherworldinn.inn_rating_5.description")
                .zh("将旅社星级提升至 5 级")
                .en("Upgrade inn rating to level 5");
        entry("advancements.otherworldinn.serve_one_vip.title")
                .zh("贵宾之礼")
                .en("VIP Hospitality");
        entry("advancements.otherworldinn.serve_one_vip.description")
                .zh("成功完成一位贵宾的点单餐品")
                .en("Successfully complete one VIP guest meal order");
        entry("advancements.otherworldinn.create_first_room.title")
                .zh("第一个房间")
                .en("First Room");
        entry("advancements.otherworldinn.create_first_room.description")
                .zh("使用房间登记册创建你的第一间客房")
                .en("Use the Room Register to create your first guest room");
        entry("advancements.otherworldinn.serve_first_guest.title")
                .zh("欢迎光临！")
                .en("Welcome!");
        entry("advancements.otherworldinn.serve_first_guest.description")
                .zh("接待第一位旅客入住你的旅社")
                .en("Receive your very first guest at the inn");
        entry("advancements.otherworldinn.blacksmith_max_favor.title")
                .zh("铁匠的挚友")
                .en("Blacksmith's Bosom Friend");
        entry("advancements.otherworldinn.blacksmith_max_favor.description")
                .zh("铁匠对你的好感达到了最高")
                .en("The Blacksmith holds you in the highest regard");
        entry("advancements.otherworldinn.farmer_max_favor.title")
                .zh("农夫的挚友")
                .en("Farmer's Bosom Friend");
        entry("advancements.otherworldinn.farmer_max_favor.description")
                .zh("农夫对你的好感达到了最高")
                .en("The Farmer holds you in the highest regard");
        entry("advancements.otherworldinn.magician_max_favor.title")
                .zh("魔法使的挚友")
                .en("Magician's Bosom Friend");
        entry("advancements.otherworldinn.magician_max_favor.description")
                .zh("魔法使对你的好感达到了最高")
                .en("The Magician holds you in the highest regard");
        entry("advancements.otherworldinn.grocer_max_favor.title")
                .zh("杂货店老板的挚友")
                .en("Grocer's Bosom Friend");
        entry("advancements.otherworldinn.grocer_max_favor.description")
                .zh("杂货店老板对你的好感达到了最高")
                .en("The Grocer holds you in the highest regard");
        entry("advancements.otherworldinn.all_npc_max_favor.title")
                .zh("人见人爱")
                .en("Everyone's Favorite");
        entry("advancements.otherworldinn.all_npc_max_favor.description")
                .zh("所有 NPC 的好感度都达到了最高")
                .en("All NPCs hold you in the highest regard");
        entry("advancements.otherworldinn.boiler_room_max_level.title")
                .zh("锅炉房完全运转")
                .en("Boiler Room Fully Operational");
        entry("advancements.otherworldinn.boiler_room_max_level.description")
                .zh("将锅炉房设施升级至满级")
                .en("Upgrade the Boiler Room facility to max level");
        entry("advancements.otherworldinn.greenhouse_max_level.title")
                .zh("温室满园春色")
                .en("Greenhouse in Full Bloom");
        entry("advancements.otherworldinn.greenhouse_max_level.description")
                .zh("将温室设施升级至满级")
                .en("Upgrade the Greenhouse facility to max level");
        entry("advancements.otherworldinn.all_facility_max_level.title")
                .zh("设施全精通")
                .en("Master of Facilities");
        entry("advancements.otherworldinn.all_facility_max_level.description")
                .zh("将所有设施升级至满级")
                .en("Upgrade all facilities to max level");
        entry("advancements.otherworldinn.complete_1_commission.title")
                .zh("初次委托")
                .en("First Commission");
        entry("advancements.otherworldinn.complete_1_commission.description")
                .zh("完成你的第一个委托任务")
                .en("Complete your first commission");
        entry("advancements.otherworldinn.complete_20_commissions.title")
                .zh("委托达人")
                .en("Commission Expert");
        entry("advancements.otherworldinn.complete_20_commissions.description")
                .zh("累计完成 20 个委托任务")
                .en("Complete a total of 20 commissions");
        entry("facility.otherworldinn.overlay.title").zh("设施状态").en("Facility Status");
        entry("facility.otherworldinn.overlay.name").zh("名称: %s").en("Name: %s");
        entry("facility.otherworldinn.overlay.level").zh("等级: %s/%s").en("Level: %s/%s");
        entry("facility.otherworldinn.overlay.repair_cost")
                .zh("维修金币: §f\uE001§r%s")
                .en("Repair Coins: §f\uE001§r %s");
        entry("facility.otherworldinn.overlay.upgrade_cost")
                .zh("升级金币: §f\uE001§r%s")
                .en("Upgrade Coins: §f\uE001§r %s");
        entry("facility.otherworldinn.overlay.repair_items")
                .zh("维修材料: %s")
                .en("Repair Materials: %s");
        entry("facility.otherworldinn.overlay.upgrade_items")
                .zh("升级材料: %s")
                .en("Upgrade Materials: %s");
        entry("facility.otherworldinn.overlay.repair").zh("维修设施").en("Repair Facility");
        entry("facility.otherworldinn.overlay.upgrade").zh("升级设施").en("Upgrade Facility");
        entry("facility.otherworldinn.overlay.no_items").zh("无").en("None");
        entry("facility.otherworldinn.upgrade_max_level")
                .zh("该设施已达到最高等级")
                .en("This facility is already at max level");
        entry("facility.otherworldinn.upgrade_fail_coins")
                .zh("金币不足，需要§f\uE001§r%s，当前§f\uE001§r%s")
                .en("Not enough coins, need %s, have %s");
        entry("facility.otherworldinn.upgrade_fail_items")
                .zh("材料不足，无法进行维修/升级")
                .en("Missing required materials for repair/upgrade");
        entry("facility.otherworldinn.upgrade_fail_structure")
                .zh("结构放置失败，请检查结构模板文件")
                .en("Failed to place structure template");
        entry("facility.otherworldinn.repair_success")
                .zh("%s 已修复")
                .en("%s has been repaired to level %s");
        entry("facility.otherworldinn.upgrade_success")
                .zh("%s 已升级至 %s 级")
                .en("%s has been upgraded to level %s");

        // 字幕
        entry("subtitles.otherworldinn.payment").zh("金币：叮铃").en("Coins: Clink");
        entry("subtitles.otherworldinn.maimai").zh("舞萌DX：激活").en("maimai DX: Activated");
        entry("subtitles.otherworldinn.maimai_end").zh("舞萌DX：结算").en("maimai DX: Result");
        entry("subtitles.otherworldinn.coin_projectile").zh("金币：弹飞").en("Coin: Fling");

        // 队伍命令
        entry("command.otherworldinn.team.already_in_team")
                .zh("你已经在一个队伍中了！")
                .en("You are already in a team!");
        entry("command.otherworldinn.team.created").zh("已创建队伍：%s").en("Created team: %s");
        entry("command.otherworldinn.team.target_no_team")
                .zh("目标玩家不在队伍中！")
                .en("Target player is not in a team!");
        entry("command.otherworldinn.team.joined").zh("已加入队伍：%s").en("Joined team: %s");
        entry("command.otherworldinn.team.not_in_team")
                .zh("你不在一个队伍中！")
                .en("You are not in a team!");
        entry("command.otherworldinn.team.left").zh("已离开队伍。").en("Left the team.");
        entry("command.otherworldinn.team.not_leader")
                .zh("只有队长可以执行此操作！")
                .en("Only the leader can perform this action!");
        entry("command.otherworldinn.team.target_not_in_team")
                .zh("目标玩家不在你的队伍中！")
                .en("Target player is not in your team!");
        entry("command.otherworldinn.team.kick_self")
                .zh("你不能踢出你自己！请使用离开命令。")
                .en("You cannot kick yourself! Use leave command.");
        entry("command.otherworldinn.team.kicked").zh("已将 %s 踢出队伍。").en("Kicked %s from the team.");
        entry("command.otherworldinn.team.you_were_kicked")
                .zh("你已被踢出队伍。")
                .en("You were kicked from the team.");
        entry("command.otherworldinn.team.transferred")
                .zh("队长职位已移交给 %s")
                .en("Transferred leadership to %s");
        entry("command.otherworldinn.team.renamed").zh("队伍已重命名为：%s").en("Renamed team to: %s");
        entry("command.otherworldinn.team.teleport_set")
                .zh("队伍传送功能已设置为：%s")
                .en("Team teleport capability set to: %s");
        entry("command.otherworldinn.team.point_unlocked")
                .zh("已解锁地图点：%s (队伍：%s)")
                .en("Unlocked map point: %s (Team: %s)");
        entry("command.otherworldinn.team.point_locked")
                .zh("已锁定地图点：%s (队伍：%s)")
                .en("Locked map point: %s (Team: %s)");
        entry("command.otherworldinn.team.coins.set")
                .zh("已将队伍 %s 的余额设置为 §f\uE001§r %d")
                .en("Set team %s balance to §f\uE001§r %d");
        entry("command.otherworldinn.team.coins.add")
                .zh("已向队伍 %s 增加 §f\uE001§r %d (当前: §f\uE001§r %d)")
                .en("Added §f\uE001§r %d to team %s (Current: §f\uE001§r %d)");
        entry("command.otherworldinn.team.coins.remove")
                .zh("已从队伍 %s 扣除 §f\uE001§r %d (当前: §f\uE001§r %d)")
                .en("Removed §f\uE001§r %d from team %s (Current: §f\uE001§r %d)");
        entry("command.otherworldinn.team.coins.remove_fail")
                .zh("扣除失败！余额不足 (当前: §f\uE001§r %d)")
                .en("Failed to remove! Not enough balance (Current: §f\uE001§r %d)");
        entry("command.otherworldinn.team.coins.get")
                .zh("队伍 %s 当前余额: §f\uE001§r %d")
                .en("Team %s current balance: §f\uE001§r %d");
        entry("command.otherworldinn.team.rating.set")
                .zh("已将队伍 %s 的旅社星级设置为 %s")
                .en("Set team %s inn rating to %s");

        // 管理员命令
        entry("command.otherworldinn.admin.facility.not_found")
                .zh("未找到设施：%s")
                .en("Facility not found: %s");
        entry("command.otherworldinn.admin.facility.level_out_of_range")
                .zh("目标等级超出范围，最大等级为 %s")
                .en("Target level is out of range, max level is %s");
        entry("command.otherworldinn.admin.facility.town_unavailable")
                .zh("城镇维度未加载，无法修改设施等级")
                .en("Town dimension is not loaded, cannot modify facility level");
        entry("command.otherworldinn.admin.facility.place_fail")
                .zh("等级修改失败：目标等级结构放置失败")
                .en("Level update failed: target level structure placement failed");
        entry("command.otherworldinn.admin.facility.downgrade_success")
                .zh("已将队伍 %s 的设施 %s 修改为 %s 级")
                .en("Updated team %s facility %s to level %s");
        entry("command.otherworldinn.admin.expedition.no_active")
                .zh("当前没有活跃的远征")
                .en("No active expedition");
        entry("command.otherworldinn.admin.store.reset_all.town_unavailable")
                .zh("城镇维度未加载，无法重置商店NPC数据")
                .en("Town dimension is not loaded, cannot reset store NPC data");
        entry("command.otherworldinn.admin.store.reset_all.success")
                .zh("已重置 %s 个商店NPC数据（好感进度归零，并按代码默认值重建）")
                .en(
                        "Reset %s store NPC(s): favor progress cleared and data rebuilt from code defaults.");

        entry("message.otherworldinn.reset.warning")
                .zh("§c[注意] §e外部维度还有%d分钟重置，请尽快回到城镇")
                .en(
                        "§c[Notice] §eExternal dimensions will reset in %d minutes! Please return to Town ASAP.");
        entry("message.otherworldinn.reset.start")
                .zh("§c[警告] 外部维度重置中，请暂时不要离开城镇")
                .en("§c[WARNING] External dimensions resetting... Please do not leave Town.");
        entry("message.otherworldinn.reset.teleported")
                .zh("§a你已被传送到城镇安全区域")
                .en("§aYou have been teleported to the Town safe zone.");
        entry("message.otherworldinn.reset.complete")
                .zh("§a外部维度重置完成，准备迎接新的冒险")
                .en("§aExternal dimension reset complete. Get ready for new adventures!");
        entry("message.otherworldinn.town.boundary_warning")
                .zh("前面的区域，还是不要去探索了吧...")
                .en("You should not explore the area ahead...");

        // 保护提示
        entry("message.otherworldinn.protection.deny")
                .zh("你不能修改城镇内的方块")
                .en("You cannot modify blocks within the Town.");
        entry("message.otherworldinn.protection.deny_guest_in_room")
                .zh("此房间有人入住，无法修改布局")
                .en("This room is occupied; you cannot modify its layout.");
        entry("message.otherworldinn.protection.banned_item")
                .zh("此物品在城镇维度被禁用")
                .en("This item is banned in the Town dimension.");
        entry("message.otherworldinn.protection.only_in_town")
                .zh("此物品仅限在城镇维度使用")
                .en("This item is usable only in the Town dimension.");
        entry("message.otherworldinn.coin.no_team")
                .zh("你还没有队伍，无法存入金币")
                .en("You are not in a team, cannot deposit coin.");
        entry("message.otherworldinn.death_penalty")
                .zh("%s 体力不支倒下，支付医疗费用 %s")
                .en("%s collapsed, medical expenses deducted: %s");
        entry("message.otherworldinn.reskillable.auto_level_up")
                .zh("技能 %s 已升级至等级 %s")
                .en("Skill %s has been upgraded to level %s");

        // 物品提示
        entry("tooltip.otherworldinn.sell_price")
                .zh("售价: §f\uE001§r%s")
                .en("Sell Price: §f\uE001§r%s");
        entry("item.otherworldinn.recall_scroll.fail_in_town")
                .zh("回程卷轴无法在城镇中使用")
                .en("Recall Scroll cannot be used in Town!");
        entry("message.otherworldinn.recall_scroll.overlay.use")
                .zh("长按3秒回到旅社")
                .en("Hold 3s to return to Inn");
        entry("tooltip.otherworldinn.banned_in_town")
                .zh("§c当前维度内禁用")
                .en("§cBanned in this dimension");
        entry("tooltip.otherworldinn.only_in_town")
                .zh("§c仅限城镇维度使用")
                .en("§cOnly usable in Town dimension");
        entry("tooltip.otherworldinn.create_clipboard_hint")
                .zh("放置在旅社范围内时，会自动添加并提醒重要的待办事项")
                .en("When placed inside inn bounds, it automatically adds and reminds important todos.");
        entry("tooltip.otherworldinn.book.roster")
                .zh("潜行右键前台铃铛可生成旅客名册")
                .en("Sneak + right-click Desk Bell to create a guest roster.");
        entry("message.otherworldinn.book.desk_bell")
                .zh("潜行生成旅客名册")
                .en("Create Guest Roster");

        // 房间登记册
        entry("message.otherworldinn.room_register.not_edit_mode")
                .zh("你当前不在任何队伍中")
                .en("You are not in any team!");
        entry("message.otherworldinn.room_register.pos1_set")
                .zh("位置1已设置：%s")
                .en("Position 1 set: %s");
        entry("message.otherworldinn.room_register.create_success")
                .zh("房间创建成功！")
                .en("Room created successfully!");
        entry("message.otherworldinn.room_register.room_count")
                .zh("当前共有%s个房间")
                .en("There are currently %s rooms");
        entry("message.otherworldinn.room_register.remove_success")
                .zh("%s已被移除")
                .en("%s has been removed");
        entry("message.otherworldinn.room_register.invalid_room")
                .zh("无效的房间结构")
                .en("Invalid room structure!");
        entry("message.otherworldinn.room_register.validation.too_small")
                .zh("房间空间太小")
                .en("Room is too small!");
        entry("message.otherworldinn.room_register.validation.out_of_bounds")
                .zh("房间超出旅社范围")
                .en("Room is out of inn bounds!");
        entry("message.otherworldinn.room_register.validation.hole_in_floor")
                .zh("房间的地板有漏洞")
                .en("There is a hole in the floor!");
        entry("message.otherworldinn.room_register.validation.hole_in_ceiling")
                .zh("房间的天花板有漏洞")
                .en("There is a hole in the ceiling!");
        entry("message.otherworldinn.room_register.validation.hole_in_wall")
                .zh("房间的墙壁有漏洞")
                .en("There is a hole in the walls!");
        entry("message.otherworldinn.room_register.validation.missing_door")
                .zh("房间缺少门")
                .en("Room is missing a door!");
        entry("message.otherworldinn.room_register.validation.missing_bed")
                .zh("房间缺少干净的床")
                .en("Room is missing a clean bed!");
        entry("message.otherworldinn.room_register.validation.overlap")
                .zh("房间与已有房间重叠！")
                .en("Room overlaps with an existing room!");
        entry("message.otherworldinn.room_register.remove_success_with_reason")
                .zh("%s已被移除。原因：%s")
                .en("%s has been removed. Reason: %s");
        entry("message.otherworldinn.room_register.manual_removal").zh("手动移除").en("Manual removal");
        entry("message.otherworldinn.room_register.validation.too_crowded")
                .zh("房间过于拥挤")
                .en("Room is too crowded!");

        entry("message.otherworldinn.room_register.overlay.delete_room")
                .zh("删除房间")
                .en("Delete Room");
        entry("message.otherworldinn.room_register.overlay.add_room").zh("添加房间").en("Add Room");
        entry("message.otherworldinn.room_register.overlay.show_room").zh("显示房间").en("Show Rooms");

        // 待办事项
        entry("todo.otherworldinn.room_cleaning").zh("%s需要打扫").en("%s needs cleaning");
        entry("todo.otherworldinn.guest_waiting").zh("%s 正在等待办理入住").en("%s is waiting to check in");
        entry("todo.otherworldinn.vip_meal_order").zh("贵宾 %s 选订了餐品 %s").en("VIP %s ordered meal %s");
        entry("todo.otherworldinn.town_commission_pending")
                .zh("有待完成的居民委托")
                .en("Pending town commission");

        // 房间重命名
        entry("message.otherworldinn.room_rename.success")
                .zh("房间已重命名为：%s")
                .en("Room renamed to: %s");
        entry("message.otherworldinn.room_rename.overlay")
                .zh("重命名房间")
                .en("Rename room");

        // 生物群系
        entry("biome.otherworldinn.town").zh("城镇").en("Town");
        entry("biome.spawn.seagrass_meadow").zh("海草床").en("Seagrass Meadow");
        entry("biome.spawn.rocky_shore").zh("岩岸").en("Rocky Shore");
        entry("biome.spawn.deep_warm_ocean").zh("暖水深海").en("Deep Warm Ocean");
        entry("biome.spawn.tropical_island").zh("热带岛屿").en("Tropical Island");
        entry("biome.spawn.sandy_island").zh("覆沙岛屿").en("Sandy Island");
        entry("biome.spawn.cold_island").zh("寒冷岛屿").en("Cold Island");
        entry("biome.spawn.dodo_island").zh("渡渡鸟岛屿").en("Dodo Island");
        entry("biome.spawn.tide_pool").zh("潮池").en("Tide Pool");
        entry("biome.spawn.volcanic_island").zh("火山岛屿").en("Volcanic Island");

        // 家具属性
        entry("tooltip.otherworldinn.furniture.comfort")
                .zh("§f\uE002§r舒适度: %s")
                .en("§f\uE002§rComfort: %s");
        entry("tooltip.otherworldinn.furniture.light")
                .zh("§f\uE003§r光照度: %s")
                .en("§f\uE003§rLight: %s");
        entry("tooltip.otherworldinn.furniture.humidity")
                .zh("§f\uE004§r湿度: %s")
                .en("§f\uE004§rHumidity: %s");

        // 地契
        entry("message.otherworldinn.land_deed.pos1_set").zh("位置1已设置：%s").en("Position 1 set: %s");
        entry("message.otherworldinn.land_deed.success")
                .zh("旅社区域扩展成功！")
                .en("Inn area expanded successfully!");
        entry("message.otherworldinn.land_deed.overlay.set_pos1").zh("设置第一点").en("Set 1st Corner");
        entry("message.otherworldinn.land_deed.overlay.set_pos2").zh("设置第二点").en("Set 2nd Corner");
        entry("message.otherworldinn.land_deed.overlay.set_pos2_with_cost")
                .zh("设置第二点 (预计花费: §f\uE001§r %d)")
                .en("Set 2nd Corner (Est. Cost: §f\uE001§r %d)");
        entry("message.otherworldinn.land_deed.pos2_set_with_cost")
                .zh("成功选定区域 (预计花费: §f\uE001§r %d)")
                .en("Area selected successfully (Est. Cost: §f\uE001§r %d)");
        entry("message.otherworldinn.land_deed.pos2_set_with_cost_fail")
                .zh("余额不足以扩展当前选定的范围！需要 §f\uE001§r %d，当前 §f\uE001§r %d")
                .en(
                        "Insufficient balance to expand the selected area! Need §f\uE001§r %d, have §f\uE001§r %d");
        entry("message.otherworldinn.land_deed.fail_no_money")
                .zh("余额不足！需要 §f\uE001§r %d，当前 §f\uE001§r %d")
                .en("Expansion failed: Not enough balance! Need §f\uE001§r %d, have §f\uE001§r %d");
        entry("message.otherworldinn.land_deed.overlay.confirm_with_cost")
                .zh("确认花费 §f\uE001§r %d 扩展旅社范围")
                .en("Confirm Expansion (Cost: §f\uE001§r %d)");
        entry("message.otherworldinn.land_deed.fail_out_of_bounds")
                .zh("无法扩展：超出最大范围")
                .en("Cannot expand: Exceeds maximum range! ");
        entry("message.otherworldinn.land_deed.fail_rating_limit")
                .zh("无法扩展：超出星级允许上限")
                .en("Cannot expand: Exceeds current star-level land limit");
        entry("message.otherworldinn.land_deed.selection_cleared")
                .zh("已取消选定范围。")
                .en("Selection cleared.");
        entry("message.otherworldinn.land_deed.overlay.cancel").zh("取消选定").en("Cancel Selection");
        entry("tooltip.otherworldinn.land_deed.rating")
                .zh("当前星级: %s")
                .en("Current Rating: %s");
        entry("tooltip.otherworldinn.land_deed.area_status")
                .zh("已扩展范围: %s / 剩余可用: %s")
                .en("Expanded Area: %s / Remaining: %s");

        // 房间钥匙
        entry("item.otherworldinn.room_key.bound").zh("%s钥匙").en("%s Key");
        entry("item.otherworldinn.room_key.bound_full").zh("%s钥匙（已满员）").en("%s Key (Full)");
        entry("message.otherworldinn.room_key.bound")
                .zh("成功绑定到 %s")
                .en("Successfully bound to %s");
        entry("message.otherworldinn.room_key.unbound").zh("已解除绑定").en("Unbound from room");
        entry("message.otherworldinn.room_key.no_room").zh("此处没有房间").en("No room here");
        entry("message.otherworldinn.room_key.overlay.bind").zh("绑定房间").en("Bind Room");
        entry("message.otherworldinn.room_key.overlay.unbind").zh("解除绑定").en("Unbind Room");
        entry("tooltip.otherworldinn.room_key.room_id").zh("房间号: %d").en("Room ID: %d");
        entry("tooltip.otherworldinn.room_key.pos").zh("位置: %s -> %s").en("Pos: %s -> %s");
        entry("tooltip.otherworldinn.room_key.beds").zh("床位: %d/%d").en("Beds: %d/%d");
        entry("tooltip.otherworldinn.room_key.price")
                .zh("床位价格: §f\uE001§r%d")
                .en("Price: §f\uE001§r%d");
        entry("tooltip.otherworldinn.room_key.theme")
                .zh("房间主题：%s")
                .en("Room Theme: %s");

        // 旅客入住
        entry("message.otherworldinn.room_key.checkin_success")
                .zh("旅客成功入住到 %s！")
                .en("Guest successfully checked into %s!");
        entry("message.otherworldinn.room_key.checkin_fail_guest_busy")
                .zh("该旅客已经住在其他房间了")
                .en("This guest is already staying in another room.");
        entry("message.otherworldinn.room_key.checkin_fail_checked_out")
                .zh("该旅客已经退房，准备离开了")
                .en("This guest has checked out and is preparing to leave.");
        entry("message.otherworldinn.room_key.checkin_fail_not_in_inn")
                .zh("错误：旅客不在旅社区域内！")
                .en("Error: Guest is not within any Inn area!");

        // Todo
        entry("message.otherworldinn.todo.new_task").zh("有新的事项待处理: %s").en("New Task: %s");
        entry("message.otherworldinn.room_key.checkin_fail_no_room")
                .zh("房间不存在或已被拆除")
                .en("Room does not exist or has been demolished.");
        entry("message.otherworldinn.room_key.checkin_fail_id_mismatch")
                .zh("房间信息不匹配 请重新绑定钥匙")
                .en("Room info mismatch. Please re-bind key.");
        entry("message.otherworldinn.room_key.checkin_fail_full")
                .zh("这间房似乎满了...")
                .en("This room seems full...");
        entry("message.otherworldinn.room_key.overlay.checkin")
                .zh("安排入住 (消耗钥匙)")
                .en("Arrange Check-in (Consumes Key)");

        // 铃铛
        entry("message.otherworldinn.desk_bell.status.open").zh("旅社营业中").en("The Inn is OPEN");
        entry("message.otherworldinn.desk_bell.status.closed").zh("旅社已打烊").en("The Inn is CLOSED");
        entry("message.otherworldinn.store.overlay.open").zh("打开商店").en("Open Store");
        entry("message.otherworldinn.store.overlay.talk").zh("与店主对话").en("Talk");
        entry("screen.otherworldinn.commission_board.title").zh("委托板").en("Commission Board");
        entry("message.otherworldinn.commission.accept").zh("接取委托").en("Accept");
        entry("message.otherworldinn.commission.accepted").zh("已接取").en("Accepted");
        entry("message.otherworldinn.commission.accepted_team_broadcast")
                .zh("%s玩家已接取居民委托，限时%s天")
                .en("%s accepted a town commission, time limit: %s days");
        entry("message.otherworldinn.commission.empty").zh("暂无委托").en("Empty");
        entry("message.otherworldinn.commission.star").zh("%s星委托").en("%s-Star Commission");
        entry("message.otherworldinn.commission.difficulty").zh("难度：").en("Difficulty: ");
        entry("message.otherworldinn.commission.limit_day").zh("限时: %s 天").en("Limit: %s days");
        entry("message.otherworldinn.commission.expire_day").zh("到期日: 第 %s 天").en("Expire Day: %s");
        entry("message.otherworldinn.commission.remaining_day").zh("剩余: %s 天").en("Remaining: %s days");
        entry("message.otherworldinn.commission.today").zh("今日").en("Today");
        entry("message.otherworldinn.commission.no_new").zh("当前没有新的委托...").en("There are no new commissions right now...");
        entry("message.otherworldinn.commission.requirement").zh("目标").en("Objectives");
        entry("message.otherworldinn.commission.reward").zh("奖励").en("Rewards");
        entry("message.otherworldinn.commission.line.submit").zh("- 提交: %s x%s").en("- Submit: %s x%s");
        entry("message.otherworldinn.commission.line.submit_icon").zh("提交：").en("Submit: ");
        entry("message.otherworldinn.commission.line.reward_item_icon").zh("物品奖励：").en("Item Reward: ");
        entry("message.otherworldinn.commission.line.kill").zh("- 击杀: %s x%s").en("- Kill: %s x%s");
        entry("message.otherworldinn.commission.line.kill_progress")
                .zh("- 击杀: %s %s/%s")
                .en("- Kill: %s %s/%s");
        entry("message.otherworldinn.commission.line.coin").zh("§0-§r \uE001§0%s").en("§0-§r \uE001§0%s");
        entry("message.otherworldinn.commission.line.favor").zh("- 好感: %s +%s").en("- Favor: %s +%s");
        entry("message.otherworldinn.commission.overlay.submit")
                .zh("交付物品")
                .en("Submit Items");
        entry("message.otherworldinn.commission.overlay.view")
                .zh("查看委托")
                .en("View Commission");
        entry("message.otherworldinn.crystal_ball.overlay.enter")
                .zh("进入魔法空间")
                .en("Enter Magic Space");
        entry("message.otherworldinn.crystal_ball.already_exists")
                .zh("此存档已存在一个水晶球，无法放置第二个")
                .en("A Crystal Ball already exists in this world; you cannot place another.");
        entry("message.otherworldinn.crystal_ball.cannot_break_in_use")
                .zh("魔法空间中仍有玩家，无法破坏水晶球")
                .en("Cannot break the Crystal Ball while players are in the Magic Space.");
        entry("message.otherworldinn.commission.completed")
                .zh("委托已完成！")
                .en("Commission Completed!");
        entry("message.otherworldinn.commission.completed_with_rewards")
                .zh("委托已完成！获得奖励：")
                .en("Commission completed! Rewards:");
        entry("message.otherworldinn.commission.expired")
                .zh("委托已过期！")
                .en("Commission Expired!");
        entry("message.otherworldinn.commission.reward_line.coin")
                .zh("- \uE001%s")
                .en("- \uE001%s");
        entry("message.otherworldinn.commission.reward_line.item")
                .zh("- %s x%s")
                .en("- %s x%s");
        entry("message.otherworldinn.commission.reward_line.favor")
                .zh("- %s好感 +%s")
                .en("- %s Favor +%s");
        entry("message.otherworldinn.commission.submit_not_needed")
                .zh("该委托无需提交物品")
                .en("This commission does not require item submission");
        entry("message.otherworldinn.commission.submit_kill_unfinished")
                .zh("击杀目标尚未完成")
                .en("Kill objectives are not complete yet");
        entry("message.otherworldinn.commission.submit_items_missing")
                .zh("背包内缺少所需提交物品")
                .en("Missing required submission items in inventory");
        entry("screen.otherworldinn.dialogue.title").zh("对话").en("Dialogue");
        entry("dialogue.otherworldinn.npc.unknown").zh("陌生人").en("Unknown");
        addDialogueTranslations();
        addCommissionTranslations();
        entry("message.otherworldinn.broom.overlay.expel").zh("驱逐旅客（降低声望）").en("Expel Guest");
        entry("message.otherworldinn.inventory.overlay.coins")
                .zh("§f\uE001§r%s")
                .en("§f\uE001§r%s");
        entry("message.otherworldinn.inventory.overlay.coins.withdraw_one")
                .zh("左键点击可取出 1 枚金币")
                .en("Left-click to withdraw 1 coin");
        entry("message.otherworldinn.inventory.overlay.reputation_detail")
                .zh("声望：%s/%s")
                .en("Reputation: %s/%s");
        entry("message.otherworldinn.inventory.overlay.level_up.title")
                .zh("升星要求")
                .en("Star Upgrade Requirements");
        entry("message.otherworldinn.inventory.overlay.level_up.max")
                .zh("已达到最高星级")
                .en("Already at max star rating");
        entry("message.otherworldinn.inventory.overlay.level_up.target_rating")
                .zh("目标星级：%s 星")
                .en("Target Rating: %s★");
        entry("message.otherworldinn.inventory.overlay.level_up.requirement.rooms")
                .zh("房间数量：%s/%s（%s）")
                .en("Room Count: %s/%s (%s)");
        entry("message.otherworldinn.inventory.overlay.level_up.requirement.income")
                .zh("总营业额：§f\uE001§r%s/§f\uE001§r%s（%s）")
                .en("Total Income: §f\uE001§r%s/§f\uE001§r%s (%s)");
        entry("message.otherworldinn.inventory.overlay.level_up.requirement.reputation")
                .zh("声望：%s/%s（%s）")
                .en("Reputation: %s/%s (%s)");
        entry("message.otherworldinn.inventory.overlay.level_up.status.pass").zh("达成").en("Met");
        entry("message.otherworldinn.inventory.overlay.level_up.status.fail")
                .zh("未达成")
                .en("Not Met");
        entry("message.otherworldinn.inventory.overlay.income.title")
                .zh("收入统计")
                .en("Income Breakdown");
        entry("message.otherworldinn.inventory.overlay.income.total")
                .zh("总收入：§f\uE001§r%s")
                .en("Total Income: §f\uE001§r%s");
        entry("message.otherworldinn.inventory.overlay.income.yesterday")
                .zh("昨日收入")
                .en("Yesterday Income");
        entry("message.otherworldinn.inventory.overlay.income.lodging")
                .zh("住宿：§f\uE001§r%s")
                .en("Lodging: §f\uE001§r%s");
        entry("message.otherworldinn.inventory.overlay.income.dining")
                .zh("餐饮：§f\uE001§r%s")
                .en("Dining: §f\uE001§r%s");
        entry("message.otherworldinn.inventory.overlay.income.other")
                .zh("其它：§f\uE001§r%s")
                .en("Other: §f\uE001§r%s");

        // 旅社钥匙
        entry("message.otherworldinn.inn_key.no_permission")
                .zh("你没有权限管理这间旅社！")
                .en("You do not have permission to manage this Inn!");
        entry("message.otherworldinn.inn_key.overlay.toggle_state")
                .zh("潜行时切换旅社状态")
                .en("Toggle Inn State while Sneaking");
        entry("message.otherworldinn.inn_key.open")
                .zh("旅社已开业，今天也要努力")
                .en("The Inn is now OPEN! Let's work hard today.");
        entry("message.otherworldinn.inn_key.closed")
                .zh("旅社已歇业，快去休息吧...")
                .en("The Inn is now CLOSED. Time to rest...");
        entry("message.otherworldinn.inn_key.status").zh("当前状态: %s").en("Current State: %s");
        entry("message.otherworldinn.guest_roster.created")
                .zh("已生成旅客名册")
                .en("Guest roster created.");
        entry("message.otherworldinn.inn_upgrade_voucher.overlay.use")
                .zh("提升旅社评级")
                .en("Upgrade Inn Rating");
        entry("message.otherworldinn.inn_upgrade_voucher.no_team")
                .zh("你当前不在任何队伍中")
                .en("You are not in any team");
        entry("message.otherworldinn.inn_upgrade_voucher.fail")
                .zh("当前条件不足或已达到最高星级，无法提升旅社评级")
                .en("Current requirements are not met or already at max star rating. Inn rating cannot be upgraded.");
        entry("message.otherworldinn.inn_upgrade_voucher.success")
                .zh("旅社已升星！")
                .en("Inn Rating Increased!");
        entry("message.otherworldinn.space_sphere.no_team")
                .zh("你当前不在任何队伍中")
                .en("You are not in any team");
        entry("message.otherworldinn.space_sphere.already_unlocked")
                .zh("当前队伍的地图传送功能已解锁")
                .en("Your team's map teleport is already unlocked");
        entry("message.otherworldinn.space_sphere.teleport_unlocked")
                .zh("地图传送功能已激活")
                .en("Map teleport has been activated");
        entry("message.otherworldinn.end_space_sphere.overlay.use")
                .zh("右键使用传送到末地主岛")
                .en("Right-click to teleport to the End main island");
        entry("tooltip.otherworldinn.schematic.survival_print")
                .zh("生存模式可使用打印工具")
                .en("Print tool usable in survival mode");
        entry("message.otherworldinn.schematic.survival_print.enabled")
                .zh("生存打印已启用")
                .en("Survival Print enabled");
        entry("message.otherworldinn.schematic.survival_print.disabled")
                .zh("生存打印已禁用")
                .en("Survival Print disabled");
        entry("message.otherworldinn.guest.tooltip.title").zh("旅客信息").en("Guest Info");
        entry("message.otherworldinn.guest.tooltip.vip").zh("[贵宾]").en("[VIP]");
        entry("message.otherworldinn.guest.tooltip.preference.comfort")
                .zh("§f\uE002§r舒适偏好: %s-%s")
                .en("§f\uE002§rComfort Preference: %s-%s");
        entry("message.otherworldinn.guest.tooltip.preference.light")
                .zh("§f\uE003§r光照偏好: %s-%s")
                .en("§f\uE003§rLight Preference: %s-%s");
        entry("message.otherworldinn.guest.tooltip.preference.humidity")
                .zh("§f\uE004§r湿度偏好: %s-%s")
                .en("§f\uE004§rHumidity Preference: %s-%s");
        entry("message.otherworldinn.guest.tooltip.budget").zh("预算: §f\uE001§r%s").en("Budget: §f\uE001§r%s");
        entry("message.otherworldinn.guest.tooltip.rewards").zh("可能奖励:").en("Possible Rewards:");
        entry("message.otherworldinn.guest.tooltip.rewards.none").zh("无").en("None");
        entry("message.otherworldinn.guest.tooltip.rewards.entry")
                .zh("- %s x%s-%s")
                .en("- %s x%s-%s");
        entry("message.otherworldinn.map.teleport_not_unlocked")
                .zh("未解锁地图传送功能，请先使用空间球解锁")
                .en("Map teleport is not unlocked. Please use a Space Sphere first.");
        entry("jei.otherworldinn.npc_store.title").zh("商店商品").en("Store Products");
        entry("jei.otherworldinn.npc_store.store").zh("店主: %s").en("Store: %s");
        entry("jei.otherworldinn.npc_store.price").zh("价格: %s 金币").en("Price: %s coins");
        entry("jei.otherworldinn.npc_store.stock").zh("库存: %s").en("Stock: %s");
        entry("jei.otherworldinn.npc_store.favor").zh("好感需求: Lv.%s").en("Favor Required: Lv.%s");
        entry("jei.otherworldinn.npc_store.advancement").zh("进度需求: %s").en("Advancement Required: %s");
        entry("jei.otherworldinn.npc_store.random").zh("每日随机商品").en("Daily Random Offer");
        entry("jei.otherworldinn.npc_store.infinite").zh("无限").en("Infinite");

        // 床单
        entry("message.otherworldinn.bed_sheet.overlay.replace")
                .zh("替换脏乱床单")
                .en("Replace Messy Sheet");
        entry("message.otherworldinn.messy_bed_sheet.overlay.wash").zh("清洗床单").en("Wash Sheet");

        // 远征
        entry("message.otherworldinn.expedition.no_team").zh("你当前不在任何队伍中").en("You are not in any team");
        entry("message.otherworldinn.expedition.not_enough_coins").zh("队伍金币不足，还需 §f\uE001§r%s").en("Not enough coins, need %s more");
        entry("message.otherworldinn.expedition.solo_started").zh("远征已开启！维度传送中…").en("Solo expedition started! Entering dimension...");
        entry("message.otherworldinn.expedition.sneak_to_confirm").zh("潜行右键以确认开启远征").en("Sneak + right click to confirm expedition");
        entry("message.otherworldinn.expedition.not_leader").zh("只有发起者才能开启远征").en("Only the leader can start the expedition");
        entry("message.otherworldinn.expedition.started").zh("✦ 远征开始！%s 名冒险者踏上征途").en("✦ Expedition started! %s adventurers depart");
        entry("message.otherworldinn.expedition.cancelled").zh("已取消远征招募").en("Expedition recruitment cancelled");
        entry("message.otherworldinn.expedition.recruit_expired").zh("该招募已过期或已取消").en("This recruitment has expired or been cancelled");
        entry("message.otherworldinn.expedition.not_same_team").zh("只有同一旅社队伍的成员才能加入").en("Only members of the same inn team can join");
        entry("message.otherworldinn.expedition.already_in").zh("你已在另一场远征中").en("You are already in another expedition");
        entry("message.otherworldinn.expedition.full").zh("远征队伍已满").en("Expedition party is full");
        entry("message.otherworldinn.expedition.already_member").zh("你已在此远征队伍中").en("You are already in this party");
        entry("message.otherworldinn.expedition.joined").zh("你已加入远征队伍！等待发起者开启远征...").en("Joined the expedition party! Waiting for leader...");
        entry("message.otherworldinn.expedition.member_joined").zh(" 加入了远征队伍 (%s人)").en(" joined the party (%s players)");
        entry("message.otherworldinn.expedition.not_in").zh("你当前不在任何远征中").en("You are not in any expedition");
        entry("message.otherworldinn.expedition.status").zh("远征剩余时间：%s 分钟").en("Expedition remaining: %s minutes");
        entry("message.otherworldinn.expedition.no_recruiting_chart").zh("你没有正在招募中的星图").en("You have no recruiting chart");
        entry("message.otherworldinn.expedition.create_failed").zh("远征维度创建失败，请稍后重试").en("Failed to create expedition dimension, try again later");
        entry("message.otherworldinn.expedition.invalid_id").zh("无效的远征ID").en("Invalid expedition ID");
        entry("message.otherworldinn.expedition.cannot_enter").zh("你无法进入此远征维度").en("You cannot enter this expedition dimension");
        entry("message.otherworldinn.expedition.cannot_leave").zh("远征中无法前往其他维度").en("Cannot leave expedition dimension");
        entry("message.otherworldinn.expedition.already_active").zh("已有正在进行的远征，请等待当前远征结束后再尝试").en("An expedition is already in progress, wait for it to end");
        entry("message.otherworldinn.expedition.click_to_join").zh("[点击加入]").en("[Click to join]");
        entry("message.otherworldinn.expedition.click_to_join_hover").zh("点击加入此次远征队伍").en("Click to join this expedition party");
        entry("message.otherworldinn.expedition.recruit_broadcast").zh("✦ %s 发起了一场远征组队（%s | 预估费用 §f\uE001§r%s）%s").en("✦ %s started an expedition party (%s | est. fee %s coins) %s");
        entry("message.otherworldinn.expedition.aborted").zh("远征已被管理员强制终止").en("Expedition has been forcibly terminated by admin");

        entry("tooltip.otherworldinn.chart_component.blank_entry").zh("  ▸ 空白组件").en("  ▸ Blank Component");
        entry("tooltip.otherworldinn.chart_component.blank").zh("用于合成其它星图组件").en("Used to craft other chart components");
        entry("tooltip.otherworldinn.chart_component.effect").zh("%s").en("%s");
        entry("tooltip.otherworldinn.chart_component.side_effect").zh("  ⚠ %s").en("  ⚠ %s");
        entry("tooltip.otherworldinn.chart_component.fee").zh("费用：+§f\uE001§r%s").en("Fee: +%s coins");

        entry("tooltip.otherworldinn.expedition_chart.recruiting").zh("  ▸ 招募中").en("  ▸ Recruiting");
        entry("tooltip.otherworldinn.expedition_chart.leader").zh("  发起者：%s").en("  Leader: %s");
        entry("tooltip.otherworldinn.expedition_chart.joined_count").zh("  已加入 (%s/%s)：").en("  Joined (%s/%s):");
        entry("tooltip.otherworldinn.expedition_chart.member_entry").zh("    ● %s").en("    ● %s");
        entry("tooltip.otherworldinn.expedition_chart.waiting_slot").zh("    ○ 等待中...").en("    ○ Waiting...");
        entry("tooltip.otherworldinn.expedition_chart.no_components").zh("  未附加组件").en("  No components attached");
        entry("tooltip.otherworldinn.expedition_chart.available_slots").zh("  可用槽位：%s").en("  Available slots: %s");
        entry("tooltip.otherworldinn.expedition_chart.attached_components").zh("  已附加组件 (%s/%s)：").en("  Attached Components (%s/%s):");
        entry("tooltip.otherworldinn.expedition_chart.component_entry").zh("    ◇ %s").en("    ◇ %s");
        entry("tooltip.otherworldinn.expedition_chart.empty_slot").zh("    ○ 空闲槽位").en("    ○ Empty slot");
        entry("tooltip.otherworldinn.expedition_chart.time_limit").zh("  时限：%s 分钟").en("  Time limit: %s min");
        entry("tooltip.otherworldinn.expedition_chart.max_players").zh("  队伍：最多 %s 人").en("  Party: up to %s players");
        entry("tooltip.otherworldinn.expedition_chart.fee").zh("  预估费用：§f\uE001§r%s").en("  Estimated fee: %s coins");
        entry("tooltip.otherworldinn.expedition_chart.fee_with_count").zh("  费用：§f\uE001§r%s（%s 人）").en("  Fee: %s coins (%s players)");
        entry("tooltip.otherworldinn.expedition_chart.dimension").zh("  维度类别：%s").en("  Dimension: %s");
        entry("message.otherworldinn.expedition.overlay.use").zh("开启远征").en("Use Chart");
        entry("message.otherworldinn.expedition.overlay.launch").zh("潜行开始远征").en("Sneak To Start");
        entry("message.otherworldinn.expedition.overlay.cancel").zh("取消招募").en("Cancel Recruitment");

        // 旅客姓名
        entry("guest.name.format").zh("%s·%s").en("%s %s");

        // First Names
        entry("guest.name.first.1").zh("亚瑟").en("Arthur");
        entry("guest.name.first.2").zh("贝阿特丽丝").en("Beatrice");
        entry("guest.name.first.3").zh("凯斯宾").en("Caspian");
        entry("guest.name.first.4").zh("多里安").en("Dorian");
        entry("guest.name.first.5").zh("埃莉诺").en("Eleanor");
        entry("guest.name.first.6").zh("菲利克斯").en("Felix");
        entry("guest.name.first.7").zh("吉迪恩").en("Gideon");
        entry("guest.name.first.8").zh("海泽尔").en("Hazel");
        entry("guest.name.first.9").zh("艾瑞丝").en("Iris");
        entry("guest.name.first.10").zh("朱利安").en("Julian");
        entry("guest.name.first.11").zh("凯尔").en("Kael");
        entry("guest.name.first.12").zh("莉珊德拉").en("Lysandra");
        entry("guest.name.first.13").zh("马格努斯").en("Magnus");
        entry("guest.name.first.14").zh("诺拉").en("Nora");
        entry("guest.name.first.15").zh("奥赖恩").en("Orion");
        entry("guest.name.first.16").zh("帕西瓦尔").en("Percival");
        entry("guest.name.first.17").zh("奎因").en("Quinn");
        entry("guest.name.first.18").zh("罗伊纳").en("Rowena");
        entry("guest.name.first.19").zh("塞拉斯").en("Silas");
        entry("guest.name.first.20").zh("塔莉亚").en("Thalia");
        entry("guest.name.first.21").zh("奥利弗").en("Oliver");
        entry("guest.name.first.22").zh("乔治").en("George");
        entry("guest.name.first.23").zh("哈利").en("Harry");
        entry("guest.name.first.24").zh("诺亚").en("Noah");
        entry("guest.name.first.25").zh("杰克").en("Jack");
        entry("guest.name.first.26").zh("查理").en("Charlie");
        entry("guest.name.first.27").zh("阿尔菲").en("Alfie");
        entry("guest.name.first.28").zh("利奥").en("Leo");
        entry("guest.name.first.29").zh("奥斯卡").en("Oscar");
        entry("guest.name.first.30").zh("亚齐").en("Archie");
        entry("guest.name.first.31").zh("艾拉").en("Isla");
        entry("guest.name.first.32").zh("奥利维娅").en("Olivia");
        entry("guest.name.first.33").zh("阿米莉亚").en("Amelia");
        entry("guest.name.first.34").zh("艾娃").en("Ava");
        entry("guest.name.first.35").zh("艾米莉").en("Emily");
        entry("guest.name.first.36").zh("伊莎贝拉").en("Isabella");
        entry("guest.name.first.37").zh("格蕾丝").en("Grace");
        entry("guest.name.first.38").zh("芙蕾娅").en("Freya");
        entry("guest.name.first.39").zh("杰西卡").en("Jessica");
        entry("guest.name.first.40").zh("索菲").en("Sophie");

        // Last Names
        entry("guest.name.last.1").zh("阿什福德").en("Ashford");
        entry("guest.name.last.2").zh("布莱克伍德").en("Blackwood");
        entry("guest.name.last.3").zh("克劳利").en("Crowley");
        entry("guest.name.last.4").zh("达文波特").en("Davenport");
        entry("guest.name.last.5").zh("埃弗哈特").en("Everhart");
        entry("guest.name.last.6").zh("弗罗斯特").en("Frost");
        entry("guest.name.last.7").zh("格林").en("Grimm");
        entry("guest.name.last.8").zh("霍桑").en("Hawthorne");
        entry("guest.name.last.9").zh("铁木").en("Ironwood");
        entry("guest.name.last.10").zh("金克斯").en("Jinx");
        entry("guest.name.last.11").zh("奈特").en("Knight");
        entry("guest.name.last.12").zh("洛夫莱斯").en("Lovelace");
        entry("guest.name.last.13").zh("穆恩").en("Moon");
        entry("guest.name.last.14").zh("霍洛韦").en("Holloway");
        entry("guest.name.last.15").zh("哈特").en("Hatter");
        entry("guest.name.last.16").zh("潘德加斯特").en("Pendergast");
        entry("guest.name.last.17").zh("雷文斯克罗夫特").en("Ravenscroft");
        entry("guest.name.last.18").zh("斯托姆").en("Storm");
        entry("guest.name.last.19").zh("索恩").en("Thorne");
        entry("guest.name.last.20").zh("温特").en("Winter");
        entry("guest.name.last.21").zh("史密斯").en("Smith");
        entry("guest.name.last.22").zh("琼斯").en("Jones");
        entry("guest.name.last.23").zh("威廉姆斯").en("Williams");
        entry("guest.name.last.24").zh("泰勒").en("Taylor");
        entry("guest.name.last.25").zh("布朗").en("Brown");
        entry("guest.name.last.26").zh("戴维斯").en("Davies");
        entry("guest.name.last.27").zh("埃文斯").en("Evans");
        entry("guest.name.last.28").zh("威尔逊").en("Wilson");
        entry("guest.name.last.29").zh("托马斯").en("Thomas");
        entry("guest.name.last.30").zh("罗伯茨").en("Roberts");
        entry("guest.name.last.31").zh("约翰逊").en("Johnson");
        entry("guest.name.last.32").zh("刘易斯").en("Lewis");
        entry("guest.name.last.33").zh("沃克").en("Walker");
        entry("guest.name.last.34").zh("赖特").en("Wright");
        entry("guest.name.last.35").zh("罗宾逊").en("Robinson");
        entry("guest.name.last.36").zh("汤普森").en("Thompson");
        entry("guest.name.last.37").zh("怀特").en("White");
        entry("guest.name.last.38").zh("休斯").en("Hughes");
        entry("guest.name.last.39").zh("爱德华兹").en("Edwards");
        entry("guest.name.last.40").zh("格林").en("Green");

        // 实体
        entry(ModEntities.ORDINARY_GUEST.get()).zh("普通旅客").en("Ordinary Guest");
        entry(ModEntities.RICH_GUEST.get()).zh("富有的旅客").en("Wealthy Guest");
        entry(ModEntities.HEAVY_PACK_GUEST.get()).zh("行囊多的旅客").en("Heavy-Pack Guest");
        entry(ModEntities.ULTRA_RICH_GUEST.get()).zh("非常富有的顾客").en("Ultra-Wealthy Customer");
        entry(ModEntities.ORDINARY_VIP_GUEST.get()).zh("普通贵宾").en("Ordinary VIP Guest");
        entry(ModEntities.ADVANCED_VIP_GUEST.get()).zh("进阶VIP旅客").en("Advanced VIP Guest");
        entry(ModEntities.SPONSOR_GUEST.get()).zh("赞助者旅客").en("Sponsor Guest");
        entry(ModEntities.BLACKSMITH.get()).zh("铁匠").en("Blacksmith");
        entry(ModEntities.MAGICIAN.get()).zh("魔法使").en("Magician");
        entry(ModEntities.FARMER.get()).zh("农夫").en("Farmer");
        entry(ModEntities.GROCER.get()).zh("杂货店老板").en("Grocer");

        // 商店 GUI
        entry("gui.otherworldinn.store.confirm").zh("确定").en("Confirm");
        entry("gui.otherworldinn.store.purchase").zh("§f\uE001§r%s购买").en("§f\uE001§r%sBuy");
        entry("gui.otherworldinn.store.price").zh("价格: §f\uE001§r%s").en("Price: §f\uE001§r%s");
        entry("gui.otherworldinn.store.stock").zh("库存: %s/%s").en("Stock: %s/%s");
        entry("gui.otherworldinn.store.limit_purchase").zh("限购%s个").en("Limit %s");
        entry("gui.otherworldinn.store.stock.infinite").zh("库存: ∞").en("Stock: ∞");
        entry("gui.otherworldinn.store.favor_unlock")
                .zh("%s级好感度解锁")
                .en("Unlocks at Favor Level %s");
        entry("gui.otherworldinn.store.progress_unlock")
                .zh("取得[%s]进度后解锁")
                .en("Unlocks after obtaining advancement [%s]");
        entry("gui.otherworldinn.store.favor.level").zh("好感度: %s").en("Favor Level: %s");
        entry("gui.otherworldinn.store.favor.progress")
                .zh("进度: §f\uE001§r%s/%s")
                .en("Progress: §f\uE001§r%s/%s");

        // 女仆任务
        entry("task.otherworldinn.clean_room").zh("清理房间").en("Room Cleaning");
        entry("task.otherworldinn.clean_room.desc")
                .zh("自动更换脏乱床铺床单，并在需要时清洗脏床单。")
                .en("Automatically replaces messy bed sheets and washes dirty sheets when needed.");
        entry("task.otherworldinn.front_desk").zh("前台接待").en("Front Desk");
        entry("task.otherworldinn.front_desk.desc")
                .zh("旅社营业时，自动为等待旅客匹配最合适房间并办理入住。")
                .en(
                        "When the Inn is open, automatically matches waiting guests to the best room and checks them in.");
    }

    private void addDialogueTranslations() {
        Set<String> seenOptionKeys = new HashSet<>();
        for (DialogueDefinition dialogue : DialogueRegistry.allDialogues()) {
            for (DialogueNodeDef node : dialogue.nodes().values()) {
                if (node.conditionalText() != null) {
                    entry(dialogue.nodeConditionalTextKey(node.id(), false))
                            .zh(node.conditionalText().unrepaired().zh())
                            .en(node.conditionalText().unrepaired().en());
                    entry(dialogue.nodeConditionalTextKey(node.id(), true))
                            .zh(node.conditionalText().repaired().zh())
                            .en(node.conditionalText().repaired().en());
                } else {
                    entry(dialogue.nodeTextKey(node.id())).zh(node.text().zh()).en(node.text().en());
                }
                for (DialogueOptionDef option : node.options()) {
                    String optionKey = dialogue.optionTextKey(node.id(), option.id());
                    if (seenOptionKeys.add(optionKey)) {
                        entry(optionKey).zh(option.label().zh()).en(option.label().en());
                    }
                }
            }
        }
    }

    private void addCommissionTranslations() {
        for (CommissionRegistry.CommissionTemplate template : CommissionRegistry.allTemplates()) {
            entry(template.descriptionKey())
                    .zh(template.description().zh())
                    .en(template.description().en());
        }
    }

    private void addGeneratedTranslations() {
        boolean isZh = "zh_cn".equals(locale);

        // 生成方块语言键
        for (Map.Entry<DeferredBlock<?>, BlockDataGenInfo> entry :
                ModBlocks.BLOCK_INFOS.entrySet()) {
            DeferredBlock<?> block = entry.getKey();
            BlockDataGenInfo info = entry.getValue();

            String name = isZh ? info.cnName() : info.enName();
            if (name != null && !name.isEmpty()) {
                add(block.get(), name);
            }

            List<String> tooltips = isZh ? info.cnTooltips() : info.enTooltips();
            for (int i = 0; i < tooltips.size(); i++) {
                add(block.get().getDescriptionId() + ".tooltip." + i, tooltips.get(i));
            }
        }

        // 生成物品语言键
        for (Map.Entry<DeferredItem<?>, ItemDataGenInfo> entry : ModItems.ITEM_INFOS.entrySet()) {
            DeferredItem<?> item = entry.getKey();
            ItemDataGenInfo info = entry.getValue();

            String name = isZh ? info.cnName() : info.enName();
            if (name != null && !name.isEmpty()) {
                add(item.get(), name);
            }

            List<String> tooltips = isZh ? info.cnTooltips() : info.enTooltips();
            for (int i = 0; i < tooltips.size(); i++) {
                add(item.get().getDescriptionId() + ".tooltip." + i, tooltips.get(i));
            }
        }

        for (FacilityRegistry.FacilityDefinition facility : FacilityRegistry.getAll()) {
            add(facility.translationKey(), isZh ? facility.zhName() : facility.enName());
        }
    }

    private TranslationBuilder entry(String key) {
        return new TranslationBuilder(key);
    }

    private TranslationBuilder entry(EntityType<?> entity) {
        return new TranslationBuilder(entity.getDescriptionId());
    }

    private class TranslationBuilder {
        private final String key;

        public TranslationBuilder(String key) {
            this.key = key;
        }

        public TranslationBuilder zh(String value) {
            if ("zh_cn".equals(locale)) {
                add(key, value);
            }
            return this;
        }

        public TranslationBuilder en(String value) {
            if (!"zh_cn".equals(locale)) {
                add(key, value);
            }
            return this;
        }
    }
}
