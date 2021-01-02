package net.nickhunter.mc.ottselturrets.blocks.tile;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.TurretType;
import net.nickhunter.mc.ottselturrets.network.packets.PacketTurretUpdate;
import net.nickhunter.mc.ottselturrets.registry.SoundRegistry;
import net.nickhunter.mc.ottselturrets.registry.TileRegistry;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.CustomInstructionKeyframeEvent;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;
import static net.nickhunter.mc.ottselturrets.TurretType.getTurretTypeFromInt;

@SuppressWarnings("rawtypes")
public class TurretTileEntity extends TileEntity implements ITickableTileEntity, IAnimatable {

    public TurretType turretType;
    public static final int range = 10;
    public static final int damage = 20;
    public static final double timeToCharge = 1.5;
    public static final double timeToCoolDown = 2.0;
    public int chargeResetTime = OttselTurrets.TICKS_PER_SECOND * 2;

    public static final float pitchMax = 45;
    public static final float headPitchMax = 15;
    public static final float tiltPitchAmount = 30;

    public float beamLength;
    public float yawToTarget;
    public float pitchToTarget;
    public Direction localDirectionToTarget, tiltDirection;
    public boolean tilt;
    public TurretState turretState = TurretState.SCANNING;
    public TurretState lastTurretState = TurretState.SCANNING;

    private boolean chargeSoundHasPlayed;
    public boolean lookingAtTarget;
    public boolean aimingPaused;

    public int chargeTime = -1;
    public int coolDownTime = -1;

    private final AnimationFactory factory = new AnimationFactory(this);

    public static enum TurretState {
        SCANNING, AIMING, FIRING
    }

    public static class TurretAnimations {
        public static final String SCANNING = "animation.turret_horizontal.scan";
        public static final String AIMING = "animation.turret_horizontal.rotate_head";
        public static final String FIRING = "animation.turret_horizontal.fire_beam";
        public static final String RESET_ROTATION = "animation.turret_horizontal.reset_rotation";
        public static final String TILT_NORTH = "animation.turret_horizontal.tilt_north";
        public static final String TILTED_NORTH = "animation.turret_horizontal.tilted_north";
        public static final String TILT_EAST = "animation.turret_horizontal.tilt_east";
        public static final String TILTED_EAST = "animation.turret_horizontal.tilted_east";
        public static final String TILT_SOUTH = "animation.turret_horizontal.tilt_south";
        public static final String TILTED_SOUTH = "animation.turret_horizontal.tilted_south";
        public static final String TILT_WEST = "animation.turret_horizontal.tilt_west";
        public static final String TILTED_WEST = "animation.turret_horizontal.tilted_west";

    }

    public TurretTileEntity() {
        this(TileRegistry.TURRET.get());
    }

    public TurretTileEntity(TurretType turretType) {
        this(TileRegistry.TURRET.get());
        this.turretType = turretType;
        this.markDirty();
    }

    public TurretTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn == null ? TileRegistry.TURRET.get() : tileEntityTypeIn);
    }

    @Override
    public void tick() {
        if (world == null)
            return;
        // Get a list of nearby potential targets.
        List<LivingEntity> targets = getTargets();
        if (!targets.isEmpty()) { // If there are potential targets...
            LivingEntity target = getClosestTarget(targets);
            if (world.isRemote) { // On client...
                // Call clientTick.
                clientTrackTarget(target.getPositionVec());
            } else { // On server...
                // Return if still cooling down.
                if (coolDownTime != -1) {
                    coolDown();

                    // Reset the turret's rotation.
                    updateClient(TurretState.SCANNING);
                    return;
                }
                // Tell the client to aim at the target, then charge up the turret.
                updateClient(TurretState.AIMING);
                chargeUp(target);
            }
        } else { // If there are no potential targets...
            if (!world.isRemote) { // On server...
                // Cool down if applicable.
                if (coolDownTime != -1) {
                    coolDown();
                }
                // Count down the charge reset timer if the turret has started to charge up...
                if (chargeTime != -1) {
                    resetTimer();
                } else { // If the charge expires...
                    // Reset the turret's rotation.
                    yawToTarget = 0;
                    pitchToTarget = 0;
                    updateClient(TurretState.SCANNING);
                }
            }
        }
    }

    /*
     * Acts as a timer to determine if the turret is charged up and can fire.
     */
    private void chargeUp(LivingEntity target) {
        if (chargeTime < timeToCharge * OttselTurrets.TICKS_PER_SECOND - 1) { // If the turret is still charging up...
            chargeTime++;

            // Return if the charge sound has already been played. TODO Sound event
            if (chargeSoundHasPlayed)
                return;

            // Play the charge sound
            playSoundEffect(SoundRegistry.LASER_CHARGE.getSound());
            chargeSoundHasPlayed = true;
        } else { // If the turret is done charging up...

            // Fire the turret and start the cooldown.
            fireTurret(target);
            coolDown();

            // Reset the charge time and charge sound flag.
            chargeTime = -1;
            chargeSoundHasPlayed = false;

        }
    }

    /*
     * Facilitates firing the turret.
     */
    private void fireTurret(LivingEntity target) {

        // Tell the client to play the shooting animation.
        updateClient(TurretState.FIRING);

        // Play shooting sound. TODO Sound event
        playSoundEffect(SoundRegistry.LASER_BOLT.getSound());

        // Damage the target. TODO customize this
        target.attackEntityFrom(new DamageSource(DamageSource.MAGIC.damageType), damage);
    }

    /*
     * Acts as a cool down timer for the turret
     */
    private void coolDown() {
        if (coolDownTime < timeToCoolDown * OttselTurrets.TICKS_PER_SECOND - 1) { // If the turret is cooling down...
            coolDownTime++;
        } else { // If the turret is done cooling down...
            coolDownTime = -1;
        }
    }

    /*
     * Acts as a timer for expiring the turret's charge.
     */
    private void resetTimer() {
        if (chargeResetTime == 0) { // If the turret's reset timer is done...

            // Reset the charge.
            chargeTime = -1;
            chargeSoundHasPlayed = false;
            chargeResetTime = OttselTurrets.TICKS_PER_SECOND * 2;
        } else { // If the turret's reset timer is not done...
            chargeResetTime--;
        }
    }

    public void playSoundEffect(SoundEvent soundEvent) {
        if (world == null)
            return;
        PlayerEntity player = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ());
        if (player == null)
            return;
        Vec3i playerPos = new Vec3i(player.getPosition().getX(), player.getPosition().getY(),
                player.getPosition().getZ());
        world.playSound(player, this.pos, soundEvent, SoundCategory.BLOCKS,
                (float) (1 / Math.sqrt((this.pos.distanceSq(playerPos)))), 1);
    }

    private List<LivingEntity> getTargets() {

        double x1 = this.pos.getX() - range;
        double y1 = this.pos.getY() - range;
        double z1 = this.pos.getZ() - range;
        AxisAlignedBB area = new AxisAlignedBB(x1, y1, z1, x1 + range * 2, y1 + range * 2, z1 + range * 2);

        // Find entities around this tile entity.
        List<LivingEntity> entities = Collections.emptyList();
        if (world != null) {
            entities = world.getEntitiesWithinAABB(MobEntity.class, area);
        }
        return entities;
    }

    private LivingEntity getClosestTarget(List<LivingEntity> targets) {

        LivingEntity target = targets.get(0);
        double shortestDist = 42069;

        for (LivingEntity entity : targets) {
            Vec3d entityPos = entity.getPositionVec();
            double dist = entityPos.distanceTo(new Vec3d(this.pos.getX(), this.pos.getY(), this.pos.getZ()));
            if (dist < shortestDist) {
                shortestDist = dist;
                target = entity;
            }
        }
        return target;
    }

    private void updateClient(TurretState turretState) {
        if (this.turretState == turretState)
            return;
        this.turretState = turretState;
        if (world != null)
            OttselTurrets.getNetworkChannel().sendToTrackingChunk(new PacketTurretUpdate(turretState, pos),
                    world.getChunkAt(pos));
    }

    @Override
    @Nonnull
    public CompoundNBT write(CompoundNBT nbt) {
        nbt.putInt("TurretType", turretType.ordinal());
        return super.write(nbt);
    }

    @Override
    public void read(CompoundNBT nbt) {
        turretType = getTurretTypeFromInt(nbt.getInt("TurretType"));
        super.read(nbt);
    }

    @Override
    @Nonnull
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    /*
     * Client
     */

    /*
     * Tracks the target position every tick.
     */
    private void clientTrackTarget(Vec3d target) {

        setBeamLength(target);

        if (aimingPaused)
            return;

        Vec3d diffPos = new Vec3d(this.pos.getX() + .5f, this.pos.getY(), this.pos.getZ() + .5f).subtract(target);
        yawToTarget = getYaw(diffPos);
        localDirectionToTarget = getTargetLocalDirection();
        pitchToTarget = getPitch(diffPos);
        tilt = false;

        // Target is above.
        if (pitchToTarget < -headPitchMax) {
            pitchToTarget += tiltPitchAmount;
            tiltDirection = localDirectionToTarget.getOpposite();
            tilt = true;
        }

        // Target is below.
        else if (pitchToTarget > headPitchMax) {
            pitchToTarget -= tiltPitchAmount;
            tiltDirection = localDirectionToTarget;
            tilt = true;
        }
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

    private float getYaw(Vec3d diffPos) {
        return (float) MathHelper.wrapDegrees(
                MathHelper.atan2(-diffPos.z, -diffPos.x) * (double) (180F / (float) Math.PI) + getYawOffset());
    }

    private float getPitch(Vec3d diffPos) {
        double horizComponent = MathHelper.sqrt((diffPos.z * diffPos.z) + (diffPos.x * diffPos.x));
        float pitch = (float) MathHelper
                .wrapDegrees((MathHelper.atan2(-horizComponent, diffPos.y) * (double) (180F / (float) Math.PI) + 90));
        if (pitch > pitchMax)
            pitch = pitchMax;
        else if (pitch < -pitchMax)
            pitch = -pitchMax;
        return pitch;
    }

    // Returns the rough cardinal direaction of the target (RELATIVE TO THE TURRET).
    // Might add intercardinal directions later.
    Direction getTargetLocalDirection() {
        // North "Quadrant"
        if (yawToTarget >= -45 && yawToTarget < 45) {
            return Direction.NORTH;
        }
        // East "Quadrant"
        else if (yawToTarget >= 45 && yawToTarget < 135) {
            return Direction.EAST;
        }
        // West "Quadrant"
        else if (yawToTarget < -45 && yawToTarget >= -135) {
            return Direction.WEST;
        }
        // South "Quadrant"
        else {
            return Direction.SOUTH;
        }
    }

    public int getYawOffset() {
        switch (this.getBlockState().get(HORIZONTAL_FACING)) {
            case NORTH:
            default:
                return 90;
            case SOUTH:
                return -90;
            case EAST:
                return 0;
            case WEST:
                return 180;
        }
    }

    private <E extends TileEntity & IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        AnimationController controller = event.getController();
        AnimationBuilder animationBuilder = new AnimationBuilder();

        switch (turretState) {
            case SCANNING:
                switch (lastTurretState) {
                    case SCANNING:
                        animationBuilder.addAnimation(TurretAnimations.RESET_ROTATION, false);
                        animationBuilder.addAnimation(TurretAnimations.SCANNING, true);
                        lastTurretState = TurretState.SCANNING;
                        break;
                    case AIMING:
                        animationBuilder.addAnimation(TurretAnimations.SCANNING, true);
                        lastTurretState = TurretState.SCANNING;
                        break;
                    case FIRING:
                        if (controller.getAnimationState().equals(AnimationState.Stopped)) {
                            animationBuilder.addAnimation(TurretAnimations.RESET_ROTATION, false);
                            animationBuilder.addAnimation(TurretAnimations.SCANNING, true);
                            lastTurretState = TurretState.SCANNING;
                        } else {
                            animationBuilder.addAnimation(TurretAnimations.FIRING);
                            lastTurretState = TurretState.FIRING;
                        }
                        break;
                }
                break;
            case AIMING:
                switch (lastTurretState) {
                    case SCANNING:
                        aimAndTilt(animationBuilder);
                        lastTurretState = TurretState.AIMING;
                        break;
                    case AIMING:
                        aimAndTilt(animationBuilder);
                        lastTurretState = TurretState.AIMING;
                        break;
                    case FIRING:
                        if (controller.getAnimationState().equals(AnimationState.Stopped)) {
                            aimAndTilt(animationBuilder);
                            lastTurretState = TurretState.AIMING;
                            //aimingPaused = true;
                        } else {
                            animationBuilder.addAnimation(TurretAnimations.FIRING);
                            lastTurretState = TurretState.FIRING;
                        }
                        break;
                }
                break;
            case FIRING:
                switch (lastTurretState) {
                    case SCANNING:
                        OttselTurrets.LOGGER.error("Something is horribly wrong: Went from scanning to shooting.");
                        lastTurretState = TurretState.SCANNING;
                        break;
                    case AIMING:
                        aimingPaused = true;
                        animationBuilder.addAnimation(TurretAnimations.FIRING, false);
                        lastTurretState = TurretState.FIRING;
                        break;
                    case FIRING:
                        animationBuilder.addAnimation(TurretAnimations.FIRING, false);
                        lastTurretState = TurretState.FIRING;
                        break;
                }
                break;
        }

        controller.setAnimation(animationBuilder);
        return PlayState.CONTINUE;

    }

    private void aimAndTilt(AnimationBuilder animationBuilder) {
        if (tilt) {
            switch (tiltDirection) {
                case NORTH:
                default:
                    animationBuilder.addAnimation(TurretAnimations.TILT_NORTH, false);
                    animationBuilder.addAnimation(TurretAnimations.TILTED_NORTH, true);
                    break;
                case EAST:
                    animationBuilder.addAnimation(TurretAnimations.TILT_EAST, false);
                    animationBuilder.addAnimation(TurretAnimations.TILTED_EAST, true);
                    break;
                case WEST:
                    animationBuilder.addAnimation(TurretAnimations.TILT_WEST, false);
                    animationBuilder.addAnimation(TurretAnimations.TILTED_WEST, true);
                    break;
                case SOUTH:
                    animationBuilder.addAnimation(TurretAnimations.TILT_SOUTH, false);
                    animationBuilder.addAnimation(TurretAnimations.TILTED_SOUTH, true);
                    break;
            }
        } else {
            animationBuilder.addAnimation(TurretAnimations.AIMING, true);
        }
    }

    private <ENTITY extends IAnimatable> void instructionListener(CustomInstructionKeyframeEvent<ENTITY> event) {
        for (String instruction : event.instructions) {
            if (instruction.equals("looking_at_target")) {
                OttselTurrets.LOGGER.debug("looking_at_target");
                lookingAtTarget = true;
            }
            if (instruction.equals("reset_rotation")) {
                OttselTurrets.LOGGER.debug("reset_rotation");
                pitchToTarget = 0;
                yawToTarget = 0;
                lookingAtTarget = true;
                aimingPaused = false;
            }
        }
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        AnimationController controller = new AnimationController(this, "controller", 0, this::predicate);
        controller.registerCustomInstructionListener(this::instructionListener);
        animationData.addAnimationController(controller);
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Override
    public void handleUpdateTag(CompoundNBT tag) {
        turretType = getTurretTypeFromInt(tag.getInt("TurretType"));
    }
}