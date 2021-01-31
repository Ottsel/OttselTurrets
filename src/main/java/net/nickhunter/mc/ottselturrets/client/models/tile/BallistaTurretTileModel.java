package net.nickhunter.mc.ottselturrets.client.models.tile;

public class BallistaTurretTileModel extends TurretTileModel {

    public static final String MODEL_LOCATION = "geo/block/turret_ballista.geo.json";
    public static final String TEXTURE_LOCATION = "textures/blocks/turret_ballista.png";
    public static final String ANIMATION_LOCATION = "animations/block/ballista_turret.animation.json";
    
    public BallistaTurretTileModel() {
        super(MODEL_LOCATION, TEXTURE_LOCATION, ANIMATION_LOCATION);
    }
}