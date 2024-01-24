package com.daringworm.antmod.colony;

import com.daringworm.antmod.entity.custom.QueenAnt;
import com.daringworm.antmod.colony.misc.CheckableBlockPosPath;
import com.daringworm.antmod.colony.misc.PosSpherePair;
import com.daringworm.antmod.goals.AntUtils;
import com.daringworm.antmod.goals.CarveGoal;
import com.daringworm.antmod.mixin.tomixin.ServerLevelUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import javax.json.Json;
import javax.json.JsonReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

public class AntColony implements AutoCloseable{
    public int colonyID;
    public BlockPos startPos;
    public BlockPos entranceBottom;
    private BlockPos crixia;
    public BlockPos queenRoomPos;
    //for queenRoomPos
    private BlockPos chosenMiddleRoom;

    
    public Random random;
    public Set<CheckableBlockPosPath> entranceSet = new HashSet<>();
    public Set<PlayerPopularity> playerPopularities = new HashSet<>();
    public Level level;
    private static final Logger LOGGER = LogUtils.getLogger();
    public File saveFolder;
    private double passageWidth = 2.1d;
    public int excavationStage = 0;
    public ArrayList<BlockPos> roomPosList = new ArrayList<>();
    private ArrayList<PosSpherePair> excavationSpheres = new ArrayList<>();
    private final int UNDERGOUND_PASSAGE_LENGTH = 20;
    private final int UNDERGOUND_ROOM_SIZE = 24;

    public AntColony(Level pLevel, int pColonyID, BlockPos pStartPos){
        this.level = pLevel;
        this.startPos = pStartPos;
        this.colonyID = pColonyID;
        this.saveFolder = getSaveFile((ServerLevel) pLevel);
        this.random = new Random(Math.abs(pStartPos.getX()*pStartPos.getY()*pStartPos.getZ()));
        this.generateNewColonyBlueprint();
    }

    public AntColony(File colonyStorageFolder, String fileName, ServerLevel pLevel) {
        this.level = pLevel;
        this.saveFolder = colonyStorageFolder.getAbsoluteFile();
        File colonyFile = new File(saveFolder,fileName);

        try {
            InputStream is = new FileInputStream(colonyFile);
            JsonReader jsonreader = Json.createReader(is);
            javax.json.JsonObject jsonObj = jsonreader.readObject();
            jsonreader.close();

            label51: {
                try {

                    if (!jsonObj.isEmpty()) {

                        this.colonyID = jsonObj.getInt("ColonyID");
                        this.excavationStage = jsonObj.getInt("ExcavationStep");
                        javax.json.JsonObject startObj = jsonObj.getJsonObject("Center");
                        this.startPos = new BlockPos(startObj.getInt("X"), startObj.getInt("Y"), startObj.getInt("Z"));
                        javax.json.JsonObject queenObj = jsonObj.getJsonObject("queenRoomPos");
                        this.queenRoomPos = new BlockPos(queenObj.getInt("X"), queenObj.getInt("Y"), queenObj.getInt("Z"));


                        javax.json.JsonArray entrances = jsonObj.getJsonArray("Tunnels");
                        for(int i = entrances.size(); i > 0; i--){
                            javax.json.JsonObject obj = entrances.getJsonObject(i-1);
                            javax.json.JsonObject top = obj.getJsonObject("top");
                            javax.json.JsonObject bottom = obj.getJsonObject("bottom");
                            this.entranceSet.add(new CheckableBlockPosPath(new BlockPos(top.getInt("X"),
                                    top.getInt("Y"),top.getInt("Z")), new BlockPos(bottom.getInt("X"),
                                    bottom.getInt("Y"),bottom.getInt("Z")), this.level));
                        }

                        javax.json.JsonArray rooms = jsonObj.getJsonArray("Rooms");
                        for(int i = rooms.size(); i > 0; i--){
                            javax.json.JsonObject obj = rooms.getJsonObject(i-1);
                            this.roomPosList.add(new BlockPos(obj.getInt("X"), obj.getInt("Y"),obj.getInt("Z")));
                        }

                        javax.json.JsonArray popularities = jsonObj.getJsonArray("PlayerPopularities");
                        for(int i = popularities.size(); i > 0; i--){
                            javax.json.JsonObject obj = popularities.getJsonObject(i-1);
                            String playerID = obj.getString("ID");
                            int pplrty = obj.getInt("popularity");
                            this.playerPopularities.add(new PlayerPopularity(playerID,pplrty));
                        }


                    }

                } catch (Throwable throwable1) {
                    try {
                        jsonreader.close();
                    } catch (Throwable throwable) {
                        throwable1.addSuppressed(throwable);
                    }

                    throw throwable1;
                }

                jsonreader.close();
            }
        } catch (JsonParseException ignored) {
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BlockPos getEntranceBottom(){
        if(this.entranceBottom != null){return this.entranceBottom;}
        this.generateNewColonyBlueprint();
        return this.entranceBottom;
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
        JsonObject startPosJ = new JsonObject();
        for(Direction.Axis axis : Direction.Axis.values()) {startPosJ.addProperty(axis.name(), startPos.get(axis));}

        JsonObject queenRoomPosJ = new JsonObject();
        for(Direction.Axis axis : Direction.Axis.values()) {queenRoomPosJ.addProperty(axis.name(), queenRoomPos.get(axis));}


        JsonArray entrancesJ = new JsonArray();

        for(CheckableBlockPosPath path : entranceSet) {

            JsonObject topJ = new JsonObject();
            for(Direction.Axis axis : Direction.Axis.values()) {topJ.addProperty(axis.name(), path.top.get(axis));}
            JsonObject bottomJ = new JsonObject();
            for(Direction.Axis axis : Direction.Axis.values()) {bottomJ.addProperty(axis.name(), path.bottom.get(axis));}

            JsonObject pathObject = new JsonObject();
            pathObject.add("top",topJ);
            pathObject.add("bottom",bottomJ);
            entrancesJ.add(pathObject);
        }

        JsonArray roomsJ = new JsonArray();

        for(BlockPos tempPos : roomPosList) {
            JsonObject posJ = new JsonObject();
            for(Direction.Axis axis : Direction.Axis.values()) {posJ.addProperty(axis.name(), tempPos.get(axis));}
            roomsJ.add(posJ);
        }

        JsonArray playerPopularityJ = new JsonArray();

        for(PlayerPopularity playerPop : playerPopularities){
            JsonObject playerJ = new JsonObject();
            playerJ.addProperty("ID", String.valueOf(playerPop.pID));
            playerJ.addProperty("popularity", playerPop.popularity);
            playerPopularityJ.add(playerJ);
        }

        JsonObject jsonobject1 = new JsonObject();
        jsonobject1.addProperty("ExcavationStep", excavationStage);
        jsonobject1.add("Center", startPosJ);
        jsonobject1.addProperty("ColonyID",colonyID);
        jsonobject1.add("Tunnels", entrancesJ);
        jsonobject1.add("PlayerPopularities", playerPopularityJ);
        jsonobject1.add("Rooms", roomsJ);
        jsonobject1.add("queenRoomPos", queenRoomPosJ);

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

    private ArrayList<PosSpherePair> generatePassageBlueprint(CheckableBlockPosPath pPath, double width){
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
            xOrZ = CarveGoal.nextBool(Math.abs(howLongX),Math.abs(howLongZ),this.random);
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
        ArrayList<PosSpherePair> returnList = new ArrayList<>();
        random = new Random((long) startPos.getX()*startPos.getY()*startPos.getZ());

        
        //finds and sets the bottom pos
        int yOffset = 15;
        Direction baseDir = Direction.fromYRot(random.nextInt(360));
        HashSet<Direction> dirList = new HashSet<>();
        dirList.add(baseDir);


        //makes the entrance
        returnList.add(new PosSpherePair(startPos.above(4),7));
        entranceBottom = (entranceBottom == null || entranceBottom == BlockPos.ZERO)? findPosForDir(startPos, 30, baseDir, random).below(yOffset) : entranceBottom;
        returnList.addAll(generatePassageBlueprint(new CheckableBlockPosPath(startPos,entranceBottom,level),passageWidth));
        crixia = (crixia == null || crixia == BlockPos.ZERO)? findPosForDir(entranceBottom,20,baseDir,random) : crixia;
        returnList.addAll(generatePassageBlueprint(new CheckableBlockPosPath(entranceBottom,crixia,level),passageWidth));


        //makes the rooms and underground passages
        if(roomPosList.isEmpty()) {
            dirList.add(Direction.NORTH);
            dirList.add(Direction.SOUTH);
            dirList.add(Direction.EAST);
            dirList.add(Direction.WEST);
            dirList.remove(baseDir.getOpposite());
            for (Direction dir : dirList) {
                BlockPos tempPos = findPosForDir(crixia, UNDERGOUND_PASSAGE_LENGTH, dir, random);
                if (AntUtils.getDist(tempPos, entranceBottom) > UNDERGOUND_ROOM_SIZE / 2) {
                    roomPosList.add(tempPos);
                }
            }
        }
        for(BlockPos tempPos : roomPosList){
            returnList.addAll(generatePassageBlueprint(new CheckableBlockPosPath(crixia,tempPos,this.level),passageWidth));
            returnList.addAll(generateRoomBlueprint(3d,24,tempPos,random));
            if(((int)AntUtils.getDist(entranceBottom, tempPos)) <= UNDERGOUND_PASSAGE_LENGTH){
                returnList.addAll(generatePassageBlueprint(new CheckableBlockPosPath(entranceBottom,tempPos,this.level),passageWidth));
            }
        }
        returnList.addAll(generateRoomBlueprint(2.1d,24,crixia,random));


        //makes the queen's room
        chosenMiddleRoom = roomPosList.get(random.nextInt(roomPosList.size()));
        ArrayList<PosSpherePair> tempList = generatePassageBlueprint(new CheckableBlockPosPath(chosenMiddleRoom,queenRoomPos, this.level), 1);
        BlockPos queenRoomPos1 = (queenRoomPos == null || queenRoomPos == BlockPos.ZERO)? findPosForDir(chosenMiddleRoom,30,baseDir,random).below(10) : tempList.get(tempList.size()/2).blockPos;
        queenRoomPos = (queenRoomPos == null || queenRoomPos == BlockPos.ZERO)? findPosForDir(queenRoomPos1,25,baseDir,random) : queenRoomPos;
        returnList.addAll(generatePassageBlueprint(new CheckableBlockPosPath(chosenMiddleRoom,queenRoomPos1,level),passageWidth));
        returnList.addAll(generatePassageBlueprint(new CheckableBlockPosPath(queenRoomPos1,queenRoomPos,level),passageWidth));
        returnList.addAll(generateRoomBlueprint(5,24,queenRoomPos.above(2),random));

        for(ServerPlayer player : level.getServer().getPlayerList().getPlayers()){
            String thingToSay = "Created colony Blueprint, containing " + returnList.size() + " total spheres." +
                    " The Queen's room is located at " + queenRoomPos.getX() + ',' + queenRoomPos.getY() + ',' + queenRoomPos.getZ() + "." +
                    " The crixia is located at " + crixia.getX() + ',' + crixia.getY() + ',' + crixia.getZ() + ".";
            player.sendMessage(new TextComponent(thingToSay), player.getUUID());
        }

        ((ServerLevelUtil) (level)).refreshColonyForID(this);
        this.entranceBottom = entranceBottom;
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


    /**
     * New colony generation below this line!
     * **/

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////



}