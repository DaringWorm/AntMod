package com.daringworm.antmod.entity.client;

import com.daringworm.antmod.AntMod;
import com.daringworm.antmod.entity.custom.WorkerAnt;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.example.client.DefaultBipedBoneIdents;
import software.bernie.example.entity.ExtendedRendererEntity;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class WorkerAntModel extends AnimatedGeoModel<WorkerAnt> {
    @Override
    public ResourceLocation getModelLocation(WorkerAnt object) {
        return new ResourceLocation(AntMod.MOD_ID, "geo/worker_ant.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(WorkerAnt object) {
        return new ResourceLocation(AntMod.MOD_ID, "textures/entity/workerant/worker_ant_texture.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(WorkerAnt animatable) {
        return new ResourceLocation(AntMod.MOD_ID, "animations/workerant_ed3.animation.json");
    }
}
