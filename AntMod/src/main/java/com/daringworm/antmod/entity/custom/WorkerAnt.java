package com.daringworm.antmod.entity.custom;

import com.daringworm.antmod.entity.ModEntityTypes;
import com.daringworm.antmod.entity.brains.LeafCutterWorkerBrain;
import com.daringworm.antmod.entity.Ant;

import com.daringworm.antmod.entity.brains.parts.WorkingStages;
import com.daringworm.antmod.item.ModItems;
import com.daringworm.antmod.util.AntUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;


public class WorkerAnt extends Ant implements IAnimatable {

    // entity data managing worker ants latching onto other entities
    private static final EntityDataAccessor<Integer> LATCH_DIRECTION = SynchedEntityData.defineId(WorkerAnt.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> LATCH_TARGET =  SynchedEntityData.defineId(WorkerAnt.class, EntityDataSerializers.OPTIONAL_UUID);

    public void setLatchDirection(int pInt){this.getEntityData().set(LATCH_DIRECTION, pInt);}
    public int getLatchDirection(){return this.getEntityData().get(LATCH_DIRECTION);}

    public boolean hasLatchTarget(){return this.entityData.get(LATCH_TARGET).isPresent();}
    public void setLatchTarget(Entity entity){this.entityData.set(LATCH_TARGET, Optional.of(entity.getUUID()));}
    public void setLatchTarget(UUID id){this.entityData.set(LATCH_TARGET, Optional.of(id));}
    public void setLatchTargetNull(){this.entityData.set(LATCH_TARGET, Optional.empty());}
    public UUID getLatchTarget(){
        if(!this.hasLatchTarget()){
            throw new RuntimeException("Tried to get the Latch Target for a Worker Ant with none.");
        }
        return this.entityData.get(LATCH_TARGET).get();
    }


    public Vec3 getLatchPos(LivingEntity forEntity){
        float addpos = this.getLatchDirection()*40;
        Vec3 changevec = Vec3.directionFromRotation(0,addpos);
        changevec = new Vec3(changevec.x, -this.getEyeHeight(), changevec.z).add(forEntity.getEyePosition());
        AntUtils.broadcastString(this.getLevel(), "Moved to: " + changevec);
        return changevec;
    }



    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(LATCH_DIRECTION, 0);
        this.entityData.define(LATCH_TARGET, Optional.empty());

    }

    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("LatchDirection", this.getLatchDirection());
        if(this.hasLatchTarget()){
            pCompound.putUUID("LatchTarget", this.getLatchTarget());
        }
        else{
            pCompound.putString("LatchTarget", "null");
        }
    }
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.setLatchDirection(pCompound.getInt("LatchDirection"));
        if(pCompound.getString("LatchTarget").equals("null")){
            this.setLatchTargetNull();
        }
        else{
            this.setLatchTarget(pCompound.getUUID("LatchTarget"));
        }
    }


    public WorkerAnt(EntityType<? extends Ant> entityType, Level level) {
        super(entityType, level);
        this.maxUpStep = 1.14f;
    }



    private AnimationFactory factory = new AnimationFactory(this);

    /**
     * Standard base attributes
     * **/

    public static AttributeSupplier setAttributes(){
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.MOVEMENT_SPEED, 0.33)
                .add(Attributes.ATTACK_DAMAGE, 5)
                .add(Attributes.ATTACK_SPEED, 5)
                .add(Attributes.ARMOR, 8)
                .add(Attributes.FOLLOW_RANGE, Integer.MAX_VALUE)
                .build();
    }

    /**
     * goals, pPriority is listed from lowest to highest, with the lowest being prioritized over higher competitors
     * **/

    protected void registerGoals (){
        this.goalSelector.addGoal(1, new FloatGoal(this));
    }



    /**
     * gives default ants items
     * **/

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        this.setHomeContainerPos(this.blockPosition());
        this.setFoodLocation(BlockPos.ZERO);
        this.setLatchDirection(findDigit((int)this.level.getGameTime(),1));
        this.setSubClass(findDigit((int) (this.level.random.nextInt(11)),1));
        this.maxUpStep = 1.13F;
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }




/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public long gameTickTime;
    //works
    protected void dropAllDeathLoot(DamageSource pDamageSource) {
        super.dropAllDeathLoot(pDamageSource);
    }
    protected void dropEquipment() { // Forge: move extra drops to dropEquipment to allow them to be captured by LivingDropsEvent
        super.dropEquipment();
        ItemStack itemstack = this.getItemBySlot(EquipmentSlot.MAINHAND);
        if (!itemstack.isEmpty()) {
            this.spawnAtLocation(itemstack);
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }

    }

    @Override
    public InteractionResult interactAt(Player pPlayer, Vec3 pVec, InteractionHand pHand) {
        super.interactAt(pPlayer,pVec,pHand);

        if(pPlayer.getMainHandItem().getItem() == ModItems.WORKER_ANT_SPAWN_EGG.get() && !this.getLevel().isClientSide()){
            WorkerAnt newAnt = new WorkerAnt(ModEntityTypes.WORKERANT.get(), this.level);
            newAnt.setPos(this.position());
            this.level.addFreshEntity(newAnt);
            newAnt.setHomeContainerPos(this.getHomeContainerPos());
            newAnt.setColonyID(this.getColonyID());
            newAnt.setWorkingStage(this.getWorkingStage());
            newAnt.setRoomID(this.getRoomID());
        }
        else if(pPlayer.getMainHandItem().getItem() == Items.BONE && !this.getLevel().isClientSide()){
            this.setShouldRunBrain(!this.getShouldRunBrain());
        }
        return InteractionResult.PASS;
    }

    public boolean canHoldItem(ItemStack pStack) {
        Item item = pStack.getItem();
        ItemStack itemstack = this.getItemBySlot(EquipmentSlot.MAINHAND);
        return itemstack.isEmpty() || item.isEdible() && !itemstack.getItem().isEdible();
    }
    private void spitOutItem(ItemStack pStack) {
        if (!pStack.isEmpty() && !this.level.isClientSide) {
            ItemEntity itementity = new ItemEntity(this.level, this.getX() + this.getLookAngle().x, this.getY() + 1.0D, this.getZ() + this.getLookAngle().z, pStack);
            itementity.setPickUpDelay(40);
            itementity.setThrower(this.getUUID());
            this.playSound(SoundEvents.FOX_SPIT, 1.0F, 1.0F);
            this.level.addFreshEntity(itementity);
        }
    }
    private void dropItemStack(ItemStack pStack) {
        ItemEntity itementity = new ItemEntity(this.level, this.getX(), this.getY(), this.getZ(), pStack);
        this.level.addFreshEntity(itementity);
    }
    /**
     * Tests if this entity should pickup a weapon or an armor. Entity drops current weapon or armor if the new one is
     * better.
     */
    @Override
    protected void pickUpItem(ItemEntity pItemEntity) {
        ItemStack itemstack = pItemEntity.getItem();
        if (this.canHoldItem(itemstack)) {
            int i = itemstack.getCount();
           //if (i > 1) {this.dropItemStack(itemstack.split(i - 1));}

            this.spitOutItem(this.getItemBySlot(EquipmentSlot.MAINHAND));
            this.onItemPickup(pItemEntity);
            //this.setItemSlot(EquipmentSlot.MAINHAND, itemstack.split(1));
            this.setItemSlot(EquipmentSlot.MAINHAND, itemstack);
            this.handDropChances[EquipmentSlot.MAINHAND.getIndex()] = 2.0F;
            this.take(pItemEntity, itemstack.getCount());
            pItemEntity.discard();
        }
    }

    /**
     * for constant status updates
    **/

    public void aiStep() {
        super.aiStep();

        if(!this.level.isClientSide) {
            this.getLevel().getProfiler().push("brain");
            LeafCutterWorkerBrain.run(this);
            this.getLevel().getProfiler().pop();
        }
        if(this.getWorkingStage() == WorkingStages.LATCHING){
            if (this.getTarget() != null && this.level.isClientSide){
                this.lookAt(this.getTarget(),360,360);
                //Actions.LATCH_ON.run(this);
            }
        }
    }

    protected void customServerAiStep() {

    }


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


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
        data.addAnimationController(new AnimationController(this, "walk_or_rest_controller", 5, this::walkPredicate));
        data.addAnimationController(new AnimationController(this, "snipcontroller", 5, this::snipPredicate));

    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
    /**
     * The sounds used
     * **/

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