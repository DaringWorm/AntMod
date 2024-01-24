package com.daringworm.antmod.colony.misc;

import com.daringworm.antmod.colony.AntColony;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class LevelColonies<T extends AntColony> {

    private final Set<T> colonies;
    public ServerLevel sLevel;

    public LevelColonies(Set<T> pColonies) {
        this.colonies = pColonies;
    }

    public void add(T newColony){
        colonies.add(newColony);
    }

    public void clear(){colonies.clear();}


    public Stream<T> getColonies() {
        return this.colonies.stream();
    }

    public T getColonyForID(int pID){
        Stream<T> colonystream = this.colonies.stream();
        colonystream = colonystream.filter(p -> p.colonyID == pID);

        Optional<T> pReturn = colonystream.findFirst();
        colonystream.close();
        return pReturn.orElse(null);
    }

    public void update(AntColony toUpdateWith){
        int id = toUpdateWith.colonyID;
        List<T> toRemove = this.getColonies().filter(c -> c.colonyID == id).toList();
        toRemove.forEach(colonies::remove);
        colonies.add((T) toUpdateWith);
    }

    public boolean isEmpty() {
        return this.colonies.isEmpty();
    }

    public int size(){return this.colonies.size();}

    public ArrayList<PosSpherePair> getNextExcavationList(int id){
        AntColony pColony = getColonyForID(id);
        return null;//pColony.getNextExcavationSteps();
    }

}
