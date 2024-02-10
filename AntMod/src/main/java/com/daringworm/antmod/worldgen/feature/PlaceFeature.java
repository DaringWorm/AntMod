package com.daringworm.antmod.worldgen.feature;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class PlaceFeature {

    public static final Holder<PlacedFeature> COLONY_PLACED = PlacementUtils.register("ant_geode",
            AntConfiguredFeatures.ANT_COLONY_SPAWN, VegetationPlacements.treePlacement(
                    PlacementUtils.countExtra(3, 0.1f, 2)));
}