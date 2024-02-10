package com.daringworm.antmod.worldgen.feature.registries;

import com.daringworm.antmod.AntMod;
import com.daringworm.antmod.worldgen.feature.custom.ColonyFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ColonyFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, AntMod.MOD_ID);

    public static final RegistryObject<ColonyFeature> ANT_COLONY_BASE_REGISTER = FEATURES.register("ant_geode",
            () -> new ColonyFeature(NoneFeatureConfiguration.CODEC));

    public static void register(IEventBus eventBus) {FEATURES.register(eventBus);}
}
