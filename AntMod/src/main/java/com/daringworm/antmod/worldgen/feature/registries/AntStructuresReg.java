package com.daringworm.antmod.worldgen.feature.registries;

import com.daringworm.antmod.AntMod;
import com.daringworm.antmod.worldgen.feature.custom.AntColonyEntranceFeature;
import com.daringworm.antmod.worldgen.feature.custom.AntColonyStructure;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AntStructuresReg {
    public static final DeferredRegister<StructureFeature<?>> STRUCTURE_FEATURES =
            DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES, AntMod.MOD_ID);


    public static final RegistryObject<AntColonyStructure> COLONY_BASE_REGISTER = STRUCTURE_FEATURES.register("ant_colony",
            () -> new AntColonyStructure(NoneFeatureConfiguration.CODEC));

    public static StructurePieceType ANT_COLONY_PIECE;

    public static void register(IEventBus eventBus) {
        STRUCTURE_FEATURES.register(eventBus);


    }

    public static void registerPieces(){
        ANT_COLONY_PIECE = Registry.register(
                Registry.STRUCTURE_PIECE,
                new ResourceLocation(AntMod.MOD_ID, "ant_colony"),
                AntColonyStructure.AntColonyPiece::new
        );
    }
}
