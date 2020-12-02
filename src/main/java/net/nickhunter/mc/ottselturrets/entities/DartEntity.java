package net.nickhunter.mc.ottselturrets.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.List;

public class DartEntity extends Entity {

    public DartEntity(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    public void tick() {
        Vec3d pos = this.getPositionVec();
        Vec3d motion = this.getMotion();
        this.setPosition(pos.x+motion.x,pos.y+motion.y,pos.z+motion.z);

        // Create bounding box.
        double range = .5;

        double x = pos.getX() - range;
        double y = pos.getY() - range;
        double z = pos.getZ() - range;
        AxisAlignedBB area = new AxisAlignedBB(x, y, z, x + range * 2, y + range * 2, z + range * 2);

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
        super.tick();
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
