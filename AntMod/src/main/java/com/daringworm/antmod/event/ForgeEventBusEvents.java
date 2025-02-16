package com.daringworm.antmod.event;


import com.daringworm.antmod.AntMod;
import com.daringworm.antmod.command.custom.AntModCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AntMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventBusEvents {

    @SubscribeEvent
    public static void registerCommands(final RegisterCommandsEvent event){
        var dispatcher = event.getDispatcher();

        AntModCommand.register(dispatcher);
    }
}
