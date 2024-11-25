package com.daringworm.antmod.item.custom;

import com.daringworm.antmod.colony.AntColony;
import com.daringworm.antmod.colony.misc.BlockPosStringifier;
import com.daringworm.antmod.entity.brains.BrainTrees;
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
import net.minecraftforge.common.Tags;
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

            /*int areaSize = 12;
            int tileSizeXZ = 5;
            int halfTileXZ = tileSizeXZ/2;
            int tileSizeY = 3;

            BlockPos basePos = pEntity.blockPosition().offset(areaSize/2 +1, 0, 0);

            int baseX = basePos.getX();
            int baseY = basePos.getY();
            int baseZ = basePos.getZ();

            int xOff = (baseX % tileSizeXZ);
            int yOff = (baseY % tileSizeY);
            int zOff = (baseZ % tileSizeXZ);

            xOff = (xOff < 0)? xOff + halfTileXZ : xOff - halfTileXZ;
            zOff = (zOff < 0)? zOff + halfTileXZ : zOff - halfTileXZ;

            BlockPos modBasePos = new BlockPos(xOff,yOff,zOff);

            for(int x = areaSize/2; x >= -areaSize/2; x--){
                for(int y = areaSize/2; y >= -areaSize/2; y--){
                    for(int z = areaSize/2; z >= -areaSize/2; z--){
                        BlockPos tempPos = basePos.offset(x,y,z);

                        int modX = (x%tileSizeXZ)+xOff;
                        int modZ = (z%tileSizeXZ)+zOff;
                        int modY = ((y+1+areaSize)%tileSizeY) + yOff;

                        //creates the main "t" shaped hallways
                        if(((modX==xOff) || (modZ==zOff)) &&  (modY==yOff || modY==yOff+1)){
                            pLevel.setBlock(tempPos, Blocks.GLASS.defaultBlockState(), 2);
                        }
                        //creates the middle stairs
                        else if((modX == xOff+halfTileXZ || modX == xOff-halfTileXZ)
                                && (modZ == zOff+halfTileXZ || modZ == zOff-halfTileXZ)
                                && modY != yOff+1){
                            pLevel.setBlock(tempPos, Blocks.YELLOW_STAINED_GLASS.defaultBlockState(), 2);
                        }
                        //creates the bottom stairs
                        else if((modX == xOff+halfTileXZ || modX == xOff-halfTileXZ)
                                && (modZ == zOff+halfTileXZ-1 || modZ == zOff-halfTileXZ+1)
                                && modY != yOff){
                            pLevel.setBlock(tempPos, Blocks.LIME_STAINED_GLASS.defaultBlockState(), 2);
                        }
                        //creates the top stairs
                        else if((modX == xOff+halfTileXZ-1 || modX == xOff-halfTileXZ+1)
                                && (modZ == zOff+halfTileXZ || modZ == zOff-halfTileXZ)
                                && modY != yOff+2){
                            pLevel.setBlock(tempPos, Blocks.ORANGE_STAINED_GLASS.defaultBlockState(), 2);
                        }
                    }
                }
            }*/

            AntColony colony = new AntColony(pLevel, pLevel.getRandom().nextInt(), pEntity.blockPosition());
            colony.hasSpawnedAnts = true;
            colony.generateNewColonyBlueprint();
            colony.generateTunnels();
            colony.updateToServer();

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
