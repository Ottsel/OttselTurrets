package net.nickhunter.mc.ottselturrets.client.renderers.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.math.MathHelper;
import net.nickhunter.mc.ottselturrets.client.models.entity.DartModel;
import net.nickhunter.mc.ottselturrets.entities.DartEntity;
import software.bernie.geckolib3.resource.GeckoLibCache;


public class DartRenderer extends NonLivingEntityRenderer<DartEntity> {
    public DartRenderer(EntityRendererManager renderManager) {
        super(renderManager, new DartModel());
    }

    @Override
    public void render(DartEntity entity, float entityYaw, float partialTicks, MatrixStack stack, IRenderTypeBuffer bufferIn, int packedLightIn) {
        GeckoLibCache.getInstance().parser.setValue("dart_beam_length",entity.beamLength);
        stack.rotate(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks,entity.prevRotationYaw,entity.rotationYaw)));
        stack.rotate(Vector3f.XN.rotationDegrees(MathHelper.lerp(partialTicks, entity.prevRotationPitch,entity.rotationPitch)));
        super.render(entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn);
    }
}

