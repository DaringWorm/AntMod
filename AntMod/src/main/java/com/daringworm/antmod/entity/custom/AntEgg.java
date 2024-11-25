package com.daringworm.antmod.entity.custom;

import com.daringworm.antmod.entity.Ant;
import com.daringworm.antmod.entity.ModEntityTypes;
import com.daringworm.antmod.entity.brains.parts.WorkingStages;
import com.daringworm.antmod.colony.AntColony;
import com.daringworm.antmod.mixin.tomixin.ServerLevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Random;

public class AntEgg extends Ant implements IAnimatable {

    private static final EntityDataAccessor<Integer> EGG_TIMER = SynchedEntityData.defineId(AntEgg.class, EntityDataSerializers.INT);

    public void setThisEggTimer(int pHunger) {
        this.entityData.set(EGG_TIMER, pHunger);
    }
    public int getThisEggTimer(){return this.entityData.get(EGG_TIMER);}




    public final Random qRandom = new Random();

    private AnimationFactory factory = new AnimationFactory(this);

    public AntEgg(EntityType<? extends Ant> entityType, Level level) {
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


    public static AttributeSupplier setAttributes(){
        return Ant.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 2)
                .add(Attributes.MOVEMENT_SPEED, 0)
                .add(Attributes.ATTACK_DAMAGE, 0)
                .add(Attributes.ATTACK_SPEED, 0)
                .add(Attributes.ARMOR, 0)
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
        if(!this.getLevel().isClientSide) {
            if (this.isAlive() && this.getThisEggTimer() >= 100) {
                WorkerAnt pAnt = new WorkerAnt(ModEntityTypes.WORKERANT.get(), this.getLevel());
                pAnt.moveTo(Vec3.atCenterOf(this.blockPosition()));
                pAnt.setColonyID(this.getColonyID());
                pAnt.setWorkingStage(WorkingStages.SCOUTING);
                pAnt.setHomeContainerPos(this.blockPosition());
                this.getLevel().addFreshEntity(pAnt);
                pAnt.setFirstSurfacePos(this.getSurfacePos());
                this.remove(RemovalReason.DISCARDED);

            }
            this.setThisEggTimer(1 + this.getThisEggTimer());
            if (this.getLastHurtByMob() != null) {
                AntColony colony = new AntColony(this.level, this.getColonyID(), BlockPos.ZERO);
                ServerLevel level = (ServerLevel) this.getLevel();
                ((ServerLevelUtil) (level)).addColonyToList(colony);
                colony.save();
                this.remove(RemovalReason.DISCARDED);
            }
        }
    }



    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event){
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

    @org.jetbrains.annotations.Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
        return null;
    }
}

