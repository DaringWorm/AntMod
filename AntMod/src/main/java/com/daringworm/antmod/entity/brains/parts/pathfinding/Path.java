package com.daringworm.antmod.entity.brains.parts.pathfinding;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class Path {
    public PathNode nodeRoot;
    public final BlockPos startPos;
    public final BlockPos endPos;
    public boolean hasPath;
    public boolean checkedForPath;
    public final PathFinder pathFinder;


    public Path(BlockPos startPos, BlockPos endPos, ServerLevel level){
        this.pathFinder = new PathFinder(startPos, endPos, level);
        this.nodeRoot = new PathNode(startPos, true, pathFinder.findWalls(startPos), Direction.UP);
        this.startPos = startPos;
        this.endPos = endPos;
        this.checkedForPath = false;
        this.hasPath = false;
    }

    public boolean calculatePath(ServerLevel level, int allowedSteps){
        this.checkedForPath = true;
        this.hasPath = pathFinder.calculatePath(allowedSteps) != null;
        return this.hasPath;
    }
}
