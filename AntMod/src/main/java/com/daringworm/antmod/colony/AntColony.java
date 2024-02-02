package com.daringworm.antmod.colony;

import com.daringworm.antmod.colony.misc.*;
import com.daringworm.antmod.entity.custom.QueenAnt;
import com.daringworm.antmod.goals.AntUtils;
import com.daringworm.antmod.mixin.tomixin.ServerLevelUtil;
import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
        return this.tunnels.getSubBranch("0").getOriginPos();
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

    private ArrayList<PosSpherePair> generatePassageBlueprint(PosPair pPath, double width){
        ArrayList<PosSpherePair> returnList = new ArrayList<>();
        BlockPos start = pPath.top;
        BlockPos end = pPath.bottom;
        if(start == null || end == null){
            String sS = (start == null)? "null ":"not null ";
            String eS = (end == null)? "null.":"not null.";
            AntUtils.broadcastString(this.level, "Couldn't generate blueprint for an Ant tunnel, there was a null endpoint value: start pos is "
                    + sS + "and the end pos is " + eS);
            return returnList;
        }

        int sX = start.getX();
        int sZ = start.getZ();
        int eX = end.getX();
        int eZ = end.getZ();

        int steps = Math.abs(eX-sX) + Math.abs(eZ - sZ);
        boolean xOrZ;
        BlockPos lastPos = start;
        int howLongX = 0;
        int howLongZ = 0;
        float yOffC = 0;

        for(int s = steps; s > 0; s--){
            int xOff = 0;
            int yOff = 0;
            int zOff = 0;
            float yFOff = (float)(end.getY()-lastPos.getY())/(float)s;
            yOffC = yOffC + yFOff;
            if(Math.abs(yOffC) > 1f) {
                yOff = (yFOff > 0) ? 1 : -1;
                yOffC = (yFOff > 0) ? yOffC - 1: yOffC + 1;
            }

            howLongX = eX-lastPos.getX();
            howLongZ = eZ-lastPos.getZ();
            xOrZ = ColonyGenerator.nextBool(Math.abs(howLongX),Math.abs(howLongZ),this.random);
            if((xOrZ && howLongX != 0) || howLongZ == 0){xOff = (howLongX > 0) ? 1 : -1;}
            else{zOff = (howLongZ > 0) ? 1: -1;}

            lastPos = new BlockPos(lastPos.getX()+xOff, lastPos.getY()+yOff, lastPos.getZ()+zOff);
            returnList.add(new PosSpherePair(lastPos, width));
        }
        return returnList;
    }

    private BlockPos findPosForDir(BlockPos startPos, int distanceFromCenter, Direction direction, Random rand){
        rand = new Random(Math.abs(startPos.getX()*startPos.getY()));
        int randomIntDistBound = rand.nextInt((int)distanceFromCenter+1);
        int bigNumber = (randomIntDistBound >= (int)distanceFromCenter/2) ? randomIntDistBound : 0;
        int smallNumber = (bigNumber != 0) ? (int)Math.abs(Math.sqrt((distanceFromCenter*distanceFromCenter)-(bigNumber*bigNumber))) : randomIntDistBound- randomIntDistBound/3;
        smallNumber = (rand.nextBoolean()) ? -smallNumber : smallNumber;
        if(bigNumber == 0){bigNumber = (int)Math.abs(Math.sqrt((distanceFromCenter*distanceFromCenter)-(smallNumber*smallNumber)));}

        int xVar = (direction == Direction.EAST || direction == Direction.WEST) ?
                ((direction == Direction.EAST) ? bigNumber : -bigNumber) : smallNumber;

        int zVar = (direction == Direction.SOUTH || direction == Direction.NORTH) ?
                ((direction == Direction.SOUTH) ? bigNumber : -bigNumber) : smallNumber;

        return startPos.offset(xVar, 0, zVar);
    }

    private ArrayList<PosSpherePair> generateRoomBlueprint(double height, int size, BlockPos center, Random rand){
        rand = new Random(Math.abs(center.getX()*center.getY()));
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();
        ArrayList<BlockPos> posList = new ArrayList<>();
        ArrayList<PosSpherePair> returnList = new ArrayList<>();
        posList.add(center);

        for(int i = size; i>0; i--){
            int xOff = rand.nextInt((int)height);
            int zOff = rand.nextInt((int)height);
            xOff = (rand.nextBoolean()) ? xOff : -xOff;
            zOff = (rand.nextBoolean()) ? zOff : -zOff;

            posList.add(new BlockPos(cx+xOff,cy,cz+zOff));
        }
        for(BlockPos tempPos : posList){
            returnList.add(new PosSpherePair(tempPos,height));
        }
        return returnList;
    }



    public ArrayList<PosSpherePair> generateNewColonyBlueprint(){
        this.tunnels = new ColonyBranch(startPos,Direction.getRandom(this.random),new ArrayList<>());
        ArrayList<PosSpherePair> returnList = new ArrayList<>();
        random = new Random((long) startPos.getX()*startPos.getY()*startPos.getZ());

        //makes the entrance
        returnList.add(new PosSpherePair(startPos.above(4),7));

        this.tunnels.generateNextBranch(25,-15,false);
        this.tunnels.branches.get(0).generateNextBranches(3,8,-3,22,true);
        String str = "";
        for(int i = 0; i<=10; i++){
            ColonyBranch tempBranch = tunnels.getSubBranch(str);
            str = str + "0";

            AntUtils.broadcastString(level, "Branch starts at " + BlockPosStringifier.jsonFromPos(tempBranch.getOriginPos()).toString());

            for(ColonyBranch tempBranch1: tempBranch.branches) {
                returnList.addAll(generatePassageBlueprint(
                        new PosPair(tempBranch.getOriginPos(), tempBranch1.getOriginPos(), this.level), this.passageWidth));
            }

            returnList.addAll(generateRoomBlueprint(this.passageWidth+1,this.UNDERGOUND_ROOM_SIZE,tempBranch.getOriginPos(),this.random));
        }

        ((ServerLevelUtil) (level)).refreshColonyForID(this);
        this.excavationSpheres = returnList;
        return returnList;
    }



    public ArrayList<PosSpherePair> getNextExcavationSteps(int stepAt){


        ArrayList<PosSpherePair> returnList = new ArrayList<>();

        if(excavationSpheres.isEmpty()){excavationSpheres = generateNewColonyBlueprint();}

        if(!excavationSpheres.isEmpty()){
            /*excavationStage = stepAt;
            int numberOfSpheres = Math.min(excavationSpheres.size() - excavationStage-1, 10);
            if(numberOfSpheres > 0) {
                for (int i = stepAt; i < stepAt + numberOfSpheres; i++) {
                    returnList.add(excavationSpheres.get(i));
                }
            }*/
        }

        ((ServerLevelUtil) (level)).refreshColonyForID(this);
        return returnList;

    }
}