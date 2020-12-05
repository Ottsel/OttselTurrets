package net.nickhunter.mc.ottselturrets.client.renderers.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimatableModel;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.GeoModelProvider;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import software.bernie.geckolib3.util.AnimationUtils;
import net.minecraft.entity.Entity;

import java.awt.*;

import static net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;

public class NonLivingEntityRenderer<T extends Entity & IAnimatable> extends EntityRenderer<T> implements IGeoRenderer<T> {
    static
    {
        AnimationController.addModelFetcher((IAnimatable object) ->
        {
            if (object instanceof net.minecraft.entity.Entity)
            {
                return (IAnimatableModel) AnimationUtils.getGeoModelForEntity((net.minecraft.entity.Entity) object);
            }
            return null;
        });
    }

    private final AnimatedGeoModel<T> modelProvider;

    protected NonLivingEntityRenderer(EntityRendererManager renderManager, AnimatedGeoModel<T> modelProvider) {
        super(renderManager);
        this.modelProvider = modelProvider;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, MatrixStack stack, IRenderTypeBuffer bufferIn, int packedLightIn) {
        GeoModel model = modelProvider.getModel(modelProvider.getModelLocation(entity));
        Minecraft.getInstance().textureManager.bindTexture(getEntityTexture(entity));
        Color renderColor = getRenderColor(entity, partialTicks, stack, bufferIn, null, packedLightIn);
        RenderType renderType = getRenderType(entity, partialTicks, stack, bufferIn, null, packedLightIn, getEntityTexture(entity));
        render(model, entity, partialTicks, renderType, stack, bufferIn, null, packedLightIn, NO_OVERLAY, (float) renderColor.getRed() / 255f, (float) renderColor.getBlue() / 255f, (float) renderColor.getGreen() / 255f, (float) renderColor.getAlpha() / 255);
        super.render(entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getEntityTexture(T entity) {
        return modelProvider.getTextureLocation(entity);
    }

    @Override
    public GeoModelProvider getGeoModelProvider() {
        return this.modelProvider;
    }

    @Override
    public ResourceLocation getTextureLocation(T instance) {
        return this.modelProvider.getTextureLocation(instance);
    }
}
