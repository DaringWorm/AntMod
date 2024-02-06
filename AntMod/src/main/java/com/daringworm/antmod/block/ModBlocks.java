package com.daringworm.antmod.block;


import com.daringworm.antmod.AntMod;
import com.daringworm.antmod.block.custom.*;
import com.daringworm.antmod.item.ModCreativeModeTab;
import com.daringworm.antmod.item.ModItems;
import com.daringworm.antmod.worldgen.feature.tree.PeachTreeGrower;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;


public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, AntMod.MOD_ID);



    public static final RegistryObject<Block> ANTSTONE = registerBlock("ant_stone",
            () -> new Block(BlockBehaviour.Properties.of(Material.STONE).strength(1.5f)
                    .requiresCorrectToolForDrops()), ModCreativeModeTab.ANT_MOD_CTAB);

    public static final RegistryObject<Block> ANT_DIRT = registerBlock("ant_dirt",
            () -> new Block(BlockBehaviour.Properties.of(Material.DIRT).strength(0.5f)
                    .requiresCorrectToolForDrops()), ModCreativeModeTab.ANT_MOD_CTAB);

    public static final RegistryObject<Block> ANTDEBRIS = registerBlock("ant_debris",
            () -> new Block(BlockBehaviour.Properties.of(Material.SPONGE).strength(1f)
                    .requiresCorrectToolForDrops()), ModCreativeModeTab.ANT_MOD_CTAB);

    public static final RegistryObject<Block> LUMINOUSDEBRIS = registerBlock("luminous_ant_debris",
            () -> new Block(BlockBehaviour.Properties.of(Material.SPONGE).lightLevel((p_50872_) -> {return 8;}).strength(0.8f)
                    .requiresCorrectToolForDrops()), ModCreativeModeTab.ANT_MOD_CTAB);


    public static final RegistryObject<Block> LEAFY_CONTAINER_BLOCK = registerBlock("fungal_container_block",
            () -> new FungalContainer(BlockBehaviour.Properties.copy(Blocks.BIRCH_LEAVES).noOcclusion()),
            ModCreativeModeTab.ANT_MOD_CTAB);

    public static final RegistryObject<Block> ANT_AIR = registerBlock("ant_air",
            () -> new AntAir(BlockBehaviour.Properties.copy(Blocks.AIR).noOcclusion()),
            ModCreativeModeTab.ANT_MOD_CTAB);

    public static final RegistryObject<Block> FERTILE_AIR = registerBlock("fertile_air",
            () -> new FertileAir(BlockBehaviour.Properties.copy(Blocks.AIR).noOcclusion()),
            ModCreativeModeTab.ANT_MOD_CTAB);

    public static final RegistryObject<Block> FUNGUS_BLOCK = registerBlock("fungus_block",
            () -> new FungusBlock(BlockBehaviour.Properties.of(Material.LEAVES).strength(0.2f).lightLevel((p_50872_) ->
                    {return 12;}).strength(0.8f).requiresCorrectToolForDrops().noOcclusion()), ModCreativeModeTab.ANT_MOD_CTAB);

    public static final RegistryObject<Block> LEAFY_MIXTURE = registerBlock("leafy_mixture",
            () -> new LeafyMixture(BlockBehaviour.Properties.of(Material.LEAVES).strength(0.2f).
                    strength(0.8f).requiresCorrectToolForDrops().noOcclusion()), ModCreativeModeTab.ANT_MOD_CTAB);

    public static final RegistryObject<Block> WING_DEBRIS = registerBlock("wing_debris",
            () -> new LeafyMixture(BlockBehaviour.Properties.of(Material.LEAVES).strength(0.2f).
                    strength(0.8f).requiresCorrectToolForDrops().noOcclusion()), ModCreativeModeTab.ANT_MOD_CTAB);

    public static final RegistryObject<Block> PEACH_LEAVES = registerBlock("peach_leaves",
            () -> new Block(BlockBehaviour.Properties.of(Material.LEAVES).strength(1f)
                    .requiresCorrectToolForDrops()), ModCreativeModeTab.ANT_MOD_CTAB);

    public static final RegistryObject<Block> PEACH_SAPLING = registerBlock("ebony_sapling",
            () -> new SaplingBlock(new PeachTreeGrower(), BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING)), ModCreativeModeTab.ANT_MOD_CTAB);

    public static final RegistryObject<Block> COLONY_PLACER = registerBlock("colony_placer",
            () -> new SaplingBlock(new PeachTreeGrower(), BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING)), ModCreativeModeTab.ANT_MOD_CTAB);


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

