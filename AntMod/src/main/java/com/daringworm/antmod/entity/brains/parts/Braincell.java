package com.daringworm.antmod.entity.brains.parts;

import com.daringworm.antmod.entity.Ant;

public class Braincell extends BrainFork{
    private boolean requiresProgress = false;
    private boolean has3Stages;
    private Action action;
    private AntPredicate middlePredicate;
    private Action middleAction;
    private Action endAction;



    public boolean requiresProgressWait(){return this.requiresProgress;}

    public Braincell addShouldChoosePredicate(AntPredicate predicate){
        this.shouldChoose =predicate;
        return this;
    }

    public Braincell(Action startAction, String key){
        super(1);
        this.action = startAction;
        this.has3Stages = false;
        this.KEY = key;

    }

    public Braincell(Action startAction, Action midAction, AntPredicate midPredicate, Action lastAction, String key){
        super(1);
        this.action = startAction;
        this.has3Stages = true;
        this.middleAction = midAction;
        this.middlePredicate = midPredicate;
        this.endAction = lastAction;
        this.requiresProgress = true;
        this.KEY = key;
    }

    public void run(Ant pAnt){
        int whichStage = pAnt.memory.braincellStage;
        if(whichStage == 1 || !has3Stages){
            this.action.run(pAnt);
            if(has3Stages) {
                pAnt.memory.braincellStage = whichStage + 1;
                whichStage++;
            }
        }
        if(has3Stages){
            if(whichStage == 2){
                this.middleAction.run(pAnt);
                if(middlePredicate.test(pAnt)){
                    pAnt.memory.braincellStage = whichStage + 1;
                    whichStage++;
                    this.endAction.run(pAnt);
                }
            }
        }
        if(!has3Stages || whichStage == 3){
            //run mem modifiers here
        }
    }
}
