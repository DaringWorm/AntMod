package com.daringworm.antmod.entity.custom;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.entity.Ant;
import com.daringworm.antmod.entity.brains.parts.WorkingStages;
import com.daringworm.antmod.goals.AntUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.*;

public class AntScentCloud extends Entity implements IAnimatable {

    public AntScentCloud(EntityType<? extends Entity> pEntityType, Level pLevel) {
        super(pEntityType,pLevel);
    }

    public int CLOUD_SIZE;
    public int COLOR;
    public int WORKING_STAGE;
    public int AGE;
    public int COLONY_ID;
    private int timer = 0;

    // for interest entities, such as larvae during nursing or enemies during attacking
    private final HashSet<LivingEntity> interestEntitySet = new HashSet<>();
    // for interest items, mainly for foraging
    private final HashSet<ItemEntity> interestItemSet = new HashSet<>();
    // the larger current pos pile to draw from
    private final HashSet<BlockPos> holdingPosSet = new HashSet<>();
    // the immediate set to pass in to getStaircasePoses
    private final HashSet<BlockPos> currentPosSet = new HashSet<>();
    // the next set to replace holdingPosSet when it's empty
    private final HashSet<BlockPos> nextPosSet = new HashSet<>();
    // for the older poses, to discard after getStaircasePoses
    private final HashSet<BlockPos> discardedPosSet = new HashSet<>();
    // for simple interest poses, like foraging blocks and fungus blocks
    private final HashSet<BlockPos> interestPosSet = new HashSet<>();
    // for container poses during farming
    private final HashSet<BlockPos> containerPosSet = new HashSet<>();
    // the number of times the cycle was repeated
    private final int maxAllowedSearchArea = 800;
    private int maxAllowedAreaPerStep = 45;

    @Override
    public void baseTick() {

        if(!this.getLevel().isClientSide()) {
            timer++;
            this.AGE++;
            if(this.CLOUD_SIZE<30 && this.getLevel().getGameTime() % 10 == 0){this.CLOUD_SIZE++;}

            if (timer > 5) {
                runStageAction();
                timer = 0;
            }

            if(this.AGE > 8000){this.remove(RemovalReason.DISCARDED);}

            if (timer == 0 && hasDataToStart()) {
                List<WorkerAnt> antsInVicinity = this.getLevel().getEntitiesOfClass(WorkerAnt.class, this.getBoundingBox().inflate(24,6,24));
                for (Ant ant : antsInVicinity) {
                    int antCurrentStage = ant.memory.workingStage;
                    if (this.WORKING_STAGE >= antCurrentStage) {
                        ant.memory.workingStage = this.WORKING_STAGE;
                        updateAntInterest(ant);
                    }
                }
            }
        }
        this.emitParticle();


    }

    public void updateAntInterest(Ant pAnt){
        if((pAnt.getTarget() == null || !pAnt.getTarget().isAlive()) &&
                this.hasDataToStart() && this.WORKING_STAGE >= pAnt.memory.workingStage){

            int stg = this.WORKING_STAGE;

            if(stg == WorkingStages.FORAGING){
                if (pAnt.getMainHandItem().isEmpty() && !AntUtils.shouldSnip(pAnt.memory.interestPos, this.getLevel()) &&
                        (this.getLevel().canSeeSky(this.blockPosition()) == this.getLevel().canSeeSky(pAnt.blockPosition()) ||
                        Math.abs(this.getY()-pAnt.getY()) < 8)) {
                    BlockPos targetPos = BlockPos.ZERO;
                    for(BlockPos tempPos : this.interestPosSet){
                        if(pAnt.getDistTo(tempPos) < pAnt.getDistTo(targetPos)){
                            targetPos = tempPos;
                        }
                    }
                    pAnt.memory.interestPos = targetPos;
                    this.interestPosSet.remove(targetPos);
                    pAnt.setWorkingStage(WorkingStages.FORAGING);
                    pAnt.memory.workingStage = WorkingStages.FORAGING;
                    //pAnt.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200));
                }
            }
            else if(stg == WorkingStages.FARMING){
                pAnt.memory.fungusPosSet = this.interestPosSet;
                pAnt.memory.containerPosSet = new ArrayList<>(this.containerPosSet);
            }
            else if(stg == WorkingStages.ATTACKING){
                pAnt.setTarget(this.interestEntitySet.iterator().next());
            }
            else if(stg == WorkingStages.NURSING){
                pAnt.memory.passiveTarget = this.interestEntitySet.iterator().next();
            }
        }
    }

    private boolean hasDataToStart(){
        int stg = this.WORKING_STAGE;
        return (stg == WorkingStages.FORAGING && !this.interestPosSet.isEmpty()) ||
                (stg == WorkingStages.FARMING && !interestPosSet.isEmpty() && !containerPosSet.isEmpty()) ||
                (stg == WorkingStages.NURSING && !interestEntitySet.isEmpty()) ||
                (stg == WorkingStages.ATTACKING && !interestEntitySet.isEmpty());
    }

    private void emitParticle(){
        Level pLevel = this.getLevel();
        Random random = pLevel.getRandom();
        final float pSpeed = 0.15f;
        final float pSpread = 20f;
        final int percentChance = 100 - Math.max(100 - this.CLOUD_SIZE, 1);

        if(random.nextInt(100) <= percentChance) {
            ParticleOptions particleoptions = ParticleTypes.SNEEZE;
            pLevel.addParticle(particleoptions,
                    this.getX() + random.nextFloat(pSpread) - (pSpread / 2f),
                    this.getY() + random.nextFloat(pSpread) - (pSpread / 2f),
                    this.getZ() + random.nextFloat(pSpread) - (pSpread / 2f),
                    random.nextFloat(pSpeed * 2) - pSpeed,
                    random.nextFloat(pSpeed * 2) - pSpeed,
                    random.nextFloat(pSpeed * 2) - pSpeed);
        }

        for(BlockPos tempPos :interestPosSet){
            ParticleOptions particleoptions = ParticleTypes.HEART;
            pLevel.addAlwaysVisibleParticle(particleoptions,
                    tempPos.getX(),
                    tempPos.getY(),
                    tempPos.getZ(),
                    random.nextFloat(pSpeed * 2) - pSpeed,
                    random.nextFloat(pSpeed * 2) - pSpeed,
                    random.nextFloat(pSpeed * 2) - pSpeed);
            //this.getLevel().setBlock(tempPos.above(7), Blocks.GLASS.defaultBlockState(), 2);
        }
    }

    private void runStageAction(){
        int stg = this.WORKING_STAGE;

        if(stg == WorkingStages.WANDERING){
        }
        if(stg == WorkingStages.SCOUTING){
        }
        if(stg == WorkingStages.FORAGING){
            if(this.holdingPosSet.isEmpty() && nextPosSet.isEmpty() && currentPosSet.isEmpty()){
                holdingPosSet.add(this.blockPosition());}
            if(this.discardedPosSet.size() >= maxAllowedSearchArea){
                holdingPosSet.clear();
                discardedPosSet.clear();
                nextPosSet.clear();
                currentPosSet.clear();
                if(interestPosSet.isEmpty()){
                    this.remove(RemovalReason.DISCARDED);
                }
            }
            this.expandSearchArea();
            this.interestEntitySet.clear();
            this.interestItemSet.addAll(this.getLevel().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(4d)));
        }
        if(stg == WorkingStages.FARMING){
            this.expandSearchArea();
        }
        if(stg == WorkingStages.NURSING){
            this.interestEntitySet.clear();
            this.interestEntitySet.addAll(this.getLevel().getEntitiesOfClass(AntEgg.class, this.getBoundingBox()));
        }
        if(stg == WorkingStages.TIDYING){

        }
        if(stg == WorkingStages.EXCAVATING){

        }
        if(stg == WorkingStages.ATTACKING){
            ArrayList<LivingEntity> tempList = (ArrayList<LivingEntity>) this.getLevel().getEntitiesOfClass(LivingEntity.class,this.getBoundingBox().inflate(3));
            for(LivingEntity tempE : tempList){
                if(tempE instanceof Ant && ((Ant) tempE).getColonyID() != this.COLONY_ID){
                    this.interestEntitySet.add(tempE);
                }
            }
        }
        if(stg == WorkingStages.LATCHING){

        }
    }

    private void expandSearchArea(){
        int i = 0;
        if(currentPosSet.isEmpty()){
            if(holdingPosSet.isEmpty()){
                holdingPosSet.addAll(nextPosSet);
                nextPosSet.clear();
            }
            for(BlockPos tempPos : holdingPosSet){
                i++;
                if(i < maxAllowedAreaPerStep){currentPosSet.add(tempPos);}
            }
            holdingPosSet.removeAll(currentPosSet);
        }
        for(BlockPos tempPos : currentPosSet){
            HashSet<BlockPos> tempSet = getStaircasePoses(tempPos);
            tempSet.removeAll(currentPosSet);
            tempSet.removeAll(holdingPosSet);
            tempSet.removeAll(nextPosSet);
            nextPosSet.addAll(tempSet);
            for(BlockPos tempPos1 : tempSet){checkForInterest(tempPos1);}
        }
        discardedPosSet.addAll(currentPosSet);
        currentPosSet.clear();
    }

    private void checkForInterest(BlockPos pPos){
        Level pLevel = this.getLevel();

        if(this.WORKING_STAGE == WorkingStages.FORAGING){
            if(AntUtils.shouldSnip(pPos, pLevel)){interestPosSet.add(pPos);}
            if(AntUtils.shouldSnip(pPos.above(), pLevel)){interestPosSet.add(pPos.above());}
            if(AntUtils.shouldSnip(pPos.below(), pLevel)){interestPosSet.add(pPos.below());}
        }
        else if(this.WORKING_STAGE == WorkingStages.FARMING){
            Block pBlock = pLevel.getBlockState(pPos).getBlock();
            if (pBlock == ModBlocks.FUNGUS_BLOCK.get()) {
                interestPosSet.add(pPos);
            } else if (pBlock == ModBlocks.LEAFY_CONTAINER_BLOCK.get()) {
                containerPosSet.add(pPos);
            }
            for(Direction dir : Direction.values()) {
                pBlock = pLevel.getBlockState(pPos.relative(dir)).getBlock();
                if (pBlock == ModBlocks.FUNGUS_BLOCK.get()) {
                    interestPosSet.add(pPos);
                } else if (pBlock == ModBlocks.LEAFY_CONTAINER_BLOCK.get()) {
                    containerPosSet.add(pPos);
                }
            }
        }
    }

    private HashSet<BlockPos> getStaircasePoses(BlockPos startPos){
        HashSet<BlockPos> returnSet = new HashSet<>();

        for(Direction dir : Direction.values()){
            for(int i = -1; i <= 1; i++) {
                BlockPos tempPos = startPos.relative(dir, 1).offset(0,i,0);
                BlockState tempState = this.getLevel().getBlockState(tempPos);
                BlockPos tempPosUnder = tempPos.below();
                BlockState tempStateUnder = this.getLevel().getBlockState(tempPosUnder);

                if (!tempState.isCollisionShapeFullBlock(this.getLevel(), tempPos)) {
                    if (tempStateUnder.isCollisionShapeFullBlock(this.getLevel(), tempPosUnder)) {
                        returnSet.add(tempPos);
                    }
                }
            }
        }

        return returnSet;
    }

    @Override
    protected void defineSynchedData() {
        this.CLOUD_SIZE = 10;
        this.WORKING_STAGE = 2;
        this.COLOR = 0;
        this.COLONY_ID = 0;
        this.AGE = 0;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        pCompound.putInt("Type", this.WORKING_STAGE);
        pCompound.putInt("Size", this.CLOUD_SIZE);
        pCompound.putInt("Color", this.COLOR);
        pCompound.putInt("Age", this.AGE);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        this.WORKING_STAGE = pCompound.getInt("Type");
        this.AGE = (pCompound.getInt("Age"));
        this.COLOR = (pCompound.getInt("Color"));
        this.CLOUD_SIZE = (pCompound.getInt("Size"));
        this.holdingPosSet.add(this.blockPosition());
        this.checkForInterest(this.blockPosition());
        this.setNoGravity(true);
    }


    public @NotNull PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    public @NotNull Packet<?> getAddEntityPacket() {return new ClientboundAddEntityPacket(this);}

    public @NotNull EntityDimensions getDimensions(Pose pPose) {return EntityDimensions.scalable(this.CLOUD_SIZE, this.CLOUD_SIZE*0.75f);}

    @Override
    public Iterable<ItemStack> getArmorSlots() {return Collections.singleton(ItemStack.EMPTY);}

    public boolean isAttackable(){return false;}

    @Override
    public void setItemSlot(EquipmentSlot pSlot, ItemStack pStack) {}

    @Override
    public void registerControllers(AnimationData data) {}

    private final AnimationFactory factory = new AnimationFactory(this);
    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

}