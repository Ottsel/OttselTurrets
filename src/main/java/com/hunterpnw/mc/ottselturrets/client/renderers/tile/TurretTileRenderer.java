package com.hunterpnw.mc.ottselturrets.client.renderers.tile;

import com.hunterpnw.mc.ottselturrets.client.models.tile.TurretTileModel;
import com.hunterpnw.mc.ottselturrets.blocks.tile.TurretTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

public class TurretTileRenderer extends GeoBlockRenderer<TurretTileEntity> {
    public TurretTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn, new TurretTileModel());
    }

    @Override
    protected void rotateBlock(Direction facing, MatrixStack stack) {
        switch (facing) {
            case SOUTH:
                stack.rotate(Vector3f.XP.rotationDegrees(90));
                stack.translate(0, -0.5, -0.5);
                break;
            case WEST:
                stack.rotate(Vector3f.ZP.rotationDegrees(90));
                stack.translate(0.5, -0.5, 0);
                break;
            case NORTH:
                stack.rotate(Vector3f.XN.rotationDegrees(90));
                stack.translate(0, -0.5, 0.5);
                break;
            case EAST:
                stack.rotate(Vector3f.ZN.rotationDegrees(90));
                stack.translate(-0.5, -0.5, 0);

                break;
            case DOWN:
                stack.rotate(Vector3f.XN.rotationDegrees(180));
                stack.translate(0, -1, 0);
                break;
        }
    }
}
