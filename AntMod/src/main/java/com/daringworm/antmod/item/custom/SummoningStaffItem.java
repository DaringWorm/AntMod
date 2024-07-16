package com.daringworm.antmod.item.custom;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.block.custom.MoldyLeaves;
import com.daringworm.antmod.colony.misc.PosSpherePair;
import com.daringworm.antmod.entity.brains.BrainTrees;
import com.daringworm.antmod.entity.brains.LeafCutterWorkerBrain;
import com.daringworm.antmod.entity.custom.WorkerAnt;
import com.daringworm.antmod.goals.AntUtils;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
        if(pLevel instanceof ServerLevel) {
            WorkerAnt ant = pLevel.getEntitiesOfClass(WorkerAnt.class, pEntity.getBoundingBox().inflate(2d,2d,2d)).get(0);
            if(ant != null) {
                AntUtils.broadcastString(pLevel, ant.saveWithoutId(new CompoundTag()).getAsString());
                AntUtils.broadcastString(pLevel, BrainTrees.getNextCell(ant).KEY + ' ' + ant.getBraincellStage());
            }

            if(pEntity.isCrouching()){
                ArrayList<WorkerAnt> ants = new ArrayList<>(pLevel.getEntitiesOfClass(WorkerAnt.class,pEntity.getBoundingBox().inflate(1000,1000,1000)));

                for(WorkerAnt tempAnt : ants){
                    tempAnt.setShouldRunBrain(!tempAnt.getShouldRunBrain());
                }
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
