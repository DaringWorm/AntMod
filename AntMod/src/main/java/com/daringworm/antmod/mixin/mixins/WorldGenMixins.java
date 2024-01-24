package com.daringworm.antmod.mixin.mixins;

import com.daringworm.antmod.entity.Ant;
import com.daringworm.antmod.worldgen.feature.PlaceFeature;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.levelgen.feature.Feature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Feature.class)
public abstract class WorldGenMixins {
/*
    @Inject(at = @At("HEAD"), method = "", cancellable = true)
    public void readAdditionalSaveData(Entity pEntity, CallbackInfo ci) {
        if(pEntity instanceof Ant){
            if(((Ant) pEntity).getWorkingStage() == 1) {
                ci.cancel();
            }
        }
    }
*/
}

