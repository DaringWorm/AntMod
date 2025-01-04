package com.daringworm.antmod.effect;

import com.daringworm.antmod.AntMod;
import com.daringworm.antmod.effect.custom.HiveHatred;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.extensions.IForgeMobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModEffects {
    public static final DeferredRegister<MobEffect> POTIONS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, AntMod.MOD_ID);


    public static final RegistryObject<MobEffect> HATRED_OF_THE_HIVE =
            registerEffect("hatred_of_the_hive", () -> new HiveHatred(MobEffectCategory.HARMFUL, 1));





    private static <T extends MobEffect> RegistryObject<T> registerEffect
            (String name, Supplier<T> effect){
        return POTIONS.register(name, effect);
    }

    public static void register(IEventBus eventBus) {
        POTIONS.register(eventBus);
    }
}
