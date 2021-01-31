package net.nickhunter.mc.ottselturrets.blocks.tile;

import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.nickhunter.mc.ottselturrets.registry.TileRegistry;

public class LaserTurretTileEntity extends TiltingTurretTileEntity {

    public static final String idleAnimation = "animation.turret_horizontal.scan";
    public static final String aimingAnimation = "animation.turret_horizontal.rotate_head";
    public static final String firingAnimation = "animation.turret_horizontal.fire_beam";
    public static final String resetAnimation = "animation.turret_horizontal.reset_rotation";

    public LaserTurretTileEntity() {
        super(TileRegistry.LASER_TURRET.get(), idleAnimation, aimingAnimation, firingAnimation, resetAnimation);
    }

    @Override
    protected void clientTrackTarget(Vec3d target) {
        setBeamLength(target);
        super.clientTrackTarget(target);
    }

    private void setBeamLength(Vec3d targetPos) {
        if (world == null)
            return;
        Vec3d posVec = new Vec3d(this.pos.getX() + .5, this.pos.getY() + 1, this.pos.getZ() + .5);
        Vec3d posOffset = posVec.subtract(targetPos);
        RayTraceResult result = world.rayTraceBlocks(new RayTraceContext(posVec.add(posOffset), targetPos,
                RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, null));
        if (result.getType() == RayTraceResult.Type.MISS) {
            beamLength = 256;
        } else {
            beamLength = (float) posVec.distanceTo(result.getHitVec());
        }
    }
}