package com.daringworm.antmod.block.custom;

import com.daringworm.antmod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.Random;

public class FungusGarden extends Block {
    public static final BooleanProperty IS_GREEN = BlockStateProperties.POWERED;

    public FungusGarden(Properties p_49795_) {
        super(p_49795_);
        this.registerDefaultState(this.stateDefinition.any().setValue(IS_GREEN, true));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(IS_GREEN);
    }


    @Override
    public boolean isRandomlyTicking(BlockState pState) {
        return true;
    }

    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {
        //pLevel.setBlock(pPos, ModBlocks.FUNGUS_GARDEN.get().defaultBlockState().setValue(IS_GREEN, false), 2);
    }
}
