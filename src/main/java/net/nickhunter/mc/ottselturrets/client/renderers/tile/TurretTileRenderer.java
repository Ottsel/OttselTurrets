package net.nickhunter.mc.ottselturrets.client.renderers.tile;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.nickhunter.mc.ottselturrets.blocks.tile.TurretTileEntity;
import net.nickhunter.mc.ottselturrets.client.models.tile.TurretTileModel;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

public class TurretTileRenderer extends GeoBlockRenderer<TurretTileEntity> {
    public TurretTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn, new TurretTileModel());
    }

    @Override
    protected void rotateBlock(Direction facing, MatrixStack stack) {
        switch (facing) {
            case SOUTH:
                stack.rotate(Vector3f.YP.rotationDegrees(180));
                break;
            case WEST:
                stack.rotate(Vector3f.YP.rotationDegrees(90));
                break;
            case EAST:
                stack.rotate(Vector3f.YN.rotationDegrees(90));
                break;
        }
    }
}
