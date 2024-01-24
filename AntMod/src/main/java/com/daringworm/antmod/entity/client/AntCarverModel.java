package com.daringworm.antmod.entity.client;

import com.daringworm.antmod.AntMod;
import com.daringworm.antmod.entity.custom.AntCarver;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class AntCarverModel extends AnimatedGeoModel<AntCarver> {
    @Override
    public ResourceLocation getModelLocation(AntCarver object) {
        return new ResourceLocation(AntMod.MOD_ID, "geo/antcarver.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(AntCarver object) {
        return new ResourceLocation(AntMod.MOD_ID, "textures/entity/antcarver/antcarvertexture.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(AntCarver animatable) {
        return new ResourceLocation(AntMod.MOD_ID, "animations/antcarver.animation.json");
    }
}