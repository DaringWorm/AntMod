package com.daringworm.antmod.worldgen.feature.registries;

import com.daringworm.antmod.AntMod;
import com.daringworm.antmod.worldgen.feature.custom.AntColonyEntranceFeature;
import com.daringworm.antmod.worldgen.feature.custom.TestAntColony;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AntFeaturesReg {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, AntMod.MOD_ID);


    public static final RegistryObject<AntColonyEntranceFeature> GEODE_BASE_REGISTER = FEATURES.register("ant_geode",
            () -> new AntColonyEntranceFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<TestAntColony> TEST_ANT_COLONY = FEATURES.register("test_ant_colony",
            () -> new TestAntColony(NoneFeatureConfiguration.CODEC));

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}
