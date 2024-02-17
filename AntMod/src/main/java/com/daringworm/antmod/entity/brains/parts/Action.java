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
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Action {

    public void run(Ant pAnt){}


    public static final class MoveToEntityAction extends Action {
        @Override
        public void run(Ant pAnt){
            Entity target = (pAnt.getTarget() != null && pAnt.getTarget().isAlive()) ? pAnt.getTarget() : pAnt.memory.passiveTarget;
            if(target != null && target.isAlive()) {
                pAnt.getNavigation().moveTo(target, 1);
            }
        }
    }

    public static final class SelectItemToPickupAction extends Action {
        @Override
        public void run(Ant pAnt){
            if(pAnt.memory.foundItemList.size()>0){
                boolean hasntChosen = true;
                /*for(ItemEntity item : pAnt.memory.foundItemList){
                    if(pAnt.canReach(item.blockPosition()) && hasntChosen){
                        pAnt.memory.passiveTarget = item;
                        hasntChosen = false;
                    }
                }*/
                pAnt.memory.passiveTarget = pAnt.memory.foundItemList.get(0);
            }
        }

    }

    public static final class FindEmptyContainerAction extends Action{
        @Override
        public void run(Ant pAnt){
            ArrayList<BlockPos> containerPoses = pAnt.memory.containerPosSet;

            if(!containerPoses.isEmpty()){
                boolean found1 = false;
                for(BlockPos tempPos : containerPoses){
                    if(!found1) {
                        BlockState tempState = pAnt.getLevel().getBlockState(tempPos);
                        if (tempState.getBlock() == ModBlocks.LEAFY_CONTAINER_BLOCK.get()) {
                            FungalContainerBlockEntity containerEntity = (FungalContainerBlockEntity) pAnt.getLevel().getBlockEntity(tempPos);
                            if (containerEntity != null && containerEntity.canAcceptHandItem(pAnt)) {
                                pAnt.memory.containerPos = tempPos;
                                found1 = true;
                            }
                        }
                    }
                }
            }
        }
    }

    public static final class ScoutAction extends Action{
        @Override
        public void run(Ant pAnt){
            if(pAnt instanceof WorkerAnt){

                if (pAnt.getNavigation().isDone() || pAnt.getNavigation().isStuck()) {
                    BlockPos tempPos = BlockPos.findClosestMatch(pAnt.blockPosition(), 6, 4, p -> AntUtils.shouldSnip(p, pAnt.getLevel())).orElse(BlockPos.ZERO);
                    ArrayList<AntScentCloud> clouds = new ArrayList<>(pAnt.getLevel().getEntitiesOfClass(AntScentCloud.class, pAnt.getBoundingBox().inflate(18)));

                    if(!clouds.isEmpty() && pAnt.getDistTo(clouds.get(0).blockPosition())>12 && pAnt.getLevel().canSeeSky(pAnt.blockPosition())){
                        pAnt.walkTo(clouds.get(0).blockPosition(),1, 0.5);
                    }
                    else if (tempPos != BlockPos.ZERO && pAnt.canReach(tempPos) && clouds.isEmpty()) {
                        AntScentCloud scent = new AntScentCloud(ModEntityTypes.ANT_EFFECT_CLOUD.get(), pAnt.getLevel());
                        scent.moveTo(Vec3.atCenterOf(tempPos));
                        scent.COLONY_ID = pAnt.getColonyID();
                        scent.WORKING_STAGE = WorkingStages.FORAGING;
                        pAnt.getLevel().addFreshEntity(scent);
                        pAnt.setWorkingStage(WorkingStages.FORAGING);
                        pAnt.getNavigation().stop();
                    } else{
                        if (pAnt.getLevel().canSeeSky(pAnt.blockPosition())) {
                            if(pAnt.getDistTo(pAnt.memory.foodPos)<6){pAnt.memory.foodPos = BlockPos.ZERO;}
                            if(pAnt.memory.foodPos != null && pAnt.memory.foodPos != BlockPos.ZERO){
                                pAnt.walkTo(pAnt.memory.foodPos, 1, 4);
                            }
                            else {
                                AntUtils.wanderRandomly(pAnt);
                            }
                        } else {
                            if (pAnt.memory.surfacePos != null && pAnt.memory.surfacePos != BlockPos.ZERO) {
                                pAnt.walkTo(pAnt.memory.surfacePos, 1, 4d);
                            }
                        }
                    }

                }
            }
        }
    }

    public static final class GoUndergroundAction extends Action{
        @Override
        public void run(Ant pAnt){
            if(pAnt.getNavigation().isStuck() || pAnt.getNavigation().isDone()){
                if(!pAnt.memory.goUndergroundList.isEmpty()) {
                    pAnt.walkAlongList(pAnt.memory.goUndergroundList, 1, 4d);
                }
            }
        }
    }

    public static final class GoAbovegroundAction extends Action{
        @Override
        public void run(Ant pAnt){
            if(pAnt.getNavigation().isStuck() || pAnt.getNavigation().isDone()){
                if(pAnt.getLevel().canSeeSky(pAnt.blockPosition()) || AntUtils.getHorizontalDist(pAnt.blockPosition(),pAnt.memory.surfacePos) < 12){
                    int stg = pAnt.memory.workingStage;
                    BlockPos fPos = pAnt.memory.foodPos;
                    if(fPos != null && fPos != BlockPos.ZERO && (stg == WorkingStages.SCOUTING || stg == WorkingStages.FORAGING)){
                        pAnt.walkTo(pAnt.memory.foodPos, 1, 3d);
                        if(AntUtils.getHorizontalDist(pAnt.blockPosition(),fPos) < 5f){
                            pAnt.memory.foodPos = null;
                        }
                    }
                    else {
                        AntUtils.wanderRandomly(pAnt);
                    }
                }
                else{
                    pAnt.walkTo(pAnt.getFirstSurfacePos(),1, 5d);
                }
            }
        }
    }

    public static final class PlaceItemInContainerAction extends Action{
        @Override
        public void run(Ant pAnt){
            BlockState pState = pAnt.getLevel().getBlockState(pAnt.memory.interestPos);
            if(pState.getBlock() == ModBlocks.LEAFY_CONTAINER_BLOCK.get()){
                FungalContainerBlockEntity pEntity = (FungalContainerBlockEntity) pAnt.getLevel().getBlockEntity(pAnt.memory.interestPos);
                if(pEntity != null){
                    pEntity.takeInHandItem(pAnt);
                    pAnt.memory.interestPos = BlockPos.ZERO;
                    pAnt.setWorkingStage(WorkingStages.SCOUTING);
                    pAnt.memory.workingStage = WorkingStages.SCOUTING;
                }
            }
        }
    }

    public static final class SetContainerAsInterestAction extends Action{
        @Override
        public void run(Ant pAnt){
            if(pAnt.memory.containerPos != BlockPos.ZERO){
                pAnt.memory.interestPos = pAnt.memory.containerPos;
            }
            else if(pAnt.memory.homePos != BlockPos.ZERO){
                pAnt.memory.interestPos = pAnt.memory.homePos;
            }
        }
    }

    public static final class MoveToBlockAction extends Action {
        @Override
        public void run(Ant pAnt){
            BlockPos pPos = pAnt.memory.interestPos;
            if(pPos != BlockPos.ZERO && (pAnt.getNavigation().isDone() || pAnt.getNavigation().isStuck())) {
                pAnt.walkTo(pPos, 1, 2d);
            }
        }
    }

    public static final class BreakBlockAction extends Action {
        @Override
        public void run(Ant pAnt){
            BlockPos pPos = pAnt.memory.interestPos;
            if(pAnt.getLevel().getBlockState(pPos).getRenderShape() == RenderShape.INVISIBLE){
                pAnt.memory.interestPos = BlockPos.ZERO;
                pPos = pAnt.memory.interestPos;
                pAnt.setSnippingAnimation(false);
            }
            int breakingProgress = pAnt.memory.breakingProgress;
            float blockToughness = Math.max(pAnt.level.getBlockState(pPos).getDestroySpeed(pAnt.level, pPos)*24, 48);
            int destroyProgress = (int)((breakingProgress/blockToughness)*10);

            if(pPos != BlockPos.ZERO && pAnt.getDistTo(pPos) < 3d) {
                if (blockToughness < breakingProgress) {
                    BlockState pState = pAnt.getLevel().getBlockState(pPos);
                    ArrayList<ItemStack> drops = (ArrayList<ItemStack>) Block.getDrops(pState, (ServerLevel) pAnt.getLevel(),pPos,pAnt.getLevel().getBlockEntity(pPos));

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
                            pAnt.memory.excavationListCooked.remove(pAnt.memory.interestPos);
                            pAnt.memory.interestPos = BlockPos.ZERO;
                            pAnt.memory.breakingProgress = 0;
                            pAnt.setSnippingAnimation(false);
                            pAnt.getLevel().setBlock(pPos, pState.setValue(((CropBlock) pState.getBlock()).getAgeProperty(), 0), 2);
                        }
                        else{
                            pAnt.memory.excavationListCooked.remove(pAnt.memory.interestPos);
                            pAnt.memory.interestPos = BlockPos.ZERO;
                            pAnt.setSnippingAnimation(false);
                        }
                    }
                    else {
                        pAnt.getLevel().destroyBlock(pPos, false);
                        pAnt.memory.excavationListCooked.remove(pAnt.memory.interestPos);
                        pAnt.memory.interestPos = BlockPos.ZERO;
                        pAnt.memory.breakingProgress = 0;
                        pAnt.setSnippingAnimation(false);
                        if(pAnt.getLevel().canSeeSky(pPos) && pAnt.getDistTo(pAnt.getFirstSurfacePos())>45) {
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
                    pAnt.memory.breakingProgress = breakingProgress +1;
                    pAnt.getLevel().destroyBlockProgress(pAnt.getId(),pPos,destroyProgress);
                    pAnt.setSnippingAnimation(true);
                }
            }
            else{
                pAnt.memory.breakingProgress = 0;
            }
        }
    }

    public static final class ExcavateColonyBlockAction extends Action {
        @Override
        public void run(Ant pAnt){
            BlockPos pPos = pAnt.memory.interestPos;
            int breakingProgress = pAnt.memory.breakingProgress;
            float blockToughness = Math.max(16, (pAnt.level.getBlockState(pPos).getDestroySpeed(pAnt.level, pPos)*32));
            int destroyProgress = (int)((breakingProgress/blockToughness)*10);

            if(pPos != BlockPos.ZERO) {
                if (blockToughness <= breakingProgress) {
                    pAnt.getLevel().destroyBlock(pPos, false, pAnt);

                    for(Direction dir : Direction.values()){
                        BlockPos tempPos = pAnt.memory.interestPos.relative(dir,1);
                        BlockState tempState = pAnt.getLevel().getBlockState(tempPos);
                        if(!pAnt.getLevel().getFluidState(tempPos).isEmpty() || (!pAnt.getLevel().canSeeSky(tempPos) && tempState.getBlock() == Blocks.AIR)){
                            pAnt.getLevel().setBlock(tempPos, ModBlocks.LUMINOUSDEBRIS.get().defaultBlockState(),2);
                        }
                    }
                    pAnt.memory.excavationListCooked.remove(pAnt.memory.interestPos);
                    pAnt.level.setBlock(pAnt.memory.interestPos,ModBlocks.ANT_AIR.get().defaultBlockState(),2);
                    pAnt.memory.interestPos = BlockPos.ZERO;
                    pAnt.memory.breakingProgress = 0;
                    pAnt.setSnippingAnimation(false);
                }
                else {
                    pAnt.setSnippingAnimation(true);
                    pAnt.memory.breakingProgress = breakingProgress +1;
                    pAnt.getLevel().destroyBlockProgress(pAnt.getId(),pPos,destroyProgress);
                }
            }
            else{
                pAnt.memory.breakingProgress = 0;
            }
        }
    }

    public static final class SetExcavationBlockAsInterestAction extends Action {
        @Override
        public void run(Ant pAnt){
            LeafCutterMemory mem = pAnt.memory;
            if(mem.excavationListCooked.isEmpty()){
                pAnt.memory.refreshExcavationList(pAnt); mem = pAnt.memory;
            }
            if(!mem.excavationListCooked.isEmpty() && !mem.excavationListCooked.contains(mem.interestPos)){
                pAnt.memory.interestPos = AntUtils.findNearestBlockPos(pAnt, mem.excavationListCooked);

                if(pAnt.getLevel().getBlockState(pAnt.memory.interestPos).getBlock() == ModBlocks.ANT_AIR.get()){
                    for(int i = mem.excavationListCooked.size()-1; i >=0; i--){
                        BlockPos tempPos = mem.excavationListCooked.get(i);
                        BlockState tempState = pAnt.getLevel().getBlockState(tempPos);
                        if(tempState.getBlock() == ModBlocks.ANT_AIR.get() || (tempState.getBlock() == Blocks.AIR && pAnt.getLevel().canSeeSky(tempPos))){
                            pAnt.memory.excavationListCooked.remove(i);
                        }
                    }
                }
                if(!pAnt.memory.excavationListCooked.isEmpty()) {
                    pAnt.memory.interestPos = AntUtils.findNearestBlockPos(pAnt, mem.excavationListCooked);
                }
            }
        }
    }

    public static final class PickupItemAction extends Action {
        @Override
        public void run(Ant pAnt){
            if(pAnt.memory.foundItemList.size() != 0) {
                List<ItemEntity> list = pAnt.getLevel().getEntitiesOfClass(ItemEntity.class,pAnt.getBoundingBox().inflate(6));
                if(list.isEmpty()){return;}
                ItemEntity item = list.get(0);
                if(!item.isRemoved()) {
                    pAnt.getNavigation().moveTo(item, 1.2d);
                    if (pAnt.getWorkingStage() == WorkingStages.SCOUTING) {
                        pAnt.setWorkingStage(WorkingStages.FORAGING);
                        pAnt.memory.workingStage = WorkingStages.FORAGING;
                        if (pAnt.getLevel().canSeeSky(item.blockPosition())) {
                            pAnt.memory.foodPos = item.blockPosition();
                            pAnt.setFoodLocation(item.blockPosition());
                        }
                    }
                    item.remove(Entity.RemovalReason.DISCARDED);
                    pAnt.setItemInHand(InteractionHand.MAIN_HAND,item.getItem());
                }
            }
        }
    }

    public static final class FindSnippableBlockAction extends Action {
        @Override
        public void run(Ant pAnt){
            assert pAnt.getLevel() instanceof ServerLevel;
            ServerLevel pLevel = (ServerLevel) pAnt.getLevel();
            pAnt.memory.interestPos = BlockPos.findClosestMatch(pAnt.blockPosition(),8,4, pAnt.memory.foodStatePredicate(pAnt)).orElse(BlockPos.ZERO);
        }
    }

    public static final class AttackEntityAction extends Action {
        @Override
        public void run(Ant pAnt){
            LivingEntity target = pAnt.getTarget();
            if(target != null && target.isAlive() && pAnt.canAttack(target)) {
                if(pAnt.distanceToSqr(target) <= 2.5f && pAnt.hasLineOfSight(target)){
                    target.hurt(DamageSource.mobAttack(pAnt), 2f);
                }
            }
        }
    }

    public static final class SetAggressorAsTargetAction extends Action {
        @Override
        public void run(Ant pAnt){
            LivingEntity target = pAnt.getLastHurtByMob();
            if(target != null && target.isAlive() && pAnt.canAttack(target)) {
                pAnt.setTarget(target);
            }
            pAnt.setLastHurtByMob(null);
        }
    }

    public static final class LatchOnTargetAction extends Action {
        @Override
        public void run(Ant pAnt){
            assert pAnt instanceof WorkerAnt;
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
    }

    public static final class ErrorAlertAction extends Action {
        @Override
        public void run(Ant pAnt){
            if(pAnt.getLevel() instanceof ServerLevel){

                String toSay = "";
                toSay = Objects.requireNonNullElse(pAnt.memory.errorAlertString, "Ant's brain encountered an error");

                for(ServerPlayer player : Objects.requireNonNull(pAnt.getLevel().getServer()).getPlayerList().getPlayers()){
                    player.sendMessage(new TextComponent(toSay), player.getUUID());
                }
            }
        }
    }
}
