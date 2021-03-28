package net.nickhunter.mc.ottselturrets.registry;

import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.nickhunter.mc.ottselturrets.OttselTurrets;

public class ParticleRegistry {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister
            .create(ForgeRegistries.PARTICLE_TYPES, OttselTurrets.MOD_ID);

    public static final RegistryObject<BasicParticleType> BEAM_PARTICLE = PARTICLE_TYPES.register("beam_particle",
            () -> new BasicParticleType(true));

    public static void init() {
        PARTICLE_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
