package com.daringworm.antmod.mixin.mixins;

import com.daringworm.antmod.entity.Ant;
import com.daringworm.antmod.entity.custom.WorkerAnt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.UUID;


@Mixin(LivingEntity.class)
public abstract class EntityClassMixins {

    @Inject(at = @At("HEAD"), method = "push", cancellable = true)
    public void push(Entity pEntity, CallbackInfo ci) {
        if(pEntity instanceof Ant pAnt){
            if(pAnt.getWorkingStage() == 3) {
                ci.cancel();
            }
        }
    }

}

