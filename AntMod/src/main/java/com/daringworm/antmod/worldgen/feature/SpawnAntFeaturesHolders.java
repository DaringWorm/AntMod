package com.daringworm.antmod.worldgen.feature;

import com.daringworm.antmod.worldgen.feature.registries.AntFeaturesReg;
import com.daringworm.antmod.worldgen.feature.registries.AntStructuresReg;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;


public class SpawnAntFeaturesHolders {

    public static final Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> GEODE_SPAWN =
            FeatureUtils.register("ant_geode", AntFeaturesReg.GEODE_BASE_REGISTER.get(),
                    NoneFeatureConfiguration.INSTANCE);

    public static final Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> TEST_ANT_COLONY_SPAWN =
            FeatureUtils.register("test_ant_colony_spawn", AntFeaturesReg.TEST_ANT_COLONY.get(),
                    NoneFeatureConfiguration.INSTANCE);



}
