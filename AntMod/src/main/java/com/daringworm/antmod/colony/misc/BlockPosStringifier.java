package com.daringworm.antmod.colony.misc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;

public final class BlockPosStringifier {

    public static JsonElement jsonFromPos(BlockPos pos){
        JsonObject j = new JsonObject();
        j.addProperty("x", pos.getX());
        j.addProperty("y", pos.getY());
        j.addProperty("z",pos.getZ());

        return j;
    }

    public static BlockPos posFromString(JsonObject j){
        return new BlockPos(
                j.get("x").getAsInt(),
                j.get("y").getAsInt(),
                j.get("z").getAsInt()
        );
    }

    public static BlockPos posFromString(String str){
        JsonObject j = JsonParser.parseString(str).getAsJsonObject();
        return posFromString(j);
    }
}
