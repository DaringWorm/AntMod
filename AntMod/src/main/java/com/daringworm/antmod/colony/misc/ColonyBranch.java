package com.daringworm.antmod.colony.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;


public class ColonyBranch {

    private final BlockPos originPos;
    private final Direction direction;
    public boolean hasRoom;
    private final int roomSize;
    public ArrayList<ColonyBranch> branches = new ArrayList<>();

    public ColonyBranch(BlockPos startPos, Direction dir, ArrayList<ColonyBranch> branches){
        this.originPos = startPos;
        this.branches = branches;
        this.direction = dir;
        this.hasRoom = false;
        this.roomSize = 12;
    }

    public ColonyBranch(JsonObject json){

        JsonObject posJ = json.getAsJsonObject("pos");
        this.originPos = BlockPosStringifier.posFromString(posJ);

        JsonObject j = json.getAsJsonObject("dir");
        this.direction = Direction.byName(json.get("direction").getAsString());
        this.hasRoom = json.get("has_room").getAsBoolean();
        this.roomSize = json.get("room_size").getAsInt();

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

        masterJ.addProperty("direction", this.direction.toString());

        JsonArray branchesJ = new JsonArray();
        for(ColonyBranch tempBranch : branches){
            branchesJ.add(tempBranch.toJson());
        }
        masterJ.add("branches", branchesJ);

        return masterJ;
    }

    public ColonyBranch getSubBranch(String str){
        if(Objects.equals(str, "") || this.branches.isEmpty()){
            return this;
        }
        else{
            int c = str.charAt(0);
            return (c <= branches.size()) ? branches.get(c) : branches.get(0).getSubBranch(str.substring(1));
        }
    }

    public BlockPos getOriginPos(){return this.originPos;}

    public BlockPos generateNextBranchPos(Direction direction, int xzOff, int yOff){
        return this.originPos.relative(direction,xzOff).relative(Direction.Axis.Y,yOff);
    }

    public void generateNextBranch(int length, int yOffset, boolean hasRoom){
        this.branches.add(new ColonyBranch(this.generateNextBranchPos(this.direction,length,yOffset),this.direction,new ArrayList<>()));
    }

    public void generateNextBranches(int numberPerStep, int steps, int yOffset, int length, boolean haveRooms){
        if(steps>0) {

            this.branches.add(new ColonyBranch(generateNextBranchPos(this.direction, length, yOffset), this.direction, new ArrayList<>()));

            if(numberPerStep>1) {
                ArrayList<Direction> l = new ArrayList<>();
                l.add(this.direction.getClockWise());
                l.add(this.direction.getCounterClockWise());
                for(Direction dir : l){
                    this.branches.add(new ColonyBranch(generateNextBranchPos(dir, length, yOffset), dir, new ArrayList<>()));
                }
            }

            for (ColonyBranch branch : this.branches) {
                branch.generateNextBranches(numberPerStep, steps - 1, yOffset, length, haveRooms);
            }
        }
    }

    public ArrayList<PosSpherePair> generateBranchBlueprint(double passageWidth, double roomHeight){
        ArrayList<PosSpherePair> returnList = new ArrayList<>();
        



        return returnList;
    }

}
