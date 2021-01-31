package net.nickhunter.mc.ottselturrets.client.models.tile;

import net.nickhunter.mc.ottselturrets.blocks.tile.TurretTileEntity;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.resource.GeckoLibCache;

public abstract class TurretTileModel extends AnimatedGeoModel<TurretTileEntity> {

    float headRotationYPrev;
    float headRotationXPrev;

    @Override
    public void setLivingAnimations(TurretTileEntity entity, Integer uniqueID) {
        float pitch = entity.pitchToTarget;
        float yaw = entity.yawToTarget;

        if (!entity.aimingPaused) {
            GeckoLibCache.getInstance().parser.setValue("head_rotation_y", yaw);
            GeckoLibCache.getInstance().parser.setValue("head_rotation_x", pitch);
        }
        GeckoLibCache.getInstance().parser.setValue("head_rotation_x_prev", headRotationXPrev);
        GeckoLibCache.getInstance().parser.setValue("head_rotation_y_prev", headRotationYPrev);
        GeckoLibCache.getInstance().parser.setValue("head_rotation_x_max", TurretTileEntity.headPitchMax);

        if (entity.lookingAtTarget) {
            headRotationXPrev = pitch;
            headRotationYPrev = yaw;
            entity.lookingAtTarget = false;
        }
        super.setLivingAnimations(entity, uniqueID);
    }
}
