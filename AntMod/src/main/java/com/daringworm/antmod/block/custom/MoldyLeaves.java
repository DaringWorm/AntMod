package com.daringworm.antmod.block.custom;

import com.daringworm.antmod.block.ModBlocks;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class MoldyLeaves extends Block {

    public static final IntegerProperty MOLD_LEVEL = BlockStateProperties.AGE_5;

    protected void createBlockStateDefinition (StateDefinition.Builder < Block, BlockState> pBuilder){
        pBuilder.add(MOLD_LEVEL);
    }

    public MoldyLeaves(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(MOLD_LEVEL, 0));
    }

    public static boolean isCopper(BlockState pState){
        String id = pState.getBlock().getDescriptionId();
        return id.contains("copper");
    }

    @Override
    public boolean isRandomlyTicking(BlockState pState) {
        return pState.getValue(MOLD_LEVEL) < 5;
    }

    @Override
    public void randomTick(@NotNull BlockState pState, @NotNull ServerLevel pLevel, @NotNull BlockPos pPos, @NotNull Random pRandom) {
        Direction dir = Lists.newArrayList(Direction.values()).get(pLevel.getRandom().nextInt(6));

        MultifaceBlock multiFB = (MultifaceBlock)ModBlocks.FUNGUS_FUZZ.get();

        BlockState state = multiFB.getStateForPlacement(pLevel.getBlockState(pPos.relative(dir)), pLevel, pPos.relative(dir), dir.getOpposite());
        if(state != null) {
            pLevel.setBlock(pPos.relative(dir), state, 2);
        }
    }

}
