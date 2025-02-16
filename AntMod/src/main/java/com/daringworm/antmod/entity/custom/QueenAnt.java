package com.daringworm.antmod.entity.custom;


import com.daringworm.antmod.entity.Ant;
import com.daringworm.antmod.entity.brains.LeafCutterQueenBrain;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import static javax.swing.UIManager.getInt;

public class QueenAnt extends Ant implements IAnimatable {


    private static final EntityDataAccessor<Integer> LAST_EGG_LAID_TIME = SynchedEntityData.defineId(QueenAnt.class, EntityDataSerializers.INT);

    public void setThisEggTimer(int pHunger) {
        this.entityData.set(LAST_EGG_LAID_TIME, pHunger);
    }
    public int getThisEggTimer(){return this.entityData.get(LAST_EGG_LAID_TIME);}


    private AnimationFactory factory = new AnimationFactory(this);

    public QueenAnt(EntityType<? extends Ant> entityType, Level level) {
        super(entityType, level);
    }


    protected void defineSynchedData() {
        this.entityData.define(LAST_EGG_LAID_TIME, 0);
        super.defineSynchedData();
    }

    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("EggTime", this.getThisEggTimer());
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readAdditionalSaveData(CompoundTag pCompound) {
        this.setThisEggTimer(pCompound.getInt("EggTime"));
    }


    public static AttributeSupplier setAttributes(){
        return Ant.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 120)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.ATTACK_DAMAGE, 15)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.ARMOR, 10)
                .build();

    }

    @Override
    public InteractionResult interactAt(Player pPlayer, Vec3 pVec, InteractionHand pHand) {
        return InteractionResult.FAIL;
    }

    protected void registerGoals (){
        this.goalSelector.addGoal(1, new FloatGoal(this));
    }

    public void aiStep(){
        if(this.getLevel() instanceof ServerLevel) {
            LeafCutterQueenBrain.run(this);
            this.maxUpStep = 1.17f;
        }
        super.aiStep();
    }



    @Override
    public boolean isOnGround(){
        return true;
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

/*
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
    }*/

    @org.jetbrains.annotations.Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
        return null;
    }

}
