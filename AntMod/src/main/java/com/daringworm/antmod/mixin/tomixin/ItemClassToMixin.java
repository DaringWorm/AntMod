package com.daringworm.antmod.mixin.tomixin;

import net.minecraft.world.item.ItemStack;

public interface ItemClassToMixin {
    boolean getIsDespawnable();

    boolean getIsDespawnable(ItemStack pStack);
}
