package com.daringworm.antmod.colony;

import com.daringworm.antmod.colony.misc.ColonyBranch;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;

public final class ColonyGenerationBuffer {
    public static ArrayList<ColonyBranch> looseBranches = new ArrayList<>();

    public static ColonyBranch getBranchForPos(BlockPos pos){
        for(ColonyBranch branch : looseBranches){
            if(pos == branch.getPos()){
                return branch;
            }
        }
        return null;
    }

    public static void tryToAdd(ColonyBranch branch){
        if(!looseBranches.contains(branch)){
            looseBranches.add(branch);
        }
    }
}
