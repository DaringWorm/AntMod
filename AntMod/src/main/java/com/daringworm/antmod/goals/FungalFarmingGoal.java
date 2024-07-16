package com.daringworm.antmod.goals;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.block.entity.custom.FungalContainerBlockEntity;
import com.daringworm.antmod.entity.Ant;
import com.daringworm.antmod.entity.custom.WorkerAnt;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static com.daringworm.antmod.block.custom.FungusCarpet.AGE;

public class FungalFarmingGoal<T extends LivingEntity> extends Goal {
    public FungalFarmingGoal(Ant pAnt) {
        this.ant = pAnt;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    private final Ant ant;

    private int advancedCheckTimer = 0;

    private int wanderWait = 0;

    private int lastUsabilityCheck = 0;
    private int lastContainerCheck = 0;

    private int lastAdjPCheck = 0;

    private double distToInteract = 5;

    float containerFullness = 0f;

    float edibleContainerFullness = 0f;
    private double aiTimer = 0;

    boolean isCompressingContainers = false;

    ArrayList<BlockPos> containerPosList = new ArrayList<>();

    ArrayList<BlockPos> usableContainerPosList = new ArrayList<>();

    ArrayList<BlockPos> fungusPosList = new ArrayList<>();

    ArrayList<BlockPos> fungusAdjacentPosList = new ArrayList<>();

    private int destroyWait = 0;

    private boolean isBreakingBlock = false;

    private static final int minAllowedHunger = 33000;



    public boolean canUse() {
        return this.ant.isAlive() && (this.ant.getTarget() == null || !ant.getTarget().isAlive()) && ant.getWorkingStage() == 0;}

    public boolean canContinueToUse() {
        return canUse();
    }

    public void start(){
        distToInteract = ant.getBoundingBox().getXsize() * 2;
    }

    public void stop() {
        advancedCheckTimer = 0;
        wanderWait = 0;
        lastUsabilityCheck = 0;
        lastContainerCheck = 0;
        lastAdjPCheck = 0;
        containerFullness = 0;
        edibleContainerFullness = 0;
        aiTimer = 0;
        isCompressingContainers = false;
        containerPosList.clear();
        usableContainerPosList.clear();
        fungusPosList.clear();
        fungusAdjacentPosList.clear();
        destroyWait = 0;
        ant.setSnippingAnimation(false);
    }

    public boolean requiresUpdateEveryTick(){return true;}


    public void tick() {
        double random = Math.random();
        wanderWait++;
        if(ant.getIsSnippingAnimation()){destroyWait++;}
        lastUsabilityCheck++;
        lastContainerCheck++;
        lastAdjPCheck++;
        aiTimer = aiTimer + random;
        BlockPos homePos = ant.getHomeContainerPos();
        BlockPos antPos = ant.blockPosition();
        ItemStack heldItem = ant.getMainHandItem();
        double dist2home = AntUtils.getDist(antPos,homePos);
        if(isBreakingBlock){destroyWait++;ant.setSnippingAnimation(true);}
        else{ant.setSnippingAnimation(false);}
        boolean isHandFungusFood = ant.getMainHandItem().getItem() == ModBlocks.LEAFY_MIXTURE.get().asItem() || ant.getMainHandItem().getItem() == ModBlocks.WING_DEBRIS.get().asItem();

        if (aiTimer>10 && ant.getWorkingStage() == 0 && ant.level.getNearestPlayer(ant, 120) != null) {
            advancedCheckTimer++;
            aiTimer = 0;
            List<BlockPos> cPosTemp = new ArrayList<>();
            if(!containerPosList.isEmpty()) {
                for (BlockPos tempPos : containerPosList) {
                    if (ant.level.getBlockState(tempPos).getBlock() != ModBlocks.LEAFY_CONTAINER_BLOCK.get()) {
                        cPosTemp.add(tempPos);
                    }
                }
                if(!cPosTemp.isEmpty()) {
                    for (BlockPos tempP : cPosTemp) {
                        usableContainerPosList.remove(tempP);
                    }

                    cPosTemp.clear();
                }
            }

            if(containerPosList.isEmpty()){usableContainerPosList.clear();}
            else {
                if(!usableContainerPosList.isEmpty() && advancedCheckTimer >5) {
                    for (BlockPos tempPos : usableContainerPosList) {
                        if (ant.level.getBlockState(tempPos).getBlock() != ModBlocks.LEAFY_CONTAINER_BLOCK.get()) {
                            cPosTemp.add(tempPos);
                        }
                    }
                    if (!cPosTemp.isEmpty()) {
                        for (BlockPos tempI : cPosTemp) {
                            usableContainerPosList.remove(tempI);
                        }

                        cPosTemp.clear();
                    }
                }
            }

            if(containerPosList.isEmpty() || advancedCheckTimer>5) {
                containerPosList = AntUtils.findAllBlockPos(ModBlocks.LEAFY_CONTAINER_BLOCK.get(), ant.blockPosition(), 10, 6, ant.level);
                containerFullness = AntUtils.checkContainersFullness(containerPosList, ant.level, true);
                if(usableContainerPosList.isEmpty()){
                    for(BlockPos tempPos : containerPosList){
                        if(AntUtils.canReach(ant,tempPos)){
                            usableContainerPosList.add(tempPos);
                        }
                    }
                }
            }

            if(fungusPosList.isEmpty() || (advancedCheckTimer > 5 && dist2home<10)){
                fungusPosList = AntUtils.findAllBlockPos(ModBlocks.FUNGUS_CARPET.get(),ant.blockPosition(),10,6,ant.level);
            }

            if(fungusPosList.isEmpty()){fungusAdjacentPosList.clear();}

            if(fungusAdjacentPosList.isEmpty() && !fungusPosList.isEmpty()){
                for(BlockPos tempPos : fungusPosList){
                    List<BlockPos> tempList = AntUtils.findBlocksAdjacentTo(RenderShape.INVISIBLE,tempPos,ant.level,true);
                    for(BlockPos tempPos2: tempList){
                        if(!fungusAdjacentPosList.contains(tempPos2)){
                            fungusAdjacentPosList.add(tempPos2);
                        }
                    }
                }
            }
            else if(advancedCheckTimer > 5){
                for(BlockPos tempPos : fungusAdjacentPosList){
                    if(ant.level.getBlockState(tempPos).getRenderShape() != RenderShape.INVISIBLE){
                        cPosTemp.add(tempPos);
                    }
                    if(!AntUtils.isAdjacentTo(ant.level,tempPos,ModBlocks.FUNGUS_CARPET.get(),true)){
                        cPosTemp.add(tempPos);
                    }
                    if(AntUtils.getDist(tempPos,homePos)>10){
                        cPosTemp.add(tempPos);
                    }
                }
                for(BlockPos tempP : cPosTemp){
                    fungusAdjacentPosList.remove(tempP);
                }
                cPosTemp.clear();
            }


            edibleContainerFullness = AntUtils.checkContainersFullness(usableContainerPosList,ant.level,true);
            containerFullness = AntUtils.checkContainersFullness(usableContainerPosList,ant.level,false);
            BlockPos ediblesInThisContainer = AntUtils.findNearestContainerToExtract(ant,usableContainerPosList);

            if(advancedCheckTimer>5){advancedCheckTimer=0;}

            ///////////////////////////////////////////////////////////////////////////////////////////////////////
            /////////////////////////// EVERYTHING ABOVE THIS IS TO UPDATE LISTS //////////////////////////////////
            ///////////////////////////////////////////////////////////////////////////////////////////////////////

            //sets leafy mixture
            if(!fungusAdjacentPosList.isEmpty() && ant.getHunger() > minAllowedHunger) {
                if ((ediblesInThisContainer != BlockPos.ZERO)) {
                    if (ant.getMainHandItem().isEmpty()) {
                        ant.walkTo(ediblesInThisContainer, 1, 1d);
                        if (AntUtils.getDist(antPos, ediblesInThisContainer) < distToInteract) {
                            AntUtils.extractLeafyMixture(ant, (FungalContainerBlockEntity) ant.level.getBlockEntity(ediblesInThisContainer));
                        }
                    }
                }
                isHandFungusFood = ant.getMainHandItem().getItem() == ModBlocks.LEAFY_MIXTURE.get().asItem() || ant.getMainHandItem().getItem() == ModBlocks.WING_DEBRIS.get().asItem();
                if (isHandFungusFood) {
                    BlockPos placePos = AntUtils.findNearestBlockPos(ant,fungusAdjacentPosList);
                    if (placePos != BlockPos.ZERO) {
                        ant.walkTo(placePos, 1, 1d);
                        if (AntUtils.getDist(placePos, antPos) < distToInteract) {
                            if (ant.level.getBlockState(placePos).getRenderShape() == RenderShape.INVISIBLE) {
                                if(ant.getMainHandItem().getItem() == ModBlocks.LEAFY_MIXTURE.get().asItem()) {
                                    ant.level.setBlock(placePos, ModBlocks.LEAFY_MIXTURE.get().defaultBlockState(), 2);
                                }
                                else if(ant.getMainHandItem().getItem() == ModBlocks.WING_DEBRIS.get().asItem()){
                                    ant.level.setBlock(placePos, ModBlocks.WING_DEBRIS.get().defaultBlockState(), 2);
                                }
                                ant.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                                fungusAdjacentPosList.remove(placePos);
                            } else {
                                fungusAdjacentPosList.clear();
                                advancedCheckTimer = 20;
                            }
                        }
                    }
                }
            }

            //finds and places items in containers
            heldItem = ant.getMainHandItem();
            if((heldItem.isEmpty() || (!isHandFungusFood || fungusAdjacentPosList.isEmpty()))){
                if(!ant.getMainHandItem().isEmpty()){
                    BlockPos dumpPos = AntUtils.findNearestUsableContainer(ant,containerPosList);
                    if(dumpPos != BlockPos.ZERO) {
                        ant.walkTo(dumpPos, 1,1d);
                        if (AntUtils.getDist(antPos, dumpPos) < distToInteract) {
                            if (ant.level.getBlockState(dumpPos).getBlock() == ModBlocks.LEAFY_CONTAINER_BLOCK.get()) {
                                AntUtils.antAddItem(ant, ant.level.getBlockEntity(dumpPos), ant.getMainHandItem());
                            } else {
                                usableContainerPosList.remove(dumpPos);
                            }
                        }
                    }
                }
                else{
                    if(ant.getHunger()>minAllowedHunger) {
                        List<ItemEntity> itemList = ant.level.getEntitiesOfClass(ItemEntity.class, ant.getBoundingBox().inflate(8d));
                        if (!itemList.isEmpty()) {
                            ant.setCanPickUpLoot(true);
                            ItemEntity tempEntity = itemList.get(0);
                            for (ItemEntity ent : itemList) {
                                if (AntUtils.canReach(ant, ent.blockPosition()) && AntUtils.getDist(antPos, ent.blockPosition()) < AntUtils.getDist(antPos, tempEntity.blockPosition())) {
                                    tempEntity = ent;
                                }
                            }
                            ant.walkTo(tempEntity.blockPosition(), 1,1d);
                        }
                    }
                }
            }

            //eats fungus
            if(ant.getHunger()<minAllowedHunger && ant.getMainHandItem().isEmpty()){
                BlockPos foodPos = AntUtils.findNearestBlockPos(ant,fungusPosList);
                BlockState pState = ant.level.getBlockState(foodPos);
                BlockState pStateUnder = ant.level.getBlockState(ant.blockPosition());
                if(pStateUnder.getBlock() == ModBlocks.FUNGUS_CARPET.get()){foodPos = ant.blockPosition();pState = pStateUnder;}

                if(pState.getBlock() == ModBlocks.FUNGUS_CARPET.get()) {
                    ant.walkTo(foodPos,1,1d);
                    if (AntUtils.getDist(antPos, foodPos) < distToInteract) {
                        isBreakingBlock = true;
                        if (destroyWait > 20) {
                            ant.setHunger(100000);
                            destroyWait = 0;
                            isBreakingBlock = false;
                            int growth = pState.getValue(AGE);
                            if(growth > 0){
                                ant.level.setBlock(foodPos, pState.setValue(AGE, growth - 1), 2);
                            }
                            else{ant.level.setBlock(foodPos,ModBlocks.ANT_AIR.get().defaultBlockState(),2);}
                        }
                    } else {
                        isBreakingBlock = false;
                        destroyWait = 0;
                    }
                }
                else{fungusPosList.remove(foodPos);}

                if(foodPos == BlockPos.ZERO){
                    ant.getNavigation().stop();
                    ant.walkTo(homePos,1,1d);
                }
            }

            //decides to enter foraging mode
            if((ediblesInThisContainer == BlockPos.ZERO && containerFullness < 1f && ant.getHunger() > minAllowedHunger)
            || (fungusAdjacentPosList.isEmpty() && !fungusPosList.isEmpty())){
                if(ant.getSurfacePos() != BlockPos.ZERO && ant instanceof WorkerAnt) {
                    ant.setWorkingStage(2);
                }
                else{
                    AntUtils.wanderRandomly(ant);
                }
            }
            /*if(ant.getIsAboveground() && dist2home > 15 && ant instanceof WorkerAnt){
                ant.setWorkingStage(2);
            }
            if(ant.getHunger()<minAllowedHunger && !ant.getIsAboveground() && dist2home>15){
                ant.walkTo(homePos,1,1d);
            }
            if((fungusPosList.isEmpty() || usableContainerPosList.isEmpty()) && ant.getHunger() < minAllowedHunger && dist2home>10){
                ant.walkTo(homePos,1,1d);
            }*/
        }
    }
}

