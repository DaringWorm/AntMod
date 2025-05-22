package com.daringworm.antmod.network;

import com.daringworm.antmod.AntMod;
import com.daringworm.antmod.network.packet.AntLatchPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(AntMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }

    public static void initialize(){
        INSTANCE.messageBuilder(AntLatchPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(AntLatchPacket::encode)
                .decoder(AntLatchPacket::decode)
                .consumer(AntLatchPacket::handle)
                .add();
    }

    public static void register() {
        INSTANCE.registerMessage(nextId(), AntLatchPacket.class,
                AntLatchPacket::encode,
                AntLatchPacket::decode,
                AntLatchPacket::handle);
    }
}