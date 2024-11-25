package com.daringworm.antmod.screen;

import com.daringworm.antmod.entity.Ant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Arrays;

public class AntTradingMenu extends AbstractContainerMenu {

    Ant ant;

   /* public AntTradingMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level.getEntitiesOfClass(Ant.class, inv.player.getBoundingBox().inflate(7d)).stream().filter(a -> Arrays.stream().anyMatch(p -> p.getId() == inv.player.getId())).findFirst().orElse(null));
    }*/

    public AntTradingMenu(int pContainerId, Inventory inv, Ant ant) {
        super(ModMenuTypes.LEAFY_CONTAINER_MENU.get(), pContainerId);
        checkContainerSize(inv, 9);
        this.ant = ant;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return this.ant != null && this.ant.distanceToSqr(pPlayer) <= 5d;
    }


}
