package net.nickhunter.mc.ottselturrets.blocks.tile;

import net.nickhunter.mc.ottselturrets.registry.TileRegistry;

public class BallistaTurretTileEntity extends TurretTileEntity {

    public static final String idleAnimation = "animation.ballista_turret.scan";
    public static final String aimingAnimation = "animation.ballista_turret.rotate_head";
    public static final String firingAnimation = "animation.ballista_turret.fire";
    public static final String resetAnimation = "animation.ballista_turret.reset_rotation";

    public BallistaTurretTileEntity() {
        super(TileRegistry.BALLISTA_TURRET.get(), idleAnimation, aimingAnimation, firingAnimation, resetAnimation);
    }
}