package com.daringworm.antmod.entity.custom;

import com.daringworm.antmod.entity.Ant;
import com.daringworm.antmod.colony.ColonyGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Random;

/*public class AntCarver extends Ant implements IAnimatable {


    @javax.annotation.Nullable
    private BlockPos targetPosition;

    public final Random qRandom = new Random((this.getBlockX()+this.getBlockZ())/this.getBlockY());


    private AnimationFactory factory = new AnimationFactory(this);

    public AntCarver(EntityType<? extends Ant> entityType, Level level) {
        super(entityType, level);
    }

    //monitors its age for despawning purposes
    private static final EntityDataAccessor<Integer> TICKS_PASSED = SynchedEntityData.defineId(AntCarver.class, EntityDataSerializers.INT);

    //if its naturally spawned
    private static final EntityDataAccessor<Boolean> IS_NATURAL = SynchedEntityData.defineId(AntCarver.class, EntityDataSerializers.BOOLEAN);

    //may be useful idk
    private static final EntityDataAccessor<BlockPos> HOME_COLONY_POS = SynchedEntityData.defineId(AntCarver.class, EntityDataSerializers.BLOCK_POS);



    public void setHomeColonyPos(BlockPos pPosition) {
        this.entityData.set(HOME_COLONY_POS, pPosition);
    }
    public BlockPos getHomeColonyPos() {
        return this.entityData.get(HOME_COLONY_POS);
    }
    public void setThisTicks(int pHunger) {
        this.entityData.set(TICKS_PASSED, pHunger);
    }
    public Integer getThisTicks(){return this.entityData.get(TICKS_PASSED);}
    public void setThisIsNatural(boolean pID) {
        this.entityData.set(IS_NATURAL, pID);
    }
    public Boolean getThisIsNatural(){return this.entityData.get(IS_NATURAL);}


    public void tick() {
        this.noPhysics = true;
        super.tick();
        this.setNoGravity(true);
    }



    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HOME_COLONY_POS, this.blockPosition());
        this.entityData.define(TICKS_PASSED, 90);
        this.entityData.define(IS_NATURAL, false);
    }

    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("HomePosX", this.getHomeColonyPos().getX());
        pCompound.putInt("HomePosY", this.getHomeColonyPos().getY());
        pCompound.putInt("HomePosZ", this.getHomeColonyPos().getZ());
        pCompound.putInt("TickCount", this.getThisTicks());
        pCompound.putBoolean("Natural", this.getThisIsNatural());
    }


    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        int i = pCompound.getInt("HomePosX");
        int j = pCompound.getInt("HomePosY");
        int k = pCompound.getInt("HomePosZ");
        this.setHomeColonyPos(new BlockPos(i, j, k));
        int h = pCompound.getInt("TickCount");
        this.setThisTicks(h);
        boolean c = pCompound.getBoolean("Natural");
        this.setThisIsNatural(c);
    }


    public static AttributeSupplier setAttributes(){
        return Ant.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 2)
                .add(Attributes.MOVEMENT_SPEED, 0)
                .add(Attributes.ATTACK_DAMAGE, 0)
                .add(Attributes.ATTACK_SPEED, 0)
                .add(Attributes.ARMOR, 0)
                .build();
    }

    protected void registerGoals (){
        this.goalSelector.addGoal(1, new BetterCarveGoal(this));
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    protected void customServerAiStep() {
        int ticks = this.getThisTicks();
        /*super.customServerAiStep();
            if (this.targetPosition != null && (this.level.isEmptyBlock(this.targetPosition) || this.targetPosition.getY() <= this.level.getMinBuildHeight())) {
                this.targetPosition = null;
            }

            if ((this.targetPosition == null || this.random.nextInt(30) == 0 || this.targetPosition.closerToCenterThan(this.position(), 2.0D))&&this.getY()>30) {
                this.targetPosition = new BlockPos(this.getX() + this.getX() % 10,
                        this.getY() - ((sqrt(Math.pow(this.getX() % 10, 2) + Math.pow(this.getZ() % 10, 2)))/(this.getHomeColonyPos().getY()-this.getY()+2)),
                        this.getZ() + this.getZ() % 10);

                /*sets a new movement target that is not too steep to be unnavigable by ants*/
        /*  }

            if (this.targetPosition != null) {
                double d2 = (double) this.targetPosition.getX() + 0.5D - this.getX();
                double d0 = (double) this.targetPosition.getY() + 0.5D - this.getY();
                double d1 = (double) this.targetPosition.getZ() + 0.5D - this.getZ();
                Vec3 vec3 = this.getDeltaMovement();
                Vec3 vec31 = vec3.add((Math.signum(d2) * 0.5D - vec3.x) * (double) 0.5F, (Math.signum(d0) * (double) 0.7F - vec3.y) * (double) 0.5F, (Math.signum(d1) * 0.5D - vec3.z) * (double) 0.5F);
                this.setDeltaMovement(vec31);
                float f = (float) (Mth.atan2(vec31.z, vec31.x) * (double) (180F / (float) Math.PI)) - 90.0F;
                float f1 = Mth.wrapDegrees(f - this.getYRot());
                this.zza = 0.5F;
                this.setYRot(this.getYRot() + f1);
            }
            ticks++;
            this.setThisTicks(ticks);*/
    /*}


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //sets home position
    @javax.annotation.Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @javax.annotation.Nullable SpawnGroupData pSpawnData, @javax.annotation.Nullable CompoundTag pDataTag) {
        this.setHomeColonyPos(this.blockPosition());
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }


    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel p_146743_, AgeableMob p_146744_) {
        return null;
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event){
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.antegg.rest", true));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller", 5, this::predicate));

    }



    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }


    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(SoundEvents.GRASS_STEP, 0.15F, 1.0F);
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


public class AntCarver extends Ant implements IAnimatable {

    private static final EntityDataAccessor<Integer> EGG_TIMER = SynchedEntityData.defineId(AntCarver.class, EntityDataSerializers.INT);

    public void setThisEggTimer(int pHunger) {
        this.entityData.set(EGG_TIMER, pHunger);
    }

    public int getThisEggTimer() {
        return this.entityData.get(EGG_TIMER);
    }


    public final Random qRandom = new Random();

    private AnimationFactory factory = new AnimationFactory(this);

    public AntCarver(EntityType<? extends Ant> entityType, Level level) {
        super(entityType, level);
    }


    protected void defineSynchedData() {
        this.entityData.define(EGG_TIMER, 0);
        super.defineSynchedData();
    }

    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("EggTimer", this.getThisEggTimer());
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readAdditionalSaveData(CompoundTag pCompound) {
        this.setThisEggTimer(pCompound.getInt("EggTimer"));
    }


    public static AttributeSupplier setAttributes() {
        return Ant.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 2)
                .add(Attributes.MOVEMENT_SPEED, 0)
                .add(Attributes.ATTACK_DAMAGE, 0)
                .add(Attributes.ATTACK_SPEED, 0)
                .add(Attributes.ARMOR, 0)
                .build();

    }

    protected void registerGoals() {
        //this.goalSelector.addGoal(1, new BetterCarveGoal(this));
    }

    public void aiStep() {
        if(!this.level.isClientSide) {
            if(Math.abs(this.getY()-this.getLevel().getSeaLevel())<10) {
                ColonyGenerator goal = new ColonyGenerator(this.getLevel());
                goal.generate(this.blockPosition());
            }
            this.remove(RemovalReason.DISCARDED);
        }
    }


    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel p_146743_, AgeableMob p_146744_) {
        return null;
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.antthis.rest", true));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller", 5, this::predicate));

    }


    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }


    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(SoundEvents.GRASS_STEP, 0.15F, 1.0F);
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