package com.daringworm.antmod.worldgen.feature.custom;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.colony.AntColony;
import com.daringworm.antmod.colony.ColonyGenerationBuffer;
import com.daringworm.antmod.colony.misc.ColonyBranch;
import com.daringworm.antmod.colony.misc.PosSpherePair;
import com.daringworm.antmod.goals.AntUtils;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

public class AntColonyCarver<C extends CarverConfiguration> extends WorldCarver<AntColonyConfiguration> {

    private static final double WALL_THICKNESS = 1.2d;

    public AntColonyCarver(Codec<AntColonyConfiguration> p_64711_) {
        super(p_64711_);
    }

    public boolean isStartChunk(AntColonyConfiguration pConfig, Random pRandom) {
        return pRandom.nextFloat() <= pConfig.probability;
    }

    /**
     * Carves the given chunk with caves that originate from the given {@code chunkPos}.
     * This method is invoked 289 times in order to generate each chunk (once for every position in an 8 chunk radius, or
     * 17x17 chunk area, centered around the target chunk).
     *
     * @see net.minecraft.world.level.chunk.ChunkGenerator#applyCarvers
     * @param pChunk The chunk to be carved
     * @param pChunkPos The chunk position this carver is being called from
     */
    @Override
    public boolean carve(CarvingContext pContext, AntColonyConfiguration pConfig, ChunkAccess pChunk, Function<BlockPos, Holder<Biome>> pBiomeAccessor, Random pRandom, Aquifer pAquifer, ChunkPos pChunkPos, CarvingMask pCarvingMask) {

        BlockPos startPosAbsolute = pChunkPos.getMiddleBlockPosition(63);

        ColonyBranch branch = ColonyGenerationBuffer.getBranchForPos(startPosAbsolute);
        if(branch == null){branch = AntColony.generateNewTunnels(startPosAbsolute);}

        ArrayList<PosSpherePair> masterArray = AntColony.generateNewColonyBlueprint(branch);

        for(int i = 0; i < masterArray.size(); i++){
            PosSpherePair sphere = masterArray.get(i);
            ChunkPos cPos = pChunk.getPos();
            BlockPos bPos = sphere.centerPos;
            BlockPos cbPos = cPos.getMiddleBlockPosition(bPos.getY());
            if(AntUtils.getDist(bPos,cbPos) <= 8+sphere.radius+(WALL_THICKNESS*3)){
                boolean replaceAir = (i >= 24);
                sphere.setSphereCarver(pChunk, ModBlocks.ANT_AIR.get().defaultBlockState(), ModBlocks.ANT_DIRT.get().defaultBlockState(),WALL_THICKNESS, replaceAir);
            }
        }

        /*
        Sets the block and colony buffer to be checked by ServerLevelMixins.
        The Mixin will check for the block at all the Buffer locations every game tick,
        and make and save a new colony if it finds a match.

        The buffer is static, so there could be a mismatch between servers or levels,
        but I'm counting on that being extremely unlikely since the start positions of
        both colonies would need to match perfectly.
        */

        if(Objects.equals(pChunk.getPos(), pChunkPos)) {
            pChunk.setBlockState(startPosAbsolute, Blocks.REDSTONE_BLOCK.defaultBlockState(), false);
        }

        ColonyGenerationBuffer.tryToAdd(branch);

        return true;
    }





    private void doCarve(CarvingContext pContext, AntColonyConfiguration pConfig, ChunkAccess pChunk, Function<BlockPos, Holder<Biome>> pBiomeAccessor, long pSeed, Aquifer pAquifer, double pX, double pY, double pZ, float pThickness, float pYaw, float pPitch, int pBranchIndex, int pBranchCount, double pHorizontalVerticalRatio, CarvingMask pCarvingMask) {
        Random random = new Random(pSeed);
        float f = 0.0F;
        float f1 = 0.0F;

        for(int i = pBranchIndex; i < pBranchCount; ++i) {
            double d0 = 1.5D + (double)(Mth.sin((float)i * (float)Math.PI / (float)pBranchCount) * pThickness);
            double d1 = d0 * pHorizontalVerticalRatio;
            float f2 = Mth.cos(pPitch);
            float f3 = Mth.sin(pPitch);
            pX += (double)(Mth.cos(pYaw) * f2);
            pY += (double)f3;
            pZ += (double)(Mth.sin(pYaw) * f2);
            pPitch *= 0.7F;
            pPitch += f1 * 0.05F;
            pYaw += f * 0.05F;
            f1 *= 0.8F;
            f *= 0.5F;
            f1 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            f += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
            if (random.nextInt(4) != 0) {
                if (!canReach(pChunk.getPos(), pX, pZ, i, pBranchCount, pThickness)) {
                    return;
                }


            }
        }

    }

    /**
     * To be used in place of carveBlock in the WorldCarver supertype.
     * This method does not mark blocks for flooding in the next world generation steps.
     * **/

    private boolean setBlock(ChunkAccess pChunk, BlockPos.MutableBlockPos pPos, BlockState pState) {
        pChunk.setBlockState(pPos, pState, false);
        return true;
    }


    private boolean shouldSkip(CarvingContext pContext, float[] pWidthFactors, double pRelativeX, double pRelativeY, double pRelativeZ, int pY) {
        return false;
    }
}
