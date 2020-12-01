package com.hunterpnw.mc.ottselturrets.blocks.tile;

import com.hunterpnw.mc.ottselturrets.OttselTurrets;
import com.hunterpnw.mc.ottselturrets.TurretType;
import com.hunterpnw.mc.ottselturrets.registry.TileRegistry;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.ParticleKeyFrameEvent;
import software.bernie.geckolib3.core.event.SoundKeyframeEvent;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nonnull;
import java.util.List;

import static com.hunterpnw.mc.ottselturrets.TurretType.getTurretTypeFromInt;


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

    private <E extends TileEntity & IAnimatable> PlayState predicate(AnimationEvent<E> event)
    {
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
    public AnimationFactory getFactory()
    {
        return this.factory;
    }

    @Override
    @Nonnull
    public CompoundNBT write(CompoundNBT nbt) {
        nbt.putInt("TurretType",turretType.ordinal());
        return super.write(nbt);
    }
    @Override
    public void read(CompoundNBT nbt){
        turretType = getTurretTypeFromInt(nbt.getInt("TurretType"));
        //OttselTurrets.LOGGER.debug("Read turret type: "+turretType);
        super.read(nbt);
    }

    public int t = 0;

    @Override
    public void tick() {

        //Return if this is the client.
        if(world.isRemote) return;

        //Return if this tick isn't a multiple of 20.
        if(t % 20 != 0) { t++; return; }

        // Create bounding box.
        int range = 10;

        double x = this.pos.getX() - range;
        double y = this.pos.getY() - range;
        double z = this.pos.getZ() - range;
        AxisAlignedBB area = new AxisAlignedBB(x, y, z, x + range * 2, y + range * 2, z + range * 2);

        // Find entities around this tile entity.
        List<Entity> entities = null;
        if (world != null) {
            entities = world.getEntitiesWithinAABB(MonsterEntity.class, area);
        }

        if (entities != null) {
            for (Entity entity : entities) {
                BlockPos pos = entity.getPosition();
                OttselTurrets.LOGGER.debug(
                        entity.getEntityString() +
                        " " + pos.getX() +
                        " " + pos.getY() +
                        " " + pos.getZ() +
                        " Turret type: " + turretType);
            }
        }
        t++;
    }

}
