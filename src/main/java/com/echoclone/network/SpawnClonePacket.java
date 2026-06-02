package com.echoclone.network;

import com.echoclone.ModRegistry;
import com.echoclone.data.RecordedAction;
import com.echoclone.entity.CloneEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Sent from client -> server when a recording ends.
 * The server spawns a CloneEntity with the recorded actions.
 */
public class SpawnClonePacket {

    private static final int MAX_CLONES_PER_PLAYER = 10;
    private static final int MAX_TICKS = 20 * 60 * 5; // 5 minutes max recording

    private final List<RecordedAction> actions;
    private final Vec3 origin;
    private final String skinId;

    public SpawnClonePacket(List<RecordedAction> actions, Vec3 origin, String skinId) {
        // Cap recording length to prevent abuse
        this.actions = actions.size() > MAX_TICKS
                ? new ArrayList<>(actions.subList(0, MAX_TICKS))
                : new ArrayList<>(actions);
        this.origin = origin;
        this.skinId = skinId;
    }

    public static void encode(SpawnClonePacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.actions.size());
        for (RecordedAction action : packet.actions) {
            action.toBytes(buf);
        }
        buf.writeDouble(packet.origin.x);
        buf.writeDouble(packet.origin.y);
        buf.writeDouble(packet.origin.z);
        buf.writeUtf(packet.skinId, 64);
    }

    public static SpawnClonePacket decode(FriendlyByteBuf buf) {
        int count = buf.readInt();
        List<RecordedAction> actions = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            actions.add(RecordedAction.fromBytes(buf));
        }
        Vec3 origin = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        String skinId = buf.readUtf(64);
        return new SpawnClonePacket(actions, origin, skinId);
    }

    public static void handle(SpawnClonePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender == null) return;

            ServerLevel level = (ServerLevel) sender.level();

            // Count existing clones owned by this player
            long existingCount = level.getEntities(
                    ModRegistry.CLONE_ENTITY.get(),
                    e -> e instanceof CloneEntity clone
                            && sender.getName().getString().equals(
                                extractPlayerName(clone.getOwnerName()))
            ).stream().count();

            if (existingCount >= MAX_CLONES_PER_PLAYER) {
                sender.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable("echoclone.recording.toomany")
                );
                return;
            }

            if (packet.actions.isEmpty()) return;

            CloneEntity clone = ModRegistry.CLONE_ENTITY.get().create(level);
            if (clone == null) return;

            clone.initClone(
                    packet.actions,
                    packet.origin,
                    packet.skinId,
                    sender.getUUID(),
                    sender.getName().getString()
            );

            level.addFreshEntity(clone);
        });
        ctx.setPacketHandled(true);
    }

    private static String extractPlayerName(String ownerName) {
        // Format is "PlayerName's Clone"
        if (ownerName.endsWith("'s Clone")) {
            return ownerName.substring(0, ownerName.length() - "'s Clone".length());
        }
        return ownerName;
    }
}
