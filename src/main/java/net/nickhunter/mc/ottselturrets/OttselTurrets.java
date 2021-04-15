package net.nickhunter.mc.ottselturrets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.nickhunter.mc.ottselturrets.client.particles.BeamParticle;
import net.nickhunter.mc.ottselturrets.registry.BlockRegistry;
import net.nickhunter.mc.ottselturrets.registry.ItemRegistry;
import net.nickhunter.mc.ottselturrets.registry.ParticleRegistry;
import net.nickhunter.mc.ottselturrets.registry.RendererRegistry;
import net.nickhunter.mc.ottselturrets.registry.TileRegistry;
import software.bernie.geckolib3.GeckoLib;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("ottselturrets")
public class OttselTurrets {
    public static final String MOD_ID = "ottselturrets";
    public static final String VERSION = "0.1.0a";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final int TICKS_PER_SECOND = 20;

    public OttselTurrets() {
        GeckoLib.initialize();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::particleRegistration);
        MinecraftForge.EVENT_BUS.register(this);

        BlockRegistry.init();
        ItemRegistry.init();
        TileRegistry.init();
        ParticleRegistry.init();
    }

    private void setup(final FMLCommonSetupEvent event) {

    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        RendererRegistry.init();
    }

    @SuppressWarnings("resource")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void particleRegistration(final ParticleFactoryRegisterEvent event) {
       Minecraft.getInstance().particleEngine.register(ParticleRegistry.BEAM_PARTICLE.get(), BeamParticle.Factory::new);
    }
}
