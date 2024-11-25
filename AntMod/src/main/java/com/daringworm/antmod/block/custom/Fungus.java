package com.daringworm.antmod.block.custom;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.goals.AntUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class Fungus extends Block {
    public Fungus(Properties p_49795_) {
        super(p_49795_);
    }

    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
    }

    @Override
    public boolean isRandomlyTicking(BlockState pState) {
        return false;
    }

    private Direction findAirDirection(ServerLevel pLevel, BlockPos pPos){
        for(Direction dir : Direction.values()){
            BlockPos tempPos = pPos.relative(dir);
            BlockState tempState = pLevel.getBlockState(tempPos);

            if(tempState.isAir()){
                return dir;
            }
        }
        return null;
    }

    @Override
    public void randomTick(@NotNull BlockState pState, @NotNull ServerLevel pLevel, @NotNull BlockPos pPos, @NotNull Random pRandom) {
        /*BlockPos moldyLeavesPos = BlockPos.findClosestMatch(pPos,3,3, p -> pLevel.getBlockState(p).getBlock() == ModBlocks.MOLDY_LEAVES.get()).orElse(BlockPos.ZERO);

        //AntUtils.broadcastString(pLevel, "Tick tock the fungus block");

        if(moldyLeavesPos != BlockPos.ZERO && pLevel.getRandom().nextFloat(1.0f) < 0.1f){
            pLevel.setBlock(pPos,ModBlocks.FUNGAL_NODULE.get().defaultBlockState(), 2);

            MoldyLeaves.rot(pLevel,moldyLeavesPos);
        }*/
    }
}
