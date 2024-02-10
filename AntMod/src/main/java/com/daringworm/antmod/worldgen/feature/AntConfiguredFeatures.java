package com.daringworm.antmod.worldgen.feature;

import com.daringworm.antmod.worldgen.feature.registries.ColonyFeatures;
import com.daringworm.antmod.worldgen.feature.registries.ConfiguredFeatures;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class AntConfiguredFeatures {

    public static final Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> ANT_GEODE =
            FeatureUtils.register("ant_geode", ColonyFeatures.ANT_COLONY_BASE_REGISTER.get());

    public static final Holder<PlacedFeature> GEODE_CHECKED = PlacementUtils.register("ant_geode",
            ConfiguredFeatures.ANT_COLONY_CONFIGURED_REGISTER.getHolder().get());

    public static final Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> ANT_COLONY_SPAWN =
            FeatureUtils.register("ant_geode", ColonyFeatures.ANT_COLONY_BASE_REGISTER.get(),
                    NoneFeatureConfiguration.INSTANCE);
}
