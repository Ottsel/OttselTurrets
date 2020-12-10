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

    public boolean rotationInit;
    public boolean playChargeSound;
    public boolean playShootSound;
    private boolean chargeSoundBroadcasted;

    public int chargeTime = -1;
    public int coolDownTime = -1;

    private final AnimationFactory factory = new AnimationFactory(this);
    public String currentAnimation = TurretAnimations.SCAN;
    public String prevAnimation = TurretAnimations.SCAN;


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
        List<LivingEntity> targets = getTargets();
        if (!targets.isEmpty()) {
            LivingEntity target = getClosestTarget(targets);

            if (world.isRemote) {
                clientTick(target.getPositionVec());
            } else {
                if (coolDownTime != -1) {
                    coolDown();
                    return;
                }
                updateClient(TurretAnimations.AIM_AT_TARGET);
                chargeUp(target);
            }
        } else if (!world.isRemote) {
            if (coolDownTime != -1) {
                coolDown();
            }
            if (chargeTime != -1) {
                chargeResetCountdown();
            } else {
                yawToTarget = 0;
                pitchToTarget = 0;
                rotationInit = false;
                updateClient(TurretAnimations.RESET_ROTATION);
            }
        }
    }

    private void chargeUp(LivingEntity target) {
        if (chargeTime < timeToCharge * OttselTurrets.TICKS_PER_SECOND - 1) {
            chargeTime++;
            if (chargeSoundBroadcasted) return;
            playSoundEffect(SoundRegistry.LASER_CHARGE.getSound());
            chargeSoundBroadcasted = true;
        } else {
            fireTurret(target);
            coolDown();
            chargeTime = -1;
            chargeSoundBroadcasted = false;

        }
    }

    private void fireTurret(LivingEntity target) {
        updateClient(TurretAnimations.SHOOT);
        playSoundEffect(SoundRegistry.LASER_BOLT.getSound());
        target.attackEntityFrom(new DamageSource(DamageSource.MAGIC.damageType), damage);
    }

    private void coolDown() {
        if (coolDownTime < timeToCoolDown * OttselTurrets.TICKS_PER_SECOND - 1) {
            coolDownTime++;
        } else {
            coolDownTime = -1;
        }
    }

    private void chargeResetCountdown() {
        if (chargeResetTime == 0) {
            chargeReset();
            chargeResetTime = OttselTurrets.TICKS_PER_SECOND * 2;
        } else {
            chargeResetTime--;
        }
    }

    private void chargeReset() {
        chargeTime = -1;
        chargeSoundBroadcasted = false;
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
        if (currentAnimation.equals(animation)) return;
        prevAnimation = currentAnimation;
        currentAnimation = animation;
        if (world != null)
            OttselTurrets.getNetworkChannel().sendToTrackingChunk(new PacketTurretUpdate(currentAnimation, pos), world.getChunkAt(pos));
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

    private void clientTick(Vec3d target) {
        if (playChargeSound) {
            playSoundEffect(SoundRegistry.LASER_CHARGE.getSound());
            playChargeSound = false;
        }
        if (playShootSound) {
            playSoundEffect(SoundRegistry.LASER_BOLT.getSound());
            playShootSound = false;
        }
        lookAtTarget(target);
        setBeamLength(target);
    }

    private void setBeamLength(Vec3d targetPos) {
        if (world == null) return;
        Vec3d posVec = new Vec3d(this.pos.getX() + .5, this.pos.getY() + 1, this.pos.getZ() + .5);
        Vec3d posOffset = posVec.subtract(targetPos);
        RayTraceResult result = world.rayTraceBlocks(new RayTraceContext(posVec.add(posOffset), targetPos, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, null));
        if (result.getType() == RayTraceResult.Type.MISS) {
            beamLength = 256;
        } else {
            beamLength = 256;//(float) posVec.distanceTo(result.getHitVec());
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

    public boolean lookingAtTarget;
    private <E extends TileEntity & IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        AnimationController controller = event.getController();

        boolean loop = false;
        if (currentAnimation.equals(TurretAnimations.RESET_ROTATION) && controller.getAnimationState() == AnimationState.Stopped) {
            currentAnimation = TurretAnimations.SCAN;
            loop = true;
        }
        if (currentAnimation.equals(TurretAnimations.AIM_AT_TARGET)) {
            loop = true;
        }

        controller.setAnimation(new AnimationBuilder().addAnimation(currentAnimation, loop));
        return PlayState.CONTINUE;
    }
    private <ENTITY extends IAnimatable> void instructionListener(CustomInstructionKeyframeEvent<ENTITY> event)
    {
        switch (event.instructions.get(0)){
            case "looking_at_target":
                lookingAtTarget = true;
                break;
            case "reset_rotation":
                pitchToTarget = 0;
                yawToTarget = 0;
                lookingAtTarget = true;
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
        currentAnimation = tag.getString("CurrentAnimation");
    }
}