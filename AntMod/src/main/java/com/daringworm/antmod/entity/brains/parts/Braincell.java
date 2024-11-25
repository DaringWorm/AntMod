package com.daringworm.antmod.entity.brains.parts;

import com.daringworm.antmod.entity.Ant;

public class Braincell {

    public final String KEY;
    private final AntPredicate shouldChoose;
    private Braincell[] subCells = {};
    private Action[] actions = {};

    public Braincell(String key, AntPredicate shouldChoose){
        this.KEY = key;
        this.shouldChoose = shouldChoose;
    }

    public Braincell(String key, AntPredicate shouldChoose, Braincell[] subCells){
        this.KEY = key;
        this.shouldChoose = shouldChoose;
        this.subCells = subCells;
    }

    public Braincell(String key, AntPredicate shouldChoose, Action[] actions){
        this.KEY = key;
        this.shouldChoose = shouldChoose;
        this.actions = actions;
    }

    public Braincell(String key, AntPredicate shouldChoose, Braincell[] subCells, Action[] actions){
        this.KEY = key;
        this.shouldChoose = shouldChoose;
        this.subCells = subCells;
        this.actions = actions;
    }

    public Braincell setSubCells(Braincell[] newCells){
        this.subCells = newCells;
        return this;
    }

    public Braincell setActions(Action[] newActions){
        this.actions = newActions;
        return this;
    }

    public boolean shouldChoose(Ant pAnt){
        return this.shouldChoose.test(pAnt);
    }

    public void run(Ant pAnt){
        pAnt.setBrainPath(pAnt.getBrainPath() + " -> " + this.KEY);
        for(Action action : this.actions){
            action.run(pAnt);
        }
        for(Braincell cell : this.subCells){
            if(cell.shouldChoose(pAnt)){
                cell.run(pAnt);
                return;
            }
        }
    }

}
