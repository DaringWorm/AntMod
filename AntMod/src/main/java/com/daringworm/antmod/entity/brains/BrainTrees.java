package com.daringworm.antmod.entity.brains;

import com.daringworm.antmod.entity.brains.parts.AntPredicates;
import com.daringworm.antmod.entity.brains.parts.BrainFork;
import com.daringworm.antmod.entity.brains.parts.Braincell;
import com.daringworm.antmod.entity.brains.parts.WorkerBrainCells;
import com.daringworm.antmod.entity.custom.WorkerAnt;

public final class BrainTrees {
    private static final BrainFork workerAntMainFork = WorkerBrainCells.MASTER_FORK;

    public static Braincell getNextCell(WorkerAnt pAnt){
        return workerAntMainFork.testForNext(pAnt);
    }
}
