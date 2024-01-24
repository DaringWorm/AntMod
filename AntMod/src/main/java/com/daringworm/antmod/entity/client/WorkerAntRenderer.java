package com.daringworm.antmod.entity.client;

import com.daringworm.antmod.AntMod;
import com.daringworm.antmod.entity.brains.parts.WorkingStages;
import com.daringworm.antmod.entity.custom.WorkerAnt;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.nbt.TagType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.ExtendedGeoEntityRenderer;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

import java.util.Objects;

public class WorkerAntRenderer extends ExtendedGeoEntityRenderer<WorkerAnt> {
    public WorkerAntRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new WorkerAntModel());
        this.shadowRadius = 0.15f;
    }



    @Override
    public ResourceLocation getTextureLocation(WorkerAnt object) {
        return new ResourceLocation(AntMod.MOD_ID, "textures/entity/workerant/worker_ant_texture.png");
    }

    @Override
    protected boolean isArmorBone(GeoBone bone) {
        return false;
    }

    @Nullable
    @Override
    protected ResourceLocation getTextureForBone(String boneName, WorkerAnt currentEntity) {
        return null;
    }

    @Nullable
    @Override
    protected ItemStack getHeldItemForBone(String boneName, WorkerAnt currentEntity) {
        if(!(currentEntity.getMainHandItem().getItem() instanceof BlockItem) && Objects.equals(boneName, "handitem")) {
            return currentEntity.getMainHandItem();
        }
        // set to "handblock" to offset it forwards, not good because floaty seeds and flowers but works for blocks, prevents clipping with head
        else if ((currentEntity.getMainHandItem().getItem() instanceof BlockItem) && Objects.equals(boneName, "handitem")){

            return currentEntity.getMainHandItem();
        }
        else return null;
    }

    @Override
    protected ItemTransforms.TransformType getCameraTransformForItemAtBone(ItemStack boneItem, String boneName) {
        return ItemTransforms.TransformType.NONE;
    }

    @Nullable
    @Override
    protected BlockState getHeldBlockForBone(String boneName, WorkerAnt currentEntity) {return null;}

    @Override
    protected void preRenderItem(PoseStack matrixStack, ItemStack item, String boneName, WorkerAnt currentEntity, IBone bone) {}

    @Override
    protected void preRenderBlock(PoseStack matrixStack, BlockState block, String boneName, WorkerAnt currentEntity) {}

    @Override
    protected void postRenderItem(PoseStack matrixStack, ItemStack item, String boneName, WorkerAnt currentEntity, IBone bone) {}

    @Override
    protected void postRenderBlock(PoseStack matrixStack, BlockState block, String boneName, WorkerAnt currentEntity) {}

    @Override
    public RenderType getRenderType(WorkerAnt ant, float partialTicks, PoseStack stack,
             MultiBufferSource renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {

        float v = (ant.getSubClass()+20)/30f;
        stack.scale(v*0.4f, v*0.4f, v*0.4f);

        return super.getRenderType(ant, partialTicks, stack, renderTypeBuffer, vertexBuilder, packedLightIn, textureLocation);
    }
}