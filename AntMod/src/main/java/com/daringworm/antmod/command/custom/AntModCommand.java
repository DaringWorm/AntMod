package com.daringworm.antmod.command.custom;

import com.daringworm.antmod.colony.AntColony;
import com.daringworm.antmod.colony.misc.BlockPosStringifier;
import com.daringworm.antmod.entity.Ant;
import com.daringworm.antmod.entity.custom.*;
import com.daringworm.antmod.mixin.tomixin.ServerLevelUtil;
import com.daringworm.antmod.util.AntUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;

public class AntModCommand {
    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(Commands.literal("antmod").requires((p_139171_) -> {
            return p_139171_.hasPermission(2);
        }).
                then(Commands.literal("colonies").
                    then(Commands.literal("get_nearest_colony").executes(AntModCommand::getNearestColony)).
                        then(Commands.literal("despawn_ants").executes(AntModCommand::despawnAnts)).
                        then(Commands.literal("respawn_ants").executes(AntModCommand::respawnAnts)).
                        then(Commands.literal("delete_colony").executes(AntModCommand::deleteColony)).
                        then(Commands.literal("generate_colony_here").executes(AntModCommand::spawnColony))).

                then(Commands.literal("ants").
                        then(Commands.literal("avg_ticks_since_last_nav").executes(AntModCommand::getAverageTicksSinceLastWalked)).
                        then(Commands.literal("freeze_all_ai").executes(AntModCommand::freezeAnts)).
                        then(Commands.literal("list_colony_ids").executes(AntModCommand::getColonyIDs)).
                        then(Commands.literal("list_surface_poses").executes(AntModCommand::getAntStartPos)).
                        then(Commands.literal("list_gounderground_lists").executes(AntModCommand::getAntGoUndergroundList))));
    }


    private static int getNearestColony(CommandContext<CommandSourceStack> context){
        ServerLevel pLevel = context.getSource().getLevel();
        AntColony colony = ((ServerLevelUtil)pLevel).getClosestColony(context.getSource().getEntity().blockPosition());
        if(colony != null) {
            AntUtils.broadcastString(pLevel, "Colony with start pos = " +
                    BlockPosStringifier.jsonFromPos(colony.startPos) +
                    " has ID = " + colony.colonyID);
        }
        else{
            AntUtils.broadcastString(pLevel, "no colonies exist");
        }
        return 0;
    }

    private static int getAverageTicksSinceLastWalked(CommandContext<CommandSourceStack> context){
        ServerLevel pLevel = context.getSource().getLevel();
        Iterable<Entity> entityList = pLevel.getEntities().getAll();

        int total_time = 0;
        int total_ants = 0;

        for(Entity tempEntity : entityList) {
            if (tempEntity instanceof WorkerAnt) {
                total_time += ((WorkerAnt) tempEntity).getWalkingCooldown();
                total_ants++;
            }
        }

        AntUtils.broadcastString(pLevel, "Average time since walking = " +
                ((float)total_time)/ ((float)total_ants) +
                " for " + total_ants + " ants.");

        return 0;
    }

    private static int freezeAnts(CommandContext<CommandSourceStack> context){
        ServerLevel pLevel = context.getSource().getLevel();
        Iterable<Entity> entityList = pLevel.getEntities().getAll();

        for(Entity tempEntity : entityList) {
            if (tempEntity instanceof WorkerAnt) {
                ((WorkerAnt) tempEntity).setShouldRunBrain(!((WorkerAnt) tempEntity).getShouldRunBrain());
            }
        }

        return 0;
    }

    private static int getColonyIDs(CommandContext<CommandSourceStack> context){
        ServerLevel pLevel = context.getSource().getLevel();
        Iterable<Entity> entityList = pLevel.getEntities().getAll();
        ArrayList<Integer> ids = new ArrayList<>();
        int antCount = 0;

        for(Entity tempEntity : entityList) {
            if (tempEntity instanceof WorkerAnt) {
                antCount++;
                int id = ((WorkerAnt) tempEntity).getColonyID();
                if(!ids.contains(id)){
                    ids.add(id);
                    AntUtils.broadcastString(pLevel, "" + id);
                }
            }
        }

        AntUtils.broadcastString(pLevel, "Colony ID list is " + ids.size() + " long for " + antCount + " loaded ants.");

        return 0;
    }

    private static int respawnAnts(CommandContext<CommandSourceStack> context){
        ServerLevel pLevel = context.getSource().getLevel();
        AntColony colony = ((ServerLevelUtil)pLevel).getClosestColony(context.getSource().getEntity().blockPosition());
        if(colony != null) {
            colony.hasSpawnedAnts = false;
        }

        return 0;
    }


    private static int deleteColony(CommandContext<CommandSourceStack> context){
        ServerLevel pLevel = context.getSource().getLevel();
        AntColony colony = ((ServerLevelUtil)pLevel).getClosestColony(context.getSource().getEntity().blockPosition());
        if(colony != null) {
            AntUtils.broadcastString(pLevel, "Removed colony with ID " + colony.colonyID +
                    " centered at " + BlockPosStringifier.jsonFromPos(colony.startPos) +
                    " from a list of " + ((ServerLevelUtil)pLevel).getNumberOfColonies() +
                    " colonies.");
            ((ServerLevelUtil)pLevel).removeColonyFromList(colony);
        }
        else{
            AntUtils.broadcastString(pLevel, "couldn't find a colony to delete");
        }
        return 0;
    }

    private static int spawnColony(CommandContext<CommandSourceStack> context){
        ServerLevel pLevel = context.getSource().getLevel();
        AntColony colony = new AntColony(pLevel, pLevel.getRandom().nextInt(), context.getSource().getEntity().blockPosition());

        colony.generateWholeColony();

        return 0;
    }

    private static int despawnAnts(CommandContext<CommandSourceStack> context){
        ServerLevel pLevel = context.getSource().getLevel();
        AntColony colony = ((ServerLevelUtil)pLevel).getClosestColony(context.getSource().getEntity().blockPosition());
        Iterable<Entity> entityList = pLevel.getEntities().getAll();
        int workerCount = 0;
        int queenCount = 0;
        int eggCount = 0;
        int cloudCount = 0;

        if(colony != null) {
            for (Entity tempEntity : entityList) {
                if(tempEntity instanceof Ant && ((Ant)tempEntity).getColonyID() == colony.colonyID){
                    tempEntity.kill();
                    if (tempEntity instanceof WorkerAnt) {
                        workerCount++;
                    }
                    else if(tempEntity instanceof QueenAnt){
                        queenCount++;
                    }
                    else if(tempEntity instanceof AntEgg){
                        eggCount++;
                    }
                    else if(tempEntity instanceof AntScentCloud){
                        cloudCount++;
                    }
                }

            }
            AntUtils.broadcastString(pLevel, "Despawned " + workerCount +
                    " worker ants, " + queenCount +
                    " queen ants, " + eggCount +
                    " eggs, " + cloudCount +
                    " scent clouds " +
                    "for the colony with ID " + colony.colonyID +
                    " who's start pos is " + BlockPosStringifier.jsonFromPos(colony.startPos));
        }
        else{
            AntUtils.broadcastString(pLevel, "no colony found");
        }

        return 0;
    }

    private static int getAntStartPos(CommandContext<CommandSourceStack> context){
        ServerLevel pLevel = context.getSource().getLevel();
        Iterable<Entity> entityList = pLevel.getEntities().getAll();
        ArrayList<String> posList = new ArrayList<>();
        int ants = 0;

        for(Entity tempEntity : entityList){
            if(tempEntity instanceof Ant pAnt){
                ants++;
                String pos = BlockPosStringifier.jsonFromPos(pAnt.getSurfacePos()).toString();
                if(!posList.contains(pos)){
                    posList.add(pos);
                    AntUtils.broadcastString(pLevel, pos + " for ant with colony ID " + pAnt.getColonyID());
                }
            }
        }

        AntUtils.broadcastString(pLevel, "For " + ants + " ants.");

        return 0;
    }

    private static int getAntGoUndergroundList(CommandContext<CommandSourceStack> context){
        ServerLevel pLevel = context.getSource().getLevel();
        Iterable<Entity> entityList = pLevel.getEntities().getAll();
        ArrayList<String> listList = new ArrayList<>();
        int ants = 0;


        for(Entity tempEntity : entityList){
            if(tempEntity instanceof Ant pAnt){
                ants++;
                String str = "start";
                ArrayList<BlockPos> posList = pAnt.getGoUndergroundList();

                for(BlockPos tempPos : posList){
                    str = str.concat(" -> " + BlockPosStringifier.jsonFromPos(tempPos));
                }

                if(!listList.contains(str)){
                    listList.add(str);
                    AntUtils.broadcastString(pLevel, str + " for ant with colony ID " + pAnt.getColonyID());
                }
            }
        }

        AntUtils.broadcastString(pLevel, "For " + ants + " ants.");


        return 0;
    }


    //this works:
    /*public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(Commands.literal("antmod").requires((p_139171_) -> {
            return p_139171_.hasPermission(2);
        }).then(Commands.literal("get_nearest_colony_id").executes(AntModCommand::getNearestColony)));
    }*/

}
