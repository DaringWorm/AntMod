package com.daringworm.antmod.colony.misc;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.block.custom.FungalCore;
import com.daringworm.antmod.util.AntUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.material.FluidState;

import java.util.ArrayList;
import java.util.Random;

public final class ColonyGenUtils {

    public static ArrayList<AntSphere> generateRoomBlueprint(double height, int size, BlockPos center, Random rand){
        rand = new Random(Math.abs(center.getX()*center.getY()));
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();
        ArrayList<BlockPos> posList = new ArrayList<>();
        ArrayList<AntSphere> returnList = new ArrayList<>();
        posList.add(center);

        for(int i = size; i>0; i--){
            int xOff = rand.nextInt((int)(height*size/8));
            int zOff = rand.nextInt((int)(height*size/8));
            xOff = (rand.nextBoolean()) ? xOff : -xOff;
            zOff = (rand.nextBoolean()) ? zOff : -zOff;

            posList.add(new BlockPos(cx+xOff,cy,cz+zOff));
        }
        for(BlockPos tempPos : posList){
            returnList.add(new AntSphere(tempPos,height));
        }
        return returnList;
    }

    public static ArrayList<AntSphere> generatePassageBlueprint(PosPair pPath, double width){
        Random rand = new Random((long) pPath.top.getX() *pPath.top.getY()*pPath.top.getZ());
        ArrayList<AntSphere> returnList = new ArrayList<>();
        BlockPos start = pPath.top;
        BlockPos end = pPath.bottom;

        int sX = start.getX();
        int sZ = start.getZ();
        int eX = end.getX();
        int eZ = end.getZ();

        int steps = Math.abs(eX-sX) + Math.abs(eZ - sZ);
        boolean xOrZ;
        BlockPos lastPos = start;
        int howLongX = 0;
        int howLongZ = 0;
        float yOffC = 0;

        for(int s = steps; s > 0; s--){
            int xOff = 0;
            int yOff = 0;
            int zOff = 0;
            float yFOff = (float)(end.getY()-lastPos.getY())/(float)s;
            yOffC = yOffC + yFOff;
            if(Math.abs(yOffC) > 1f) {
                yOff = (yFOff > 0) ? 1 : -1;
                yOffC = (yFOff > 0) ? yOffC - 1: yOffC + 1;
            }

            howLongX = eX-lastPos.getX();
            howLongZ = eZ-lastPos.getZ();
            xOrZ = nextBool(Math.abs(howLongX),Math.abs(howLongZ),rand);
            if((xOrZ && howLongX != 0) || howLongZ == 0){xOff = (howLongX > 0) ? 1 : -1;}
            else{zOff = (howLongZ > 0) ? 1: -1;}

            lastPos = new BlockPos(lastPos.getX()+xOff, lastPos.getY()+yOff, lastPos.getZ()+zOff);
            AntSphere sphere = new AntSphere(lastPos, width, true);
            returnList.add(sphere);
        }
        /*System.out.println("Generated a passage with " + returnList.size() + " positions");*/
        return returnList;
    }

    public static void generateEntrance(ChunkAccess chunk, BlockPos startPos, BlockState surfaceState){
        double slope = 1;
        double roughness = 1.7;
        Random rand = AntUtils.randFromPos(startPos);

        if(chunk.getBlockState(startPos.above(12)).isAir()) {
            for (int yOff = 1; yOff < 16; yOff++) {
                double xzOff = (yOff / slope) + 3;

                for (BlockPos tempPos : BlockPos.betweenClosed(startPos.offset(xzOff, yOff - 1, xzOff), startPos.offset(-xzOff, yOff, -xzOff))) {
                    if (AntUtils.isPosInChunk(tempPos, chunk.getPos())) {
                        double xzTempPosOff = AntUtils.getHorizontalDist(tempPos, startPos);
                        if ((xzTempPosOff - yOff / slope) < (rand.nextDouble() * roughness)) {
                            chunk.setBlockState(tempPos, ModBlocks.ANT_AIR.get().defaultBlockState(), false);
                            if (!chunk.getBlockState(tempPos.below()).isAir() && nextBool(10, yOff, rand)) {
                                chunk.setBlockState(tempPos.below(), surfaceState, false);
                            }
                        }
                    }
                }
            }
        }
    }

    public static boolean nextBool(int yes, int no, Random rand){
        int total = yes+no;
        int chosen = (total>0)? rand.nextInt(total) : 0;
        return chosen <= yes;
    }

    public static void carpetArea(BlockPos center, int distance, int vertical, ArrayList<BlockState> stateArrayList, Random rand, Level pLevel){
        for(int x = distance/2; x >= -distance/2; x--){
            for(int z = distance/2; z >= -distance/2; z--){
                for(int y = vertical/2; y >= -vertical/2; y--){
                    BlockPos tempPos = center.offset(x,y,z);
                    BlockState tempState = pLevel.getBlockState(tempPos);
                    if(tempState.getRenderShape() == RenderShape.INVISIBLE && tempState.getFluidState().getAmount() != FluidState.AMOUNT_FULL) {
                        BlockState underTempPos = pLevel.getBlockState(tempPos.below());
                        if(underTempPos.isFaceSturdy(pLevel,tempPos.below(),Direction.UP, SupportType.FULL)){
                            int listSize = stateArrayList.size();
                            BlockState stateToSet = stateArrayList.get(rand.nextInt(listSize));
                            pLevel.setBlock(tempPos,stateToSet,2);
                        }
                    }
                }
            }
        }
    }

    public static void sprinkleArea(BlockPos center, int distance, int vertical, int maxAmount, Block pBlock, Random rand, ServerLevel pLevel){
        for(BlockPos tempPos : BlockPos.randomBetweenClosed(rand, maxAmount,
                center.getX() - distance, center.getY() -vertical, center.getZ() - distance,
                center.getX() + distance, center.getY() + vertical, center.getZ() + distance)){
            BlockState tempState = pLevel.getBlockState(tempPos);
            if(tempState.isAir()) {
                BlockState underTempPos = pLevel.getBlockState(tempPos.below());
                if(underTempPos.isFaceSturdy(pLevel,tempPos.below(),Direction.UP, SupportType.FULL)){
                    pLevel.setBlock(tempPos,pBlock.defaultBlockState(),2);
                }
            }
        }
    }

    public static void sprinkleAreaWorldgen(BlockPos center, int distance, int vertical, int maxAmount, Block pBlock, Random rand, ChunkAccess chunkAccess){
        for(BlockPos tempPos : BlockPos.randomBetweenClosed(rand, maxAmount,
                center.getX() - distance, center.getY() -vertical, center.getZ() - distance,
                center.getX() + distance, center.getY() + vertical, center.getZ() + distance)){
            BlockState tempState = chunkAccess.getBlockState(tempPos);
            if(AntUtils.isPosInChunk(tempPos, chunkAccess.getPos()) && tempState.isAir()) {
                BlockState underTempPos = chunkAccess.getBlockState(tempPos.below());
                if(underTempPos.isFaceSturdy(chunkAccess,tempPos.below(),Direction.UP, SupportType.FULL)){
                    chunkAccess.setBlockState(tempPos,pBlock.defaultBlockState(),false);

                }
            }
        }
    }

    public static void growFungusWordlgen(BlockPos center, ChunkAccess chunkAccess, double radius){
        if(AntUtils.isPosInChunk(center, chunkAccess.getPos())){
            chunkAccess.setBlockState(center, ModBlocks.FUNGAL_CORE.get().defaultBlockState(), false);
        }
        FungalCore.growWorldgen(chunkAccess, center, radius);
    }


    /*public static BlockPos findExitPoint(Level level, BlockPos startPos, double maxIncline, int facingDegrees){
        if(maxIncline <= 0){return BlockPos.ZERO;}
        if(level.canSeeSky(startPos)){return startPos;}

        int i = 1;
        while(i < 256){
            i++;
            BlockPos pos = ColonyBranch.nextBranchPos(startPos, facingDegrees, i, (int)(i * maxIncline));
            if(level.canSeeSky(pos)){return pos;}
        }

        return BlockPos.ZERO;
    }*/


    /*public static void generateBranch(ColonyBranch branch, boolean wontReplaceAir, boolean wholeThing, int stepsIfNotWholeThing, ServerLevel pLevel) {
        ArrayList<AntSphere> sphereArray = (wholeThing)?
                branch.generateBranchBlueprint(AntColony.passageWidth,AntColony.passageWidth+1,AntColony.UNDERGOUND_ROOM_SIZE) :
                branch.generateLimitedBlueprint(AntColony.passageWidth,AntColony.passageWidth+1,AntColony.UNDERGOUND_ROOM_SIZE, stepsIfNotWholeThing, wontReplaceAir);
        for(AntSphere sphere : sphereArray){
            sphere.setSphere((ServerLevel) pLevel,BLOCK1,BLOCK2, 2);
        }

        AntUtils.broadcastString(pLevel,"Successfully generated branch. Carver placed " + sphereArray.size() + " spheres.");
    }*/


    public static ArrayList<BlockState> getAllFungusStates(){
        ArrayList<BlockState> fungusStateList = new ArrayList<>();
        for(int i = 5; i >= 0; i--){
            fungusStateList.add(ModBlocks.FUNGUS.get().defaultBlockState());
        }
        return fungusStateList;
    }
}
