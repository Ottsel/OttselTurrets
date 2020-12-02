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
import net.minecraft.util.math.Vec3d;
import net.nickhunter.mc.ottselturrets.TurretType;
import net.nickhunter.mc.ottselturrets.blocks.TurretBlock;
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

        BlockPos pos = this.pos;
        Vec3d dPos = new Vec3d(pos.getX(), pos.getY(), pos.getZ());

        double x1 = pos.getX() - range;
        double y1 = pos.getY() - range;
        double z1 = pos.getZ() - range;
        AxisAlignedBB area = new AxisAlignedBB(x1, y1, z1, x1 + range * 2, y1 + range * 2, z1 + range * 2);

        // Find entities around this tile entity.
        List<LivingEntity> entities = null;
        if (world != null) {
            entities = world.getEntitiesWithinAABB(MobEntity.class, area);
        }

        if (entities != null) {
            double shortestDist = range;
            LivingEntity target = null;
            for (LivingEntity entity : entities) {
                Vec3d entityPos = entity.getPositionVec();
                double dist = entityPos.distanceTo(dPos);
                if (dist < shortestDist) {
                    shortestDist = dist;
                    target = entity;
                }
            }
            if (target != null && target.isAlive()) {
                Vec3d targetPos = target.getPositionVec();

                DartEntity dart = EntityRegistry.DART.get().spawn(
                        world,
                        null,
                        null,
                        null,
                        new BlockPos(pos.getX(), pos.getY(), pos.getZ()).add(getDartSpawnOffset()),
                        SpawnReason.DISPENSER,
                        false,
                        false);
                dart.shootAt(targetPos, 1);
            }
        }
        t++;
    }

    public BlockPos getDartSpawnOffset() {
        switch (getBlockState().get(TurretBlock.FACING)) {
            case NORTH:
                return new BlockPos(0, 0, -1);
            case SOUTH:
                return new BlockPos(0, 0, 1);
            case EAST:
                return new BlockPos(1, 0, 0);
            case WEST:
                return new BlockPos(-1, 0, 0);
            case DOWN:
                return new BlockPos(0, -1, 0);
            case UP:
            default:
                return new BlockPos(0, 1, 0);
        }
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
