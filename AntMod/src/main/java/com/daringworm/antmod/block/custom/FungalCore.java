package com.daringworm.antmod.block.custom;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.util.AntUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    public static final double MAX_SPREAD_DISTANCE = 6f;

    private static final int tileSizeXZ = 5;
    private static final int tileSizeY = 4;
    private static final int halfTileXZ = 3;


    private static boolean doesConformToShape(BlockPos questionPos, BlockPos centerPos){
        if(questionPos == centerPos){
            return false;
        }

        int tempX = Math.abs((questionPos.getX() - centerPos.getX()) % tileSizeXZ);
        int tempY = (-1 + questionPos.getY() - centerPos.getY() + tileSizeY * 300) % tileSizeY;
        int tempZ = Math.abs((questionPos.getZ() - centerPos.getZ()) % tileSizeXZ);

        // creates the main passageways
        if ((tempX % tileSizeXZ == 0 || tempZ % tileSizeXZ == 0)) {
            if ((tempY % tileSizeY <= 1)) {
                return false;
            }
        }

        //creates the stairs
        if ((
                ((tempX - halfTileXZ) % tileSizeXZ == 0 && (tempZ - 1) % tileSizeXZ <= 3 && (tempY + 1 + (tempZ - 1) % tileSizeXZ) % tileSizeY <= 2) ||
                        ((tempZ - halfTileXZ) % tileSizeXZ == 0 && (tempX - 1) % tileSizeXZ <= 3 && (tempY + 1 + (tempX - 1) % tileSizeXZ) % tileSizeY <= 2))) {
            return false;
        }

        return true;
    }


    private static ArrayList<BlockPos> blocksToSpreadTo(ServerLevel pLevel, BlockPos startPos, double radius){
        Random rand = pLevel.getRandom();
        int searchIterations = (int)MAX_SPREAD_DISTANCE;
        int maxRandomsPerIteration = 16;
        ArrayList<BlockPos> searchedPoses = new ArrayList<>();
        ArrayList<BlockPos> currentPoses = new ArrayList<>(List.of(startPos));
        ArrayList<BlockPos> nextPoses = new ArrayList<>();

        ArrayList<BlockPos> returnPoses = new ArrayList<>();

        for(int i = searchIterations; i > 0; i--){
            for(int j = 0; j < maxRandomsPerIteration && j < currentPoses.size(); j++){
                BlockPos tempPos = currentPoses.get(rand.nextInt(currentPoses.size()));
                searchedPoses.add(tempPos);
                currentPoses.remove(tempPos);

                if(AntUtils.getDist(tempPos, startPos) > radius){
                    continue;
                }

                for(BlockPos tempPos1 : BlockPos.betweenClosed(tempPos.offset(1,1,1), tempPos.offset(-1,-1,-1))){
                    if(tempPos1 != tempPos){
                        if(pLevel.getBlockState(tempPos1).isAir() && doesConformToShape(tempPos1, startPos) && !returnPoses.contains(tempPos1)){
                            returnPoses.add(tempPos1.immutable());
                        }
                        else if(isFungus(pLevel.getBlockState(tempPos1).getBlock()) && !nextPoses.contains(tempPos1) && !searchedPoses.contains(tempPos1)){
                            nextPoses.add(tempPos1.immutable());
                        }
                    }
                }

                currentPoses.addAll(nextPoses);
            }

            if(returnPoses.size() > 32){
                break;
            }

            if(currentPoses.size() <= 10) {
                searchedPoses.addAll(currentPoses);
                currentPoses.addAll(nextPoses);
                nextPoses.clear();
            }
        }

        //AntUtils.broadcastString(pLevel, "searched " + searchedPoses.size() + " poses.");
        //AntUtils.broadcastString(pLevel, "next_poses include " + nextPoses.size() + " poses.");
        //AntUtils.broadcastString(pLevel, "returned " + returnPoses.size() + " poses.");
        return returnPoses;
    }


    public static void grow(ServerLevel pLevel, BlockPos pPos, int amount) {
        Random rand = pLevel.getRandom();
        ArrayList<BlockPos> spreadList = blocksToSpreadTo(pLevel, pPos, MAX_SPREAD_DISTANCE);

        if(spreadList.isEmpty()){
            BlockPos nodulePos = BlockPos.findClosestMatch(pPos, MAX_DISTANCE_FROM_CORE, MAX_DISTANCE_FROM_CORE, p -> pLevel.getBlockState(p).getBlock() == ModBlocks.FUNGUS.get()).orElse(BlockPos.ZERO);
            if(nodulePos != BlockPos.ZERO) {
                pLevel.setBlock(nodulePos, ModBlocks.FUNGAL_NODULE.get().defaultBlockState(), 2);
            }
        }
        else {

            for (int i = 0; i < amount; i++) {
                if (spreadList.isEmpty()) {
                    spreadList = blocksToSpreadTo(pLevel, pPos, MAX_SPREAD_DISTANCE);
                    if (spreadList.isEmpty()) {
                        break;
                    }
                }
                spreadList.remove(pPos);

                int percentOverrideChance = 0;
                BlockPos nearestPos = spreadList.get(rand.nextInt(spreadList.size()));

                for (BlockPos tempPos : spreadList) {
                    percentOverrideChance += 5;
                    if (rand.nextInt(100) < percentOverrideChance) {
                        break;
                    } else if (AntUtils.getDist(tempPos, pPos) < AntUtils.getDist(nearestPos, pPos)) {
                        nearestPos = tempPos;
                    }
                }

                pLevel.setBlock(nearestPos, ModBlocks.FUNGUS.get().defaultBlockState(), 2);
                spreadList.remove(nearestPos);
            }
        }
    }

    public static void growWorldgen(ChunkAccess chunkAccess, BlockPos nucleusPos, double radius) {
        if(AntUtils.getHorizontalDist(chunkAccess.getPos().getMiddleBlockPosition(0), nucleusPos) - 16 > radius){
            return;
        }

        if(AntUtils.isPosInChunk(nucleusPos,chunkAccess.getPos())){
            chunkAccess.setBlockState(nucleusPos, ModBlocks.FUNGAL_CORE.get().defaultBlockState(), false);
        }

        for(BlockPos tempPos : BlockPos.betweenClosed(nucleusPos.offset(radius, radius, radius), nucleusPos.offset(-radius, -radius/2, -radius))){
            if(AntUtils.isPosInChunk(tempPos, chunkAccess.getPos()) && AntUtils.getDist(tempPos, nucleusPos) <= radius) {
                if(doesConformToShape(tempPos, nucleusPos) && chunkAccess.getBlockState(tempPos).isAir()){
                    chunkAccess.setBlockState(tempPos, ModBlocks.FUNGUS.get().defaultBlockState(), false);
                }
            }
        }
    }
}
