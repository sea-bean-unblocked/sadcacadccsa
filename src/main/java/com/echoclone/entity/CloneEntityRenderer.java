package com.echoclone.entity;

import com.echoclone.client.ClientSkinManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders the CloneEntity using a player-like skin.
 * Applies the skin texture based on the clone's skinId data.
 */
public class CloneEntityRenderer extends HumanoidMobRenderer<CloneEntity, PlayerModel<CloneEntity>> {

    public CloneEntityRenderer(EntityRendererProvider.Context context) {
        super(context,
                new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false),
                0.5f);

        // Add armor layer so equipped items render
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()
        ));
    }

    @Override
    public ResourceLocation getTextureLocation(CloneEntity entity) {
        return ClientSkinManager.getSkinTexture(entity.getSkinId());
    }

    @Override
    public void render(CloneEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    protected void scale(CloneEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.9375f, 0.9375f, 0.9375f);
    }
}
