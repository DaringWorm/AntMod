package com.daringworm.antmod.block.custom;


import com.daringworm.antmod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.daringworm.antmod.block.custom.FungusCarpet.AGE;


public class LeafyMixture extends Block {
    protected static final VoxelShape[] SHAPE_BY_CONSUMPTION = new VoxelShape[]{Shapes.empty(),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D)};

    protected boolean isCopper(BlockState pState){
        Block pBlock = pState.getBlock();
        return pBlock.getSoundType(pState) == SoundType.COPPER || pBlock == Blocks.COPPER_ORE || pBlock == Blocks.DEEPSLATE_COPPER_ORE;
    }

    @Override
    public boolean isRandomlyTicking(BlockState pState) {
        return true;
    }

    public static final IntegerProperty CONSUMPTION = BlockStateProperties.RESPAWN_ANCHOR_CHARGES;

    public LeafyMixture(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(CONSUMPTION, 0));
    }

    public VoxelShape getVisualShape(BlockState pState, BlockGetter pReader, BlockPos pPos, CollisionContext pContext) {
        return SHAPE_BY_CONSUMPTION[pState.getValue(CONSUMPTION)+1];
    }

    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE_BY_CONSUMPTION[pState.getValue(CONSUMPTION)+1];
    }

    public VoxelShape getBlockSupportShape(BlockState pState, BlockGetter pReader, BlockPos pPos) {
        return SHAPE_BY_CONSUMPTION[pState.getValue(CONSUMPTION)+1];
    }

    public VoxelShape getInteractionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return SHAPE_BY_CONSUMPTION[pState.getValue(CONSUMPTION)+1];
    }

    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return pState.getValue(CONSUMPTION)>3;
    }



    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {
        if (!pState.canSurvive(pLevel, pPos)) {
            pLevel.destroyBlock(pPos, true);
        }
    }

    /**
     * Performs a random tick on a block.
     */
    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {
        BlockState baseFungusState = ModBlocks.FUNGUS_CARPET.get().defaultBlockState();
        boolean shouldConsume = false;
        if (pLevel.canSeeSky(pPos.above()) && pLevel.isDay()) {
            pLevel.playSound(null, pPos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 2.0F, 1.0F);
            pLevel.setBlock(pPos, ModBlocks.ANT_DEBRIS.get().defaultBlockState(), 2);
        }
        else {
            int searchRadius = 2;
            List<BlockPos> fungusPosList = new ArrayList<>();
            List<BlockPos> fungusOnList = new ArrayList<>();
            fungusOnList.add(pPos.below());
            fungusOnList.add(pPos.north());
            fungusOnList.add(pPos.south());
            fungusOnList.add(pPos.east());
            fungusOnList.add(pPos.west());

            for (int x = searchRadius; x >= -searchRadius; x--) {
                for (int y = 2; y >= -2; y--) {
                    for (int z = searchRadius; z >= -searchRadius; z--) {
                        BlockPos tempPos = new BlockPos(x + pPos.getX(), y + pPos.getY(), z + pPos.getZ());
                        if (pLevel.getBlockState(tempPos).getBlock() == ModBlocks.FUNGUS_CARPET.get()) {
                            fungusPosList.add(tempPos);
                            fungusOnList.add(tempPos.below());
                            fungusOnList.add(tempPos.north());
                            fungusOnList.add(tempPos.south());
                            fungusOnList.add(tempPos.east());
                            fungusOnList.add(tempPos.west());
                        }
                    }
                }
            }
            fungusPosList.removeIf(tempPos -> pLevel.getBlockState(tempPos).getValue(AGE)>3);

            fungusOnList.removeIf(tempPos -> pLevel.getBlockState(tempPos).getBlock() == ModBlocks.FUNGUS_CARPET.get() ||
                    pLevel.getBlockState(tempPos).getRenderShape() != RenderShape.INVISIBLE);


            if (!fungusPosList.isEmpty() || pState.getValue(CONSUMPTION) > 0) {

                if (ForgeHooks.onCropsGrowPre(pLevel, pPos, pState, true)) {
                    BlockPos spreadPos = BlockPos.ZERO;
                    if(!fungusPosList.isEmpty()) {
                        for (int i = 0; i < searchRadius * searchRadius; i++) {
                            double random = Math.random();
                            double listPosRough = random * fungusPosList.size();
                            int listPos = (int) Math.round(listPosRough);
                            if (fungusPosList.size() - 1 < listPos) {
                                listPos--;
                            }
                            BlockPos tempPos = fungusPosList.get(listPos);
                            if (pLevel.getBlockState(tempPos).getValue(AGE) < 4) {
                                spreadPos = tempPos;
                                i = searchRadius * searchRadius;
                            }
                        }
                    }

                    if (spreadPos != BlockPos.ZERO) {
                        BlockState blockstate = pLevel.getBlockState(spreadPos);
                        BlockState blockstate2 = pLevel.getBlockState(spreadPos.below());
                        boolean copperTest = isCopper(pLevel.getBlockState(spreadPos.below())) ||
                                isCopper(pLevel.getBlockState(spreadPos.north()))||
                                isCopper(pLevel.getBlockState(spreadPos.south()))||
                                isCopper(pLevel.getBlockState(spreadPos.east()))||
                                isCopper(pLevel.getBlockState(spreadPos.west()));

                        //decides if it wants to spread to the pos given from the above
                        if (blockstate.getBlock() == ModBlocks.FUNGUS_CARPET.get() || getRenderShape(blockstate) == RenderShape.INVISIBLE) {
                            //if its air:
                            if (getRenderShape(blockstate) == RenderShape.INVISIBLE && Block.isFaceFull(blockstate2.getCollisionShape(pLevel, spreadPos.below()), Direction.UP) &&
                                    !pLevel.canSeeSky(spreadPos) && !copperTest) {
                                pLevel.setBlock(spreadPos, baseFungusState.setValue(AGE, 0), 2);
                                shouldConsume = true;
                                pLevel.updateNeighborsAt(pPos, this);

                            }
                            //if it's upgrading an existing fungal block:
                            else if (blockstate.getBlock() == ModBlocks.FUNGUS_CARPET.get() && blockstate.getValue(AGE) < 4) {
                                int newage = blockstate.getValue(AGE);
                                if(copperTest){
                                    if(newage>0) {
                                        pLevel.setBlock(spreadPos, baseFungusState.setValue(AGE, newage - 1), 2);
                                    }
                                    else{
                                        pLevel.setBlock(spreadPos, ModBlocks.ANT_AIR.get().defaultBlockState(), 2);
                                        pLevel.playSound(null, spreadPos,SoundEvents.RESPAWN_ANCHOR_CHARGE,SoundSource.BLOCKS,1f,1f);
                                    }
                                }
                                else {
                                    shouldConsume = true;
                                    pLevel.setBlock(spreadPos, baseFungusState.setValue(AGE, newage + 1), 2);
                                    pLevel.updateNeighborsAt(pPos, this);
                                }

                            }
                        }
                    }
                }
            }

            if (shouldConsume) {
                if (pState.getValue(CONSUMPTION) < 4) {
                    pLevel.setBlock(pPos, ModBlocks.LEAFY_MIXTURE.get().defaultBlockState().setValue(CONSUMPTION, pState.getValue(CONSUMPTION) + 1), 2);
                } else {
                    pLevel.setBlock(pPos, ModBlocks.FUNGUS_CARPET.get().defaultBlockState().setValue(AGE, 4), 2);
                }
            }
        }
    }


        protected void createBlockStateDefinition (StateDefinition.Builder < Block, BlockState > pBuilder){
            pBuilder.add(CONSUMPTION);
        }
        @Override
        public boolean canSurvive (BlockState pState, LevelReader pLevel, BlockPos pPos){
            BlockState belowState = pLevel.getBlockState(pPos.below());
            return isFaceFull(belowState.getCollisionShape(pLevel, pPos.below()), Direction.UP) && super.canSurvive(pState, pLevel, pPos);

    }

}