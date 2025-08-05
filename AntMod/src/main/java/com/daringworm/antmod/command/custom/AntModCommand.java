package com.daringworm.antmod.command.custom;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.block.custom.FungalCore;
import com.daringworm.antmod.colony.AntColony;
import com.daringworm.antmod.colony.misc.BlockPosStringifier;
import com.daringworm.antmod.colony.misc.ColonyBranch;
import com.daringworm.antmod.entity.Ant;
import com.daringworm.antmod.entity.brains.parts.pathfinding.PathFinder;
import com.daringworm.antmod.entity.brains.parts.pathfinding.PathNode;
import com.daringworm.antmod.entity.custom.*;
import com.daringworm.antmod.mixin.tomixin.ServerLevelUtil;
import com.daringworm.antmod.util.AntUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;


public class AntModCommand {
    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(Commands.literal("antmod").requires((p_139171_) -> {
            return p_139171_.hasPermission(2);
        }).
                then(Commands.literal("colonies").
                    then(Commands.literal("get_nearest_colony").executes(AntModCommand::getNearestColony)).
                        then(Commands.literal("despawn_ants").executes(AntModCommand::despawnAnts)).
                        then(Commands.literal("respawn_ants").executes(AntModCommand::respawnAnts)).
                        then(Commands.literal("delete_colony").
                                then(Commands.literal("nearest").executes(AntModCommand::deleteColony)).
                                then(Commands.literal("all").executes(AntModCommand::deleteAllColonies))).
                        then(Commands.literal("generate_colony_here").executes(AntModCommand::spawnColony))).

                then(Commands.literal("ants").
                        then(Commands.literal("avg_ticks_since_last_nav").executes(AntModCommand::getAverageTicksSinceLastWalked)).
                        then(Commands.literal("freeze_all_ai").executes(AntModCommand::freezeAnts)).
                        then(Commands.literal("list_colony_ids").executes(AntModCommand::getColonyIDs)).
                        then(Commands.literal("list_surface_poses").executes(AntModCommand::getAntStartPos)).
                        then(Commands.literal("list_gounderground_lists").executes(AntModCommand::getAntGoUndergroundList))).

                then(Commands.literal("branches").
                        then(Commands.literal("add_to_nearest").executes(AntModCommand::createAndAddToNearestBranch)).
                        then(Commands.literal("create_new").
                                then(Commands.literal("branch").executes(AntModCommand::createBranchAtPos)).
                                then(Commands.literal("blueprint").executes(AntModCommand::createBranchBlueprint))).
                        then(Commands.literal("print_nearest").executes(AntModCommand::printNearestBranch))).
                then(Commands.literal("fungus").
                        then(Commands.literal("grow_here").executes(AntModCommand::growFungusHere))).
                then(Commands.literal("testing_dummies").
                        then(Commands.literal("1").executes(AntModCommand::dummy1)).
                        then(Commands.literal("2").executes(AntModCommand::dummy2)).
                        then(Commands.literal("3").executes(AntModCommand::dummy3)).
                        then(Commands.literal("4").then(Commands.argument( "vec3", Vec3Argument.vec3()).executes((context)-> {
                            return dummy4(context,
                                    Vec3Argument.getVec3(context, "vec3"));
                        })))

                ));
    }

    private static int dummy1(CommandContext<CommandSourceStack> context){
        ServerLevel pLevel = context.getSource().getLevel();
        ServerPlayer player = (ServerPlayer) context.getSource().getEntity();
        BlockPos pPos = player.blockPosition();

        CmdStatic.BP0 = pPos;

        AntUtils.broadcastString(pLevel, "StartPos set to: " + pPos);

        return 0;
    }

    private static int dummy2(CommandContext<CommandSourceStack> context){
        ServerLevel pLevel = context.getSource().getLevel();
        ServerPlayer player = (ServerPlayer) context.getSource().getEntity();
        BlockPos pPos = player.blockPosition();

        CmdStatic.BP1 = pPos;

        AntUtils.broadcastString(pLevel, "EndPos set to: " + pPos);

        return 0;
    }

    private static int dummy3(CommandContext<CommandSourceStack> context){
        ServerLevel pLevel = context.getSource().getLevel();
        ServerPlayer player = (ServerPlayer) context.getSource().getEntity();
        BlockPos pPos = player.blockPosition();
        PathFinder finder = new PathFinder(CmdStatic.BP0, CmdStatic.BP1, pLevel);
        //PathNode node = new PathNode(CmdStatic.BP0, true, finder.findWalls(pPos), Direction.UP, 0);

        PathNode result = finder.calculatePath(30000);

        while(result != null){
            BlockState tempState = Blocks.LIGHTNING_ROD.defaultBlockState().setValue(LightningRodBlock.FACING, result.facing);

            pLevel.setBlock(result.pos, tempState, 2);

            result = result.previous;
        }

        return 0;
    }

    private static int dummy4(CommandContext<CommandSourceStack> context, Vec3 vec){
        ServerLevel pLevel = context.getSource().getLevel();
        CmdStatic.int0 = (int) vec.x;
        CmdStatic.int1 = (int) vec.y;
        CmdStatic.int2 = (int) vec.z;
        AntUtils.broadcastString(pLevel, "Rotation vector set to [" + CmdStatic.int0 + ", " + CmdStatic.int1 + ", " + CmdStatic.int2 + "]");
        return 0;
    }


    private static int createBranchAtPos(CommandContext<CommandSourceStack> context){
        ServerLevel pLevel = context.getSource().getLevel();
        BlockPos pos = context.getSource().getEntity().blockPosition().below(2);

        ColonyBranch colonyBranch = new ColonyBranch(pos);
        ColonyBranch.testing.add(colonyBranch);

        colonyBranch.carve(pLevel);
        AntUtils.broadcastString(pLevel, colonyBranch.toString());

        return 0;
    }

    private static int createAndAddToNearestBranch(CommandContext<CommandSourceStack> context){
        ServerLevel pLevel = context.getSource().getLevel();
        BlockPos pos = context.getSource().getEntity().blockPosition().below(2);

        ColonyBranch nearest = ColonyBranch.getNearest_testing(pos);
        ColonyBranch colonyBranch = new ColonyBranch(pos);

        if(nearest == null){
            AntUtils.broadcastString(pLevel,"couldn't find a branch to add on to");
            return 0;
        }
        nearest.addChild(colonyBranch);
        colonyBranch.carve(pLevel);
        ColonyBranch.testing.add(colonyBranch);
        AntUtils.broadcastString(pLevel, colonyBranch + " was added to:\n" + nearest);

        return 0;
    }

    private static int createBranchBlueprint(CommandContext<CommandSourceStack> context){
        long startTime = System.nanoTime();
        ServerLevel pLevel = context.getSource().getLevel();
        BlockPos pos = context.getSource().getEntity().blockPosition();

        AntColony colony = new AntColony(pLevel, 0, pos);
        colony.tunnels.carve(pLevel);

        AntUtils.broadcastString(pLevel, colony.tunnels.getExcavationSpheres().size() + " spheres.");

        int num = 0;

        //////////
        for(BlockPos tempPos : BlockPos.betweenClosed(pos.offset(20, -3, 20), pos.offset(-20, 20, -20))){
            double horizontalDistance = AntUtils.getHorizontalDist(tempPos, pos);
            double verticalDistance = tempPos.getY() - pos.getY() + 2;
            double slope = 0.8d;

            if((horizontalDistance - verticalDistance / slope) < (pLevel.getRandom().nextDouble() * 1.7) && !pLevel.getBlockState(tempPos).isAir()){
                pLevel.setBlock(tempPos, ModBlocks.ANT_AIR.get().defaultBlockState(), 2);
                if(!pLevel.getBlockState(tempPos.below()).isAir()){
                    pLevel.setBlock(tempPos.below(), ModBlocks.ANT_DIRT.get().defaultBlockState(), 2);
                }
                num++;
            }
        }
        AntUtils.broadcastString(pLevel, "" + num);
        //////////

        long endTime = System.nanoTime();

        AntUtils.broadcastString(pLevel, "Generated new branch. It took " + (endTime-startTime) + " nanoseconds.");

        return 0;
    }

    private static int printNearestBranch(CommandContext<CommandSourceStack> context){
        ServerLevel pLevel = context.getSource().getLevel();
        BlockPos pos = context.getSource().getEntity().blockPosition();
        ColonyBranch nearest = ColonyBranch.getNearest_testing(pos);

        if(nearest != null) {
            AntUtils.broadcastString(pLevel, nearest.toString());
        }
        else{
            AntUtils.broadcastString(pLevel, "couldn't find a branch to print");
        }

        return 0;
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

    private static int deleteAllColonies(CommandContext<CommandSourceStack> context){
        ServerLevel pLevel = context.getSource().getLevel();
        ServerLevelUtil colonies = (ServerLevelUtil)pLevel;
        AntColony colony;
        int num = 0;

        while(colonies.getNumberOfColonies() > 0) {
            colony = colonies.getClosestColony(context.getSource().getEntity().blockPosition());
            if (colony != null) {
                num++;
                ((ServerLevelUtil) pLevel).removeColonyFromList(colony);
            }
        }

        AntUtils.broadcastString(pLevel, "Removed " + num + " colonies.");
        return 0;
    }

    private static int spawnColony(CommandContext<CommandSourceStack> context){
        ServerLevel pLevel = context.getSource().getLevel();
        AntColony.generateWholeNewColony(pLevel,context.getSource().getEntity().blockPosition());

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

    private static int growFungusHere(CommandContext<CommandSourceStack> context){
        ServerLevel pLevel = context.getSource().getLevel();
        BlockPos pPos = context.getSource().getEntity().blockPosition().below();

        pLevel.setBlock(pPos, ModBlocks.FUNGAL_CORE.get().defaultBlockState(), 2);

        FungalCore.grow(pLevel,pPos, 60);

        return 0;
    }



}
