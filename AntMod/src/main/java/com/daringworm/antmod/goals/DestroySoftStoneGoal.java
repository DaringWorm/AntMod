package com.daringworm.antmod.goals;

import java.util.EnumSet;
import java.util.function.Predicate;

import com.daringworm.antmod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.gameevent.GameEvent;

public class DestroySoftStoneGoal extends Goal {
    private static final int EAT_ANIMATION_TICKS = 40;

    public boolean shouldDestroy(BlockState pState) {
        return pState.is(Blocks.BARREL) || pState.is(Blocks.GRASS);
    }


    /** The entity owner of this AITask */
    private final Mob mob;
    /** The world the grass eater entity is eating from */
    private final Level level;
    /** Number of ticks since the entity started to eat grass */
    private int eatAnimationTick;

    public DestroySoftStoneGoal(Mob pMob) {
        this.mob = pMob;
        this.level = pMob.level;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    public DestroySoftStoneGoal(Mob mob, Level level) {
        this.mob = mob;
        this.level = level;
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
        if (this.mob.getRandom().nextInt(this.mob.isBaby() ? 50 : 1000) != 0) {
            return false;
        } else {
            BlockPos blockpos = this.mob.blockPosition();
            if (shouldDestroy(this.mob.getFeetBlockState())) {
                return true;
            } else {
                return this.level.getBlockState(blockpos.below()).is(ModBlocks.ANTSTONE.get());
            }
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void start() {
        this.eatAnimationTick = this.adjustedTickDelay(40);
        this.level.broadcastEntityEvent(this.mob, (byte)10);
        this.mob.getNavigation().stop();
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void stop() {
        this.eatAnimationTick = 0;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean canContinueToUse() {
        return this.eatAnimationTick > 0;
    }

    /**
     * Number of ticks since the entity started to eat grass
     */
    public int getEatAnimationTick() {
        return this.eatAnimationTick;
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void tick() {
        this.eatAnimationTick = Math.max(0, this.eatAnimationTick - 1);
        if (this.eatAnimationTick == this.adjustedTickDelay(4)) {
            BlockPos blockpos = this.mob.blockPosition();
            BlockPos blockpos1 = blockpos.below();
            BlockPos blockpos2 = blockpos.north();
            if (this.level.getBlockState(blockpos2).is(ModBlocks.ANTSTONE.get())) {
                if (net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level, this.mob)) {
                    this.level.destroyBlock(blockpos2, true);
                }

                this.mob.ate();
                this.mob.gameEvent(GameEvent.EAT, this.mob.eyeBlockPosition());
            }

            else {

                if (this.level.getBlockState(blockpos1).is(ModBlocks.ANTSTONE.get())) {
                    if (net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level, this.mob)) {
                        this.level.levelEvent(2001, blockpos1, Block.getId(ModBlocks.ANTSTONE.get().defaultBlockState()));
                        this.level.destroyBlock(blockpos1, false);
                    }

                    this.mob.ate();
                    this.mob.gameEvent(GameEvent.EAT, this.mob.eyeBlockPosition());
                }
            }

        }
    }
}

