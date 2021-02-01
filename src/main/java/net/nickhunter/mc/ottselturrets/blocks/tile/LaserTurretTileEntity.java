package net.nickhunter.mc.ottselturrets.blocks.tile;

import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.nickhunter.mc.ottselturrets.registry.TileRegistry;

public class LaserTurretTileEntity extends TiltingTurretTileEntity {

    public static final String IDLE_ANIMATION = "animation.turret_horizontal.scan";
    public static final String AIMING_ANIMATION = "animation.turret_horizontal.rotate_head";
    public static final String FIRING_ANIMATION = "animation.turret_horizontal.fire_beam";
    public static final String RESET_ANIMATION = "animation.turret_horizontal.reset_rotation";

    public static final int RANGE = 10;
    public static final int DAMAGE = 20;
    public static final double CHARGE_TIME = 1.5;
    public static final double COOLDOWN_TIME = 2;
    public static final float PITCH_MAX = 45;
    public static final float HEAD_PITCH_MAX = 15;

    public LaserTurretTileEntity() {
        super(TileRegistry.LASER_TURRET.get(), IDLE_ANIMATION, AIMING_ANIMATION, FIRING_ANIMATION, RESET_ANIMATION,
                RANGE, DAMAGE, CHARGE_TIME, COOLDOWN_TIME, PITCH_MAX, HEAD_PITCH_MAX);
    }

    @Override
    protected void clientTrackTarget(Vec3d target) {
        setBeamLength(target);
        super.clientTrackTarget(target);
    }

    private void setBeamLength(Vec3d targetPos) {
        if (world == null)
            return;
        Vec3d posVec = new Vec3d(this.pos.getX()+.5f, this.pos.getY()+1f, this.pos.getZ()+.5f);
        Vec3d posDiff = targetPos.add(targetOffset).subtract(posVec);
        RayTraceResult result = world.rayTraceBlocks(new RayTraceContext(posVec, posDiff.scale(256),
                RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, null));
        if (result.getType() == RayTraceResult.Type.MISS) {
            beamLength = 256;
        } else if (result.getType() == RayTraceResult.Type.BLOCK) {
            beamLength = (float) posVec.distanceTo(result.getHitVec()) - .9f;
        }
    }
}