package com.daringworm.antmod.goals;

import com.daringworm.antmod.entity.Ant;
import com.daringworm.antmod.entity.custom.WorkerAnt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

public class AggroManagerGoal extends Goal implements AntUtils {
    private final WorkerAnt ant;
    private final int getFollowDistance;

    private int speedModifier = 1;
    @Nullable
    private LivingEntity potentialTarget;
    private final TargetingConditions attackTargeting = TargetingConditions.forCombat().range(64.0D);


    public AggroManagerGoal(WorkerAnt pAnt, int followDistance, int pSpeedModifier) {
        super();
        this.ant = pAnt;
        this.getFollowDistance = followDistance;
        speedModifier = pSpeedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
        return ant.getWorkingStage() == 1 || findNearbyEnemyAnts(ant) != null || ant.getLastHurtByMob() != null || ant.getTarget() != null;
    }

    public boolean canContinueToUse(){
        return canUse();
    }

    public void start() {

        super.start();

        LivingEntity entity = ant.getLastHurtByMob();

        if (ant.getLastHurtByMob() != null) {
            assert entity != null;
            if (entity.isAlive()) {
                potentialTarget = entity;
            }
        }

        if(potentialTarget == null){
            Ant badAnt = findNearbyEnemyAnts(ant);
            if(badAnt != null){
                potentialTarget = badAnt;
            }
        }

        if (potentialTarget == null || !potentialTarget.isAlive()) {
            AABB aabb = ant.getBoundingBox().inflate(10.0D, 8.0D, 10.0D);
            List<WorkerAnt> list1 = this.ant.level.getNearbyEntities(WorkerAnt.class, attackTargeting, this.ant, aabb);
            List<LivingEntity> list2 = this.ant.level.getNearbyEntities(LivingEntity.class,attackTargeting, ant, aabb);

            for (WorkerAnt workerAnt : list1) {
                int ID = workerAnt.getColonyID();
                if (ID != this.ant.getColonyID()){
                    if (potentialTarget == null) {
                        this.potentialTarget = workerAnt;
                    } else if (ant.distanceToSqr(workerAnt) < ant.distanceToSqr(potentialTarget)) {
                        this.potentialTarget = workerAnt;
                    }
                }
            }
            if(potentialTarget == null){
                for(LivingEntity tempEntity : list2){
                    if(tempEntity.getActiveEffects().contains(MobEffects.DIG_SLOWDOWN)){
                        ant.setTarget(tempEntity);
                    }
                }
            }
        }

        if(potentialTarget != null){
            ant.setTarget(this.potentialTarget);
        }
        else super.stop();

        if(ant.getMainHandItem() != ItemStack.EMPTY){
            ant.spawnAtLocation(ant.getMainHandItem().getItem());
            ant.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        }
        ant.setWorkingStage(1);
        ant.setLastHurtByMob(null);
    }

    public void stop(){
        if(ant.getTarget() != null && !ant.getTarget().isAlive()) {ant.setTarget(null);}
        ant.setLastHurtByMob(null);
        ant.setNoGravity(false);
        ant.setWorkingStage(0);
    }

    public boolean requiresUpdateEveryTick(){
        return true;
}

    public void tick() {

        LivingEntity target = ant.getTarget();
        if(target == null && potentialTarget != null){
            target = potentialTarget;
            potentialTarget = null;
        }

        if(potentialTarget == null){
            potentialTarget = findNearbyEnemyAnts(ant);
            if(potentialTarget == null){
                potentialTarget = ant.getLastHurtByMob();
                if((target == null || (target !=null && !target.isAlive())) && potentialTarget == null){
                    ant.setWorkingStage(0);
                    ant.setTarget(null);
                }
            }
            else{ant.setTarget(potentialTarget);}
        }

        if(ant.getWorkingStage() == 1) {
            if(ant.getMainHandItem() != ItemStack.EMPTY){
                ant.spawnAtLocation(ant.getMainHandItem().getItem());
                ant.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            }

            if (target == null || !target.isAlive()) {
                ant.setTarget(findNearbyEnemyAnts(ant));
            }

            if (target != null && target.isAlive()) {
                if(target instanceof WorkerAnt){
                    List<WorkerAnt> antList = ant.level.getEntitiesOfClass(WorkerAnt.class, ant.getBoundingBox().inflate(8d));
                    boolean hasPassengers = false;
                    for(WorkerAnt tempAnt : antList){
                        if(tempAnt.getTarget() == ant && tempAnt.getWorkingStage() == 3){
                            hasPassengers = true;
                        }
                    }
                    if(!hasPassengers){
                        ant.setWorkingStage(3);
                    }
                    else{
                        ant.lookAt(target,100,100);
                    }
                    if(((WorkerAnt) target).getWorkingStage() == 1){
                        ant.setWorkingStage(3);
                    }
                }

                else if(target.getBbHeight() > 1){ant.setWorkingStage(3);}

                if(ant.getWorkingStage() == 1){
                    ant.getNavigation().moveTo(target,1.5);
                    if(ant.distanceToSqr(target)<target.getBbWidth()*1.2){
                        target.hurt(DamageSource.mobAttack(ant),2);
                    }
                }

            }
        }

        else{super.stop();}
    }
}