package com.hunterpnw.mc.ottselturrets;

import com.hunterpnw.mc.ottselturrets.registry.BlockRegistry;
import com.hunterpnw.mc.ottselturrets.registry.ItemRegistry;
import com.hunterpnw.mc.ottselturrets.registry.RendererRegistry;
import com.hunterpnw.mc.ottselturrets.registry.TileRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib3.GeckoLib;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("ottselturrets")
public class OttselTurrets {
    public static final String MOD_ID = "ottselturrets";
    public static final Logger LOGGER = LogManager.getLogger();

    public OttselTurrets() {
        GeckoLib.initialize();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        MinecraftForge.EVENT_BUS.register(this);

        BlockRegistry.init();
        ItemRegistry.init();
        TileRegistry.init();
    }
    private void setup(final FMLCommonSetupEvent event) {

    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        RendererRegistry.init();
    }

}
