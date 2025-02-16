package com.daringworm.antmod.colony.misc;

import com.daringworm.antmod.util.AntUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;


public class ColonyBranch {

    private final BlockPos originPos;
    private final int facingDegrees;
    public boolean hasRoom;
    public final int roomSize;
    public String branchID;
    public ArrayList<ColonyBranch> branches = new ArrayList<>();

    public ColonyBranch(BlockPos startPos, int facing, boolean hasRoom, String roomID){
        this.originPos = startPos;
        this.facingDegrees = facing;
        this.hasRoom = hasRoom;
        this.roomSize = 12;
        this.branchID = roomID;
    }

    public ColonyBranch(BlockPos startPos, int facing, boolean hasRoom, int roomSize, String roomID){
        this.originPos = startPos;
        this.facingDegrees = facing;
        this.hasRoom = hasRoom;
        this.roomSize = roomSize;
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


    public int getDegFacing(){return this.facingDegrees;}

    public ColonyBranch updateID(String idToAddStart, String idToAddEnd){
        this.branchID = idToAddStart + this.branchID + idToAddEnd;
        ArrayList<ColonyBranch> newList = new ArrayList<>();
        for(ColonyBranch branch : this.branches){
            newList.add(branch.updateID(idToAddStart,idToAddEnd));
        }
        this.branches = newList;
        return this;
    }

    public static BlockPos nextBranchPos(BlockPos startPos, int facingDegrees, double length, int yOff){
        int deg = facingDegrees % 360;
        double rad = Math.toRadians(deg);

        int xOff = (int)Math.round(length*Math.cos(rad));
        int zOff = (int)Math.round(length*Math.sin(rad));

        return startPos.offset(xOff, yOff, zOff);
    }

    public void generateNextBranch(int length, int yOffset, boolean hasRoom){
        this.branches.add(new ColonyBranch(nextBranchPos(this.getPos(), this.facingDegrees,length,yOffset),this.facingDegrees, hasRoom, this.branchID + "0"));
    }

    public void generateNextBranch(int length, int yOffset, boolean hasRoom, int roomSize){
        this.branches.add(new ColonyBranch(nextBranchPos(this.getPos(), this.facingDegrees,length,yOffset),this.facingDegrees, hasRoom, this.branchID + "0"));
    }

    public void generateNextBranches(int numberPerStep, int degreesSpread, int steps, int minYOff, int maxYOff, double length, boolean haveRooms, int[] roomSizes){
        Random random = AntUtils.randFromPos(this.getPos());
        if(!this.branches.isEmpty()){
            for (ColonyBranch branch : this.branches) {
                branch.generateNextBranches(numberPerStep, degreesSpread, steps, minYOff, maxYOff, length, haveRooms, roomSizes);
            }
            return;
        }
        if(steps > 0 && numberPerStep > 0) {
            int i = -1;
            int accumulatedDeg = 0;
            int numLeft = numberPerStep-1;
            if(numberPerStep%2 == 0){
                int newDir = this.facingDegrees+(degreesSpread/2);
                this.branches.add(new ColonyBranch(nextBranchPos(this.getPos(), newDir, length, numberBetween(random, minYOff, maxYOff)), newDir, haveRooms, roomSizes[random.nextInt(roomSizes.length)],this.branchID+"0"));
                while(numLeft>0){
                    accumulatedDeg += degreesSpread;
                    newDir = newDir + (accumulatedDeg * i);
                    i= -i;
                    this.branches.add(new ColonyBranch(nextBranchPos(this.getPos(), newDir, length, numberBetween(random, minYOff, maxYOff)), newDir, haveRooms, roomSizes[random.nextInt(roomSizes.length)], this.branchID + numLeft));
                    numLeft--;
                }
            }
            else{
                this.branches.add(new ColonyBranch(nextBranchPos(this.getPos(), this.facingDegrees, length, numberBetween(random, minYOff, maxYOff)), this.facingDegrees, haveRooms, roomSizes[random.nextInt(roomSizes.length)], this.branchID+"0"));
                int newDir = this.facingDegrees;
                while(numLeft>0){
                    accumulatedDeg += degreesSpread;
                    newDir = newDir + (accumulatedDeg * i);
                    i= -i;
                    this.branches.add(new ColonyBranch(nextBranchPos(this.getPos(), newDir, length, numberBetween(random, minYOff, maxYOff)), newDir, haveRooms, roomSizes[random.nextInt(roomSizes.length)], this.branchID + numLeft));
                    numLeft--;
                }
            }

            for (ColonyBranch branch : this.branches) {
                branch.generateNextBranches(numberPerStep, degreesSpread/numberPerStep, steps - 1, minYOff, maxYOff, length, haveRooms, roomSizes);
            }
        }
    }


    private int numberBetween(Random random, int min, int max){
        return (min+random.nextInt((Math.abs(max-min))));
    }

    /****/
    public ArrayList<PosSpherePair> generateLimitedBlueprint(double passageWidth, double roomHeight, int roomSize, int steps, boolean wontReplaceAir){
        ArrayList<PosSpherePair> returnList = new ArrayList<>();

        for(ColonyBranch branch : this.branches){
            returnList.addAll(ColonyGenUtils.generatePassageBlueprint(new PosPair(this.getPos(),branch.getPos()),passageWidth, wontReplaceAir));

            if(branch.hasRoom){
                returnList.addAll(ColonyGenUtils.generateRoomBlueprint(branch.roomSize/2f,branch.roomSize,branch.getPos(),AntUtils.randFromPos(this.getPos())));
            }
            if(steps > 0) {
                returnList.addAll(branch.generateLimitedBlueprint(passageWidth, roomHeight, roomSize, steps - 1, wontReplaceAir));
            }
        }

        return returnList;
    }

    /**Returns the spheres for the branch tunnel, optionally its room, and the same for all subbranches.**/
    public ArrayList<PosSpherePair> generateBranchBlueprint(double passageWidth, double roomHeight, int roomSize){
        ArrayList<PosSpherePair> returnList = new ArrayList<>();

        for(ColonyBranch branch : this.branches){
            returnList.addAll(ColonyGenUtils.generatePassageBlueprint(new PosPair(this.getPos(),branch.getPos()),passageWidth, false));

            if(branch.hasRoom){
                returnList.addAll(ColonyGenUtils.generateRoomBlueprint(roomHeight,branch.roomSize,branch.getPos(),AntUtils.randFromPos(this.getPos())));
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
        if(index < 0){return null;}
        return this.listBranchIDs().get(index);
    }

    /**Only works if the branch ID points to this branch or a valid child.**/
    public ArrayList<BlockPos> getPosesToBranch(String branchID){
        ArrayList<BlockPos> returnList = new ArrayList<>();

        if(!this.listBranchIDs().contains(branchID)){
            return returnList;
        }

        for(int i = 1; i <= branchID.length(); i++){
            returnList.add(this.getSubBranch(branchID.substring(0,i)).getPos());
        }

        return returnList;
    }

    /**Gets the list of positions leading from any child branch to any other child branch.
     * Starts at the first branch, given by its ID, goes back to the common ancestor with the second branch, also given by its ID,
     * and then goes to the second branch.**/
    public ArrayList<BlockPos> getPosesFromBranchToBranch(String startID, String endID){

        if(startID.equals(endID)){return new ArrayList<>(List.of(getSubBranch(startID).getPos()));}

        ArrayList<BlockPos> startPosList = getPosesToBranch(startID);
        ArrayList<BlockPos> endPosList = getPosesToBranch(endID);

        //It needs to check the next one down the list so that the parent is preserved.
        while(startPosList.size() > 1 && endPosList.size() > 1 && startPosList.get(1) == endPosList.get(1)){
            startPosList.remove(0);
            endPosList.remove(0);
        }
        startPosList.remove(0);

        ArrayList<BlockPos> returnList = new ArrayList<>();

        for(int i = startPosList.size()-1; i > -1; i--){
            returnList.add(startPosList.get(i));
        }
        returnList.addAll(endPosList);

        return returnList;
    }
}