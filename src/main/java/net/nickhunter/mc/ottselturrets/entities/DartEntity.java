package net.nickhunter.mc.ottselturrets.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.List;

public class DartEntity extends Entity {

    public DartEntity(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    public double velocity;

    public void shootAt(Vec3d target, double velocity) {
        this.setMotion(target.subtract(this.getPositionVec()).normalize());
        this.velocity = velocity;
    }

    public void tick() {
        super.tick();
        Vec3d pos = this.getPositionVec();
        Vec3d motion = this.getMotion();
        Vec3d posMotion = pos.add(motion.mul(this.velocity, this.velocity, this.velocity));
        float f = MathHelper.sqrt(horizontalMag(motion));
        this.rotationYaw = (float) (MathHelper.atan2(motion.x, motion.z) * (double) (180F / (float) Math.PI));
        this.rotationPitch = (float) (MathHelper.atan2(motion.y, f) * (double) (180F / (float) Math.PI));
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
        this.setPositionAndUpdate(posMotion.x, posMotion.y, posMotion.z);
        // Create bounding box.
        double rangeHoriz = this.getSize(getPose()).width;
        double rangeVert = this.getSize(getPose()).height;

        double x = pos.getX() - rangeHoriz;
        double y = pos.getY() - rangeHoriz;
        double z = pos.getZ() - rangeVert;
        AxisAlignedBB area = new AxisAlignedBB(x, y, z, x + rangeHoriz * 2, y + rangeVert * 2, z + rangeHoriz * 2);

        List<LivingEntity> entities = null;
        if (world != null) {
            entities = world.getEntitiesWithinAABB(MobEntity.class, area);
        }

        if (entities != null) {
            for (LivingEntity entity : entities) {
                DamageSource damageSource;
                damageSource = DamageSource.causeIndirectMagicDamage(entity, this);
                damageSource.setProjectile();
                entity.attackEntityFrom(damageSource, 4);
                this.remove();
            }
        }
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

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
