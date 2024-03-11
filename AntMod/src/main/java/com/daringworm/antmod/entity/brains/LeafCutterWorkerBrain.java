package com.daringworm.antmod.entity.brains;

import com.daringworm.antmod.entity.brains.memories.LeafCutterMemory;

import com.daringworm.antmod.entity.brains.parts.*;
import com.daringworm.antmod.entity.custom.WorkerAnt;
import com.daringworm.antmod.goals.AntUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

import java.util.ArrayList;

public final class LeafCutterWorkerBrain extends LeafCutterBrain{
    public static void run(WorkerAnt pAnt){

        Braincell cellToRun = pAnt.memory.cellToRun;
        if((pAnt.getLevel().getGameTime()%(pAnt.findDigit(pAnt.getId(),1)+1) == 0 || cellToRun == null) && pAnt.shouldRunBrain()) {
            pAnt.getLevel().getProfiler().push("update_memory");
            pAnt.memory.softRefresh(pAnt);
            pAnt.getLevel().getProfiler().pop();

            Braincell nextCell = BrainTrees.getNextCell(pAnt);
            boolean shouldMSG = false;

            if (nextCell != cellToRun) {
                pAnt.memory.braincellStage = 1;
                cellToRun = nextCell;
                pAnt.memory.cellToRun = nextCell;
                for (ServerPlayer player : pAnt.getLevel().getServer().getPlayerList().getPlayers()) {
                    if (player.getMainHandItem().getItem() == Items.BLAZE_POWDER) {
                        player.sendMessage(new TextComponent(cellToRun.KEY + " " + pAnt.memory.braincellStage), player.getUUID());
                    }
                }
            }
        }
        if (cellToRun != null) {
            cellToRun.run(pAnt);
        } else {
            WorkerBrainCells.ERROR_ALERT.run(pAnt);
        }

        pAnt.maxUpStep = 1.2f;
    }
}
