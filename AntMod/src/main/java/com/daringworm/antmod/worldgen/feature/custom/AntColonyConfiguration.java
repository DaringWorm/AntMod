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
        }),IntProvider.CODEC.fieldOf("start_x").forGetter((p_158988_) -> {
            return p_158988_.verticalRotation;
        }),IntProvider.CODEC.fieldOf("start_z").forGetter((p_158988_) -> {
            return p_158988_.verticalRotation;
        })).apply(codecConfig, AntColonyConfiguration::new);
    });
    public final IntProvider verticalRotation;
    public final IntProvider startX;
    public final IntProvider startZ;


    public AntColonyConfiguration(float pProbability, HeightProvider pY, FloatProvider pYScale, VerticalAnchor pLavaLevel, CarverDebugSettings pDebugSettings, IntProvider pVerticalRotation, IntProvider startX,IntProvider startZ) {
        super(pProbability, pY, pYScale, pLavaLevel, pDebugSettings);
        this.verticalRotation = pVerticalRotation;
        this.startX = startX;
        this.startZ = startZ;
    }

    public AntColonyConfiguration(CarverConfiguration p_158980_, IntProvider rotationProvider,IntProvider startX,IntProvider startZ) {
        this(p_158980_.probability, p_158980_.y, p_158980_.yScale, p_158980_.lavaLevel, p_158980_.debugSettings, rotationProvider, startX, startZ);
    }

    public static class ColonyShapeConfiguration {
        public static final Codec<AntColonyConfiguration.ColonyShapeConfiguration> CODEC = RecordCodecBuilder.create((p_159007_) -> {
            return p_159007_.group(IntProvider.CODEC.fieldOf("distance_factor").forGetter((p_159019_) -> {
                return p_159019_.distanceFactor;
            })).apply(p_159007_, AntColonyConfiguration.ColonyShapeConfiguration::new);
        });
        public final IntProvider distanceFactor;

        public ColonyShapeConfiguration(IntProvider p_159000_) {
            this.distanceFactor = p_159000_;
        }
    }
}
