package com.daringworm.antmod.block.custom;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.util.AntUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;

import static com.daringworm.antmod.block.custom.MoldyLeaves.MAX_DISTANCE_FROM_CORE;
import static com.daringworm.antmod.block.custom.MoldyLeaves.isFungus;

public class FungalCore extends Block {

    public FungalCore(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isRandomlyTicking(BlockState pState) {
        return false;
    }



    private static ArrayList<BlockPos> blocksToSpreadTo(BlockPos nucleusPos, BlockPos selfPos, ServerLevel pLevel){

        //expand the fungus-based search area, and find the nearest air-like positions. Filter through these later.

        ArrayList<BlockPos> searchedPoses = new ArrayList<>();
        ArrayList<BlockPos> currentPoses = new ArrayList<>();
        ArrayList<BlockPos> nextPoses = new ArrayList<>();
        ArrayList<BlockPos> airPoses = new ArrayList<>();
        currentPoses.add(selfPos);

        int searchSize = MAX_DISTANCE_FROM_CORE+3;

        for(; searchSize >= 0; searchSize--) {
            for (BlockPos tempPos : currentPoses) {
                for (Direction dir : Direction.values()) {
                    BlockPos tempPos1 = tempPos.relative(dir, 1);
                    BlockState tempState1 = pLevel.getBlockState(tempPos1);
                    if (isFungus(tempState1.getBlock())) {
                        if(!searchedPoses.contains(tempPos1)) {
                            nextPoses.add(tempPos1);
                        }
                    }
                    else if(tempState1.canBeReplaced(Fluids.FLOWING_WATER)){
                        airPoses.add(tempPos1);
                    }

                    if(!searchedPoses.contains(tempPos1)){
                        searchedPoses.add(tempPos1);
                    }
                }
            }
            currentPoses.clear();
            currentPoses.addAll(nextPoses);
            nextPoses.clear();
        }

        //AntUtils.broadcastString(pLevel, "Searched size = " + searchedPoses.size() + ". Current size = " + currentPoses.size());


        // Filter the air positions found to find ones which can be safely replaced.
        ArrayList<BlockPos> returnPoses = new ArrayList<>();

        int baseX = nucleusPos.getX();
        int baseY = nucleusPos.getY();
        int baseZ = nucleusPos.getZ();

        int tileSizeXZ = 5;
        int tileSizeY = 3;
        int halfTileXZ = 2;

        int xOff = (baseX % tileSizeXZ);
        int yOff = (baseY % tileSizeY);
        int zOff = (baseZ % tileSizeXZ);

        xOff = (xOff < 0)? xOff + 2 : xOff - 2;
        zOff = (zOff < 0)? zOff + 2 : zOff - 2;

        for(BlockPos tempPos : airPoses){
            int x = tempPos.getX();
            int y = tempPos.getY();
            int z = tempPos.getZ();

            int modX = ((x+tileSizeXZ*5)%tileSizeXZ)+xOff;
            int modZ = ((z+tileSizeXZ*5)%tileSizeXZ)+zOff;
            int modY = ((y+1+tileSizeY*400)%tileSizeY) + yOff;

            boolean shouldPlace = true;

            //creates the main "t" shaped hallways
            if(((modX==xOff) || (modZ==zOff)) && (modY==yOff || modY==yOff+1)){
                shouldPlace = false;

                //
                //pLevel.setBlock(tempPos, Blocks.GLASS.defaultBlockState(),2);
                //
            }
            //creates the middle stairs
            else if((modX == xOff+halfTileXZ || modX == xOff-halfTileXZ)
                    && (modZ == zOff+halfTileXZ || modZ == zOff-halfTileXZ)
                    && modY != yOff+1){
                shouldPlace = false;

                //
                //pLevel.setBlock(tempPos, Blocks.YELLOW_STAINED_GLASS.defaultBlockState(),2);
                //
            }
            //creates the bottom stairs
            else if((modX == xOff+halfTileXZ || modX == xOff-halfTileXZ)
                    && (modZ == zOff+halfTileXZ-1 || modZ == zOff-halfTileXZ+1)
                    && modY != yOff){
                shouldPlace = false;
                //
                //pLevel.setBlock(tempPos, Blocks.LIME_STAINED_GLASS.defaultBlockState(),2);
                //
            }
            //creates the top stairs
            else if((modX == xOff+halfTileXZ-1 || modX == xOff-halfTileXZ+1)
                    && (modZ == zOff+halfTileXZ || modZ == zOff-halfTileXZ)
                    && modY != yOff+2){
                shouldPlace = false;
                //
                //pLevel.setBlock(tempPos, Blocks.ORANGE_STAINED_GLASS.defaultBlockState(),2);
                //
            }

            if(shouldPlace && AntUtils.getDist(tempPos,nucleusPos) <= MAX_DISTANCE_FROM_CORE){
                returnPoses.add(tempPos);
            }
        }
        return returnPoses;
    }


    public static void grow(ServerLevel pLevel, BlockPos pPos, int amount) {
        ArrayList<BlockPos> growthPoses = blocksToSpreadTo(pPos, pPos, pLevel);
        int fungusPlaced = 0;

        //grows!
        //Grows fungus first
        if(!growthPoses.isEmpty() && amount > 0) {
            for(; amount > 0 && !growthPoses.isEmpty(); amount--) {
                BlockPos growthPos = AntUtils.findNearestBlockPos(pPos, growthPoses);
                if(growthPos != BlockPos.ZERO && pLevel.getBlockState(growthPos).getBlock() != ModBlocks.FUNGUS.get()) {
                    pLevel.setBlock(growthPos, ModBlocks.FUNGUS.get().defaultBlockState(), 2);
                    growthPoses.remove(growthPos);
                    fungusPlaced ++;
                }
                else{
                    growthPoses.remove(growthPos);
                    amount ++;
                    AntUtils.broadcastString(pLevel, "BlockPos Zero or repeat pos used in fungus logic");
                }
            }
        }
        //If it runs out of poses, checks if it can grow recursively, and if it can't, grows nodules
        if(growthPoses.isEmpty() && amount > 0){
            growthPoses = blocksToSpreadTo(pPos, pPos, pLevel);

            if(growthPoses.isEmpty()){
                for(; amount > 0; amount --){
                    BlockPos nodulePos = BlockPos.findClosestMatch(pPos,5,5, p -> pLevel.getBlockState(p).getBlock() == ModBlocks.FUNGUS.get()).orElse(BlockPos.ZERO);
                    if(nodulePos != BlockPos.ZERO){
                        pLevel.setBlock(nodulePos,ModBlocks.FUNGAL_NODULE.get().defaultBlockState(), 2);
                        fungusPlaced ++;
                    }
                }
            }
            else{
                grow(pLevel,pPos,amount);
            }
        }
        AntUtils.broadcastString(pLevel,"Fungus placed: " + fungusPlaced);
    }
}
