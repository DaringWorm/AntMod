package com.daringworm.antmod.worldgen.feature;

import com.daringworm.antmod.worldgen.feature.registries.AntCarversReg;
import com.daringworm.antmod.worldgen.feature.registries.AntFeaturesReg;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.TrapezoidFloat;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;

public class SpawnAntFeaturesHolders {

    public static final Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> GEODE_SPAWN =
            FeatureUtils.register("ant_geode", AntFeaturesReg.GEODE_BASE_REGISTER.get(),
                    NoneFeatureConfiguration.INSTANCE);

    /*public static final Holder<ConfiguredWorldCarver<CanyonCarverConfiguration>> COLONY_SPAWN =
            FeatureUtils.register("ant_colony", new ConfiguredWorldCarver<CanyonCarverConfiguration>( AntCarversReg.COLONY_BASE_REGISTER.get(), new CanyonCarverConfiguration(0.01F, UniformHeight.of(VerticalAnchor.absolute(10), VerticalAnchor.absolute(67)), ConstantFloat.of(3.0F), VerticalAnchor.aboveBottom(8), CarverDebugSettings.of(false, Blocks.WARPED_BUTTON.defaultBlockState()), UniformFloat.of(-0.125F, 0.125F), new CanyonCarverConfiguration.CanyonShapeConfiguration(UniformFloat.of(0.75F, 1.0F), TrapezoidFloat.of(0.0F, 6.0F, 2.0F), 3, UniformFloat.of(0.75F, 1.0F), 1.0F, 0.0F))));

*/

}
