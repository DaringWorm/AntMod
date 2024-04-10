package com.daringworm.antmod.block.entity.custom;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.block.custom.FungalContainer;
import com.daringworm.antmod.block.custom.FungusCarpet;
import com.daringworm.antmod.block.entity.ModBlockEntities;
import com.daringworm.antmod.entity.Ant;
import com.daringworm.antmod.entity.ModEntityTypes;
import com.daringworm.antmod.entity.brains.parts.WorkingStages;
import com.daringworm.antmod.entity.custom.AntScentCloud;
import com.daringworm.antmod.goals.AntUtils;
import com.daringworm.antmod.item.ModItems;
import com.daringworm.antmod.screen.LeafyContainerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Random;

public class FungalContainerBlockEntity extends BlockEntity implements MenuProvider {
    public final int containerSize = 9;

    public final ItemStackHandler itemHandler = new ItemStackHandler(containerSize){
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setChanged();
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();


    public FungalContainerBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModBlockEntities.FUNGAL_CULTIVAR_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return new TextComponent("Leafy Container");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
        return new LeafyContainerMenu(pContainerId,pInventory,this);}


    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @javax.annotation.Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps()  {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        if(this.level != null) {
            Containers.dropContents(this.level, this.worldPosition, inventory);
        }

    }

    public boolean canAcceptHandItem(LivingEntity pEntity){
        ItemStack inStack = pEntity.getItemInHand(InteractionHand.MAIN_HAND);
        int itemsLeft = inStack.getCount();
        if(inStack.isEmpty()){return true;}
        for(int i = 0; i < itemHandler.getSlots(); i++){
            ItemStack tempStack = itemHandler.getStackInSlot(i);
            int tempCount = tempStack.getCount();
            if(tempStack.isEmpty()){
                return true;
            }
            else if(ItemStack.tagMatches(tempStack,inStack) && ItemStack.isSame(tempStack,inStack)){
                int tempSpace = tempStack.getMaxStackSize()-tempCount;
                if(tempSpace > 0){
                    itemsLeft = itemsLeft-tempSpace;
                }
            }

            if(itemsLeft <= 0){
                return true;
            }
        }
        return false;
    }

    private boolean shouldTriggerFarming(){
        int numberOfEdibleSlots = 0;
        for(int i = 0; i < itemHandler.getSlots(); i++){
            ItemStack tempStack = itemHandler.getStackInSlot(i);
            if(!tempStack.isEmpty() && FungusCarpet.isFood(tempStack)){
                ++numberOfEdibleSlots;
            }
        }
        if(numberOfEdibleSlots >= itemHandler.getSlots()/2){
            return true;
        }
        return false;
    }


    public void takeInHandItem(Ant pAnt){
        if(this.canAcceptHandItem(pAnt)){
            ItemStack inStack = pAnt.getItemInHand(InteractionHand.MAIN_HAND);
            int itemsLeft = inStack.getCount();
            if(!inStack.isEmpty()) {
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    if(itemsLeft > 0) {
                        ItemStack tempStack = itemHandler.getStackInSlot(i);
                        int tempCount = tempStack.getCount();
                        if (tempStack.isEmpty()) {
                            itemHandler.setStackInSlot(i, inStack);
                            pAnt.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                            break;
                        }
                        if (ItemStack.tagMatches(tempStack, inStack) && ItemStack.isSame(tempStack, inStack)) {
                            int tempSpace = tempStack.getMaxStackSize() - tempCount;
                            if(tempSpace >= itemsLeft){
                                tempStack.setCount(tempStack.getCount()+itemsLeft);
                                itemHandler.setStackInSlot(i,tempStack);
                                itemsLeft = 0;
                                pAnt.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                                pAnt.setHomeContainerPos(this.worldPosition);
                                pAnt.memory.containerPos = this.worldPosition;
                            }
                            else{
                                tempStack.setCount(tempStack.getMaxStackSize());
                                itemHandler.setStackInSlot(i,tempStack);
                                itemsLeft = itemsLeft-tempSpace;
                            }
                        }
                    }
                }
            }
            this.setChanged();
        }
        else if(this.getLevel() != null){
            BlockState fullState = ModBlocks.LEAFY_CONTAINER_BLOCK.get().defaultBlockState().setValue(FungalContainer.FULL, true);
            this.getLevel().setBlock(this.getBlockPos(),fullState,2);
        }
        if(this.shouldTriggerFarming()){
            spawnScentCloud(pAnt.getLevel());
            pAnt.setWorkingStage(WorkingStages.FARMING);
        }
    }


    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, FungalContainerBlockEntity pBlockEntity) {
        /*if(hasRecipe(pBlockEntity) && hasNotReachedStackLimit(pBlockEntity)) {
            //craftItem(pBlockEntity);
        }*/
        if(!pLevel.isClientSide() && !pLevel.getBlockState(pPos).getValue(FungalContainer.FULL)){
            for(Ant pAnt : pLevel.getEntitiesOfClass(Ant.class, new AABB(pPos.getX()-5, pPos.getY()-3, pPos.getZ()-5,pPos.getX()+5, pPos.getY()+3, pPos.getZ()+5))){
                BlockState antHomeState = pLevel.getBlockState(pAnt.getHomeContainerPos());
                boolean distanceFlag = AntUtils.getDist(pAnt.getHomeContainerPos(), pPos) > AntUtils.getDist(pAnt.getColony().tunnels.getSubBranch(pAnt.getRoomID()).getPos(), pPos);
                if(antHomeState.getBlock() != ModBlocks.LEAFY_CONTAINER_BLOCK.get() || !antHomeState.getValue(FungalContainer.FULL) || distanceFlag){
                    pAnt.setHomeContainerPos(pPos);
                    pAnt.memory.containerPos = pPos;
                    pAnt.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200));
                }
            }
        }
    }

    private void spawnScentCloud(Level pLevel){
        for(int i = 0; i < itemHandler.getSlots(); i++) {
            this.itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
        AntScentCloud cloud = new AntScentCloud(ModEntityTypes.ANT_EFFECT_CLOUD.get(), pLevel);
        cloud.WORKING_STAGE = WorkingStages.FARMING;
        //cloud.COLONY_ID = ((ServerLevelUtil)pLevel).getClosestColony(this.worldPosition).colonyID;
        cloud.setPos(this.worldPosition.getX(),this.worldPosition.getY(), this.worldPosition.getZ());
        pLevel.addFreshEntity(cloud);
    }


    private static void craftItem(FungalContainerBlockEntity entity) {
        entity.itemHandler.extractItem(0, 1, false);
        entity.itemHandler.extractItem(1, 1, false);
        entity.itemHandler.getStackInSlot(2).hurt(1, new Random(), null);

        entity.itemHandler.setStackInSlot(3, new ItemStack(Items.GRASS,
                entity.itemHandler.getStackInSlot(3).getCount() + 1));
    }

    private static boolean hasRecipe(FungalContainerBlockEntity entity) {
        boolean hasItemInWaterSlot = PotionUtils.getPotion(entity.itemHandler.getStackInSlot(0)) == Potions.WATER;
        boolean hasItemInFirstSlot = entity.itemHandler.getStackInSlot(1).getItem() == ModItems.FUNGUS.get();
        boolean hasItemInSecondSlot = entity.itemHandler.getStackInSlot(2).getItem() == ModItems.ANT_FOOD.get();

        return hasItemInWaterSlot && hasItemInFirstSlot && hasItemInSecondSlot;
    }

    private static boolean hasNotReachedStackLimit(FungalContainerBlockEntity entity) {
        return entity.itemHandler.getStackInSlot(3).getCount() < entity.itemHandler.getStackInSlot(3).getMaxStackSize();
    }
}
