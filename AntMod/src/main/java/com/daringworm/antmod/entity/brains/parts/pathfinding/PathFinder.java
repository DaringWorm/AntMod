package com.daringworm.antmod.entity.brains.parts.pathfinding;

import com.daringworm.antmod.util.AntUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SupportType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public class PathFinder {
    private final BlockPos startPos;
    private final BlockPos endPos;
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
            if(pLevel.getBlockState(relativePos).isFaceSturdy(pLevel, pos, dir.getOpposite(), SupportType.CENTER)){
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
            returnList.removeIf(tempDir -> Stream.of(findWalls(node.pos.relative(tempDir))).noneMatch(d -> d == node.facing.getOpposite()));

            Direction[] returnDirs = new Direction[returnList.size()];
            return returnList.toArray(returnDirs);
        }
        else{
            ArrayList<Direction> wallList = new ArrayList<>(List.of(node.walls));

            for(Direction tempDir : Direction.values()) {
                if(!wallList.contains(tempDir) && wallList.stream().anyMatch(d -> d != tempDir && d != tempDir.getOpposite())){
                    returnList.add(tempDir);
                }
            }

            Direction[] returnDirs = new Direction[returnList.size()];
            return returnList.toArray(returnDirs);
        }

    }



    /**
     * Mutates the given startNode to a path. Returns true if it found a complete one, false otherwise.
     * **/
    public PathNode calculatePath(int allowedSteps){
        PathNode startNode = new PathNode(startPos, true, findWalls(startPos), Direction.UP);;
        PathNode endNode = new PathNode(endPos, false, findWalls(endPos), Direction.UP);

        LinkedList<PathNode> startToEndActive = new LinkedList<>();
        LinkedList<PathNode> endToStartActive = new LinkedList<>();
        startToEndActive.add(startNode);
        endToStartActive.add(endNode);

        HashMap<BlockPos, PathNode> posMap = new HashMap<>((int) Math.pow(startPos.distManhattan(endPos), 1));
        posMap.put(startPos, startNode);
        posMap.put(endPos, endNode);

        Comparator<PathNode> posComparer = getPathNodeComparator();

        PriorityQueue<PathNode> startQueue = new PriorityQueue<>(posComparer);
        PriorityQueue<PathNode> endQueue = new PriorityQueue<>(posComparer);
        //TODO: Worry about path types, like water and fire

        int i = 0;
        while(!startToEndActive.isEmpty() && !endToStartActive.isEmpty() && i < allowedSteps){
            int j = 0;

            while(!startToEndActive.isEmpty() && j < 10) {
                j++;
                startNode = startToEndActive.poll();

                for (Direction tempDir : this.nextSearchDirs(startNode)) {
                    BlockPos newPos = startNode.pos.relative(tempDir);
                    PathNode newNode = new PathNode(newPos, startNode.isStartToEnd, this.findWalls(newPos), tempDir, startNode);
                    if (!posMap.containsKey(newPos)) {
                        if (newNode.isDiagonal() && startNode.isDiagonal()) {
                            continue;
                        }
                        posMap.put(newPos, newNode);
                        startQueue.add(newNode);
                    } else if (!posMap.get(newPos).isStartToEnd) {
                        AntUtils.broadcastString(pLevel, "For searched " + posMap.size());

                        for(BlockPos pos : posMap.keySet()){
                            pLevel.setBlock(pos, Blocks.GLASS.defaultBlockState(), 2);
                        }
                        return resolvePath(posMap.get(newPos), newNode);
                    }
                }
                if(startNode.isDiagonal()){
                    posMap.remove(startNode.pos);
                }
            }

            j = 0;

            while(!endToStartActive.isEmpty() && j < 10) {
                j++;
                endNode = endToStartActive.poll();

                for (Direction tempDir : this.nextSearchDirs(endNode)) {
                    BlockPos newPos = endNode.pos.relative(tempDir);
                    PathNode newNode = new PathNode(newPos, endNode.isStartToEnd, this.findWalls(newPos), tempDir, endNode);
                    if (!posMap.containsKey(newPos)) {
                        if (newNode.isDiagonal() && endNode.isDiagonal()) {
                            continue;
                        }
                        posMap.put(newPos, newNode);
                        endQueue.add(newNode);
                    } else if (posMap.get(newPos).isStartToEnd) {
                        AntUtils.broadcastString(pLevel, "For searched " + posMap.size());

                        for(BlockPos pos : posMap.keySet()){
                            pLevel.setBlock(pos, Blocks.GLASS.defaultBlockState(), 2);
                        }
                        return resolvePath(newNode, posMap.get(newPos));
                    }
                }
                if(endNode.isDiagonal()){
                    posMap.remove(endNode.pos);
                }
            }

            j = 0;

            while(j < 1 && !startQueue.isEmpty() && !endQueue.isEmpty()){
                j++;
                i++;
                startToEndActive.add(startQueue.poll());
                endToStartActive.add(endQueue.poll());
            }
        }
        AntUtils.broadcastString(pLevel, "Failed Iterations: " + i + ", For Map size: " + posMap.size());

        return null;
    }

    private @NotNull Comparator<PathNode> getPathNodeComparator() {
        BlockPos middlePos = new BlockPos((startPos.getX() + endPos.getX())/2, (startPos.getY() + endPos.getY())/2, (startPos.getZ() + endPos.getZ())/2);
        Comparator<PathNode> posComparer = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                if(o1 instanceof PathNode node1 && o2 instanceof PathNode node2){

                    return (node1.pos.distSqr(middlePos) < node2.pos.distSqr(middlePos))? -1 : 1;

                    /*int x1 = node1.pos.getX();
                    int y1 = node1.pos.getY();
                    int z1 = node1.pos.getZ();

                    int x2 = node2.pos.getX();
                    int y2 = node2.pos.getY();
                    int z2 = node2.pos.getZ();*/
                }
                return 0;
            }
        };
        return posComparer;
    }


    private PathNode resolvePath(PathNode startNode, PathNode endNode){

        while(startNode != null){
            if(startNode.isDiagonal()){
                //AntUtils.broadcastString(pLevel, startNode.pos + startNode.facing.toString());
            }
            pLevel.setBlock(startNode.pos, Blocks.SEA_LANTERN.defaultBlockState(), 2);
            startNode = startNode.previous;
        }
        while(endNode != null){
            pLevel.setBlock(endNode.pos, Blocks.GLOWSTONE.defaultBlockState(), 2);
            endNode = endNode.previous;
        }
        pLevel.setBlock(startPos, Blocks.LAPIS_BLOCK.defaultBlockState(), 2);
        pLevel.setBlock(endPos, Blocks.REDSTONE_BLOCK.defaultBlockState(), 2);

        return null;
    }
}
