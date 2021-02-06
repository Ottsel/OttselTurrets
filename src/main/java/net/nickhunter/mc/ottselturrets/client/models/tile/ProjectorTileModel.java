package net.nickhunter.mc.ottselturrets.client.models.tile;

import net.minecraft.util.ResourceLocation;
import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.blocks.tile.ProjectorTileEntity;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.resource.GeckoLibCache;

public class ProjectorTileModel extends AnimatedGeoModel<ProjectorTileEntity> {

    public static final String MODEL_LOCATION = "geo/block/laser_projector.geo.json";
    public static final String TEXTURE_LOCATION = "textures/blocks/turret_horizontal.png";
    public static final String ANIMATION_LOCATION = "animations/block/laser_projector.animation.json";

    private float pitch = 0;
    private float yaw = 0;

    float rotationXPrev = 0;
    float rotationYPrev = 0;

    @Override
    public void setLivingAnimations(ProjectorTileEntity entity, Integer uniqueID) {
        pitch = entity.getPitchToTarget();
        yaw = entity.getYawToTarget();
        
        this.rotationXPrev = pitch;
        this.rotationYPrev = yaw;

        GeckoLibCache.getInstance().parser.setValue("rotation_y", yaw);
        GeckoLibCache.getInstance().parser.setValue("rotation_x", pitch);
        GeckoLibCache.getInstance().parser.setValue("rotation_x_prev", rotationXPrev);
        GeckoLibCache.getInstance().parser.setValue("rotation_y_prev", rotationYPrev);
        GeckoLibCache.getInstance().parser.setValue("beam_length", ((ProjectorTileEntity) entity).getBeamLength());
        
        super.setLivingAnimations(entity, uniqueID);
    }

    @Override
    public ResourceLocation getAnimationFileLocation(ProjectorTileEntity animatable) {
        return new ResourceLocation(OttselTurrets.MOD_ID, ANIMATION_LOCATION);
    }

    @Override
    public ResourceLocation getModelLocation(ProjectorTileEntity object) {
        return new ResourceLocation(OttselTurrets.MOD_ID, MODEL_LOCATION);
    }

    @Override
    public ResourceLocation getTextureLocation(ProjectorTileEntity object) {
        return new ResourceLocation(OttselTurrets.MOD_ID, TEXTURE_LOCATION);
    }
}