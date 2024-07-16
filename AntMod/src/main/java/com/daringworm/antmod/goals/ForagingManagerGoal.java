package com.daringworm.antmod.goals;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.entity.Ant;
import com.daringworm.antmod.entity.custom.WorkerAnt;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ForagingManagerGoal extends Goal {



private static final int minAllowedHunger = 33000;

    private final Ant ant;

    private int wanderWait = 0;

    private double aiTimer = 0;

    private int advancedCheckTimer = 0;

    BlockPos fungusPos = BlockPos.ZERO;

    private boolean hasCheckedFPos = false;

    private int destroyWait = 0;
    private static final double interactionDist = 2.5d;

    private List<BlockPos> containerPosList = new ArrayList<>();

    private List<BlockPos> foodPosList = new ArrayList<>();

    private static final int DEFAULT_SEARCH_RADIUS = 20;

    public ForagingManagerGoal(Ant pMob) {
        ant = pMob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }


    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
    return ant.isAlive() && (ant.getTarget() == null || !ant.getTarget().isAlive()) &&
            ant.getWorkingStage() == 2;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean canContinueToUse() {
        return canUse();
    }
    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void stop() {
        ant.getNavigation().stop();
        wanderWait = 0;
        aiTimer = 0;
        hasCheckedFPos = false;
        destroyWait = 0;
        containerPosList.clear();
        foodPosList.clear();
        advancedCheckTimer = 0;
    }
    public boolean requiresUpdateEveryTick(){return true;}

    public void start(){ant.setSnippingAnimation(false);}

    /**
     * Keep ticking a continuous task that has already been started
     */

    public void tick() {
        /*double random = Math.random();
        wanderWait++;
        aiTimer = aiTimer + random;
        advancedCheckTimer++;
        boolean isSnipping = ant.getIsSnippingAnimation();
        if (isSnipping){destroyWait ++;}
        if(AntUtils.getDist(ant.blockPosition(),ant.getFoodLocation())>3){
            ant.setSnippingAnimation(false);
        }




        if ((ant.getTarget() == null || !ant.getTarget().isAlive()) && ant.level.getNearestPlayer(ant,120) != null && aiTimer>10 && ant.getWorkingStage() == 2) {
            BlockPos homePos = ant.getHomeContainerPos();
            double dist2Home = ant.distanceToSqr(homePos.getX(), homePos.getY(), homePos.getZ());
            BlockPos foodPos = ant.getFoodLocation();
            double dist2food = ant.distanceToSqr(foodPos.getX(), foodPos.getY(), foodPos.getZ());

            ItemStack handItem = ant.getMainHandItem();
            BlockPos surfacePos = ant.getFirstSurfacePos();
            double dist2Surface = ant.distanceToSqr(surfacePos.getX(), surfacePos.getY(), surfacePos.getZ());
            boolean isInTransition = ant.getIsInTransition();
            boolean isAboveground = ant.getIsAboveground();
            if(!isAboveground && isInTransition && !ant.getMainHandItem().isEmpty()){
                ant.setIsInTransition(false);
                isInTransition = false;
            }

            if (dist2Home < 10 && !ant.level.canSeeSky(ant.blockPosition())) {
                ant.setIsAboveground(false);
            }

            int antx = ant.blockPosition().getX();
            int anty = ant.blockPosition().getY();
            int antz = ant.blockPosition().getZ();

            List<BlockPos> keepPosList = new ArrayList<>();

            //manages container pos list
            if (dist2Home < 10) {
                if (containerPosList.isEmpty() || advancedCheckTimer>1000) {
                    containerPosList = AntUtils.findAllBlockPos(ModBlocks.LEAFY_CONTAINER_BLOCK.get(), homePos, 15, 6, ant.level);
                } else {
                    for (BlockPos tempPos : containerPosList) {
                        if (ant.level.getBlockState(tempPos).getBlock() == ModBlocks.LEAFY_CONTAINER_BLOCK.get()) {
                            keepPosList.add(tempPos);
                        }
                    }
                    containerPosList.clear();
                    containerPosList.addAll(keepPosList);
                    keepPosList.clear();
                }
            }

///////////////////////////////////////////////////////////////////////go down
            if (!handItem.isEmpty() && ant.getIsAboveground()) {


                //if first stage is not over, it begins executing first stage
                if (isAboveground && !isInTransition && (ant.getHasCheckedHome() == 0 || dist2Home > 30)) {
                    if (dist2Surface < 15) {
                        ant.setIsInTransition(true);
                        ant.setIsAboveground(true);
                    } else {
                        ant.getNavigation().moveTo(surfacePos.getX(), surfacePos.getY(), surfacePos.getZ(), 1);
                    }
                }

                //if first stage is over, begin second stage
                if ((isAboveground && isInTransition) || (ant.getFirstSurfacePos() == BlockPos.ZERO)) {
                    if (dist2Home < 10) {
                        ant.setIsInTransition(false);
                        ant.setIsAboveground(false);
                        ant.getNavigation().isDone();
                        ant.setHasCheckedHome(1);
                        super.stop();
                    } else {
                        ant.getNavigation().moveTo(homePos.getX(), homePos.getY(), homePos.getZ(), 1);
                    }
                }
            }
///////////////////////////////////////////////////////////////////////go up
            if (!isAboveground && handItem.isEmpty() && surfacePos != BlockPos.ZERO && ant.getHunger() >= 40000) {
                ant.walkTo(surfacePos, 1, 5d);
                if (dist2Surface < 10) {
                    ant.setIsInTransition(false);
                    ant.setIsAboveground(true);
                    ant.getNavigation().isDone();
                    if (foodPosList.isEmpty()) {
                        foodPosList = AntUtils.findAllSnippableBlockPos(ant.blockPosition(), 15, 10, ant);
                    } else {
                        for (BlockPos tempPos : foodPosList) {
                            if (AntUtils.shouldSnip(tempPos, ant.getLevel())) {
                                keepPosList.add(tempPos);
                            }
                        }
                        foodPosList.clear();
                        foodPosList.addAll(keepPosList);
                        keepPosList.clear();
                    }
                }
            }
            else if(!isAboveground && handItem.isEmpty() && surfacePos != BlockPos.ZERO && ant.getHunger() < 40000){
                ant.setWorkingStage(0);
                super.stop();
            }
///////////////////////////////////////////////////////////////////////find & place in container
            if (!handItem.isEmpty() && (dist2Home < 16 || ant.getHasCheckedHome() == 1)) {

                BlockPos containerPos = BlockPos.ZERO;
                for (BlockPos pPos : containerPosList) {
                    boolean closerThan = AntUtils.getDist(pPos, ant.blockPosition()) < AntUtils.getDist(containerPos, ant.blockPosition());
                    if (AntUtils.canReach(ant, pPos) && closerThan && AntUtils.canAddItem(ant.getMainHandItem(), ant.level.getBlockEntity(pPos))) {
                        containerPos = pPos;
                    } else {
                        if (containerPosList.isEmpty()) {
                            ant.setWorkingStage(0);
                            containerPos = BlockPos.ZERO;
                        }
                    }
                }
                ////////////////////////////////////// places item in container
                if(containerPos != BlockPos.ZERO) {
                    ant.getNavigation().moveTo(containerPos.getX(), containerPos.getY(), containerPos.getZ(), 1);
                    if (AntUtils.getDist(ant.blockPosition(), containerPos) < interactionDist) {
                        AntUtils.antAddItem((WorkerAnt) ant, ant.level.getBlockEntity(containerPos), ant.getMainHandItem());
                        ant.setHasCheckedHome(0);
                        ant.setHomeContainerPos(containerPos);
                    }
                }
            }


            ////manages aboveground stuff, like foraging

            //checks for items, for later decisions
            List<ItemEntity> itemList = ant.level.getEntitiesOfClass(ItemEntity.class, ant.getBoundingBox().inflate(8.0D, 6.0D, 8.0D));
            //looks for grass
            if (handItem.isEmpty() && isAboveground) {
                if(!AntUtils.shouldSnip(foodPos,ant.getLevel())){
                    foodPosList.remove(foodPos);
                    ant.setFoodLocation(AntUtils.findNearestBlockPos(ant,foodPosList, true));
                }

                //finds breakables and sets it as food position
                if (foodPosList.isEmpty() || foodPos == BlockPos.ZERO) {
                    foodPosList.clear();
                    foodPosList = AntUtils.findAllSnippableBlockPos(ant.blockPosition(), 5, 5, ant);
                    ant.setFoodLocation(AntUtils.findNearestBlockPos(ant, foodPosList, true));
                }
                // ik it can set to 0, that is intentional for wandering

                dist2food = AntUtils.getDist(foodPos, ant.blockPosition());
                foodPos = ant.getFoodLocation();


                if(foodPos != BlockPos.ZERO){ant.walkTo(foodPos,1, 5d);}


                //breaks it
                if (dist2food < interactionDist && foodPos != BlockPos.ZERO) {
                    if (AntUtils.shouldSnip(foodPos, ant.getLevel())) {
                        //for animation
                        ant.setSnippingAnimation(true);
                        if (destroyWait > 20) {
                            ant.level.destroyBlock(foodPos, true);
                            destroyWait = 0;
                            ant.setSnippingAnimation(false);
                            foodPosList.remove(foodPos);
                        }

                    } else{
                        foodPosList.remove(foodPos);
                        ant.setSnippingAnimation(false);
                        destroyWait = 0;
                    }
                }
            }
            //moves to, and picks up, items
            if(!itemList.isEmpty() && handItem.isEmpty() && itemList.get(0).blockPosition() != BlockPos.ZERO) {
                ant.getNavigation().moveTo(itemList.get(0),1);
                ant.getLookControl().setLookAt(itemList.get(0).getX(),itemList.get(0).getY(),itemList.get(0).getZ());
                ItemStack thisItem =  itemList.get(0).getItem();
                BlockPos antPos = ant.blockPosition();
                ant.setCanPickUpLoot(true);
                ant.canTakeItem(thisItem);
                ant.setSnippingAnimation(false);
                if (ant.level.canSeeSky(antPos) || (dist2Home>40&&dist2Surface>40)){ant.setFoodLocation(ant.blockPosition());}
                else if(!ant.level.canSeeSky(antPos)){ant.setIsInTransition(true);}
            }
            //moves to food pos, aka grass or items it found previously
            else if(dist2food >= 0 && handItem.isEmpty() && foodPos != BlockPos.ZERO && !hasCheckedFPos && isAboveground && ant.getHunger() > 40000 && !isInTransition){
                ant.walkTo(foodPos,1, 3d);
                ant.getLookControl().setLookAt(foodPos.getX(),foodPos.getY(),foodPos.getZ());
                wanderWait = 0;
            }
            //otherwise walks randomly
            if((hasCheckedFPos || ant.getFirstSurfacePos()==BlockPos.ZERO || foodPos==BlockPos.ZERO || ant.getHasCheckedHome()==1)) {
                AntUtils.wanderRandomly(ant);
            }
            //sets food pos of successful ants
            if (!ant.getIsCompetent() && (ant.getFoodLocation() == BlockPos.ZERO || ant.getFirstSurfacePos() == BlockPos.ZERO)){
                List<? extends WorkerAnt> antList = ant.level.getEntitiesOfClass(WorkerAnt.class, ant.getBoundingBox().inflate(32.0D, 16.0D, 32.0D));
                WorkerAnt ant1 = null;

                for(WorkerAnt ant2 : antList) {
                    //&& ant1.getThisColonyID() == ant.getThisColonyID()
                    if (ant2.getIsCompetent()) {
                        double d1 = ant.distanceToSqr(ant2);
                        if (d1 < 254) {
                            ant1 = ant2;
                        }
                    }
                }
                if (ant1 != null) {
                    ant.setFoodLocation(ant1.getFoodLocation());
                    ant.setFirstSurfacePos(ant1.getFirstSurfacePos());
                }
            }
            aiTimer = 0;
        }
        if(ant.getLastHurtByMob() != null){
            if(ant.getLastHurtByMob().isAlive()) {
                ant.setWorkingStage(1);
                super.stop();
            }
            else{ant.setLastHurtByMob(null);}
        }
        if(ant.getHunger() < minAllowedHunger && AntUtils.getDist(ant.blockPosition(),ant.getHomeContainerPos())<15){
            ant.setWorkingStage(0);
            super.stop();
        }*/
    }
}

