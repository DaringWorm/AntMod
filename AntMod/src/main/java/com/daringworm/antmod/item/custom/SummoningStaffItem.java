package com.daringworm.antmod.item.custom;

import com.daringworm.antmod.goals.AntUtils;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Random;

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

    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving, int pTimeLeft) {
        if (pEntityLiving instanceof Player) {
            /*Player player = (Player)pEntityLiving;
            pLevel.addParticle(ParticleTypes.EXPLOSION_EMITTER,true,pEntityLiving.getX(),pEntityLiving.getY(),pEntityLiving.getZ(),0,0,0);
            for(int i = 10; i>0; i--) {
                Wolf wolf = new Wolf(EntityType.WOLF, pLevel);
                wolf.setTame(true);
                wolf.tame(player);
                wolf.randomTeleport(pEntityLiving.getX(),pEntityLiving.getY(), pEntityLiving.getZ(), true);
                pLevel.addFreshEntity(wolf);
            }
            player.getCooldowns().addCooldown(this, 200);*/
        }
        int maxRooms = 15;
        int maxSpread = 100;
        BlockPos center = pEntityLiving.blockPosition();
        double distBetween = Math.sqrt((double)(maxSpread*maxSpread)/(double)maxRooms);
        Random rand = pLevel.getRandom();
        ArrayList<BlockPos> posList = new ArrayList<>();

        for(int i = maxRooms; i > 0; i--){
            int xOffset = rand.nextInt(maxSpread/2) * ((rand.nextBoolean()) ? -1:1);
            int zOffset = rand.nextInt(maxSpread/2) * ((rand.nextBoolean()) ? -1:1);
            BlockPos pos = center.relative(Direction.Axis.X, xOffset).relative(Direction.Axis.Z, zOffset);
            boolean shouldAdd = true;

            for(BlockPos tempPos : posList){
                if(tempPos != null && AntUtils.getDist(pos, tempPos) < distBetween){
                    shouldAdd = false;
                }
            }
            if(shouldAdd){posList.add(pos);}
        }
        for(BlockPos tempPos : posList){
            pLevel.setBlock(tempPos, Blocks.GLOWSTONE.defaultBlockState(),2);
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
