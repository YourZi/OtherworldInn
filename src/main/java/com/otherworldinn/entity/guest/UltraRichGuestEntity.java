package com.otherworldinn.entity.guest;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.base.GuestEntity;
import com.otherworldinn.util.ClientServices;
import com.otherworldinn.entity.base.VipGuestEntity;
import com.otherworldinn.world.inn.GuestData;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class UltraRichGuestEntity extends GuestEntity {
    private static final ResourceLocation DEFAULT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    OtherworldInn.MODID, "textures/entity/guest/ordinary_guest/1.png");
    private static final List<ResourceLocation> TEXTURES = new ArrayList<>();
    private static boolean texturesLoaded = false;

    public UltraRichGuestEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected GuestProfile getGuestProfile() {
        return new GuestProfile(
                new PreferenceRangeProfile(new GuestData.IntRange(44, 62), new GuestData.IntRange(80, 100)),
                new PreferenceRangeProfile(new GuestData.IntRange(46, 64), new GuestData.IntRange(70, 100)),
                new PreferenceRangeProfile(new GuestData.IntRange(5, 20), new GuestData.IntRange(65, 100)),
                new GuestData.IntRange(86, 140),
                1.4D);
    }

    @Override
    public ResourceLocation getSkinTexture() {
        if (!texturesLoaded && this.level().isClientSide) {
            try {
                List<ResourceLocation> found =
                        ClientServices.findTexturesInFolder(
                                OtherworldInn.MODID, "textures/entity/guest/ultra_rich_guest");
                if (!found.isEmpty()) {
                    TEXTURES.clear();
                    TEXTURES.addAll(found);
                }
            } catch (Throwable e) {
            }
            texturesLoaded = true;
        }

        if (TEXTURES.isEmpty()) {
            return DEFAULT_TEXTURE;
        }
        return TEXTURES.get(Math.abs(this.getSkinVariant()) % TEXTURES.size());
    }

    @Override
    public String getModelType() {
        return "default";
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType reason,
            @Nullable SpawnGroupData spawnData) {
        spawnData = super.finalizeSpawn(level, difficulty, reason, spawnData);
        this.setSkinVariant(this.getRandom().nextInt(10000));
        return spawnData;
    }
}
