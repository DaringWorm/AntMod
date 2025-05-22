package com.daringworm.antmod.colony.misc;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.block.custom.FungalCore;
import com.daringworm.antmod.util.AntUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.units.qual.A;

import java.util.*;
import java.util.function.Predicate;

/**
 * Current values:
 * has_room: boolean
 * room_size: float
 * direction: float
 * room_type: String -> "storage" || "fungus" || "queen" || "empty"
 * **/

public class ColonyBranch implements Branch<ColonyBranch>{

    private BlockPos position;
    private ColonyBranch parent;
    private ArrayList<ColonyBranch> children;
    private HashMap<String, String> values;

    public static ArrayList<ColonyBranch> testing = new ArrayList<>();

    public static ColonyBranch getNearest_testing(BlockPos pos){
        if(testing.isEmpty()){
            System.out.println("testing branches list is empty and was queried");
            return null;
        }
        ColonyBranch toReturn = testing.get(0);
        for(ColonyBranch tempBranch : testing){
            if(AntUtils.getDist(tempBranch.getPos(), pos) < AntUtils.getDist(toReturn.getPos(), pos)){
                toReturn = tempBranch;
            }
        }
        return toReturn;
    }


    public ColonyBranch(BlockPos pos, ArrayList<ColonyBranch> children, ColonyBranch parent, HashMap<String, String> values){
        this.position = pos;
        this.children = children;
        this.parent = parent;
        this.values = values;
        testing.add(this);
    }


    public ColonyBranch(BlockPos pos){
        this.position = pos;
        this.children = new ArrayList<>();
        this.parent = null;
        this.values = new HashMap<>();
        testing.add(this);
    }

    public ColonyBranch(JsonObject baseObj){
        this.position = BlockPosStringifier.posFromString(baseObj.get("start_pos").getAsJsonObject());
        this.values = new HashMap<>();
        this.children = new ArrayList<>();

        JsonObject valuesObj = baseObj.get("values").getAsJsonObject();

        for(Map.Entry<String, JsonElement> element : valuesObj.entrySet()){
            Map<String, String> values = this.values;
            String val = element.getValue().getAsString();

            this.values.put(element.getKey(), val);
        }



        for(JsonElement tempElement : baseObj.get("children").getAsJsonArray()){
            ColonyBranch newChild = new ColonyBranch(tempElement.getAsJsonObject());
            this.addChild(newChild);
        }
        testing.add(this);
    }

    public JsonObject toJson(){
        JsonObject returnObj = new JsonObject();
        JsonObject valuesObj = new JsonObject();
        JsonArray childrenObj = new JsonArray();

        for(String tempKey : this.getValues().keySet()){
            valuesObj.addProperty(tempKey, this.getValue(tempKey));
        }

        for(ColonyBranch tempBranch : this.getChildren()){
            childrenObj.add(tempBranch.toJson());
        }

        returnObj.add("values", valuesObj);
        returnObj.add("start_pos", BlockPosStringifier.jsonFromPos(this.getPos()));
        returnObj.add("children", childrenObj);

        return returnObj;
    }


    public ArrayList<PosSpherePair> getExcavationSpheres(){
        ArrayList<PosSpherePair> returnSpheres = (this.getParent() != null) ? ColonyGenUtils.generatePassageBlueprint(new PosPair(parent.getPos(), this.position), 1.7) : new ArrayList<>();

        if(!(this.values.get("has_room") == null || this.values.get("has_room").equals("false"))) {
            float roomSize = (!this.values.containsKey("room_size")) ? 2.85f : Float.parseFloat(this.values.get("room_size"));
            BlockPos centerPos = this.getPos().above((int) roomSize - 1);
            BlockPos tempPos = centerPos;
            Random rand = AntUtils.randFromPos(centerPos);
            for (int i = 0; i < roomSize; i++) {
                returnSpheres.add(new PosSpherePair(tempPos, roomSize));
                tempPos = centerPos.offset(rand.nextInt((int) roomSize) * (rand.nextBoolean() ? 1 : -1), 0, rand.nextInt((int) roomSize) * (rand.nextBoolean() ? 1 : -1));
            }
        }

        for(ColonyBranch tempBranch : this.children){
            returnSpheres.addAll(tempBranch.getExcavationSpheres());
        }

        return returnSpheres;
    }


    public void carve(ServerLevel pLevel){
        ArrayList<PosSpherePair> tunnelPoses = getExcavationSpheres();

        //this.newCarve(pLevel, pLevel.getRandom());
        //AntUtils.broadcastString(pLevel, "for " + this.getAllChildren().size());
        for(PosSpherePair tempSphere : tunnelPoses){
            tempSphere.setSphere(pLevel, ModBlocks.ANT_AIR.get(), ModBlocks.ANT_DIRT.get(), 1.8f);
        }

        for(ColonyBranch tempChild : this.children){
            tempChild.carve(pLevel);
        }

        decorateRoom(pLevel);
    }

    public void newCarve(ServerLevel pLevel, Random rand){
        if(this.getParent() != null) {
            Block innerBlock = ModBlocks.ANT_AIR.get();
            Block outerBlock = ModBlocks.ANT_DIRT.get();

            BlockPos start = this.getPos();
            BlockPos end = this.getParent().getPos();
            final double passageRadius = 1.45d;
            final double wallThickness = 2d;
            final int radiusInt = (int) Math.ceil(passageRadius + wallThickness);
            final Vec3 absVector = new Vec3(end.getX() - start.getX(), end.getY() - start.getY(), end.getZ() - start.getZ()).normalize();

            PosSpherePair startSphere = new PosSpherePair(start, passageRadius);
            startSphere.setSphere(pLevel, innerBlock, outerBlock, wallThickness);

            ArrayList<BlockPos> innerList = new ArrayList<>();
            ArrayList<BlockPos> outerList = new ArrayList<>();
            innerList.ensureCapacity(radiusInt * radiusInt * 4);
            outerList.ensureCapacity(radiusInt * 8);

            for (BlockPos tempPos : BlockPos.betweenClosed(new BlockPos(radiusInt, radiusInt, radiusInt), new BlockPos(-radiusInt, -radiusInt, -radiusInt))) {
                double dist = AntUtils.getDist(tempPos, BlockPos.ZERO);
                if (Math.abs(absVector.dot(Vec3.atCenterOf(tempPos).normalize())) < (1.2d/(dist))) {
                    if(dist <= passageRadius) {
                        innerList.add(tempPos.immutable());
                    }
                    else if(dist <= passageRadius + wallThickness){
                        outerList.add(tempPos.immutable());
                    }
                }
            }

            int sX = start.getX();
            int sZ = start.getZ();
            int eX = end.getX();
            int eZ = end.getZ();

            int steps = Math.abs(eX - sX) + Math.abs(eZ - sZ);
            boolean xOrZ;
            BlockPos lastPos = start.mutable();
            int howLongX = 0;
            int howLongZ = 0;
            float yOffC = 0;


            for (int s = steps; s > 0; s--) {
                int xOff = 0;
                int yOff = 0;
                int zOff = 0;
                float yFOff = (float) (end.getY() - lastPos.getY()) / (float) s;
                yOffC = yOffC + yFOff;
                if (Math.abs(yOffC) > 1f) {
                    yOff = (yFOff > 0) ? 1 : -1;
                    yOffC = (yFOff > 0) ? yOffC - 1 : yOffC + 1;
                }

                howLongX = eX - lastPos.getX();
                howLongZ = eZ - lastPos.getZ();
                xOrZ = ColonyGenUtils.nextBool(Math.abs(howLongX), Math.abs(howLongZ), rand);
                if ((xOrZ && howLongX != 0) || howLongZ == 0) {
                    xOff = (howLongX > 0) ? 1 : -1;
                } else {
                    zOff = (howLongZ > 0) ? 1 : -1;
                }

                lastPos = lastPos.offset(xOff, yOff, zOff);

                for(BlockPos offsetPos : outerList){
                    if(pLevel.getBlockState(lastPos.offset(offsetPos)) != innerBlock.defaultBlockState()) {
                        pLevel.setBlock(lastPos.offset(offsetPos), outerBlock.defaultBlockState(), 2);
                    }
                }

                for (BlockPos offsetPos : innerList) {
                    pLevel.setBlock(lastPos.offset(offsetPos), innerBlock.defaultBlockState(), 2);
                }
                if(s == steps || s == 1){

                }

            }
            //AntUtils.broadcastString(pLevel, "OffsetList contains " + (innerList.size() + outerList.size()) + " positions for an expected " + (int)(passageRadius*passageRadius*Math.PI + passageRadius * Math.PI) + " out of " + radiusInt * radiusInt * radiusInt * 8);
        }

        for(ColonyBranch tempBranch : this.getChildren()) {
            tempBranch.newCarve(pLevel, rand);
        }
    }


    private void decorateRoom(ServerLevel pLevel){
        BlockPos tempPos = this.getPos();
        Random rand = AntUtils.randFromPos(tempPos);

        if(this.values.containsKey("room_type")) {
            switch (this.values.get("room_type")) {
                case "storage":
                    ColonyGenUtils.sprinkleArea(this.getPos(), 6, 5, 20, ModBlocks.LEAFY_CONTAINER_BLOCK.get(), rand, pLevel);
                    break;
                case "fungus":
                    while(pLevel.getBlockState(tempPos.below()).isAir() && tempPos.getY() > pLevel.getMinBuildHeight()){tempPos = tempPos.below();}
                    pLevel.setBlock(tempPos, ModBlocks.FUNGAL_CORE.get().defaultBlockState(), 2);
                    FungalCore.grow(pLevel, tempPos, rand.nextInt(20)+20);
                    break;
                case "queen":
                    while(pLevel.getBlockState(tempPos.below()).isAir() && tempPos.getY() > pLevel.getMinBuildHeight()){tempPos = tempPos.below();}
                    ColonyGenUtils.sprinkleArea(tempPos, 12, 3, 20, ModBlocks.LEAFY_CONTAINER_BLOCK.get(), rand, pLevel);
                    pLevel.setBlock(tempPos, ModBlocks.FUNGAL_CORE.get().defaultBlockState(), 2);
                    FungalCore.grow(pLevel,tempPos, rand.nextInt(40)+35);

                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Returns every recursive child which passes the predicate, whether their parent passes it or not.
     * **/
    public ArrayList<ColonyBranch> getChildrenPassing(Predicate<ColonyBranch> predicate){
        ArrayList<ColonyBranch> returnList = getChildrenRecursive();
        returnList.removeIf(predicate.negate());
        return returnList;
    }

    public ColonyBranch createAndReturnChild(BlockPos childPos){
        ColonyBranch newChild = new ColonyBranch(childPos);
        this.addChild(newChild);
        return newChild;
    }

    public ColonyBranch createAndReturnChild(double directionRad, double horizontalDistance, double verticalOffset){
        ColonyBranch newChild = new ColonyBranch(this.getPos().offset(
                Math.sin(directionRad)*horizontalDistance,
                verticalOffset,
                Math.cos(directionRad)*horizontalDistance
        ));
        this.addChild(newChild);
        return newChild;
    }


    public ColonyBranch getAbsoluteParent(){
        ColonyBranch tempBranch = this;

        while(tempBranch.getParent() != null){
            tempBranch = tempBranch.getParent();
        }
        return tempBranch;
    }

    public ArrayList<BlockPos> getPosesToAbsParent(){
        ArrayList<BlockPos> returnList = new ArrayList<>();
        ColonyBranch tempBranch = this;
        getPosesToAbsParentHelper(returnList, tempBranch);
        return returnList;
    }

    private void getPosesToAbsParentHelper(ArrayList<BlockPos> list, ColonyBranch branchAt){
        list.add(branchAt.getPos());
        if(branchAt.parent != null){
            getPosesToAbsParentHelper(list, branchAt.parent);
        }
    }

    /**
     * Assesses all children of the subject's absolute parent, not only children of the subject.
     * **/
    public ColonyBranch getNearestBranch(BlockPos pos){
        ColonyBranch returnBranch = this.getAbsoluteParent();

        for(ColonyBranch tempBranch : returnBranch.getAllChildren()){
            if(AntUtils.getDist(tempBranch.position, pos) < AntUtils.getDist(returnBranch.position, pos)){
                returnBranch = tempBranch;
            }
        }

        return returnBranch;
    }

    /**
     * Does not only work for child branches: it first finds the absolute parent and then finds the path from the
     * absolute parent to both the subject and the target, and then trims off the overlap to create a direct path.
     * **/
    public ArrayList<BlockPos> getPosesToNearestBranchTo(BlockPos pos){
        ColonyBranch nearestBranch = this.getNearestBranch(pos);
        ArrayList<BlockPos> posesThisParent = this.getPosesToAbsParent();
        ArrayList<BlockPos> posesNearestParent = nearestBranch.getPosesToAbsParent();

        while(!posesThisParent.isEmpty() &&
                !posesNearestParent.isEmpty() &&
                posesThisParent.get(posesThisParent.size()-1) ==
                        posesNearestParent.get(posesNearestParent.size()-1)){

            posesThisParent.remove(posesThisParent.size()-1);
            posesNearestParent.remove(posesNearestParent.size()-1);
        }

        for(int i = posesNearestParent.size()-1; i >= 0; i--){
            posesThisParent.add(posesNearestParent.get(i));
        }

        return posesThisParent;
    }


    /**
     * Recursively returns the grandchildren if the children have children. Basically, collects the leaves of the branch node tree.
     * **/
    public ArrayList<ColonyBranch> getAbsoluteChildren(){
        ArrayList<ColonyBranch> returnList = new ArrayList<>();
        for(ColonyBranch tempBranch : this.getChildren()){
            if(tempBranch.getChildren().isEmpty()){
                returnList.add(tempBranch);
            }
            else{
                returnList.addAll(tempBranch.getAbsoluteChildren());
            }
        }
        return returnList;
    }

    /**
     * Returns a list of every child and every child's child, recursively.
     * **/
    public ArrayList<ColonyBranch> getAllChildren(){
        ArrayList<ColonyBranch> returnList = new ArrayList<>();
        for(ColonyBranch tempBranch : this.getChildren()){
            if(!returnList.contains(tempBranch)){
                returnList.add(tempBranch);
                returnList.addAll(tempBranch.getAllChildren());
            }
        }
        return returnList;
    }


    public String getPosStr() {
        return BlockPosStringifier.jsonFromPos(this.position).toString();
    }

    @Override
    public BlockPos getPos() {
        return this.position;
    }

    @Override
    public ColonyBranch getParent(){
        return this.parent;
    }

    @Override
    public ArrayList<ColonyBranch> getChildren() {
        return this.children;
    }

    /**
     * Gets all the children and recursively grandchildren of this branch.
     * **/
    @Override
    public ArrayList<ColonyBranch> getChildrenRecursive() {
        ArrayList<ColonyBranch> returnList = new ArrayList<>();

        for(ColonyBranch tempBranch : this.children){
            returnList.add(tempBranch);
            returnList.addAll(tempBranch.getChildrenRecursive());
        }

        return returnList;
    }

    @Override
    public void setPos(BlockPos newPos) {
        this.position = newPos;
    }

    /**
     * Sets the argument as the subject's child, and sets the argument's parent to the subject.
     * **/
    @Override
    public void addChild(ColonyBranch child) {
        this.children.add(child);
        child.parent = this;
    }

    /**
     * Sets the argument as the subject's parent, and adds the subject to the argument's children.
     * **/
    @Override
    public void setParent(ColonyBranch parent) {
        this.parent = parent;
        parent.children.add(this);
    }

    @Override
    public void removeChild(ColonyBranch child) {
        this.children.remove(child);
        child.parent = null;
    }

    @Override
    public void setChildren(ArrayList<ColonyBranch> newChildren) {
        this.children = newChildren;
        for(ColonyBranch tempBranch : newChildren){
            tempBranch.parent = this;
        }
    }

    @Override
    public boolean hasChild(String string) {
        for(ColonyBranch tempChild : this.children){
            if(tempChild.getPosStr().equals(string)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasChild(ColonyBranch child) {
        return this.children.contains(child);
    }

    @Override
    public void setValues(HashMap<String, String> vals) {
        this.values = vals;
    }

    @Override
    public HashMap<String, String> getValues() {
        return this.values;
    }

    @Override
    public void setValue(String key, String val) {
        this.values.put(key, val);
    }

    @Override
    public String getValue(String key){
        return this.values.get(key);
    }

    @Override
    public void removeKey(String key) {
        this.values.remove(key);
    }

    @Override
    public boolean hasKey(String key) {
        return this.values.containsKey(key);
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder("Branch at " + this.getPosStr() + " has values:\n");

        str.append(this.values.toString());
        if(this.parent != null) {
            str.append("\nParent:\n");
            str.append(this.parent.getPosStr());
        }
        str.append("\nChildren:\n");
        for(ColonyBranch tempChild : this.children){
            str.append(tempChild.getPosStr());
        }

        return str.toString();
    }
}
