package com.daringworm.antmod.worldgen.feature.registries;

import com.daringworm.antmod.AntMod;
import com.daringworm.antmod.worldgen.feature.custom.AntColonyCarver;
import com.daringworm.antmod.worldgen.feature.custom.AntColonyConfiguration;
import net.minecraft.core.Registry;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.TrapezoidFloat;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.*;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AntCarversReg<C extends CarverConfiguration> {
    public static final DeferredRegister<WorldCarver<?>> CARVERS =
            DeferredRegister.create(ForgeRegistries.WORLD_CARVERS, AntMod.MOD_ID);

    public static final DeferredRegister<ConfiguredWorldCarver<?>> CONFIGURED_CARVERS =
            DeferredRegister.create(Registry.CONFIGURED_CARVER_REGISTRY, AntMod.MOD_ID);


    public static final RegistryObject<AntColonyCarver<AntColonyConfiguration>> COLONY_BASE_REGISTER =
            CARVERS.register("ant_colony",
            () -> new AntColonyCarver<>(AntColonyConfiguration.CODEC));

    public static final RegistryObject<ConfiguredWorldCarver<AntColonyConfiguration>> CONFIGURED_COLONY_REGISTER =
            CONFIGURED_CARVERS.register("mod_cave",
            () -> COLONY_BASE_REGISTER.get().configured(new AntColonyConfiguration(
                    0.003F,
                    UniformHeight.of(VerticalAnchor.absolute(40), VerticalAnchor.absolute(80)),
                    ConstantFloat.of(3.0F),
                    VerticalAnchor.aboveBottom(40),
                    CarverDebugSettings.of(false, Blocks.WARPED_BUTTON.defaultBlockState()),
                    UniformInt.of(1,360),
                    UniformInt.of(1,2),
                    UniformInt.of(1,2)
                    )));


    public static void register(IEventBus eventBus) {
        CARVERS.register(eventBus);
        CONFIGURED_CARVERS.register(eventBus);
    }
}
