package com.daringworm.antmod.event;


import com.daringworm.antmod.AntMod;
import com.daringworm.antmod.colony.AntColony;
import com.daringworm.antmod.command.custom.AntModCommand;
import com.daringworm.antmod.entity.brains.parts.pathfinding.PathFinder;
import com.daringworm.antmod.mixin.tomixin.ServerLevelUtil;
import com.daringworm.antmod.util.AntUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.Arrays;

@Mod.EventBusSubscriber(modid = AntMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventBusEvents {

    @SubscribeEvent
    public static void registerCommands(final RegisterCommandsEvent event){
        var dispatcher = event.getDispatcher();

        AntModCommand.register(dispatcher);
    }

    @SubscribeEvent
    public static void tickColonies(final TickEvent.WorldTickEvent event){
        Level eLevel = event.world;

        if(eLevel instanceof ServerLevel pLevel){
            ServerLevelUtil levelUtil = (ServerLevelUtil) pLevel;

            for(AntColony colony : levelUtil.getColonies()){
                if(colony.isInLoadedChunk()) {
                    colony.tick();
                }
            }
        }
    }

    @SubscribeEvent
    public static void loadColonies(final WorldEvent.Load event){


        if(!event.getWorld().isClientSide()) {
            ServerLevel pLevel = (ServerLevel) event.getWorld();
            try {
                ((ServerLevelUtil) pLevel).loadColoniesFromFile(pLevel);
            }
            catch(IOException exception){
                System.out.println("Error loading colonies from file.");
            }
        }
    }



}
