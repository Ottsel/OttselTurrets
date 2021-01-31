package net.nickhunter.mc.ottselturrets.client.models.tile;

public class LaserTurretTileModel extends TurretTileModel {

    public static final String MODEL_LOCATION = "geo/block/turret_horizontal.geo.json";
    public static final String TEXTURE_LOCATION = "textures/blocks/turret_horizontal.png";
    public static final String ANIMATION_LOCATION = "animations/block/turret.animation.json";

    public static final String YAW_BONE = "head";
    public static final String PITCH_BONE = "head";

    public LaserTurretTileModel() {
        super(MODEL_LOCATION, TEXTURE_LOCATION, ANIMATION_LOCATION, YAW_BONE, PITCH_BONE);
    }
}