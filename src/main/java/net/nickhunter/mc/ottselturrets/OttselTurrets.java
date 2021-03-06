package net.nickhunter.mc.ottselturrets;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.nickhunter.mc.ottselturrets.network.NetworkChannel;
import net.nickhunter.mc.ottselturrets.registry.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib3.GeckoLib;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("ottselturrets")
public class OttselTurrets {
    private static OttselTurrets instance;
    public static final String MOD_ID = "ottselturrets";
    public static final String VERSION = "0.1.0a";
    public static final Logger LOGGER = LogManager.getLogger();
    private final NetworkChannel networkChannel = new NetworkChannel(MOD_ID);
    public static final int TICKS_PER_SECOND = 20;

    public OttselTurrets() {
        instance = this;
        GeckoLib.initialize();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        MinecraftForge.EVENT_BUS.register(this);

        BlockRegistry.init();
        ItemRegistry.init();
        TileRegistry.init();
        EntityRegistry.init();
        networkChannel.registerCommonMessages();
    }

    public static NetworkChannel getNetworkChannel() {
        return instance.networkChannel;
    }

    private void setup(final FMLCommonSetupEvent event) {

    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        RendererRegistry.init();
    }
}
