package com.daringworm.antmod.entity.brains.parts;

import com.daringworm.antmod.entity.Ant;

import java.util.function.Predicate;

public class AntPredicate {

    private Predicate<Ant> predicate;


    public AntPredicate(Predicate<Ant> pPredicate){
        this.predicate = pPredicate;

    }
    public AntPredicate opposite(){
        this.predicate = this.predicate.negate();
        return this;
    }

    public boolean test(Ant pAnt) {
        return this.predicate.test(pAnt);
    }

}
