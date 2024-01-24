package com.daringworm.antmod.worldgen.feature.colony;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;

import java.util.List;

public class ColonyConfiguration implements FeatureConfiguration {
    public static final Codec<ColonyConfiguration> CODEC = RecordCodecBuilder.create((p_191347_) -> {
        return p_191347_.group(BlockStateProvider.CODEC.fieldOf("trunk_provider").forGetter((p_161248_) -> {
            return p_161248_.trunkProvider;
        }), TrunkPlacer.CODEC.fieldOf("trunk_placer").forGetter((p_161246_) -> {
            return p_161246_.trunkPlacer;
        }), BlockStateProvider.CODEC.fieldOf("foliage_provider").forGetter((p_161244_) -> {
            return p_161244_.foliageProvider;
        }), FoliagePlacer.CODEC.fieldOf("foliage_placer").forGetter((p_191357_) -> {
            return p_191357_.foliagePlacer;
        }), BlockStateProvider.CODEC.fieldOf("dirt_provider").forGetter((p_191355_) -> {
            return p_191355_.dirtProvider;
        }), FeatureSize.CODEC.fieldOf("minimum_size").forGetter((p_191353_) -> {
            return p_191353_.minimumSize;
        }), TreeDecorator.CODEC.listOf().fieldOf("decorators").forGetter((p_191351_) -> {
            return p_191351_.decorators;
        }), Codec.BOOL.fieldOf("ignore_vines").orElse(false).forGetter((p_191349_) -> {
            return p_191349_.ignoreVines;
        }), Codec.BOOL.fieldOf("force_dirt").orElse(false).forGetter((p_161232_) -> {
            return p_161232_.forceDirt;
        })).apply(p_191347_, ColonyConfiguration::new);
    });
    
    public final BlockStateProvider trunkProvider;
    public final BlockStateProvider dirtProvider;
    public final TrunkPlacer trunkPlacer;
    public final BlockStateProvider foliageProvider;
    public final FoliagePlacer foliagePlacer;
    public final FeatureSize minimumSize;
    public final List<TreeDecorator> decorators;
    public final boolean ignoreVines;
    public final boolean forceDirt;

    protected ColonyConfiguration(BlockStateProvider p_191337_, TrunkPlacer p_191338_, BlockStateProvider p_191339_, FoliagePlacer p_191340_, BlockStateProvider p_191341_, FeatureSize p_191342_, List<TreeDecorator> p_191343_, boolean p_191344_, boolean p_191345_) {
        this.trunkProvider = p_191337_;
        this.trunkPlacer = p_191338_;
        this.foliageProvider = p_191339_;
        this.foliagePlacer = p_191340_;
        this.dirtProvider = p_191341_;
        this.minimumSize = p_191342_;
        this.decorators = p_191343_;
        this.ignoreVines = p_191344_;
        this.forceDirt = p_191345_;
    }

    public static class ColonyConfigurationBuilder {
        public final BlockStateProvider trunkProvider;
        private final TrunkPlacer trunkPlacer;
        public final BlockStateProvider foliageProvider;
        private final FoliagePlacer foliagePlacer;
        private BlockStateProvider dirtProvider;
        private final FeatureSize minimumSize;
        private List<TreeDecorator> decorators = ImmutableList.of();
        private boolean ignoreVines;
        private boolean forceDirt;

        public ColonyConfigurationBuilder(BlockStateProvider pTrunkProvider, TrunkPlacer pTrunkPlacer, BlockStateProvider pFoliageProvider, FoliagePlacer pFoliagePlacer, FeatureSize pMinimumSize) {
            this.trunkProvider = pTrunkProvider;
            this.trunkPlacer = pTrunkPlacer;
            this.foliageProvider = pFoliageProvider;
            this.dirtProvider = BlockStateProvider.simple(Blocks.DIRT);
            this.foliagePlacer = pFoliagePlacer;
            this.minimumSize = pMinimumSize;
        }

        public ColonyConfiguration.ColonyConfigurationBuilder dirt(BlockStateProvider pDirtProvider) {
            this.dirtProvider = pDirtProvider;
            return this;
        }

        public ColonyConfiguration.ColonyConfigurationBuilder decorators(List<TreeDecorator> pDecorators) {
            this.decorators = pDecorators;
            return this;
        }

        public ColonyConfiguration.ColonyConfigurationBuilder ignoreVines() {
            this.ignoreVines = true;
            return this;
        }

        public ColonyConfiguration.ColonyConfigurationBuilder forceDirt() {
            this.forceDirt = true;
            return this;
        }

        public ColonyConfiguration build() {
            return new ColonyConfiguration(this.trunkProvider, this.trunkPlacer, this.foliageProvider, this.foliagePlacer, this.dirtProvider, this.minimumSize, this.decorators, this.ignoreVines, this.forceDirt);
        }
    }
}

