package com.daringworm.antmod.entity.brains;

import com.daringworm.antmod.entity.brains.parts.WorkerBrainCells;
import com.daringworm.antmod.entity.custom.WorkerAnt;

public final class BrainTrees {


    //this is optional. The idea was to use method signatures to filter the different types of Ant.
    public static void runBrainFor(WorkerAnt pAnt){
        WorkerBrainCells.MAIN_FORK.run(pAnt);
    }
}
