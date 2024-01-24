package com.daringworm.antmod.block.custom;


import com.daringworm.antmod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


public class FungusBlock extends Block {

    public void grow (BlockPos pPos, LivingEntity pEntity, Level pLevel){
        BlockState pState = pLevel.getBlockState(pPos);
        ItemStack item = pEntity.getMainHandItem();

        if (pState.getValue(AGE)<4 && isFood(item)){
            pLevel.setBlock(pPos, pState.setValue(AGE, pState.getValue(AGE) + 1), 2);
            item.hurtAndBreak(1, pEntity, (p_55287_) -> {p_55287_.broadcastBreakEvent(pEntity.getUsedItemHand());});

            pLevel.playSound((Player)null, pPos, SoundEvents.ARROW_HIT, SoundSource.BLOCKS, 1.0F, 2.0F);

        }
    }

    protected boolean isCopper(BlockState pState){
        Block pBlock = pState.getBlock();
        return pBlock.getSoundType(pState) == SoundType.COPPER || pBlock == Blocks.COPPER_ORE || pBlock == Blocks.DEEPSLATE_COPPER_ORE;
    }

    private boolean isFood(ItemStack pStack) {
        return pStack.is(Items.WHEAT_SEEDS) ||
                pStack.is(Items.BEETROOT_SEEDS) ||
                pStack.is(Items.PUMPKIN_SEEDS) ||
                pStack.is(Items.MELON_SEEDS) ||
                pStack.is(Items.CARVED_PUMPKIN) ||
                pStack.is(Items.WHEAT) ||
                pStack.is(Items.CACTUS) ||
                pStack.is(Items.GRASS) ||
                pStack.is(Items.SEAGRASS) ||
                pStack.is(Items.KELP) ||
                pStack.is(Items.DRIED_KELP) ||
                pStack.is(Items.LEATHER) ||
                pStack.is(Items.FEATHER) ||
                pStack.is(Items.BONE) ||
                pStack.is(Items.FERN) ||
                pStack.is(Items.FERMENTED_SPIDER_EYE) ||
                pStack.is(Items.OAK_SAPLING) ||
                pStack.is(Items.DARK_OAK_SAPLING) ||
                pStack.is(Items.BIRCH_SAPLING) ||
                pStack.is(Items.ACACIA_SAPLING) ||
                pStack.is(Items.JUNGLE_SAPLING) ||
                pStack.is(Items.POPPY) ||
                pStack.is(Items.DANDELION) ||
                pStack.is(Items.CORNFLOWER) ||
                pStack.is(Items.BLUE_ORCHID) ||
                pStack.is(Items.AZURE_BLUET) ||
                pStack.is(Items.LILY_OF_THE_VALLEY) ||
                pStack.is(Items.OXEYE_DAISY) ||
                pStack.is(Items.ALLIUM) ||
                pStack.is(Items.RED_TULIP) ||
                pStack.is(Items.ORANGE_TULIP) ||
                pStack.is(Items.WHITE_TULIP) ||
                pStack.is(Items.PINK_TULIP) ||
                pStack.is(Items.SUNFLOWER) ||
                pStack.is(Items.ROSE_BUSH) ||
                pStack.is(Items.LILAC) ||
                pStack.is(Items.PEONY) ||
                pStack.is(Items.VINE) ||
                pStack.is(Items.LILY_PAD) ||
                pStack.is(Items.TALL_GRASS) ||
                pStack.is(Items.LARGE_FERN) ||
                pStack.is(Items.SEA_PICKLE) ||
                pStack.is(Items.NAUTILUS_SHELL) ||
                pStack.isEdible();
    }

    protected static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{Shapes.empty(), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D)};

    protected static final VoxelShape[] STATICSHAPE = new VoxelShape[]{Shapes.empty(), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D)};

    @Override
    public boolean isRandomlyTicking(BlockState pState) {
        return true;
    }

    public static final IntegerProperty AGE = BlockStateProperties.AGE_5;
    public FungusBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
    }

    public VoxelShape getVisualShape(BlockState pState, BlockGetter pReader, BlockPos pPos, CollisionContext pContext) {
        return STATICSHAPE[1];
    }
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return STATICSHAPE[1];
    }

    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return STATICSHAPE[1];
    }

    public VoxelShape getBlockSupportShape(BlockState pState, BlockGetter pReader, BlockPos pPos) {
        return STATICSHAPE[1];
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
        boolean poisonTest = isCopper(pLevel.getBlockState(pPos.below())) ||
                isCopper(pLevel.getBlockState(pPos.north())) ||
                isCopper(pLevel.getBlockState(pPos.south())) ||
                isCopper(pLevel.getBlockState(pPos.east())) ||
                isCopper(pLevel.getBlockState(pPos.west()));
        if (!poisonTest) {
            if (pLevel.canSeeSky(pPos) && pLevel.isDay()) {
                pLevel.playSound(null, pPos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 2.0F, 1.0F);
                pLevel.setBlock(pPos, Blocks.FIRE.defaultBlockState(), 2);
            } else {
                int j = pState.getValue(AGE);
                if (net.minecraftforge.common.ForgeHooks.onCropsGrowPre(pLevel, pPos, pState, true)) {
                    if (j > 1) {
                        byte add = 0;
                        boolean xOrz = ThreadLocalRandom.current().nextBoolean();
                        boolean posOrNeg = ThreadLocalRandom.current().nextBoolean();

                        //sets the number to add to coords for random pos
                        if (posOrNeg) {
                            add = 1;
                        } else {
                            add = -1;
                        }

                        //creates an adjacent random pos for spreading
                        BlockPos newPos;

                        if (xOrz) {
                            newPos = new BlockPos(pPos.getX() + add, pPos.getY(), pPos.getZ());
                        } else {
                            newPos = new BlockPos(pPos.getX(), pPos.getY(), pPos.getZ() + add);
                        }
                        //sets y coord distribution, checks and maybe places
                        int yDist;
                        for (yDist = 1; yDist >= -1; yDist--) {
                            BlockPos newPos2 = new BlockPos(newPos.getX(), newPos.getY() + yDist, newPos.getZ());
                            BlockState blockstate = pLevel.getBlockState(newPos2);
                            BlockState blockstate2 = pLevel.getBlockState(newPos2.below());
                            //decides if it wants to spread to the pos given from the above
                            if (blockstate.getBlock() == ModBlocks.FUNGUS_BLOCK.get() || blockstate.getRenderShape() == RenderShape.INVISIBLE) {

                                boolean copperTest = isCopper(pLevel.getBlockState(newPos.below())) ||
                                        isCopper(pLevel.getBlockState(newPos.north())) ||
                                        isCopper(pLevel.getBlockState(newPos.south())) ||
                                        isCopper(pLevel.getBlockState(newPos.east())) ||
                                        isCopper(pLevel.getBlockState(newPos.west()));
                                //if its air:
                                if (blockstate.getRenderShape() == RenderShape.INVISIBLE &&
                                        Block.isFaceFull(blockstate2.getCollisionShape(pLevel, newPos2.below()), Direction.UP) &&
                                        !pLevel.canSeeSky(newPos2)) {
                                    if (!copperTest) {
                                        pLevel.setBlock(newPos2, pState.setValue(AGE, 0), 2);
                                        pLevel.setBlock(pPos, pState.setValue(AGE, j - 1), 2);
                                        pLevel.updateNeighborsAt(pPos, this);
                                    }
                                }
                                //if it's upgrading an existing fungal block:
                                else if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
                                    if (!copperTest) {
                                        int newage = blockstate.getValue(AGE);
                                        if (j >= newage + 2) {
                                            pLevel.setBlock(pPos, pState.setValue(AGE, j - 1), 2);
                                            pLevel.setBlock(newPos2, pState.setValue(AGE, newage + 1), 2);
                                            pLevel.updateNeighborsAt(pPos, this);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else{
            pLevel.setBlock(pPos, ModBlocks.ANT_AIR.get().defaultBlockState(), 2);
            pLevel.playSound(null,pPos,SoundEvents.RESPAWN_ANCHOR_CHARGE,SoundSource.BLOCKS,1f,1f);
        }
    }


    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);

            if (pState.getValue(AGE)<4 && isFood(itemstack)){
                pLevel.setBlock(pPos, pState.setValue(AGE, pState.getValue(AGE) + 1), 2);
                itemstack.hurtAndBreak(1, pPlayer, (p_55287_) -> {p_55287_.broadcastBreakEvent(pHand);});

                pLevel.playSound((Player)null, pPos, SoundEvents.BAT_TAKEOFF, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            return InteractionResult.CONSUME;
    }


    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(AGE);
    }
    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        BlockState belowState = pLevel.getBlockState(pPos.below());
        return isFaceFull(belowState.getCollisionShape(pLevel, pPos.below()), Direction.UP) && super.canSurvive(pState, pLevel, pPos);
    }
}