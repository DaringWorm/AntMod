package com.daringworm.antmod.entity.client;

import com.daringworm.antmod.AntMod;
import com.daringworm.antmod.entity.custom.AntCarver;
import com.daringworm.antmod.entity.custom.AntLarva;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class AntLarvaModel extends AnimatedGeoModel<AntLarva> {
    @Override
    public ResourceLocation getModelLocation(AntLarva object) {
        return new ResourceLocation(AntMod.MOD_ID, "geo/antlarva.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(AntLarva object) {
        return new ResourceLocation(AntMod.MOD_ID, "textures/entity/antlarva/larva1texture.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(AntLarva animatable) {
        return new ResourceLocation(AntMod.MOD_ID, "animations/antlarva1.animation.json");
    }
}