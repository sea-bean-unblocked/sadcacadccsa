package com.echoclone.network;

import com.echoclone.ModRegistry;
import com.echoclone.entity.CloneEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/**
 * Sent from client -> server to remove clone(s).
 */
public class RemoveClonePacket {

    public enum Mode {
        ALL,       // Remove all clones owned by this player
        NEAREST,   // Remove the nearest clone
        BY_INDEX   // Remove a specific clone by index
    }

    private final Mode mode;
    private final int index;

    public RemoveClonePacket(Mode mode, int index) {
        this.mode = mode;
        this.index = index;
    }

    public static void encode(RemoveClonePacket packet, FriendlyByteBuf buf) {
        buf.writeEnum(packet.mode);
        buf.writeInt(packet.index);
    }

    public static RemoveClonePacket decode(FriendlyByteBuf buf) {
        Mode mode = buf.readEnum(Mode.class);
        int index = buf.readInt();
        return new RemoveClonePacket(mode, index);
    }

    public static void handle(RemoveClonePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender == null) return;

            ServerLevel level = (ServerLevel) sender.level();
            String playerName = sender.getName().getString();

            List<CloneEntity> myClones = level.getEntities(
                    ModRegistry.CLONE_ENTITY.get(),
                    e -> e instanceof CloneEntity clone
                            && clone.getOwnerName().startsWith(playerName)
            ).stream()
                    .filter(e -> e instanceof CloneEntity)
                    .map(e -> (CloneEntity) e)
                    .collect(java.util.stream.Collectors.toList());

            switch (packet.mode) {
                case ALL -> myClones.forEach(CloneEntity::discard);
                case NEAREST -> {
                    myClones.stream()
                            .min(java.util.Comparator.comparingDouble(
                                    c -> c.distanceToSqr(sender)))
                            .ifPresent(CloneEntity::discard);
                }
                case BY_INDEX -> {
                    if (packet.index >= 0 && packet.index < myClones.size()) {
                        myClones.get(packet.index).discard();
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
