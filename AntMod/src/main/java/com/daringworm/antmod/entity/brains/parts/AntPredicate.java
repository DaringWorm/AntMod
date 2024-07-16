package com.daringworm.antmod.entity.brains.parts;

import com.daringworm.antmod.entity.Ant;

import java.util.function.Predicate;

public class AntPredicate {

    private Predicate<Ant> predicate;


    public AntPredicate(Predicate<Ant> pPredicate){
        this.predicate = pPredicate;

    }
    public AntPredicate opposite(){
        return new AntPredicate(this.predicate.negate());
    }

    public AntPredicate and(AntPredicate other){
        return new AntPredicate(this.predicate.and(other.predicate));
    }

    public boolean test(Ant pAnt) {
        return this.predicate.test(pAnt);
    }

}
