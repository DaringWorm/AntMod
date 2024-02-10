package com.daringworm.antmod.worldgen.feature;

import com.daringworm.antmod.worldgen.feature.registries.AntCarversReg;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

public class PlaceAntFeaturesHolders {

    public static final Holder<PlacedFeature> GEODE_PLACED = PlacementUtils.register("ant_geode",
            SpawnAntFeaturesHolders.GEODE_SPAWN, RarityFilter.onAverageOnceEvery(100), InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());

}