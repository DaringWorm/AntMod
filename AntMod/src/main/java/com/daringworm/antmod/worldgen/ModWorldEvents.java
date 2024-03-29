package com.daringworm.antmod.worldgen;

import com.daringworm.antmod.AntMod;
import com.daringworm.antmod.worldgen.gen.ModColonyGeneration;
import com.daringworm.antmod.worldgen.gen.ModDecorationGeneration;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AntMod.MOD_ID)
public class ModWorldEvents {
    @SubscribeEvent
    public static void biomeLoadingEvent(final BiomeLoadingEvent event) {
        ModDecorationGeneration.generateTrees(event);
        ModColonyGeneration.generateColonies(event);
        //ModEntityGeneration.onEntitySpawn(event);
    }
}