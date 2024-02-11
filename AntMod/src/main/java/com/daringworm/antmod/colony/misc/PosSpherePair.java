package com.daringworm.antmod.colony.misc;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.goals.AntUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.function.Predicate;

public class PosSpherePair {
    public BlockPos centerPos;
    public double radius;
    private boolean checkSky = false;

    public PosSpherePair(BlockPos center, double sphereRadius){
        this.centerPos = center;
        this.radius = sphereRadius;
    }

    public PosSpherePair(BlockPos center, double sphereRadius, boolean runSkyCheck){
        this.centerPos = center;
        this.radius = sphereRadius;
        this.checkSky = runSkyCheck;
    }

    public ArrayList<BlockPos> getBlockPoses(){
        ArrayList<BlockPos> returnList = new ArrayList<>();
        for(int x = (int)(radius+2); x >= -radius; x--){
            for(int y = (int)(radius+2); y >= -radius; y--){
                for(int z = (int)(radius+2); z >= -radius; z--){
                    BlockPos tempPos = centerPos.offset(x,y,z);
                    if(AntUtils.getDist(tempPos, centerPos) <= radius){
                        returnList.add(tempPos);
                    }
                }
            }
        }

        return returnList;
    }

    public ArrayList<BlockPos> getBlockPoses(Level pLevel){
        ArrayList<BlockPos> returnList = new ArrayList<>();
        for(int x = (int)(radius+2); x >= -radius; x--){
            for(int y = (int)(radius+2); y >= -radius; y--){
                for(int z = (int)(radius+2); z >= -radius; z--){
                    BlockPos tempPos = centerPos.offset(x,y,z);
                    if(AntUtils.getDist(tempPos, centerPos) <= radius && pLevel.getBlockState(tempPos).getBlock() != ModBlocks.ANT_AIR.get()){
                        if(pLevel.getBlockState(tempPos).getBlock() == Blocks.AIR){
                            pLevel.setBlock(tempPos,ModBlocks.ANT_AIR.get().defaultBlockState(),2);
                        }
                        else {
                            returnList.add(tempPos);
                        }
                    }
                }
            }
        }

        return returnList;
    }

    public void setSphere(ServerLevel pLevel, Block innerBlock, Block outerBlock, double wallThickness){
        PosSpherePair outerShell = new PosSpherePair(this.centerPos,this.radius+wallThickness);
        ArrayList<BlockPos> totalList = outerShell.getBlockPoses();
        ArrayList<BlockPos> innerList = this.getBlockPoses();
        totalList.removeAll(innerList);
        for(BlockPos pos : totalList){
            if(!checkSky || !pLevel.canSeeSky(pos)) {
                BlockState pState = pLevel.getBlockState(pos);
                if (pState.getBlock() != innerBlock && pState.getBlock() != outerBlock) {
                    pLevel.setBlock(pos, outerBlock.defaultBlockState(), 2);
                }
            }
        }
        for(BlockPos pos : innerList){
            pLevel.setBlock(pos, innerBlock.defaultBlockState(),2);
        }
    }

    public void setSphereWorldgen(WorldGenLevel worldGenLevel, Block innerBlock, Block outerBlock, double wallThickness, Predicate<BlockState> replaceabilityPredicate){
        PosSpherePair outerShell = new PosSpherePair(this.centerPos,this.radius+wallThickness);
        ArrayList<BlockPos> totalList = outerShell.getBlockPoses();
        ArrayList<BlockPos> innerList = this.getBlockPoses();
        totalList.removeAll(innerList);
        for(BlockPos pos : totalList){
            BlockState pState = worldGenLevel.getBlockState(pos);
            if (pState.getBlock() != innerBlock && pState.getBlock() != outerBlock && replaceabilityPredicate.test(pState)) {
                worldGenLevel.setBlock(pos,outerBlock.defaultBlockState(),2);
            }
        }

        for(BlockPos pos : innerList){
            if(replaceabilityPredicate.test(worldGenLevel.getBlockState(pos))) {
                worldGenLevel.setBlock(pos, innerBlock.defaultBlockState(), 2);
            }
        }
    }

    public void setSphereCarver(ChunkAccess chunkAccess, BlockState innerState, BlockState outerState, double wallThickness){
        for(int x = (int)(radius+wallThickness); x >= -radius-wallThickness; x--){
            for(int y = (int)(radius+wallThickness); y >= -radius-wallThickness; y--){
                for(int z = (int)(radius+wallThickness); z >= -radius-wallThickness; z--){
                    BlockPos tempPos = centerPos.offset(x,y,z);
                    if(AntUtils.getDist(tempPos, centerPos) <= radius+wallThickness){
                        if(chunkAccess.getBlockState(tempPos) != innerState){
                            if(AntUtils.getDist(tempPos, this.centerPos) <= this.radius) {
                                chunkAccess.setBlockState(tempPos, innerState, false);
                            }
                            else if(chunkAccess.getBlockState(tempPos) != outerState){
                                chunkAccess.setBlockState(tempPos,outerState,false);
                            }
                        }
                    }
                }
            }
        }
    }
}
