package com.daringworm.antmod.entity.brains;

import com.daringworm.antmod.colony.misc.BlockPosStringifier;
import com.daringworm.antmod.entity.brains.parts.WorkingStages;
import com.daringworm.antmod.entity.custom.WorkerAnt;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

public final class LeafCutterWorkerBrain extends LeafCutterBrain{
    public static void run(WorkerAnt pAnt){
        if(pAnt.shouldRunBrain()) {
            pAnt.setBrainPath("Brain");
            BrainTrees.runBrainFor(pAnt);


            boolean shouldMSG = false;

            //?
            pAnt.setBraincellStage(1);

            for (ServerPlayer player : pAnt.getLevel().getServer().getPlayerList().getPlayers()) {
                if (player.getMainHandItem().getItem() == Items.BLAZE_POWDER && pAnt.distanceToSqr(player) < 4d) {
                    player.sendMessage(new TextComponent(BlockPosStringifier.jsonFromPos(pAnt.blockPosition()) + " " + pAnt.getBrainPath()), player.getUUID());
                    //player.sendMessage(new TextComponent(""+BlockPosStringifier.jsonFromPos(player.blockPosition())), player.getUUID());
                }
            }
        }
    }
}
