package com.daringworm.antmod.mixin.mixins;

import com.daringworm.antmod.mixin.tomixin.ItemClassToMixin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemClassMixins implements ItemClassToMixin {
    /*private static final String TAG_DESPAWNABLE = "Despawnable";

    @Override
    public boolean getIsDespawnable(ItemStack pStack) {
        return pStack.
    }

    @Inject(at = @At("HEAD"), method = "save", cancellable = false)
    public void save(CompoundTag pCompoundTag, CallbackInfoReturnable<CompoundTag> cir) {
        pCompoundTag.put(TAG_DESPAWNABLE, this.getIsDespawnable());
    }*/

}

