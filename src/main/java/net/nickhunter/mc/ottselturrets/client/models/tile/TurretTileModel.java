package net.nickhunter.mc.ottselturrets.client.models.tile;

import net.minecraft.util.ResourceLocation;
import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.blocks.tile.TurretTileEntity;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.resource.GeckoLibCache;

public class TurretTileModel extends AnimatedGeoModel<TurretTileEntity> {

    public final String modelLocation;
    public final String textureLocation;
    public final String animationLocation;

    private float pitch = 0;
    private float yaw = 0;

    float headRotationXPrev = 0;
    float headRotationYPrev = 0;

    TurretTileModel(String modelLocation, String textureLocation, String animationLocation) {

        this.modelLocation = modelLocation;
        this.textureLocation = textureLocation;
        this.animationLocation = animationLocation;
    }

    @Override
    public void setLivingAnimations(TurretTileEntity entity, Integer uniqueID) {

        pitch = entity.getPitchToTarget();
        yaw = entity.getYawToTarget();
        
        this.headRotationXPrev = pitch;
        this.headRotationYPrev = yaw;

        GeckoLibCache.getInstance().parser.setValue("head_rotation_y", yaw);
        GeckoLibCache.getInstance().parser.setValue("head_rotation_x", pitch);
        GeckoLibCache.getInstance().parser.setValue("head_rotation_x_prev", headRotationXPrev);
        GeckoLibCache.getInstance().parser.setValue("head_rotation_y_prev", headRotationYPrev);
        GeckoLibCache.getInstance().parser.setValue("head_rotation_x_max", entity.getHeadPitchMax());

        entity.setLookingAtTarget(false);

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
