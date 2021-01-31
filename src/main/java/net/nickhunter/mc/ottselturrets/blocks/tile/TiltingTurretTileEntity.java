package net.nickhunter.mc.ottselturrets.blocks.tile;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.Vec3d;
import net.nickhunter.mc.ottselturrets.util.TiltDirection;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;

@SuppressWarnings("rawtypes")
public class TiltingTurretTileEntity extends TurretTileEntity {

    public TiltingTurretTileEntity(TileEntityType<?> tileEntityType, String idleAnimation, String aimingAnimation,
            String firingAnimation, String resetAnimation) {
        super(tileEntityType, idleAnimation, aimingAnimation, firingAnimation, resetAnimation);
    }

    public float beamLength;

    public TiltDirection localDirectionToTarget, tiltDirection;
    public boolean tilt;

    float tiltPitchAmount;

    public static final String TILT_NORTH = "animation.turret_horizontal.tilt_north";
    public static final String TILTED_NORTH = "animation.turret_horizontal.tilted_north";
    public static final String TILT_EAST = "animation.turret_horizontal.tilt_east";
    public static final String TILTED_EAST = "animation.turret_horizontal.tilted_east";
    public static final String TILT_SOUTH = "animation.turret_horizontal.tilt_south";
    public static final String TILTED_SOUTH = "animation.turret_horizontal.tilted_south";
    public static final String TILT_WEST = "animation.turret_horizontal.tilt_west";
    public static final String TILTED_WEST = "animation.turret_horizontal.tilted_west";

    public static final String TILT_NORTHEAST = "animation.turret_horizontal.tilt_north_east";
    public static final String TILTED_NORTHEAST = "animation.turret_horizontal.tilted_north_east";
    public static final String TILT_SOUTHEAST = "animation.turret_horizontal.tilt_south_east";
    public static final String TILTED_SOUTHEAST = "animation.turret_horizontal.tilted_south_east";
    public static final String TILT_SOUTHWEST = "animation.turret_horizontal.tilt_south_west";
    public static final String TILTED_SOUTHWEST = "animation.turret_horizontal.tilted_south_west";
    public static final String TILT_NORTHWEST = "animation.turret_horizontal.tilt_north_west";
    public static final String TILTED_NORTHWEST = "animation.turret_horizontal.tilted_north_west";

    public static final String RESET_LEGS = "animation.turret_horizontal.reset_legs";

    @Override
    protected void clientTrackTarget(Vec3d target) {
        super.clientTrackTarget(target);

        if (aimingPaused)
            return;

        localDirectionToTarget = getTargetLocalDirection();
        tilt = false;

        // Target is above.
        if (pitchToTarget < -headPitchMax) {
            tiltDirection = localDirectionToTarget.getOpposite();
            switch (tiltDirection) {
                default:
                case NORTH:
                case EAST:
                case SOUTH:
                case WEST:
                    pitchToTarget += tiltPitchAmount;
                    break;
                case NORTHEAST:
                case SOUTHEAST:
                case SOUTHWEST:
                case NORTHWEST:
                    pitchToTarget += (tiltPitchAmount + 15);
                    break;
            }
            tilt = true;
        }

        // Target is below.
        else if (pitchToTarget > headPitchMax) {
            tiltDirection = localDirectionToTarget;
            switch (tiltDirection) {
                default:
                case NORTH:
                case EAST:
                case SOUTH:
                case WEST:
                    pitchToTarget -= tiltPitchAmount;
                    break;
                case NORTHEAST:
                case SOUTHEAST:
                case SOUTHWEST:
                case NORTHWEST:
                    pitchToTarget -= (tiltPitchAmount + 15);
                    break;
            }
            tilt = true;
        }
    }

    @Override
    protected void animateLegs(AnimationController controller) {

        AnimationBuilder animationBuilder = new AnimationBuilder();

        boolean animate = false;
        if (tilt) {

            animationBuilder.addAnimation(RESET_LEGS, false);
            switch (tiltDirection) {
                case NORTH:
                default:
                    if (animate)
                        animationBuilder.addAnimation(TILT_NORTH, false);
                    animationBuilder.addAnimation(TILTED_NORTH, true);
                    break;
                case EAST:
                    if (animate)
                        animationBuilder.addAnimation(TILT_EAST, false);
                    animationBuilder.addAnimation(TILTED_EAST, true);
                    break;
                case WEST:
                    if (animate)
                        animationBuilder.addAnimation(TILT_WEST, false);
                    animationBuilder.addAnimation(TILTED_WEST, true);
                    break;
                case SOUTH:
                    if (animate)
                        animationBuilder.addAnimation(TILT_SOUTH, false);
                    animationBuilder.addAnimation(TILTED_SOUTH, true);
                    break;
                case NORTHEAST:
                    if (animate)
                        animationBuilder.addAnimation(TILT_NORTHEAST, false);
                    animationBuilder.addAnimation(TILTED_NORTHEAST, true);
                    break;
                case SOUTHEAST:
                    if (animate)
                        animationBuilder.addAnimation(TILT_SOUTHEAST, false);
                    animationBuilder.addAnimation(TILTED_SOUTHEAST, true);
                    break;
                case SOUTHWEST:
                    if (animate)
                        animationBuilder.addAnimation(TILT_SOUTHWEST, false);
                    animationBuilder.addAnimation(TILTED_SOUTHWEST, true);
                    break;
                case NORTHWEST:
                    if (animate)
                        animationBuilder.addAnimation(TILT_NORTHWEST, false);
                    animationBuilder.addAnimation(TILTED_NORTHWEST, true);
                    break;
            }
        } else {
            if (!controller.getAnimationState().equals(AnimationState.Stopped)) {
                animationBuilder.addAnimation(RESET_LEGS, false);
            }
        }
        controller.setAnimation(animationBuilder);
    }

    @Override
    protected void noTargets() {
        tilt = false;
    }
}