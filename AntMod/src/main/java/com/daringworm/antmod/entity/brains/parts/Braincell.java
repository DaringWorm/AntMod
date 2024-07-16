package com.daringworm.antmod.entity.brains.parts;

import com.daringworm.antmod.entity.Ant;

import java.util.ArrayList;

public class Braincell extends BrainFork{

    private ArrayList<Action> actions = new ArrayList<>();

    public Braincell(AntPredicate isValidChoice, String key){
        super(isValidChoice,key);
    }

    public void run(Ant pAnt){
        if(actions.isEmpty()){
            pAnt.setErrorMessage("The braincell " + this.KEY + " has no available actions.");
            Actions.ERROR_MSG_ACTION.run(pAnt);
            return;
        }

        int braincellStage = pAnt.getBraincellStage();

        if(braincellStage > this.actions.size()-1){
            braincellStage = 0;
            pAnt.setBraincellStage(braincellStage);
        }

        actions.get(braincellStage).run(pAnt);
        pAnt.setBraincellStage(++braincellStage);
    }

    public Braincell addActions(ArrayList<Action> actions){
        this.actions = actions;
        return this;
    }

    public Braincell addAction(Action action){
        actions.add(action);
        return this;
    }
}
