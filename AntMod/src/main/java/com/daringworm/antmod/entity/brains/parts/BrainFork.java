package com.daringworm.antmod.entity.brains.parts;

import com.daringworm.antmod.entity.custom.WorkerAnt;

import java.util.ArrayList;


public class BrainFork {
    public int memLvl = 0;
    ArrayList<BrainFork> cells = new ArrayList<>();
    AntPredicate firstOrSecond;

    public BrainFork incrementMem(int current){
        this.memLvl = current + 1;
        return this;
    }

    public BrainFork(int memLvl, BrainFork first, BrainFork second, AntPredicate firstOrSecond){
        this.memLvl = memLvl;
        this.firstOrSecond = firstOrSecond;
        cells.add(first.incrementMem(memLvl));
        cells.add(second.incrementMem(memLvl));
    }
    public BrainFork(int memLvl){
        this.memLvl = memLvl;
    }

    public Braincell testForNext(WorkerAnt pAnt){

        if(cells.size() < 2){return WorkerBrainCells.ERROR_ALERT;}
        else {
            boolean test = firstOrSecond.test(pAnt);
            if (test) {
                BrainFork test1 = cells.get(0);
                if(test1 instanceof Braincell){return (Braincell) test1;}
                else{return test1.testForNext(pAnt);}
            } else {
                BrainFork test2 = cells.get(1);
                if(test2 instanceof Braincell){return (Braincell) test2;}
                else{return test2.testForNext(pAnt);}
            }
        }
    }

}
