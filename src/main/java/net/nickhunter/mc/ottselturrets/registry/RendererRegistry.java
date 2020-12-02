package net.nickhunter.mc.ottselturrets.registry;

import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.nickhunter.mc.ottselturrets.client.renderers.entity.DartRenderer;
import net.nickhunter.mc.ottselturrets.client.renderers.tile.TurretTileRenderer;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class RendererRegistry {
    public static void init(){

        //TileEntities
        ClientRegistry.bindTileEntityRenderer(TileRegistry.TURRET.get(), TurretTileRenderer::new);

        //Entities
        RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.DART.get(), DartRenderer::new);
    }
}
