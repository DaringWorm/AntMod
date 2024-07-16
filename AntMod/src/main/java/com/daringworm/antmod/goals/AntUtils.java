package com.daringworm.antmod.goals;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.block.entity.custom.FungalContainerBlockEntity;
import com.daringworm.antmod.entity.Ant;
import com.daringworm.antmod.entity.custom.WorkerAnt;
import com.daringworm.antmod.colony.misc.PosPair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class AntUtils {

    public static Ant findNearbyEnemyAnts(Ant pAnt){
        final TargetingConditions attackTargeting = TargetingConditions.forCombat().range(64.0D);
        Ant target = null;
        AABB aabb = pAnt.getBoundingBox().inflate(10.0D, 8.0D, 10.0D);
        List<Ant> list1 = pAnt.level.getNearbyEntities(Ant.class, attackTargeting, pAnt, aabb);

        for (Ant ant1 : list1) {
            int ID = ant1.getColonyID();
            if (ID != pAnt.getColonyID()) {
                if (target == null) {
                    target = ant1;
                } else if (pAnt.distanceToSqr(ant1) < pAnt.distanceToSqr(target)) {
                    target = ant1;
                }
            }
        }
        return target;
    }

    public static int findDigit(int input, int digit){
        int ten = (int) Math.pow(10,digit-1);
        int noBigger = (input%(ten*10)-(input%ten));
        return noBigger/ten;
    }

    public static boolean shouldSnip(BlockPos pPos, Level pLevel) {

        if(pPos == null || pPos == BlockPos.ZERO){return false;}
        BlockState pState = pLevel.getBlockState(pPos);
        if(pLevel.getBlockState(pPos.below()).getBlock() == Blocks.FARMLAND){
            if(pState.getBlock() instanceof CropBlock) {
                if( ((CropBlock)pState.getBlock()).getMaxAge() == pState.getValue(((CropBlock) pState.getBlock()).getAgeProperty())) {
                    return true;
                }
                else return false;
            }
        }

        if(pState.getBlock() == Blocks.NETHER_PORTAL || pState.getBlock() == Blocks.END_PORTAL || !pState.getFluidState().isEmpty()){
            return false;
        }

        return pState.getRenderShape() != RenderShape.INVISIBLE && pState.getDestroySpeed(pLevel, pPos) < 0.1 && !pState.canOcclude();
    }

    private ItemStack getRandomItem(){
        return null;
    }

    public static List<BlockPos> findAllSnippableBlockPos(BlockPos middlePos, int pHorizontal, int pVertical, Ant pAnt){
        List<BlockPos> list = new ArrayList<BlockPos> ();
        for (int x = -(pHorizontal); x <= (pHorizontal); ++x) {
            for (int y = -(pVertical); y <= (pVertical); ++y) {
                for (int z = -(pHorizontal); z <= (pHorizontal); ++z) {
                    double antX = middlePos.getX();
                    double antY = middlePos.getY();
                    double antZ = middlePos.getZ();
                    BlockPos testBlock = new BlockPos(x+antX, y+antY, z+antZ);
                    if (shouldSnip(testBlock,pAnt.getLevel())) {
                        list.add(testBlock);
                    }
                }
            }
        }
        return list;
    }

    public static Set<BlockPos> checkAdjacentsLinearStaired(BlockPos pos, LevelReader level){
        Set<BlockPos> returnSet = new java.util.HashSet<>(Set.of());
        for(Direction dir : Direction.values()) {
            BlockPos directToSide = pos.relative(dir);
            if (isPathfindableWithFloor(directToSide, level)) {
                returnSet.add(directToSide);
            }
            else if (isWalkableUnder(directToSide, level) && isPathfindableWithFloor(directToSide.below(),level)){
                returnSet.add(directToSide.below());
            }
            else if(isWalkableUnder(pos.above(),level) && isPathfindableWithFloor(directToSide.above(),level)){
                returnSet.add(directToSide.above());
            }
        }
        return returnSet;
    }

    private static boolean isWalkableUnder(BlockPos pos, LevelReader level){
        final VoxelShape SUPPORT_SHAPE = Shapes.join(Shapes.block(), Block.box(0.0D, 8.0D, 0.0D, 16.0D, 16.0D, 16.0D), BooleanOp.ONLY_FIRST);

        return !Shapes.joinIsNotEmpty(level.getBlockState(pos).getBlockSupportShape(level, pos).getFaceShape(Direction.DOWN), SUPPORT_SHAPE, BooleanOp.ONLY_SECOND);
    }

    public static boolean isPathfindableWithFloor(BlockPos pos, LevelReader level){
        return level.getBlockState(pos).isPathfindable(level,pos, PathComputationType.LAND) &&
                level.getBlockState(pos.below()).isFaceSturdy(level,pos.below(), Direction.UP, SupportType.RIGID);
    }

    public static List<BlockPos> findBlocksAdjacentTo(Block pBlock, BlockPos middlePos, LevelReader pLevel){
        List<BlockPos> returnList = new ArrayList<>();
        for(int x = -1; x < 2; x++){
            for(int y = -1; y < 2; y++){
                for(int z = -1; z < 2; z++){
                    BlockPos tempPos = new BlockPos(middlePos.getX()+x,middlePos.getY()+y,middlePos.getZ()+z);
                    Block tempBlock = pLevel.getBlockState(tempPos).getBlock();
                    if(tempPos != middlePos && tempBlock == pBlock){
                        returnList.add(tempPos);
                    }
                }
            }
        }
        return returnList;
    }

    public static List<BlockPos> findBlocksAdjacentTo(RenderShape pShape, BlockPos middlePos, LevelReader pLevel, boolean checkunder){
        List<BlockPos> returnList = new ArrayList<>();
        boolean hasChecked = checkunder;
        for(int x = -1; x < 2; x++){
            for(int y = -1; y < 2; y++){
                for(int z = -1; z < 2; z++){
                    BlockPos tempPos = new BlockPos(middlePos.getX()+x,middlePos.getY()+y,middlePos.getZ()+z);
                    BlockState tempState = pLevel.getBlockState(tempPos);
                    Block tempBlock = pLevel.getBlockState(tempPos).getBlock();
                    if(hasChecked){
                        BlockState tempStateUnder = pLevel.getBlockState(tempPos.below());
                        if(!Block.isFaceFull(tempStateUnder.getCollisionShape(pLevel, tempPos.below()), Direction.UP)){
                            hasChecked = false;
                        }
                    }
                    else{
                        hasChecked = true;
                    }
                    if(tempPos != middlePos && tempState.getRenderShape() == pShape && hasChecked){
                        returnList.add(tempPos);
                    }
                    hasChecked = checkunder;
                }
            }
        }
        return returnList;
    }

    public static void wanderRandomly(Ant pAnt){
        Vec3 vec3 = DefaultRandomPos.getPos(pAnt, 20, 7);
        boolean isAboveground = true;
        if(vec3 !=null) {
            double wantedX = vec3.x;
            double wantedY = vec3.y;
            double wantedZ = vec3.z;
            if((isAboveground && pAnt.level.canSeeSky(new BlockPos(wantedX,wantedY,wantedZ)) || !isAboveground)){
                pAnt.getNavigation().moveTo(wantedX, wantedY, wantedZ, 1);
            }
        }
    }

    public static void broadcastString(Level pLevel, String string){
        if(!pLevel.isClientSide()) {
            for (ServerPlayer player : pLevel.getServer().getPlayerList().getPlayers()) {
                player.sendMessage(new TextComponent(string), player.getUUID());
            }
        }
    }

    private static void antAddItem(WorkerAnt holder, BlockEntity pDestination, ItemStack holderStack){
        if (pDestination instanceof FungalContainerBlockEntity) {
            ItemStackHandler inventory = ((FungalContainerBlockEntity) pDestination).itemHandler;
            int slots = 9;
            int checkedslots;
            int chosenslot = -1;
            byte mergetype = 0;
            for (checkedslots = 0; checkedslots <= slots - 1; checkedslots++) {

                if (inventory.getStackInSlot(checkedslots).isEmpty()) {
                    chosenslot = checkedslots;
                    checkedslots = slots - 1;
                    mergetype = 1;
                } else if (inventory.getStackInSlot(checkedslots).getCount() < 64 && inventory.getStackInSlot(checkedslots).getItem() == holderStack.getItem()) {
                    chosenslot = checkedslots;
                    checkedslots = slots - 1;
                    mergetype = 2;
                }
            }

            if (chosenslot > -1 && !holderStack.isEmpty() && mergetype == 1) {
                inventory.setStackInSlot(chosenslot, holderStack);
                pDestination.setChanged();
                holder.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                holderStack = ItemStack.EMPTY;
            }

            if (chosenslot > -1 && !holderStack.isEmpty() && mergetype == 2) {
                ItemStack newStack = new ItemStack(inventory.getStackInSlot(chosenslot).getItem(), inventory.getStackInSlot(chosenslot).getCount() + 1);
                inventory.setStackInSlot(chosenslot, newStack);
                pDestination.setChanged();
                holder.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                holderStack = ItemStack.EMPTY;
            }
        }
    }

    public static BlockPos findNearestContainerToExtract(Ant pAnt, List<BlockPos> posList){
        BlockPos antPos = pAnt.blockPosition();
        BlockPos returnPos = BlockPos.ZERO;
        List<FungalContainerBlockEntity> pList = new ArrayList<>();
        for(BlockPos pPos : posList){
            if(pAnt.getLevel().getBlockState(pPos) == ModBlocks.LEAFY_CONTAINER_BLOCK.get().defaultBlockState()){
                pList.add((FungalContainerBlockEntity) pAnt.getLevel().getBlockEntity(pPos));
            }
        }
        for(FungalContainerBlockEntity pContainerEntity : pList) {
            List<Integer> list16 = new ArrayList<>();
            List<Integer> list6 = new ArrayList<>();
            List<Integer> list3 = new ArrayList<>();
            List<Integer> list1 = new ArrayList<>();
            ItemStackHandler container = pContainerEntity.itemHandler;
            for (int i = 0; i < container.getSlots() - 1; i++) {
                ItemStack tempStack = container.getStackInSlot(i);
                if (isFungusEdible(tempStack)) {
                    int count = tempStack.getCount();
                    if (count > 0) {
                        list1.add(i);
                        if (count > 2) {
                            list3.add(i);
                            if (count > 5) {
                                list6.add(i);
                                if (count > 15) {
                                    list16.add(i);
                                }
                            }
                        }
                    }
                }
            }
            if(list1.size()>3 || list3.size()>2 || list6.size()>1 || list16.size()>0){
                BlockPos tempPos = pContainerEntity.getBlockPos();
                if(getDist(antPos,tempPos)<getDist(antPos,returnPos)){
                    returnPos = tempPos;
                }
            }
        }
        return returnPos;
    }

    public static void extractLeafyMixture(Ant pAnt, FungalContainerBlockEntity pContainerEntity){
        boolean hasTaken = false;
        if(pAnt.getMainHandItem().isEmpty()){
            ItemStackHandler container = pContainerEntity.itemHandler;
            List<Integer> list16 = new ArrayList<>();
            List<Integer> list6 = new ArrayList<>();
            List<Integer> list3 = new ArrayList<>();
            List<Integer> list1 = new ArrayList<>();
            for(int i = 0; i < container.getSlots(); i ++){
                ItemStack tempStack = container.getStackInSlot(i);
                if(isFungusEdible(tempStack)) {
                    int count = tempStack.getCount();
                    if(count>0){
                        list1.add(i);
                        if(count>2){
                            list3.add(i);
                            if(count>5){
                                list6.add(i);
                                if(count>15){
                                    list16.add(i);
                                }
                            }
                        }
                    }
                }
            }
            if(list1.size()>3 && !hasTaken){
                for(int i = 0; i < 3; i++){
                    int slot = list1.get(i);
                    Item tempItem = container.getStackInSlot(slot).getItem();
                    int tempCount = container.getStackInSlot(slot).getCount();
                    container.setStackInSlot(slot, new ItemStack(tempItem,tempCount-1));
                }
                pAnt.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModBlocks.LEAFY_MIXTURE.get(), 1));
                hasTaken = true;
            }
            if(list3.size()>2 && !hasTaken){
                for(int i = 0; i < 2; i++){
                    int slot = list1.get(i);
                    Item tempItem = container.getStackInSlot(slot).getItem();
                    int tempCount = container.getStackInSlot(slot).getCount();
                    container.setStackInSlot(slot, new ItemStack(tempItem,tempCount-3));
                }
                pAnt.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModBlocks.LEAFY_MIXTURE.get(), 1));
                hasTaken = true;
            }
            if(list6.size()>1 && !hasTaken){
                for(int i = 0; i < 1; i++){
                    int slot = list1.get(i);
                    Item tempItem = container.getStackInSlot(slot).getItem();
                    int tempCount = container.getStackInSlot(slot).getCount();
                    container.setStackInSlot(slot, new ItemStack(tempItem,tempCount-6));
                }
                pAnt.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModBlocks.LEAFY_MIXTURE.get(), 1));
                hasTaken = true;
            }
            if(list16.size()>0 && !hasTaken){
                int slot = list1.get(0);
                Item tempItem = container.getStackInSlot(slot).getItem();
                int tempCount = container.getStackInSlot(slot).getCount();
                container.setStackInSlot(slot, new ItemStack(tempItem,tempCount-16));
                pAnt.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModBlocks.LEAFY_MIXTURE.get(), 1));
                hasTaken = true;
            }
        }
    }

    public static boolean isAdjacentTo(LevelReader pLevel, BlockPos pPos, Block pBlock, boolean includeDiagonally){
        if(!includeDiagonally){
            if(pLevel.getBlockState(pPos.north()).getBlock() == pBlock){
                return true;
            }
            else if(pLevel.getBlockState(pPos.south()).getBlock() == pBlock){
                return true;
            }
            else if(pLevel.getBlockState(pPos.east()).getBlock() == pBlock){
                return true;
            }
            else if(pLevel.getBlockState(pPos.west()).getBlock() == pBlock){
                return true;
            }
            else if(pLevel.getBlockState(pPos.above()).getBlock() == pBlock){
                return true;
            }
            else if(pLevel.getBlockState(pPos.below()).getBlock() == pBlock){
                return true;
            }
            else{return false;}
        }
        else{
            for(int x = -1; x < 1; x ++){
                for(int y = -1; y < 1; y ++){
                    for(int z = -1; z < 1; z ++) {
                        BlockPos tempPos = new BlockPos(x+pPos.getX(),y+pPos.getY(), z+pPos.getZ());
                        BlockState tempState = pLevel.getBlockState(tempPos);
                        if(tempState.getBlock() == pBlock){
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }


    public static boolean isFungusEdible(ItemStack pStack) {
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
                pStack.is(Items.BONE_MEAL) ||
                pStack.isEdible();
    }

    public static void antRemoveItem(Ant ant, FungalContainerBlockEntity pOrigin, boolean validItem){
        int usableSlot = -1;
        if (ant.getMainHandItem() == ItemStack.EMPTY) {
            for (int slot = 0; slot < pOrigin.itemHandler.getSlots(); slot++){
                if (isFungusEdible(pOrigin.itemHandler.getStackInSlot(slot))){
                    usableSlot = slot;
                    slot = pOrigin.itemHandler.getSlots();
                }
            }
            if (usableSlot != -1){
                ItemStack hotStack = pOrigin.itemHandler.getStackInSlot(usableSlot);
                Item hotItem = hotStack.getItem();
                int stackSize = hotStack.getCount();
                if (stackSize<2){
                    ant.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(hotItem,1));
                    pOrigin.itemHandler.setStackInSlot(usableSlot,ItemStack.EMPTY);
                    pOrigin.setChanged();
                }
                else {
                    ant.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(hotItem,1));
                    pOrigin.itemHandler.setStackInSlot(usableSlot, new ItemStack(hotItem,stackSize-1));
                    pOrigin.setChanged();
                }
            }
        }
    }

    public static ArrayList<BlockPos> findAllBlockPos(Block pBlock, BlockPos middlePos, int pHorizontal, int pVertical, LevelReader pLevel){
        ArrayList<BlockPos> list = new ArrayList<BlockPos>();
        for (int x = -(pHorizontal); x <= (pHorizontal); ++x) {
            for (int y = -(pVertical); y <= (pVertical); ++y) {
                for (int z = -(pHorizontal); z <= (pHorizontal); ++z) {
                    double antX = middlePos.getX();
                    double antY = middlePos.getY();
                    double antZ = middlePos.getZ();
                    BlockPos testBlock = new BlockPos(x+antX, y+antY, z+antZ);
                    if (pBlock == pLevel.getBlockState(testBlock).getBlock()) {
                        list.add(testBlock);
                    }
                }
            }
        }
        return list;
    }

    public static BlockPos findNearestBlockPos(Ant pAnt, Block pBlock, int pHorizontal, int pVertical, boolean shouldReach){
        BlockPos pPos = pAnt.blockPosition();
        List<BlockPos> list = findAllBlockPos(pBlock, pPos, pHorizontal, pVertical, pAnt.getLevel());
        BlockPos returnPos = BlockPos.ZERO;
        for(BlockPos pos : list) {
            if (getDist(pPos, pos)<getDist(returnPos,pPos)){
                if(shouldReach){
                    if (canReach(pAnt,pos)){
                        returnPos = pos;
                    }
                }
                else returnPos = pos;
            }
        }
        return returnPos;
    }

    public static List<BlockPos> findNearestBlockPoses(LevelReader level, BlockPos endPos, ArrayList<BlockPos> startingList, int endListSize){
        ArrayList<BlockPos> returnList = new ArrayList<>();
        for(int i = endListSize+1; i>0; i--) {
            BlockPos returnPos = startingList.get(0);
            for (BlockPos pos : startingList) {
                if (getDist(endPos, pos) < getDist(returnPos, pos)) {
                    returnPos = pos;
                }
            }
            returnList.add(returnPos);
            startingList.removeAll(returnList);
        }
        return returnList;
    }

    public static BlockPos findNearestBlockPos(Ant pAnt, ArrayList<BlockPos> pList){
        if (pList.isEmpty()) {return BlockPos.ZERO;}
        BlockPos returnPos = pList.get(0);

        BlockPos antPos = pAnt.blockPosition();
        for(BlockPos tempPos : pList) {
            if (getDist(antPos, tempPos)<getDist(returnPos,antPos)){
                returnPos = tempPos;
            }
        }
        return returnPos;
    }

    public static BlockPos findNearestBlockPos(BlockPos pos, ArrayList<BlockPos> pList){
        if(pList.isEmpty()){return BlockPos.ZERO;}
        BlockPos returnPos = pList.get(0);
        for(BlockPos tempPos : pList) {
            if (getDist(pos, tempPos)<getDist(returnPos,pos)){
                returnPos = tempPos;
            }
        }
        return returnPos;
    }

    public static BlockPos findNearestBlockPos(Ant pAnt, List<BlockPos> pList, boolean shouldReach){
        BlockPos pPos = pAnt.blockPosition();
        BlockPos returnPos = BlockPos.ZERO;
        for(BlockPos pos : pList) {
            if (getDist(pPos, pos)<getDist(returnPos,pPos)){
                if(shouldReach){
                    if (canReach(pAnt,pos)){
                        returnPos = pos;
                    }
                }
                else returnPos = pos;
            }
        }
        return returnPos;
    }



    public static boolean canReach(Ant pAnt, BlockPos targetPos) {
        if(getDist(pAnt.blockPosition(), targetPos) < 20) {
            Path tempPath = pAnt.getNavigation().createPath(targetPos, 1);
            if (tempPath != null) {
                net.minecraft.world.level.pathfinder.Node finalPathPoint = tempPath.getEndNode();
                if (finalPathPoint != null) {
                    BlockPos pathEndPos = new BlockPos(finalPathPoint.x, finalPathPoint.y, finalPathPoint.z);
                    return getDist(targetPos, pathEndPos) < 1.2f;
                }
                else return false;
            }
        }
        else{
            return new PosPair(pAnt.blockPosition(), targetPos, pAnt.level).canConnectWithFloor(25);
        }
        return false;
    }

    public static boolean canAddItem(ItemStack pStack, BlockEntity pContainer){
        boolean returnval = false;
        if (pContainer instanceof FungalContainerBlockEntity) {
            ItemStackHandler inventory = ((FungalContainerBlockEntity) pContainer).itemHandler;
            int slots = 9;
            int checkedslots;

            for (checkedslots = 0; checkedslots <= slots - 1; checkedslots++) {

                if (inventory.getStackInSlot(checkedslots).isEmpty()) {
                    checkedslots = slots - 1;
                    returnval = true;
                } else if (inventory.getStackInSlot(checkedslots).getCount() < 64 && inventory.getStackInSlot(checkedslots).getItem() == pStack.getItem()) {
                    checkedslots = slots - 1;
                    returnval = true;
                }
            }
        }
        return returnval;
    }
    public static boolean canCompressContainers(List<BlockPos> pList, LevelReader pLevel, List<BlockPos> blockPoses, List<Integer> whatSlots){
        List<FungalContainerBlockEntity> containerList = new ArrayList<>();
        blockPoses.clear();
        whatSlots.clear();


        for(BlockPos pPos:pList){
            if(pLevel.getBlockEntity(pPos) instanceof FungalContainerBlockEntity){
                containerList.add((FungalContainerBlockEntity) pLevel.getBlockEntity(pPos));
            }
        }

        for(FungalContainerBlockEntity pEntity:containerList){
            ItemStackHandler inventory = pEntity.itemHandler;
            int slots = 9;
            for(FungalContainerBlockEntity pEntity1:containerList){
                ItemStackHandler inventory1 = pEntity1.itemHandler;
                for(int i =0; i<slots;i++){
                    ItemStack stack = inventory.getStackInSlot(i);
                    for(int l =0; l<slots; l++){
                        ItemStack stack1 = inventory1.getStackInSlot(l);
                        Item item = stack.getItem();
                        Item item1 = stack1.getItem();
                        int stackLimit =item.getMaxStackSize();
                        if((pEntity1 != pEntity)||(l != i)){
                            if(item == item1 && stack.getCount()+stack1.getCount()<stackLimit){
                                blockPoses.add(pEntity.getBlockPos());
                                blockPoses.add(pEntity1.getBlockPos());
                                whatSlots.add(i+(l*10));
                            }
                        }
                    }
                }
            }
            containerList.remove(pEntity);
        }
        return !blockPoses.isEmpty();
    }

    public static float checkContainersFullness(List<BlockPos> posList, LevelReader pLevel, boolean checkEdible){
        List<FungalContainerBlockEntity> entityList = new ArrayList<>();
        int fullSlots = 0;
        boolean edible = checkEdible;
        int totalSlots = 0;
        for(BlockPos pPos : posList){
            if(pLevel.getBlockEntity(pPos) instanceof FungalContainerBlockEntity){
                entityList.add((FungalContainerBlockEntity) pLevel.getBlockEntity(pPos));
            }
        }
        for(FungalContainerBlockEntity pEntity : entityList){
            ItemStackHandler inventory = pEntity.itemHandler;
            for(int i = 0; i < 9; i++){
                ItemStack tempStack = inventory.getStackInSlot(i);
                if(edible){
                    if(!isFungusEdible(tempStack)){
                        edible = false;
                    }
                }
                else{
                    edible = true;
                }
                if(!tempStack.isEmpty() && edible){
                    fullSlots++;
                }
                totalSlots++;
                edible = checkEdible;
            }
        }

        if(totalSlots!=0 && fullSlots!=0){return (float) fullSlots/totalSlots;}
        else{return 0f;}
    }
    
 

    public static void antAddItem(Ant holder, BlockEntity pDestination, ItemStack holderStack){
        if (pDestination instanceof FungalContainerBlockEntity) {
            ItemStackHandler inventory = ((FungalContainerBlockEntity) pDestination).itemHandler;
            int slots = 9;
            int checkedslots;
            int chosenslot = -1;
            byte mergetype = 0;
            for (checkedslots = 0; checkedslots <= slots - 1; checkedslots++) {

                if (inventory.getStackInSlot(checkedslots).isEmpty()) {
                    chosenslot = checkedslots;
                    checkedslots = slots - 1;
                    mergetype = 1;
                } else if (inventory.getStackInSlot(checkedslots).getCount() < 64 && inventory.getStackInSlot(checkedslots).getItem() == holderStack.getItem()) {
                    chosenslot = checkedslots;
                    checkedslots = slots - 1;
                    mergetype = 2;
                }
            }

            if (chosenslot > -1 && !holderStack.isEmpty() && mergetype == 1) {
                inventory.setStackInSlot(chosenslot, holderStack);
                pDestination.setChanged();
                holder.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                holderStack = ItemStack.EMPTY;
            }

            if (chosenslot > -1 && !holderStack.isEmpty() && mergetype == 2) {
                ItemStack newStack = new ItemStack(inventory.getStackInSlot(chosenslot).getItem(),
                        inventory.getStackInSlot(chosenslot).getCount() + 1);
                inventory.setStackInSlot(chosenslot, newStack);
                pDestination.setChanged();
                holder.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                holderStack = ItemStack.EMPTY;
            }
        }
    }

    public static double getDist(BlockPos pPos1, BlockPos pPos2) {
        double X = (pPos1.getX()-pPos2.getX())*(pPos1.getX()-pPos2.getX());
        double Y = (pPos1.getY()-pPos2.getY())*(pPos1.getY()-pPos2.getY());
        double Z = (pPos1.getZ()-pPos2.getZ())*(pPos1.getZ()-pPos2.getZ());

        return Math.sqrt(X+Y+Z);
    }

    public static double getHorizontalDist(BlockPos pPos1, BlockPos pPos2) {
        double X = (pPos1.getX()-pPos2.getX())*(pPos1.getX()-pPos2.getX());
        double Z = (pPos1.getZ()-pPos2.getZ())*(pPos1.getZ()-pPos2.getZ());

        return Math.sqrt(X+Z);
    }

    public static Random randFromPos(BlockPos pos){
        return new Random(Math.abs(pos.getX()*pos.getY()*pos.getZ()));
    }

    public static boolean isPosInChunk(BlockPos pos, ChunkPos chunk){
        int cMaxX = chunk.getMaxBlockX();
        int cMaxZ = chunk.getMaxBlockZ();
        int cMinX = chunk.getMinBlockX();
        int cMinZ = chunk.getMinBlockZ();

        return pos.getX() <= cMaxX && pos.getX() >= cMinX && pos.getZ() <= cMaxZ && pos.getZ() >= cMinZ;
    }

    public static boolean isCopper(BlockState pState){
        Block pBlock = pState.getBlock();
        return pBlock.getSoundType(pState) == SoundType.COPPER || pBlock == Blocks.COPPER_ORE || pBlock == Blocks.DEEPSLATE_COPPER_ORE;
    }

    public static BlockPos findNearestUsableContainer(Ant pAnt, List<BlockPos> pList){
        BlockPos returnPos = BlockPos.ZERO;
        for(BlockPos tempPos : pList){
            FungalContainerBlockEntity tempEntity = (FungalContainerBlockEntity) pAnt.getLevel().getBlockEntity(tempPos);
            boolean canPlace = canAddItem(pAnt.getMainHandItem(),tempEntity);
            if(getDist(pAnt.blockPosition(),tempPos)<getDist(pAnt.blockPosition(),returnPos) && canPlace && canReach(pAnt,tempPos)){
                returnPos = tempPos;
            }
        }
        return returnPos;
    }
}
