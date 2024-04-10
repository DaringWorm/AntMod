package com.daringworm.antmod.block;


import com.daringworm.antmod.AntMod;
import com.daringworm.antmod.block.custom.*;
import com.daringworm.antmod.item.ModCreativeModeTab;
import com.daringworm.antmod.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;


public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, AntMod.MOD_ID);



    public static final RegistryObject<Block> ANT_STONE = registerBlock("ant_stone",
            () -> new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5f)
                    .requiresCorrectToolForDrops()), ModCreativeModeTab.ANT_MOD_CTAB);

    public static final RegistryObject<Block> ANT_DIRT = registerBlock("ant_dirt",
            () -> new Block(BlockBehaviour.Properties.of(Material.DIRT).strength(0.5f)
                    .requiresCorrectToolForDrops()), ModCreativeModeTab.ANT_MOD_CTAB);

    public static final RegistryObject<Block> ANT_DEBRIS = registerBlock("ant_debris",
            () -> new Block(BlockBehaviour.Properties.of(Material.SPONGE).strength(1f)
                    .requiresCorrectToolForDrops()), ModCreativeModeTab.ANT_MOD_CTAB);

    public static final RegistryObject<Block> GLOWING_DEBRIS = registerBlock("luminous_ant_debris",
            () -> new Block(BlockBehaviour.Properties.of(Material.SPONGE).lightLevel((p_50872_) -> {return 8;}).strength(0.8f)
                    .requiresCorrectToolForDrops()), ModCreativeModeTab.ANT_MOD_CTAB);

    public static final RegistryObject<Block> FUNGUS_GARDEN = registerBlock("fungus_garden",
            () -> new FungusGarden(BlockBehaviour.Properties.of(Material.SPONGE).lightLevel((p_50872_) -> {return 3;}).strength(0.8f)
                    .requiresCorrectToolForDrops()), ModCreativeModeTab.ANT_MOD_CTAB);

    public static final RegistryObject<Block> FUNGUS_BLOCK = registerBlock("fungus_block",
            () -> new Block(BlockBehaviour.Properties.of(Material.SPONGE).lightLevel((p_50872_) -> {return 3;}).strength(0.8f)
                    .requiresCorrectToolForDrops()), ModCreativeModeTab.ANT_MOD_CTAB);

    public static final RegistryObject<Block> FUNGUS_FUZZ = registerBlock("fungus_fuzz",
            () -> new FungusFuzz(BlockBehaviour.Properties.of(Material.LEAVES)
                    .requiresCorrectToolForDrops().noOcclusion().noCollission().instabreak()), ModCreativeModeTab.ANT_MOD_CTAB);


    public static final RegistryObject<Block> LEAFY_CONTAINER_BLOCK = registerBlock("fungal_container_block",
            () -> new FungalContainer(BlockBehaviour.Properties.copy(Blocks.BIRCH_LEAVES).noOcclusion()),
            ModCreativeModeTab.ANT_MOD_CTAB);

    public static final RegistryObject<Block> ANT_AIR = registerBlock("ant_air",
            () -> new AntAir(BlockBehaviour.Properties.copy(Blocks.AIR).noOcclusion()),
            ModCreativeModeTab.ANT_MOD_CTAB);

    public static final RegistryObject<Block> FERTILE_AIR = registerBlock("fertile_air",
            () -> new FertileAir(BlockBehaviour.Properties.copy(Blocks.AIR).noOcclusion()),
            ModCreativeModeTab.ANT_MOD_CTAB);

    public static final RegistryObject<Block> FUNGUS_CARPET = registerBlock("fungus_carpet",
            () -> new FungusCarpet(BlockBehaviour.Properties.of(Material.LEAVES).strength(0.2f).lightLevel((p_50872_) ->
                    12).strength(0.8f).requiresCorrectToolForDrops().noOcclusion()), ModCreativeModeTab.ANT_MOD_CTAB);

    public static final RegistryObject<Block> LEAFY_MIXTURE = registerBlock("leafy_mixture",
            () -> new LeafyMixture(BlockBehaviour.Properties.of(Material.LEAVES).strength(0.2f).
                    strength(0.8f).requiresCorrectToolForDrops().noOcclusion()), ModCreativeModeTab.ANT_MOD_CTAB);

    public static final RegistryObject<Block> WING_DEBRIS = registerBlock("wing_debris",
            () -> new LeafyMixture(BlockBehaviour.Properties.of(Material.LEAVES).strength(0.2f).
                    strength(0.8f).requiresCorrectToolForDrops().noOcclusion()), ModCreativeModeTab.ANT_MOD_CTAB);



    private static <T extends Block> RegistryObject<T> registerBlock
            (String name, Supplier<T> block, CreativeModeTab tab){
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn, tab);
        return toReturn;
    }


    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block, CreativeModeTab tab){
    return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(),
            new Item.Properties().tab(tab)));
    }



    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}

