package com.daringworm.antmod.block.entity;

import com.daringworm.antmod.AntMod;
import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.block.entity.custom.FungalContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, AntMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<FungalContainerBlockEntity>> FUNGAL_CULTIVAR_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("fungal_container_block_entity", () ->
                    BlockEntityType.Builder.of(FungalContainerBlockEntity::new,
                            ModBlocks.LEAFY_CONTAINER_BLOCK.get()).build(null));



    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
