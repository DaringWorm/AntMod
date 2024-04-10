package com.daringworm.antmod.item.custom;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.colony.misc.PosSpherePair;
import com.daringworm.antmod.goals.AntUtils;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SummoningStaffItem extends Item {
    private final Multimap<Attribute, AttributeModifier> itemModifiers;

    public SummoningStaffItem(Properties pProperties) {
        super(pProperties);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", 4.0D, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -1.5F, AttributeModifier.Operation.ADDITION));
        this.itemModifiers = builder.build();
    }



    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BOW;
    }

    public int getUseDuration(ItemStack pStack) {
        return 36000;
    }

    @Override
    public void releaseUsing(@NotNull ItemStack pStack, Level pLevel, @NotNull LivingEntity pEntity, int pTimeLeft) {
        if(pLevel instanceof ServerLevel level) {
            /*AntColony colony = ((ServerLevelUtil)pLevel).getClosestColony(pEntity.blockPosition());

            if(colony != null){
                ColonyBranch tunnels = colony.tunnels;
                if(tunnels != null){
                    String branchID = tunnels.getNearestBranchID(pEntity.blockPosition());
                    BlockPos branchPos = AntUtils.findNearestBlockPos(pEntity.blockPosition(), tunnels.listBranchPoses());
                    ArrayList<BlockPos> posList = tunnels.getPosesToBranch(tunnels.getNearestBranchID(pEntity.blockPosition()));
                    AntUtils.broadcastString(level, "Nearest colony's ID is " +
                            colony.colonyID +
                            ". The nearest room within that colony is " +
                            branchID +
                            ", which is located at " +
                            BlockPosStringifier.jsonFromPos(branchPos) +
                            '.');


                    if(posList.isEmpty()){AntUtils.broadcastString(level,"list is empty");}
                    else{AntUtils.broadcastString(level," which is " + posList.size() + " long.");}
                    for(BlockPos tempPos : posList){
                        AntUtils.broadcastString(level,"" + BlockPosStringifier.jsonFromPos(tempPos));
                    }

                    AntUtils.broadcastString(level, "The colony has " +
                            colony.tunnels.listBranchPoses().size() +
                            " branch positions, and " +
                            colony.tunnels.listBranchIDs().size() +
                            " branch IDs.");
                }
                else{
                    AntUtils.broadcastString(level, "Nearest colony has no branches.");
                }
            }
            else{
                AntUtils.broadcastString(level, "No colony was found.");
            }*/
            /*
            WorkerAnt ant = pLevel.getEntitiesOfClass(WorkerAnt.class, pEntity.getBoundingBox().inflate(2d,2d,2d)).get(0);
            if(ant != null){
                AntUtils.broadcastString(pLevel,"" + BlockPosStringifier.jsonFromPos(ant.memory.interestPos));
                AntUtils.broadcastString(ant.getLevel(),ant.memory.cellToRun.KEY);
            }
            AntScentCloud cloud = pLevel.getEntitiesOfClass(AntScentCloud.class, pEntity.getBoundingBox().inflate(8d,8d,8d)).get(0);
            if(cloud != null){
                AntUtils.broadcastString(pLevel, "This cloud has " + cloud.getInterestPosesSize() + " positions.");
            }*/
            int size = 16;
            double sphereOffset = 3.4d;
            double sphereRadius = 2d;
            BlockPos ep = pEntity.blockPosition();
            ArrayList<BlockPos> airPoses = new ArrayList<>();
            for(int x = size; x > -size; --x){
                for(int y = size; y > -size; --y){
                    for(int z = size; z > -size; --z){
                        BlockPos pos = new BlockPos(x,y,z).offset(ep.getX(),ep.getY(),ep.getZ());
                        if(AntUtils.getDist(pos, ep) < size) {
                            level.setBlock(pos, (level.getRandom().nextInt(5) == 1)? ModBlocks.FUNGUS_GARDEN.get().defaultBlockState() : ModBlocks.FUNGUS_BLOCK.get().defaultBlockState(), 2);
                        }

                        if(level.getRandom().nextBoolean() &&
                                AntUtils.getDist(AntUtils.findNearestBlockPos(pos,airPoses), pos) > sphereOffset){
                            airPoses.add(pos);
                        }
                    }
                }
            }

            for(BlockPos pos : airPoses){
                new PosSpherePair(pos,sphereRadius).setSphere(level, Blocks.AIR, Blocks.AIR, 0);
            }

        }

    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        pPlayer.startUsingItem(pHand);
        return InteractionResultHolder.consume(itemstack);

    }

    public int getEnchantmentValue() {
        return 1;
    }

}
