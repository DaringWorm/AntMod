package com.daringworm.antmod.entity.brains.parts;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.block.entity.custom.FungalContainerBlockEntity;
import com.daringworm.antmod.entity.Ant;
import com.daringworm.antmod.entity.ModEntityTypes;
import com.daringworm.antmod.entity.brains.memories.LeafCutterMemory;
import com.daringworm.antmod.entity.custom.AntScentCloud;
import com.daringworm.antmod.entity.custom.WorkerAnt;
import com.daringworm.antmod.goals.AntUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.system.CallbackI;

import java.util.ArrayList;
import java.util.List;

public class Actions {
    public static final Action ATTACK_ENTITY = new Action(){
        @Override
        public void run(Ant pAnt) {
            LivingEntity target = pAnt.getTarget();
            if (target != null && target.isAlive() && pAnt.canAttack(target)) {
                if (pAnt.distanceToSqr(target) <= 2.5f && pAnt.hasLineOfSight(target)) {
                    target.hurt(DamageSource.mobAttack(pAnt), 2f);
                }
            }
            
        }
    };
    public static final Action WALK_TO_TARGET = new Action(){
        @Override
        public void run(Ant pAnt) {

            Entity target = pAnt.getTarget();
            if(target != null && target.isAlive()) {
                pAnt.getNavigation().moveTo(target, 1);
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
            if(pPos != BlockPos.ZERO/* && (pAnt.getNavigation().isDone() || pAnt.getNavigation().isStuck())*/) {
                pAnt.walkTo(pPos, 1, 2d);
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

            if(pAnt.getPassiveTarget() != null && pAnt.getPassiveTarget().isAlive() && pAnt.getPassiveTarget() instanceof ItemEntity) {
                pAnt.setItemInHand(InteractionHand.MAIN_HAND, ((ItemEntity)pAnt.getPassiveTarget()).getItem());
                pAnt.getPassiveTarget().remove(Entity.RemovalReason.DISCARDED);
                pAnt.setPassiveTarget(null);
            }
            
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


            if(!pAnt.getCookedExcavationPosList().isEmpty() && !pAnt.getCookedExcavationPosList().contains(pAnt.getInterestPos())){
                pAnt.setInterestPos(AntUtils.findNearestBlockPos(pAnt,pAnt.getCookedExcavationPosList()));

                if(pAnt.getLevel().getBlockState(pAnt.getInterestPos()).getBlock() == ModBlocks.ANT_AIR.get()){
                    for(int i = pAnt.getCookedExcavationPosList().size()-1; i >=0; i--){
                        BlockPos tempPos = pAnt.getCookedExcavationPosList().get(i);
                        BlockState tempState = pAnt.getLevel().getBlockState(tempPos);
                        if(tempState.getBlock() == ModBlocks.ANT_AIR.get() || (tempState.getBlock() == Blocks.AIR && pAnt.getLevel().canSeeSky(tempPos))){
                            pAnt.getCookedExcavationPosList().remove(i);
                        }
                    }
                }
                if(!pAnt.getCookedExcavationPosList().isEmpty()) {
                    pAnt.setInterestPos(AntUtils.findNearestBlockPos(pAnt, pAnt.getCookedExcavationPosList()));
                }
            }
            
        }
    };
    public static final Action EXCAVATE_INTEREST_POS = new Action(){
        @Override
        public void run(Ant pAnt) {

            BlockPos pPos = pAnt.getInterestPos();
            int breakingProgress = pAnt.getBreakingProgress();
            float blockToughness = Math.max(16, (pAnt.level.getBlockState(pPos).getDestroySpeed(pAnt.level, pPos)*32));
            int destroyProgress = (int)((breakingProgress/blockToughness)*10);

            if(pPos != BlockPos.ZERO) {
                if (blockToughness <= breakingProgress) {
                    pAnt.getLevel().destroyBlock(pPos, false, pAnt);

                    for(Direction dir : Direction.values()){
                        BlockPos tempPos = pAnt.getInterestPos().relative(dir,1);
                        BlockState tempState = pAnt.getLevel().getBlockState(tempPos);
                        if(!pAnt.getLevel().getFluidState(tempPos).isEmpty() || (!pAnt.getLevel().canSeeSky(tempPos) && tempState.getBlock() == Blocks.AIR)){
                            pAnt.getLevel().setBlock(tempPos, ModBlocks.GLOWING_DEBRIS.get().defaultBlockState(),2);
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
                    pAnt.walkTo(pAnt.getSurfacePos(),1, 5d);
                }
            }
            
        }
    };
    public static final Action EAT_FUNGUS = new Action(){
        @Override
        public void run(Ant pAnt) {
            
        }
    };

}
