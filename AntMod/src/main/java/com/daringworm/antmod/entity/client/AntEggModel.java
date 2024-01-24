package com.daringworm.antmod.entity.client;

import com.daringworm.antmod.AntMod;
import com.daringworm.antmod.entity.custom.AntEgg;
import com.daringworm.antmod.entity.custom.QueenAnt;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class AntEggModel extends AnimatedGeoModel<AntEgg> {
    @Override
    public ResourceLocation getModelLocation(AntEgg object) {
        return new ResourceLocation(AntMod.MOD_ID, "geo/ant_egg.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(AntEgg object) {
        return new ResourceLocation(AntMod.MOD_ID, "textures/entity/antegg/anteggtexture.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(AntEgg animatable) {
        return new ResourceLocation(AntMod.MOD_ID, "animations/antegg.animation.json");
    }
}
