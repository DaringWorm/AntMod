package com.daringworm.antmod.entity.client;

import com.daringworm.antmod.AntMod;
import com.daringworm.antmod.entity.custom.AntCarver;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class AntCarverRenderer extends GeoEntityRenderer<AntCarver> {
    public AntCarverRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AntCarverModel());
        this.shadowRadius = 0.02f;
    }

    @Override
    public ResourceLocation getTextureLocation(AntCarver object) {
        return new ResourceLocation(AntMod.MOD_ID, "textures/entity/antcarver/antcarvertexture.png");
    }

    @Override
    public RenderType getRenderType
            (AntCarver animatable, float partialTicks, PoseStack stack,
             MultiBufferSource renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {

        stack.scale(1f, 1f, 1f);
        return super.getRenderType(animatable, partialTicks, stack, renderTypeBuffer, vertexBuilder, packedLightIn, textureLocation);

    }

}
