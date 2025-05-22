package com.daringworm.antmod.event;

import com.daringworm.antmod.AntMod;
import com.daringworm.antmod.entity.brains.parts.pathfinding.PathFinder;
import com.daringworm.antmod.entity.custom.WorkerAnt;
import com.daringworm.antmod.util.AntUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;

@Mod.EventBusSubscriber(modid = AntMod.MOD_ID, value = Dist.CLIENT)
public class ClientEventBusEvents {


    @SubscribeEvent
    public static void postRenderWorld(final RenderLevelLastEvent event) {
    }

    @SubscribeEvent
    public static void onWorldRender(final TickEvent.RenderTickEvent event){
        Minecraft mc = Minecraft.getInstance();

        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;
        if(level == null || player == null || !player.isAlive()){
            //System.out.println("[ClientEventBusEvents.latchAnts] no player, dead player, or no level.");
            return;
        }

        for(WorkerAnt tempAnt : level.getEntitiesOfClass(WorkerAnt.class, player.getBoundingBox().inflate(3))){
            if(tempAnt.hasLatchTarget() && tempAnt.getLatchTarget().equals(player.getUUID())){
                Vec3 latchPos = tempAnt.getLatchPos(player);
                tempAnt.setPosRaw(latchPos.x, latchPos.y, latchPos.z);
                tempAnt.lookAt(player, 100, 0);
            }
        }
    }
}
