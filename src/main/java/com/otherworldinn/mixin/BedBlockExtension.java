package com.otherworldinn.mixin;

import net.minecraft.world.level.block.state.properties.BooleanProperty;

/** BedBlock 扩展接口，提供对 MESSY 属性的全局访问。 */
public interface BedBlockExtension {
    BooleanProperty MESSY = BooleanProperty.create("messy");
}
