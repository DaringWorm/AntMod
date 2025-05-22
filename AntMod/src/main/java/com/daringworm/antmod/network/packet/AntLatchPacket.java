package com.daringworm.antmod.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class AntLatchPacket {
    public UUID id;
    public Vec3 pos;
    public Vec3 motion;
    public float yaw;
    public float pitch;

    public AntLatchPacket(UUID id, Vec3 pos, Vec3 motion, float yaw, float pitch) {
        this.id = id;
        this.pos = pos;
        this.motion = motion;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static void encode(AntLatchPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.id);
        buf.writeDouble(msg.pos.x);
        buf.writeDouble(msg.pos.y);
        buf.writeDouble(msg.pos.z);
        buf.writeDouble(msg.motion.x);
        buf.writeDouble(msg.motion.y);
        buf.writeDouble(msg.motion.z);
        buf.writeFloat(msg.yaw);
        buf.writeFloat(msg.pitch);
    }

    public static AntLatchPacket decode(FriendlyByteBuf buf) {
        return new AntLatchPacket(
                buf.readUUID(),
                new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                buf.readFloat(),
                buf.readFloat()
        );
    }

    public static void handle(AntLatchPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            //System.out.println("[Server] Got AntMovePacket on thread: " + Thread.currentThread().getName());
            ServerPlayer sender = ctx.get().getSender();
            if (sender == null) {
                System.out.println("[Server] No sender, probably client side — abort.");
                return;
            }

            ServerLevel level = sender.getLevel();
            Entity entity = level.getEntity(msg.id);

            //level.setBlock(sender.blockPosition().above(2), Blocks.DIAMOND_BLOCK.defaultBlockState(), 2);

            if (entity != null) {
                //entity.startRiding(entity);
                entity.absMoveTo(msg.pos.x, msg.pos.y, msg.pos.z, msg.yaw, msg.pitch);
                entity.resetFallDistance();
                //entity.setDeltaMovement(msg.motion);
                //entity.setYBodyRot(msg.yaw);
                //level.getChunkSource().broadcast(entity, new ClientboundTeleportEntityPacket(entity));
            } else {
                sender.sendMessage(Component.nullToEmpty("[Server] Could not find entity for UUID: " + msg.id),
                        sender.getUUID());

            }
        });

        ctx.get().setPacketHandled(true);
    }
}