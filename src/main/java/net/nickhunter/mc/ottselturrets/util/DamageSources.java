package net.nickhunter.mc.ottselturrets.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;

public class DamageSources extends DamageSource {
    public static final DamageSource LASER_TURRET = (new DamageSource("laserTurret").setIsFire().bypassArmor());
    public static final DamageSource BALLISTA_TURRET = (new DamageSource("ballistaTurret").setProjectile());

    public DamageSources(String damageTypeIn) {
        super(damageTypeIn);
    }
    public static final DamageSource causeArmCannonDamage(Entity entity){
        return new EntityDamageSource("armCannon", entity).setProjectile();
    }
    
}
