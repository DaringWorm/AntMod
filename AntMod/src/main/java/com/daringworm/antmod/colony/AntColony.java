package com.daringworm.antmod.colony;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.colony.misc.*;
import com.daringworm.antmod.entity.ModEntityTypes;
import com.daringworm.antmod.entity.brains.parts.WorkingStages;
import com.daringworm.antmod.entity.custom.WorkerAnt;
import com.daringworm.antmod.mixin.tomixin.ServerLevelUtil;
import com.daringworm.antmod.util.AntUtils;
import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import javax.json.Json;
import javax.json.JsonReader;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class AntColony{

    public int colonyID;
    public ColonyBranch tunnels;
    public BlockPos startPos;
    private int excavationStage;
    
    public Random random;
    public Set<PlayerPopularity> playerPopularities = new HashSet<>();
    public ServerLevel level;
    private static final Logger LOGGER = LogUtils.getLogger();
    public File saveFolder;
    private ArrayList<PosSpherePair> excavationSpheres = new ArrayList<>();
    public boolean hasSpawnedAnts;
    public boolean hasSpawnedDecoration;


    public static Block BLOCK1 = ModBlocks.ANT_AIR.get();
    public static Block BLOCK2 = ModBlocks.ANT_DIRT.get();

    public AntColony(ServerLevel pLevel, int pColonyID, BlockPos pStartPos){
        this.level = pLevel;
        this.colonyID = pColonyID;
        this.saveFolder = getSaveFile(pLevel);
        this.random = new Random(Math.abs(pStartPos.getX()*pStartPos.getY()*pStartPos.getZ()));
        this.startPos = pStartPos;
        this.generateNewColonyBlueprint();
    }
    public AntColony(ServerLevel pLevel, int pColonyID, ColonyBranch tunnels){
        this.startPos = tunnels.getPos();
        this.level = pLevel;
        this.colonyID = pColonyID;
        this.saveFolder = getSaveFile(level);
        this.random = AntUtils.randFromPos(startPos);
        this.tunnels = tunnels;
        this.generateNewColonyBlueprint();
    }

    public AntColony(File colonyStorageFolder, String fileName, ServerLevel pLevel) {
        this.level = pLevel;
        this.saveFolder = colonyStorageFolder.getAbsoluteFile();
        File colonyFile = new File(saveFolder,fileName);
        JsonObject j;

        try {
            InputStream is = new FileInputStream(colonyFile);
            JsonReader jsonreader = Json.createReader(is);
            javax.json.JsonObject temp = jsonreader.readObject();
            jsonreader.close();

            j = JsonParser.parseString(temp.toString()).deepCopy().getAsJsonObject();

                try {
                    this.startPos = BlockPosStringifier.posFromString(j.getAsJsonObject("start_pos"));
                    this.colonyID = j.get("colony_ID").getAsInt();
                    this.hasSpawnedAnts = j.get("has_spawned_ants").getAsBoolean();
                    this.excavationStage = j.get("excavation_stage").getAsInt();
                    this.random = new Random(Math.abs(this.startPos.getX()*this.startPos.getY()*this.startPos.getZ()));

                    this.tunnels = new ColonyBranch(j.get("tunnels").getAsJsonObject());

                    JsonArray popularities = j.get("player_popularities").getAsJsonArray();
                    if(!popularities.isEmpty()) {
                        for (int i = popularities.size(); i > 0; i--) {
                            JsonObject obj = popularities.get(i - 1).getAsJsonObject();
                            String playerID = obj.get("ID").getAsString();
                            int pplrty = obj.get("popularity").getAsInt();
                            this.playerPopularities.add(new PlayerPopularity(playerID, pplrty));
                        }
                    }


                } catch (Throwable throwable1) {
                    try {
                    } catch (Throwable throwable) {
                        throwable1.addSuppressed(throwable);
                    }
                    throw throwable1;
                }

                jsonreader.close();
            }
        catch (JsonParseException | FileNotFoundException ignored){
        }
    }

    public ArrayList<PosSpherePair> getColonyBlueprint(){
        if(this.excavationSpheres.isEmpty()){
            if(this.tunnels == null) {
                this.generateNewColonyBlueprint();
            }
            else{
                this.excavationSpheres = tunnels.getExcavationSpheres();
            }
        }
        return this.excavationSpheres;
    }


    public void tick(){
        //AntUtils.broadcastString(level, "Colony with ID " + this.colonyID + " at location " + this.colonyID + " was ticked.");

        if(!this.hasSpawnedAnts){
            this.spawnAnts();
        }
    }

    public boolean isInLoadedChunk(){
        return this.level.isLoaded(this.startPos);
    }

    public void save() {
        try {
            File saveFile = new File(saveFolder, this.colonyID + ".json");
            FileUtils.writeStringToFile(saveFile, this.toJson(), Charset.defaultCharset());
        } catch (IOException ioexception) {
            LOGGER.error("Couldn't save colony", ioexception);
        }

        AntUtils.broadcastString(this.level, "saved colony at " + this.startPos);
    }

    public void addToLevel(ServerLevel pLevel){
        ((ServerLevelUtil) level).addColonyToList(this);
        this.level = pLevel;
    }

    public void delete(){
        try {
            File saveFile = new File(saveFolder, this.colonyID + ".json");
            FileUtils.delete(saveFile);
        } catch (IOException ioexception) {
            LOGGER.error("Couldn't delete colony", ioexception);
        }
    }

    protected String toJson() {

        JsonArray playerPopularityJ = new JsonArray();

        for(PlayerPopularity playerPop : playerPopularities){
            JsonObject playerJ = new JsonObject();
            playerJ.addProperty("ID", String.valueOf(playerPop.pID));
            playerJ.addProperty("popularity", playerPop.popularity);
            playerPopularityJ.add(playerJ);
        }

        JsonObject tunnelsJ = this.tunnels.toJson();

        JsonObject jsonobject1 = new JsonObject();
        jsonobject1.addProperty("colony_ID",this.colonyID);
        jsonobject1.addProperty("has_spawned_ants",this.hasSpawnedAnts);
        jsonobject1.addProperty("excavation_stage",this.excavationStage);
        jsonobject1.add("player_popularities", playerPopularityJ);
        jsonobject1.add("start_pos", BlockPosStringifier.jsonFromPos(this.startPos));
        jsonobject1.add("tunnels", tunnelsJ);

        return jsonobject1.toString();
    }

    private File getSaveFile(ServerLevel pLevel){
        String levelName = pLevel.toString();
        levelName = levelName.replace(".","_");
        levelName = levelName.replace("]","");
        levelName = levelName.replace("[","");
        levelName = levelName.replaceAll("ServerLevel","");

        String worldFilePath = pLevel.getServer().getServerDirectory().getPath() + "/saves/" + levelName + "/data/ant_colonies";
        File ret = new File(worldFilePath);
        boolean success = ret.mkdirs();
        return ret;
    }

    public static ColonyBranch generateNewTunnels(BlockPos startPos){
        double passageLength = 8.0d;
        double passageDepth = -3.0d;

        Random generationRandom = AntUtils.randFromPos(startPos);
        float pi = (float)Math.PI;
        float pi2 = (float)Math.PI * 2f;
        float dirRad = generationRandom.nextFloat()*pi2;

        ColonyBranch rootBranch = new ColonyBranch(startPos);
        rootBranch.setValue("direction", ""+dirRad);
        rootBranch.setValue("has_room", "false");
        rootBranch.setValue("is_entrance", "true");

        ColonyBranch.testing.add(rootBranch);
        ColonyBranch entranceBottom = rootBranch;

        for(int i = 0; i < 8; i++){
            dirRad += (generationRandom.nextBoolean())? generationRandom.nextFloat() : -generationRandom.nextFloat();

            ColonyBranch newChild = entranceBottom.createAndReturnChild(dirRad, passageLength, passageDepth);
            newChild.setValue("direction", ""+dirRad);
            newChild.setValue("has_room", "false");
            entranceBottom = newChild;

            ColonyBranch.testing.add(newChild);
        }

        //creates the rest of the colony
        float roomCountMultiplier = 0.3f; //inverse, >1 = 0
        passageLength *= 2;

        for(int i = 0; i < 4; i++){
            for(ColonyBranch tempBranch : rootBranch.getAbsoluteChildren()){
                int numNew = (int)(generationRandom.nextFloat()/roomCountMultiplier) + ((i == 0)? 1 : 0);
                dirRad = Float.parseFloat(tempBranch.getValue("direction"));
                float offset = generationRandom.nextFloat()/1.3f;
                dirRad += generationRandom.nextBoolean()? offset : -offset;

                for(int j = 1; j <= numNew; j++){
                    float tempDir = (dirRad-pi/2) + j*(pi/(numNew+1));
                    ColonyBranch tempColonyBranch = tempBranch.createAndReturnChild(tempDir, passageLength, -generationRandom.nextInt((int)passageLength/3));

                    tempColonyBranch.setValue("has_room", "true");
                    tempColonyBranch.setValue("room_size", "3.3");
                    tempColonyBranch.setValue("direction", ""+ tempDir);
                    ColonyBranch.testing.add(tempColonyBranch);
                }
            }
        }

        assignRoomTypes(rootBranch);

        return rootBranch;
    }

    public static WorkerAnt getNewWorker(ServerLevel pLevel, int iD, BlockPos homePos, BlockPos surfacePos){
        WorkerAnt pAnt = new WorkerAnt(ModEntityTypes.WORKERANT.get(), pLevel);
        pAnt.moveTo(Vec3.atCenterOf(homePos));
        pAnt.setColonyID(iD);
        pAnt.setWorkingStage(WorkingStages.SCOUTING);
        pAnt.setHomeContainerPos(homePos);
        pAnt.setFirstSurfacePos(surfacePos);

        return pAnt;
    }

    public boolean spawnAnts(){
        if(this.level.isClientSide || this.tunnels == null){return false;}

        ArrayList<ColonyBranch> allBranches = this.tunnels.getChildrenPassing(t -> t.hasKey("has_room") && t.getValue("has_room").equals("true"));

        //checks if the ants have an exit leading to the surface.

        /*if(!this.level.canSeeSky(startPos)){
            //
        }*/


        //Spawns the ants
        for (ColonyBranch tempBranch : allBranches) {

            BlockPos roomPos = tempBranch.getPos();

            level.addFreshEntity(getNewWorker(level, this.colonyID, roomPos, this.tunnels.getPos()));

        }

        this.hasSpawnedAnts = true;
        this.save();
        return true;
    }


    public ArrayList<PosSpherePair> generateNewColonyBlueprint(){
        //makes the rest
        if(this.tunnels == null) {
            this.tunnels = generateNewTunnels(startPos);
        }
        this.excavationSpheres = tunnels.getExcavationSpheres();
        return this.excavationSpheres;
    }

    public ArrayList<PosSpherePair> getNextExcavationSteps(int stepAt){
        final int maxNumberOfSpheresToGive = 1;

        ArrayList<PosSpherePair> returnList = new ArrayList<>();

        if(excavationSpheres.isEmpty()){excavationSpheres = generateNewColonyBlueprint();}

        if(!excavationSpheres.isEmpty()){
            excavationStage = stepAt;
            int numberOfSpheres = Math.min(excavationSpheres.size() - excavationStage-1, maxNumberOfSpheresToGive);
            if(numberOfSpheres > 0) {
                for (int i = stepAt; i < stepAt + numberOfSpheres; i++) {
                    returnList.add(excavationSpheres.get(i));
                }
            }
        }

        return returnList;
    }

    public static AntColony generateWholeNewColony(ServerLevel pLevel, BlockPos startPos){
        AntColony returnColony = new AntColony(pLevel, AntUtils.randFromPos(startPos).nextInt(), startPos);
        returnColony.tunnels.carve(pLevel);
        returnColony.hasSpawnedAnts = false;

        AntUtils.broadcastString(pLevel, "Generated a colony starting at " + BlockPosStringifier.jsonFromPos(returnColony.startPos));

        returnColony.addToLevel(pLevel);
        return returnColony;
    }

    private static void assignRoomTypes(ColonyBranch rootBranch){
        Random rand = AntUtils.randFromPos(rootBranch.getPos());

        while (rootBranch.getValues().get("has_room") == null || rootBranch.getValues().get("has_room").equals("false")){
            rootBranch = rootBranch.getChildren().get(0);
        }
        rootBranch = rootBranch.getParent();

        //assigns the room types
        ArrayList<ColonyBranch> allRooms = rootBranch.getAllChildren();
        ArrayList<ColonyBranch> endRooms = rootBranch.getAbsoluteChildren();
        ArrayList<ColonyBranch> firstRooms = rootBranch.getChildren();

        for(ColonyBranch tempBranch : firstRooms){
            tempBranch.setValue("room_type", "storage");
            allRooms.remove(tempBranch);
        }

        for(ColonyBranch tempBranch : endRooms){
            tempBranch.setValue("room_type", "fungus");
            tempBranch.setValue("room_size", "4.3");
            allRooms.remove(tempBranch);
        }

        for (ColonyBranch tempBranch : allRooms) {
            tempBranch.setValue("room_type", (rand.nextFloat() < 0.3) ? "storage" : (rand.nextFloat() < 0.3) ? "fungus" : "empty");
        }

        ColonyBranch queenRoom = endRooms.get(rand.nextInt(endRooms.size()));
        queenRoom.setValue("room_type", "queen");
        queenRoom.setValue("room_size", "5.5");
    }
}