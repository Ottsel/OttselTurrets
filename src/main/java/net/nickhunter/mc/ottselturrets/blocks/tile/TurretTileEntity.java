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
public abstract class TurretTileEntity extends TileEntity implements ITickableTileEntity, IAnimatable {

    protected static final Vec3d targetOffset = new Vec3d(0, .75f, 0);

    private final String idleAnimation;
    private final String aimingAnimation;
    private final String firingAnimation;
    private final String resetAnimation;

    private final SoundEvent chargeSound;
    private final SoundEvent firingSound;

    private final DamageSource damageSource;

    private final int range;
    private final int damage;
    private final double timeToCharge;
    private final double timeToCoolDown;
    private final float pitchMax;
    private final float headPitchMax;
    
    private float headRotationXPrev;
    private float headRotationYPrev;

    private Vec3d posOffset = new Vec3d(.5f, .75f, .5f);

    private LivingEntity target;

    private float yawToTarget;
    private float pitchToTarget;

    private TurretState turretState = TurretState.SCANNING;
    private TurretState lastTurretState = TurretState.SCANNING;

    private boolean chargeSoundHasPlayed;
    private boolean lookingAtTarget;

    private int chargeCounter = -1;
    private int chargeResetCounter = OttselTurrets.TICKS_PER_SECOND * 2;
    private int coolDownTimer = -1;

    private final AnimationFactory factory = new AnimationFactory(this);

    public enum TurretState {
        SCANNING, AIMING, FIRING
    }

    public TurretTileEntity(TileEntityType<?> tileEntityTypeIn, String idleAnimation, String aimingAnimation,
            String firingAnimation, String resetAnimation, SoundEvent chargingSound, SoundEvent firingSound,
            DamageSource damageSource, int range, int damage, double timeToCharge, double timeToCoolDown,
            float pitchMax, float headPitchMax) {
        super(tileEntityTypeIn);
        this.idleAnimation = idleAnimation;
        this.aimingAnimation = aimingAnimation;
        this.firingAnimation = firingAnimation;
        this.resetAnimation = resetAnimation;

        this.chargeSound = chargingSound;
        this.firingSound = firingSound;

        this.damageSource = damageSource;

        this.range = range;
        this.damage = damage;
        this.timeToCharge = timeToCharge;
        this.timeToCoolDown = timeToCoolDown;
        this.pitchMax = pitchMax;
        this.headPitchMax = headPitchMax;
    }

    public float getHeadPitchMax() {
        return headPitchMax;
    }

    public float getPitchToTarget() {
        return pitchToTarget;
    }

    public float getYawToTarget() {
        return yawToTarget;
    }

    public float getHeadRotationXPrev() {
        return headRotationXPrev;
    }
    public float getHeadRotationYPrev() {
        return headRotationYPrev;
    }

    public boolean getLookingAtTarget() {
        return lookingAtTarget;
    }

    public LivingEntity getTarget() {
        return target;
    }

    public Vec3d getPosOffset() {
        return posOffset;
    }

    public TurretState getTurretState() {
        return turretState;
    }

    public void setLookingAtTarget(boolean lookingAtTarget) {
        this.lookingAtTarget = lookingAtTarget;
    }

    public void setTurretState(TurretState turretState) {
        this.turretState = turretState;
    }

    public void setPitchToTarget(float pitchToTarget) {
        if (pitchToTarget > pitchMax)
            pitchToTarget = pitchMax;
        if (pitchToTarget < -pitchMax)
            pitchToTarget = -pitchMax;
        this.pitchToTarget = pitchToTarget;
    }

    public void setYawToTarget(float yawToTarget) {
        this.yawToTarget = yawToTarget;
    }
    public void setHeadRotationXPrev(float headRotationXPrev) {
        this.headRotationXPrev = headRotationXPrev;
    }
    public void setHeadRotationYPrev(float headRotationYPrev) {
        this.headRotationYPrev = headRotationYPrev;
    }

    @Override
    public void tick() {
        commonTick();
        if (world.isRemote) {
            clientTick();
        } else {
            serverTick();
        }
    }

    protected void commonTick() {

        List<LivingEntity> targets = getTargets();

        if (!targets.isEmpty()) {
            target = getClosestTarget(targets);
        } else {
            target = null;
        }
    }

    protected void serverTick() {
        if (target != null) {
            targetsOnServer();
        } else {
            noTargetsOnServer();
        }
    }

    protected void clientTick() {
        if (target != null) {
            targetsOnClient();
        } else {
            noTargetsOnClient();
        }
    }

    protected void targetsOnServer() {
        updateClient(TurretState.AIMING);
        if (coolDownTimer != -1) {
            coolDownTimer();
            return;
        }
        chargeTimer(target);
    }

    protected void noTargetsOnServer() {
        if (coolDownTimer != -1) {
            coolDownTimer();
        }
        if (chargeCounter != -1) {
            resetTimer();
        }
    }

    protected void targetsOnClient() {
        clientTrackTarget();
    }

    protected void noTargetsOnClient() {

    }

    protected void chargingActions() {
        if (!chargeSoundHasPlayed) {
            playSoundEffect(chargeSound);
            chargeSoundHasPlayed = true;
        }
    }

    protected void chargeComplete() {
        // Fire the turret and start the cooldown.
        fireTurret(target);
        chargeSoundHasPlayed = false;
        coolDownTimer();
    }

    // Facilitates firing the turret.
    protected void fireTurret(LivingEntity target) {

        updateClient(TurretState.FIRING);
        playSoundEffect(firingSound);

        // Damage the target.
        target.attackEntityFrom(damageSource, damage);
    }

    protected void coolDownComplete() {
        chargeSoundHasPlayed = false;
        updateClient(TurretState.SCANNING);
    }

    protected void resetComplete() {
        chargeSoundHasPlayed = false;
        updateClient(TurretState.SCANNING);
    }

    protected void clientTrackTarget() {
        Vec3d targetPos = target.getPositionVec();
        yawToTarget = calculateYaw(targetPos);
        pitchToTarget = calculatePitch(targetPos);
    }

    protected final List<LivingEntity> getTargets() {

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
            float pitch = calculatePitch(entity.getPositionVec());

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

    protected final RayTraceResult rayTraceToTarget(Vec3d target) {
        Vec3d posVec = new Vec3d(this.pos.getX() + posOffset.x, this.pos.getY() + posOffset.y,
                this.pos.getZ() + posOffset.z);
        return world.rayTraceBlocks(new RayTraceContext(posVec, target.add(targetOffset),
                RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, null));
    }

    protected final LivingEntity getClosestTarget(List<LivingEntity> targets) {

        LivingEntity closestTarget = targets.get(0);
        double shortestDist = range;

        for (LivingEntity entity : targets) {
            Vec3d entityPos = entity.getPositionVec();
            double dist = entityPos.distanceTo(new Vec3d(this.pos.getX(), this.pos.getY(), this.pos.getZ()));
            if (dist < shortestDist) {
                shortestDist = dist;
                closestTarget = entity;
            }
        }
        return closestTarget;
    }

    // Returns the rough cardinal direction of the target (RELATIVE TO THE TURRET).
    protected final TiltDirection getTargetLocalDirection() {
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

    private float calculateYaw(Vec3d target) {
        Vec3d diffPos = new Vec3d(this.pos.getX() + .5f, this.pos.getY(), this.pos.getZ() + .5f).subtract(target);
        return (float) MathHelper.wrapDegrees(
                MathHelper.atan2(-diffPos.z, -diffPos.x) * (double) (180F / (float) Math.PI) + getYawOffset());
    }

    private float calculatePitch(Vec3d target) {
        Vec3d diffPos = new Vec3d(this.pos.getX() + .5f, this.pos.getY(), this.pos.getZ() + .5f).subtract(target);
        double horizComponent = MathHelper.sqrt((diffPos.z * diffPos.z) + (diffPos.x * diffPos.x));
        float pitch = (float) MathHelper
                .wrapDegrees((MathHelper.atan2(-horizComponent, diffPos.y) * (double) (180F / (float) Math.PI) + 90));
        return pitch;
    }

    // Acts as a timer to determine if the turret is charged up and can fire.
    private void chargeTimer(LivingEntity target) {
        if (chargeCounter < timeToCharge * OttselTurrets.TICKS_PER_SECOND - 1) {
            chargingActions();
            chargeCounter++;
        } else {
            chargeComplete();
            chargeCounter = -1;

        }
    }

    // Acts as a cool down timer for the turret
    private void coolDownTimer() {
        if (coolDownTimer < timeToCoolDown * OttselTurrets.TICKS_PER_SECOND - 1) { // If the turret is cooling down...
            coolDownTimer++;
        } else { // If the turret is done cooling down...
            coolDownComplete();
            coolDownTimer = -1;
        }
    }

    // Acts as a timer for expiring the turret's charge.
    private void resetTimer() {
        if (chargeResetCounter == 0) {
            resetComplete();
            chargeCounter = -1;
            chargeResetCounter = OttselTurrets.TICKS_PER_SECOND * 2;
        } else {
            chargeResetCounter--;
        }
    }

    private void playSoundEffect(SoundEvent soundEvent) {
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

    private void updateClient(TurretState turretState) {
        if (this.turretState == turretState)
            return;
        this.turretState = turretState;
        if (world != null)
            OttselTurrets.getNetworkChannel().sendToTrackingChunk(new PacketTurretUpdate(turretState, pos),
                    world.getChunkAt(pos));
    }

    private int getYawOffset() {
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

    private void animateHead(AnimationController controller) {
        AnimationBuilder animationBuilder = new AnimationBuilder();
        switch (turretState) {
            case SCANNING:
                switch (lastTurretState) {
                    case SCANNING:
                        scanningFromScanning(controller, animationBuilder);
                        break;
                    case AIMING:
                        scanningFromAiming(controller, animationBuilder);
                        break;
                    case FIRING:
                        scanningFromFiring(controller, animationBuilder);
                        break;
                }
                break;
            case AIMING:
                switch (lastTurretState) {
                    case SCANNING:
                        aimingFromScanning(controller, animationBuilder);
                        break;
                    case AIMING:
                        aimingFromAiming(controller, animationBuilder);
                        break;
                    case FIRING:
                        aimingFromFiring(controller, animationBuilder);
                        break;
                }
                break;
            case FIRING:
                switch (lastTurretState) {
                    case SCANNING:
                        firingFromScanning(controller, animationBuilder);
                        break;
                    case AIMING:
                        firingFromAiming(controller, animationBuilder);
                        break;
                    case FIRING:
                        firingFromFiring(controller, animationBuilder);
                        break;
                }
                break;
        }
        controller.setAnimation(animationBuilder);
    }

    protected void scanningFromScanning(AnimationController controller, AnimationBuilder animationBuilder) {
        animationBuilder.addAnimation(resetAnimation, false);
        animationBuilder.addAnimation(idleAnimation, true);
        lastTurretState = TurretState.SCANNING;
    }

    protected void scanningFromAiming(AnimationController controller, AnimationBuilder animationBuilder) {
        animationBuilder.addAnimation(idleAnimation, true);
        lastTurretState = TurretState.SCANNING;
    }

    protected void scanningFromFiring(AnimationController controller, AnimationBuilder animationBuilder) {
        if (controller.getAnimationState().equals(AnimationState.Stopped)) {
            animationBuilder.addAnimation(resetAnimation, false);
            animationBuilder.addAnimation(idleAnimation, true);
            lastTurretState = TurretState.SCANNING;
        } else {
            animationBuilder.addAnimation(firingAnimation, false);
            lastTurretState = TurretState.FIRING;
        }
    }

    protected void aimingFromScanning(AnimationController controller, AnimationBuilder animationBuilder) {
        animationBuilder.addAnimation(aimingAnimation, true);
        playSoundEffect(chargeSound);
        lastTurretState = TurretState.AIMING;
    }

    protected void aimingFromAiming(AnimationController controller, AnimationBuilder animationBuilder) {
        animationBuilder.addAnimation(aimingAnimation, true);
        lastTurretState = TurretState.AIMING;
    }

    protected void aimingFromFiring(AnimationController controller, AnimationBuilder animationBuilder) {
        if (controller.getAnimationState().equals(AnimationState.Stopped)) {
            animationBuilder.addAnimation(aimingAnimation, true);
            lastTurretState = TurretState.AIMING;
        } else {
            animationBuilder.addAnimation(firingAnimation, false);
            lastTurretState = TurretState.FIRING;
        }
    }

    protected void firingFromScanning(AnimationController controller, AnimationBuilder animationBuilder) {
        OttselTurrets.LOGGER.error("Something is horribly wrong: Went from scanning to shooting.");
        lastTurretState = TurretState.SCANNING;
    }

    protected void firingFromAiming(AnimationController controller, AnimationBuilder animationBuilder) {
        animationBuilder.addAnimation(firingAnimation, false);
        playSoundEffect(firingSound);
        lastTurretState = TurretState.FIRING;
    }

    protected void firingFromFiring(AnimationController controller, AnimationBuilder animationBuilder) {
        animationBuilder.addAnimation(firingAnimation, false);
        lastTurretState = TurretState.FIRING;
    }

    protected <ENTITY extends IAnimatable> void instructionListener(CustomInstructionKeyframeEvent<ENTITY> event) {
        for (String instruction : event.instructions) {
            if (instruction.equals("looking_at_target")) {
                OttselTurrets.LOGGER.debug("looking_at_target instruction");
                lookingAtTarget();
            }
            if (instruction.equals("reset_rotation")) {
                OttselTurrets.LOGGER.debug("reset_rotation instruction");
                resetRotation();
            }
        }
    }

    protected void lookingAtTarget() {
        lookingAtTarget = true;
    }

    protected void resetRotation() {
        pitchToTarget = 0;
        yawToTarget = 0;
        lookingAtTarget = true;
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