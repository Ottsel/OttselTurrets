package net.nickhunter.mc.ottselturrets.blocks.tile;

import net.nickhunter.mc.ottselturrets.registry.TileRegistry;

public class BallistaTurretTileEntity extends TurretTileEntity {

    public static final String IDLE_ANIMATION = "animation.ballista_turret.scan";
    public static final String AIMING_ANIMATION = "animation.ballista_turret.rotate_head";
    public static final String FIRING_ANIMATION = "animation.ballista_turret.fire";
    public static final String RESET_ANIMATION = "animation.ballista_turret.reset_rotation";

    public static final int RANGE = 10;
    public static final int DAMAGE = 10;
    public static final double CHARGE_TIME = 0.5;
    public static final double COOLDOWN_TIME = 2.7;

    public static final float PITCH_MAX = 45;
    public static final float HEAD_PITCH_MAX = 15;

    public BallistaTurretTileEntity() {
        super(TileRegistry.BALLISTA_TURRET.get(), IDLE_ANIMATION, AIMING_ANIMATION, FIRING_ANIMATION, RESET_ANIMATION,
        RANGE, DAMAGE, CHARGE_TIME, COOLDOWN_TIME, PITCH_MAX, HEAD_PITCH_MAX);
    }
}