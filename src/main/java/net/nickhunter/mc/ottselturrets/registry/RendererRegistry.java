package net.nickhunter.mc.ottselturrets.registry;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.nickhunter.mc.ottselturrets.client.models.tile.BallistaTurretTileModel;
import net.nickhunter.mc.ottselturrets.client.models.tile.LaserTurretTileModel;
import net.nickhunter.mc.ottselturrets.client.models.tile.ProjectorTileModel;
import net.nickhunter.mc.ottselturrets.client.renderers.tile.TileEntityRenderer;

public class RendererRegistry {
    public static void init() {

        // TileEntities
        ClientRegistry.bindTileEntityRenderer(TileRegistry.LASER_TURRET.get(),
                (rendererDispatcherIn) -> new TileEntityRenderer(rendererDispatcherIn, new LaserTurretTileModel()));
        ClientRegistry.bindTileEntityRenderer(TileRegistry.BALLISTA_TURRET.get(),
                (rendererDispatcherIn) -> new TileEntityRenderer(rendererDispatcherIn, new BallistaTurretTileModel()));
        ClientRegistry.bindTileEntityRenderer(TileRegistry.PROJECTOR.get(),
                (rendererDispatcherIn) -> new TileEntityRenderer(rendererDispatcherIn, new ProjectorTileModel()));
    }
}
