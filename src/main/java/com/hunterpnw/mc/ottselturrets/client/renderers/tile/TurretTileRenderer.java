package com.hunterpnw.mc.ottselturrets.client.renderers.tile;

import com.hunterpnw.mc.ottselturrets.client.models.tile.TurretTileModel;
import com.hunterpnw.mc.ottselturrets.blocks.tile.TurretTileEntity;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

public class TurretTileRenderer extends GeoBlockRenderer<TurretTileEntity> {
    public TurretTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn, new TurretTileModel());
    }
}
