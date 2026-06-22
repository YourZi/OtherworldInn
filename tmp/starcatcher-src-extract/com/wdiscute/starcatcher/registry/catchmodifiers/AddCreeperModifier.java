package com.wdiscute.starcatcher.registry.catchmodifiers;

import com.wdiscute.starcatcher.U;
import com.wdiscute.starcatcher.registry.FishProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;

import java.util.List;

import static com.wdiscute.starcatcher.registry.fishing.FishingPropertiesRegistry.fish;

public class AddCreeperModifier extends AbstractCatchModifier
{
    @Override
    public List<FishProperties> modifyAvailablePool(List<FishProperties> available)
    {
        if (U.r.nextInt(25) == 6)
            return List.of(fish(BuiltInRegistries.ITEM.wrapAsHolder(Items.CREEPER_HEAD))
                    .withAlwaysSpawnEntity(true)
                    .withEntityToSpawn(EntityType.CREEPER.builtInRegistryHolder())
                    .withSkipMinigame(true)
                    .build());
        return super.modifyAvailablePool(available);
    }
}
