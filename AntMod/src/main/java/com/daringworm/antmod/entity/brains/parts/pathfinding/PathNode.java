package com.daringworm.antmod.entity.brains.parts.pathfinding;

import com.daringworm.antmod.colony.misc.BlockPosStringifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

public class PathNode implements Comparable<PathNode>{
    public int distance;
    public final BlockPos pos;
    public PathNode previous;
    public final boolean isStartToEnd;
    public final Direction[] walls;
    public final Direction facing;

    @Override
    public int compareTo(@NotNull PathNode o) {
        return pos.compareTo(o.pos);
    }

    public boolean isDiagonal(){
        return this.walls.length == 0;
    }

    public PathNode(BlockPos pos, boolean isStartToEnd, Direction[] walls, Direction facing){
        this.pos = pos;
        this.distance = 0;
        this.isStartToEnd = isStartToEnd;
        this.walls = walls;
        this.facing = facing;
    }

    public PathNode(BlockPos pos, boolean isStartToEnd, Direction[] walls, Direction facing, int distance){
        this.pos = pos;
        this.distance = distance;
        this.isStartToEnd = isStartToEnd;
        this.walls = walls;
        this.facing = facing;
    }

    public PathNode(BlockPos pos, boolean isStartToEnd, Direction[] walls, Direction facing, PathNode parent){
        this.pos = pos;
        this.previous = parent;
        this.isStartToEnd = isStartToEnd;
        this.distance = parent.distance + 1;
        this.walls = walls;
        this.facing = facing;
    }

    @Override
    public String toString(){
        return "[pos = " + BlockPosStringifier.getTagForPos(pos) + ", dist = " + distance + ']';
    }
}