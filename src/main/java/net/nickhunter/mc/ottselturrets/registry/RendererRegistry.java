package net.nickhunter.mc.ottselturrets.registry;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.nickhunter.mc.ottselturrets.client.models.tile.BallistaTurretTileModel;
import net.nickhunter.mc.ottselturrets.client.models.tile.LaserTurretTileModel;
import net.nickhunter.mc.ottselturrets.client.renderers.tile.TurretTileRenderer;

public class RendererRegistry {
    public static void init() {

        // TileEntities
        ClientRegistry.bindTileEntityRenderer(TileRegistry.LASER_TURRET.get(),
                (rendererDispatcherIn) -> new TurretTileRenderer(rendererDispatcherIn, new LaserTurretTileModel()));
        ClientRegistry.bindTileEntityRenderer(TileRegistry.BALLISTA_TURRET.get(),
                (rendererDispatcherIn) -> new TurretTileRenderer(rendererDispatcherIn, new BallistaTurretTileModel()));
    }
}
