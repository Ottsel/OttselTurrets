package net.nickhunter.mc.ottselturrets.client.models.tile;

import net.minecraft.util.ResourceLocation;
import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.blocks.tile.TurretTileEntity;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.resource.GeckoLibCache;

import static net.nickhunter.mc.ottselturrets.TurretType.getTextureFromType;

public class TurretTileModel extends AnimatedGeoModel<TurretTileEntity> {

    public static final float headRotationXMax = 15;
    float headRotationYPrev;
    float headRotationXPrev;

    @Override
    public void setLivingAnimations(TurretTileEntity entity, Integer uniqueID) {
        float pitch = entity.pitchToTarget;
        if(pitch > 15){
            pitch = 15;
        }
        if(pitch < -15){
            pitch = -15;
        }
        GeckoLibCache.getInstance().parser.setValue("head_rotation_x_max",headRotationXMax);
        GeckoLibCache.getInstance().parser.setValue("head_rotation_y_prev",headRotationYPrev);
        GeckoLibCache.getInstance().parser.setValue("head_rotation_y",entity.yawToTarget);
        GeckoLibCache.getInstance().parser.setValue("head_rotation_x_prev",headRotationXPrev);
        GeckoLibCache.getInstance().parser.setValue("head_rotation_x",pitch);
        GeckoLibCache.getInstance().parser.setValue("beam_length",entity.beamLength);
        headRotationXPrev = pitch;
        headRotationYPrev = entity.yawToTarget;
        super.setLivingAnimations(entity, uniqueID);
    }

    @Override
    public ResourceLocation getModelLocation(TurretTileEntity tileEntity) {
        return new ResourceLocation(OttselTurrets.MOD_ID, "geo/block/turret_horizontal.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(TurretTileEntity tileEntity) {
        return getTextureFromType(tileEntity.turretType);
    }

    @Override
    public ResourceLocation getAnimationFileLocation(TurretTileEntity tileEntity) {
        return new ResourceLocation(OttselTurrets.MOD_ID, "animations/block/turret.animation.json");
    }
}
