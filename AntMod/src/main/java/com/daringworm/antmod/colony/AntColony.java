package com.daringworm.antmod.colony;

import com.daringworm.antmod.colony.misc.*;
import com.daringworm.antmod.entity.custom.QueenAnt;
import com.daringworm.antmod.mixin.tomixin.ServerLevelUtil;
import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import javax.json.Json;
import javax.json.JsonReader;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

public class AntColony implements AutoCloseable{

    public int colonyID;
    public ColonyBranch tunnels;
    public BlockPos startPos;
    private int excavationStage;
    
    public Random random;
    public Set<PosPair> entranceSet = new HashSet<>();
    public Set<PlayerPopularity> playerPopularities = new HashSet<>();
    public Level level;
    private static final Logger LOGGER = LogUtils.getLogger();
    public File saveFolder;
    private double passageWidth = 2.1d;
    private ArrayList<PosSpherePair> excavationSpheres = new ArrayList<>();
    private final int UNDERGOUND_PASSAGE_LENGTH = 20;
    private final int UNDERGOUND_ROOM_SIZE = 24;

    public AntColony(Level pLevel, int pColonyID, BlockPos pStartPos){
        this.level = pLevel;
        this.colonyID = pColonyID;
        this.saveFolder = getSaveFile((ServerLevel) pLevel);
        this.random = new Random(Math.abs(pStartPos.getX()*pStartPos.getY()*pStartPos.getZ()));
        this.startPos = pStartPos;
        this.generateNewColonyBlueprint();
    }

    public AntColony(File colonyStorageFolder, String fileName, ServerLevel pLevel) {
        this.level = pLevel;
        this.saveFolder = colonyStorageFolder.getAbsoluteFile();
        File colonyFile = new File(saveFolder,fileName);
        JsonObject j = new JsonObject();

        try {
            InputStream is = new FileInputStream(colonyFile);
            JsonReader jsonreader = Json.createReader(is);
            javax.json.JsonObject temp = jsonreader.readObject();
            jsonreader.close();

            j = JsonParser.parseString(temp.toString()).deepCopy().getAsJsonObject();

                try {
                    this.startPos = BlockPosStringifier.posFromString(j.getAsJsonObject("start_pos"));
                    this.colonyID = j.get("colony_ID").getAsInt();
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

    public BlockPos getEntranceBottom(){
        return this.tunnels.getSubBranch("0").getPos();
    }

    public ArrayList<PosSpherePair> getColonyBlueprint(){
        if(this.excavationSpheres.isEmpty()){this.generateNewColonyBlueprint();}
        return this.excavationSpheres;
    }


    public QueenAnt getQueen(){
        Stream<QueenAnt> queenAntStream = this.level.getEntitiesOfClass(QueenAnt.class, new AABB(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE)).stream();
        return queenAntStream.filter(t -> t.getColonyID() == this.colonyID).findFirst().orElse(null);
    }


    @Override
    public void close() {
        this.save();
    }


    public void save() {
        try {
            File saveFile = new File(saveFolder, this.colonyID + ".json");
            FileUtils.writeStringToFile(saveFile, this.toJson());
        } catch (IOException ioexception) {
            LOGGER.error("Couldn't save colony", ioexception);
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
        jsonobject1.addProperty("colony_ID",colonyID);
        jsonobject1.addProperty("excavation_stage",excavationStage);
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


    public ArrayList<PosSpherePair> generateNewColonyBlueprint(){
        this.tunnels = new ColonyBranch(startPos, this.random.nextInt(360),new ArrayList<>(), false, "0");
        ArrayList<PosSpherePair> returnList = new ArrayList<>();
        random = new Random((long) startPos.getX()*startPos.getY()*startPos.getZ());

        //makes the entrance
        returnList.add(new PosSpherePair(startPos.above(4),7));

        this.tunnels.generateNextBranch(35,-20,false);
        this.tunnels.generateNextBranches(5, 48,1,0,24,true);
        this.tunnels.generateNextBranches(2, 43,1,0,20,true);
        this.tunnels.generateNextBranches(2, 35,1,0,16,true);

        returnList.addAll(this.tunnels.generateBranchBlueprint(
                this.passageWidth,this.passageWidth+0.5d, UNDERGOUND_ROOM_SIZE,this.random,this.level));

        ((ServerLevelUtil) (level)).refreshColonyForID(this);
        this.excavationSpheres = returnList;
        return returnList;
    }

    public ArrayList<PosSpherePair> getNextExcavationSteps(int stepAt){
        ArrayList<PosSpherePair> returnList = new ArrayList<>();

        if(excavationSpheres.isEmpty()){excavationSpheres = generateNewColonyBlueprint();}

        if(!excavationSpheres.isEmpty()){
            excavationStage = stepAt;
            int numberOfSpheres = Math.min(excavationSpheres.size() - excavationStage-1, 10);
            if(numberOfSpheres > 0) {
                for (int i = stepAt; i < stepAt + numberOfSpheres; i++) {
                    returnList.add(excavationSpheres.get(i));
                }
            }
        }

        ((ServerLevelUtil) (level)).refreshColonyForID(this);
        return returnList;
    }
}