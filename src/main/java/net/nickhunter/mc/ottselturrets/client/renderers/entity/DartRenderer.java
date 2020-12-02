package net.nickhunter.mc.ottselturrets.client.renderers.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.client.models.entity.DartModel;
import net.nickhunter.mc.ottselturrets.entities.DartEntity;

public class DartRenderer extends EntityRenderer<DartEntity> {
    public DartRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    public static final ResourceLocation TEXTURE = new ResourceLocation(OttselTurrets.MOD_ID,"textures/entities/laser_dart.png");
    final DartModel model = new DartModel();
    @Override
    public void render(DartEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        IVertexBuilder ivertexbuilder = bufferIn.getBuffer(this.model.getRenderType(TEXTURE));
        matrixStackIn.push();
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, entityIn.prevRotationYaw, entityIn.rotationYaw)));
        matrixStackIn.rotate(Vector3f.XN.rotationDegrees(MathHelper.lerp(partialTicks, entityIn.prevRotationPitch, entityIn.rotationPitch)));
        matrixStackIn.translate(0,-1,0);
        this.model.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        matrixStackIn.pop();
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getEntityTexture(DartEntity entity) {
        return TEXTURE;
    }
}

