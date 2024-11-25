package com.daringworm.antmod.block.custom;

import com.daringworm.antmod.entity.Ant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FertileAir extends AirBlock {
    public FertileAir(Properties p_48756_) {
        super(p_48756_);
    }

    @Override
    public boolean isRandomlyTicking(BlockState pState) {
        return true;
    }

    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {
        BlockState stateUnder = pLevel.getBlockState(pPos.below());
        if(stateUnder.getBlock() instanceof BonemealableBlock){
            List<Ant> antList = pLevel.getEntitiesOfClass(Ant.class, AABB.ofSize(Vec3.atCenterOf(pPos),2,2,2));
            boolean badAnts = false;
            for(Ant tempAnt : antList){if(tempAnt.blockPosition() == pPos){badAnts = true;}}
            if(!badAnts) {
                BonemealableBlock block = (BonemealableBlock) stateUnder.getBlock();
                block.performBonemeal(pLevel, pRandom, pPos, pState);
            }
        }
        pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(),2);
    }
}
