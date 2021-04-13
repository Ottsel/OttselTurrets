package net.nickhunter.mc.ottselturrets.client.models.tile;

import net.nickhunter.mc.ottselturrets.blocks.tile.LaserTurretTileEntity;
import net.nickhunter.mc.ottselturrets.blocks.tile.TurretTileEntity;
import software.bernie.geckolib3.resource.GeckoLibCache;

public class LaserTurretTileModel extends TurretTileModel {

    public static final String MODEL_LOCATION = "geo/block/turret_horizontal.geo.json";
    public static final String TEXTURE_LOCATION = "textures/block/turret_horizontal.png";
    public static final String ANIMATION_LOCATION = "animations/block/turret.animation.json";

    public LaserTurretTileModel() {
        super(MODEL_LOCATION, TEXTURE_LOCATION, ANIMATION_LOCATION);
    }

    @Override
    public void setLivingAnimations(TurretTileEntity entity, Integer uniqueID) {
        GeckoLibCache.getInstance().parser.setValue("beam_length", ((LaserTurretTileEntity) entity).getBeamLength());
        super.setLivingAnimations(entity, uniqueID);
    }
}