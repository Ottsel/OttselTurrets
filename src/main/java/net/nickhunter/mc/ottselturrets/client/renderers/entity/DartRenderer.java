package net.nickhunter.mc.ottselturrets.client.renderers.entity;

import net.minecraft.client.renderer.entity.*;
import net.nickhunter.mc.ottselturrets.client.models.entity.DartModel;
import net.nickhunter.mc.ottselturrets.entities.DartEntity;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class DartRenderer extends GeoEntityRenderer<DartEntity> {
    public DartRenderer(EntityRendererManager renderManager) {
        super(renderManager, new DartModel());
    }
    //matrixStackIn.scale(entityIn.beamLength,1,1);
    //matrixStackIn.rotate(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, entityIn.prevRotationYaw, entityIn.rotationYaw)));
    //matrixStackIn.rotate(Vector3f.XN.rotationDegrees(MathHelper.lerp(partialTicks, entityIn.prevRotationPitch, entityIn.rotationPitch)));
}

