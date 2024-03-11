package com.daringworm.antmod.entity.custom;

import com.daringworm.antmod.entity.Ant;
import com.daringworm.antmod.entity.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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

public class AntLarva extends AntEgg implements IAnimatable {

    private AnimationFactory factory = new AnimationFactory(this);

    public AntLarva(EntityType<? extends Ant> entityType, Level level) {
        super(entityType, level);
    }


    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
    }


    public static AttributeSupplier setAttributes(){
        return Ant.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6)
                .add(Attributes.MOVEMENT_SPEED, 0.05f)
                .add(Attributes.ATTACK_DAMAGE, 0)
                .add(Attributes.ATTACK_SPEED, 0)
                .add(Attributes.ARMOR, 0)
                .build();

    }
    public void aiStep(){
        if(!this.getLevel().isClientSide) {
            if (this.isAlive()) {
                WorkerAnt newAnt = ModEntityTypes.WORKERANT.get().create(this.level);
                assert newAnt != null;
                newAnt.setColonyID(this.getColonyID());
                newAnt.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                newAnt.maxUpStep = 1.13f;
                newAnt.setLatchDirection((int) (this.level.getGameTime() % 100));
                this.level.addFreshEntity(newAnt);
                this.remove(Entity.RemovalReason.DISCARDED);
            }
        }
    }



    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event){
        event.getController().setAnimation(new AnimationBuilder().addAnimation("antlarva1.animation.new", true));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller", 20, this::predicate));

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