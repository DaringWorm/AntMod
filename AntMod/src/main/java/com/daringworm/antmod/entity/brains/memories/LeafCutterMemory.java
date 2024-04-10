package com.daringworm.antmod.entity.brains.memories;

import com.daringworm.antmod.colony.misc.ColonyBranch;
import com.daringworm.antmod.entity.Ant;
import com.daringworm.antmod.entity.brains.parts.Braincell;
import com.daringworm.antmod.entity.brains.parts.WorkingStages;
import com.daringworm.antmod.entity.custom.AntScentCloud;
import com.daringworm.antmod.entity.custom.QueenAnt;
import com.daringworm.antmod.colony.AntColony;
import com.daringworm.antmod.colony.misc.PosSpherePair;
import com.daringworm.antmod.mixin.tomixin.ServerLevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Predicate;

public class LeafCutterMemory {
    public int workingStage;
    public int breakingProgress;
    public int cID;
    public int navDelay;
    private int excavationStepAt;
    public int hungerLevel;
    public BlockPos homePos;
    public BlockPos surfacePos;

    private ServerLevel pSLevel;

    public int braincellStage = 1;
    public Braincell cellToRun;
    public String errorAlertString;

    public BlockPos interestPos = BlockPos.ZERO;
    public BlockPos containerPos = BlockPos.ZERO;
    public BlockPos foodPos = BlockPos.ZERO;
    public Player[] tradingWith;

    public ArrayList<PosSpherePair> excavationListRAW = new ArrayList<>();
    public ArrayList<BlockPos> excavationListCooked = new ArrayList<>();
    public HashSet<BlockPos> fungusPosSet = new HashSet<>();
    public ArrayList<BlockPos> containerPosSet = new ArrayList<>();
    public ArrayList<ItemEntity> foundItemList = new ArrayList<>();
    public ArrayList<BlockPos> goUndergroundList = new ArrayList<>();

    public ColonyBranch colonyBranch;

    public Predicate<BlockPos> foodStatePredicate(Ant pAnt){
        return pPos -> {
            BlockState pState = pAnt.getLevel().getBlockState(pPos);
            return pState.getRenderShape()
                != RenderShape.INVISIBLE && pState.getDestroySpeed(pAnt.level, pPos) < 0.1
                && !pState.canOcclude() && pState.getBlock().getClass() != NetherPortalBlock.class;
        };
    }
    public Entity passiveTarget;

    // TODO remove the setWorkingStage on mem load
    public LeafCutterMemory(Ant pAnt){
        this.cID = pAnt.getColonyID();
        this.hungerLevel = pAnt.getHunger();
        this.homePos = pAnt.getHomeContainerPos();
        this.pSLevel = (ServerLevel)pAnt.getLevel();
        //pAnt.setWorkingStage(WorkingStages.SCOUTING);
        this.workingStage = pAnt.getWorkingStage();
        this.breakingProgress = 0;
        this.containerPos = pAnt.getHomeContainerPos();
    }

    public LeafCutterMemory(String str, Ant pAnt){
        if(Objects.equals(str, "null")){
            new LeafCutterMemory(pAnt);
        }
    }

    public void softRefresh(Ant pAnt){
        this.containerPos = pAnt.getHomeContainerPos();
        this.pSLevel = (ServerLevel) pAnt.getLevel();
        assert pAnt.getLevel() instanceof ServerLevel;

        this.cID = pAnt.getColonyID();
        this.navDelay++;
        this.workingStage = (pAnt.getWorkingStage());
        this.hungerLevel = pAnt.getHunger();
        AntColony colony = ((ServerLevelUtil) (pSLevel)).getColonyWithID(cID);

        if(pAnt.getMainHandItem().isEmpty() && workingStage != WorkingStages.ATTACKING && workingStage != WorkingStages.LATCHING){
            this.foundItemList = ((ArrayList<ItemEntity>) pAnt.getLevel().getEntitiesOfClass(ItemEntity.class,pAnt.getBoundingBox().inflate(6)));
        }

        if(pAnt.getHomeContainerPos() == BlockPos.ZERO){pAnt.setHomeContainerPos(pAnt.blockPosition());}
        this.homePos = pAnt.getHomeContainerPos();

        if(pAnt instanceof QueenAnt && colony == null){
            colony = new AntColony(pSLevel, this.cID, this.homePos);
            ((ServerLevelUtil) (pSLevel)).addColonyToList(colony);
        }


        if(colony != null) {
            if(this.colonyBranch == null) {
                this.colonyBranch = colony.tunnels;
            }
            if (this.colonyBranch != null && (this.goUndergroundList.isEmpty() || Objects.equals(pAnt.getRoomID(),""))) {
                if(this.colonyBranch.getNearestBranchID(this.homePos) != null) {
                    pAnt.setRoomID(this.colonyBranch.getNearestBranchID(this.homePos));
                }
                if(pAnt.getRoomID() != null && this.goUndergroundList.isEmpty()) {
                    this.goUndergroundList = (this.colonyBranch.getPosesToBranch(pAnt.getRoomID()));
                }
            }
        }

        if(this.workingStage == WorkingStages.ATTACKING || this.workingStage == WorkingStages.LATCHING){
            if(pAnt.getTarget() == null || !pAnt.getTarget().isAlive()){
                this.workingStage = WorkingStages.SCOUTING;
                pAnt.setWorkingStage(WorkingStages.SCOUTING);
            }
        }

        if(this.workingStage == WorkingStages.SCOUTING && (!pAnt.getMainHandItem().isEmpty() || !foundItemList.isEmpty())){
            pAnt.setWorkingStage(WorkingStages.FORAGING);
            this.workingStage = WorkingStages.FORAGING;
        }

        /*if(this.workingStage == WorkingStages.FORAGING && (pAnt.getMainHandItem().isEmpty() || pAnt.memory.interestPos == null || pAnt.memory.interestPos == BlockPos.ZERO)){
            pAnt.setWorkingStage(WorkingStages.SCOUTING);
        }*/

        if(this.surfacePos == null || this.surfacePos == BlockPos.ZERO){
            this.surfacePos = pAnt.getFirstSurfacePos();
            if(this.surfacePos == null || this.surfacePos == BlockPos.ZERO){
                if(colony != null) {
                    this.surfacePos = colony.startPos;
                    pAnt.setFirstSurfacePos(this.surfacePos);
                }
            }
        }

        boolean noCloud = pAnt.getLevel().getEntitiesOfClass(AntScentCloud.class,pAnt.getBoundingBox().inflate(8)).isEmpty();

        if(pAnt.getWorkingStage() == WorkingStages.FORAGING && foundItemList.isEmpty() && pAnt.getMainHandItem().isEmpty()
                && noCloud && interestPos == BlockPos.ZERO){
            this.workingStage = WorkingStages.SCOUTING;
            pAnt.setWorkingStage(WorkingStages.SCOUTING);
            this.interestPos = BlockPos.ZERO;
        }

    }


    public void refreshExcavationList(Ant pAnt){
        if(excavationListCooked.isEmpty()) {
            AntColony pColony = ((ServerLevelUtil) (pSLevel)).getColonyWithID(cID);
            if (pColony != null) {
                this.excavationStepAt = this.excavationStepAt +this.excavationListRAW.size();
                this.excavationListRAW = pColony.getNextExcavationSteps(excavationStepAt);

                for (PosSpherePair sphere : excavationListRAW) {
                    excavationListCooked.removeAll(sphere.getBlockPoses(pAnt.level));
                    excavationListCooked.addAll(sphere.getBlockPoses(pAnt.level));
                }
            }
            else {
                ((ServerLevelUtil) (pSLevel)).addColonyToList(new AntColony(pSLevel, this.cID, this.homePos));
            }
        }
    }

    public String saveToString(){
        return "null";
    }
}
