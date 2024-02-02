package com.daringworm.antmod.block.entity.custom;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.block.entity.ModBlockEntities;
import com.daringworm.antmod.entity.Ant;
import com.daringworm.antmod.goals.AntUtils;
import com.daringworm.antmod.item.ModItems;
import com.daringworm.antmod.screen.FungalContainerMenu;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
    public Component getDisplayName() {
        return new TextComponent("Leafy Container");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
        return new FungalContainerMenu(pContainerId,pInventory,this);}


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

        Containers.dropContents(this.level, this.worldPosition, inventory);

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
                                pAnt.setHomePos(this.worldPosition);
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
        else{tempMethodForTest(pAnt.getLevel());}
    }


    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, FungalContainerBlockEntity pBlockEntity) {
        if(hasRecipe(pBlockEntity) && hasNotReachedStackLimit(pBlockEntity)) {
            //craftItem(pBlockEntity);
        }
        if(!pLevel.isClientSide()){
            for(Ant pAnt : pLevel.getEntitiesOfClass(Ant.class, new AABB(pPos.getX()-10, pPos.getY()-5, pPos.getZ()-10,pPos.getX()+10, pPos.getY()+5, pPos.getZ()+10))){
                if(pAnt.memory.containerPos == BlockPos.ZERO || pAnt.memory.containerPos == null ||
                        pLevel.getBlockState(pAnt.memory.containerPos).getBlock() != ModBlocks.LEAFY_CONTAINER_BLOCK.get() ||
                AntUtils.getDist(pAnt.memory.containerPos, pAnt.getHomePos()) > AntUtils.getDist(pPos, pAnt.getHomePos())){
                    pAnt.memory.containerPos = pPos;
                    pAnt.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200));
                }
            }
        }
    }

    private void tempMethodForTest(Level pLevel){
        Block blockToPlace = Blocks.TINTED_GLASS;
        BlockPos pPos = BlockPos.findClosestMatch(this.worldPosition,32,16, p -> pLevel.getBlockState(p).isSolidRender(pLevel,p)
                && pLevel.getBlockState(p).getBlock() != blockToPlace). orElse(null);
        assert pPos != null;
        pLevel.setBlock(pPos, blockToPlace.defaultBlockState(),2);
        for(int i = 0; i < itemHandler.getSlots(); i++) {
            this.itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
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
