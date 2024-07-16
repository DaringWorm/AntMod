package com.daringworm.antmod.entity.brains;

import com.daringworm.antmod.entity.brains.memories.LeafCutterMemory;

import com.daringworm.antmod.entity.brains.parts.*;
import com.daringworm.antmod.entity.custom.WorkerAnt;
import com.daringworm.antmod.goals.AntUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

import java.util.ArrayList;

public final class LeafCutterWorkerBrain extends LeafCutterBrain{
    public static void run(WorkerAnt pAnt){
        if(pAnt.shouldRunBrain()) {
            Braincell cellToRun = BrainTrees.getNextCell(pAnt);


            boolean shouldMSG = false;

            //?
            pAnt.setBraincellStage(1);

            for (ServerPlayer player : pAnt.getLevel().getServer().getPlayerList().getPlayers()) {
                if (player.getMainHandItem().getItem() == Items.BLAZE_POWDER) {
                    player.sendMessage(new TextComponent(cellToRun.KEY + " " + pAnt.getBraincellStage()), player.getUUID());
                }
            }

            if (cellToRun != null && cellToRun != WorkerBrainCells.ERROR_ALERT) {
                cellToRun.run(pAnt);
                //AntUtils.broadcastString(pAnt.getLevel(), cellToRun.KEY + ' ' + mem.braincellStage);

            } else {
                WorkerBrainCells.ERROR_ALERT.run(pAnt);

            }
            pAnt.maxUpStep = 1.2f;
        }
    }
}
