package com.daringworm.antmod.worldgen.feature.registries;

import com.daringworm.antmod.AntMod;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ConfiguredAntFeaturesReg {
    public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES = DeferredRegister.create(Registry.CONFIGURED_FEATURE_REGISTRY, AntMod.MOD_ID);

    public static final RegistryObject<ConfiguredFeature<?,?>> GEODE_CONFIGURED_REGISTER = CONFIGURED_FEATURES.register("ant_geode",
            () -> new ConfiguredFeature<>(AntFeaturesReg.GEODE_BASE_REGISTER.get(), NoneFeatureConfiguration.INSTANCE));

    public static void register(IEventBus bus) {
        CONFIGURED_FEATURES.register(bus);
    }
}
