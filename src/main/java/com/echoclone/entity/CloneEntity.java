package com.echoclone.entity;

import com.echoclone.data.RecordedAction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The Echo Clone entity.
 * Loops through a recorded list of player actions on the server side.
 */
public class CloneEntity extends PathfinderMob {

    private static final EntityDataAccessor<String> SKIN_ID =
            SynchedEntityData.defineId(CloneEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> OWNER_NAME =
            SynchedEntityData.defineId(CloneEntity.class, EntityDataSerializers.STRING);

    private List<RecordedAction> actions = new ArrayList<>();
    private int currentTick = 0;
    private Vec3 originPos = Vec3.ZERO;
    private UUID ownerUUID = null;

    public CloneEntity(EntityType<? extends CloneEntity> type, Level level) {
        super(type, level);
        this.setNoAi(true);
        this.setInvulnerable(true);
        this.setSilent(true);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SKIN_ID, "dream");
        this.entityData.define(OWNER_NAME, "Clone");
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    public void initClone(List<RecordedAction> actions, Vec3 origin,
                          String skinId, UUID ownerUUID, String ownerName) {
        this.actions = new ArrayList<>(actions);
        this.originPos = origin;
        this.currentTick = 0;
        this.ownerUUID = ownerUUID;
        this.entityData.set(SKIN_ID, skinId);
        this.entityData.set(OWNER_NAME, ownerName + "'s Clone");
        // Teleport to origin
        this.moveTo(origin.x, origin.y, origin.z, 0, 0);
    }

    public String getSkinId() {
        return this.entityData.get(SKIN_ID);
    }

    public String getOwnerName() {
        return this.entityData.get(OWNER_NAME);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide || actions.isEmpty()) return;

        // Loop through recorded actions
        RecordedAction action = actions.get(currentTick % actions.size());

        // Move to recorded relative position
        double targetX = originPos.x + action.relX;
        double targetY = originPos.y + action.relY;
        double targetZ = originPos.z + action.relZ;

        this.setPos(targetX, targetY, targetZ);
        this.setYRot(action.yaw);
        this.setXRot(action.pitch);
        this.yBodyRot = action.yaw;
        this.yHeadRot = action.yaw;

        // Apply movement flags
        this.setSprinting(action.isSprinting);
        this.setShiftKeyDown(action.isSneaking);

        // Perform actions
        if (action.actionType != RecordedAction.ActionType.NONE) {
            performAction(action);
        }

        currentTick++;
    }

    private void performAction(RecordedAction action) {
        ServerLevel serverLevel = (ServerLevel) this.level();

        switch (action.actionType) {
            case ATTACK -> {
                // Swing arm visually
                this.swing(InteractionHand.MAIN_HAND);
                // Play swing sound
                this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.8f, 1.0f);
            }
            case PLACE_BLOCK -> {
                if (action.blockPos != null && !action.heldItem.isEmpty()) {
                    BlockPos placePos = action.blockPos.above();
                    if (serverLevel.getBlockState(placePos).isAir()) {
                        net.minecraft.world.item.BlockItem blockItem =
                                action.heldItem.getItem() instanceof net.minecraft.world.item.BlockItem bi ? bi : null;
                        if (blockItem != null) {
                            BlockState state = blockItem.getBlock().defaultBlockState();
                            serverLevel.setBlockAndUpdate(placePos, state);
                            this.playSound(state.getSoundType().getPlaceSound(), 0.8f, 1.0f);
                        }
                    }
                }
            }
            case BREAK_BLOCK -> {
                if (action.blockPos != null) {
                    BlockState state = serverLevel.getBlockState(action.blockPos);
                    if (!state.isAir()) {
                        serverLevel.destroyBlock(action.blockPos, true, this);
                    }
                }
            }
            case INTERACT -> {
                // Swing arm
                this.swing(InteractionHand.MAIN_HAND);
            }
            case JUMP -> {
                this.jumpFromGround();
            }
            default -> {}
        }
    }

    // ---- NBT Persistence ----

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("OriginX", originPos.x);
        tag.putDouble("OriginY", originPos.y);
        tag.putDouble("OriginZ", originPos.z);
        tag.putInt("CurrentTick", currentTick);
        tag.putString("SkinId", getSkinId());
        tag.putString("OwnerName", getOwnerName());
        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
        }

        ListTag actionsList = new ListTag();
        for (RecordedAction action : actions) {
            net.minecraft.network.FriendlyByteBuf buf =
                    new net.minecraft.network.FriendlyByteBuf(
                            io.netty.buffer.Unpooled.buffer());
            action.toBytes(buf);
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            CompoundTag actionTag = new CompoundTag();
            actionTag.putByteArray("data", bytes);
            actionsList.add(actionTag);
        }
        tag.put("Actions", actionsList);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        originPos = new Vec3(
                tag.getDouble("OriginX"),
                tag.getDouble("OriginY"),
                tag.getDouble("OriginZ")
        );
        currentTick = tag.getInt("CurrentTick");
        this.entityData.set(SKIN_ID, tag.getString("SkinId"));
        this.entityData.set(OWNER_NAME, tag.getString("OwnerName"));
        if (tag.hasUUID("OwnerUUID")) {
            ownerUUID = tag.getUUID("OwnerUUID");
        }

        actions.clear();
        ListTag actionsList = tag.getList("Actions", Tag.TAG_COMPOUND);
        for (int i = 0; i < actionsList.size(); i++) {
            CompoundTag actionTag = actionsList.getCompound(i);
            byte[] bytes = actionTag.getByteArray("data");
            net.minecraft.network.FriendlyByteBuf buf =
                    new net.minecraft.network.FriendlyByteBuf(
                            io.netty.buffer.Unpooled.wrappedBuffer(bytes));
            actions.add(RecordedAction.fromBytes(buf));
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(net.minecraft.world.damagesource.DamageSource source) {
        // Only the owner (or ops) can remove clones via the manager
        return true;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }
}
