package net.nickhunter.mc.ottselturrets.blocks.tile;

import static net.minecraft.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.nickhunter.mc.ottselturrets.blocks.IOptical;
import net.nickhunter.mc.ottselturrets.registry.TileRegistry;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

@SuppressWarnings("rawtypes")
public class ProjectorTileEntity extends TileEntity implements ITickableTileEntity, IAnimatable, IOptical {

    public static final String IDLE_ANIMATION = "animation.laser_projector.idle";
    public static final String LEAVE_IDLE_ANIMATION = "animation.laser_projector.leave_idle";
    public static final String FIRE_BEAM_ANIMATION = "animation.laser_projector.fire_beam";
    public static final String BEAM_ANIMATION = "animation.laser_projector.beam";
    public static final String RETRACT_BEAM_ANIMATION = "animation.laser_projector.retract_beam";
    public static final String ROTATE_ANIMATION = "animation.laser_projector.rotate_arm";
    public static final String PAIR_ANIMATION = "animation.laser_projector.pairing";

    public static final int RANGE = 256;
    public static final float pitchMax = 45;

    private final AnimationFactory factory = new AnimationFactory(this);

    private static ProjectorTileEntity pairingProjectorServer;
    private static ProjectorTileEntity pairingProjectorClient;

    private ProjectorTileEntity partnerProjector;

    private int lastPlayerTicks;

    private float yawPrev;
    private float pitchPrev;

    private float yaw;
    private float pitch;

    private float beamLength;

    public enum ProjectorState {
        IDLE, PAIRING, PAIRED_IDLE, PAIRED_TX, PAIRED_RX
    }

    private ProjectorState projectorState = ProjectorState.IDLE;

    private PowerType powerType = PowerType.OFF;

    public boolean powered, northOptical, eastOptical, southOptical, westOptical;

    public ProjectorTileEntity() {
        super(TileRegistry.PROJECTOR.get());
    }

    public ProjectorTileEntity getPartnerProjector() {
        return partnerProjector;
    }

    public ProjectorState getProjectorState() {
        return projectorState;
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

    public void setProjectorState(ProjectorState projectorState) {
        this.projectorState = projectorState;
    }

    public void setPartnerProjector(ProjectorTileEntity partnerProjector) {
        this.partnerProjector = partnerProjector;
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
        PowerType neighborPower = checkForOpticalNeighbors();
        if (world.isBlockPowered(pos)) {
            powerType = PowerType.ENDPOINT;
            powered = true;
        } else if (projectorState == ProjectorState.PAIRED_RX) {
            powerType = PowerType.SOURCE;
            powered = true;
        } else if (neighborPower == PowerType.SOURCE) {
            powerType = PowerType.PASSTHROUGH;
            powered = true;
        } else {
            powerType = PowerType.OFF;
            powered = false;
        }
        switch (projectorState) {
            case IDLE:
                break;
            case PAIRED_RX:
                break;
            case PAIRED_IDLE:
            case PAIRED_TX:
                if (powered) {
                    projectorState = ProjectorState.PAIRED_TX;
                    partnerProjector.setProjectorState(ProjectorState.PAIRED_RX);
                } else {
                    if (projectorState == ProjectorState.PAIRED_TX) {
                        projectorState = ProjectorState.PAIRED_IDLE;
                        partnerProjector.setProjectorState(ProjectorState.PAIRED_IDLE);
                    }
                }
                break;
            case PAIRING:
                break;
            default:
                break;
        }
    }

    protected void serverTick() {

    }

    protected void clientTick() {

    }

    protected PowerType checkForOpticalNeighbors() {

        PowerType neighborsPowered = PowerType.OFF;

        TileEntity north = world.getTileEntity(pos.add(0, 0, -1));
        TileEntity south = world.getTileEntity(pos.add(0, 0, 1));
        TileEntity east = world.getTileEntity(pos.add(1, 0, 0));
        TileEntity west = world.getTileEntity(pos.add(-1, 0, 0));

        if (north != null && north instanceof IOptical) {
            northOptical = true;
            switch (((IOptical) north).getPowerType()) {
                case PASSTHROUGH:
                    if (neighborsPowered != PowerType.SOURCE)
                        neighborsPowered = PowerType.PASSTHROUGH;
                    break;
                case SOURCE:
                    neighborsPowered = PowerType.SOURCE;
                    break;
                case ENDPOINT:
                case OFF:
                default:
                    break;
            }
        } else {
            northOptical = false;
        }
        if (south != null && south instanceof IOptical) {
            southOptical = true;
            switch (((IOptical) south).getPowerType()) {
                case PASSTHROUGH:
                    if (neighborsPowered != PowerType.SOURCE)
                        neighborsPowered = PowerType.PASSTHROUGH;
                    break;
                case SOURCE:
                    neighborsPowered = PowerType.SOURCE;
                    break;
                case ENDPOINT:
                case OFF:
                default:
                    break;
            }
        } else {
            southOptical = false;
        }
        if (east != null && east instanceof IOptical) {
            eastOptical = true;
            switch (((IOptical) east).getPowerType()) {
                case PASSTHROUGH:
                    if (neighborsPowered != PowerType.SOURCE)
                        neighborsPowered = PowerType.PASSTHROUGH;
                    break;
                case SOURCE:
                    neighborsPowered = PowerType.SOURCE;
                    break;
                case ENDPOINT:
                case OFF:
                default:
                    break;
            }
        } else {
            eastOptical = false;
        }
        if (west != null && west instanceof IOptical) {
            westOptical = true;
            switch (((IOptical) west).getPowerType()) {
                case PASSTHROUGH:
                    if (neighborsPowered != PowerType.SOURCE)
                        neighborsPowered = PowerType.PASSTHROUGH;
                    break;
                case SOURCE:
                    neighborsPowered = PowerType.SOURCE;
                    break;
                case ENDPOINT:
                case OFF:
                default:
                    break;
            }
        } else {
            westOptical = false;
        }
        return neighborsPowered;
    }

    public void onBlockDestroyed(PlayerEntity player) {
        if (partnerProjector == null)
            return;
        partnerProjector.setProjectorState(ProjectorState.IDLE);
        partnerProjector.setPartnerProjector(null);
    }

    public void onBlockActivated(PlayerEntity player, BlockRayTraceResult hit, Hand handIn) {
        if (player.ticksExisted - lastPlayerTicks < 15)
            return;
        lastPlayerTicks = player.ticksExisted;

        if (player.isSneaking()) {
            switch (projectorState) {
                case IDLE:
                    if (pairingProjectorServer != null && !world.isRemote) {
                        if (pairingProjectorServer.getProjectorState() == ProjectorState.PAIRING)
                            pairingProjectorServer.setProjectorState(ProjectorState.IDLE);
                    }
                    if (pairingProjectorClient != null && world.isRemote) {
                        if (pairingProjectorClient.getProjectorState() == ProjectorState.PAIRING)
                            pairingProjectorClient.setProjectorState(ProjectorState.IDLE);
                    }
                    if (!world.isRemote) {
                        pairingProjectorServer = this;
                    }
                    if (world.isRemote) {
                        pairingProjectorClient = this;
                    }
                    projectorState = ProjectorState.PAIRING;
                    break;
                case PAIRED_IDLE:
                case PAIRED_RX:
                case PAIRED_TX:
                    partnerProjector.setProjectorState(ProjectorState.IDLE);
                    partnerProjector.setPartnerProjector(null);
                    partnerProjector = null;
                    projectorState = ProjectorState.IDLE;
                    break;
                case PAIRING:
                    projectorState = ProjectorState.IDLE;
                    break;
                default:
                    break;
            }
        } else {
            switch (projectorState) {
                case IDLE:
                    if (world.isRemote && pairingProjectorClient != null) {
                        partnerProjector = pairingProjectorClient;
                    }
                    if (!world.isRemote && pairingProjectorServer != null) {
                        partnerProjector = pairingProjectorServer;
                    }
                    if (partnerProjector == null || partnerProjector.getProjectorState() != ProjectorState.PAIRING)
                        return;
                    partnerProjector.setPartnerProjector(this);
                    Vector3d partnerPos = new Vector3d(partnerProjector.getPos().getX(),
                            partnerProjector.getPos().getY(), partnerProjector.getPos().getZ());
                    Vector3d thisPos = new Vector3d(getPos().getX(), getPos().getY(), getPos().getZ());
                    setYawToTarget(calculateYaw(partnerPos));
                    setPitchToTarget(calculatePitch(partnerPos));
                    beamLength = (float) thisPos.distanceTo(partnerPos) - .4f;
                    partnerProjector.beamLength = beamLength;
                    partnerProjector.setYawToTarget(partnerProjector.calculateYaw(thisPos));
                    partnerProjector.setPitchToTarget(partnerProjector.calculatePitch(thisPos));
                    projectorState = ProjectorState.PAIRED_IDLE;
                    partnerProjector.setProjectorState(ProjectorState.PAIRED_IDLE);
                    break;
                case PAIRED_IDLE:
                    break;
                case PAIRED_RX:
                    break;
                case PAIRED_TX:
                    break;
                case PAIRING:
                default:
                    break;
            }
        }
    }

    protected final RayTraceResult rayTraceToTarget(Vector3d target) {
        Vector3d posVec = new Vector3d(this.pos.getX(), this.pos.getY(), this.pos.getZ());
        return world.rayTraceBlocks(new RayTraceContext(posVec, target, RayTraceContext.BlockMode.COLLIDER,
                RayTraceContext.FluidMode.NONE, null));
    }

    private float calculateYaw(Vector3d target) {
        Vector3d diffPos = new Vector3d(this.pos.getX(), this.pos.getY(), this.pos.getZ()).subtract(target);
        return (float) MathHelper.wrapDegrees(
                MathHelper.atan2(-diffPos.z, -diffPos.x) * (double) (180F / (float) Math.PI) + getYawOffset());
    }

    private float calculatePitch(Vector3d target) {
        Vector3d diffPos = new Vector3d(this.pos.getX(), this.pos.getY(), this.pos.getZ()).subtract(target);
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
        AnimationBuilder animationBuilder = new AnimationBuilder();
        switch (projectorState) {
            case IDLE:
            default:
                animationBuilder.addAnimation(IDLE_ANIMATION, true);
                break;
            case PAIRED_IDLE:
                animationBuilder.addAnimation(ROTATE_ANIMATION, true);
                break;
            case PAIRED_RX:
                animationBuilder.addAnimation(ROTATE_ANIMATION, false);
                animationBuilder.addAnimation(ROTATE_ANIMATION, true);
                break;
            case PAIRED_TX:
                animationBuilder.addAnimation(FIRE_BEAM_ANIMATION, false);
                animationBuilder.addAnimation(BEAM_ANIMATION, true);
                break;
            case PAIRING:
                // animationBuilder.addAnimation(LEAVE_IDLE_ANIMATION, false);
                animationBuilder.addAnimation(PAIR_ANIMATION, true);
                break;

        }
        event.getController().setAnimation(animationBuilder);
        return PlayState.CONTINUE;
    }

    @Override
    public PowerType getPowerType() {
        return powerType;
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