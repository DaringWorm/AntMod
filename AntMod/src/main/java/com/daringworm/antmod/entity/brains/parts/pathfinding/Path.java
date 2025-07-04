package com.daringworm.antmod.entity.brains.parts.pathfinding;

import com.daringworm.antmod.entity.Ant;
import com.daringworm.antmod.util.AntUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class Path {
    private PathNode nodeRoot;
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

    public boolean calculatePath(int allowedSteps){
        this.checkedForPath = true;
        this.nodeRoot = pathFinder.calculatePath(allowedSteps);
        this.hasPath = this.nodeRoot != null;
        return this.hasPath;
    }

    public PathNode getNextNode(Ant pAnt){
        PathNode tempNode = nodeRoot;
        PathNode returnNode = tempNode;
        BlockPos antPos = pAnt.blockPosition();

        while(tempNode.previous != null){

            if(AntUtils.getDist(antPos, tempNode.pos) <= AntUtils.getDist(antPos, returnNode.pos)){
                returnNode = tempNode;
            }

            tempNode = tempNode.previous;
        }

        if(returnNode.previous != null){
            returnNode = returnNode.previous;
        }

        return returnNode;
    }
}
