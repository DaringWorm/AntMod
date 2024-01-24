package com.daringworm.antmod.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeModeTab {

    public static final CreativeModeTab ANT_MOD_CTAB = new CreativeModeTab("antmodctab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModItems.FUNGUS.get());
        }
    };

}
