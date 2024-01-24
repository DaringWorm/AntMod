package com.daringworm.antmod.entity;

import com.daringworm.antmod.AntMod;
import com.daringworm.antmod.entity.custom.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class ModEntityTypes {

   public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES, AntMod.MOD_ID);


   public static final RegistryObject<EntityType<WorkerAnt>> WORKERANT =
           ENTITY_TYPES.register("workerant", () -> EntityType.Builder.of(WorkerAnt::new, MobCategory.CREATURE)
                   .sized( 0.6f, 0.5f)
                   .build(new ResourceLocation(AntMod.MOD_ID, "workerant").toString())
           );

    public static final RegistryObject<EntityType<QueenAnt>> QUEENANT =
            ENTITY_TYPES.register("queenant", () -> EntityType.Builder.of(QueenAnt::new, MobCategory.CREATURE)
                    .sized( 2f, 2f)
                    .build(new ResourceLocation(AntMod.MOD_ID, "queenant").toString())
            );

    public static final RegistryObject<EntityType<AntEgg>> ANTEGG =
            ENTITY_TYPES.register("antegg", () -> EntityType.Builder.of(AntEgg::new, MobCategory.CREATURE)
                    .sized( 0.5f, 0.5f)
                    .build(new ResourceLocation(AntMod.MOD_ID, "antegg").toString())
            );

    public static final RegistryObject<EntityType<AntCarver>> ANTCARVER =
            ENTITY_TYPES.register("antcarver", () -> EntityType.Builder.of(AntCarver::new, MobCategory.CREATURE)
                    .sized( 0.5f, 0.5f)
                    .build(new ResourceLocation(AntMod.MOD_ID, "antcarver").toString())
            );

    public static final RegistryObject<EntityType<AntLarva>> ANTLARVA =
            ENTITY_TYPES.register("antlarva", () -> EntityType.Builder.of(AntLarva::new, MobCategory.CREATURE)
                    .sized( 0.6f, 0.5f)
                    .build(new ResourceLocation(AntMod.MOD_ID, "antlarva").toString())
            );

    public static final RegistryObject<EntityType<AntScentCloud>> ANT_EFFECT_CLOUD =
            ENTITY_TYPES.register("ant_effect_cloud", () -> EntityType.Builder.of(AntScentCloud::new, MobCategory.AMBIENT)
                    .sized( 20f, 15f).fireImmune()
                    .build(new ResourceLocation(AntMod.MOD_ID, "ant_effect_cloud").toString())
            );


   public static void register(IEventBus eventBus){ENTITY_TYPES.register(eventBus);
   }

}
