package com.daringworm.antmod.entity.brains.parts;

import com.daringworm.antmod.entity.Ant;
import com.daringworm.antmod.entity.custom.WorkerAnt;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;


public class BrainFork {
    public int memLvl = 0;
    ArrayList<BrainFork> cells = new ArrayList<>();
    AntPredicate firstOrSecond;
    AntPredicate shouldChoose = AntPredicates.TRUE;
    public String KEY;

    public BrainFork incrementMem(int current){
        this.memLvl = current + 1;
        return this;
    }

    public BrainFork(AntPredicate firstOrSecond){
        this.firstOrSecond = firstOrSecond;
    }

    public BrainFork(AntPredicate firstOrSecond, AntPredicate shouldChoose){
        this.firstOrSecond = firstOrSecond;
        this.shouldChoose = shouldChoose;
    }

    public BrainFork add(BrainFork cell){
        cells.add(cell);
        return this;
    }

    public BrainFork addKey(String str){
        this.KEY = str;
        return this;
    }

    public BrainFork addAll(ArrayList<BrainFork> array){
        cells.addAll(array);
        return this;
    }

    public BrainFork(int memLvl){
        this.memLvl = memLvl;
    }

    public boolean test(Ant pAnt){return shouldChoose.test(pAnt);}

    public Braincell testForNext(Ant pAnt){
        if(this instanceof Braincell){
            return (Braincell) this;
        }
        else if(cells.size() < 2){
            pAnt.memory.errorAlertString = "Ant's brain has a fork with just " + cells.size() + " options.";
            if(this.KEY != null){pAnt.memory.errorAlertString = pAnt.memory.errorAlertString + " The fork's key is " + '"' + this.KEY + '"';}
            return WorkerBrainCells.ERROR_ALERT;
        }
        else if(cells.size() == 2){
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
        else{
            for(BrainFork fork : cells){
                if(fork.test(pAnt)){return fork.testForNext(pAnt);}
            }
        }
        pAnt.memory.errorAlertString = "Ant's brain has a null ending: no Valid shouldSelect predicates.";
        return WorkerBrainCells.ERROR_ALERT;
    }
}
