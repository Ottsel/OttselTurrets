package net.nickhunter.mc.ottselturrets.client.models.tile;

import net.minecraft.util.ResourceLocation;
import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.blocks.tile.LaserNodeTileEntity;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.resource.GeckoLibCache;

public class LaserNodeTileModel extends AnimatedGeoModel<LaserNodeTileEntity> {

    public static final String MODEL_LOCATION = "geo/block/laser_projector.geo.json";
    public static final String TEXTURE_LOCATION = "textures/blocks/turret_horizontal.png";
    public static final String ANIMATION_LOCATION = "animations/block/laser_projector.animation.json";

    private float pitch = 0;
    private float yaw = 0;

    float rotationXPrev = 0;
    float rotationYPrev = 0;

    @Override
    public void setLivingAnimations(LaserNodeTileEntity entity, Integer uniqueID) {

        pitch = entity.getPitchToTarget();
        yaw = entity.getYawToTarget();

        this.rotationXPrev = pitch;
        this.rotationYPrev = yaw;

        GeckoLibCache.getInstance().parser.setValue("rotation_y", yaw);
        GeckoLibCache.getInstance().parser.setValue("rotation_x", pitch);
        GeckoLibCache.getInstance().parser.setValue("rotation_x_prev", rotationXPrev);
        GeckoLibCache.getInstance().parser.setValue("rotation_y_prev", rotationYPrev);
        GeckoLibCache.getInstance().parser.setValue("beam_length", entity.getBeamLength());
        GeckoLibCache.getInstance().parser.setValue("beam_start", entity.getBeamStart());

        switch (entity.getState()) {
            case PAIRING:
            case IDLE:
            default:
                getBone("lights_red").setHidden(false);
                getBone("lights_yellow").setHidden(true);
                getBone("lights_green").setHidden(true);
                break;
            case PAIRED_IDLE:
            case PAIRED_RX_OBSTRUCTED:
                getBone("lights_red").setHidden(true);
                getBone("lights_yellow").setHidden(false);
                getBone("lights_green").setHidden(true);
                break;
            case PAIRED_RX:
            case PAIRED_TX:
                getBone("lights_red").setHidden(true);
                getBone("lights_yellow").setHidden(true);
                getBone("lights_green").setHidden(false);
                break;
        }
        if (entity.northOptical) {
            getBone("cable_north").setHidden(false);
        } else {
            getBone("cable_north").setHidden(true);
        }
        if (entity.southOptical) {
            getBone("cable_south").setHidden(false);
        } else {
            getBone("cable_south").setHidden(true);
        }
        if (entity.eastOptical) {
            getBone("cable_east").setHidden(false);
        } else {
            getBone("cable_east").setHidden(true);
        }
        if (entity.westOptical) {
            getBone("cable_west").setHidden(false);
        } else {
            getBone("cable_west").setHidden(true);
        }
        super.setLivingAnimations(entity, uniqueID);
    }

    @Override
    public ResourceLocation getAnimationFileLocation(LaserNodeTileEntity animatable) {
        return new ResourceLocation(OttselTurrets.MOD_ID, ANIMATION_LOCATION);
    }

    @Override
    public ResourceLocation getModelLocation(LaserNodeTileEntity object) {
        return new ResourceLocation(OttselTurrets.MOD_ID, MODEL_LOCATION);
    }

    @Override
    public ResourceLocation getTextureLocation(LaserNodeTileEntity object) {
        return new ResourceLocation(OttselTurrets.MOD_ID, TEXTURE_LOCATION);
    }
}