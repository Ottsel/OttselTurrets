package com.hunterpnw.mc.ottselturrets.registry;

import com.hunterpnw.mc.ottselturrets.client.renderers.tile.TurretTileRenderer;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class RendererRegistry {
    public static void init(){
        ClientRegistry.bindTileEntityRenderer(TileRegistry.TURRET.get(), TurretTileRenderer::new);
    }
}
