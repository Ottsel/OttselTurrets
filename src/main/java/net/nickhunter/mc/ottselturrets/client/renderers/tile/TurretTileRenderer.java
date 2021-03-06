package net.nickhunter.mc.ottselturrets.client.renderers.tile;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.nickhunter.mc.ottselturrets.blocks.tile.TurretTileEntity;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

public class TurretTileRenderer extends GeoBlockRenderer<TurretTileEntity> {
    public TurretTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn,
            AnimatedGeoModel<TurretTileEntity> model) {
        super(rendererDispatcherIn, model);
    }

    @Override
    public void render(TileEntity tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn,
            int combinedLightIn, int combinedOverlayIn) {
        super.render(tile, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
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
