package net.nickhunter.mc.ottselturrets.registry;

import net.nickhunter.mc.ottselturrets.client.renderers.tile.TurretTileRenderer;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class RendererRegistry {
    public static void init(){
        ClientRegistry.bindTileEntityRenderer(TileRegistry.TURRET.get(), TurretTileRenderer::new);
    }
}
