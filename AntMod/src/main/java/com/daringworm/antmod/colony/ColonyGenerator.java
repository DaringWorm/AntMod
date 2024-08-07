package com.daringworm.antmod.colony;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.colony.misc.ColonyBranch;
import com.daringworm.antmod.colony.misc.ColonyGenUtils;
import com.daringworm.antmod.colony.misc.PosSpherePair;
import com.daringworm.antmod.entity.ModEntityTypes;
import com.daringworm.antmod.entity.brains.parts.WorkingStages;
import com.daringworm.antmod.entity.custom.WorkerAnt;
import com.daringworm.antmod.goals.AntUtils;
import com.daringworm.antmod.mixin.tomixin.ServerLevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Random;

public class ColonyGenerator {
/*

 private BlockPos findPosForDir(BlockPos startPos, int distanceFromCenter, Direction direction, Random rand){
        int randomIntDistBound = rand.nextInt((int)distanceFromCenter+1);
        int bigNumber = (randomIntDistBound >= (int)distanceFromCenter/2) ? randomIntDistBound : 0;
        int smallNumber = (bigNumber != 0) ? (int)Math.abs(Math.sqrt((distanceFromCenter*distanceFromCenter)-(bigNumber*bigNumber))) : randomIntDistBound- randomIntDistBound/3;
        smallNumber = (rand.nextBoolean()) ? -smallNumber : smallNumber;
        if(bigNumber == 0){bigNumber = (int)Math.abs(Math.sqrt((distanceFromCenter*distanceFromCenter)-(smallNumber*smallNumber)));}

        int xVar = (direction == Direction.EAST || direction == Direction.WEST) ?
                ((direction == Direction.EAST) ? bigNumber : -bigNumber) : smallNumber;

        int zVar = (direction == Direction.SOUTH || direction == Direction.NORTH) ?
                ((direction == Direction.SOUTH) ? bigNumber : -bigNumber) : smallNumber;

        return startPos.offset(xVar, 0, zVar);
    }

    private static BlockPos findSurfacePos(BlockPos pos, Level level){
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
    }


    private boolean isAcceptableWall (Block pBlock, boolean accepAir){
        return pBlock == Blocks.STONE || pBlock == Blocks.GRANITE || pBlock == Blocks.ANDESITE || pBlock == Blocks.DIORITE ||
                pBlock == Blocks.COAL_ORE || pBlock == Blocks.IRON_ORE || pBlock == Blocks.COPPER_ORE ||
                pBlock == Blocks.DRIPSTONE_BLOCK || pBlock == Blocks.GOLD_ORE || pBlock == Blocks.MOSS_BLOCK || pBlock == Blocks.BONE_BLOCK ||
                pBlock == ModBlocks.ANT_AIR.get() || pBlock == Blocks.GLASS || pBlock == ModBlocks.FUNGUS_BLOCK.get() ||
                (accepAir && pBlock == Blocks.AIR);
    }

    private void placeSphere(BlockPos middlePos, double radius, Block airBlock, Block wallBlock, boolean randomSplotches, boolean acceptAir){
        int mx = middlePos.getX();
        int my = middlePos.getY();
        int mz = middlePos.getZ();
        for (int rx = (int) (radius*2); rx > -radius*2; rx--){
            for (int ry = (int) (radius*2); ry > -radius*2; ry--){
                for (int rz = (int) (radius*2); rz > -radius*2; rz--){
                    BlockPos tempPos = new BlockPos (mx+rx, my+ry,mz+rz);
                    double dist = AntUtils.getDist(tempPos,middlePos);

                    if (dist <= radius-1.5d){
                        level.setBlock(tempPos, airBlock.defaultBlockState(), 2);
                    }
                    else if (dist > radius-1 && dist < radius+0.9){
                        if (level.getBlockState(tempPos) != airBlock.defaultBlockState()){
                            boolean bool1 = Math.abs(tempPos.getX()%10 - tempPos.getZ()%10) < 5;
                            if(bool1 && randomSplotches) {
                                level.setBlock(tempPos, wallBlock.defaultBlockState(), 2);
                            }
                            else if(!isAcceptableWall(level.getBlockState(tempPos).getBlock(), acceptAir)){
                                level.setBlock(tempPos, wallBlock.defaultBlockState(), 2);
                            }
                        }
                    }
                }
            }
        }
    }

    private void generateRandomPassage(PosPair pPath, Random rand, double width, Block block, Block block2){
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
            xOrZ = ColonyGenUtils.nextBool(Math.abs(howLongX),Math.abs(howLongZ),rand);
            if((xOrZ && howLongX != 0) || howLongZ == 0){xOff = (howLongX > 0) ? 1 : -1;}
            else{zOff = (howLongZ > 0) ? 1: -1;}

            lastPos = new BlockPos(lastPos.getX()+xOff, lastPos.getY()+yOff, lastPos.getZ()+zOff);
            placeSphere(lastPos,width,block,block2,true, false);
        }
    }

    private void connectWithPassage(PosPair pPath, double width, Block block, Block block2, boolean replaceAir){
        BlockPos startPos = pPath.top;
        BlockPos endPos = pPath.bottom;
        BlockPos currentPos = startPos;
        BlockPos nextPos = startPos;
        double distToEnd = AntUtils.getDist(currentPos,endPos);
        double shouldX = 0;
        double shouldY = 0;
        double shouldZ = 0;
        int step = 0;

        while (distToEnd > width){

            int pX = endPos.getX()-currentPos.getX();
            double xProb = pX/distToEnd;
            int pY = endPos.getY()-currentPos.getY();
            double yProb = pY/distToEnd;
            int pZ = endPos.getZ()-currentPos.getZ();
            double zProb = pZ/distToEnd;

            shouldX = shouldX+Math.abs(xProb);
            if(shouldX>=1 && pX !=0){
                nextPos = nextPos.east(pX/Math.abs(pX));
                shouldX=shouldX-1;
            }
            shouldY = shouldY+Math.abs(yProb);
            if(shouldY>=1 && pY !=0){
                nextPos = nextPos.above(pY/Math.abs(pY));
                shouldY=shouldY-1;
            }
            shouldZ = shouldZ+Math.abs(zProb);
            if(shouldZ>=1 && pZ !=0){
                nextPos = nextPos.south(pZ/Math.abs(pZ));
                shouldZ=shouldZ-1;
            }

            if(step%5>2){
                nextPos.below();
            }
            double width1 = width;
            if (step%14>8){
                width1 = width1*0.9;
            }

            placeSphere(nextPos,width1,block, block2, true, replaceAir);
            currentPos = nextPos;

            distToEnd = AntUtils.getDist(currentPos,endPos);
            step++;
        }
    }

    private void createRandomRoom(double height, int size, BlockPos center, Random rand, Block wallBlock, boolean randomSplotches){
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();
        int multiplyer = (int)Math.sqrt(size)+1;
        ArrayList<BlockPos> posList = new ArrayList<>();
        posList.add(center);

        for(; size>0; size--){
            int xOff = rand.nextInt((int)height-(int)(height/4d));
            int zOff = rand.nextInt((int)height-(int)(height/4d));
            xOff = (rand.nextBoolean()) ? xOff : -xOff;
            zOff = (rand.nextBoolean()) ? zOff : -zOff;

            posList.add(new BlockPos(cx+xOff,cy,cz+zOff));
        }
        for(BlockPos tempPos : posList){
            placeSphere(tempPos, height/2d, BLOCK1, wallBlock, randomSplotches, false);
        }
    }

    private void carpetArea(BlockPos center, int distance, int vertical, Block pBlock, Level pLevel){
        for(int x = distance/2; x >= -distance/2; x--){
            for(int z = distance/2; z >= -distance/2; z--){
                for(int y = vertical/2; y >= -vertical/2; y--){
                    BlockPos tempPos = center.offset(x,y,z);
                    BlockState tempState = pLevel.getBlockState(tempPos);
                    if(tempState.getRenderShape() == RenderShape.INVISIBLE && tempState.getFluidState().getAmount() != FluidState.AMOUNT_FULL) {
                        BlockState underTempPos = pLevel.getBlockState(tempPos.below());
                        if(underTempPos.isFaceSturdy(pLevel,tempPos.below(),Direction.UP, SupportType.FULL)){
                            pLevel.setBlock(tempPos,pBlock.defaultBlockState(),2);
                        }
                    }
                }
            }
        }
    }
*/


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

    public static void sprinkleArea(BlockPos center, int distance, int vertical, int chancePercent, Block pBlock, Random rand, Level pLevel){
        for(int x = distance/2; x >= -distance/2; x--){
            for(int z = distance/2; z >= -distance/2; z--){
                for(int y = vertical/2; y >= -vertical/2; y--){
                    BlockPos tempPos = center.offset(x,y,z);
                    BlockState tempState = pLevel.getBlockState(tempPos);
                    if(tempState.getRenderShape() == RenderShape.INVISIBLE && tempState.getFluidState().getAmount() != FluidState.AMOUNT_FULL) {
                        BlockState underTempPos = pLevel.getBlockState(tempPos.below());
                        boolean allow = ColonyGenUtils.nextBool(chancePercent, 100, rand);
                        if(underTempPos.isFaceSturdy(pLevel,tempPos.below(),Direction.UP, SupportType.FULL) && allow){
                            pLevel.setBlock(tempPos,pBlock.defaultBlockState(),2);
                        }
                    }
                }
            }
        }
    }

    public static BlockPos findExitPoint(Level level, BlockPos startPos, double maxIncline, int facingDegrees){
        if(maxIncline <= 0){return BlockPos.ZERO;}
        if(level.canSeeSky(startPos)){return startPos;}

        int i = 1;
        while(i < 256){
            i++;
            BlockPos pos = ColonyBranch.nextBranchPos(startPos, facingDegrees, i, (int)(i * maxIncline));
            if(level.canSeeSky(pos)){return pos;}
        }

        return BlockPos.ZERO;
    }


    public static ArrayList<BlockState> getAllFungusStates(){
        ArrayList<BlockState> fungusStateList = new ArrayList<>();
        for(int i = 5; i >= 0; i--){
            fungusStateList.add(ModBlocks.FUNGUS_CARPET.get().defaultBlockState().setValue(BlockStateProperties.AGE_5, i));
        }
        return fungusStateList;
    }

    Block BLOCK1 = ModBlocks.ANT_AIR.get();
    Block BLOCK2 = ModBlocks.ANT_DIRT.get();
    Block BLOCK3 = ModBlocks.ANT_DEBRIS.get();

    private final Level level;


    public ColonyGenerator(Level pLevel) {
        this.level = pLevel;
    }


    public void createAndGenerateColony(BlockPos pPos) {

        ArrayList<BlockState> fungusStateList = getAllFungusStates();

        AntColony colony = new AntColony(level,level.getRandom().nextInt(),pPos);
        ArrayList<PosSpherePair> sphereArray = colony.getColonyBlueprint();
        for(PosSpherePair sphere : sphereArray){
            sphere.setSphere((ServerLevel) this.level,this.BLOCK1,this.BLOCK2, 2);
        }

        ((ServerLevelUtil)(this.level)).addColonyToList(colony);

        //Adds the ants, decoration, and functionality blocks

        for (BlockPos roomPos : colony.tunnels.listRoomPoses()) {
            ColonyGenerator.sprinkleArea(roomPos, 8, 4, 10, ModBlocks.LEAFY_CONTAINER_BLOCK.get(), colony.random, level);
            ColonyGenerator.carpetArea(roomPos, 8, 4, fungusStateList, colony.random, level);

            WorkerAnt pAnt = new WorkerAnt(ModEntityTypes.WORKERANT.get(), level);
            pAnt.moveTo(Vec3.atCenterOf(roomPos));
            pAnt.setColonyID(colony.colonyID);
            pAnt.setWorkingStage(WorkingStages.SCOUTING);
            pAnt.setHomeContainerPos(roomPos);
            pAnt.setWorkingStage(WorkingStages.SCOUTING);
            level.addFreshEntity(pAnt);
            pAnt.setFirstSurfacePos(pPos);
        }


        AntUtils.broadcastString(level,"Successfully generated colony. Carver placed " + sphereArray.size() + " spheres.");
    }

    public void generateColony(AntColony colony) {
        ArrayList<PosSpherePair> sphereArray = colony.getColonyBlueprint();
        for(PosSpherePair sphere : sphereArray){
            sphere.setSphere((ServerLevel) this.level,this.BLOCK1,this.BLOCK2, 2);
        }

        ((ServerLevelUtil)(this.level)).addColonyToList(colony);

        //Adds the ants, decoration, and functionality blocks

        for (BlockPos roomPos : colony.tunnels.listRoomPoses()) {
            ColonyGenerator.sprinkleArea(roomPos, 8, 4, 10, ModBlocks.LEAFY_CONTAINER_BLOCK.get(), colony.random, level);
            ColonyGenerator.carpetArea(roomPos, 8, 4, ColonyGenerator.getAllFungusStates(), colony.random, level);

            WorkerAnt pAnt = new WorkerAnt(ModEntityTypes.WORKERANT.get(), level);
            pAnt.moveTo(Vec3.atCenterOf(roomPos));
            pAnt.setColonyID(colony.colonyID);
            pAnt.setWorkingStage(WorkingStages.SCOUTING);
            pAnt.setHomeContainerPos(roomPos);
            level.addFreshEntity(pAnt);
            pAnt.setFirstSurfacePos(colony.startPos);
        }
        AntUtils.broadcastString(level,"Successfully generated colony. Carver placed " + sphereArray.size() + " spheres.");
    }

    public void generateBranch(ColonyBranch branch, boolean wontReplaceAir, boolean wholeThing, int stepsIfNotWholeThing) {
        ArrayList<PosSpherePair> sphereArray = (wholeThing)?
                branch.generateBranchBlueprint(AntColony.passageWidth,AntColony.passageWidth+1,AntColony.UNDERGOUND_ROOM_SIZE) :
                branch.generateLimitedBlueprint(AntColony.passageWidth,AntColony.passageWidth+1,AntColony.UNDERGOUND_ROOM_SIZE, stepsIfNotWholeThing, wontReplaceAir);
        for(PosSpherePair sphere : sphereArray){
            sphere.setSphere((ServerLevel) this.level,this.BLOCK1,this.BLOCK2, 2);
        }

        AntUtils.broadcastString(level,"Successfully generated branch. Carver placed " + sphereArray.size() + " spheres.");
    }


}
