package com.daringworm.antmod.colony.misc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.Collection;

public final class BlockPosStringifier {

    public static CompoundTag getTagForPos(BlockPos pos){
        CompoundTag returnTag = new CompoundTag();
        returnTag.putInt("X", pos.getX());
        returnTag.putInt("Y", pos.getY());
        returnTag.putInt("Z",pos.getZ());
        return returnTag;
    }

    public static BlockPos getPosForTag(CompoundTag tag){
        if(tag.isEmpty() || !tag.contains("X") || !tag.contains("Y") || !tag.contains("Z")){return BlockPos.ZERO;}
        else return new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"));
    }

    public static CompoundTag getTagForPosList(ArrayList<BlockPos> posList){
        CompoundTag tag = new CompoundTag();

        if(!posList.isEmpty()) {
            int[] xArray = new int[posList.size()];
            int[] yArray = new int[posList.size()];
            int[] zArray = new int[posList.size()];

            int stepAt = 0;
            for (BlockPos tempPos : posList) {
                xArray[stepAt] = tempPos.getX();
                yArray[stepAt] = tempPos.getY();
                zArray[stepAt] = tempPos.getZ();
                ++stepAt;
            }

            tag.putIntArray("X", xArray);
            tag.putIntArray("Y", yArray);
            tag.putIntArray("Z", zArray);
        }

        return tag;
    }

    public static ArrayList<BlockPos> getPosesForTag(CompoundTag tag){
        ArrayList<BlockPos> returnList = new ArrayList<>();
        if(!tag.contains("X") || !tag.contains("Y") || !tag.contains("Z")){return returnList;}

        int[] xArray = tag.getIntArray("X");
        int[] yArray = tag.getIntArray("Y");
        int[] zArray = tag.getIntArray("Z");

        int min = Math.min(xArray.length,Math.min(yArray.length,zArray.length));
        for(int i = 0; i < min; i++){
            returnList.add(new BlockPos(xArray[i],yArray[i],zArray[i]));
        }

        return returnList;
    }



    public static BlockPos posFromString(JsonObject j){
        return new BlockPos(
                j.get("x").getAsInt(),
                j.get("y").getAsInt(),
                j.get("z").getAsInt()
        );
    }
    public static JsonElement jsonFromPos(BlockPos pos){
        JsonObject j = new JsonObject();
        j.addProperty("x", pos.getX());
        j.addProperty("y", pos.getY());
        j.addProperty("z",pos.getZ());

        return j;
    }

    public static BlockPos posFromString(String str){
        JsonObject j = JsonParser.parseString(str).getAsJsonObject();
        return posFromString(j);
    }
}
