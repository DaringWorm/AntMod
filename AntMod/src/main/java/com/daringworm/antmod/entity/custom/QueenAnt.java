package com.daringworm.antmod.entity.custom;


import com.daringworm.antmod.entity.Ant;
import com.daringworm.antmod.goals.FungalFarmingGoal;
import com.daringworm.antmod.goals.LayEggGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Random;

import static javax.swing.UIManager.getInt;

public class QueenAnt extends Ant implements IAnimatable {

    public final Random qRandom = new Random();

    private final AnimationFactory factory = new AnimationFactory(this);

    public QueenAnt(EntityType<? extends Ant> entityType, Level level) {
        super(entityType, level);
    }
    private static final EntityDataAccessor<Integer> LAST_EGG_LAID_TIME = SynchedEntityData.defineId(QueenAnt.class, EntityDataSerializers.INT);

    public void setThisEggTimer(int pID) {
        this.entityData.set(LAST_EGG_LAID_TIME, pID);
    }
    public int getThisEggTimer(){return this.entityData.get(LAST_EGG_LAID_TIME);}




    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(LAST_EGG_LAID_TIME, 10000);
    }


    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("EggTime", this.getThisEggTimer());
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        int e = pCompound.getInt("EggTime");
        this.setThisEggTimer(e);
    }


    public static AttributeSupplier setAttributes(){
        return Ant.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 120)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 15)
                .add(Attributes.ATTACK_SPEED, 2)
                .add(Attributes.ARMOR, 12)
                .add(Attributes.FOLLOW_RANGE, 32)
                .build();

    }

    protected void registerGoals (){
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3,new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(4,new LayEggGoal(this));
        this.goalSelector.addGoal(6,new RandomStrollGoal(this, 1));
    }



    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event){
        if (event.isMoving()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.model.worker_ant_walk_fast", true));
        }
        else {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.model.rest", true));
        }
        return PlayState.CONTINUE;
    }


    private <E extends IAnimatable> PlayState walkPredicate(AnimationEvent<E> event){
        if (event.isMoving()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.model.worker_ant_walk_fast", true));
        }
        else {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.model.rest", true));
        }
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState snipPredicate(AnimationEvent<E> event){
        if (this.getIsSnippingAnimation() && event.getController().getAnimationState().equals(AnimationState.Stopped)) {
            event.getController().markNeedsReload();
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.model.snip", false));
            this.setSnippingAnimation(false);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        this.animationSpeed = 0.5f;
        data.addAnimationController(new AnimationController(this, "walk_or_rest_controller", 5, this::walkPredicate));
        data.addAnimationController(new AnimationController(this, "snipcontroller", 5, this::snipPredicate));

    }



    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }


    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(SoundEvents.GRASS_STEP, 0.15F, 1.0F);
        this.maxUpStep = 2F;
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_STEP;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.TURTLE_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.TURTLE_DEATH;
    }

    protected float getSoundVolume() {
        return 0.5F;
    }


}
