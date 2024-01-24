package com.daringworm.antmod.util;

import com.daringworm.antmod.AntMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;


public class ModTags{
    public static class Blocks {
        public static final TagKey<Block> ANTMOD_PRIMARY_TAG
                = tag("antmod_primary_tag");

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(new ResourceLocation(AntMod.MOD_ID, name));
        }

        private static TagKey<Block> forgeTag(String name) {
            return BlockTags.create(new ResourceLocation("forge", name));
        }

        public static class Items {

        }

    }
}
