package com.daringworm.antmod.entity.brains.parts;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.entity.Ant;
import com.daringworm.antmod.goals.AntUtils;
import com.daringworm.antmod.mixin.tomixin.ServerLevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;

import java.util.ArrayList;

public class AntPredicates {
    public static final AntPredicate TRUE = new AntPredicate(a -> true);
    public static final AntPredicate FALSE = new AntPredicate(a -> false);
    public static final AntPredicate IS_ALIVE = new AntPredicate(LivingEntity::isAlive);
    public static final AntPredicate TARGET_EXISTS = new AntPredicate(a -> (a.getTarget() != null && a.getTarget().isAlive() && a.canAttack(a.getTarget())) || a.getLastHurtByMob() != null);
    public static final AntPredicate HAS_SELECTED_TARGET = new AntPredicate(a -> a.getTarget() != null && a.getTarget().isAlive());
    public static final AntPredicate CAN_REACH_TARGET = new AntPredicate(a -> a.getTarget() != null && a.distanceToSqr(a.getTarget()) < 2.25d);
    public static final AntPredicate SEES_ITEMS  = new AntPredicate(a -> a.getNearbyItemCount() > 0);
    public static final AntPredicate SEES_SNIPPABLE_BLOCK = new AntPredicate(a-> BlockPos.findClosestMatch(a.blockPosition(),3,2, b -> AntUtils.shouldSnip(b, a.getLevel())).isPresent());
    public static final AntPredicate NAV_DONE = new AntPredicate(a -> a.getNavigation().isDone() || a.getNavigation().isStuck());
    public static final AntPredicate FOUND_INTEREST_BLOCK = new AntPredicate(a -> a.getInterestPos() != BlockPos.ZERO);
    public static final AntPredicate IN_RANGE_OF_INTEREST_BLOCK = new AntPredicate(a -> a.getDistTo(a.getInterestPos())<12d);
    public static final AntPredicate IN_RANGE_OF_FUNGUS_POS = new AntPredicate(a -> a.getDistTo(a.getFungusLocation())<12d && a.getFungusLocation() != BlockPos.ZERO);
    public static final AntPredicate IS_HOLDING_LEAVES = new AntPredicate(a -> a.getMainHandItem().getItem() == ModBlocks.MOLDY_LEAVES.get().asItem());
    public static final AntPredicate WAS_HURT = new AntPredicate(a-> a.getLastHurtByMob() != null && a.getLastHurtByMob().isAlive());
    public static final AntPredicate HAS_PASSIVE_TARGET = new AntPredicate(a -> a.getPassiveTarget() != null && a.getPassiveTarget().isAlive());
    public static final AntPredicate CAN_REACH_PASSIVE_TARGET = new AntPredicate(a -> a.getPassiveTarget() != null && a.getDistTo(a.getPassiveTarget().blockPosition())<2.3d && a.hasLineOfSight(a.getPassiveTarget()));
    public static final AntPredicate SEES_PASSIVE_TARGET = new AntPredicate(a -> !a.getLevel().getEntitiesOfClass(Animal.class,a.getBoundingBox().inflate(2d)).isEmpty());
    public static final AntPredicate HAS_ITEM = new AntPredicate(a -> !a.getMainHandItem().isEmpty());
    public static final AntPredicate HAS_CONTAINER = new AntPredicate(a -> a.getHomeContainerPos() != BlockPos.ZERO && a.getLevel().getBlockState(a.getHomeContainerPos()).getBlock() == ModBlocks.LEAFY_CONTAINER_BLOCK.get());
    public static final AntPredicate SHOULD_SNIP_INTEREST = new AntPredicate(a -> Ant.foodStatePredicate(a).test(a.getInterestPos()) && a.getInterestPos() != BlockPos.ZERO);
    public static final AntPredicate IS_UNDERGROUND = new AntPredicate(a -> a.getLevel().getBlockState(a.blockPosition()).getBlock() == ModBlocks.ANT_AIR.get());
    public static final AntPredicate HAS_COLONY = new AntPredicate(a-> ((ServerLevelUtil)a.getLevel()).getColonyWithID(a.getColonyID()) != null);
    public static final AntPredicate NEAR_HOMEPOS = new AntPredicate(a-> a.getDistTo(a.getHomeContainerPos()) < 12f);
    public static final AntPredicate HAS_CONTAINER_AS_INTEREST = new AntPredicate(a -> a.getLevel().getBlockState(a.getInterestPos()).getBlock() == ModBlocks.LEAFY_CONTAINER_BLOCK.get());
    public static final AntPredicate IS_HUNGRY = new AntPredicate(a -> a.getHunger() < 40000);
    public static final AntPredicate HAS_FUNGUS_POS = new AntPredicate(a -> a.getFungusLocation() != BlockPos.ZERO);

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
