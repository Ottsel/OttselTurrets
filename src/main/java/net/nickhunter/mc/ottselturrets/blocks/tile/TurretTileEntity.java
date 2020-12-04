package net.nickhunter.mc.ottselturrets.blocks.tile;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.TurretType;
import net.nickhunter.mc.ottselturrets.entities.DartEntity;
import net.nickhunter.mc.ottselturrets.registry.SoundRegistry;
import net.nickhunter.mc.ottselturrets.registry.TileRegistry;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
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


public class TurretTileEntity extends TileEntity implements ITickableTileEntity, IAnimatable {

    private final AnimationFactory factory = new AnimationFactory(this);
    public TurretType turretType;
    public float yawToTarget;
    public boolean targetExists;
    public static final int range = 10;
    public static final double timeToCharge = 1.5;
    public static final double timeToCoolDown = 2.0;

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

    private <E extends TileEntity & IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        AnimationController controller = event.getController();
        if (!targetExists)
            controller.setAnimation(new AnimationBuilder().addAnimation("animation.turret_horizontal.scan", true));
        controller.transitionLengthTicks = 0;

        return PlayState.CONTINUE;
    }

    @Override
    public void tick() {

        if (coolDownTime != -1) {
            coolDown();
            return;
        }

        List<LivingEntity> targets = getTargets();
        if (!targets.isEmpty()) {
            LivingEntity target = getClosestTarget(targets);
            if (target.isAlive()) {
                targetExists = true;
                lookAtTarget(new Vec3d(target.getPosition().getX(), target.getPosition().getY(), target.getPosition().getZ()));
                chargeUp(target.getPositionVec());
            } else {
                targetExists = false;
            }
        } else {
            targetExists = false;
        }
    }

    public int chargeTime = -1;
    private boolean chargeSoundPlayed;

    private void chargeUp(Vec3d targetPos) {
        if (chargeTime < timeToCharge * OttselTurrets.TICKS_PER_SECOND - 1) {
            chargeTime++;
            if (chargeSoundPlayed) return;
            playSoundEffect(SoundRegistry.LASER_CHARGE.getSound());
            chargeSoundPlayed = true;
        } else {
            fireTurret(targetPos);
            coolDown();
            chargeTime = -1;
            chargeSoundPlayed = false;
        }
    }

    public int coolDownTime = -1;

    private void coolDown() {
        if (coolDownTime < timeToCharge * OttselTurrets.TICKS_PER_SECOND - 1) {
            coolDownTime++;
        } else {
            coolDownTime = -1;
        }
    }

    private void lookAtTarget(Vec3d targetPos) {
        Vec3d diffPos = new Vec3d(this.pos.getX(), this.pos.getY(), this.pos.getZ()).subtract(targetPos);
        yawToTarget = (float) (MathHelper.atan2(diffPos.x, diffPos.z) * (double) (180F / (float) Math.PI)) + getYawOffset();
    }

    private int getYawOffset() {
        switch (this.getBlockState().get(HORIZONTAL_FACING)) {
            case NORTH:
            default:
                return 180;
            case SOUTH:
                return 0;
            case EAST:
                return 90;
            case WEST:
                return -90;
        }
    }

    private void fireTurret(Vec3d targetPos) {
        world.addEntity(new DartEntity(this.pos.add(getDartSpawnOffset()), world, targetPos));
        playSoundEffect(SoundRegistry.LASER_BOLT.getSound());
    }

    private void playSoundEffect(SoundEvent soundEvent) {

        for (PlayerEntity player : world.getPlayers()) {
            if (player.getPosition().withinDistance(this.pos, range * 2)) {
                Vec3i playerPos = new Vec3i(player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
                world.playSound(
                        player,
                        this.pos,
                        soundEvent,
                        SoundCategory.BLOCKS,
                        (float) (1 / Math.sqrt((this.pos.distanceSq(playerPos)))),
                        1);
            }
        }
    }

    private BlockPos getDartSpawnOffset() {
        switch (this.getBlockState().get(HORIZONTAL_FACING)) {
            case NORTH:
            default:
                return new BlockPos(0, 0, 1);
            case SOUTH:
                return new BlockPos(0, 0, -1);
            case EAST:
                return new BlockPos(1, 0, 0);
            case WEST:
                return new BlockPos(-1, 0, 0);
        }
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

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
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

    @Override
    public void handleUpdateTag(CompoundNBT tag) {
        turretType = getTurretTypeFromInt(tag.getInt("TurretType"));
    }
}
