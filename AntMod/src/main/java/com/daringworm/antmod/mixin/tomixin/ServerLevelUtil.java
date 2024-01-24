package com.daringworm.antmod.mixin.tomixin;

import com.daringworm.antmod.colony.AntColony;

import java.util.Set;

public interface ServerLevelUtil {
    AntColony getColonyWithID(int pID);
    void addColonyList(Set<AntColony> pSet);
    void refreshColonyForID(AntColony pColony);
    void addColonyToList(AntColony pColony);
}
