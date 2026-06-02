package com.echoclone.data;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/**
 * Represents one tick of recorded player action.
 */
public class RecordedAction {

    public enum ActionType {
        NONE,
        JUMP,
        ATTACK,
        PLACE_BLOCK,
        BREAK_BLOCK,
        INTERACT,
        USE_ITEM
    }

    public double relX;        // relative to clone spawn origin
    public double relY;
    public double relZ;
    public float yaw;
    public float pitch;
    public boolean isSprinting;
    public boolean isSneaking;
    public boolean isSwimming;
    public ActionType actionType;
    public BlockPos blockPos;  // for PLACE/BREAK
    public ItemStack heldItem; // snapshot of held item

    public RecordedAction() {
        this.actionType = ActionType.NONE;
        this.heldItem = ItemStack.EMPTY;
    }

    public RecordedAction(double relX, double relY, double relZ,
                          float yaw, float pitch,
                          boolean sprinting, boolean sneaking, boolean swimming,
                          ActionType type, BlockPos blockPos, ItemStack heldItem) {
        this.relX = relX;
        this.relY = relY;
        this.relZ = relZ;
        this.yaw = yaw;
        this.pitch = pitch;
        this.isSprinting = sprinting;
        this.isSneaking = sneaking;
        this.isSwimming = swimming;
        this.actionType = type;
        this.blockPos = blockPos;
        this.heldItem = heldItem != null ? heldItem.copy() : ItemStack.EMPTY;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(relX);
        buf.writeDouble(relY);
        buf.writeDouble(relZ);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
        buf.writeBoolean(isSprinting);
        buf.writeBoolean(isSneaking);
        buf.writeBoolean(isSwimming);
        buf.writeEnum(actionType);
        buf.writeBoolean(blockPos != null);
        if (blockPos != null) {
            buf.writeBlockPos(blockPos);
        }
        buf.writeItem(heldItem);
    }

    public static RecordedAction fromBytes(FriendlyByteBuf buf) {
        RecordedAction action = new RecordedAction();
        action.relX = buf.readDouble();
        action.relY = buf.readDouble();
        action.relZ = buf.readDouble();
        action.yaw = buf.readFloat();
        action.pitch = buf.readFloat();
        action.isSprinting = buf.readBoolean();
        action.isSneaking = buf.readBoolean();
        action.isSwimming = buf.readBoolean();
        action.actionType = buf.readEnum(ActionType.class);
        if (buf.readBoolean()) {
            action.blockPos = buf.readBlockPos();
        }
        action.heldItem = buf.readItem();
        return action;
    }
}
