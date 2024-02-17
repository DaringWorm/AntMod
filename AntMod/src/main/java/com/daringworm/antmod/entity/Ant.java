package com.daringworm.antmod.entity;


import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.block.entity.custom.FungalContainerBlockEntity;
import com.daringworm.antmod.entity.brains.memories.LeafCutterMemory;
import com.daringworm.antmod.colony.misc.PosPair;
import com.daringworm.antmod.goals.AntUtils;
import com.daringworm.antmod.item.ModItems;
import com.daringworm.antmod.mixin.tomixin.ServerLevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.daringworm.antmod.goals.AntUtils.getDist;


public abstract class Ant extends PathfinderMob implements MenuProvider {

    public LeafCutterMemory memory;

    private static final EntityDataAccessor<BlockPos> HOME_POS = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<String> ROOM_ID = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<BlockPos> FIRST_SURFACE_POS = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<BlockPos> FOOD_LOCATION = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Boolean> IS_ABOVEGROUND = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_COMPETENT = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_IN_TRANSITION = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_MINING_ANIMATION = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> HUNGER_LEVEL = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> COLONY_ID = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.INT);
    /**1 place is workingstage; 10 place is subclass; 100s place is for hascheckedhomepos; 1000s is latch direction for workerants**/
    private static final EntityDataAccessor<Integer> MISC_DATA = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WORKING_STAGE = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.INT);


    public double getDistTo(BlockPos pPos){return getDist(this.blockPosition(),pPos);}


    public void setInterestBlock(BlockPos pPos){memory.interestPos = pPos;}


    public BlockPos getColonyPos(){
        if(((ServerLevelUtil) this.getLevel()).getColonyWithID(this.getColonyID()) != null){
            return ((ServerLevelUtil) this.getLevel()).getColonyWithID(this.getColonyID()).getEntranceBottom();
        }
        else return BlockPos.ZERO;
    }

    public void setHomePos(BlockPos pPosition) {this.entityData.set(HOME_POS, pPosition);}
    public BlockPos getHomePos() {
        return this.entityData.get(HOME_POS);
    }
    public void setRoomID(String branchID) {this.entityData.set(ROOM_ID, branchID);}
    public String getRoomID() {return this.entityData.get(ROOM_ID);}
    public void setFirstSurfacePos(BlockPos pPosition) {
        this.entityData.set(FIRST_SURFACE_POS, pPosition);
    }
    public BlockPos getFirstSurfacePos() {
        return this.entityData.get(FIRST_SURFACE_POS);
    }
    public void setFoodLocation(BlockPos pPosition) {
        this.entityData.set(FOOD_LOCATION, pPosition);
    }
    public BlockPos getFoodLocation(){return this.entityData.get(FOOD_LOCATION);}
    public void setIsInTransition(boolean pHasEgg) {
        this.entityData.set(IS_IN_TRANSITION, pHasEgg);
    }
    public boolean getIsInTransition(){return this.entityData.get(IS_IN_TRANSITION);}
    public void setSnippingAnimation(boolean pHasEgg2) {
        this.entityData.set(IS_MINING_ANIMATION, pHasEgg2);
    }
    public boolean getIsSnippingAnimation(){return this.entityData.get(IS_MINING_ANIMATION);}
    public void setIsAboveground(boolean isUp) {
        this.entityData.set(IS_ABOVEGROUND, isUp);
    }
    public boolean getIsAboveground(){return this.entityData.get(IS_ABOVEGROUND);}
    public boolean isCompetent() {
        return this.entityData.get(IS_COMPETENT);
    }
    public void setIsCompetent(boolean pHasEgg) {
        this.entityData.set(IS_COMPETENT, pHasEgg);
    }
    public boolean getIsCompetent(){return this.entityData.get(IS_COMPETENT);}
    public void setHunger(int pHunger) {
        this.entityData.set(HUNGER_LEVEL, pHunger);
    }
    public int getHunger(){return this.entityData.get(HUNGER_LEVEL);}
    public void setColonyID(int pID) {
        this.entityData.set(COLONY_ID, pID);
    }
    public int getColonyID(){return this.entityData.get(COLONY_ID);}
    public void setThisMiscRAW(int pMisc) {
        this.entityData.set(MISC_DATA, pMisc);
    }
    public void setWorkingStage(int pStage) {
        this.entityData.set(WORKING_STAGE, pStage);
    }
    public int getWorkingStage(){return this.entityData.get(WORKING_STAGE);}

    public void setThisMisc(int pValue, int digit) {
        int totalMisc = this.entityData.get(MISC_DATA);
        int digitInQuestion = findDigit(totalMisc,digit);
        int miscWithoutDigit = (int) (totalMisc - digitInQuestion*Math.pow(10,digit-1));
        int newTotalMisc = (int) (miscWithoutDigit + pValue*Math.pow(10,digit-1));

        this.entityData.set(MISC_DATA, newTotalMisc);
    }
// under MISC
    public void setSubClass(int pClass){
        this.setThisMisc(pClass,2);
    }
    public int getSubClass(){return findDigit(this.entityData.get(MISC_DATA), 2);}
// under MISC
    public int getHasCheckedHome(){return findDigit(this.entityData.get(MISC_DATA), 3);}
// under MISC
    public void setHasCheckedHome(int oneOrTwo){this.setThisMisc(oneOrTwo,3);}
    public int getThisMisc(int value){return findDigit(this.entityData.get(MISC_DATA), value);}
    public int getThisMiscRAW(){return this.entityData.get(MISC_DATA);}

    public int findDigit(int input, int digit){
        int ten = (int) Math.pow(10,digit-1);
        int noBigger = (input%(ten*10)-(input%ten));
        return noBigger/ten;
    }

    public void walkTo(BlockPos blockPos, double speedModifier, double distanceModifier){
        if(this.memory != null && this.memory.navDelay <= 25){return;}
        assert this.isOnGround();
        assert blockPos != null && this.getLevel().isLoaded(blockPos) && blockPos != this.getNavigation().getTargetPos();
        this.memory.navDelay = 0;
        Path path = this.getNavigation().getPath();
        Level pLevel = this.getLevel();
        BlockPos targetPos = blockPos;

        if(!pLevel.getBlockState(targetPos).isPathfindable(pLevel,targetPos, PathComputationType.LAND)) {
            boolean isTargetUpdated = false;
            for (Direction dir : Direction.values()) {
                BlockPos tempPos = targetPos.relative(dir);
                BlockState tempState = pLevel.getBlockState(tempPos);
                if(tempState.isPathfindable(pLevel,tempPos,PathComputationType.LAND)){
                    if (isTargetUpdated){
                        if(AntUtils.getDist(tempPos, this.blockPosition()) < AntUtils.getDist(targetPos, this.blockPosition())){
                            targetPos = tempPos;
                        }
                    }
                    else{
                        targetPos = tempPos;
                    }
                }
            }
        }

        if(path == null || (path != null && path.getTarget() != targetPos)) {
            this.getNavigation().setMaxVisitedNodesMultiplier((float)distanceModifier);
            this.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), speedModifier);
            path = this.getNavigation().getPath();
        }
    }

    public void walkAlongList(ArrayList<BlockPos> posList, int speedModifier, double farthestAllowed){
        assert !posList.isEmpty();

        double MAX_ALLOWED_PATH_NODES = 4;

        if(AntUtils.getDist(this.blockPosition(),posList.get(posList.size()-1)) > farthestAllowed) {
            if (this.getNavigation().isDone() || this.getNavigation().isStuck()) {
                BlockPos nearestPos = AntUtils.findNearestBlockPos(this, posList);
                int index = posList.indexOf(nearestPos);
                BlockPos nextPos = (index + 2 <= posList.size()) ? posList.get(index+1) : nearestPos;
                BlockPos antPos = this.blockPosition();
                double distanceToNearest = AntUtils.getHorizontalDist(nearestPos, antPos);
                double distanceToNext = AntUtils.getHorizontalDist(nextPos, antPos);
                double distanceFromNearestToNext = AntUtils.getDist(nearestPos, nextPos);

                int verticalDistanceToNearest = Math.abs(antPos.getY()-nearestPos.getY());
                int verticalDistanceToNext = Math.abs(antPos.getY()-nextPos.getY());
                int verticalDistanceToFirst = Math.abs(antPos.getY()-posList.get(0).getY());

                if(verticalDistanceToFirst < verticalDistanceToNearest){
                    this.walkTo(posList.get(0), 1, Math.min(MAX_ALLOWED_PATH_NODES,distanceToNext));
                }
                else if (distanceToNext < distanceFromNearestToNext || distanceToNearest < farthestAllowed) {
                    this.walkTo(nextPos, 1, Math.min(MAX_ALLOWED_PATH_NODES,distanceToNext));
                } else {
                    this.walkTo(nearestPos, 1, Math.min(MAX_ALLOWED_PATH_NODES,distanceToNearest));
                }
            }
        }
    }
    
    public boolean shouldRunBrain() {
        return this.getLevel().isLoaded(this.blockPosition()) &&
            this.getLevel().isLoaded(this.blockPosition().north(16)) &&
            this.getLevel().isLoaded(this.blockPosition().south(16)) &&
            this.getLevel().isLoaded(this.blockPosition().east(16)) &&
            this.getLevel().isLoaded(this.blockPosition().west(16));
    }



    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HOME_POS, this.blockPosition());
        this.entityData.define(ROOM_ID, "");
        this.entityData.define(FIRST_SURFACE_POS, BlockPos.ZERO);
        this.entityData.define(FOOD_LOCATION, BlockPos.ZERO);
        this.entityData.define(IS_ABOVEGROUND, false);
        this.entityData.define(IS_IN_TRANSITION, false);
        this.entityData.define(IS_MINING_ANIMATION, false);
        this.entityData.define(IS_COMPETENT, false);
        this.entityData.define(HUNGER_LEVEL, 70000);
        this.entityData.define(COLONY_ID, (int) this.level.getGameTime());
        this.entityData.define(MISC_DATA, 1000000);
        this.entityData.define(WORKING_STAGE, 0);
    }

    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("HomePosX", this.getHomePos().getX());
        pCompound.putInt("HomePosY", this.getHomePos().getY());
        pCompound.putInt("HomePosZ", this.getHomePos().getZ());
        pCompound.putString("RoomID", this.getRoomID());
        pCompound.putBoolean("IsAboveground", this.getIsAboveground());
        pCompound.putBoolean("IsInTransition", this.getIsInTransition());
        pCompound.putBoolean("IsInTransition2", this.getIsSnippingAnimation());
        pCompound.putBoolean("IsCompetent", this.getIsCompetent());
        pCompound.putInt("FirstSurfaceX", this.getFirstSurfacePos().getX());
        pCompound.putInt("FirstSurfaceY", this.getFirstSurfacePos().getY());
        pCompound.putInt("FirstSurfaceZ", this.getFirstSurfacePos().getZ());
        pCompound.putInt("FoodPosX", this.getFoodLocation().getX());
        pCompound.putInt("FoodPosY", this.getFoodLocation().getY());
        pCompound.putInt("FoodPosZ", this.getFoodLocation().getZ());
        pCompound.putInt("ColonyID", this.getColonyID());
        pCompound.putInt("HungerLevel", this.getHunger());
        pCompound.putInt("MiscData", this.getThisMiscRAW());
        pCompound.putInt("WorkingStage", this.getWorkingStage());
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readAdditionalSaveData(CompoundTag pCompound) {
        this.maxUpStep = 1.13F;
        int i = pCompound.getInt("HomePosX");
        int j = pCompound.getInt("HomePosY");
        int k = pCompound.getInt("HomePosZ");
        this.setHomePos(new BlockPos(i, j, k));
        this.setRoomID(pCompound.getString("RoomID"));
        super.readAdditionalSaveData(pCompound);
        this.setIsAboveground(pCompound.getBoolean("IsAboveground"));
        this.setIsInTransition(pCompound.getBoolean("IsInTransition"));
        this.setSnippingAnimation(pCompound.getBoolean("IsInTransition2"));
        this.setIsCompetent(pCompound.getBoolean("IsCompetent"));
        int l = pCompound.getInt("FirstSurfaceX");
        int i1 = pCompound.getInt("FirstSurfaceY");
        int j1 = pCompound.getInt("FirstSurfaceZ");
        this.setFirstSurfacePos(new BlockPos(l, i1, j1));
        int w = pCompound.getInt("FoodPosX");
        int m = pCompound.getInt("FoodPosY");
        int n = pCompound.getInt("FoodPosZ");
        this.setFoodLocation(new BlockPos(w, m, n));
        this.setColonyID(pCompound.getInt("ColonyID"));
        this.setHunger(pCompound.getInt("HungerLevel"));
        this.setThisMiscRAW(pCompound.getInt("MiscData"));
        this.setWorkingStage(pCompound.getInt("WorkingStage"));
    }


    protected Ant(EntityType<? extends Ant> entitytype, Level pLevel) {
        super(entitytype, pLevel);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 16.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
        if(!pLevel.isClientSide){
            this.memory = new LeafCutterMemory(this);
        }

    }

    protected void customServerAiStep() {

    }


    public void aiStep() {
        super.aiStep();
        //sets first aboveground position and moderates the aboveground status
        if (this.isAlive()) {
            boolean flag = this.level.canSeeSky(this.blockPosition());
            if (flag) {
                if (this.getFirstSurfacePos()==BlockPos.ZERO) {
                    this.setFirstSurfacePos(this.getOnPos());
                    this.setIsAboveground(true);
                }
                setIsAboveground(true);
            }
        }
        //ticks down hunger if players are nearby
        if(this.level.getNearestPlayer(this,100)!=null){
            this.setHunger(this.getHunger()-1);
        }
    }


    public boolean hurt(DamageSource pSource, float pAmount) {
        if (this.isInvulnerableTo(pSource)) {
            return false;
        } else {
            return super.hurt(pSource, pAmount);
        }
    }

    public float getWalkTargetValue(BlockPos pPos, LevelReader pLevel) {
        return pLevel.getBlockState(pPos).is(ModBlocks.ANT_AIR.get()) ? 10.0F : pLevel.getBrightness(pPos) - 0.5F;
    }



    /**
     * Returns the Y Offset of this entity.
     */
    public double getMyRidingOffset() {
        return 0.14D;
    }


    /**
     * Static predicate for determining whether or not an animal can spawn at the provided location.
     * @param pAnimal The animal entity to be spawned
     */
    public static boolean checkAnimalSpawnRules(EntityType<? extends Animal> pAnimal, LevelAccessor pLevel, MobSpawnType pReason, BlockPos pPos, Random pRandom) {
        return pLevel.getBlockState(pPos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON) && isBrightEnoughToSpawn(pLevel, pPos);
    }

    protected static boolean isBrightEnoughToSpawn(BlockAndTintGetter p_186210_, BlockPos p_186211_) {
        return p_186210_.getRawBrightness(p_186211_, 0) > 8;
    }

    /**
     * Get number of ticks, at least during which the living entity will be silent.
     */
    public int getAmbientSoundInterval() {
        return 120;
    }

    public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
        return false;
    }

    /**
     * Get the experience points the entity currently has.
     */
    protected int getExperienceReward(Player pPlayer) {
        return 1 + this.level.random.nextInt(3);
    }

    /**
     * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
     * the animal type)
     */
    public boolean isFood(ItemStack pStack) {return pStack.is(ModItems.ANT_FOOD.get());}

    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        Level pLevel = pPlayer.getLevel();
        if(!pLevel.isClientSide()){
            //NetworkHooks.openGui(((ServerPlayer) pPlayer), (MenuProvider) this, this.blockPosition());
        }
        return super.mobInteract(pPlayer, pHand);
    }

    protected void usePlayerItem(Player p_148715_, InteractionHand p_148716_, ItemStack p_148717_) {
        if (!p_148715_.getAbilities().instabuild) {
            p_148717_.shrink(1);
        }

    }


    /**
     * Handler for @link World#setEntityState
     */
    public void handleEntityEvent(byte pId) {
        if (pId == 18) {
            for(int i = 0; i < 7; ++i) {
                double d0 = this.random.nextGaussian() * 0.02D;
                double d1 = this.random.nextGaussian() * 0.02D;
                double d2 = this.random.nextGaussian() * 0.02D;
                this.level.addParticle(ParticleTypes.HEART, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
            }
        } else {
            super.handleEntityEvent(pId);
        }

    }

    @Override
    public void push(@NotNull Entity pEntity){
        boolean shouldCancel = false;
        if(pEntity instanceof Ant){
            boolean heldItem = !((Ant) pEntity).getMainHandItem().isEmpty();
            if (((Ant) pEntity).getColonyID() == this.getColonyID() && (!((Ant) pEntity).getMainHandItem().isEmpty()) || !this.getMainHandItem().isEmpty()){
                shouldCancel = true;
            }
        }

        if(this.getTarget() == pEntity){shouldCancel = true;}

        if (!this.isPassengerOfSameVehicle(pEntity) && !shouldCancel) {
            if (!pEntity.noPhysics && !this.noPhysics) {
                double d0 = pEntity.getX() - this.getX();
                double d1 = pEntity.getZ() - this.getZ();
                double d2 = Mth.absMax(d0, d1);
                if (d2 >= (double)0.01F) {
                    d2 = Math.sqrt(d2);
                    d0 /= d2;
                    d1 /= d2;
                    double d3 = 1.0D / d2;
                    if (d3 > 1.0D) {d3 = 1.0D;}
                    d0 *= d3;
                    d1 *= d3;
                    d0 *= (double)0.05F;
                    d1 *= (double)0.05F;
                    if (!this.isVehicle()) {this.push(-d0, 0.0D, -d1);}
                    if (!pEntity.isVehicle()) {pEntity.push(d0, 0.0D, d1);}
                }
            }
        }
    }



    public boolean canReach(BlockPos targetPos) {
        Ant pAnt = this;
        if(AntUtils.getDist(this.blockPosition(), targetPos) < 20) {
            Path tempPath = this.getNavigation().createPath(targetPos, 1);
            if (tempPath != null) {
                net.minecraft.world.level.pathfinder.Node finalPathPoint = tempPath.getEndNode();
                if (finalPathPoint != null) {
                    BlockPos pathEndPos = new BlockPos(finalPathPoint.x, finalPathPoint.y, finalPathPoint.z);
                    return AntUtils.getDist(targetPos, pathEndPos) < 1.2f;
                }
                else return false;
            }
        }
        else{
            return new PosPair(this.blockPosition(), targetPos, this.level).canConnectWithFloor(150);
        }
        return false;
    }

}


