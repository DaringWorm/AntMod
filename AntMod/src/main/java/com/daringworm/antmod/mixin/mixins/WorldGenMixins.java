package com.daringworm.antmod.mixin.mixins;

import net.minecraft.world.level.levelgen.feature.Feature;
import org.spongepowered.asm.mixin.Mixin;


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

