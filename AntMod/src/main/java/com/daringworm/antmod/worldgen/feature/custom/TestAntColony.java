package com.daringworm.antmod.worldgen.feature.custom;

import com.daringworm.antmod.colony.AntColony;
import com.daringworm.antmod.mixin.tomixin.ServerLevelUtil;
import com.daringworm.antmod.util.AntUtils;
import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class TestAntColony extends Feature<NoneFeatureConfiguration> {
    public TestAntColony(Codec<NoneFeatureConfiguration> config) {
        super(config);
    }

    private BlockPos getMiddlePos(BlockPos rawPos){
        return new BlockPos((rawPos.getX() / 16) * 16 + 8, rawPos.getY(), (rawPos.getZ() / 16) * 16 + 8);
    }

    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> p_160368_) {
        WorldGenLevel genLevel = p_160368_.level();
        BlockPos originPos = getMiddlePos(p_160368_.origin());

        ServerLevel pLevel = Minecraft.getInstance().getSingleplayerServer().getLevel(Level.OVERWORLD);

        ServerLevelUtil colonyUtil = (ServerLevelUtil)pLevel;
        AntColony closestColony = colonyUtil.getClosestColony(getMiddlePos(originPos));

        if(closestColony != null && AntUtils.getHorizontalDist(closestColony.startPos, originPos) < 16*300) {
            int j = 10;
            if(getMiddlePos(closestColony.startPos).equals(originPos)){
                j = 50;
            }
            for (int i = 0; i <= j; i++) {
                genLevel.setBlock(originPos.above(i), Blocks.TNT.defaultBlockState(), 32);
            }
        }

        return true;
    }
}
