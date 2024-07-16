package com.daringworm.antmod.colony.misc;

import com.daringworm.antmod.goals.AntUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Set;

public class PosPair {
    public BlockPos top;
    public BlockPos bottom;
    static final double maxDistanceAllowed = 300d;
    public Level level;

     public PosPair(BlockPos top, BlockPos bottom, Level pLevel){
        this.top = top;
        this.bottom = bottom;
        this.level = pLevel;
    }

    public PosPair(BlockPos top, BlockPos bottom){
        this.top = top;
        this.bottom = bottom;
    }

    public boolean canConnectWithFloor(int distance){
        if(AntUtils.getDist(top,bottom)>distance){return false;}
        if(top == bottom){return true;}
        Set<BlockPos> lastSetT = new java.util.HashSet<>(Set.of());
        Set<BlockPos> currentSetT = new java.util.HashSet<>(Set.of());
        currentSetT.add(top);
        Set<BlockPos> toCheckSetB = new java.util.HashSet<>(Set.of());
        Set<BlockPos> lastSetB = new java.util.HashSet<>(Set.of());
        Set<BlockPos> currentSetB = new java.util.HashSet<>(Set.of());
        currentSetB.add(bottom);
        Set<BlockPos> toCheckSetT = new java.util.HashSet<>(Set.of());
        for(int i = 0; i < distance; i ++) {
            for(BlockPos tempPos : currentSetT){toCheckSetT.addAll(checkAdjacentsLinearStaired(tempPos,level));}
            toCheckSetT.removeAll(currentSetT);
            toCheckSetT.removeAll(lastSetT);
            if (toCheckSetT.isEmpty()) {return false;}
            for(BlockPos tempPos : toCheckSetT){if(currentSetB.contains(tempPos)){return true;}}
            lastSetT = currentSetT;
            currentSetT = toCheckSetT;
            toCheckSetT.clear();
            for(BlockPos tempPos : currentSetB){toCheckSetB.addAll(checkAdjacentsLinearStaired(tempPos,level));}
            toCheckSetB.removeAll(currentSetB);
            toCheckSetB.removeAll(lastSetB);
            if(toCheckSetT.isEmpty()){return false;}
            for(BlockPos tempPos : toCheckSetB){if(currentSetT.contains(tempPos)){return true;}}
            lastSetB = currentSetB;
            currentSetB = toCheckSetB;
            toCheckSetB.clear();

            //for(BlockPos temp : currentSet){/*level.setBlock(temp, Blocks.WHITE_CARPET.defaultBlockState(),2);*/}

        }
        return false;
    }

    private static Set<BlockPos> checkAdjacentsLinearStaired(BlockPos pos, LevelReader level){
        Set<BlockPos> returnSet = new java.util.HashSet<>(Set.of());
        for(Direction dir : Direction.values()) {
            BlockPos directToSide = pos.relative(dir);
            if (isPathfindableWithFloor(directToSide, level)) {
                returnSet.add(directToSide);
            } else if (isWalkableUnder(directToSide, level) && isPathfindableWithFloor(directToSide.below(),level)){
                returnSet.add(directToSide.below());
            }
            else if(isWalkableUnder(pos.above(),level) && isPathfindableWithFloor(directToSide.above(),level)){
                returnSet.add(directToSide.above());
            }
        }
        return returnSet;
    }

    private static boolean isPathfindableWithFloor(BlockPos pos, LevelReader level){
        return level.getBlockState(pos).isPathfindable(level,pos, PathComputationType.LAND) &&
                level.getBlockState(pos.below()).isFaceSturdy(level,pos.below(), Direction.UP, SupportType.RIGID);
    }

    private static boolean isWalkableUnder(BlockPos pos, LevelReader level){
        final VoxelShape SUPPORT_SHAPE = Shapes.join(Shapes.block(), Block.box(0.0D, 8.0D, 0.0D, 16.0D, 16.0D, 16.0D), BooleanOp.ONLY_FIRST);

        return !Shapes.joinIsNotEmpty(level.getBlockState(pos).getBlockSupportShape(level, pos).getFaceShape(Direction.DOWN), SUPPORT_SHAPE, BooleanOp.ONLY_SECOND);
    }
}
