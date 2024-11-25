package com.daringworm.antmod.block.custom;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.colony.misc.BlockPosStringifier;
import com.daringworm.antmod.goals.AntUtils;
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
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
        return true;//pState.getValue(MOLD_LEVEL) < 5;
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

    private boolean isFungus(Block block){
        return block == ModBlocks.FUNGAL_CORE.get() ||
                //block == ModBlocks.FUNGUS_CARPET.get() ||
                block == ModBlocks.FUNGAL_NODULE.get() ||
                block == ModBlocks.MOLDY_LEAVES.get() ||
                block == ModBlocks.FUNGUS_FUZZ.get() ||
                block == ModBlocks.WING_DEBRIS.get() ||
                block == ModBlocks.FUNGUS.get();
    }

    private BlockPos blockToSpreadTo(BlockPos nucleusPos, BlockPos selfPos, ServerLevel pLevel){

        //expand the fungus-based search area, and find the nearest air-like positions. Filter through these later.

        ArrayList<BlockPos> searchedPoses = new ArrayList<>();
        ArrayList<BlockPos> currentPoses = new ArrayList<>();
        ArrayList<BlockPos> nextPoses = new ArrayList<>();
        ArrayList<BlockPos> airPoses = new ArrayList<>();
        currentPoses.add(selfPos);

        int searchSize = 6;

        for(; searchSize >= 0; searchSize--) {
            for (BlockPos tempPos : currentPoses) {
                for (Direction dir : Direction.values()) {
                    BlockPos tempPos1 = tempPos.relative(dir, 1);
                    BlockState tempState1 = pLevel.getBlockState(tempPos1);
                    if (isFungus(tempState1.getBlock())) {
                        if(!searchedPoses.contains(tempPos1)) {
                            nextPoses.add(tempPos1);
                        }
                    }
                    else if(tempState1.canBeReplaced(Fluids.FLOWING_WATER)){
                        airPoses.add(tempPos1);
                    }

                    if(!searchedPoses.contains(tempPos1)){
                        searchedPoses.add(tempPos1);
                    }
                }
            }
            currentPoses.clear();
            currentPoses.addAll(nextPoses);
            nextPoses.clear();
        }

        //AntUtils.broadcastString(pLevel, "Searched size = " + searchedPoses.size() + ". Current size = " + currentPoses.size());


        // Filter the air positions found to find ones which can be safely replaced.

        int baseX = nucleusPos.getX();
        int baseY = nucleusPos.getY();
        int baseZ = nucleusPos.getZ();

        int tileSizeXZ = 5;
        int tileSizeY = 3;
        int halfTileXZ = 2;

        int xOff = (baseX % tileSizeXZ);
        int yOff = (baseY % tileSizeY);
        int zOff = (baseZ % tileSizeXZ);

        xOff = (xOff < 0)? xOff + 2 : xOff - 2;
        zOff = (zOff < 0)? zOff + 2 : zOff - 2;

        BlockPos returnPos = BlockPos.ZERO;

        for(BlockPos tempPos : airPoses){
            int x = tempPos.getX();
            int y = tempPos.getY();
            int z = tempPos.getZ();

            int modX = ((x+tileSizeXZ*5)%tileSizeXZ)+xOff;
            int modZ = ((z+tileSizeXZ*5)%tileSizeXZ)+zOff;
            int modY = ((y+1+tileSizeY*400)%tileSizeY) + yOff;

            boolean shouldPlace = true;

            //creates the main "t" shaped hallways
            if(((modX==xOff) || (modZ==zOff)) && (modY==yOff || modY==yOff+1)){
                shouldPlace = false;

                //
                //pLevel.setBlock(tempPos, Blocks.GLASS.defaultBlockState(),2);
                //
            }
            //creates the middle stairs
            else if((modX == xOff+halfTileXZ || modX == xOff-halfTileXZ)
                    && (modZ == zOff+halfTileXZ || modZ == zOff-halfTileXZ)
                    && modY != yOff+1){
                shouldPlace = false;

                //
                //pLevel.setBlock(tempPos, Blocks.YELLOW_STAINED_GLASS.defaultBlockState(),2);
                //
            }
            //creates the bottom stairs
            else if((modX == xOff+halfTileXZ || modX == xOff-halfTileXZ)
                    && (modZ == zOff+halfTileXZ-1 || modZ == zOff-halfTileXZ+1)
                    && modY != yOff){
                shouldPlace = false;
                //
                //pLevel.setBlock(tempPos, Blocks.LIME_STAINED_GLASS.defaultBlockState(),2);
                //
            }
            //creates the top stairs
            else if((modX == xOff+halfTileXZ-1 || modX == xOff-halfTileXZ+1)
                    && (modZ == zOff+halfTileXZ || modZ == zOff-halfTileXZ)
                    && modY != yOff+2){
                shouldPlace = false;
                //
                //pLevel.setBlock(tempPos, Blocks.ORANGE_STAINED_GLASS.defaultBlockState(),2);
                //
            }

            if(AntUtils.getDist(nucleusPos, returnPos) > AntUtils.getDist(nucleusPos,tempPos) && shouldPlace){
                returnPos = tempPos;
            }
        }
        return returnPos;
    }

    @Override
    public void randomTick(@NotNull BlockState pState, @NotNull ServerLevel pLevel, @NotNull BlockPos pPos, @NotNull Random pRandom) {

        // figures out how to orient the fungal superstructure. Defaults to ZERO
        BlockPos fungalMatrixReferencePos = BlockPos.findClosestMatch(pPos,12,12, p -> pLevel.getBlockState(p).getBlock() == ModBlocks.FUNGAL_CORE.get()).orElse(BlockPos.ZERO);

        //figures out which block to try to grow to
        BlockPos growthPos = blockToSpreadTo(fungalMatrixReferencePos, pPos, pLevel);

        //grows!
        if(growthPos != BlockPos.ZERO) {
            pLevel.setBlock(growthPos, ModBlocks.FUNGUS.get().defaultBlockState(), 2);
            rot(pLevel,pPos);
        }
        else{
            BlockPos nodulePos = BlockPos.findClosestMatch(pPos,5,5, p -> pLevel.getBlockState(p).getBlock() == ModBlocks.FUNGUS.get()).orElse(BlockPos.ZERO);
            if(nodulePos != BlockPos.ZERO){
                pLevel.setBlock(nodulePos,ModBlocks.FUNGAL_NODULE.get().defaultBlockState(), 2);
                rot(pLevel, pPos);
            }
        }

        //AntUtils.broadcastString(pLevel, "Fungus growth tick. Placed at " + BlockPosStringifier.getTagForPos(growthPos));

    }
}
