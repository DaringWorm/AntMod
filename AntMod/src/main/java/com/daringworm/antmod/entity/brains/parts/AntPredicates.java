package com.daringworm.antmod.entity.brains.parts;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.entity.Ant;
import com.daringworm.antmod.goals.AntUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;

public class AntPredicates {
    public static final AntPredicate TRUE = new AntPredicate(a -> true);
    public static final AntPredicate FALSE = new AntPredicate(a -> false);
    public static final AntPredicate IS_ALIVE = new AntPredicate(LivingEntity::isAlive);
    public static final AntPredicate TARGET_EXISTS = new AntPredicate(a -> (a.getTarget() != null && a.getTarget().isAlive() && a.canAttack(a.getTarget())) || a.getLastHurtByMob() != null);
    public static final AntPredicate HAS_SELECTED_TARGET = new AntPredicate(a -> a.getTarget() != null && a.getTarget().isAlive());
    public static final AntPredicate CAN_REACH_TARGET = new AntPredicate(a -> a.getTarget() != null && a.distanceToSqr(a.getTarget()) < 2.25d);
    public static final AntPredicate SEES_ITEMS  = new AntPredicate(a -> a.getNearbyItemCount() > 0);
    public static final AntPredicate NAV_DONE = new AntPredicate(a -> a.getNavigation().isDone() || a.getNavigation().isStuck());
    public static final AntPredicate FOUND_INTEREST_BLOCK = new AntPredicate(a -> a.getInterestPos() != BlockPos.ZERO);
    public static final AntPredicate IN_RANGE_OF_INTEREST_BLOCK = new AntPredicate(a -> a.getDistTo(a.getInterestPos())<12d);
    public static final AntPredicate WAS_HURT = new AntPredicate(a-> a.getLastHurtByMob() != null && a.getLastHurtByMob().isAlive());
    public static final AntPredicate HAS_PASSIVE_TARGET = new AntPredicate(a -> a.getPassiveTarget() != null && a.getPassiveTarget().isAlive());
    public static final AntPredicate CAN_REACH_PASSIVE_TARGET = new AntPredicate(a -> a.getPassiveTarget() != null && a.getDistTo(a.getPassiveTarget().blockPosition())<2.3d);
    public static final AntPredicate HAS_ITEM = new AntPredicate(a -> !a.getMainHandItem().isEmpty());
    public static final AntPredicate FOUND_CONTAINER = new AntPredicate(a -> a.getHomeContainerPos() != BlockPos.ZERO && a.getLevel().getBlockState(a.getHomeContainerPos()).getBlock() == ModBlocks.LEAFY_CONTAINER_BLOCK.get() && AntUtils.getDist(a.blockPosition(),a.getHomeContainerPos()) < 16d);
    public static final AntPredicate SHOULD_SNIP_INTEREST = new AntPredicate(a -> Ant.foodStatePredicate(a).test(a.getInterestPos()));



    public static final AntPredicate IS_WANDERING = new AntPredicate(a -> a.getWorkingStage() == (WorkingStages.WANDERING));
    public static final AntPredicate IS_SCOUTING = new AntPredicate(a -> a.getWorkingStage() == (WorkingStages.SCOUTING));
    public static final AntPredicate IS_FORAGING = new AntPredicate(a -> a.getWorkingStage() == (WorkingStages.FORAGING));
    public static final AntPredicate IS_FARMING = new AntPredicate(a -> a.getWorkingStage() == (WorkingStages.FARMING));
    public static final AntPredicate IS_NURSING = new AntPredicate(a -> a.getWorkingStage() == (WorkingStages.NURSING));
    public static final AntPredicate IS_TIDYING = new AntPredicate(a -> a.getWorkingStage() == (WorkingStages.TIDYING));
    public static final AntPredicate IS_EXCAVATING = new AntPredicate(a -> a.getWorkingStage() == (WorkingStages.EXCAVATING));
    public static final AntPredicate IS_ATTACKING = new AntPredicate(a -> a.getWorkingStage() == (WorkingStages.ATTACKING));
    public static final AntPredicate IS_LATCHING = new AntPredicate(a -> a.getWorkingStage() == (WorkingStages.LATCHING));
}
