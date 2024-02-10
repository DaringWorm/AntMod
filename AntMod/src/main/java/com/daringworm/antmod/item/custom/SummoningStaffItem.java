package com.daringworm.antmod.item.custom;

import com.daringworm.antmod.worldgen.feature.custom.AntGeodeFeature;
import com.daringworm.antmod.worldgen.feature.registries.AntFeaturesReg;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
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
        /*if(!pLevel.isClientSide()) {
            WorkerAnt pAnt = pLevel.getEntitiesOfClass(WorkerAnt.class,pEntity.getBoundingBox().inflate(60)).get(0);
            assert pAnt != null;
            AntColony colony = ((ServerLevelUtil)pLevel).getColonyWithID(pAnt.getColonyID());
            if(colony != null){
                ColonyBranch branch = colony.tunnels;
                if(branch != null){
                    String nearPosID = branch.getNearestBranchID(pEntity.blockPosition());
                    AntUtils.broadcastString(pLevel, nearPosID);

                    WorkerAnt newAnt = new WorkerAnt(ModEntityTypes.WORKERANT.get(), pLevel);
                    newAnt.setHomePos(branch.getSubBranch(nearPosID).getPos());
                    newAnt.setColonyID(pAnt.getColonyID());
                    newAnt.setRoomID(nearPosID);
                    newAnt.setWorkingStage(WorkingStages.SCOUTING);
                    newAnt.moveTo(newAnt.getHomePos(),0,0);
                    pLevel.addFreshEntity(newAnt);
                }
            }
        }*/
        assert pLevel instanceof ServerLevel;
        ServerLevel serverlevel = (ServerLevel) pLevel;
        AntGeodeFeature colonyFeature = AntFeaturesReg.GEODE_BASE_REGISTER.get();
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
