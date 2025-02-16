package com.daringworm.antmod.effect.custom;

import com.daringworm.antmod.entity.brains.parts.WorkingStages;
import com.daringworm.antmod.entity.custom.WorkerAnt;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;

public class HiveHatred extends MobEffect {
    public HiveHatred(MobEffectCategory p_19451_, int p_19452_) {
        super(p_19451_, p_19452_);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier){
        if(!(pLivingEntity.getLevel() instanceof ServerLevel)){
            return;
        }
        
        ServerLevel pLevel = (ServerLevel) pLivingEntity.getLevel();
        ArrayList<WorkerAnt> antList = (ArrayList<WorkerAnt>) pLevel.getEntitiesOfClass(WorkerAnt.class, pLivingEntity.getBoundingBox().inflate(8d+pAmplifier));

        for(WorkerAnt tempAnt : antList){
            if(tempAnt.getWorkingStage() < WorkingStages.ATTACKING){
                tempAnt.setTarget(pLivingEntity);
                tempAnt.setWorkingStage(WorkingStages.ATTACKING);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return true;
    }
}
