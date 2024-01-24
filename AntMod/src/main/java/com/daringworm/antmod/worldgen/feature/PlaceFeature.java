package com.daringworm.antmod.worldgen.feature;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class PlaceFeature {
    public static final Holder<PlacedFeature> PEACH_PLACED = PlacementUtils.register("peach_placed",
            AntConfiguredFeatures.PEACH_SPAWN, VegetationPlacements.treePlacement(
                    PlacementUtils.countExtra(3, 0.1f, 2)));

    /*public static final Holder<PlacedFeature> ANT_COLONY_PLACED = PlacementUtils.register("ant_colony_placed",
            AntConfiguredFeatures.ANT_COLONY_SPAWN, VegetationPlacements.treePlacement(
                    PlacementUtils.countExtra(1, 0.1f, 1)));*/
}