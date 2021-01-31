package net.nickhunter.mc.ottselturrets.client.models.tile;

import net.minecraft.util.ResourceLocation;
import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.blocks.tile.TurretTileEntity;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.resource.GeckoLibCache;

public abstract class TurretTileModel extends AnimatedGeoModel<TurretTileEntity> {

    public final String modelLocation;
    public final String textureLocation;
    public final String animationLocation;

    float pitch = 0;
    float yaw = 0;

    TurretTileModel(String modelLocation, String textureLocation, String animationLocation){

        this.modelLocation = modelLocation;
        this.textureLocation = textureLocation;
        this.animationLocation = animationLocation;
    }

    @Override
    public void setLivingAnimations(TurretTileEntity entity, Integer uniqueID) {

        pitch = entity.pitchToTarget;
        yaw = entity.yawToTarget;
        GeckoLibCache.getInstance().parser.setValue("head_rotation_y", yaw);
        GeckoLibCache.getInstance().parser.setValue("head_rotation_x", pitch);
        GeckoLibCache.getInstance().parser.setValue("head_rotation_x_prev", entity.headRotationXPrev);
        GeckoLibCache.getInstance().parser.setValue("head_rotation_y_prev", entity.headRotationYPrev);
        GeckoLibCache.getInstance().parser.setValue("head_rotation_x_max", entity.headPitchMax);
        
        entity.headRotationXPrev = pitch;
        entity.headRotationYPrev = yaw;
        entity.lookingAtTarget = false;

        super.setLivingAnimations(entity, uniqueID);
    }

    @Override
    public ResourceLocation getModelLocation(TurretTileEntity tileEntity) {
        return new ResourceLocation(OttselTurrets.MOD_ID, modelLocation);
    }

    @Override
    public ResourceLocation getTextureLocation(TurretTileEntity tileEntity) {
        return new ResourceLocation(OttselTurrets.MOD_ID, textureLocation);
    }

    @Override
    public ResourceLocation getAnimationFileLocation(TurretTileEntity tileEntity) {
        return new ResourceLocation(OttselTurrets.MOD_ID, animationLocation);
    }
}
