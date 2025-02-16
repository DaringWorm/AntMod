package com.daringworm.antmod.command;

import com.daringworm.antmod.command.custom.AntModCommand;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

public class ModCommands {
    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher){

        AntModCommand.register(dispatcher);
    }
}
