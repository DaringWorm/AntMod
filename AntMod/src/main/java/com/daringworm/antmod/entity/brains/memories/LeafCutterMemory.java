package com.daringworm.antmod.entity.brains.memories;

import com.daringworm.antmod.colony.misc.BlockPosStringifier;
import com.daringworm.antmod.entity.Ant;
import com.daringworm.antmod.entity.brains.parts.WorkingStages;
import com.daringworm.antmod.entity.custom.QueenAnt;
import com.daringworm.antmod.colony.AntColony;
import com.daringworm.antmod.colony.misc.PosSpherePair;
import com.daringworm.antmod.mixin.tomixin.ServerLevelUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Predicate;

public class LeafCutterMemory {

    public boolean shouldRunBrain = true;

    public int workingStage = WorkingStages.SCOUTING;
    public int breakingProgress;
    public int cID;
    public String roomID;
    public int navDelay;
    private int excavationStepAt;
    public int hungerLevel;
    public int nearbyItemCount;

    public BlockPos homeContainerPos;
    public BlockPos surfacePos;
    public BlockPos interestPos = BlockPos.ZERO;
    public BlockPos foodPos = BlockPos.ZERO;
    

    public int braincellStage;
    public String cellToRun;
    public String errorAlertString = "";


    public Player[] tradingWith;

    public ArrayList<PosSpherePair> excavationListRAW = new ArrayList<>();
    public ArrayList<BlockPos> excavationListCooked = new ArrayList<>();
    public HashSet<BlockPos> fungusPosSet = new HashSet<>();
    public ArrayList<BlockPos> goUndergroundList = new ArrayList<>();
    public HashSet<BlockPos> containerPosSet = new HashSet<>();

    // TODO remove the setWorkingStage on mem load
    public LeafCutterMemory(Ant pAnt){
        this.cID = pAnt.getColonyID();
        this.hungerLevel = pAnt.getHunger();
        this.homeContainerPos = pAnt.getHomeContainerPos();
        this.workingStage = pAnt.getWorkingStage();
        this.breakingProgress = 0;
        this.surfacePos = BlockPos.ZERO;
        this.interestPos = BlockPos.ZERO;
        this.roomID = "";
    }

    public LeafCutterMemory(CompoundTag tag, Ant pAnt){
        if(tag.isEmpty()){
            new LeafCutterMemory(pAnt);
        }
        else{
            this.braincellStage = tag.getInt("Braincell_stage");
            this.workingStage = tag.getInt("Working_stage");
            this.cID = tag.getInt("Colony_ID");
            this.roomID = tag.getString("Room_ID");
            //this.colonyBranch = pAnt.getColony().tunnels.getSubBranch(tag.getString("Home_room_ID"));
            this.hungerLevel = tag.getInt("Hunger_level");
            this.errorAlertString = tag.getString("Error_Alert_String");
            this.nearbyItemCount = tag.getInt("Nearby_item_count");
            this.breakingProgress = tag.getInt("Block_breaking_progress");
            //this.shouldRunBrain = tag.getBoolean("Should_run_AI");


            this.interestPos = BlockPosStringifier.getPosForTag(tag.getCompound("Interest_pos"));
            this.homeContainerPos = BlockPosStringifier.getPosForTag(tag.getCompound("Home_pos"));
            this.surfacePos = BlockPosStringifier.getPosForTag(tag.getCompound("Surface_pos"));
            this.foodPos = BlockPosStringifier.getPosForTag(tag.getCompound("Food_pos"));


            this.excavationListCooked = BlockPosStringifier.getPosesForTag(tag.getCompound("Excavation_poses"));
            this.goUndergroundList = BlockPosStringifier.getPosesForTag(tag.getCompound("Go_underground_list"));
            this.fungusPosSet = new HashSet<>(BlockPosStringifier.getPosesForTag(tag.getCompound("Fungus_list")));
            this.containerPosSet = new HashSet<>(BlockPosStringifier.getPosesForTag(tag.getCompound("Container_list")));
        }
    }

    public void softRefresh(Ant pAnt, ServerLevel pSLevel){
        if(pAnt.getLevel() instanceof ClientLevel){return;}

        //pAnt.setWalkingCooldown(pAnt.getWalkingCooldown()+1);

        if(pAnt.getWorkingStage() == WorkingStages.WANDERING){
            pAnt.setWorkingStage(WorkingStages.SCOUTING);
        }

        if(pAnt.getHomeContainerPos() == BlockPos.ZERO){pAnt.setHomeContainerPos(pAnt.blockPosition());}


        AntColony colony = ((ServerLevelUtil) (pSLevel)).getColonyWithID(cID);

        this.navDelay++;

        if(pAnt.getMainHandItem().isEmpty() && workingStage != WorkingStages.ATTACKING && workingStage != WorkingStages.LATCHING){
            this.nearbyItemCount = (pAnt.getLevel().getEntitiesOfClass(ItemEntity.class,pAnt.getBoundingBox().inflate(6))).size();
        }


        if(colony == null && pAnt instanceof QueenAnt){
            colony = new AntColony(pSLevel, this.cID, this.homeContainerPos);
            ((ServerLevelUtil) (pSLevel)).addColonyToList(colony);
        }


        if(colony != null) {
            if((pAnt.getRoomID() == null || Objects.equals(pAnt.getRoomID(), "")) && colony.tunnels != null){
                this.roomID = (colony.tunnels.getNearestBranchID(pAnt.getHomeContainerPos()));
                this.goUndergroundList = colony.tunnels.getPosesToBranch(roomID);
            }
        }

        if(this.workingStage == WorkingStages.ATTACKING || this.workingStage == WorkingStages.LATCHING){
            if(pAnt.getTarget() == null || !pAnt.getTarget().isAlive()){
                this.workingStage = WorkingStages.SCOUTING;
                pAnt.setWorkingStage(WorkingStages.SCOUTING);
            }
        }

        if(this.workingStage == WorkingStages.SCOUTING && (!pAnt.getMainHandItem().isEmpty() || nearbyItemCount > 0)){
            pAnt.setWorkingStage(WorkingStages.FORAGING);
            this.workingStage = WorkingStages.FORAGING;
        }

        /*if(this.workingStage == WorkingStages.FORAGING && (pAnt.getMainHandItem().isEmpty() || pAnt.getInterestPos() == null || pAnt.getInterestPos() == BlockPos.ZERO)){
            pAnt.setWorkingStage(WorkingStages.SCOUTING);
        }*/

        if(this.surfacePos == null || this.surfacePos == BlockPos.ZERO){
            this.surfacePos = pAnt.getSurfacePos();
            if(this.surfacePos == null || this.surfacePos == BlockPos.ZERO){
                if(colony != null) {
                    this.surfacePos = colony.startPos;
                    pAnt.setFirstSurfacePos(this.surfacePos);
                }
            }
        }


        if(pAnt.getWorkingStage() == WorkingStages.FORAGING && nearbyItemCount <= 0 && pAnt.getMainHandItem().isEmpty()
               && interestPos == BlockPos.ZERO){
            this.workingStage = WorkingStages.SCOUTING;
            pAnt.setWorkingStage(WorkingStages.SCOUTING);
            this.navDelay = 50;
            pAnt.getNavigation().stop();
        }

    }


    public void refreshExcavationList(Ant pAnt, ServerLevel pSLevel){
        return;
    }

    public CompoundTag saveToTag(){
        CompoundTag tag = new CompoundTag();




        return tag;
    }
}
