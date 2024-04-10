package com.daringworm.antmod.block.custom;

import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.Map;

public class FungusFuzz extends Block {

    public static final BooleanProperty UP = PipeBlock.UP;
    public static final BooleanProperty DOWN = PipeBlock.DOWN;
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty WEST = PipeBlock.WEST;



    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(UP);
        pBuilder.add(DOWN);
        pBuilder.add(NORTH);
        pBuilder.add(SOUTH);
        pBuilder.add(EAST);
        pBuilder.add(WEST);
    }

    public FungusFuzz(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().
                setValue(UP, false).
                setValue(DOWN, true).
                setValue(NORTH, false).
                setValue(SOUTH, false).
                setValue(EAST, false).
                setValue(WEST, false));
    }

}
