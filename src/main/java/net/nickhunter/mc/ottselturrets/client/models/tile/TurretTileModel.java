package net.nickhunter.mc.ottselturrets.client.models.tile;

import net.minecraft.util.ResourceLocation;
import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.blocks.tile.TurretTileEntity;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.resource.GeckoLibCache;

import static net.nickhunter.mc.ottselturrets.TurretType.getTextureFromType;

public class TurretTileModel extends AnimatedGeoModel<TurretTileEntity> {

    float headRotationYPrev;

    @Override
    public void setLivingAnimations(TurretTileEntity entity, Integer uniqueID) {
        GeckoLibCache.getInstance().parser.setValue("head_rotation_y_prev",headRotationYPrev);
        GeckoLibCache.getInstance().parser.setValue("head_rotation_y",entity.yawToTarget);
        GeckoLibCache.getInstance().parser.setValue("beam_length",entity.beamLength);
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
