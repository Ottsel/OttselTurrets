package net.nickhunter.mc.ottselturrets.blocks.tile;

import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.nickhunter.mc.ottselturrets.registry.TileRegistry;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

@SuppressWarnings("rawtypes")
public class ProjectorTileEntity extends TileEntity implements ITickableTileEntity, IAnimatable {

    protected static final Vector3d targetOffset = new Vector3d(0, .75f, 0);

    public static final String FIRE_BEAM_ANIMATION = "animation.laser_projector.fire_beam";
    public static final String BEAM_ANIMATION = "animation.laser_projector.beam";
    public static final String RETRACT_BEAM_ANIMATION = "animation.laser_projector.retract_beam";
    public static final String ROTATE_ANIMATION = "animation.laser_projector.rotate_arm";

    public static final int RANGE = 256;
    public static final float pitchMax = 45;

    private static final Vector3d posOffset = new Vector3d(.5f, .25f, .5f);

    private float yawPrev;
    private float pitchPrev;

    private float yaw;
    private float pitch;

    private float beamLength;

    private final AnimationFactory factory = new AnimationFactory(this);

    public ProjectorTileEntity() {
        super(TileRegistry.PROJECTOR.get());
    }

    public float getPitchMax() {
        return pitchMax;
    }

    public float getPitchToTarget() {
        return pitch;
    }

    public float getYawToTarget() {
        return yaw;
    }

    public float getPitchPrev() {
        return pitchPrev;
    }

    public float getYawPrev() {
        return yawPrev;
    }

    public float getBeamLength() {
        return beamLength;
    }

    public void setPitchToTarget(float pitch) {
        if (pitch > pitchMax)
            pitch = pitchMax;
        if (pitch < -pitchMax)
            pitch = -pitchMax;
        this.pitch = pitch;
    }

    public void setYawToTarget(float yawToTarget) {
        this.yaw = yawToTarget;
    }

    public void setPitchPrev(float pitchPrev) {
        if (pitchPrev > pitchMax)
            pitchPrev = pitchMax;
        if (pitchPrev < -pitchMax)
            pitchPrev = -pitchMax;
        this.pitchPrev = pitchPrev;
    }

    public void setHeadRotationYPrev(float yawPrev) {
        this.yawPrev = yawPrev;
    }

    @Override
    public void tick() {
        commonTick();
        if (world.isRemote) {
            clientTick();
        } else {
            serverTick();
        }
    }

    protected void commonTick() {

    }

    protected void serverTick() {

    }

    protected void clientTick() {

    }

    protected final RayTraceResult rayTraceToTarget(Vector3d target) {
        Vector3d posVec = new Vector3d(this.pos.getX() + posOffset.x, this.pos.getY() + posOffset.y,
                this.pos.getZ() + posOffset.z);
        return world.rayTraceBlocks(new RayTraceContext(posVec, target.add(targetOffset),
                RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, null));
    }

    private float calculateYaw(Vector3d target) {
        Vector3d diffPos = new Vector3d(this.pos.getX() + .5f, this.pos.getY(), this.pos.getZ() + .5f).subtract(target);
        return (float) MathHelper.wrapDegrees(
                MathHelper.atan2(-diffPos.z, -diffPos.x) * (double) (180F / (float) Math.PI) + getYawOffset());
    }

    private float calculatePitch(Vector3d target) {
        Vector3d diffPos = new Vector3d(this.pos.getX() + .5f, this.pos.getY(), this.pos.getZ() + .5f).subtract(target);
        double horizComponent = MathHelper.sqrt((diffPos.z * diffPos.z) + (diffPos.x * diffPos.x));
        float pitch = (float) MathHelper
                .wrapDegrees((MathHelper.atan2(-horizComponent, diffPos.y) * (double) (180F / (float) Math.PI) + 90));
        return pitch;
    }

    private void playSoundEffect(SoundEvent soundEvent) {
        if (world == null)
            return;
        PlayerEntity player = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 64, true);
        if (player == null)
            return;
        Vector3i playerPos = new Vector3i(player.getPosition().getX(), player.getPosition().getY(),
                player.getPosition().getZ());
        world.playSound(player, this.pos, soundEvent, SoundCategory.BLOCKS,
                (float) (1 / Math.sqrt((this.pos.distanceSq(playerPos)))), 1);
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
        AnimationBuilder animationBuilder = new AnimationBuilder();
        controller.setAnimation(animationBuilder);
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        AnimationController controller = new AnimationController(this, "controller", 0, this::predicate);
        animationData.addAnimationController(controller);
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}