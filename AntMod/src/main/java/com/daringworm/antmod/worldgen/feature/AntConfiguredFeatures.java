package com.daringworm.antmod.worldgen.feature;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.worldgen.feature.colony.ColonyConfiguration;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;

public class AntConfiguredFeatures {
    public static final Holder<ConfiguredFeature<TreeConfiguration, ?>> PEACH_TREE =
            FeatureUtils.register("peach", Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                    BlockStateProvider.simple(ModBlocks.PEACH_WOOD.get()),
                    new StraightTrunkPlacer(5, 6, 3),
                    BlockStateProvider.simple(ModBlocks.PEACH_LEAVES.get()),
                    new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 4),
                    new TwoLayersFeatureSize(1, 0, 2)).build());

    /*public static final Holder<ConfiguredFeature<ColonyConfiguration, ?>> ANT_COLONY =
            FeatureUtils.register("ant_colony", Feature.TREE, new ColonyConfiguration.ColonyConfigurationBuilder(
                    BlockStateProvider.simple(Blocks.REDSTONE_BLOCK),
                    new StraightTrunkPlacer(5, 6, 3),
                    BlockStateProvider.simple(Blocks.DIAMOND_BLOCK),
                    new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 4),
                    new TwoLayersFeatureSize(1, 0, 2)).build());*/

    public static final Holder<PlacedFeature> PEACH_CHECKED = PlacementUtils.register("peach_checked", PEACH_TREE,
            PlacementUtils.filteredByBlockSurvival(ModBlocks.PEACH_SAPLING.get()));

    /*public static final Holder<PlacedFeature> COLONY_CHECKED = PlacementUtils.register("colony_checked", ANT_COLONY,
            PlacementUtils.filteredByBlockSurvival(ModBlocks.COLONY_PLACER.get()));
*/
    public static final Holder<ConfiguredFeature<RandomFeatureConfiguration, ?>> PEACH_SPAWN =
            FeatureUtils.register("peach_spawn", Feature.RANDOM_SELECTOR,
                    new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(PEACH_CHECKED,
                            0.5F)), PEACH_CHECKED));
/*
    public static final Holder<ConfiguredFeature<RandomFeatureConfiguration, ?>> ANT_COLONY_SPAWN =
            FeatureUtils.register("ant_colony_spawn", Feature.RANDOM_SELECTOR,
                    new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(COLONY_CHECKED,
                            0.2F)), COLONY_CHECKED));
*/

}
