package com.daringworm.antmod.entity.brains;

import com.daringworm.antmod.entity.brains.parts.AntPredicates;
import com.daringworm.antmod.entity.brains.parts.BrainFork;
import com.daringworm.antmod.entity.brains.parts.Braincell;
import com.daringworm.antmod.entity.brains.parts.WorkerBrainCells;
import com.daringworm.antmod.entity.custom.WorkerAnt;

import java.util.ArrayList;

public final class BrainTrees {

    public static Braincell getNextCell(WorkerAnt pAnt){
        return WorkerBrainCells.MAIN_FORK.testForNext(pAnt);
    }
}
