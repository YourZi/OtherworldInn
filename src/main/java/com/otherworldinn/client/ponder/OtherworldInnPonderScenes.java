package com.otherworldinn.client.ponder;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.otherworldinn.init.ModItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
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

public final class OtherworldInnPonderScenes {
    private static final String DEPOT_HELD_ITEM_NBT =
            "{HeldItem:{Angle:272,InDirection:5,InSegment:0,Item:{count:16,id:\"kaleidoscope_cookery:sweet_and_sour_ender_pearls\"},Offset:0.0f,Pos:0.49812075f,PrevOffset:0.0f,PrevPos:0.49812075f},OutputBuffer:{Items:[],Size:8}}";

    private OtherworldInnPonderScenes() {}

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemLike> itemHelper =
                helper.withKeyFunction(itemLike -> BuiltInRegistries.ITEM.getKey(itemLike.asItem()));

        itemHelper.forComponents(createDepot())
                .addStoryBoard("basic", OtherworldInnPonderScenes::depot, OtherworldInnPonderTags.INN_SYSTEM);

        itemHelper.forComponents(Items.WOODEN_HOE, Items.STONE_HOE, Items.IRON_HOE)
                .addStoryBoard(
                        "greenhouse",
                        OtherworldInnPonderScenes::greenhouse,
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
                        OtherworldInnPonderTags.INN_SYSTEM)
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
        scene.world().showSection(util.select().everywhere(), Direction.UP);
        scene.idle(20);

        scene.overlay().showText(40)
                .text("Crops planted in the greenhouse grow faster")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().of(6.5, 8.5, 0));
        scene.idle(60);

        scene.overlay().showText(60)
                .text("The growth multiplier increases with greenhouse level and can exceed 3x at max level")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().of(6.5, 8.5, 0));
        scene.idle(80);

        scene.overlay().showText(60)
                .text("Blocks inside the greenhouse can be changed freely, so build any layout you like")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().of(9.5, 1, 2.5));
        scene.idle(60);
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

    private static ItemLike createDepot() {
        return block("create", "depot");
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

    private static void mergeNbt(CompoundTag target, String nbtLiteral) {
        try {
            target.merge(TagParser.parseTag(nbtLiteral));
        } catch (CommandSyntaxException e) {
            throw new IllegalStateException("Failed to parse Ponder NBT: " + nbtLiteral, e);
        }
    }
}
