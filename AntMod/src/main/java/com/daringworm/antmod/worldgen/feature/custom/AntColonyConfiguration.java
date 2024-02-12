package com.daringworm.antmod.worldgen.feature.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class AntColonyConfiguration extends CarverConfiguration {
    public static final Codec<AntColonyConfiguration> CODEC = RecordCodecBuilder.create((codecConfig) -> {
        return codecConfig.group(CarverConfiguration.CODEC.forGetter((p_158990_) -> {
            return p_158990_;
        }), IntProvider.CODEC.fieldOf("vertical_rotation").forGetter((p_158988_) -> {
            return p_158988_.verticalRotation;
        }),IntProvider.CODEC.fieldOf("start_y").forGetter((p_158988_) -> {
            return p_158988_.startY;
        })).apply(codecConfig, AntColonyConfiguration::new);
    });
    public final IntProvider verticalRotation;
    public final IntProvider startY;



    public AntColonyConfiguration(float pProbability, HeightProvider pY, FloatProvider pYScale, VerticalAnchor pLavaLevel, CarverDebugSettings pDebugSettings, IntProvider pVerticalRotation, IntProvider startY) {
        super(pProbability, pY, pYScale, pLavaLevel, pDebugSettings);
        this.verticalRotation = pVerticalRotation;
        this.startY = startY;
    }

    public AntColonyConfiguration(CarverConfiguration p_158980_, IntProvider rotationProvider,IntProvider startX) {
        this(p_158980_.probability, p_158980_.y, p_158980_.yScale, p_158980_.lavaLevel, p_158980_.debugSettings, rotationProvider, startX);
    }
}
