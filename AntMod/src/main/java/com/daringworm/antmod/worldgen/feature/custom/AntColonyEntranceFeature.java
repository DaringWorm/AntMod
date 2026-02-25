package com.daringworm.antmod.worldgen.feature.custom;

import com.daringworm.antmod.util.AntUtils;
import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class AntColonyEntranceFeature extends Feature<NoneFeatureConfiguration> {
    private static final Direction[] DIRECTIONS = Direction.values();

    public AntColonyEntranceFeature(Codec<NoneFeatureConfiguration> p_159834_) {
        super(p_159834_);
    }


    public boolean place(@NotNull FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {

        //AntUtils.broadcastString(featurePlaceContext.level().getLevel(), "Placed a entrance at " + featurePlaceContext.origin());

        return true;
    }
}
