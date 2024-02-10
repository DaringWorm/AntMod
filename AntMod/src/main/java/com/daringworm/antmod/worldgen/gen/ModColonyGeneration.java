package com.daringworm.antmod.worldgen.gen;

import com.daringworm.antmod.worldgen.feature.PlaceAntFeaturesHolders;
import com.daringworm.antmod.worldgen.feature.registries.AntCarversReg;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.world.BiomeLoadingEvent;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ModColonyGeneration {
    public static void generateColonies(final BiomeLoadingEvent event) {
        ResourceKey<Biome> key = ResourceKey.create(Registry.BIOME_REGISTRY, Objects.requireNonNull(event.getName()));
        Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(key);

        if(types.contains(BiomeDictionary.Type.SAVANNA)) {
            List<Holder<PlacedFeature>> base = event.getGeneration().getFeatures(GenerationStep.Decoration.VEGETAL_DECORATION);
            base.add(PlaceAntFeaturesHolders.GEODE_PLACED);

        }

        if(types.contains(BiomeDictionary.Type.PLAINS)) {
            event.getGeneration().addCarver(GenerationStep.Carving.AIR, AntCarversReg.CONFIGURED_COLONY_REGISTER.getHolder().get());
        }
    }
}
