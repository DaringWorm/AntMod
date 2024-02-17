package com.daringworm.antmod.item.custom;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.colony.AntColony;
import com.daringworm.antmod.colony.misc.BlockPosStringifier;
import com.daringworm.antmod.colony.misc.ColonyBranch;
import com.daringworm.antmod.colony.misc.PosSpherePair;
import com.daringworm.antmod.goals.AntUtils;
import com.daringworm.antmod.mixin.tomixin.ServerLevelUtil;
import com.daringworm.antmod.worldgen.feature.custom.AntGeodeFeature;
import com.daringworm.antmod.worldgen.feature.registries.AntFeaturesReg;
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
            AntColony colony = ((ServerLevelUtil)pLevel).getClosestColony(pEntity.blockPosition());

            if(colony != null){
                ColonyBranch tunnels = colony.tunnels;
                if(tunnels != null){
                    String branchID = tunnels.getNearestBranchID(pEntity.blockPosition());
                    BlockPos branchPos = AntUtils.findNearestBlockPos(pEntity.blockPosition(), tunnels.listBranchPoses());
                    AntUtils.broadcastString(level, "Nearest colony's ID is " +
                            colony.colonyID +
                            ". The nearest room within that colony is " +
                            branchID +
                            ", which is located at " +
                            BlockPosStringifier.jsonFromPos(branchPos) +
                            '.');
                }
                else{
                    AntUtils.broadcastString(level, "Nearest colony has no branches.");
                }
            }
            else{
                AntUtils.broadcastString(level, "No colony was found.");
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
