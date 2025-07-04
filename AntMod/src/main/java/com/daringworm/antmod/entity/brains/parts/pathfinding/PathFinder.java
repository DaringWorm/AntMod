package com.daringworm.antmod.entity.brains.parts.pathfinding;

import com.daringworm.antmod.util.AntUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

import java.util.*;

public class PathFinder {
    public final BlockPos startPos;
    public final BlockPos endPos;
    private final ServerLevel pLevel;


    public PathFinder(BlockPos start, BlockPos end, ServerLevel pLevel){
        this.startPos = start;
        this.endPos = end;
        this.pLevel = pLevel;
    }

    /**
     * Finds the walkable walls surrounding the given pos.
     * **/
    public Direction[] findWalls(BlockPos pos){
        ArrayList<Direction> dirList = new ArrayList<>(6);

        for(Direction dir : Direction.values()){
            BlockPos relativePos = pos.relative(dir);
            if(pLevel.getBlockState(relativePos).isFaceSturdy(pLevel, pos, dir.getOpposite())){
                dirList.add(dir);
            }
        }

        Direction[] directions = new Direction[dirList.size()];
        return dirList.toArray(directions);
    }

    /**
     * Finds the searchable directions for the given PathNode.
     * **/
    public Direction[] nextSearchDirs(PathNode node){
        ArrayList<Direction> returnList = new ArrayList<>(6);

        if(node.isDiagonal()){
            returnList.addAll(List.of(Direction.values()));
            returnList.remove(node.facing);
            returnList.remove(node.facing.getOpposite());
            //returnList.removeIf(tempDir -> Stream.of(node.walls).noneMatch(d -> d == node.facing.getOpposite()));

            Direction[] returnDirs = new Direction[returnList.size()];
            return returnList.toArray(returnDirs);
        }
        else{
            ArrayList<Direction> wallList = new ArrayList<>(List.of(node.walls));

            for(Direction tempDir : Direction.values()) {
                if(wallList.contains(tempDir) || tempDir == node.facing.getOpposite()){
                    continue;
                }

                if (wallList.stream().noneMatch(d -> d != tempDir.getOpposite())) {
                    continue;
                }

                returnList.add(tempDir);
            }

            Direction[] returnDirs = new Direction[returnList.size()];
            return returnList.toArray(returnDirs);
        }

    }

    /**
     * Calculates the weight of the given node based on things like liquids or fire, for later comparisons in heaps.
     * **/
    public void calculateNodeWeight(PathNode node){
        int i = 0;
        FluidState fluidState = pLevel.getFluidState(node.pos);
        BlockPathTypes type = pLevel.getBlockState(node.pos).getBlockPathType(pLevel, node.pos);

        if(!fluidState.isEmpty()){
            i += 32;
            AntUtils.broadcastString(pLevel, "Found a fluid");
            if(!pLevel.getFluidState(node.pos.above()).isEmpty()){
                i += 69420;
            }
        }
        if(type != null && type.getDanger() != null){
            AntUtils.broadcastString(pLevel, "Found a danger");
            i += 128;
        }
        node.weight = i;
    }


    private boolean extendPath(DynamicNodeHeap primaryHeap, DynamicNodeHeap targetHeap, PathNode activeNode, HashMap<BlockPos, PathNode> posMap){
        for (Direction tempDir : this.nextSearchDirs(activeNode)) {
            BlockPos newPos = activeNode.pos.relative(tempDir);
            /*if (!posMap.containsKey(newPos)) {*/
            PathNode newNode = new PathNode(newPos, activeNode.isStartToEnd, this.findWalls(newPos), tempDir, activeNode);
            calculateNodeWeight(newNode);

            if (newNode.isDiagonal() && activeNode.isDiagonal()) {
                continue;
            }
            PathNode previousNode = posMap.put(newPos, newNode);
            if(previousNode == null) {
                primaryHeap.add(newNode, targetHeap.peek());
            }
            else if (previousNode.isStartToEnd != activeNode.isStartToEnd) {
                for (BlockPos pos : posMap.keySet()) {
                    //pLevel.setBlock(pos, Blocks.GLASS.defaultBlockState(), 2);
                }
                return true;
            }
            if (activeNode.isDiagonal()) {
                posMap.remove(activeNode.pos);
            }
        }
        return false;
    }

    /**
     * Finds a path, alternating between the start and end and guiding each side toward the other.
     * **/
    public PathNode calculatePath(int allowedSteps) {
        PathNode startToEndActive = new PathNode(startPos, true, findWalls(startPos), Direction.UP);
        PathNode endToStartActive = new PathNode(endPos, false, findWalls(endPos), Direction.UP);

        int estimatedSpace = (int) startPos.distSqr(endPos);

        HashMap<BlockPos, PathNode> posMap = new HashMap<>(estimatedSpace);
        posMap.put(startPos, startToEndActive);
        posMap.put(endPos, endToStartActive);

        DynamicNodeHeap startHeap = new DynamicNodeHeap(endToStartActive, estimatedSpace);
        DynamicNodeHeap endHeap = new DynamicNodeHeap(startToEndActive, estimatedSpace);

        startHeap.add(startToEndActive);
        endHeap.add(endToStartActive);

        int i = 0;
        long timeStartNanos = System.nanoTime();

        while (startToEndActive != null && endToStartActive != null && i < allowedSteps) {

            if (extendPath(startHeap, endHeap, startToEndActive, posMap) || extendPath(endHeap, startHeap, endToStartActive, posMap)) {

                startToEndActive = startHeap.remove();
                return resolvePath(startToEndActive, endToStartActive);
                //AntUtils.broadcastString(pLevel, "By searching " + posMap.size() + " poses in " + ((double)(System.nanoTime() - timeStartNanos)/1000000d) + " millis");
            }

            startToEndActive = startHeap.remove();
            endToStartActive = endHeap.remove();
            i++;
        }
        AntUtils.broadcastString(pLevel, "Failed Iterations: " + i + ", For Map size: " + posMap.size() + " and heap sizes (s/e): " + startHeap.size() + ", " + endHeap.size());
        return null;
    }

    /**
     * Returns the head of a linked list comprised of PathNodes.
     * **/
    private PathNode resolvePath(PathNode startNode, PathNode endNode){
        PathNode holder;

        while(startNode != null) {
            holder = startNode;
            startNode = startNode.previous;
            holder.previous = endNode;
            endNode = holder;
        }

        return endNode;
    }
}
