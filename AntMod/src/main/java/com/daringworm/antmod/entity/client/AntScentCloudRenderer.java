package com.daringworm.antmod.entity.client;

import com.daringworm.antmod.AntMod;
import com.daringworm.antmod.entity.custom.AntCarver;
import com.daringworm.antmod.entity.custom.AntScentCloud;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class AntScentCloudRenderer extends EntityRenderer<AntScentCloud> {
    public AntScentCloudRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
        this.shadowRadius = 0f;
    }

    @Override
    public ResourceLocation getTextureLocation(AntScentCloud object) {
        return new ResourceLocation(AntMod.MOD_ID, "textures/entity/antcarver/antcarvertexture.png");
    }

}
