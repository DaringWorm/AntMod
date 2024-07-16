package com.daringworm.antmod.entity;


import com.daringworm.antmod.DebugHelper;
import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.colony.AntColony;
import com.daringworm.antmod.colony.misc.BlockPosStringifier;
import com.daringworm.antmod.colony.misc.PosSpherePair;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

import static com.daringworm.antmod.goals.AntUtils.getDist;


public abstract class Ant extends PathfinderMob implements MenuProvider {

    @Nullable
    private Entity passiveTarget;

    private final ArrayList<BlockPos> cookedExcavationPosList;
    private final ArrayList<BlockPos> goUndergroundList;
    private final ArrayList<PosSpherePair> rawExcavationList;


    private static final EntityDataAccessor<Boolean> IS_MINING_ANIMATION = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SHOULD_RUN_AI = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.BOOLEAN);


    private static final EntityDataAccessor<String> ERROR_MSG = SynchedEntityData.defineId(Ant.class,EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ROOM_ID = SynchedEntityData.defineId(Ant.class,EntityDataSerializers.STRING);


    /**1 place is workingstage; 10 place is subclass; 100s place is for hascheckedhomepos; 1000s is latch direction for workerants**/
    private static final EntityDataAccessor<Integer> MISC_DATA = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> COLONY_ID = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WALKING_COOLDOWN = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BLOCK_BREAKING_PROGRESS = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HUNGER_LEVEL = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> NEARBY_ITEM_COUNT = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WORKING_STAGE = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BRAINCELL_STAGE = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EXCAVATION_STAGE = SynchedEntityData.defineId(Ant.class,EntityDataSerializers.INT);


    private static final EntityDataAccessor<BlockPos> HOME_POS = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<BlockPos> INTEREST_POS = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<BlockPos> SURFACE_POS = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<BlockPos> FOOD_POS = SynchedEntityData.defineId(Ant.class, EntityDataSerializers.BLOCK_POS);


    public void setHomeContainerPos(BlockPos pPos) {this.entityData.set(HOME_POS, pPos);}
    public BlockPos getHomeContainerPos() {return this.entityData.get(HOME_POS);}
    public void setFirstSurfacePos(BlockPos pPos) {this.entityData.set(SURFACE_POS, pPos);}
    public BlockPos getSurfacePos(){return this.entityData.get(SURFACE_POS);}
    public void setInterestPos(BlockPos pPos) {this.entityData.set(INTEREST_POS, pPos);}
    public BlockPos getInterestPos(){return this.entityData.get(INTEREST_POS);}
    public void setFoodLocation(BlockPos pPos) {this.entityData.set(FOOD_POS,pPos);}
    public BlockPos getFoodLocation(){return this.entityData.get(FOOD_POS);}
    public void setHunger(int pHunger) {this.entityData.set(HUNGER_LEVEL,pHunger);}
    public int getHunger(){return this.entityData.get(HUNGER_LEVEL);}
    public void setColonyID(int pID) {this.entityData.set(COLONY_ID,pID);}
    public int getColonyID(){return this.entityData.get(COLONY_ID);}
    public void setWorkingStage(int pStage) {this.entityData.set(WORKING_STAGE, pStage);}
    public int getWorkingStage(){return this.entityData.get(WORKING_STAGE);}
    public void setBreakingProgress(int pTicks) {this.entityData.set(BLOCK_BREAKING_PROGRESS, pTicks);}
    public int getBreakingProgress(){return this.entityData.get(BLOCK_BREAKING_PROGRESS);}
    public void setErrorMessage(String msg){this.entityData.set(ERROR_MSG,msg);}
    public String getErrorMessage(){return this.entityData.get(ERROR_MSG);}
    public void setBraincellStage(int pStage){this.entityData.set(BRAINCELL_STAGE,pStage);}
    public int getBraincellStage(){return this.entityData.get(BRAINCELL_STAGE);}
    public void setSnippingAnimation(boolean pBool) {this.entityData.set(IS_MINING_ANIMATION, pBool);}
    public boolean getIsSnippingAnimation(){return this.entityData.get(IS_MINING_ANIMATION);}
    public void setNearbyItemCount(int pCount){this.entityData.set(NEARBY_ITEM_COUNT,pCount);}
    public int getNearbyItemCount(){return this.entityData.get(NEARBY_ITEM_COUNT);}
    public void setShouldRunBrain(boolean pBool){this.entityData.set(SHOULD_RUN_AI,pBool);}
    public boolean getShouldRunBrain(){return this.entityData.get(SHOULD_RUN_AI);}
    public void setWalkingCooldown(int pStage) {this.entityData.set(WALKING_COOLDOWN, pStage);}
    public int getWalkingCooldown(){return this.entityData.get(WALKING_COOLDOWN);}
    public void setExcavationStage(int pStage){this.entityData.set(EXCAVATION_STAGE, pStage);}
    public int getExcavationStage(){return this.entityData.get(EXCAVATION_STAGE);}
    public void setRoomID(String id){this.entityData.set(ROOM_ID,id);}
    public String getRoomID(){return this.entityData.get(ROOM_ID);}

    public void setPassiveTarget(Entity pEntity){passiveTarget = pEntity;}
    public Entity getPassiveTarget(){return passiveTarget;}
    public void setThisMiscRAW(int pMisc) {this.entityData.set(MISC_DATA, pMisc);}


    public ArrayList<BlockPos> getCookedExcavationPosList(){
        if(this.cookedExcavationPosList != null && !this.cookedExcavationPosList.isEmpty()){
            return this.cookedExcavationPosList;
        }
        else if(!this.getLevel().isClientSide()){
            AntColony pColony = ((ServerLevelUtil) (this.getLevel())).getColonyWithID(this.getColonyID());
            if (pColony != null) {
                this.setExcavationStage(this.getExcavationStage()+ this.rawExcavationList.size());
                this.rawExcavationList.clear();
                this.rawExcavationList.addAll(pColony.getNextExcavationSteps(this.getExcavationStage()));

                for (PosSpherePair sphere : rawExcavationList) {
                    cookedExcavationPosList.removeAll(sphere.getBlockPoses(this.getLevel()));
                    Collection<BlockPos> tempList = sphere.getBlockPoses(this.getLevel());
                    tempList.removeIf(pos -> this.getLevel().getBlockState(pos).isAir());
                    cookedExcavationPosList.addAll(tempList);
                }
            }
        }
        return cookedExcavationPosList;
    }

    public ArrayList<BlockPos> getGoUndergroundList(){
        AntColony pColony = ((ServerLevelUtil) (this.getLevel())).getColonyWithID(this.getColonyID());
        /*
        if(pColony.hasBeenUpdated){
            this.goUndergroundList.clear();
            this.goUndergroundList.addAll(pColony.tunnels.getPosesToBranch(this.getRoomID()));
        }
        */

        return pColony.tunnels.getPosesToBranch(this.getRoomID());//this.goUndergroundList;
    }

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

    public double getDistTo(BlockPos pPos){return getDist(this.blockPosition(),pPos);}

    public BlockPos getColonyPos(){
        if(((ServerLevelUtil) this.getLevel()).getColonyWithID(this.getColonyID()) != null){
            AntColony colony = getColony();
            if(colony != null) {
                return colony.getEntranceBottom();
            }
            else return BlockPos.ZERO;
        }
        else return BlockPos.ZERO;
    }

    public AntColony getColony(){
        return ((ServerLevelUtil) this.getLevel()).getColonyWithID(this.getColonyID());
    }



    public int findDigit(int input, int digit){
        int ten = (int) Math.pow(10,digit-1);
        int noBigger = (input%(ten*10)-(input%ten));
        return noBigger/ten;
    }

    public static Predicate<BlockPos> foodStatePredicate(Ant pAnt){
        return pPos -> {
            BlockState pState = pAnt.getLevel().getBlockState(pPos);
            return pState.getRenderShape()
                    != RenderShape.INVISIBLE && pState.getDestroySpeed(pAnt.level, pPos) < 0.1
                    && !pState.canOcclude() && pState.getBlock().getClass() != NetherPortalBlock.class;
        };
    }

    public void walkTo(BlockPos blockPos, double speedModifier, double distanceModifier){
        this.getLevel().getProfiler().push("ant_navigation");

        if(this.getWalkingCooldown() < 20 || blockPos == BlockPos.ZERO){
            //AntUtils.broadcastString(this.getLevel(), "Cancelled walking due to inadequate cooldown");
            return;
        }

        if(!this.isOnGround() && !this.isInWater()){return;}
        assert blockPos != null && this.getLevel().isLoaded(blockPos) && blockPos != this.getNavigation().getPath().getEndNode().asBlockPos();
        this.setWalkingCooldown(0);
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
                            break;
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
        this.setWalkingCooldown(0);
        this.getLevel().getProfiler().pop();
        DebugHelper.numberOfPathsRequested = DebugHelper.numberOfPathsRequested + 1;
    }

    public void walkAlongList(ArrayList<BlockPos> posList, int speedModifier, double farthestAllowed){
        if(this.getWalkingCooldown() < 20){return;}
        if(posList.isEmpty()) return;

        double MAX_ALLOWED_PATH_NODES = 4;

        if(AntUtils.getDist(this.blockPosition(),posList.get(posList.size()-1)) > farthestAllowed) {
            if (!this.getNavigation().isInProgress()) {
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
        this.setWalkingCooldown(0);
        DebugHelper.numberOfPathsRequested = DebugHelper.numberOfPathsRequested + 1;
    }
    
    public boolean shouldRunBrain() {
        return !this.getEntityData().isEmpty() &&
                this.getLevel().isLoaded(this.blockPosition()) &&
                this.getLevel().isLoaded(this.blockPosition().north(16)) &&
                this.getLevel().isLoaded(this.blockPosition().south(16)) &&
                this.getLevel().isLoaded(this.blockPosition().east(16)) &&
                this.getLevel().isLoaded(this.blockPosition().west(16))
                && this.getShouldRunBrain();
                //((ServerLevelUtil)(ServerLevel)this.getLevel()).getColonyWithID(this.getColonyID()) != null
                //&& ((ServerLevelUtil)(ServerLevel)this.getLevel()).getColonyWithID(this.getColonyID()).tunnels != null;
    }


    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_MINING_ANIMATION, false);
        this.entityData.define(MISC_DATA, 1000000);
        this.entityData.define(WALKING_COOLDOWN, -10);
        this.entityData.define(BRAINCELL_STAGE, 0);
        this.entityData.define(WORKING_STAGE,0);
        this.entityData.define(COLONY_ID,0);
        this.entityData.define(HUNGER_LEVEL,50000);
        this.entityData.define(ERROR_MSG,"An error occured (default msg)");
        this.entityData.define(NEARBY_ITEM_COUNT,0);
        this.entityData.define(BLOCK_BREAKING_PROGRESS,0);
        this.entityData.define(SHOULD_RUN_AI, true);
        this.entityData.define(EXCAVATION_STAGE, 0);
        this.entityData.define(ROOM_ID, "0");

        this.entityData.define(HOME_POS, BlockPos.ZERO);
        this.entityData.define(INTEREST_POS, BlockPos.ZERO);
        this.entityData.define(SURFACE_POS, BlockPos.ZERO);
        this.entityData.define(FOOD_POS, BlockPos.ZERO);
    }

    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("MiscData", this.getThisMiscRAW());
        pCompound.putInt("WalkingCooldown", this.getWalkingCooldown());
        pCompound.putInt("Braincell_stage", this.getBraincellStage());
        pCompound.putInt("Working_stage", this.getWorkingStage());
        pCompound.putInt("Colony_ID", this.getColonyID());
        pCompound.putString("Room_ID", this.getRoomID());
        pCompound.putInt("Hunger_level", this.getHunger());
        pCompound.putString("Error_Alert_String", this.getErrorMessage());
        pCompound.putInt("Nearby_item_count", this.getNearbyItemCount());
        pCompound.putInt("Block_breaking_progress", this.getBreakingProgress());
        pCompound.putBoolean("Should_run_AI", this.getShouldRunBrain());

        pCompound.put("Interest_pos", BlockPosStringifier.getTagForPos(this.getInterestPos()));
        pCompound.put("Home_pos", BlockPosStringifier.getTagForPos(this.getHomeContainerPos()));
        pCompound.put("Surface_pos", BlockPosStringifier.getTagForPos(this.getSurfacePos()));
        pCompound.put("Food_pos", BlockPosStringifier.getTagForPos(this.getFoodLocation()));
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readAdditionalSaveData(CompoundTag pCompound) {
        this.maxUpStep = 1.13F;
        super.readAdditionalSaveData(pCompound);
        this.setThisMiscRAW(pCompound.getInt("MiscData"));
        this.setWalkingCooldown(pCompound.getInt("WalkingCooldown"));
        this.setBraincellStage(pCompound.getInt("Braincell_stage"));
        this.setWorkingStage(pCompound.getInt("Working_stage"));
        this.setColonyID(pCompound.getInt("Colony_ID"));
        this.setRoomID(pCompound.getString("Room_ID"));
        this.setHunger(pCompound.getInt("Hunger_level"));
        this.setErrorMessage(pCompound.getString("Error_Alert_String"));
        this.setNearbyItemCount(pCompound.getInt("Nearby_item_count"));
        this.setBreakingProgress(pCompound.getInt("Block_breaking_progress"));
        this.setShouldRunBrain(pCompound.getBoolean("Should_run_AI"));


        this.setInterestPos(BlockPosStringifier.getPosForTag((CompoundTag) pCompound.get("Interest_pos")));
        this.setHomeContainerPos(BlockPosStringifier.getPosForTag((CompoundTag) pCompound.get("Home_pos")));
        this.setFirstSurfacePos(BlockPosStringifier.getPosForTag((CompoundTag) pCompound.get("Surface_pos")));
        this.setFoodLocation(BlockPosStringifier.getPosForTag((CompoundTag) pCompound.get("Food_pos")));
    }



    protected Ant(EntityType<? extends Ant> entitytype, Level pLevel) {
        super(entitytype, pLevel);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 16.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
        this.cookedExcavationPosList = new ArrayList<>();
        this.rawExcavationList = new ArrayList<>();
        this.goUndergroundList = new ArrayList<>();
    }

    protected void customServerAiStep() {

    }


    public void aiStep() {
        super.aiStep();
        this.getLevel().getProfiler().push("ant_ai");

        //ticks down hunger if players are nearby
        if(this.level.getNearestPlayer(this,100)!=null){
            this.setHunger(this.getHunger()-1);
        }
        this.setWalkingCooldown(this.getWalkingCooldown()+1);
        this.getLevel().getProfiler().pop();
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

    @Override
    public InteractionResult interactAt(Player pPlayer, Vec3 pVec, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        Level pLevel = pPlayer.getLevel();
        if(!pLevel.isClientSide()){
            if(pPlayer.getMainHandItem().getItem() == Items.BONE){
                this.setShouldRunBrain(!this.getShouldRunBrain());
            }
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


