package net.nickhunter.mc.ottselturrets.blocks.tile;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.TurretType;
import net.nickhunter.mc.ottselturrets.entities.DartEntity;
import net.nickhunter.mc.ottselturrets.registry.EntityRegistry;
import net.nickhunter.mc.ottselturrets.registry.TileRegistry;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nonnull;
import java.util.List;

import static net.nickhunter.mc.ottselturrets.TurretType.getTurretTypeFromInt;


public class TurretTileEntity extends TileEntity implements ITickableTileEntity, IAnimatable {

    private final AnimationFactory factory = new AnimationFactory(this);
    public TurretType turretType;

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
        controller.transitionLengthTicks = 0;
        controller.setAnimation(new AnimationBuilder().addAnimation("animation.ottselturrets.test", true));

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

    public int t = 0;

    @Override
    public void tick() {

        //Return if this is the client.
        if (world != null && world.isRemote) return;

        //Return if this tick isn't a multiple of 20.
        if (t % 20 != 0) {
            t++;
            return;
        }

        // Create bounding box.
        int range = 10;

        double x = this.pos.getX() - range;
        double y = this.pos.getY() - range;
        double z = this.pos.getZ() - range;
        AxisAlignedBB area = new AxisAlignedBB(x, y, z, x + range * 2, y + range * 2, z + range * 2);

        // Find entities around this tile entity.
        List<LivingEntity> entities = null;
        if (world != null) {
            entities = world.getEntitiesWithinAABB(MobEntity.class, area);
        }

        if (entities != null) {
            double shortestDist = range;
            LivingEntity shortest = null;
            for (LivingEntity entity : entities) {
                BlockPos pos = entity.getPosition();
                double dist = pos.distanceSq(this.pos);
                if (dist < shortestDist) {
                    shortestDist = dist;
                    shortest = entity;
                }
            }
            if (shortest != null) {
                OttselTurrets.LOGGER.debug(
                        shortest.getEntityString() +
                                " " + pos.getX() +
                                " " + pos.getY() +
                                " " + pos.getZ() +
                                " Turret type: " + turretType);
                DartEntity dart = EntityRegistry.DART.get().spawn(world, null, null, null, this.pos.add(new Vec3i(0, 1, 0)), SpawnReason.DISPENSER, false, false);
                dart.setMotion(0,0.2,0);
            }
        }
        t++;
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
