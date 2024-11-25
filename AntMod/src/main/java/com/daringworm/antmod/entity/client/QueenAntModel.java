package com.daringworm.antmod.entity.client;

import com.daringworm.antmod.AntMod;
import com.daringworm.antmod.entity.custom.QueenAnt;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class QueenAntModel extends AnimatedGeoModel<QueenAnt> {
    @Override
    public ResourceLocation getModelLocation(QueenAnt object) {
        return new ResourceLocation(AntMod.MOD_ID, "geo/queen_ant.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(QueenAnt object) {
        return new ResourceLocation(AntMod.MOD_ID, "textures/entity/queenant/queen_ant_texture.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(QueenAnt animatable) {
        return new ResourceLocation(AntMod.MOD_ID, "animations/workerant_ed3.animation.json");
    }
}
