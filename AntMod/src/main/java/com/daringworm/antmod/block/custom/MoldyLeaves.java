package com.daringworm.antmod.block.custom;

import com.daringworm.antmod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class MoldyLeaves extends Block {

    public static final IntegerProperty MOLD_LEVEL = BlockStateProperties.AGE_5;
    static final int MAX_DISTANCE_FROM_CORE = 7;

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
        return true;
    }

    public static void rot(ServerLevel pLevel, BlockPos pPos){
        BlockState pState = pLevel.getBlockState(pPos);

        if(pState.getBlock() instanceof MoldyLeaves){
            //Depletes the amount of juice left in the block of moldy leaves being ticked
            if(pLevel.getRandom().nextFloat(1.0f) < 0.2) {
                if (pState.getValue(MOLD_LEVEL) < 5) {
                    pLevel.setBlock(pPos, pState.setValue(MOLD_LEVEL, pState.getValue(MOLD_LEVEL) + 1), 2);
                } else {
                    pLevel.setBlock(pPos, ModBlocks.ANT_AIR.get().defaultBlockState(), 2);
                }
            }
        }
    }

    static boolean isFungus(Block block){
        return block == ModBlocks.FUNGAL_CORE.get() ||
                //block == ModBlocks.FUNGUS_CARPET.get() ||
                block == ModBlocks.FUNGAL_NODULE.get() ||
                block == ModBlocks.MOLDY_LEAVES.get() ||
                block == ModBlocks.FUNGUS_FUZZ.get() ||
                block == ModBlocks.WING_DEBRIS.get() ||
                block == ModBlocks.FUNGUS.get();
    }


    @Override
    public void randomTick(@NotNull BlockState pState, @NotNull ServerLevel pLevel, @NotNull BlockPos pPos, @NotNull Random pRandom) {

        // finds a nearby fungal core to tick
        BlockPos fungalMatrixReferencePos = BlockPos.findClosestMatch(pPos,12,12, p -> pLevel.getBlockState(p).getBlock() == ModBlocks.FUNGAL_CORE.get()).orElse(BlockPos.ZERO);

        //if it exists, tick it and rot
        if(fungalMatrixReferencePos != BlockPos.ZERO){
            BlockState tempState = pLevel.getBlockState(fungalMatrixReferencePos);
            FungalCore.grow(pLevel,fungalMatrixReferencePos, 1);
            rot(pLevel,pPos);
        }
        else{
            pLevel.setBlock(pPos, ModBlocks.FUNGAL_CORE.get().defaultBlockState(), 2);
        }

    }
}
