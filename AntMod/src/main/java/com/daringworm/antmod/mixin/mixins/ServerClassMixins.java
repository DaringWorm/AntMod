package com.daringworm.antmod.mixin.mixins;

import com.daringworm.antmod.colony.AntColony;
import com.daringworm.antmod.colony.LevelColonies;
import com.daringworm.antmod.util.AntUtils;
import com.daringworm.antmod.mixin.tomixin.ServerLevelUtil;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(ServerLevel.class)
public abstract class ServerClassMixins implements ServerLevelUtil {

    @Final
    @Shadow
    final List<ServerPlayer> players = Lists.newArrayList();

    @Shadow public abstract ServerLevel getLevel();

    public LevelColonies<AntColony> levelColonies = new LevelColonies<>(new HashSet<>());

    private File lastSaveFile;

    @Override
    public ArrayList<AntColony> getColonies(){
        return new ArrayList<>(levelColonies.getColonies().toList());
    }
    @Override
    public AntColony getColonyWithID(int pID){return levelColonies.getColonyForID(pID);}
    @Override
    public AntColony getFirstColony(){return levelColonies.getColonies().toList().get(0);}
    @Override
    public AntColony getOrCreateColonyForPos(BlockPos pos){
        for(AntColony tempColony : getColonies()){
            if(tempColony.startPos.equals(pos)){
                return tempColony;
            }
        }
        AntColony newColony = new AntColony(getLevel(), AntUtils.randFromPos(pos).nextInt(), pos);
        newColony.save();

        return newColony;
    }
    @Override
    public void addColonyList(Set<AntColony> pSet){levelColonies = new LevelColonies<>(pSet);}
    @Override
    public void addColonyToList(AntColony pColony){
        levelColonies.add(pColony);
        pColony.save();
    }
    @Override
    public int getNumberOfColonies(){return levelColonies.size();}
    @Override
    public void removeColonyFromList(AntColony pColony){
        pColony.delete();
        levelColonies.remove(pColony);
    }
    @Override
    public void loadColoniesFromFile(ServerLevel pLevel) throws IOException {
        Component component = Component.nullToEmpty("colonies empty");


        //this.spawnAnts();

        String levelName = pLevel.toString();
        levelName = levelName.replace(".","_");
        levelName = levelName.replace("]","");
        levelName = levelName.replace("[","");
        levelName = levelName.replaceAll("ServerLevel","");


        String worldFilePath = pLevel.getServer().getServerDirectory().getPath() + "/saves/" + levelName + "/data/ant_colonies";
        File colonyStorageDirFile = new File(worldFilePath);
        boolean fileIsValid = colonyStorageDirFile.mkdirs();

        if (levelColonies.isEmpty() || !colonyStorageDirFile.equals(lastSaveFile)) {
            levelColonies = createLevelColonies(colonyStorageDirFile,pLevel);
            lastSaveFile = colonyStorageDirFile;
        }
        else {
            Set<String> test = getColonyFileNames(colonyStorageDirFile);
            component = Component.nullToEmpty(levelColonies.size()+" Colonies successfully loaded" + ", " + test.size() + " Files were found.");
        }
        for (ServerPlayer serverplayer : players) {
            if(serverplayer.getMainHandItem().getItem() == Items.BLAZE_ROD) {
                serverplayer.displayClientMessage(component, false);
            }
        }
    }

    @Override
    public void refreshColonyForID(AntColony pColony){
        levelColonies.update(pColony);
        //pColony.save();
    }
    @Override
    public AntColony getClosestColony(BlockPos pos){
        ArrayList<AntColony> colonies = new ArrayList<>(levelColonies.getColonies().toList());
        if(colonies.isEmpty()){return null;}
        AntColony currentNearest = colonies.get(0);
        for(AntColony temp : colonies){
            if(AntUtils.getDist(temp.startPos,pos) < AntUtils.getDist(currentNearest.startPos,pos)){
                currentNearest = temp;
            }
        }
        return currentNearest;
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void tickColonyDoohickeys(CallbackInfo ci){

    }


    private Set<String> getColonyFileNames(File directory) throws IOException {

        try (Stream<Path> stream = Files.list(Paths.get(directory.getPath()))) {
            return stream.filter(file -> !Files.isDirectory(file)).map(Path::getFileName).map(Path::toString).collect(Collectors.toSet());
        }
    }

    private LevelColonies<AntColony> createLevelColonies(File colonyStorageDir, ServerLevel pLevel) throws IOException {
        Set<AntColony> toReturn = new HashSet<>();
        Set<String> colonyFileSet = getColonyFileNames(colonyStorageDir);
        for(String name : colonyFileSet){
            toReturn.add(new AntColony(colonyStorageDir,name,pLevel));
        }

        return new LevelColonies<>(toReturn);
    }
}

