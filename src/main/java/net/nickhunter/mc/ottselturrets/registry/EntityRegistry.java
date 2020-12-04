package net.nickhunter.mc.ottselturrets.registry;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.entities.DartEntity;

public class EntityRegistry {

    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, OttselTurrets.MOD_ID);

    public static void init() {
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final String LASER_DART_NAME = "laser_dart";
    public static final RegistryObject<EntityType<DartEntity>> DART = ENTITIES.register(LASER_DART_NAME,
            () -> EntityType.Builder.create((EntityType.IFactory<DartEntity>) DartEntity::new, EntityClassification.MISC)
                    .size(1, 1)
                    .build(new ResourceLocation(LASER_DART_NAME, OttselTurrets.MOD_ID).toString())
    );
}
