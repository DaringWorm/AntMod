package com.daringworm.antmod.event;


import com.daringworm.antmod.AntMod;
import com.daringworm.antmod.entity.ModEntityTypes;
import com.daringworm.antmod.entity.custom.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = AntMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {


    @SubscribeEvent
    public static void registerModifierSerializers(@Nonnull final RegistryEvent.Register<GlobalLootModifierSerializer<?>> event) {
        event.getRegistry().registerAll();

    }


    @SubscribeEvent
    public static void registerRecipeTypes(final RegistryEvent.Register<RecipeSerializer<?>> event) {
    }

    @SubscribeEvent
    public static void registerParticleFactories(final ParticleFactoryRegisterEvent event) {
    }

    @SubscribeEvent
    public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.WORKERANT.get(), WorkerAnt.setAttributes());
        event.put(ModEntityTypes.QUEENANT.get(), QueenAnt.setAttributes());
        event.put(ModEntityTypes.ANTEGG.get(), AntEgg.setAttributes());
        event.put(ModEntityTypes.ANTCARVER.get(), AntCarver.setAttributes());
        event.put(ModEntityTypes.ANTLARVA.get(), AntLarva.setAttributes());
        //event.put(ModEntityTypes.ANT_EFFECT_CLOUD.get(), AntCarver.setAttributes());
    }
}