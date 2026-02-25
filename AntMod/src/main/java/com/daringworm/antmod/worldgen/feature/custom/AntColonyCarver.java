package com.daringworm.antmod.worldgen.feature.custom;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.function.Function;

public class AntColonyCarver<C extends CarverConfiguration> extends WorldCarver<AntColonyConfiguration> {

    private static final double WALL_THICKNESS = 1.2d;

    public AntColonyCarver(Codec<AntColonyConfiguration> p_64711_) {
        super(p_64711_);
    }

    @Override
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
    public boolean carve(@NotNull CarvingContext pContext, AntColonyConfiguration pConfig, ChunkAccess pChunk, Function<BlockPos, Holder<Biome>> pBiomeAccessor, Random pRandom, Aquifer pAquifer, ChunkPos pChunkPos, CarvingMask pCarvingMask) {

        if(pChunk.getPos().equals(pChunkPos)){
            /*BlockPos startPos = pChunkPos.getMiddleBlockPosition(63);
            //TODO: figure out how to make a multiplayer server work.
            ServerLevel pLevel = Minecraft.getInstance().getSingleplayerServer().getLevel(Level.OVERWORLD);
            ColonyBranch newBranch = AntColony.generateNewTunnels(startPos);
            //ArrayList<AntSphere> spheres = newBranch.getExcavationSpheres();
            int colonyID = Integer.parseInt(("" + startPos.getX() + "" + startPos.getY() + "" + startPos.getZ()).substring(0, 8));
            AntColony colony = new AntColony(pLevel,colonyID,newBranch);
            colony.hasSpawnedAnts = true;
            colony.hasSpawnedDecoration = true;
            colony.save();*/
        }
        else{
            //pChunk.setStartForFeature();
        }

        /*



        if(pChunk.getPos().equals(pChunkPos)){
            AntColony colony = new AntColony(pLevel,colonyID,newBranch);

            if(((ServerLevelUtil)pLevel).getColonyWithID(colonyID) == null) {
                AntUtils.broadcastString(pLevel, "spheres: " + spheres.size());
                colony.hasSpawnedAnts = true;
                ((ServerLevelUtil) pLevel).addColonyToList(colony);
            }
        }

        for(AntSphere tempSphere : spheres){
            if(AntUtils.getDist(tempSphere.centerPos, pChunk.getPos().getMiddleBlockPosition(tempSphere.centerPos.getY())) < (16 + tempSphere.radius)) {
                tempSphere.setSphereCarver(pChunk, ModBlocks.ANT_AIR.get().defaultBlockState(), ModBlocks.ANT_DIRT.get().defaultBlockState(), 1.8);
            }
        }

        if(pChunkPos.getChessboardDistance(pChunk.getPos()) < 3){
            ColonyGenUtils.generateEntrance(pChunk, startPos, ModBlocks.ANT_DIRT.get().defaultBlockState());
        }


        for(ColonyBranch tempBranch : newBranch.getChildrenPassing(b -> b.hasKey("has_room") && b.getValue("has_room").equals("true") && b.getValue("room_type") != null)){
            BlockPos tempPos = tempBranch.getPos();
            String roomType = tempBranch.getValue("room_type");

            if(AntUtils.isPosInChunk(tempPos,pChunk.getPos())){
                pChunk.addEntity(AntColony.getNewWorker(pLevel, colonyID, tempPos, newBranch.getPos()));
            }

            if(roomType == null || roomType.equals("empty")){
                continue;
            }
            boolean isQueen = roomType.equals("queen");

            if(isQueen || roomType.equals("fungus")){
                FungalCore.growWorldgen(pChunk, tempPos, 3d);
            }
            if(isQueen || roomType.equals("storage")){
                ColonyGenUtils.sprinkleAreaWorldgen(tempPos, 5, 5, 12, ModBlocks.LEAFY_CONTAINER_BLOCK.get(), pRandom, pChunk);
            }

        }
*/
        return true;
    }
}
