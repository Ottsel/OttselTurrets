package net.nickhunter.mc.ottselturrets.entities;

import net.minecraft.entity.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.nickhunter.mc.ottselturrets.registry.EntityRegistry;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.List;

public class DartEntity extends Entity implements IAnimatable {

    private final AnimationFactory factory = new AnimationFactory(this);
    public boolean isBeam;
    public float beamLength = 1;
    public double velocity;
    int ticksAlive;
    double damage = 20;

    AxisAlignedBB boundingBox = getBoundingBox();

    public DartEntity(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
        this.ignoreFrustumCheck = true;
    }

    public DartEntity(BlockPos spawnPos, World worldIn, Vec3d target) {
        this(EntityRegistry.DART.get(), worldIn);
        this.setPositionAndUpdate(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        isBeam = true;
        shootBeamAt(target);
    }

    public DartEntity(BlockPos spawnPos, World worldIn, Vec3d target, double velocity) {
        this(EntityRegistry.DART.get(), worldIn);
        this.setPositionAndUpdate(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        shootAt(target, velocity);
    }

    private void shootBeamAt(Vec3d target) {
        this.setMotion(target.subtract(this.getPositionVec()).normalize());

        beamLength = (float) target.distanceTo(this.getPositionVec());
        Vec3d motion = this.getMotion();
        Vec3d middle = motion.scale(beamLength / 2);

        setAngleFromMotion(motion);
        setBoundingBoxWidth(beamLength);

        Vec3d pos = this.getPositionVec().add(middle);

        this.setPositionAndUpdate(pos.getX(), pos.getY(), pos.getZ());
    }


    private void shootAt(Vec3d target, double velocity) {
        this.setMotion(target.subtract(this.getPositionVec()).normalize());
        this.setAngleFromMotion(this.getMotion());
        this.velocity = velocity;
    }

    private void setBoundingBoxWidth(double width) {
        // Create bounding box.
        double rangeVert = this.getSize(getPose()).height;
        Vec3d pos = this.getPositionVec();

        double x = pos.getX() - width;
        double y = pos.getY() - width;
        double z = pos.getZ() - rangeVert;
        boundingBox = new AxisAlignedBB(x, y, z, x + width * 2, y + rangeVert * 2, z + width * 2);
    }

    private void stepProjectile() {
        Vec3d pos = this.getPositionVec();
        Vec3d posMotion = pos.add(getMotion().mul(this.velocity, this.velocity, this.velocity));
        this.setPositionAndUpdate(posMotion.x, posMotion.y, posMotion.z);
    }

    public void tick() {
        super.tick();
        ticksAlive++;
        if (ticksAlive > 1000) this.remove();
        if (!isBeam) {
            stepProjectile();
        }

        AxisAlignedBB area = boundingBox;

        List<LivingEntity> entities = null;
        if (world != null) {
            entities = world.getEntitiesWithinAABB(MobEntity.class, area);
        }

        if (entities != null) {
            for (LivingEntity entity : entities) {
                DamageSource damageSource;
                damageSource = DamageSource.causeIndirectMagicDamage(entity, this);
                damageSource.setProjectile();
                entity.attackEntityFrom(damageSource, 20);
                this.remove();
            }
        }
    }

    private void setAngleFromMotion(Vec3d motion) {
        float f = MathHelper.sqrt(horizontalMag(motion));
        this.rotationYaw = (float) (MathHelper.atan2(motion.x, motion.z) * (double) (180F / (float) Math.PI));
        this.rotationPitch = (float) (MathHelper.atan2(motion.y, f) * (double) (180F / (float) Math.PI));
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event)
    {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.dart.shrink", false));
        return PlayState.CONTINUE;
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Override
    protected void registerData() {

    }

    @Override
    protected void readAdditional(CompoundNBT compound) {

    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {

    }

}
