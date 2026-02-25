package com.daringworm.antmod.worldgen.feature.custom;

import com.daringworm.antmod.colony.AntColony;
import com.daringworm.antmod.colony.misc.AntSphere;
import com.daringworm.antmod.mixin.tomixin.ServerLevelUtil;
import com.daringworm.antmod.worldgen.feature.registries.AntStructuresReg;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.*;

import java.util.Optional;
import java.util.Random;

public class AntColonyStructure extends StructureFeature<NoneFeatureConfiguration> {
    public AntColonyStructure(Codec<NoneFeatureConfiguration> codec) {
        super(codec,
                AntColonyStructure::pieceGeneratorSupplier,
                AntColonyStructure::afterPlace);
    }





    public static class AntColonyPiece extends StructurePiece{
        private final BlockPos originPos;

        public AntColonyPiece(StructurePieceType p_209994_, int p_209995_, BoundingBox p_209996_, BlockPos origin) {
            super(p_209994_, p_209995_, p_209996_);
            this.originPos = origin;
        }

        public AntColonyPiece(StructurePieceSerializationContext context, CompoundTag tag) {
            super(AntStructuresReg.ANT_COLONY_PIECE, tag);

            if(tag.contains("OriginX")) {
                this.originPos = new BlockPos(
                        tag.getInt("OriginX"),
                        tag.getInt("OriginY"),
                        tag.getInt("OriginZ")
                );
            }
            else{this.originPos = BlockPos.ZERO;}
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
            tag.putInt("OriginX", originPos.getX());
            tag.putInt("OriginY", originPos.getY());
            tag.putInt("OriginZ", originPos.getZ());
        }

        @Override
        public void postProcess(WorldGenLevel levelWG, StructureFeatureManager manager, ChunkGenerator chunkGenerator, Random random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
            ServerLevel level = levelWG.getLevel();

            AntColony colony = ((ServerLevelUtil)level).getOrCreateColonyForPos(this.originPos);

            AntSphere sphere = new AntSphere(this.originPos, 5d);
            for(BlockPos tempPos : sphere){
                if(boundingBox.isInside(tempPos)) {
                    levelWG.setBlock(tempPos, Blocks.GLASS.defaultBlockState(), 16);
                }
            }

            if(boundingBox.isInside(pos)) {
                levelWG.setBlock(pos, Blocks.GLOWSTONE.defaultBlockState(), 16);
            }

            colony.generateWG(levelWG, box, chunkPos);

        }
    }

    private static Optional<PieceGenerator<NoneFeatureConfiguration>> pieceGeneratorSupplier(
            PieceGeneratorSupplier.Context<NoneFeatureConfiguration> context) {


        //Need this for later. Just determines where the middle of the colony will be (the entrance):

        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();

        int y = context.chunkGenerator().getFirstOccupiedHeight(
                x, z,
                Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor()
        );

        BlockPos origin = new BlockPos(x, y, z);

        //Quick filter to avoid blatantly invalid spawns:
        int seaLevel = context.chunkGenerator().getSeaLevel();

        if (!context.validBiomeOnTop(Heightmap.Types.WORLD_SURFACE_WG) || y <= seaLevel + 2 || y > (int)(seaLevel * 1.2)) {
            return Optional.empty();
        }

        //Check the generation noise values to avoid ocean spawns directly:

        if (context.chunkGenerator() instanceof NoiseBasedChunkGenerator noiseGenerator) {
            Climate.Sampler sampler = noiseGenerator.climateSampler();
            Climate.TargetPoint target = sampler.sample(x, y, z);

            float erosion = Climate.unquantizeCoord(target.erosion());
            float continentalness = Climate.unquantizeCoord(target.continentalness());

            // Example: reject oceans (low continentalness) and mountains (low erosion)
            if (continentalness < 0.08f || erosion < 0.0f) {
                return Optional.empty();
            }
        }


        return Optional.of((builder, generatorContext) -> {

            int radius = 150;

            BoundingBox entireStructureBounds = new BoundingBox(
                    origin.getX()-radius, origin.getY(), origin.getZ()-radius,
                    origin.getX() + radius, origin.getY(), origin.getZ() + radius
            );

            builder.addPiece(new AntColonyPiece(
                    AntStructuresReg.ANT_COLONY_PIECE,
                    0,
                    entireStructureBounds,
                    origin
            ));
        });
    }


    private static void afterPlace(WorldGenLevel level_wg, StructureFeatureManager featureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, PiecesContainer pieces_dontUse) {
        int edge_len = 16;

        /*for(BlockPos pos : BlockPos.randomInCube(random, edge_len * edge_len, chunkPos.getBlockAt(0, 120, 0), edge_len)){
            if(boundingBox.isInside(pos)) {
                level_wg.setBlock(pos, Blocks.REDSTONE_BLOCK.defaultBlockState(), 16);
            }
        }*/
    }

    @Override
    public GenerationStep.Decoration step() {
        return GenerationStep.Decoration.SURFACE_STRUCTURES;
    }
}
