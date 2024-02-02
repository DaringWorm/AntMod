package com.daringworm.antmod.mixin.mixins;

import com.daringworm.antmod.colony.AntColony;
import com.daringworm.antmod.colony.LevelColonies;
import com.daringworm.antmod.mixin.tomixin.ServerLevelUtil;
import com.google.common.collect.Lists;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(ServerLevel.class)
public abstract class ServerClassMixins implements ServerLevelUtil {


    @Final
    @Shadow
    final List<ServerPlayer> players = Lists.newArrayList();

    public LevelColonies<AntColony> levelColonies = new LevelColonies<>(new HashSet<>());

    private File lastSaveFile;

    @Override
    public AntColony getColonyWithID(int pID){return levelColonies.getColonyForID(pID);}
    @Override
    public void addColonyList(Set<AntColony> pSet){levelColonies = new LevelColonies<>(pSet);}
    @Override
    public void addColonyToList(AntColony pColony){levelColonies.add(pColony);}
    @Override
    public void refreshColonyForID(AntColony pColony){
        levelColonies.update(pColony);
        pColony.save();
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void createLevelColonyList(CallbackInfo ci) throws IOException {
        Component component = Component.nullToEmpty("colonies empty");
        if(!players.isEmpty()) {
            ServerLevel pLevel = players.get(0).getLevel();
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

