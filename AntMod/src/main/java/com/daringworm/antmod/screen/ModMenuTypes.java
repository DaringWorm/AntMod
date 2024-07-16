package com.daringworm.antmod.screen;

import com.daringworm.antmod.AntMod;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.CONTAINERS, AntMod.MOD_ID);

    public static final RegistryObject<MenuType<LeafyContainerMenu>> LEAFY_CONTAINER_MENU =
            registerMenuType(LeafyContainerMenu::new, "leafy_container_menu");

    /*public static final RegistryObject<MenuType<AntTradingMenu>> ANT_TRADING_MENU =
            registerMenuType(AntTradingMenu::new, "ant_trading_menu");
*/
    private static <T extends AbstractContainerMenu>RegistryObject<MenuType<T>> registerMenuType(IContainerFactory<T> factory, String name) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
