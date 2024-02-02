package com.daringworm.antmod.entity.brains.parts;

import com.daringworm.antmod.block.ModBlocks;
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
    public static final AntPredicate SEES_ITEMS  = new AntPredicate(a -> !a.memory.foundItemList.isEmpty());
    public static final AntPredicate NAV_DONE = new AntPredicate(a -> a.getNavigation().isDone());
    public static final AntPredicate FOUND_INTEREST_BLOCK = new AntPredicate(a -> a.memory.interestPos != BlockPos.ZERO);
    public static final AntPredicate IN_RANGE_OF_INTEREST_BLOCK = new AntPredicate(a -> a.getDistTo(a.memory.interestPos)<12d);
    public static final AntPredicate WAS_HURT = new AntPredicate(a-> a.getLastHurtByMob() != null && a.getLastHurtByMob().isAlive());
    public static final AntPredicate HAS_PASSIVE_TARGET = new AntPredicate(a -> a.memory.passiveTarget != null && a.memory.passiveTarget.isAlive());
    public static final AntPredicate CAN_REACH_PASSIVE_TARGET = new AntPredicate(a -> a.memory.passiveTarget != null && a.getDistTo(a.memory.passiveTarget.blockPosition())<2.3d);
    public static final AntPredicate HAS_ITEM = new AntPredicate(a -> !a.getMainHandItem().isEmpty());
    public static final AntPredicate FOUND_CONTAINER = new AntPredicate(a -> a.memory.containerPos != BlockPos.ZERO && a.getLevel().getBlockState(a.memory.containerPos).getBlock() == ModBlocks.LEAFY_CONTAINER_BLOCK.get() && AntUtils.getDist(a.blockPosition(),a.memory.containerPos) < 10);
    public static final AntPredicate SHOULD_SNIP_INTEREST = new AntPredicate(a -> a.memory.foodStatePredicate(a).test(a.memory.interestPos));




    public static final AntPredicate IS_WANDERING = new AntPredicate(a -> a.memory.workingStage == (WorkingStages.WANDERING));
    public static final AntPredicate IS_SCOUTING = new AntPredicate(a -> a.memory.workingStage == (WorkingStages.SCOUTING));
    public static final AntPredicate IS_FORAGING = new AntPredicate(a -> a.memory.workingStage == (WorkingStages.FORAGING));
    public static final AntPredicate IS_FARMING = new AntPredicate(a -> a.memory.workingStage == (WorkingStages.FARMING));
    public static final AntPredicate IS_NURSING = new AntPredicate(a -> a.memory.workingStage == (WorkingStages.NURSING));
    public static final AntPredicate IS_TIDYING = new AntPredicate(a -> a.memory.workingStage == (WorkingStages.TIDYING));
    public static final AntPredicate IS_EXCAVATING = new AntPredicate(a -> a.memory.workingStage == (WorkingStages.EXCAVATING));
    public static final AntPredicate IS_ATTACKING = new AntPredicate(a -> a.memory.workingStage == (WorkingStages.ATTACKING));
    public static final AntPredicate IS_LATCHING = new AntPredicate(a -> a.memory.workingStage == (WorkingStages.LATCHING));
}
