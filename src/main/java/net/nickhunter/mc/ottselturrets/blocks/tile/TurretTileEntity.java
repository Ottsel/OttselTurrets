package net.nickhunter.mc.ottselturrets.blocks.tile;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.DamageSource;
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
import software.bernie.geckolib3.core.builder.Animation;
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

    public float beamLength;
    public float yawToTarget;
    public float pitchToTarget;

    private boolean chargeSoundHasPlayed;
    public boolean lookingAtTarget;
    public boolean aimingPaused;

    public int chargeTime = -1;
    public int coolDownTime = -1;

    private final AnimationFactory factory = new AnimationFactory(this);
    public String queuedAnimation = TurretAnimations.SCAN;

    public static class TurretAnimations {
        public static final String SCAN = "animation.turret_horizontal.scan";
        public static final String AIM_AT_TARGET = "animation.turret_horizontal.rotate_head";
        public static final String SHOOT = "animation.turret_horizontal.fire_beam";
        public static final String RESET_ROTATION = "animation.turret_horizontal.reset_rotation";
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
        if (world == null) return;
        //Get a list of nearby potential targets.
        List<LivingEntity> targets = getTargets();
        if (!targets.isEmpty()) { //If there are potential targets...
            LivingEntity target = getClosestTarget(targets);
            if (world.isRemote) { //On client...
                //Call clientTick.
                clientTrackTarget(target.getPositionVec());
            } else { //On server...
                //Return if still cooling down.
                if (coolDownTime != -1) {
                    coolDown();

                    //Reset the turret's rotation.
                    updateClient(TurretAnimations.RESET_ROTATION);
                    return;
                }
                //Tell the client to aim at the target, then charge up the turret.
                updateClient(TurretAnimations.AIM_AT_TARGET);
                chargeUp(target);
            }
        } else { //If there are no potential targets...
            if (!world.isRemote) { //On server...
                //Cool down if applicable.
                if (coolDownTime != -1) {
                    coolDown();
                }
                //Count down the charge reset timer if the turret has started to charge up...
                if (chargeTime != -1) {
                    resetTimer();
                } else { //If the charge expires...
                    //Reset the turret's rotation.
                    yawToTarget = 0;
                    pitchToTarget = 0;
                    updateClient(TurretAnimations.RESET_ROTATION);
                }
            }
        }
    }

    /*
    Acts as a timer to determine if the turret is charged up and can fire.
     */
    private void chargeUp(LivingEntity target) {
        if (chargeTime < timeToCharge * OttselTurrets.TICKS_PER_SECOND - 1) { //If the turret is still charging up...
            chargeTime++;

            //Return if the charge sound has already been played. TODO Sound event
            if (chargeSoundHasPlayed) return;

            //Play the charge sound
            playSoundEffect(SoundRegistry.LASER_CHARGE.getSound());
            chargeSoundHasPlayed = true;
        } else { //If the turret is done charging up...

            //Fire the turret and start the cooldown.
            fireTurret(target);
            coolDown();

            //Reset the charge time and charge sound flag.
            chargeTime = -1;
            chargeSoundHasPlayed = false;

        }
    }

    /*
    Facilitates firing the turret.
     */
    private void fireTurret(LivingEntity target) {

        //Tell the client to play the shooting animation.
        updateClient(TurretAnimations.SHOOT);

        //Play shooting sound. TODO Sound event
        playSoundEffect(SoundRegistry.LASER_BOLT.getSound());

        //Damage the target. //TODO customize this
        target.attackEntityFrom(new DamageSource(DamageSource.MAGIC.damageType), damage);
    }

    /*
    Acts as a cool down timer for the turret
     */
    private void coolDown() {
        if (coolDownTime < timeToCoolDown * OttselTurrets.TICKS_PER_SECOND - 1) { //If the turret is cooling down...
            coolDownTime++;
        } else { //If the turret is done cooling down...
            coolDownTime = -1;
        }
    }

    /*
    Acts as a timer for expiring the turret's charge.
     */
    private void resetTimer() {
        if (chargeResetTime == 0) { //If the turret's reset timer is done...

            //Reset the charge.
            chargeTime = -1;
            chargeSoundHasPlayed = false;
            chargeResetTime = OttselTurrets.TICKS_PER_SECOND * 2;
        } else { //If the turret's reset timer is not done...
            chargeResetTime--;
        }
    }

    public void playSoundEffect(SoundEvent soundEvent) {
        if (world == null) return;
        PlayerEntity player = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ());
        if (player == null) return;
        Vec3i playerPos = new Vec3i(player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
        world.playSound(player, this.pos, soundEvent, SoundCategory.BLOCKS, (float) (1 / Math.sqrt((this.pos.distanceSq(playerPos)))), 1);
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

    private void updateClient(String animation) {
        if (queuedAnimation.equals(animation)) return;
        queuedAnimation = animation;
        if (world != null)
            OttselTurrets.getNetworkChannel().sendToTrackingChunk(new PacketTurretUpdate(queuedAnimation, pos), world.getChunkAt(pos));
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
    Client
     */

    /*
    Tracks the target position every tick.
     */
    private void clientTrackTarget(Vec3d target) {
        setBeamLength(target);
        if (aimingPaused) return;
        lookAtTarget(target);
    }

    private void setBeamLength(Vec3d targetPos) {
        if (world == null) return;
        Vec3d posVec = new Vec3d(this.pos.getX() + .5, this.pos.getY() + 1, this.pos.getZ() + .5);
        Vec3d posOffset = posVec.subtract(targetPos);
        RayTraceResult result = world.rayTraceBlocks(new RayTraceContext(posVec.add(posOffset), targetPos, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, null));
        if (result.getType() == RayTraceResult.Type.MISS) {
            beamLength = 256;
        } else {
            beamLength = (float) posVec.distanceTo(result.getHitVec());
        }
    }

    private void lookAtTarget(Vec3d targetPos) {
        Vec3d diffPos = new Vec3d(this.pos.getX() + .5f, this.pos.getY(), this.pos.getZ() + .5f).subtract(targetPos);
        yawToTarget = getYaw(diffPos);
        pitchToTarget = getPitch(diffPos);
    }

    private float getYaw(Vec3d diffPos) {
        return (float) MathHelper.wrapDegrees(MathHelper.atan2(-diffPos.z, -diffPos.x) * (double) (180F / (float) Math.PI) + getYawOffset());
    }

    private float getPitch(Vec3d diffPos) {
        double horizComponent = MathHelper.sqrt((diffPos.z * diffPos.z) + (diffPos.x * diffPos.x));
        int sign;
        if (yawToTarget < 0)
            sign = -1;
        else {
            sign = 1;
        }
        return (float) MathHelper.wrapDegrees((MathHelper.atan2(sign * horizComponent, diffPos.y) * (double) (180F / (float) Math.PI) + 90));
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

    boolean loop;

    private <E extends TileEntity & IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        AnimationController controller = event.getController();
        Animation currentAnimation = controller.getCurrentAnimation();

        if (currentAnimation != null) {

            //Don't update animation if it will interrupt the SHOOT animation.
            if (currentAnimation.animationName.equals(TurretAnimations.SHOOT) && controller.getAnimationState().equals(AnimationState.Running))
                return PlayState.CONTINUE;
        }

        //Set loop and other attributes depending on the animation that's queued.
        switch (queuedAnimation) {
            case TurretAnimations.SCAN:
            case TurretAnimations.AIM_AT_TARGET:
                loop = true;
                break;
            case TurretAnimations.SHOOT:
                aimingPaused = true;
                loop = false;
                break;
            case TurretAnimations.RESET_ROTATION:
                if (controller.getAnimationState() == AnimationState.Stopped && !currentAnimation.animationName.equals(TurretAnimations.SHOOT)) {
                    queuedAnimation = TurretAnimations.SCAN;
                    loop = true;
                    break;
                }
                loop = false;
                break;
        }

        controller.setAnimation(new AnimationBuilder().addAnimation(queuedAnimation, loop));
        return PlayState.CONTINUE;
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