package com.echoclone.client;

import com.echoclone.data.RecordedAction;
import com.echoclone.network.ModNetworking;
import com.echoclone.network.SpawnClonePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages client-side recording of player actions.
 * Runs on the client thread each game tick.
 */
public class RecordingManager {

    private static boolean recording = false;
    private static final List<RecordedAction> recordedActions = new ArrayList<>();
    private static Vec3 originPos = Vec3.ZERO;
    private static float originYaw = 0f;

    // Called by ClientEventHandler on key press
    public static void toggleRecording() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        if (!recording) {
            startRecording(player);
        } else {
            stopRecording(player);
        }
    }

    private static void startRecording(LocalPlayer player) {
        recording = true;
        recordedActions.clear();
        originPos = player.position();
        originYaw = player.getYRot();
        player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable("echoclone.recording.start"),
                true
        );
    }

    private static void stopRecording(LocalPlayer player) {
        recording = false;

        if (recordedActions.isEmpty()) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§cNothing was recorded."),
                    true
            );
            return;
        }

        // Send to server to spawn clone
        ModNetworking.sendToServer(new SpawnClonePacket(
                new ArrayList<>(recordedActions),
                originPos,
                ClientSkinManager.getSelectedSkinId()
        ));

        player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable("echoclone.recording.stop"),
                true
        );

        recordedActions.clear();
    }

    /**
     * Called every client tick while recording.
     */
    public static void tick(LocalPlayer player, RecordedAction.ActionType pendingAction,
                            net.minecraft.core.BlockPos pendingBlockPos) {
        if (!recording || player == null) return;

        Vec3 pos = player.position();
        double relX = pos.x - originPos.x;
        double relY = pos.y - originPos.y;
        double relZ = pos.z - originPos.z;

        RecordedAction action = new RecordedAction(
                relX, relY, relZ,
                player.getYRot(), player.getXRot(),
                player.isSprinting(),
                player.isCrouching(),
                player.isSwimming(),
                pendingAction,
                pendingBlockPos,
                player.getMainHandItem()
        );

        recordedActions.add(action);
    }

    public static boolean isRecording() {
        return recording;
    }

    public static int getRecordedTickCount() {
        return recordedActions.size();
    }
}
