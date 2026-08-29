package com.otherworldinn.client.ponder;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.otherworldinn.init.ModItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public final class OtherworldInnPonderScenes {
    private static final String DEPOT_HELD_ITEM_NBT =
            "{HeldItem:{Angle:272,InDirection:5,InSegment:0,Item:{count:16,id:\"kaleidoscope_cookery:sweet_and_sour_ender_pearls\"},Offset:0.0f,Pos:0.49812075f,PrevOffset:0.0f,PrevPos:0.49812075f},OutputBuffer:{Items:[],Size:8}}";
    private static final BlockPos BOILER_ROOM_SOURCE_MOTOR_POS = new BlockPos(0, 2, 4);
    private static final BlockPos BOILER_ROOM_REMOTE_MOTOR_POS = new BlockPos(1, 1, 1);
    private static final BlockPos MINE_OUTPUT_BARREL_POS = new BlockPos(14, 2, 3);
    private static final int GREENHOUSE_DEMO_SPLIT_Z = 25;
    private static final int GREENHOUSE_DEMO_MAX_X = 16;

    private OtherworldInnPonderScenes() {}

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemLike> itemHelper =
                helper.withKeyFunction(itemLike -> BuiltInRegistries.ITEM.getKey(itemLike.asItem()));

        itemHelper.forComponents(createDepot())
                .addStoryBoard("basic", OtherworldInnPonderScenes::depot, OtherworldInnPonderTags.INN_SYSTEM);

        itemHelper.forComponents(Items.WOODEN_HOE, Items.STONE_HOE, Items.IRON_HOE)
                .addStoryBoard(
                        "greenhouse_demo",
                        OtherworldInnPonderScenes::greenhouse,
                        OtherworldInnPonderTags.INN_SYSTEM);

        itemHelper.forComponents(createWrench())
                .addStoryBoard(
                        "boiler_room",
                        OtherworldInnPonderScenes::boilerRoom,
                        OtherworldInnPonderTags.INN_SYSTEM);

        itemHelper.forComponents(Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE)
                .addStoryBoard(
                        "mine",
                        OtherworldInnPonderScenes::mine,
                        OtherworldInnPonderTags.INN_SYSTEM);

        itemHelper.forComponents(ModItems.INN_KEY.get())
                .addStoryBoard(
                        "inn_key",
                        OtherworldInnPonderScenes::innKey,
                        OtherworldInnPonderTags.INN_SYSTEM);

        itemHelper.forComponents(ModItems.ROOM_REGISTER.get())
                .addStoryBoard(
                        "room_key",
                        OtherworldInnPonderScenes::roomRegister,
                        OtherworldInnPonderTags.INN_SYSTEM);

        itemHelper.forComponents(ModItems.ROOM_KEY.get())
                .addStoryBoard(
                        "room_key",
                        OtherworldInnPonderScenes::roomBinding,
                        OtherworldInnPonderTags.INN_SYSTEM);
    }

    public static void depot(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("depot", "Depot as Buffet Counter");
        scene.world().showSection(util.select().everywhere(), Direction.UP);
        scene.addKeyframe();
        scene.idle(5);

        scene.world().setBlock(util.grid().at(2, 1, 2), block("create", "depot").defaultBlockState(), false);
        scene.overlay().showText(40)
                .text("Depots can be used as buffet counters")
                .placeNearTarget()
                .pointAt(util.vector().of(2.5, 2, 2.5));
        scene.idle(60);

        scene.addKeyframe();
        scene.overlay().showControls(util.vector().of(2.5, 2, 2.5), Pointing.DOWN, 20)
                .rightClick()
                .withItem(stack("kaleidoscope_cookery:sweet_and_sour_ender_pearls"));
        scene.world().modifyBlockEntityNBT(
                util.select().position(2, 1, 2),
                BlockEntity.class,
                nbt -> mergeNbt(nbt, DEPOT_HELD_ITEM_NBT),
                true);
        scene.idle(40);

        scene.overlay().showText(50)
                .text("Guests will buy the dishes placed on top")
                .placeNearTarget()
                .pointAt(util.vector().of(2.5, 2, 2.5));
        scene.idle(50);
        scene.markAsFinished();
    }

    public static void greenhouse(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("greenhouse", "Greenhouse");
        scene.scaleSceneView(0.5f);
        scene.addKeyframe();
        scene.world().showSection(greenhousePreviewSection(util), Direction.UP);
        scene.idle(20);

        scene.overlay().showText(70)
                .text("Normally, crops cannot grow during the wrong season...")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().of(5.5, 2.5, 8.5));
        scene.idle(80);

        scene.world().hideSection(greenhousePreviewSection(util), Direction.UP);
        scene.idle(20);
        ElementLink<WorldSectionElement> greenhouseFacility =
                scene.world().showIndependentSection(greenhouseFacilitySection(util), Direction.UP);
        scene.world().moveSection(greenhouseFacility, util.vector().of(0, 0, -GREENHOUSE_DEMO_SPLIT_Z), 0);
        scene.overlay().showText(60)
                .text("...but after repairing the town greenhouse, you can grow off-season crops inside, though a bit slower")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().of(6.5, 8.5, 0));
        scene.idle(80);

        scene.overlay().showText(60)
                .text("The greenhouse also accelerates crop growth, and the effect improves with facility level")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().of(6.5, 8.5, 0));
        scene.idle(80);

        scene.overlay().showText(60)
                .text("Blocks inside the greenhouse can be changed freely, so build any layout you like!")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().of(9.5, 1, 2.5));
        scene.idle(60);
        scene.markAsFinished();
    }

    public static void boilerRoom(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("boiler_room", "Boiler Room");
        scene.scaleSceneView(0.9f);
        scene.addKeyframe();
        scene.world().showSection(util.select().everywhere(), Direction.UP);
        scene.idle(20);

        scene.overlay().showText(60)
                .text("The repaired boiler room can provide remote stress to machinery")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(centerOf(util, BOILER_ROOM_SOURCE_MOTOR_POS));
        scene.idle(70);

        scene.world().setBlock(
                BOILER_ROOM_REMOTE_MOTOR_POS,
                block("createutilities", "void_motor").defaultBlockState(),
                true);
        scene.overlay().showOutline(
                PonderPalette.GREEN,
                "boiler_room_source_motor",
                util.select().position(BOILER_ROOM_SOURCE_MOTOR_POS),
                80);
        scene.overlay().showOutline(
                PonderPalette.BLUE,
                "boiler_room_remote_motor",
                util.select().position(BOILER_ROOM_REMOTE_MOTOR_POS),
                80);
        scene.overlay().showControls(centerOf(util, BOILER_ROOM_SOURCE_MOTOR_POS), Pointing.DOWN, 30)
                .rightClick()
                .withItem(new ItemStack(createWrench()));
        scene.overlay().showText(70)
                .text("Use the void motor here to connect remotely to another void motor")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(centerOf(util, BOILER_ROOM_REMOTE_MOTOR_POS));
        scene.idle(80);

        scene.overlay().showText(60)
                .text("...and make sure the frequency is set correctly")
                .placeNearTarget()
                .pointAt(centerOf(util, BOILER_ROOM_REMOTE_MOTOR_POS));
        scene.idle(70);

        scene.overlay().showText(60)
                .text("Higher facility levels provide more stress")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(centerOf(util, BOILER_ROOM_SOURCE_MOTOR_POS));
        scene.idle(70);
        scene.markAsFinished();
    }

    public static void mine(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("mine", "Mine");
        scene.scaleSceneView(0.6f);
        scene.addKeyframe();
        scene.world().showSection(util.select().everywhere(), Direction.UP);
        scene.world().setBlock(MINE_OUTPUT_BARREL_POS, block("minecraft", "barrel").defaultBlockState(), false);
        scene.idle(20);

        scene.overlay().showOutline(
                PonderPalette.GREEN,
                "mine_output_barrel",
                util.select().position(MINE_OUTPUT_BARREL_POS),
                80);
        scene.overlay().showText(70)
                .text("After the mine is repaired, the barrel inside will produce ores every day")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(centerOf(util, MINE_OUTPUT_BARREL_POS));
        scene.idle(80);

        scene.overlay().showText(60)
                .text("Higher mine levels improve both the variety and amount of ores")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(centerOf(util, MINE_OUTPUT_BARREL_POS));
        scene.idle(70);

        scene.overlay().showText(60)
                .text("Remember to come back and collect the day's output from the barrel")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(centerOf(util, MINE_OUTPUT_BARREL_POS));
        scene.idle(70);
        scene.markAsFinished();
    }

    public static void innKey(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("inn_key", "Inn Key");
        scene.scaleSceneView(1.5f);
        scene.addKeyframe();
        scene.world().showSection(util.select().everywhere(), Direction.UP);
        scene.idle(20);

        scene.overlay().showText(60)
                .text("The inn key can be used to manage the inn's business status")
                .placeNearTarget()
                .pointAt(util.vector().of(0.5, 2, 2.5));
        scene.idle(60);

        scene.addKeyframe();
        scene.overlay().showControls(util.vector().of(0.5, 3, 2.5), Pointing.DOWN, 60)
                .rightClick()
                .withItem(new ItemStack(ModItems.INN_KEY.get()))
                .whileSneaking();
        scene.idle(30);

        scene.overlay().showText(60)
                .text("Sneak-right-click the service bell inside the inn to toggle business status")
                .pointAt(util.vector().of(0.5, 2, 2.5));
        scene.idle(60);
        scene.markAsFinished();
    }

    public static void roomRegister(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("room_register", "Room Register");
        scene.addKeyframe();
        scene.world().showSection(util.select().everywhere(), Direction.UP);
        scene.idle(20);

        scene.overlay().showText(50)
                .text("Hold the room register in your offhand, then right-click two corners")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().of(2.5, 1, 2.5));
        scene.idle(60);

        scene.overlay().showControls(util.vector().of(3.5, 1, 0.5), Pointing.DOWN, 20)
                .rightClick()
                .withItem(new ItemStack(ModItems.ROOM_REGISTER.get()));
        scene.idle(10);
        scene.overlay().showControls(util.vector().of(0.5, 4.5, 4), Pointing.RIGHT, 20)
                .rightClick()
                .withItem(new ItemStack(ModItems.ROOM_REGISTER.get()));
        scene.addKeyframe();

        Selection roomArea = roomArea(util);
        scene.overlay().showOutline(PonderPalette.GREEN, "room_area", roomArea, 80);
        scene.idle(60);

        scene.overlay().showText(30)
                .text("Each room must contain at least one bed...")
                .pointAt(util.vector().of(3, 1.5, 2.5));
        scene.idle(45);

        scene.overlay().showText(60)
                .text("...and one door")
                .pointAt(util.vector().of(1.5, 1.5, 4));
        scene.idle(20);
        scene.markAsFinished();
    }

    public static void roomBinding(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("room_binding", "Room Binding");
        scene.world().showSection(util.select().everywhere(), Direction.UP);
        scene.idle(20);

        scene.overlay().showOutline(PonderPalette.WHITE, "room_area", roomArea(util), 80);
        scene.overlay().showControls(util.vector().of(1.5, 1, 1.5), Pointing.DOWN, 60)
                .rightClick()
                .withItem(new ItemStack(ModItems.ROOM_KEY.get()));
        scene.idle(20);

        scene.overlay().showText(40)
                .text("Right-click a room while holding an empty room key to bind it")
                .pointAt(util.vector().of(1.5, 1, 1.5));
        scene.idle(40);
        scene.markAsFinished();
    }

    private static Selection roomArea(SceneBuildingUtil util) {
        return util.select().fromTo(0, 1, 0, 3, 4, 3);
    }

    private static Selection greenhousePreviewSection(SceneBuildingUtil util) {
        return util.select().fromTo(0, 0, 0, GREENHOUSE_DEMO_MAX_X, 10, GREENHOUSE_DEMO_SPLIT_Z - 1);
    }

    private static Selection greenhouseFacilitySection(SceneBuildingUtil util) {
        return util.select().fromTo(0, 0, GREENHOUSE_DEMO_SPLIT_Z, GREENHOUSE_DEMO_MAX_X, 10, GREENHOUSE_DEMO_SPLIT_Z + 24);
    }

    private static ItemLike createDepot() {
        return block("create", "depot");
    }

    private static Item createWrench() {
        return item("create:wrench");
    }

    private static Block block(String namespace, String path) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    private static Item item(String id) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
    }

    private static ItemStack stack(String id) {
        return new ItemStack(item(id));
    }

    private static Vec3 centerOf(SceneBuildingUtil util, BlockPos pos) {
        return util.vector().of(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
    }

    private static void mergeNbt(CompoundTag target, String nbtLiteral) {
        try {
            target.merge(TagParser.parseTag(nbtLiteral));
        } catch (CommandSyntaxException e) {
            throw new IllegalStateException("Failed to parse Ponder NBT: " + nbtLiteral, e);
        }
    }
}
