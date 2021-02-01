package net.nickhunter.mc.ottselturrets.blocks.tile;

import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.network.packets.PacketTurretUpdate;
import net.nickhunter.mc.ottselturrets.registry.SoundRegistry;
import net.nickhunter.mc.ottselturrets.util.TiltDirection;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.CustomInstructionKeyframeEvent;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

@SuppressWarnings("rawtypes")
public class TurretTileEntity extends TileEntity implements ITickableTileEntity, IAnimatable {

    protected static final Vec3d targetOffset = new Vec3d(0, 1f, 0);

    public final String idleAnimation;
    public final String aimingAnimation;
    public final String firingAnimation;
    public final String resetAnimation;

    public final int range;
    public final int damage;
    public final double timeToCharge;
    public final double timeToCoolDown;
    public final float pitchMax;
    public final float headPitchMax;

    public float yawToTarget;
    public float pitchToTarget;

    public TurretState turretState = TurretState.SCANNING;
    public TurretState lastTurretState = TurretState.SCANNING;

    private boolean chargeSoundHasPlayed;
    public boolean lookingAtTarget;

    public int chargeTimer = -1;
    public int chargeResetTimer = OttselTurrets.TICKS_PER_SECOND * 2;
    public int coolDownTimer = -1;

    public float headRotationYPrev;
    public float headRotationXPrev;

    private final AnimationFactory factory = new AnimationFactory(this);

    public enum TurretState {
        SCANNING, AIMING, FIRING
    }

    public TurretTileEntity(TileEntityType<?> tileEntityTypeIn, String idleAnimation, String aimingAnimation,
            String firingAnimation, String resetAnimation, int range, int damage, double timeToCharge,
            double timeToCoolDown, float pitchMax, float headPitchMax) {
        super(tileEntityTypeIn);
        this.idleAnimation = idleAnimation;
        this.aimingAnimation = aimingAnimation;
        this.firingAnimation = firingAnimation;
        this.resetAnimation = resetAnimation;

        this.range = range;
        this.damage = damage;
        this.timeToCharge = timeToCharge;
        this.timeToCoolDown = timeToCoolDown;
        this.pitchMax = pitchMax;
        this.headPitchMax = headPitchMax;

        this.markDirty();
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
                if (coolDownTimer != -1) {
                    coolDown();

                    // Reset the turret's rotation.
                    updateClient(TurretState.AIMING);
                    return;
                }
                // Tell the client to aim at the target, then charge up the turret.
                updateClient(TurretState.AIMING);
                chargeUp(target);
            }
        } else { // If there are no potential targets...
            if (!world.isRemote) { // On server...
                // Cool down if applicable.
                if (coolDownTimer != -1) {
                    coolDown();
                }
                // Count down the charge reset timer if the turret has started to charge up...
                if (chargeTimer != -1) {
                    resetTimer();
                } else { // If the charge expires...
                    // Reset the turret's rotation.
                    yawToTarget = 0;
                    pitchToTarget = 0;
                    updateClient(TurretState.SCANNING);
                }
            } else {
                noTargets();
            }
        }
    }

    protected void noTargets() {

    }

    /*
     * Acts as a timer to determine if the turret is charged up and can fire.
     */
    private void chargeUp(LivingEntity target) {
        if (chargeTimer < timeToCharge * OttselTurrets.TICKS_PER_SECOND - 1) { // If the turret is still charging up...
            chargeTimer++;

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
            chargeTimer = -1;
            chargeSoundHasPlayed = false;

        }
    }

    // Facilitates firing the turret.
    private void fireTurret(LivingEntity target) {

        // TODO Abstract this.

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
        if (coolDownTimer < timeToCoolDown * OttselTurrets.TICKS_PER_SECOND - 1) { // If the turret is cooling down...
            coolDownTimer++;
        } else { // If the turret is done cooling down...
            coolDownTimer = -1;
        }
    }

    /*
     * Acts as a timer for expiring the turret's charge.
     */
    private void resetTimer() {
        if (chargeResetTimer == 0) { // If the turret's reset timer is done...

            // Reset the charge.
            chargeTimer = -1;
            chargeSoundHasPlayed = false;
            chargeResetTimer = OttselTurrets.TICKS_PER_SECOND * 2;
        } else { // If the turret's reset timer is not done...
            chargeResetTimer--;
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

        entities = world.getEntitiesWithinAABB(MobEntity.class, area);
        List<LivingEntity> validTargets = new ArrayList<LivingEntity>(entities);
        entities.forEach((entity) -> {

            RayTraceResult result = rayTraceToTarget(entity.getPositionVec());
            float pitch = getPitch(entity.getPositionVec());

            if (result.getType() == RayTraceResult.Type.BLOCK) {
                validTargets.remove(entity);
            } else if (pitch > pitchMax) {
                validTargets.remove(entity);
            } else if (pitch < -pitchMax) {
                validTargets.remove(entity);
            }
        });
        return validTargets;
    }

    protected RayTraceResult rayTraceToTarget(Vec3d target) {
        Vec3d posVec = new Vec3d(this.pos.getX() + .5f, this.pos.getY() + 1f, this.pos.getZ() + .5f);
        return world.rayTraceBlocks(new RayTraceContext(posVec, target.add(targetOffset),
                RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, null));
    }

    private LivingEntity getClosestTarget(List<LivingEntity> targets) {

        LivingEntity target = targets.get(0);
        double shortestDist = range;

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

    /*
     * Client
     */

    // Tracks the target position every tick.
    protected void clientTrackTarget(Vec3d target) {
        yawToTarget = getYaw(target);
        pitchToTarget = getPitch(target);
    }

    private float getYaw(Vec3d target) {
        Vec3d diffPos = new Vec3d(this.pos.getX() + .5f, this.pos.getY(), this.pos.getZ() + .5f).subtract(target);
        return (float) MathHelper.wrapDegrees(
                MathHelper.atan2(-diffPos.z, -diffPos.x) * (double) (180F / (float) Math.PI) + getYawOffset());
    }

    private float getPitch(Vec3d target) {
        Vec3d diffPos = new Vec3d(this.pos.getX() + .5f, this.pos.getY(), this.pos.getZ() + .5f).subtract(target);
        double horizComponent = MathHelper.sqrt((diffPos.z * diffPos.z) + (diffPos.x * diffPos.x));
        float pitch = (float) MathHelper
                .wrapDegrees((MathHelper.atan2(-horizComponent, diffPos.y) * (double) (180F / (float) Math.PI) + 90));
        return pitch;
    }

    // Returns the rough cardinal direction of the target (RELATIVE TO THE TURRET).
    // Might add intercardinal directions later.
    TiltDirection getTargetLocalDirection() {
        // North "Quadrant"
        if (yawToTarget >= -30 && yawToTarget < 30) {
            return TiltDirection.NORTH;
        }
        // Northeast "Quadrant"
        if (yawToTarget >= 30 && yawToTarget < 60) {
            return TiltDirection.NORTHEAST;
        }
        // East "Quadrant"
        else if (yawToTarget >= 60 && yawToTarget < 120) {
            return TiltDirection.EAST;
        }
        // Southeast "Quadrant"
        else if (yawToTarget >= 120 && yawToTarget < 150) {
            return TiltDirection.SOUTHEAST;
        }
        // Northwest "Quadrant"
        else if (yawToTarget < -30 && yawToTarget >= -60) {
            return TiltDirection.NORTHWEST;
        }
        // West "Quadrant"
        else if (yawToTarget < -60 && yawToTarget >= -120) {
            return TiltDirection.WEST;
        }
        // Southest "Quadrant"
        else if (yawToTarget < -120 && yawToTarget >= -150) {
            return TiltDirection.SOUTHWEST;
        }
        // South "Quadrant"
        else {
            return TiltDirection.SOUTH;
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
        switch (controller.getName()) {
            case "leg_controller":
                animateLegs(controller);
                return PlayState.CONTINUE;
            case "head_controller":
                animateHead(controller);
                return PlayState.CONTINUE;
            default:
                return PlayState.CONTINUE;
        }
    }

    protected void animateLegs(AnimationController controller) {

    }

    protected void animateHead(AnimationController controller) {
        AnimationBuilder animationBuilder = new AnimationBuilder();
        switch (turretState) {
            case SCANNING:
                switch (lastTurretState) {
                    case SCANNING:
                        animationBuilder.addAnimation(resetAnimation, false);
                        animationBuilder.addAnimation(idleAnimation, true);
                        lastTurretState = TurretState.SCANNING;
                        break;
                    case AIMING:
                        animationBuilder.addAnimation(idleAnimation, true);
                        lastTurretState = TurretState.SCANNING;
                        break;
                    case FIRING:
                        if (controller.getAnimationState().equals(AnimationState.Stopped)) {
                            animationBuilder.addAnimation(resetAnimation, false);
                            animationBuilder.addAnimation(idleAnimation, true);
                            lastTurretState = TurretState.SCANNING;
                        } else {
                            animationBuilder.addAnimation(firingAnimation, false);
                            lastTurretState = TurretState.FIRING;
                        }
                        break;
                }
                break;
            case AIMING:
                switch (lastTurretState) {
                    case SCANNING:
                        animationBuilder.addAnimation(aimingAnimation, true);
                        lastTurretState = TurretState.AIMING;
                        break;
                    case AIMING:
                        animationBuilder.addAnimation(aimingAnimation, true);
                        lastTurretState = TurretState.AIMING;
                        break;
                    case FIRING:
                        if (controller.getAnimationState().equals(AnimationState.Stopped)) {
                            animationBuilder.addAnimation(aimingAnimation, true);
                            lastTurretState = TurretState.AIMING;
                        } else {
                            animationBuilder.addAnimation(firingAnimation, false);
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
                        animationBuilder.addAnimation(firingAnimation, false);
                        lastTurretState = TurretState.FIRING;
                        break;
                    case FIRING:
                        animationBuilder.addAnimation(firingAnimation, false);
                        lastTurretState = TurretState.FIRING;
                        break;
                }
                break;
        }
        controller.setAnimation(animationBuilder);
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
            }
        }
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        AnimationController legController = new AnimationController(this, "leg_controller", 3, this::predicate);
        AnimationController headController = new AnimationController(this, "head_controller", 0, this::predicate);

        headController.registerCustomInstructionListener(this::instructionListener);

        animationData.addAnimationController(legController);
        animationData.addAnimationController(headController);
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}