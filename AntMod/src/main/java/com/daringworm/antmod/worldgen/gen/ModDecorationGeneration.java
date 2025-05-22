package com.daringworm.antmod.worldgen.gen;

import com.daringworm.antmod.worldgen.feature.PlaceAntFeaturesHolders;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.world.BiomeLoadingEvent;

import java.util.Objects;
import java.util.Set;

public class ModDecorationGeneration {
    public static void generateTrees(final BiomeLoadingEvent event) {
        //event.getGeneration().addFeature(GenerationStep.Decoration.RAW_GENERATION, PlaceAntFeaturesHolders.TEST_ANT_COLONY_PLACED);
    }

    public static void generateColonies(final BiomeLoadingEvent event){
        //event.getGeneration().addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, PlaceAntFeaturesHolders.)
    }
}
