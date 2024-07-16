package com.daringworm.antmod.entity.brains.parts;

import com.daringworm.antmod.entity.Ant;

import java.util.ArrayList;


public class BrainFork {
    ArrayList<BrainFork> cells = new ArrayList<>();
    public final AntPredicate shouldChoose;
    public final String KEY;


    public BrainFork(AntPredicate shouldChoose, String key){
        this.shouldChoose = shouldChoose;
        this.KEY = key;
    }

    public BrainFork add(BrainFork cell){
        this.cells.add(cell);
        return this;
    }

    public BrainFork addAll(ArrayList<BrainFork> array){
        cells.addAll(array);
        return this;
    }

    public boolean test(Ant pAnt){return shouldChoose.test(pAnt);}

    public Braincell testForNext(Ant pAnt){
        if(this instanceof Braincell){
            return ((Braincell) this);
        }

        for(BrainFork tempFork : cells){
            if(tempFork.test(pAnt)){
                return tempFork.testForNext(pAnt);
            }
        }

        pAnt.setErrorMessage("Ant's brain has a null ending: no Valid shouldSelect predicates. The last fork reached is " + this.KEY);
        return WorkerBrainCells.ERROR_ALERT;
    }
}
