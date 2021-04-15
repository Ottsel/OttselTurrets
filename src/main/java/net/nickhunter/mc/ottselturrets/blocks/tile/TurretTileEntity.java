package net.nickhunter.mc.ottselturrets.blocks.tile;

import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.blocks.TurretBlock;
import net.nickhunter.mc.ottselturrets.util.TiltDirection;
import net.nickhunter.mc.ottselturrets.util.TrigHelper;
import net.nickhunter.mc.ottselturrets.util.TurretState;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.CustomInstructionKeyframeEvent;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public abstract class TurretTileEntity extends AnimatedTileEntity implements ITickableTileEntity {

    protected static final Vector3d targetOffset = new Vector3d(0, .75f, 0);

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

    private Vector3d posOffset = new Vector3d(.5f, .75f, .5f);

    private LivingEntity target;

    private float yawToTarget;
    private float pitchToTarget;

    private TurretState lastTurretState = TurretState.SCANNING;

    private boolean chargeSoundHasPlayed;
    private boolean lookingAtTarget;

    private int chargeCounter = -1;
    private int chargeResetCounter = OttselTurrets.TICKS_PER_SECOND * 2;
    private int coolDownTimer = -1;

    private final AnimationFactory factory = new AnimationFactory(this);

    public TurretTileEntity(TileEntityType<? extends AnimatedTileEntity> tileEntityTypeIn, String idleAnimation,
            String aimingAnimation, String firingAnimation, String resetAnimation, SoundEvent chargingSound,
            SoundEvent firingSound, DamageSource damageSource, int range, int damage, double timeToCharge,
            double timeToCoolDown, float pitchMax, float headPitchMax) {
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

    public Vector3d getPosOffset() {
        return posOffset;
    }

    public TurretState getState() {
        if (level != null) {
            return level.getBlockState(worldPosition).getValue(TurretBlock.TURRET_STATE);
        } else {
            return TurretState.SCANNING;
        }
    }

    public void setLookingAtTarget(boolean lookingAtTarget) {
        this.lookingAtTarget = lookingAtTarget;
    }

    public void setState(TurretState turretState) {
        if (level != null) {
            BlockState blockState = level.getBlockState(worldPosition);
            if (blockState.getValue(TurretBlock.TURRET_STATE) != turretState)
                level.setBlock(worldPosition, blockState.setValue(TurretBlock.TURRET_STATE, turretState), 2);
        } else {
            OttselTurrets.LOGGER.error(worldPosition + " Failed to set turret state: " + turretState);
        }
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
        if (headRotationXPrev > pitchMax)
            headRotationXPrev = pitchMax;
        if (headRotationXPrev < -pitchMax)
            headRotationXPrev = -pitchMax;
        this.headRotationXPrev = headRotationXPrev;
    }

    public void setHeadRotationYPrev(float headRotationYPrev) {
        this.headRotationYPrev = headRotationYPrev;
    }

    @Override
    public void tick() {
        commonTick();
        if (level.isClientSide) {
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
        setState(TurretState.AIMING);
        if (coolDownTimer != -1) {
            coolDownTimer(true);
            return;
        }
        chargeTimer(target);
    }

    protected void noTargetsOnServer() {
        if (coolDownTimer != -1) {
            coolDownTimer(false);
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

    protected void chargeComplete(boolean hasTarget) {
        // Fire the turret and start the cooldown.
        fireTurret(target);
        chargeSoundHasPlayed = false;
        coolDownTimer(hasTarget);
    }

    // Facilitates firing the turret.
    protected void fireTurret(LivingEntity target) {
        setState(TurretState.FIRING);
        playSoundEffect(firingSound);
        target.hurt(damageSource, damage);
    }

    protected void coolDownComplete(boolean hasTarget) {
        chargeSoundHasPlayed = false;
        setState(hasTarget ? TurretState.AIMING : TurretState.SCANNING);
    }

    protected void resetComplete() {
        chargeSoundHasPlayed = false;
        setState(TurretState.SCANNING);
    }

    protected void clientTrackTarget() {
        Vector3d targetPos = target.position();
        yawToTarget = TrigHelper.calculateYaw(this.worldPosition, targetPos) + getYawOffset();
        pitchToTarget = TrigHelper.calculatePitch(this.worldPosition, targetPos) + 90;
    }

    protected final List<LivingEntity> getTargets() {

        double x1 = this.worldPosition.getX() - range;
        double y1 = this.worldPosition.getY() - range;
        double z1 = this.worldPosition.getZ() - range;
        AxisAlignedBB area = new AxisAlignedBB(x1, y1, z1, x1 + range * 2, y1 + range * 2, z1 + range * 2);

        // Find entities around this tile entity.
        List<LivingEntity> entities = Collections.emptyList();

        entities = level.getEntitiesOfClass(MobEntity.class, area);
        List<LivingEntity> validTargets = new ArrayList<LivingEntity>(entities);
        entities.forEach((entity) -> {

            RayTraceResult result = rayTraceToTarget(entity.position());
            float pitch = TrigHelper.calculatePitch(this.worldPosition, entity.position()) + 90;

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

    protected final RayTraceResult rayTraceToTarget(Vector3d target) {
        Vector3d posVec = new Vector3d(this.worldPosition.getX() + posOffset.x, this.worldPosition.getY() + posOffset.y,
                this.worldPosition.getZ() + posOffset.z);
        return level.clip(new RayTraceContext(posVec, target.add(targetOffset),
                RayTraceContext.BlockMode.VISUAL, RayTraceContext.FluidMode.NONE, null));
    }

    protected final LivingEntity getClosestTarget(List<LivingEntity> targets) {

        LivingEntity closestTarget = targets.get(0);
        double shortestDist = range;

        for (LivingEntity entity : targets) {
            Vector3d entityPos = entity.position();
            double dist = entityPos.distanceTo(new Vector3d(this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ()));
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

    // Acts as a timer to determine if the turret is charged up and can fire.
    private void chargeTimer(LivingEntity target) {
        if (chargeCounter < timeToCharge * OttselTurrets.TICKS_PER_SECOND - 1) {
            chargingActions();
            chargeCounter++;
        } else {
            chargeComplete(true);
            chargeCounter = -1;

        }
    }

    // Acts as a cool down timer for the turret
    private void coolDownTimer(boolean hasTarget) {
        if (coolDownTimer < timeToCoolDown * OttselTurrets.TICKS_PER_SECOND - 1) { // If the turret is cooling down...
            coolDownTimer++;
        } else { // If the turret is done cooling down...
            coolDownComplete(hasTarget);
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

    protected void playSoundEffect(SoundEvent soundEvent) {
        level.playSound(null, this.worldPosition, soundEvent, SoundCategory.BLOCKS, .5f, 1);
    }

    private int getYawOffset() {
        switch (this.getBlockState().getValue(HORIZONTAL_FACING)) {
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
        AnimationController<?> controller = event.getController();
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

    protected void animateLegs(AnimationController<?> controller) {

    }

    private void animateHead(AnimationController<?> controller) {
        AnimationBuilder animationBuilder = new AnimationBuilder();
        switch (getState()) {
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
        default:
            break;
        }
        controller.setAnimation(animationBuilder);
    }

    protected void scanningFromScanning(AnimationController<?> controller, AnimationBuilder animationBuilder) {
        animationBuilder.addAnimation(resetAnimation, false);
        animationBuilder.addAnimation(idleAnimation, true);
        lastTurretState = TurretState.SCANNING;
    }

    protected void scanningFromAiming(AnimationController<?> controller, AnimationBuilder animationBuilder) {
        animationBuilder.addAnimation(idleAnimation, true);
        lastTurretState = TurretState.SCANNING;
    }

    protected void scanningFromFiring(AnimationController<?> controller, AnimationBuilder animationBuilder) {
        if (controller.getAnimationState().equals(AnimationState.Stopped)) {
            animationBuilder.addAnimation(resetAnimation, false);
            animationBuilder.addAnimation(idleAnimation, true);
            lastTurretState = TurretState.SCANNING;
        } else {
            animationBuilder.addAnimation(firingAnimation, false);
            lastTurretState = TurretState.FIRING;
        }
    }

    protected void aimingFromScanning(AnimationController<?> controller, AnimationBuilder animationBuilder) {
        animationBuilder.addAnimation(aimingAnimation, true);
        lastTurretState = TurretState.AIMING;
    }

    protected void aimingFromAiming(AnimationController<?> controller, AnimationBuilder animationBuilder) {
        animationBuilder.addAnimation(aimingAnimation, true);
        lastTurretState = TurretState.AIMING;
    }

    protected void aimingFromFiring(AnimationController<?> controller, AnimationBuilder animationBuilder) {
        if (controller.getAnimationState().equals(AnimationState.Stopped)) {
            animationBuilder.addAnimation(aimingAnimation, true);
            lastTurretState = TurretState.AIMING;
        } else {
            animationBuilder.addAnimation(firingAnimation, false);
            lastTurretState = TurretState.FIRING;
        }
    }

    protected void firingFromScanning(AnimationController<?> controller, AnimationBuilder animationBuilder) {
        OttselTurrets.LOGGER.error("Something is horribly wrong: Went from scanning to shooting.");
        lastTurretState = TurretState.SCANNING;
    }

    protected void firingFromAiming(AnimationController<?> controller, AnimationBuilder animationBuilder) {
        animationBuilder.addAnimation(firingAnimation, false);
        lastTurretState = TurretState.FIRING;
    }

    protected void firingFromFiring(AnimationController<?> controller, AnimationBuilder animationBuilder) {
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
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