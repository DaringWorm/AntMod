package com.daringworm.antmod.worldgen.feature.custom;

import com.mojang.serialization.Codec;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import java.util.List;

public class AntGeodeConfiguration implements FeatureConfiguration {
    public static final Codec<AntGeodeConfiguration> CODEC = Codec.unit(() -> AntGeodeConfiguration.INSTANCE);
    public static final AntGeodeConfiguration INSTANCE = new AntGeodeConfiguration();

    public final GeodeBlockSettings geodeBlockSettings;
    public final GeodeLayerSettings geodeLayerSettings;
    public final GeodeCrackSettings geodeCrackSettings;
    public final double usePotentialPlacementsChance;
    public final double useAlternateLayer0Chance;
    public final boolean placementsRequireLayer0Alternate;
    public final IntProvider outerWallDistance;
    public final IntProvider distributionPoints;
    public final IntProvider pointOffset;
    public final int minGenOffset;
    public final int maxGenOffset;
    public final double noiseMultiplier;
    public final int invalidBlocksThreshold;

    public AntGeodeConfiguration() {
        this.geodeBlockSettings = new GeodeBlockSettings(BlockStateProvider.simple(Blocks.AIR), BlockStateProvider.simple(Blocks.GLOWSTONE),
                BlockStateProvider.simple(Blocks.BUDDING_AMETHYST), BlockStateProvider.simple(Blocks.DIAMOND_BLOCK),
                BlockStateProvider.simple(Blocks.GLASS), List.of(Blocks.SMALL_AMETHYST_BUD.defaultBlockState(),
                Blocks.MEDIUM_AMETHYST_BUD.defaultBlockState(), Blocks.LARGE_AMETHYST_BUD.defaultBlockState(),
                Blocks.AMETHYST_CLUSTER.defaultBlockState()), BlockTags.FEATURES_CANNOT_REPLACE,
                BlockTags.GEODE_INVALID_BLOCKS);
        this.geodeLayerSettings = new GeodeLayerSettings(1.7D, 2.2D, 3.2D, 4.2D);
        this.geodeCrackSettings = new GeodeCrackSettings(0.95D, 2.0D, 2);
        this.usePotentialPlacementsChance = 0.35D;
        this.useAlternateLayer0Chance = 0.083D;
        this.placementsRequireLayer0Alternate = true;
        this.outerWallDistance = UniformInt.of(4, 6);
        this.distributionPoints = UniformInt.of(3, 4);
        this.pointOffset = UniformInt.of(1, 2);
        this.minGenOffset = -16;
        this.maxGenOffset = 16;
        this.noiseMultiplier = 0.05D;
        this.invalidBlocksThreshold = 1;
    }

}
