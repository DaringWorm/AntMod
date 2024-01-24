package com.daringworm.antmod.entity.brains;

import com.daringworm.antmod.entity.brains.memories.LeafCutterMemory;

import com.daringworm.antmod.entity.brains.parts.*;
import com.daringworm.antmod.entity.custom.WorkerAnt;
import com.daringworm.antmod.goals.AntUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

import java.util.ArrayList;

public class LeafCutterWorkerBrain extends LeafCutterBrain{
    private Braincell cellToRun;
    private ArrayList<AntPredicate> predicateList = new ArrayList<>();
    private BrainFork masterFork = new BrainFork(0, WorkerBrainCells.AGGRO_MANAGER_FORK, WorkerBrainCells.MASTER_PASSIVE_FORK, AntPredicates.TARGET_EXISTS);
    private int wait = 0;

    public LeafCutterWorkerBrain(WorkerAnt pAnt){
    }

    public void run(WorkerAnt pAnt){
        if((pAnt.getLevel().getGameTime()%(pAnt.findDigit(pAnt.getId(),1)+1) == 0 || cellToRun == null) && pAnt.shouldRunBrain()) {
            pAnt.memory.softRefresh(pAnt);

            Braincell nextCell = masterFork.testForNext(pAnt);
            boolean shouldMSG = false;

            if (nextCell != cellToRun) {
                pAnt.memory.braincellStage = 1;
                cellToRun = nextCell;
                shouldMSG = true;
            }
            for (ServerPlayer player : pAnt.getLevel().getServer().getPlayerList().getPlayers()) {
                if (shouldMSG) {
                    //player.sendMessage(new TextComponent(cellToRun.KEY), player.getUUID());
                } else if (player.getMainHandItem().getItem() == Items.BLAZE_POWDER) {
                    player.sendMessage(new TextComponent(cellToRun.KEY + " " + pAnt.memory.braincellStage), player.getUUID());
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
