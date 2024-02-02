package com.daringworm.antmod.colony;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.colony.misc.PosPair;
import com.daringworm.antmod.colony.misc.PosSpherePair;
import com.daringworm.antmod.goals.AntUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;

import java.util.ArrayList;
import java.util.Random;

public class ColonyGenerator implements AntUtils {

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
            xOrZ = nextBool(Math.abs(howLongX),Math.abs(howLongZ),rand);
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

    static boolean nextBool(int yes, int no, Random rand){
        int total = yes+no;
        int chosen = (total>0)? rand.nextInt(total) : 0;
        return chosen <= yes;
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

    private void carpetArea(BlockPos center, int distance, int vertical, ArrayList<BlockState> stateArrayList, Random rand, Level pLevel){
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

    private void sprinkleArea(BlockPos center, int distance, int vertical, int chancePercent, Block pBlock, Random rand, Level pLevel){
        for(int x = distance/2; x >= -distance/2; x--){
            for(int z = distance/2; z >= -distance/2; z--){
                for(int y = vertical/2; y >= -vertical/2; y--){
                    BlockPos tempPos = center.offset(x,y,z);
                    BlockState tempState = pLevel.getBlockState(tempPos);
                    if(tempState.getRenderShape() == RenderShape.INVISIBLE && tempState.getFluidState().getAmount() != FluidState.AMOUNT_FULL) {
                        BlockState underTempPos = pLevel.getBlockState(tempPos.below());
                        boolean allow = nextBool(chancePercent, 100, rand);
                        if(underTempPos.isFaceSturdy(pLevel,tempPos.below(),Direction.UP, SupportType.FULL) && allow){
                            pLevel.setBlock(tempPos,pBlock.defaultBlockState(),2);
                        }
                    }
                }
            }
        }
    }

    private BlockPos findExitPoint(BlockPos startPos, double maxIncline, Direction direction, Random rand){

            assert maxIncline != 0;
            int depth = findSurfacePos(startPos, level).getY() - startPos.getY();
            double distanceFromCenter = Math.abs(depth / maxIncline);
            int randomIntDistBound = rand.nextInt((int)(distanceFromCenter-distanceFromCenter/3)+1);
            int bigNumber = (randomIntDistBound >= (int)distanceFromCenter/2) ? randomIntDistBound : 0;
            int smallNumber = (bigNumber != 0) ? (int)Math.abs(Math.sqrt((distanceFromCenter*distanceFromCenter)-(bigNumber*bigNumber))) : randomIntDistBound;
            smallNumber = (rand.nextBoolean()) ? -smallNumber : smallNumber;
            if(bigNumber == 0){bigNumber = (int)Math.abs(Math.sqrt((distanceFromCenter*distanceFromCenter)-(smallNumber*smallNumber)));}

            int xVar = (direction == Direction.EAST || direction == Direction.WEST) ?
                    ((direction == Direction.EAST) ? bigNumber : -bigNumber) : smallNumber;

            int zVar = (direction == Direction.SOUTH || direction == Direction.NORTH) ?
                ((direction == Direction.SOUTH) ? bigNumber : -bigNumber) : smallNumber;

            BlockPos returnPos = findSurfacePos(startPos.offset(xVar, 0, zVar), level);
            for (int i = 1000; i > 0; i--) {
                assert distanceFromCenter != 0;
                if (((depth / distanceFromCenter) <= maxIncline)) {
                    return returnPos;
                } else {
                    returnPos = findSurfacePos(returnPos.offset(xVar / Math.abs(xVar), 0, zVar / Math.abs(zVar)), level);
                    distanceFromCenter = distanceFromCenter + 1.2;
                    depth = findSurfacePos(returnPos, level).getY() - startPos.getY();
                }
            }

        return BlockPos.ZERO;
    }

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

    private BlockPos findSurfacePos(BlockPos pos, Level level){
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
    }

    private double CARVE_RADIUS = 1d;
    private int PASSAGE_LENGTH = 30;
    
    Block BLOCK1 = ModBlocks.ANT_AIR.get();
    Block BLOCK2 = ModBlocks.LUMINOUSDEBRIS.get();
    Block BLOCK3 = ModBlocks.ANTDEBRIS.get();

    private AntColony colony;
    private Level level;
    boolean hasSpawnedStuff = false;


    public ColonyGenerator(Level pLevel) {
        this.level = pLevel;
    }

    public void generate(BlockPos pPos) {
        ArrayList<BlockState> fungusStateList = new ArrayList<>();
        for(int i = 5; i >= 0; i--){
            fungusStateList.add(ModBlocks.FUNGUS_BLOCK.get().defaultBlockState().setValue(BlockStateProperties.AGE_5, i));
        }


        int numberOfBlocks = 0;
        AntColony colony = new AntColony(level,level.getRandom().nextInt(),pPos);
        ArrayList<PosSpherePair> sphereArray = colony.getColonyBlueprint();
        for(PosSpherePair tempSphere : sphereArray){
            ArrayList<BlockPos> posList = tempSphere.getBlockPoses();
            for(BlockPos tempPos : posList){
                if(level.getBlockState(tempPos).getBlock() != ModBlocks.ANT_AIR.get()){
                    level.setBlock(tempPos,ModBlocks.ANT_AIR.get().defaultBlockState(), 2);

                    for(Direction dir : Direction.values()){
                        BlockPos tempPos1 = tempPos.relative(dir,1);
                        BlockState tempState = level.getBlockState(tempPos1);
                        if(!level.getFluidState(tempPos1).isEmpty()
                                || (!level.canSeeSky(tempPos1) && tempState.getBlock() == Blocks.AIR)
                                || (tempState.getBlock() instanceof FallingBlock)){
                            level.setBlock(tempPos1, ModBlocks.LUMINOUSDEBRIS.get().defaultBlockState(),2);
                        }
                    }
                    numberOfBlocks++;
                }
            }
        }

        //Adds the ants and decoration and functionality blocks
        /*{
            for (BlockPos roomPos : colony.roomPosList) {
                sprinkleArea(roomPos, 8, 4, 10, ModBlocks.LEAFY_CONTAINER_BLOCK.get(), colony.random,carver.getLevel());
                carpetArea(roomPos, 8, 4, fungusStateList, colony.random, carver.getLevel());

                WorkerAnt pAnt = new WorkerAnt(ModEntityTypes.WORKERANT.get(), carver.getLevel());
                pAnt.moveTo(Vec3.atCenterOf(roomPos));
                pAnt.setColonyID(carver.getColonyID());
                pAnt.setWorkingStage(WorkingStages.SCOUTING);
                pAnt.setHomePos(roomPos);
                pAnt.memory.workingStage = WorkingStages.SCOUTING;
                carver.getLevel().addFreshEntity(pAnt);
                pAnt.memory.surfacePos = carver.blockPosition();
                pAnt.setFirstSurfacePos(carver.blockPosition());
            }

            sprinkleArea(colony.queenRoomPos, 8, 4, 10, ModBlocks.LEAFY_CONTAINER_BLOCK.get(), colony.random,carver.getLevel());
            carpetArea(colony.queenRoomPos, 8, 4, fungusStateList, colony.random, carver.getLevel());

            QueenAnt pQueen = new QueenAnt(ModEntityTypes.QUEENANT.get(), carver.getLevel());
            pQueen.moveTo(Vec3.atCenterOf(colony.queenRoomPos));
            pQueen.setColonyID(carver.getColonyID());
            pQueen.setWorkingStage(WorkingStages.FARMING);
            pQueen.setHomePos(colony.queenRoomPos);
            carver.getLevel().addFreshEntity(pQueen);
            pQueen.memory.surfacePos = carver.blockPosition();
            pQueen.setFirstSurfacePos(carver.blockPosition());
        }
*/

        AntUtils.broadcastString(level,"Successfully generated colony. Carver placed " + numberOfBlocks + " blocks.");
    }
}
