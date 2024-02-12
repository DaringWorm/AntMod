package com.daringworm.antmod.colony;

import com.daringworm.antmod.colony.misc.ColonyBranch;
import com.daringworm.antmod.mixin.tomixin.ServerLevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;

public class ColonyBuffer {
    public static ArrayList<ColonyBranch> BRANCHES_BUFFER = new ArrayList<>();

    public static ColonyBranch getBranchForPos(BlockPos pos){
        for(ColonyBranch branch : BRANCHES_BUFFER){
            if(branch.getPos() == pos){
                return branch;
            }
        }
        return AntColony.generateNewTunnels(pos);
    }

    public static void addColonyToLevel(BlockPos startPos, ServerLevel pLevel){
        ColonyBranch branchToAdd = null;

        for(ColonyBranch branch : BRANCHES_BUFFER){
            if(branch.getPos() == startPos){
                branchToAdd = branch;
                break;
            }
        }
        if(branchToAdd != null && pLevel != null){
            AntColony colony = new AntColony(pLevel,pLevel.getRandom().nextInt(),startPos);
            colony.tunnels = branchToAdd;
            colony.hasSpawnedAnts = false;
            ((ServerLevelUtil)pLevel).addColonyToList(colony);
        }
    }
}
