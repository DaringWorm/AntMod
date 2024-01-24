package com.daringworm.antmod.entity.client;

import com.daringworm.antmod.AntMod;
import com.daringworm.antmod.entity.custom.QueenAnt;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class QueenAntRenderer extends GeoEntityRenderer<QueenAnt> {
    public QueenAntRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new QueenAntModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public ResourceLocation getTextureLocation(QueenAnt object) {
        return new ResourceLocation(AntMod.MOD_ID, "textures/entity/queenant/queen_ant_texture.png");
    }

    @Override
    public RenderType getRenderType
            (QueenAnt animatable, float partialTicks, PoseStack stack,
             MultiBufferSource renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {

        stack.scale(0.8f, 0.8f, 0.8f);
        return super.getRenderType(animatable, partialTicks, stack, renderTypeBuffer, vertexBuilder, packedLightIn, textureLocation);

    }

}
