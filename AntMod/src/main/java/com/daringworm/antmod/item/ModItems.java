package com.daringworm.antmod.item;

import com.daringworm.antmod.AntMod;
import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.entity.ModEntityTypes;
import com.daringworm.antmod.item.custom.SummoningStaffItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

import static com.daringworm.antmod.block.ModBlocks.BLOCKS;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, AntMod.MOD_ID);

    public static final RegistryObject<Item> FUNGUS = ITEMS.register("fungus",
            ()-> new Item(new Item.Properties().tab(ModCreativeModeTab.ANT_MOD_CTAB).stacksTo(64)));

    public static final RegistryObject<Item> SUMMONING_STAFF = ITEMS.register("summoning_staff",
            ()-> new SummoningStaffItem(new Item.Properties().tab(ModCreativeModeTab.ANT_MOD_CTAB).stacksTo(1)));

    public static final RegistryObject<Item> ANT_FOOD = ITEMS.register("antfood",
            ()-> new Item(new Item.Properties().tab(ModCreativeModeTab.ANT_MOD_CTAB).stacksTo(64)));

    public static final RegistryObject<Item> WORKER_ANT_SPAWN_EGG = ITEMS.register("worker_ant_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.WORKERANT, 0x12345a, 0x123456b,
                    new Item.Properties().tab(ModCreativeModeTab.ANT_MOD_CTAB)));

    public static final RegistryObject<Item> QUEEN_ANT_SPAWN_EGG = ITEMS.register("queen_ant_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.QUEENANT, 0x54321a, 0x654321b,
                    new Item.Properties().tab(ModCreativeModeTab.ANT_MOD_CTAB)));

    public static final RegistryObject<Item> ANT_EGG_SPAWN_EGG = ITEMS.register("ant_larva_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.ANTEGG, 0x2323a, 0x31245b,
                    new Item.Properties().tab(ModCreativeModeTab.ANT_MOD_CTAB)));

    /*public static final RegistryObject<Item> ANT_EFFECT_CLOUD_SPAWN_EGG = ITEMS.register("cloud_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntityTypes.ANT_EFFECT_CLOUD, 0x2323a, 0x31245b,
                    new Item.Properties().tab(ModCreativeModeTab.ANT_MOD_CTAB)));*/

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
