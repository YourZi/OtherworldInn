package com.otherworldinn.mixin;

import com.otherworldinn.item.CreativeBlueprintItem;
import com.simibubi.create.content.schematics.table.SchematicTableBlockEntity;
import com.simibubi.create.content.schematics.table.SchematicTableMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SchematicTableMenu.class)
public class MixinSchematicTableCreativeBlueprint {

    @Shadow
    @Final
    private SchematicTableBlockEntity contentHolder;

    @Shadow
    private Slot inputSlot;

    @Shadow
    public Player player;

    @Shadow
    public NonNullList<Slot> slots;

    /**
     * 替换蓝图桌输入槽，允许放入创造蓝图。
     */
    @Inject(method = "addSlots", at = @At("TAIL"))
    private void otherworldinn$acceptCreativeBlueprintInSlot(CallbackInfo ci) {
        inputSlot = new SlotItemHandler(contentHolder.inventory, 0, 21, 59) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return super.mayPlace(stack)
                        || stack.getItem() instanceof CreativeBlueprintItem;
            }
        };
        slots.set(0, inputSlot);
        inputSlot.index = 0;
    }

    /**
     * 创造蓝图在蓝图桌中，只有创造模式的玩家才能写入。
     */
    @Inject(method = "canWrite", at = @At("RETURN"), cancellable = true)
    private void otherworldinn$blockNonCreativeWrite(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        if (!inputSlot.hasItem()) return;
        if (!(inputSlot.getItem().getItem() instanceof CreativeBlueprintItem)) return;

        if (player != null && !player.isCreative()) {
            cir.setReturnValue(false);
        }
    }
}
