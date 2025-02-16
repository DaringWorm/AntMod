package com.daringworm.antmod.entity.brains.parts;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.block.entity.custom.FungalContainerBlockEntity;
import com.daringworm.antmod.colony.AntColony;
import com.daringworm.antmod.colony.misc.BlockPosStringifier;
import com.daringworm.antmod.colony.misc.ColonyBranch;
import com.daringworm.antmod.entity.Ant;
import com.daringworm.antmod.entity.ModEntityTypes;
import com.daringworm.antmod.entity.custom.AntScentCloud;
import com.daringworm.antmod.entity.custom.WorkerAnt;
import com.daringworm.antmod.util.AntUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Actions {
    public static final Action ATTACK_HOSTILE_TARGET = new Action(){
        @Override
        public void run(Ant pAnt) {
            LivingEntity target = pAnt.getTarget();
            if (target != null && target.isAlive() && pAnt.canAttack(target)) {
                if (pAnt.distanceToSqr(target) <= 2.5f && pAnt.hasLineOfSight(target)) {
                    target.hurt(DamageSource.mobAttack(pAnt), (float) pAnt.getAttribute(Attributes.ATTACK_DAMAGE).getValue());
                }
            }
            
        }
    };
    public static final Action ATTACK_PASSIVE_TARGET = new Action(){
        @Override
        public void run(Ant pAnt) {
            if(!(pAnt.getPassiveTarget() instanceof LivingEntity)){return;}
            LivingEntity target = (LivingEntity) pAnt.getPassiveTarget();
            if (target != null && target.isAlive() && pAnt.canAttack(target)) {
                target.hurt(DamageSource.mobAttack(pAnt), (float) pAnt.getAttribute(Attributes.ATTACK_DAMAGE).getValue());
            }
        }
    };
    public static final Action WALK_TO_HOSTILE_TARGET = new Action(){
        @Override
        public void run(Ant pAnt) {

            Entity target = pAnt.getTarget();
            if(target != null && target.isAlive() && target.blockPosition() != BlockPos.ZERO) {
                pAnt.getNavigation().moveTo(target, 1);
            }
            
        }
    };
    public static final Action WALK_TO_PASSIVE_TARGET = new Action(){
        @Override
        public void run(Ant pAnt) {
            Entity target = pAnt.getPassiveTarget();
            if(target != null && target.isAlive() && target.blockPosition() != BlockPos.ZERO) {
                pAnt.getNavigation().moveTo(target, 1);
            }
        }
    };
    public static final Action WANDER = new Action(){
        @Override
        public void run(Ant pAnt) {
            if(!pAnt.getNavigation().isInProgress()) {
                AntUtils.wanderRandomly(pAnt);
            }
        }
    };
    public static final Action SPAWN_CLOUD_FOR_CURRENT_WS = new Action(){
        @Override
        public void run(Ant pAnt) {
            if(pAnt.isAlive() && pAnt.getLevel().getEntitiesOfClass(AntScentCloud.class, pAnt.getBoundingBox().inflate(8d)).isEmpty()){
                AntScentCloud cloud = new AntScentCloud(ModEntityTypes.ANT_EFFECT_CLOUD.get(), pAnt.getLevel());
                cloud.WORKING_STAGE = pAnt.getWorkingStage();
                cloud.COLONY_ID = pAnt.getColonyID();
                cloud.moveTo(pAnt.position());

                pAnt.getLevel().addFreshEntity(cloud);
            }
        }
    };
    public static final Action SET_AGGRESSOR_AS_TARGET = new Action(){
        @Override
        public void run(Ant pAnt) {

            LivingEntity target = pAnt.getLastHurtByMob();
            if(target != null && target.isAlive() && pAnt.canAttack(target)) {
                pAnt.setTarget(target);
            }
            pAnt.setLastHurtByMob(null);
            
        }
    };
    public static final Action ERROR_MSG_ACTION = new Action(){
        @Override
        public void run(Ant pAnt) {

            if(pAnt.getLevel() instanceof ServerLevel){
                AntUtils.broadcastString(pAnt.getLevel(), pAnt.getErrorMessage());
            }
            
        }
    };
    public static final Action WALK_TO_BLOCK = new Action(){
        @Override
        public void run(Ant pAnt) {

            BlockPos pPos = pAnt.getInterestPos();
            AntColony pColony = pAnt.getColony();

            if(pPos != BlockPos.ZERO/* && (pAnt.getNavigation().isDone() || pAnt.getNavigation().isStuck())*/) {
                if(pAnt.getWorkingStage() == WorkingStages.FARMING && !AntPredicates.IN_RANGE_OF_INTEREST_BLOCK.test(pAnt) && pColony != null){
                    ColonyBranch tunnels = pColony.tunnels;
                    ArrayList<BlockPos> walkList = tunnels.getPosesFromBranchToBranch(tunnels.getNearestBranchID(pAnt.blockPosition()), tunnels.getNearestBranchID(pPos));
                    walkList.add(pPos);

                    pAnt.walkAlongList(walkList, 1, 5d);
                }
                else {
                    pAnt.walkTo(pPos, 1, 2d);
                }
            }
            
        }
    };
    public static final Action FIND_OR_PLACE_NEW_CONTAINER = new Action(){
        @Override
        public void run(Ant pAnt){
            Level pLevel = pAnt.getLevel();

            BlockPos newPos = BlockPos.findClosestMatch(pAnt.getHomeContainerPos(), 8, 3, p -> pLevel.getBlockState(p).getBlock() == ModBlocks.LEAFY_CONTAINER_BLOCK.get()).orElse(pAnt.getHomeContainerPos());

            if(pLevel.getBlockState(newPos).getBlock() == ModBlocks.LEAFY_CONTAINER_BLOCK.get()) {
                pAnt.setHomeContainerPos(newPos);
                return;
            }
            else{
                pAnt.setHomeContainerPos(BlockPos.findClosestMatch(
                        pAnt.getHomeContainerPos(), 8, 3, p ->
                                pLevel.getBlockState(p).isAir() &&
                                pLevel.getBlockState(p.below()).isFaceSturdy(pLevel, p.below(), Direction.UP)
                    ).orElse(pAnt.getHomeContainerPos()));
            }

            if(pLevel.getBlockState(pAnt.getHomeContainerPos()).isAir() && pLevel.getBlockState(pAnt.getHomeContainerPos().below()).isFaceSturdy(pLevel,pAnt.getHomeContainerPos().below(),Direction.UP)){
                pLevel.setBlock(pAnt.getHomeContainerPos(), ModBlocks.LEAFY_CONTAINER_BLOCK.get().defaultBlockState(),2);
            }
        }
    };
    public static final Action WALK_TO_HOMEPOS = new Action(){
        @Override
        public void run(Ant pAnt) {
            if(pAnt.getHomeContainerPos() != BlockPos.ZERO){
                pAnt.walkTo(pAnt.getHomeContainerPos(), 1, 2f);
            }
        }
    };
    public static final Action BREAK_INTEREST_BLOCK = new Action(){
        @Override
        public void run(Ant pAnt) {
            BlockPos pPos = pAnt.getInterestPos();
            if(pAnt.getLevel().getBlockState(pPos).getRenderShape() == RenderShape.INVISIBLE){
                pAnt.setInterestPos(BlockPos.ZERO);
                pAnt.setSnippingAnimation(false);
            }

            pAnt.setBreakingProgress(pAnt.getBreakingProgress()+1);

            float blockToughness = Math.max(pAnt.level.getBlockState(pPos).getDestroySpeed(pAnt.level, pPos)*24, 48);
            int destroyProgress = (int)((pAnt.getBreakingProgress()/blockToughness)*10);

            if(pPos != BlockPos.ZERO && pAnt.getDistTo(pPos) < 3d) {
                if (blockToughness < pAnt.getBreakingProgress()) {
                    BlockState pState = pAnt.getLevel().getBlockState(pPos);
                    ArrayList<ItemStack> drops = new ArrayList<>(Block.getDrops(pState, (ServerLevel) pAnt.getLevel(),pPos,pAnt.getLevel().getBlockEntity(pPos)));

                    if(pState.getBlock() instanceof CropBlock){
                        if(((CropBlock)pState.getBlock()).getMaxAge() == pState.getValue(((CropBlock) pState.getBlock()).getAgeProperty())) {
                            pAnt.getLevel().destroyBlock(pPos, false);
                            if(!drops.isEmpty()) {
                                pAnt.setItemInHand(InteractionHand.MAIN_HAND, drops.get(0));
                                drops.remove(0);
                                if(!drops.isEmpty()){
                                    for(ItemStack stack : drops){
                                        ItemEntity entity = new ItemEntity(pAnt.getLevel(),pAnt.getX(),pAnt.getY(),pAnt.getZ(),stack);
                                        pAnt.getLevel().addFreshEntity(entity);
                                    }
                                }
                            }
                            pAnt.getCookedExcavationPosList().remove(pAnt.getInterestPos());
                            pAnt.setInterestPos(BlockPos.ZERO);
                            pAnt.setBreakingProgress(0);
                            pAnt.setSnippingAnimation(false);
                            pAnt.getLevel().setBlock(pPos, pState.setValue(((CropBlock) pState.getBlock()).getAgeProperty(), 0), 2);
                        }
                        else{
                            pAnt.getCookedExcavationPosList().remove(pAnt.getInterestPos());
                            pAnt.setInterestPos(BlockPos.ZERO);
                            pAnt.setSnippingAnimation(false);
                        }
                    }
                    else {
                        pAnt.getLevel().destroyBlock(pPos, false);
                        pAnt.getCookedExcavationPosList().remove(pAnt.getInterestPos());
                        pAnt.setInterestPos(BlockPos.ZERO);
                        pAnt.setBreakingProgress(0);
                        pAnt.setSnippingAnimation(false);
                        if(pAnt.getLevel().canSeeSky(pPos) && pAnt.getDistTo(pAnt.getSurfacePos())>45 && pAnt.getLevel().getRandom().nextBoolean()) {
                            pAnt.getLevel().setBlock(pPos, ModBlocks.FERTILE_AIR.get().defaultBlockState(), 2);
                        }
                    }

                    if(!drops.isEmpty()) {
                        pAnt.setItemInHand(InteractionHand.MAIN_HAND, drops.get(0));
                        drops.remove(0);
                        if(!drops.isEmpty()){
                            for(ItemStack stack : drops){
                                ItemEntity entity = new ItemEntity(pAnt.getLevel(),pAnt.getX(),pAnt.getY(),pAnt.getZ(),stack);
                                pAnt.getLevel().addFreshEntity(entity);
                            }
                        }
                    }
                }
                else {
                    pAnt.getLevel().destroyBlockProgress(pAnt.getId(),pPos,destroyProgress);
                    pAnt.setSnippingAnimation(true);
                }
            }
            else{
                pAnt.setBreakingProgress(0);
            }
        }
    };
    public static final Action PLACE_HELD_BLOCK_AT_INTEREST = new Action(){
        @Override
        public void run(Ant pAnt) {
            BlockPos pPos = pAnt.getInterestPos();
            ServerLevel pLevel = (ServerLevel) pAnt.getLevel();
            ItemStack handStack = pAnt.getMainHandItem();

            if(pPos != BlockPos.ZERO && handStack.getItem() instanceof BlockItem){
                if(!pLevel.getBlockState(pPos).isAir()){
                    pPos = BlockPos.findClosestMatch(pPos, 5,2, p -> pLevel.getBlockState(p).isAir()).orElse(BlockPos.ZERO);
                }

                if(pPos != BlockPos.ZERO) {
                    pLevel.setBlock(pPos, ((BlockItem) handStack.getItem()).getBlock().defaultBlockState(), 2);

                    handStack.shrink(1);
                    pAnt.setItemInHand(InteractionHand.MAIN_HAND, handStack);
                }
            }
        }
    };
    public static final Action FIND_INTEREST_BLOCK = new Action(){
        @Override
        public void run(Ant pAnt) {
            pAnt.setInterestPos(BlockPos.findClosestMatch(pAnt.blockPosition(),8,4, Ant.foodStatePredicate(pAnt)).orElse(BlockPos.ZERO));
        }
    };
    public static final Action SELECT_ITEM_TO_TARGET = new Action(){
        @Override
        public void run(Ant pAnt) {

            if(pAnt.getNearbyItemCount() > 0){
                ArrayList<ItemEntity> itemEntities = (ArrayList<ItemEntity>) pAnt.getLevel().getEntitiesOfClass(ItemEntity.class,pAnt.getBoundingBox().inflate(8,8,8));
                if(itemEntities.size() > 0) {
                    pAnt.setPassiveTarget(itemEntities.get(0));
                }
            }
            
        }
    };
    public static final Action PICKUP_ITEM = new Action(){
        @Override
        public void run(Ant pAnt) {

            if(pAnt.getPassiveTarget() == null || !pAnt.getPassiveTarget().isAlive()){
                ArrayList<ItemEntity> itemList = (ArrayList<ItemEntity>) pAnt.getLevel().getEntitiesOfClass(ItemEntity.class,pAnt.getBoundingBox().inflate(6d));
                if(!itemList.isEmpty()) {
                    pAnt.setPassiveTarget(itemList.get(0));
                }
            }

            if(pAnt.getPassiveTarget() != null && pAnt.getPassiveTarget().isAlive() && pAnt.getPassiveTarget() instanceof ItemEntity) {
                pAnt.setItemInHand(InteractionHand.MAIN_HAND, ((ItemEntity)pAnt.getPassiveTarget()).getItem());
                pAnt.getPassiveTarget().remove(Entity.RemovalReason.DISCARDED);
                pAnt.setPassiveTarget(null);
            }

            pAnt.getNavigation().stop();
            
        }
    };
    public static final Action DROP_ITEM = new Action(){
        @Override
        public void run(Ant pAnt) {
            ItemStack heldItem = pAnt.getMainHandItem();
            if(!heldItem.isEmpty()){
                pAnt.getLevel().addFreshEntity(new ItemEntity(pAnt.getLevel(),pAnt.getX(), pAnt.getY(), pAnt.getZ(),heldItem));
                pAnt.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }
        }
    };
    public static final Action SET_INTEREST_TO_FUNGUS_POS = new Action(){
        @Override
        public void run(Ant pAnt) {
            pAnt.setInterestPos(pAnt.getFungusLocation());
        }
    };
    public static final Action SET_CONTAINER_TO_INTEREST = new Action(){
        @Override
        public void run(Ant pAnt) {

            if(pAnt.getHomeContainerPos() != BlockPos.ZERO){
                pAnt.setInterestPos(pAnt.getHomeContainerPos());
            }
        }
    };
    public static final Action PLACE_ITEM_IN_CONTAINER = new Action(){
        @Override
        public void run(Ant pAnt) {

            BlockState pState = pAnt.getLevel().getBlockState(pAnt.getHomeContainerPos());
            if(pState.getBlock() == ModBlocks.LEAFY_CONTAINER_BLOCK.get()){
                FungalContainerBlockEntity containerEntity = (FungalContainerBlockEntity) pAnt.getLevel().getBlockEntity(pAnt.getHomeContainerPos());
                if(containerEntity != null){
                    containerEntity.takeInHandItem(pAnt);
                    pAnt.setInterestPos(BlockPos.ZERO);
                    pAnt.setWorkingStage(WorkingStages.SCOUTING);
                }
            }
        }
    };
    public static final Action FIND_CONTAINER_POS = new Action(){
        @Override
        public void run(Ant pAnt) {

            //ArrayList<BlockPos> containerPoses = new ArrayList<>(mem.containerPosSet);
            BlockPos containerPos = pAnt.getHomeContainerPos();

            if(containerPos != null && containerPos != BlockPos.ZERO && pAnt.getLevel().getBlockState(containerPos).getBlock() == ModBlocks.LEAFY_CONTAINER_BLOCK.get()) {
                pAnt.setInterestPos(containerPos);
            }

            /*if(!containerPoses.isEmpty()){
                boolean found1 = false;
                for(BlockPos tempPos : containerPoses){
                    if(!found1) {
                        BlockState tempState = pAnt.getLevel().getBlockState(tempPos);
                        if (tempState.getBlock() == ModBlocks.LEAFY_CONTAINER_BLOCK.get()) {
                            FungalContainerBlockEntity containerEntity = (FungalContainerBlockEntity) pAnt.getLevel().getBlockEntity(tempPos);
                            if (containerEntity != null && containerEntity.canAcceptHandItem(pAnt)) {
                                pAnt.memory().containerPos = tempPos;
                                pAnt.memory().interestPos = tempPos;
                                found1 = true;
                            }
                        }
                    }
                }
            }*/
        }
    };
    public static final Action SET_EXCAVATION_POS_TO_INTEREST = new Action(){
        @Override
        public void run(Ant pAnt) {


            if(!pAnt.getCookedExcavationPosList().isEmpty() && (pAnt.getInterestPos() == null || pAnt.getInterestPos() == BlockPos.ZERO || pAnt.getLevel().getBlockState(pAnt.getInterestPos()).isAir())){
                pAnt.setInterestPos(AntUtils.findNearestBlockPos(pAnt,pAnt.getCookedExcavationPosList()));
                pAnt.setBreakingProgress(0);

                if(AntUtils.isColonyBlock(pAnt.getLevel().getBlockState(pAnt.getInterestPos()))){
                    for(int i = pAnt.getCookedExcavationPosList().size()-1; i >=0; i--){
                        BlockPos tempPos = pAnt.getCookedExcavationPosList().get(i);
                        BlockState tempState = pAnt.getLevel().getBlockState(tempPos);
                        if(AntUtils.isColonyBlock(tempState) || (tempState.getBlock() == Blocks.AIR && pAnt.getLevel().canSeeSky(tempPos))){
                            pAnt.getCookedExcavationPosList().remove(i);
                        }
                    }
                }
                else if(!pAnt.getCookedExcavationPosList().isEmpty()) {
                    pAnt.setInterestPos(AntUtils.findNearestBlockPos(pAnt, pAnt.getCookedExcavationPosList()));
                }
            }
        }
    };
    public static final Action EXCAVATE_INTEREST_POS = new Action(){
        @Override
        public void run(Ant pAnt) {

            BlockPos pPos = pAnt.getInterestPos();
            pAnt.lookAt(new ItemEntity(pAnt.getLevel(),pPos.getX(),pPos.getY(), pPos.getZ(), ItemStack.EMPTY), 0.5f, 0.5f);
            int breakingProgress = pAnt.getBreakingProgress();
            float blockToughness = Math.max(16, (pAnt.level.getBlockState(pPos).getDestroySpeed(pAnt.level, pPos)*32));
            int destroyProgress = (int)((breakingProgress/blockToughness)*10);

            if(pPos != BlockPos.ZERO) {
                if (blockToughness <= breakingProgress) {
                    pAnt.getLevel().destroyBlock(pPos, false, pAnt);

                    for(Direction dir : Direction.values()){
                        BlockPos tempPos = pAnt.getInterestPos().relative(dir,1);
                        BlockState tempState = pAnt.getLevel().getBlockState(tempPos);
                        if(!pAnt.getCookedExcavationPosList().contains(tempPos) && !AntUtils.isColonyBlock(tempState) && !pAnt.getLevel().canSeeSky(tempPos)){
                            pAnt.getLevel().setBlock(tempPos, ModBlocks.ANT_DIRT.get().defaultBlockState(),2);
                        }
                    }
                    pAnt.getCookedExcavationPosList().remove(pAnt.getInterestPos());
                    pAnt.level.setBlock(pAnt.getInterestPos(),ModBlocks.ANT_AIR.get().defaultBlockState(),2);
                    pAnt.setInterestPos(BlockPos.ZERO);
                    pAnt.setBreakingProgress(0);
                    pAnt.setSnippingAnimation(false);
                }
                else {
                    pAnt.setSnippingAnimation(true);
                    pAnt.setBreakingProgress(pAnt.getBreakingProgress()+1);
                    pAnt.getLevel().destroyBlockProgress(pAnt.getId(),pPos,destroyProgress);
                }

                if(pAnt.getLevel().getBlockState(pPos).isAir()){
                    pAnt.getLevel().setBlock(pPos,ModBlocks.ANT_AIR.get().defaultBlockState(), 2);
                }
            }
            else{
                pAnt.setBreakingProgress(0);
            }
            
        }
    };
    public static final Action LATCH_ON = new Action(){
        @Override
        public void run(Ant pAnt) {

            if(!(pAnt instanceof WorkerAnt)){}

            LivingEntity target = pAnt.getTarget();
            if(target != null && target.isAlive()){
                boolean loopTest = true;
                if(pAnt.getTarget() instanceof WorkerAnt){
                    List<WorkerAnt> tempList = pAnt.level.getEntitiesOfClass(WorkerAnt.class, pAnt.getBoundingBox().inflate(8d));
                    for(WorkerAnt tempAnt : tempList){
                        if(tempAnt.getTarget() == pAnt && tempAnt.getWorkingStage() == 3){
                            loopTest = false;
                        }
                    }
                }

                if(loopTest){
                    Vec3 changevec = ((WorkerAnt) pAnt).getLatchOffset();

                    pAnt.startRiding(pAnt.getTarget());
                    pAnt.moveTo(target.position().add(target.getDeltaMovement().add(new Vec3(0,target.getBbHeight()/2,0)).add(changevec)));
                    pAnt.setDeltaMovement(target.getDeltaMovement());
                    pAnt.getLookControl().setLookAt(target);
                    pAnt.resetFallDistance();
                    //target.hurt(DamageSource.GENERIC,4);
                    target.setLastHurtByMob(pAnt);
                    //target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 5), ant);
                    //target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0), ant);
                    target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, 0), pAnt);
                    //target.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, -5), ant);
                    pAnt.setWorkingStage(5);
                }
            }
            
        }
    };
    public static final Action SCOUT = new Action(){
        @Override
        public void run(Ant pAnt) {

            if(pAnt instanceof WorkerAnt){
                if (pAnt.getNavigation().isDone() || pAnt.getNavigation().isStuck()) {
                    BlockPos tempPos = BlockPos.findClosestMatch(pAnt.blockPosition(), 6, 4, p -> AntUtils.shouldSnip(p, pAnt.getLevel())).orElse(BlockPos.ZERO);
                    ArrayList<AntScentCloud> clouds = new ArrayList<>(pAnt.getLevel().getEntitiesOfClass(AntScentCloud.class, pAnt.getBoundingBox().inflate(18)));

                    if(!clouds.isEmpty() && pAnt.getDistTo(clouds.get(0).blockPosition())>12 && pAnt.getLevel().canSeeSky(pAnt.blockPosition())){
                        pAnt.walkTo(clouds.get(0).blockPosition(),1, 0.5);
                        //AntUtils.broadcastString(pAnt.getLevel(), "Scouting action check 1");
                    }
                    else if (tempPos != BlockPos.ZERO && pAnt.canReach(tempPos) && clouds.isEmpty()) {
                        AntScentCloud scent = new AntScentCloud(ModEntityTypes.ANT_EFFECT_CLOUD.get(), pAnt.getLevel());
                        scent.moveTo(Vec3.atCenterOf(tempPos));
                        scent.COLONY_ID = pAnt.getColonyID();
                        scent.WORKING_STAGE = WorkingStages.FORAGING;
                        pAnt.getLevel().addFreshEntity(scent);
                        pAnt.setWorkingStage(WorkingStages.FORAGING);
                        pAnt.getNavigation().stop();
                        //AntUtils.broadcastString(pAnt.getLevel(), "Scouting action check 2");
                    } else{
                        if (pAnt.getLevel().canSeeSky(pAnt.blockPosition())) {
                            if(pAnt.getDistTo(pAnt.getFoodLocation())<6){pAnt.setFoodLocation(BlockPos.ZERO);}
                            if(pAnt.getFoodLocation() != BlockPos.ZERO){
                                pAnt.walkTo(pAnt.getFoodLocation(), 1, 4);
                            }
                            else {
                                AntUtils.wanderRandomly(pAnt);
                            }
                           // AntUtils.broadcastString(pAnt.getLevel(), "Scouting action check 3");
                        } else {
                            if (pAnt.getSurfacePos() != BlockPos.ZERO) {
                                pAnt.walkTo(pAnt.getSurfacePos(), 1, 4d);
                               // AntUtils.broadcastString(pAnt.getLevel(), "Scouting action check 4");
                            }
                        }
                    }
                }
            }
        }
    };
    public static final Action GO_UNDERGROUND = new Action(){
        @Override
        public void run(Ant pAnt) {
            if(pAnt.getNavigation().isStuck() || pAnt.getNavigation().isDone()){
                if(pAnt.getGoUndergroundList().size() > 0) {
                    pAnt.walkAlongList(pAnt.getGoUndergroundList(), 1, 6d);
                }
                else{
                    pAnt.setErrorMessage("Ant cannot identify a list of positions to follow to enter its colony");
                    Actions.ERROR_MSG_ACTION.run(pAnt);
                }
            }
        }
    };
    public static final Action GO_ABOVEGROUND = new Action(){
        @Override
        public void run(Ant pAnt) {

            if(pAnt.getNavigation().isStuck() || pAnt.getNavigation().isDone()){
                if(pAnt.getLevel().canSeeSky(pAnt.blockPosition()) || AntUtils.getHorizontalDist(pAnt.blockPosition(), pAnt.getSurfacePos()) < 12){
                    int stg = pAnt.getWorkingStage();
                    BlockPos fPos = pAnt.getFoodLocation();
                    if(fPos != null && fPos != BlockPos.ZERO && (stg == WorkingStages.SCOUTING || stg == WorkingStages.FORAGING)){
                        pAnt.walkTo(fPos, 1, 3d);
                        if(AntUtils.getHorizontalDist(pAnt.blockPosition(),fPos) < 5f){
                            pAnt.setFoodLocation(BlockPos.ZERO);
                        }
                    }
                    else {
                        AntUtils.wanderRandomly(pAnt);
                    }
                }
                else{
                    AntColony colony = pAnt.getColony();
                    if(colony != null) {
                        ColonyBranch tunnels = colony.tunnels;
                        if(tunnels != null) {
                            ArrayList<BlockPos> posesToBottom = tunnels.getPosesToBranch(tunnels.getNearestBranchID(pAnt.blockPosition()));
                            ArrayList<BlockPos> posesToTop = new ArrayList<>();
                            for(int i = posesToBottom.size()-1; i >= 0; i --){
                                posesToTop.add(posesToBottom.get(i));
                            }
                            pAnt.walkAlongList(posesToTop, 1, 4d);
                        }
                    }
                }
            }
            
        }
    };
    public static final Action EAT_FUNGUS = new Action(){
        @Override
        public void run(Ant pAnt) {
            Level pLevel = pAnt.getLevel();
            Predicate<BlockPos> isEdible = p -> {Block predBlock = (pLevel.getBlockState(p).getBlock());
                return predBlock == ModBlocks.FUNGAL_NODULE.get() || predBlock == ModBlocks.FUNGUS.get();
            };

            BlockPos fungusPos = BlockPos.findClosestMatch(pAnt.blockPosition(), 3, 2, isEdible).orElse(BlockPos.ZERO);

            if(fungusPos == BlockPos.ZERO){
                fungusPos = pAnt.getFungusLocation();
            }
            else{
                pAnt.setFungusLocation(fungusPos);
            }

            if(fungusPos != BlockPos.ZERO){
                if(!isEdible.test(fungusPos)) {
                    fungusPos = BlockPos.findClosestMatch(fungusPos, 3, 3, isEdible).orElse(BlockPos.ZERO);
                    pAnt.setFungusLocation(fungusPos);
                    pAnt.setInterestPos(fungusPos);
                }
                if(fungusPos != BlockPos.ZERO){
                    //AntUtils.broadcastString(pLevel,"Ant eating fungus at " +BlockPosStringifier.jsonFromPos(fungusPos));
                    Block oldBlock = pLevel.getBlockState(fungusPos).getBlock();
                    BREAK_INTEREST_BLOCK.run(pAnt);

                    // Basically, use the break_interest_block action for the animation and such,
                    // but detect the block breaking from it and replace with the correct block as designated by the oldBlock var
                    if(!isEdible.test(fungusPos)) {
                        if (oldBlock == ModBlocks.FUNGAL_NODULE.get()) {
                            pAnt.setHunger(pAnt.getHunger() + 60000);
                            pLevel.setBlock(fungusPos, ModBlocks.FUNGUS.get().defaultBlockState(), 2);
                            pLevel.playLocalSound(fungusPos.getX(), fungusPos.getY(), fungusPos.getZ(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 5f, 1f, false);
                        } else if (oldBlock == ModBlocks.FUNGUS.get()) {
                            pAnt.setHunger(pAnt.getHunger() + 40000);
                            pLevel.setBlock(fungusPos, ModBlocks.ANT_AIR.get().defaultBlockState(), 2);
                            pLevel.playLocalSound(fungusPos.getX(), fungusPos.getY(), fungusPos.getZ(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 5f, 1f, false);
                        }
                        AntUtils.broadcastString(pLevel,"Ant ate fungus at " + BlockPosStringifier.jsonFromPos(fungusPos) + ". Its hunger is now " + pAnt.getHunger());
                    }
                }
            }
        }
    };
    /**Draws from interest pos first**/
    public static final Action EXTRACT_LEAVES = new Action(){
        @Override
        public void run(Ant pAnt){
            ServerLevel pLevel = (ServerLevel)pAnt.getLevel();
            BlockPos containerPos = pAnt.getInterestPos();
            if(pLevel.getBlockState(containerPos).getBlock() != ModBlocks.LEAFY_CONTAINER_BLOCK.get()){
                containerPos = pAnt.getHomeContainerPos();
            }

            BlockEntity tempEntity = pLevel.getBlockEntity(containerPos);
            if(tempEntity instanceof FungalContainerBlockEntity){
                FungalContainerBlockEntity containerBlockEntity = (FungalContainerBlockEntity) tempEntity;

                if(containerBlockEntity.giveFungusLeaves(pAnt)){
                    AntUtils.broadcastString(pAnt.getLevel(),"Ant got leaves at " + BlockPosStringifier.jsonFromPos(containerPos));
                    pAnt.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200));
                }
            }
        }
    };
    public static final Action GET_PASSIVE_TARGET = new Action(){
        @Override
        public void run(Ant pAnt) {
            ArrayList<Animal> list = (ArrayList<Animal>) pAnt.getLevel().getEntitiesOfClass(Animal.class,pAnt.getBoundingBox().inflate(3d));

            if(!list.isEmpty()){
                pAnt.setPassiveTarget(list.get(0));
            }
        }
    };
    public static final Action SET_WORKING_STAGE_WANDER = new Action(){
        @Override
        public void run(Ant pAnt){
            pAnt.setWorkingStage(WorkingStages.WANDERING);
        }
    };
    public static final Action SET_WORKING_STAGE_SCOUT = new Action(){
        @Override
        public void run(Ant pAnt){
            pAnt.setWorkingStage(WorkingStages.SCOUTING);
        }
    };
    public static final Action SET_WORKING_STAGE_FORAGE = new Action(){
        @Override
        public void run(Ant pAnt){
            pAnt.setWorkingStage(WorkingStages.FORAGING);
        }
    };
    public static final Action SET_WORKING_STAGE_FARM = new Action(){
        @Override
        public void run(Ant pAnt){
            pAnt.setWorkingStage(WorkingStages.FARMING);
        }
    };
    public static final Action SET_WORKING_STAGE_NURSE = new Action(){
        @Override
        public void run(Ant pAnt){
            pAnt.setWorkingStage(WorkingStages.NURSING);
        }
    };
    public static final Action SET_WORKING_STAGE_TIDY = new Action(){
        @Override
        public void run(Ant pAnt){
            pAnt.setWorkingStage(WorkingStages.TIDYING);
        }
    };
    public static final Action SET_WORKING_STAGE_EXCAVATE = new Action(){
        @Override
        public void run(Ant pAnt){
            pAnt.setWorkingStage(WorkingStages.EXCAVATING);
        }
    };
    public static final Action SET_WORKING_STAGE_ATTACK = new Action(){
        @Override
        public void run(Ant pAnt){
            pAnt.setWorkingStage(WorkingStages.ATTACKING);
        }
    };
    public static final Action SET_WORKING_STAGE_LATCH = new Action(){
        @Override
        public void run(Ant pAnt){
            pAnt.setWorkingStage(WorkingStages.LATCHING);
        }
    };

}
