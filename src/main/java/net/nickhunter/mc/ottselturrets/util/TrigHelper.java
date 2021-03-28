package net.nickhunter.mc.ottselturrets.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class TrigHelper {

    public static float calculateYaw(BlockPos origin, Vector3d target) {
        return calculateYaw(new Vector3d(origin.getX() + .5f, origin.getY(), origin.getZ() + .5f), target);
    }

    public static final float calculateYaw(Vector3d origin, Vector3d target) {
        Vector3d diffPos = target.subtract(origin);
        return (float) MathHelper.wrapDegrees((radToDeg(MathHelper.atan2(diffPos.z, diffPos.x))));
    }

    public static final float calculatePitch(BlockPos origin, Vector3d target) {
        return calculatePitch(new Vector3d(origin.getX() + .5f, origin.getY(), origin.getZ() + .5f), target);
    }

    public static final float calculatePitch(Vector3d origin, Vector3d target) {
        Vector3d diffPos = target.subtract(origin);
        double horizComponent = Math.hypot(diffPos.x, diffPos.z);
        return (float) MathHelper.wrapDegrees(-(radToDeg(MathHelper.atan2(horizComponent, diffPos.y))));
    }

    public static final double degToRad(double degrees) {
        return degrees * Math.PI / 180D;
    }

    public static final double radToDeg(double radians) {
        return radians * 180D / Math.PI;
    }
}
