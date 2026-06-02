package com.echoclone;

import com.echoclone.entity.CloneEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRegistry {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, EchoCloneMod.MOD_ID);

    public static final RegistryObject<EntityType<CloneEntity>> CLONE_ENTITY =
            ENTITIES.register("clone", () ->
                    EntityType.Builder.<CloneEntity>of(CloneEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build(new ResourceLocation(EchoCloneMod.MOD_ID, "clone").toString())
            );

    public static void registerAll(IEventBus bus) {
        ENTITIES.register(bus);
    }
}
