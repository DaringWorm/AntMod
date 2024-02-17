package com.daringworm.antmod.block.custom;

import com.daringworm.antmod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class AntAir extends Block {

    public AntAir(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
        return RenderShape.INVISIBLE;
    }
    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return Shapes.empty();
    }
    @Override
    public boolean isRandomlyTicking(@NotNull BlockState pState){
        return true;
    }

    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {
        /*//if(!pLevel.isAreaLoaded(pPos, 3)) {
            for (Direction dir : Direction.values()) {
                BlockPos pos = pPos.relative(dir);

                BlockState tempState = pLevel.getBlockState(pos);

                if (tempState.canOcclude() || tempState.isSolidRender(pLevel, pos)) {
                    pLevel.setBlock(pos, ModBlocks.ANT_DIRT.get().defaultBlockState(), 2);
                }
           // }
        }*/
    }
}
