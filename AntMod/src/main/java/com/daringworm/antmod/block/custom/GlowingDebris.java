package com.daringworm.antmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Random;

public class GlowingDebris extends Block {

    BlockPos findSpreadPos(BlockPos startPoint, Block lookingFor, Level pLevel){
        BlockPos chosenPos = BlockPos.ZERO;
        for(int x=1; x>=-1; x--){
            for (int y =1; y>=-1;y--){
                for (int z = 1; z>=-1;z--){
                    BlockPos tempPos = new BlockPos(x+startPoint.getX(),y+startPoint.getY(),z+startPoint.getZ());
                    //pLevel.get
                }
            }
        }
        return chosenPos;
    }


    @Override
    public boolean isRandomlyTicking(BlockState pState) {
        return true;
    }

    public GlowingDebris(BlockBehaviour.Properties properties) {
        super(properties);
    }

    protected static final VoxelShape[] BOX = new VoxelShape[]{Shapes.empty(), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D)};


    public VoxelShape getShape(BlockState pState, BlockGetter pReader, BlockPos pPos, CollisionContext pContext) {
        return BOX[1];
    }


    /**
     * Performs a random tick on a block.
     */
    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {


        if (pLevel.getLightEngine().getRawBrightness(pPos, 8)> 0) {

        }
    }
}
