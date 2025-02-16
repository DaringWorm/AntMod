package com.daringworm.antmod.entity.brains;

import com.daringworm.antmod.colony.misc.BlockPosStringifier;
import com.daringworm.antmod.entity.custom.QueenAnt;
import com.daringworm.antmod.entity.custom.WorkerAnt;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

public final class LeafCutterQueenBrain extends LeafCutterBrain{

    public static void run(QueenAnt pAnt){
        if(pAnt.shouldRunBrain()) {
            pAnt.setBrainPath("Brain");
            BrainTrees.runBrainFor(pAnt);


            boolean shouldMSG = false;

            //?
            pAnt.setBraincellStage(1);

            if(pAnt.getLevel().getServer() != null) {

                for (ServerPlayer player : pAnt.getLevel().getServer().getPlayerList().getPlayers()) {
                    if (player.getOffhandItem().getItem() == Items.DEBUG_STICK && pAnt.distanceToSqr(player) < 4d) {
                        player.sendMessage(new TextComponent(BlockPosStringifier.jsonFromPos(pAnt.blockPosition()) + " " + pAnt.getBrainPath()), player.getUUID());
                        //player.sendMessage(new TextComponent(""+BlockPosStringifier.jsonFromPos(player.blockPosition())), player.getUUID());
                    }
                }
            }
        }
    }
}
