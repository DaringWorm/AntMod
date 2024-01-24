package com.daringworm.antmod;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.block.entity.ModBlockEntities;
import com.daringworm.antmod.entity.ModEntityTypes;
import com.daringworm.antmod.entity.client.*;
import com.daringworm.antmod.item.ModItems;
import com.daringworm.antmod.screen.FungalContainerScreen;
import com.daringworm.antmod.screen.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib3.GeckoLib;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(AntMod.MOD_ID)
public class AntMod
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "antmod";

    public AntMod() {

        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(eventBus);

        ModBlocks.register(eventBus);

        ModEntityTypes.register(eventBus);

        ModBlockEntities.register(eventBus);

        ModMenuTypes.register((eventBus));


        eventBus.addListener(this::setup);
        eventBus.addListener(this::clientSetup);

        GeckoLib.initialize();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

    }




    private void clientSetup(final FMLClientSetupEvent event) {
        EntityRenderers.register(ModEntityTypes.WORKERANT.get(), WorkerAntRenderer::new);
        EntityRenderers.register(ModEntityTypes.QUEENANT.get(), QueenAntRenderer::new);
        EntityRenderers.register(ModEntityTypes.ANTEGG.get(), AntEggRenderer::new);
        EntityRenderers.register(ModEntityTypes.ANTCARVER.get(), AntCarverRenderer::new);
        EntityRenderers.register(ModEntityTypes.ANTLARVA.get(), AntLarvaRenderer::new);
        EntityRenderers.register(ModEntityTypes.ANT_EFFECT_CLOUD.get(), AntScentCloudRenderer::new);

        ItemBlockRenderTypes.setRenderLayer(ModBlocks.LEAFY_CONTAINER_BLOCK.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.FUNGUS_BLOCK.get(), RenderType.cutout());

        MenuScreens.register(ModMenuTypes.FUNGAL_CULTIVAR_MENU.get(), FungalContainerScreen::new);
    }

    private void setup(final FMLCommonSetupEvent event){}
}