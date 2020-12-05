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
                updateClient(TurretAnimations.SCAN);
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
        setBeamLength(target);
        lookAtTarget(target);
    }

    private void setBeamLength(Vec3d targetPos) {
        beamLength = (float) targetPos.distanceTo(new Vec3d(this.pos.getX(), this.pos.getY(), this.pos.getZ()));
    }

    private void lookAtTarget(Vec3d targetPos) {

        Vec3d diffPos = new Vec3d(this.pos.getX(), this.pos.getY(), this.pos.getZ()).subtract(targetPos);
        yawToTarget = (float) MathHelper.wrapDegrees(MathHelper.atan2(-diffPos.z, -diffPos.x) * (double) (180F / (float) Math.PI) + getYawOffset());
        OttselTurrets.LOGGER.debug(yawToTarget);
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
        AnimationBuilder builder = new AnimationBuilder();
        Animation animation = controller.getCurrentAnimation();
        AnimationState state = controller.getAnimationState();
        controller.transitionLengthTicks = 0;
        if (animation != null) {
            if (!animation.animationName.equals(currentAnimation)) {
                OttselTurrets.LOGGER.debug(currentAnimation);
            }

            //If the current animation is the SHOOT animation, do not interrupt it.
            if (animation.animationName.equals(TurretAnimations.SHOOT) && state.equals(AnimationState.Running))
                return PlayState.CONTINUE;

            //If the current animation is equal to the next animation, do not interrupt it.
            if (currentAnimation.equals(animation.animationName) && state.equals(AnimationState.Running))
                return PlayState.CONTINUE;
        }

        //Set the new animation, loop only if it isn't the SHOOT animation.
        if(currentAnimation.equals(TurretAnimations.SHOOT)) {
            controller.setAnimation(builder.addAnimation(currentAnimation,false));
        }else{
            controller.setAnimation(builder.addAnimation(currentAnimation,true));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController(this, "controller", 0, this::predicate));
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