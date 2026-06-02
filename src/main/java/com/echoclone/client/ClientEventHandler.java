package com.echoclone.client;

import com.echoclone.data.RecordedAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.echoclone.EchoCloneMod;

@Mod.EventBusSubscriber(modid = EchoCloneMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEventHandler {

    private static RecordedAction.ActionType pendingAction = RecordedAction.ActionType.NONE;
    private static net.minecraft.core.BlockPos pendingBlockPos = null;

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return;

        if (KeyBindings.RECORD_KEY.consumeClick()) {
            RecordingManager.toggleRecording();
        }

        if (KeyBindings.MANAGER_KEY.consumeClick()) {
            mc.setScreen(new CloneManagerScreen());
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        if (RecordingManager.isRecording()) {
            RecordingManager.tick(player, pendingAction, pendingBlockPos);
            // reset pending action after recording it
            pendingAction = RecordedAction.ActionType.NONE;
            pendingBlockPos = null;
        }
    }

    @SubscribeEvent
    public static void onMouseClick(InputEvent.MouseButton.Pre event) {
        if (!RecordingManager.isRecording()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (event.getButton() == 0) { // Left click
            if (mc.hitResult != null &&
                    mc.hitResult.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
                net.minecraft.world.phys.BlockHitResult blockHit =
                        (net.minecraft.world.phys.BlockHitResult) mc.hitResult;
                pendingAction = RecordedAction.ActionType.BREAK_BLOCK;
                pendingBlockPos = blockHit.getBlockPos();
            } else {
                pendingAction = RecordedAction.ActionType.ATTACK;
                pendingBlockPos = null;
            }
        } else if (event.getButton() == 1) { // Right click
            if (mc.hitResult != null &&
                    mc.hitResult.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
                net.minecraft.world.phys.BlockHitResult blockHit =
                        (net.minecraft.world.phys.BlockHitResult) mc.hitResult;
                pendingAction = RecordedAction.ActionType.PLACE_BLOCK;
                pendingBlockPos = blockHit.getBlockPos();
            } else {
                pendingAction = RecordedAction.ActionType.INTERACT;
                pendingBlockPos = null;
            }
        }
    }

    public static void setPendingAction(RecordedAction.ActionType type,
                                         net.minecraft.core.BlockPos pos) {
        pendingAction = type;
        pendingBlockPos = pos;
    }
}
