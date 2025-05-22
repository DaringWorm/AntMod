package com.daringworm.antmod.mixin.tomixin;

import com.daringworm.antmod.colony.AntColony;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public interface ServerLevelUtil {
    ArrayList<AntColony> getColonies();
    AntColony getColonyWithID(int pID);
    AntColony getFirstColony();
    AntColony getClosestColony(BlockPos position);
    void addColonyList(Set<AntColony> pSet);
    void refreshColonyForID(AntColony pColony);
    void addColonyToList(AntColony pColony);
    void removeColonyFromList(AntColony pColony);
    int getNumberOfColonies();
    void loadColoniesFromFile(ServerLevel pLevel) throws IOException;
}
