package com.daringworm.antmod.colony.misc;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.goals.AntUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;

public class PosSpherePair {
    public BlockPos blockPos;
    public double radius;
    public boolean acceptAir;

    public PosSpherePair(BlockPos center, double sphereRadius){
        this.blockPos = center;
        this.radius = sphereRadius;
    }

    public ArrayList<BlockPos> getBlockPoses(){
        ArrayList<BlockPos> returnList = new ArrayList<>();
        for(int x = (int)(radius+2); x >= -radius; x--){
            for(int y = (int)(radius+2); y >= -radius; y--){
                for(int z = (int)(radius+2); z >= -radius; z--){
                    BlockPos tempPos = blockPos.offset(x,y,z);
                    if(AntUtils.getDist(tempPos,blockPos) <= radius){
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
                    BlockPos tempPos = blockPos.offset(x,y,z);
                    if(AntUtils.getDist(tempPos,blockPos) <= radius && pLevel.getBlockState(tempPos).getBlock() != ModBlocks.ANT_AIR.get()){
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
}
