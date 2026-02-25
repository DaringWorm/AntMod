package com.daringworm.antmod.worldgen.gen;

import com.daringworm.antmod.util.AntUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.world.ChunkEvent;

public class ModColonyGeneration {
    public static void generateColonies(final ChunkEvent event) {
        ChunkAccess chunk = event.getChunk();

        if(true) {
            int height = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, 4, 4) + 5;
            BlockPos pos = chunk.getPos().getBlockAt(4, height, 4);
        }



        /*
        ResourceKey<Biome> key = ResourceKey.create(Registry.BIOME_REGISTRY, Objects.requireNonNull(event.getName()));
        Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(key);


        if((types.contains(BiomeDictionary.Type.FOREST)
                || types.contains(BiomeDictionary.Type.JUNGLE)
                || types.contains(BiomeDictionary.Type.SAVANNA)) &&
                !types.contains(BiomeDictionary.Type.HILLS) &&
                !types.contains(BiomeDictionary.Type.PLATEAU) &&
                !types.contains(BiomeDictionary.Type.PEAK) &&
                !types.contains(BiomeDictionary.Type.MODIFIED) &&
                !types.contains(BiomeDictionary.Type.BEACH) &&
                !types.contains(BiomeDictionary.Type.OCEAN) &&
                !types.contains(BiomeDictionary.Type.MAGICAL) &&
                !types.contains(BiomeDictionary.Type.RIVER) &&
                !types.contains(BiomeDictionary.Type.DENSE)
        ) {
            event.getGeneration().addCarver(GenerationStep.Carving.AIR, AntCarversReg.CONFIGURED_COLONY_REGISTER.getHolder().get());
        }



         */
    }



}
