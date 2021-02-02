package net.nickhunter.mc.ottselturrets.util;

import net.minecraft.util.DamageSource;

public class TurretDamageSource extends DamageSource {
    public static final DamageSource LASER_TURRET = (new DamageSource("laserTurret").setFireDamage().setDamageBypassesArmor());
    public static final DamageSource BALLISTA_TURRET = (new DamageSource("ballistaTurret").setProjectile());

    public TurretDamageSource(String damageTypeIn) {
        super(damageTypeIn);
    }
    
}
