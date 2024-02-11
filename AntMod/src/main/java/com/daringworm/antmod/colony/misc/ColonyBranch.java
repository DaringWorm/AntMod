package com.daringworm.antmod.colony.misc;

import com.daringworm.antmod.goals.AntUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;


public class ColonyBranch {

    private final BlockPos originPos;
    private final int facingDegrees;
    public boolean hasRoom;
    private final int roomSize;
    public final String branchID;
    public ArrayList<ColonyBranch> branches = new ArrayList<>();

    public ColonyBranch(BlockPos startPos, int facing, ArrayList<ColonyBranch> branches, boolean hasRoom, String roomID){
        this.originPos = startPos;
        this.branches = branches;
        this.facingDegrees = facing;
        this.hasRoom = hasRoom;
        this.roomSize = 12;
        this.branchID = roomID;
    }

    public ColonyBranch(JsonObject json){

        JsonObject posJ = json.getAsJsonObject("pos");
        this.originPos = BlockPosStringifier.posFromString(posJ);

        JsonObject j = json.getAsJsonObject("dir");
        this.facingDegrees = json.get("direction").getAsInt();
        this.hasRoom = json.get("has_room").getAsBoolean();
        this.roomSize = json.get("room_size").getAsInt();
        this.branchID = json.get("branch_id").getAsString();

        JsonArray j1 = json.getAsJsonArray("branches");
        for(JsonElement element : j1){
            this.branches.add(new ColonyBranch(element.getAsJsonObject()));
        }
    }


    public JsonObject toJson(){
        JsonObject masterJ = new JsonObject();

        masterJ.add("pos", BlockPosStringifier.jsonFromPos(this.originPos));
        masterJ.addProperty("has_room", this.hasRoom);
        masterJ.addProperty("room_size", this.roomSize);
        masterJ.addProperty("branch_id", this.branchID);

        masterJ.addProperty("direction", this.facingDegrees);

        JsonArray branchesJ = new JsonArray();
        for(ColonyBranch tempBranch : branches){
            branchesJ.add(tempBranch.toJson());
        }
        masterJ.add("branches", branchesJ);

        return masterJ;
    }

    public ColonyBranch getSubBranch(String id){
        if(id == null || id.isEmpty()){
            return this;
        }
        else{
            ArrayList<ColonyBranch> l = getBranchesForID(id);
            if(!l.isEmpty()){
                return l.get(0);
            }
        }
        return this;
    }

    public ArrayList<ColonyBranch> getBranchesForID(String id){
        ArrayList<ColonyBranch> returnList = new ArrayList<>();
        for(ColonyBranch branch : this.branches){
            if(Objects.equals(branch.branchID, id)){
                returnList.add(branch);
            }
            returnList.addAll(branch.getBranchesForID(id));
        }
        return returnList;
    }

    public BlockPos getPos(){return this.originPos;}

    public BlockPos nextBranchPos(int facingDegrees, double length, int yOff){
        int deg = facingDegrees % 360;
        double rad = Math.toRadians(deg);

        int xOff = (int)Math.round(length*Math.cos(rad));
        int zOff = (int)Math.round(length*Math.sin(rad));

        return this.originPos.offset(xOff, yOff, zOff);
    }

    public void generateNextBranch(int length, int yOffset, boolean hasRoom){
        this.branches.add(new ColonyBranch(this.nextBranchPos(this.facingDegrees,length,yOffset),this.facingDegrees,new ArrayList<>(), hasRoom, this.branchID + "0"));
    }

    public void generateNextBranches(int numberPerStep, int degreesSpread, int steps, int yOffset, double length, boolean haveRooms){
        if(!this.branches.isEmpty()){
            for (ColonyBranch branch : this.branches) {
                branch.generateNextBranches(numberPerStep, degreesSpread, steps, yOffset, length, haveRooms);
            }
            return;
        }
        if(steps > 0 && numberPerStep > 0) {
            int i = -1;
            int accumulatedDeg = 0;
            int numLeft = numberPerStep-1;
            if(numberPerStep%2 == 0){
                int newDir = this.facingDegrees+(degreesSpread/2);
                this.branches.add(new ColonyBranch(nextBranchPos(newDir, length, yOffset), newDir, new ArrayList<>(),haveRooms,this.branchID+"0"));
                while(numLeft>0){
                    accumulatedDeg += degreesSpread;
                    newDir = newDir + (accumulatedDeg * i);
                    i= -i;
                    this.branches.add(new ColonyBranch(nextBranchPos(newDir, length, yOffset), newDir, new ArrayList<>(),haveRooms, this.branchID + numLeft));
                    numLeft--;
                }
            }
            else{
                this.branches.add(new ColonyBranch(nextBranchPos(this.facingDegrees, length, yOffset), this.facingDegrees, new ArrayList<>(),haveRooms, this.branchID+"0"));
                int newDir = this.facingDegrees;
                while(numLeft>0){
                    accumulatedDeg += degreesSpread;
                    newDir = newDir + (accumulatedDeg * i);
                    i= -i;
                    this.branches.add(new ColonyBranch(nextBranchPos(newDir, length, yOffset), newDir, new ArrayList<>(),haveRooms, this.branchID + numLeft));
                    numLeft--;
                }
            }

            for (ColonyBranch branch : this.branches) {
                branch.generateNextBranches(numberPerStep, degreesSpread/numberPerStep, steps - 1, yOffset, length, haveRooms);
            }
        }
    }

    public ArrayList<PosSpherePair> generateBranchBlueprint(double passageWidth, double roomHeight, int roomSize){
        ArrayList<PosSpherePair> returnList = new ArrayList<>();

        for(ColonyBranch branch : this.branches){
            returnList.addAll(ColonyGenUtils.generatePassageBlueprint(
                    new PosPair(this.getPos(),branch.getPos()),passageWidth));

            if(branch.hasRoom){
                returnList.addAll(ColonyGenUtils.generateRoomBlueprint(roomHeight,roomSize,branch.getPos(),AntUtils.randFromPos(this.getPos())));
            }
            returnList.addAll(branch.generateBranchBlueprint(passageWidth,roomHeight, roomSize));
        }

        return returnList;
    }

    public ArrayList<BlockPos> listRoomPoses(){
        ArrayList<BlockPos> returnList = new ArrayList<>();
        if(this.hasRoom){returnList.add(this.originPos);}
        for(ColonyBranch branch : this.branches){
            returnList.addAll(branch.listRoomPoses());
        }
        return returnList;
    }

    public ArrayList<BlockPos> listBranchPoses(){
        ArrayList<BlockPos> returnList = new ArrayList<>();
        returnList.add(this.originPos);
        for(ColonyBranch branch : this.branches){
            returnList.addAll(branch.listBranchPoses());
        }
        return returnList;
    }

    public ArrayList<String> listBranchIDs(){
        ArrayList<String> returnList = new ArrayList<>();
        returnList.add(this.branchID);
        for(ColonyBranch branch : this.branches){
            returnList.addAll(branch.listBranchIDs());
        }
        return returnList;
    }

    public String getNearestBranchID(BlockPos pos){
        ArrayList<BlockPos> posList= this.listBranchPoses();

        BlockPos closestRoomPos = AntUtils.findNearestBlockPos(pos, posList);
        int index = posList.indexOf(closestRoomPos);
        return this.listBranchIDs().get(index);
    }

    public ArrayList<BlockPos> getPosesToBranch(String branchID){
        ArrayList<BlockPos> returnList = new ArrayList<>();

        for(int i = 1; i <= branchID.length(); i++){
            returnList.add(this.getSubBranch(branchID.substring(0,i)).getPos());
        }

        return returnList;
    }
}