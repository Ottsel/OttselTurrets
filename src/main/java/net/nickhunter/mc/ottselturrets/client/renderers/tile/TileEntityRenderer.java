package net.nickhunter.mc.ottselturrets.client.renderers.tile;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;
import net.nickhunter.mc.ottselturrets.blocks.tile.AnimatedTileEntity;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

@SuppressWarnings({ "unchecked" })
public class TileEntityRenderer extends GeoBlockRenderer<AnimatedTileEntity> {
    public TileEntityRenderer(TileEntityRendererDispatcher rendererDispatcherIn,
            AnimatedGeoModel<? extends AnimatedTileEntity> model) {
        super(rendererDispatcherIn, (AnimatedGeoModel<AnimatedTileEntity>) model);
    }

    @Override
    protected void rotateBlock(Direction facing, MatrixStack stack) {
        switch (facing) {
            case NORTH:
                break;
            case EAST:
                stack.rotate(Vector3f.YN.rotationDegrees(90));
                break;
            case SOUTH:
                stack.rotate(Vector3f.YP.rotationDegrees(180));
                break;
            case WEST:
                stack.rotate(Vector3f.YP.rotationDegrees(90));
                break;
            case DOWN:
                break;
            case UP:
                break;
            default:
                break;
        }
    }
}
