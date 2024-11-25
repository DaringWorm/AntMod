package com.daringworm.antmod.mixin.tomixin;

import com.daringworm.antmod.colony.AntColony;
import net.minecraft.core.BlockPos;

import java.util.Set;

public interface ServerLevelUtil {
    AntColony getColonyWithID(int pID);
    AntColony getFirstColony();
    AntColony getClosestColony(BlockPos position);
    void addColonyList(Set<AntColony> pSet);
    void refreshColonyForID(AntColony pColony);
    void addColonyToList(AntColony pColony);
}
