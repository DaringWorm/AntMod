package com.daringworm.antmod.worldgen.feature.registries;

import com.daringworm.antmod.AntMod;
import com.daringworm.antmod.worldgen.feature.custom.AntColonyCarver;
import net.minecraft.core.Registry;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.TrapezoidFloat;
import net.minecraft.util.valueproviders.UniformFloat;
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



    public static final RegistryObject<AntColonyCarver> COLONY_BASE_REGISTER =
            CARVERS.register("ant_colony",
            () -> new AntColonyCarver(CanyonCarverConfiguration.CODEC));

    public static final RegistryObject<ConfiguredWorldCarver<CanyonCarverConfiguration>> CONFIGURED_COLONY_REGISTER =
            CONFIGURED_CARVERS.register("mod_cave",
            () -> COLONY_BASE_REGISTER.get().configured(new CanyonCarverConfiguration(
                    0.5F,
                    UniformHeight.of(VerticalAnchor.absolute(40), VerticalAnchor.absolute(80)),
                    ConstantFloat.of(3.0F),
                    VerticalAnchor.aboveBottom(40),
                    CarverDebugSettings.of(false, Blocks.WARPED_BUTTON.defaultBlockState()),
                    UniformFloat.of(-0.125F, 0.125F),
                    new CanyonCarverConfiguration.CanyonShapeConfiguration(UniformFloat.of(0.75F, 1.0F),
                            TrapezoidFloat.of(0.0F, 6.0F, 2.0F), 3,
                            UniformFloat.of(0.75F, 1.0F),
                            1.0F,
                            0.0F))));


    public static void register(IEventBus eventBus) {
        CARVERS.register(eventBus);
        CONFIGURED_CARVERS.register(eventBus);
    }
}
