package com.daringworm.antmod.goals;

import com.daringworm.antmod.entity.custom.WorkerAnt;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class ClampOnTargetGoal extends Goal {
    private final WorkerAnt ant;

    private int lockedOnDuration =0;

    public ClampOnTargetGoal(WorkerAnt pAnt, LivingEntity pTarget) {
        super();
        this.ant = pAnt;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        return ant.getWorkingStage() == 3;
    }

    @Override
    public boolean canContinueToUse(){
        return canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick(){
        return true;
    }

    public void stop(){
        if(ant.getTarget() != null && !ant.getTarget().isAlive()){
            ant.setTarget(null);
            ant.setWorkingStage(1);
        }
        if(lockedOnDuration > 2000){ant.kill();}
        else{lockedOnDuration = 0;}
    }

    public void tick(){
        LivingEntity target = ant.getTarget();


        if(target != null && target.isAlive()) {
            // makes sure the ants won't latch on each other and enter a loop of zooming into the heavens
            boolean loopTest = true;
            if(target instanceof WorkerAnt){
                List<WorkerAnt> tempList = ant.level.getEntitiesOfClass(WorkerAnt.class,ant.getBoundingBox().inflate(8d));
                for(WorkerAnt tempAnt : tempList){
                    if(tempAnt.getTarget() == ant && tempAnt.getWorkingStage() == 3){
                        loopTest = false;
                    }
                }
            }
            if(!loopTest){ant.setWorkingStage(1);super.stop();}

            if (ant.isAlive() && loopTest) {
                ant.setLatchDirection((ant.getLatchDirection()));
                float addpos = ant.getLatchDirection()*40;
                Vec3 changevec= Vec3.directionFromRotation(0,addpos);
                ant.moveTo(target.position().add(target.getDeltaMovement().add(new Vec3(0,target.getBbHeight()/2,0)).add(changevec)));
                ant.setDeltaMovement(target.getDeltaMovement());
                ant.lookAt(target, 20, 20);
                ant.resetFallDistance();
                //target.hurt(DamageSource.GENERIC,4);
                target.setLastHurtByMob(ant);
                lockedOnDuration++;
                //target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 5), ant);
                //target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0), ant);
                target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, 0), ant);
                //target.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, -5), ant);
            }
            double random = Math.random();
            if(!loopTest && random > 0.5){ant.setWorkingStage(1);}
        }

        if(target == null || !target.isAlive()){
            if( target != null && !target.isAlive()){ant.setTarget(null);}
            ant.setWorkingStage(1);
        }
    }
}
