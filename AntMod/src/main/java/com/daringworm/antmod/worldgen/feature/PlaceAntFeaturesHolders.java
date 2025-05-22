package com.daringworm.antmod.worldgen.feature;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.placement.*;

public class PlaceAntFeaturesHolders {

    public static final Holder<PlacedFeature> GEODE_PLACED = PlacementUtils.register("ant_geode",
            SpawnAntFeaturesHolders.GEODE_SPAWN, RarityFilter.onAverageOnceEvery(100), InSquarePlacement.spread(),
            PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());

    public static final Holder<PlacedFeature> TEST_ANT_COLONY_PLACED = PlacementUtils.register("test_ant_colony",
            SpawnAntFeaturesHolders.TEST_ANT_COLONY_SPAWN, BlockPredicateFilter.forPredicate(BlockPredicate.alwaysTrue()),
            InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_TOP_SOLID, BiomeFilter.biome());

    public static final Holder<PlacedFeature> COLONY_STRUCTURE_PLACED = PlacementUtils.register("ant_structure",
            SpawnAntFeaturesHolders.TEST_ANT_COLONY_SPAWN, BlockPredicateFilter.forPredicate(BlockPredicate.alwaysTrue()),
            InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_TOP_SOLID, BiomeFilter.biome());

}