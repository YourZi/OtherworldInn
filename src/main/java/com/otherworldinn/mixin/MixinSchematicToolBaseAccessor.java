package com.otherworldinn.mixin;

import com.simibubi.create.content.schematics.client.SchematicHandler;
import com.simibubi.create.content.schematics.client.tools.SchematicToolBase;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SchematicToolBase.class)
public interface MixinSchematicToolBaseAccessor {
    @Accessor("schematicHandler")
    SchematicHandler otherworldinn$getSchematicHandler();

    @Accessor("selectedPos")
    BlockPos otherworldinn$getSelectedPos();
}
