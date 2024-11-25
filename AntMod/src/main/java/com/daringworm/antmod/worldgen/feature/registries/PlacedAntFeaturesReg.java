package com.daringworm.antmod.worldgen.feature.registries;

import com.daringworm.antmod.AntMod;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class PlacedAntFeaturesReg {
    public static final DeferredRegister<PlacedFeature> PLACED_FEATURES = DeferredRegister.create(Registry.PLACED_FEATURE_REGISTRY, AntMod.MOD_ID);

    public static final RegistryObject<PlacedFeature> ANT_GEODE_PLACED = PLACED_FEATURES.register("ant_geode",
            () -> new PlacedFeature(ConfiguredAntFeaturesReg.GEODE_CONFIGURED_REGISTER.getHolder().get(),
                    VegetationPlacements.treePlacement(PlacementUtils.countExtra(3, 0.1f, 2))));


    public static void register(IEventBus bus) {
        PLACED_FEATURES.register(bus);
    }
}
