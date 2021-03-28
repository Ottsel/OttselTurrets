package net.nickhunter.mc.ottselturrets.blocks.tile;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.blocks.IOptical;
import net.nickhunter.mc.ottselturrets.blocks.LaserNodeBlock;
import net.nickhunter.mc.ottselturrets.registry.TileRegistry;
import net.nickhunter.mc.ottselturrets.util.NodeState;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

@SuppressWarnings("rawtypes")
public class LaserNodeTileEntity extends AnimatedTileEntity implements ITickableTileEntity, IOptical {

    public static final String IDLE_ANIMATION = "animation.laser_projector.idle";
    public static final String FIRE_BEAM_ANIMATION = "animation.laser_projector.fire_beam";
    public static final String BEAM_ANIMATION = "animation.laser_projector.beam";
    public static final String ROTATE_ANIMATION = "animation.laser_projector.rotate_arm";
    public static final String PAIR_ANIMATION = "animation.laser_projector.pairing";
    public static final String FIRE_BEAM_REMOTE_ANIMATION = "animation.laser_projector.fire_beam_remote";
    public static final String RECEIVE_BEAM_ANIMATION = "animation.laser_projector.receive_beam";

    public static final float PITCH_MAX = 55;

    private final AnimationFactory factory = new AnimationFactory(this);

    private static LaserNodeTileEntity pairingNode;

    private BlockPos partnerNodePos;
    private BlockPos obstruction;

    private int lastUseTicks;

    private float yaw, pitch;
    private float beamLength;
    private float beamStart;

    public boolean northOptical, eastOptical, southOptical, westOptical;

    public LaserNodeTileEntity() {
        super(TileRegistry.LASER_NODE.get());
    }

    public void setPartnerNode(BlockPos partnerNodePos) {
        if (partnerNodePos != this.partnerNodePos) {
            this.partnerNodePos = partnerNodePos;
            this.markDirty();
        }
    }

    public void setObstruction(BlockPos obstruction) {
        this.obstruction = obstruction;
        BlockState blockState = world.getBlockState(pos);
        if (obstruction == null) {
            if (blockState.get(LaserNodeBlock.OBSTRUCTED)) {
                world.setBlockState(pos, blockState.with(LaserNodeBlock.OBSTRUCTED, false), 2);
            }
        } else {
            if (!blockState.get(LaserNodeBlock.OBSTRUCTED)) {
                world.setBlockState(pos, blockState.with(LaserNodeBlock.OBSTRUCTED, true), 2);
            }
        }
        world.notifyBlockUpdate(pos, blockState, world.getBlockState(pos), 2);
        markDirty();
    }

    public float getPitchToTarget() {
        return pitch;
    }

    public float getYawToTarget() {
        return yaw;
    }

    public float getBeamLength() {
        return beamLength;
    }

    public float getBeamStart() {
        return beamStart;
    }

    public void setPitchToTarget(float pitch) {
        if (pitch > PITCH_MAX)
            pitch = PITCH_MAX;
        if (pitch < -PITCH_MAX)
            pitch = -PITCH_MAX;
        this.pitch = pitch;
    }

    public void setYawToTarget(float yawToTarget) {
        this.yaw = yawToTarget;
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        OttselTurrets.LOGGER.debug(pos + " write");
        if (partnerNodePos != null) {
            nbt.putBoolean("has_partner", true);
            nbt.putInt("partner_x", partnerNodePos.getX());
            nbt.putInt("partner_y", partnerNodePos.getY());
            nbt.putInt("partner_z", partnerNodePos.getZ());
            if (obstruction != null) {
                nbt.putBoolean("has_obstruction", true);
                nbt.putDouble("obstruction_x", obstruction.getX());
                nbt.putDouble("obstruction_y", obstruction.getY());
                nbt.putDouble("obstruction_z", obstruction.getZ());
            }

        }
        return super.write(nbt);
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        OttselTurrets.LOGGER.debug(pos + " read");
        super.read(state, nbt);
        if (nbt.getBoolean("has_partner")) {
            setPartnerNode(new BlockPos(nbt.getInt("partner_x"), nbt.getInt("partner_y"), nbt.getInt("partner_z")));
        }
    }

    // Server side chunk load
    @Override
    public CompoundNBT getUpdateTag() {
        OttselTurrets.LOGGER.debug(pos + " getUpdateTag");
        CompoundNBT nbt = super.getUpdateTag();
        return write(nbt);
    }

    // Client side chunk load
    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT nbt) {
        OttselTurrets.LOGGER.debug(pos + " handleUpdateTag");
        super.handleUpdateTag(state, nbt);
        loadNBT(nbt);
        updateClient();
    }

    // Server side block update
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        OttselTurrets.LOGGER.debug(pos + " getUpdatePacket");
        CompoundNBT nbt = write(new CompoundNBT());
        return new SUpdateTileEntityPacket(getPos(), -1, nbt);
    }

    // Client side block update
    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        super.onDataPacket(net, pkt);
        OttselTurrets.LOGGER.debug(pos + " onDataPacket");
        loadNBT(pkt.getNbtCompound());
        updateClient();
    }

    // Unpackage NBT data
    private void loadNBT(CompoundNBT nbt) {
        if (nbt.getBoolean("has_partner")) {
            setPartnerNode(new BlockPos(nbt.getInt("partner_x"), nbt.getInt("partner_y"), nbt.getInt("partner_z")));
            if (nbt.getBoolean("has_obstruction")) {
                setObstruction(new BlockPos(nbt.getDouble("obstruction_x"), nbt.getDouble("obstruction_y"),
                        nbt.getDouble("obstruction_z")));
            }
        }
    }

    // Called when block is placed next to this block (Server)
    public void neighborUpdate() {
        updateServer();
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            serverTick();
        } else {
            clientTick();
        }
    }

    private boolean obstructed;

    private void serverTick() {
        if (partnerNodePos != null) {
            switch (world.getBlockState(pos).get(LaserNodeBlock.NODE_STATE)) {
                case PAIRED_RX:
                    if (checkForObstruction(getHeadOffset(partnerNodePos), getHeadOffset(pos))) {
                        updateServer();
                    }
                    break;
                case PAIRED_RX_OBSTRUCTED:
                    if (!checkForObstruction(getHeadOffset(partnerNodePos), getHeadOffset(pos))) {
                        updateServer();
                    }
                    break;
                case IDLE:
                case PAIRED_IDLE:
                case PAIRED_TX:
                    if (obstructed != checkForObstruction(getHeadOffset(pos), getHeadOffset(partnerNodePos))) {
                        obstructed = !obstructed;
                        updateServer();
                    }
                case PAIRING:
                default:
                    break;
            }
        }
    }

    private void clientTick() {
        UpdateNeighborConnections();
        if (partnerNodePos != null
                && (getState() == NodeState.PAIRED_RX_OBSTRUCTED || getState() == NodeState.PAIRED_RX))
            checkForObstruction(getHeadOffset(partnerNodePos), getHeadOffset(pos));
    }

    @SuppressWarnings({ "deprecation" })
    private void updateServer() {
        if (partnerNodePos == null) {
            setState(NodeState.IDLE);
            return;
        }
        OttselTurrets.LOGGER.debug(pos + " updateServer: " + getState());
        switch (getState()) {
            case PAIRED_IDLE:
                if (world.isBlockPowered(pos)) {
                    setState(NodeState.PAIRED_TX);
                    if (checkForObstruction(getHeadOffset(pos), getHeadOffset(partnerNodePos))) {
                        setState(partnerNodePos, NodeState.PAIRED_RX_OBSTRUCTED);
                    } else {
                        setState(partnerNodePos, NodeState.PAIRED_RX);
                    }
                } else {
                    setState(partnerNodePos, NodeState.PAIRED_IDLE);
                }
                break;
            case PAIRED_RX:
                if (world.isBlockPowered(partnerNodePos)) {
                    if (checkForObstruction(getHeadOffset(partnerNodePos), getHeadOffset(pos))) {
                        setState(NodeState.PAIRED_RX_OBSTRUCTED);
                    }
                } else {
                    setState(NodeState.PAIRED_IDLE);
                }
                break;
            case PAIRED_RX_OBSTRUCTED:
                if (world.isBlockPowered(partnerNodePos)) {
                    if (!checkForObstruction(getHeadOffset(partnerNodePos), getHeadOffset(pos))) {
                        setState(NodeState.PAIRED_RX);
                    }
                } else {
                    setState(NodeState.PAIRED_IDLE);
                }
                break;
            case PAIRED_TX:
                if (world.isBlockPowered(pos)) {
                    NodeState state;
                    if (checkForObstruction(getHeadOffset(pos), getHeadOffset(partnerNodePos))) {
                        state = NodeState.PAIRED_RX_OBSTRUCTED;
                    } else {
                        state = NodeState.PAIRED_RX;
                    }
                    if (world.getBlockState(partnerNodePos).get(LaserNodeBlock.NODE_STATE) != state) {
                        setState(partnerNodePos, state);
                        world.notifyNeighborsOfStateChange(partnerNodePos,
                                world.getBlockState(partnerNodePos).getBlock());
                    }

                } else {
                    setState(NodeState.PAIRED_IDLE);
                    setState(partnerNodePos, NodeState.PAIRED_IDLE);
                    if (world.isBlockLoaded(partnerNodePos)) {
                        LaserNodeTileEntity laserNode = getTileEntityFromPos(partnerNodePos);
                        if (laserNode != null) {
                            laserNode.updateServer();
                        }
                    }
                }
                break;
            case PAIRING:
            case IDLE:
            default:
                break;
        }
    }

    private void updateClient() {
        OttselTurrets.LOGGER.debug(pos + "updateClient: " + getState());
        switch (getState()) {
            case PAIRED_IDLE:
            case PAIRED_RX:
                pointTo(partnerNodePos);
                break;
            case PAIRED_RX_OBSTRUCTED:
                pointTo(partnerNodePos);
                break;
            case PAIRED_TX:
                pointTo(partnerNodePos);
                checkForObstruction(getHeadOffset(pos), getHeadOffset(partnerNodePos));
                break;
            case PAIRING:
            case IDLE:
            default:
                break;
        }
    }

    private void pointTo(BlockPos targetPos) {
        OttselTurrets.LOGGER.debug(pos + " pointTo: " + targetPos);
        setYawToTarget(calculateYaw(getHeadOffset(targetPos)));
        setPitchToTarget(calculatePitch(getHeadOffset(targetPos)));
    }

    public NodeState getState() {
        if (world != null) {
            return world.getBlockState(pos).get(LaserNodeBlock.NODE_STATE);
        }
        OttselTurrets.LOGGER.error(pos + " World null when trying to retrieve state");
        return NodeState.IDLE;
    }

    private void setState(NodeState nodeState) {
        setState(pos, nodeState);
    }

    private void setState(BlockPos pos, NodeState nodeState) {
        if (world.isRemote)
            return;
        if (world.getBlockState(pos).get(LaserNodeBlock.NODE_STATE) != nodeState) {
            world.setBlockState(pos, world.getBlockState(pos).with(LaserNodeBlock.NODE_STATE, nodeState), 2);
            world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock());
        }

    }

    public void onBlockDestroyed() {
        switch (getState()) {
            case IDLE:
            default:
                break;
            case PAIRED_IDLE:
            case PAIRED_RX:
            case PAIRED_RX_OBSTRUCTED:
            case PAIRED_TX:
                unpair();
                break;
            case PAIRING:
                pairingNode = null;
                break;
        }
    }

    public void onBlockActivated(PlayerEntity player, BlockRayTraceResult hit, Hand handIn) {
        if (world.isRemote)
            return;
        if (player.ticksExisted - lastUseTicks < 15)
            return;
        lastUseTicks = player.ticksExisted;

        if (player.isSneaking()) {
            switch (getState()) {
                case IDLE:
                    if (pairingNode != null) {
                        if (pairingNode.getState() == NodeState.PAIRING)
                            pairingNode.setState(NodeState.IDLE);
                    }
                    pairingNode = this;
                    setState(NodeState.PAIRING);
                    break;
                case PAIRED_IDLE:
                case PAIRED_RX:
                case PAIRED_TX:
                case PAIRED_RX_OBSTRUCTED:
                    onBlockDestroyed();
                    break;
                case PAIRING:
                    pairingNode = null;
                    setState(NodeState.IDLE);
                    break;
                default:
                    break;
            }
        } else {
            switch (getState()) {
                case IDLE:
                    if (pairingNode != null) {
                        if (world.getDimensionKey() == pairingNode.getWorld().getDimensionKey()) {
                            if (Math.abs(calculatePitch(getHeadOffset(pairingNode.pos))) > PITCH_MAX)
                                break;
                            setPartnerNode(pairingNode.pos);
                            pair();
                            pairingNode = null;
                        }
                    }
                    break;
                case PAIRED_IDLE:
                case PAIRED_RX:
                case PAIRED_TX:
                case PAIRED_RX_OBSTRUCTED:
                case PAIRING:
                default:
                    break;
            }
        }
    }

    private boolean checkForObstruction(Vector3d origin, Vector3d target) {
        RayTraceResult result = rayTraceToTarget(origin, target);
        if (result.getType() != Type.MISS) {
            beamLength = (float) origin.distanceTo(result.getHitVec()) - .15f;
            beamStart = (float) target.distanceTo(origin) - .37f;
            if (!world.isRemote) {
                BlockPos obs = new BlockPos(result.getHitVec());
                if (obstruction == null || !(new Vector3i(obstruction.getX(), obstruction.getY(), obstruction.getZ())
                        .equals(new Vector3i(obs.getX(), obs.getY(), obs.getZ())))) {
                    setObstruction(obs);
                }
            }
            return true;
        } else {
            beamLength = (float) origin.distanceTo(target) - .4f;
            beamStart = beamLength;
            if (!world.isRemote) {
                if (obstruction != null)
                    obstruction = null;
            }
            return false;
        }
    }

    private void pair() {
        LaserNodeTileEntity partnerTileEntity = getTileEntityFromPos(partnerNodePos);
        partnerTileEntity.setPartnerNode(this.pos);
        if (world.isBlockPowered(partnerNodePos)) {
            if (checkForObstruction(getHeadOffset(partnerNodePos), getHeadOffset(pos))) {
                setState(NodeState.PAIRED_RX_OBSTRUCTED);
            } else {
                setState(NodeState.PAIRED_RX);
            }
            setState(partnerNodePos, NodeState.PAIRED_TX);
        } else {
            if (world.getBlockState(pos).get(LaserNodeBlock.RECEIVING_POWER)) {
                setState(NodeState.PAIRED_TX);
                if (checkForObstruction(getHeadOffset(pos), getHeadOffset(partnerNodePos))) {
                    setState(partnerNodePos, NodeState.PAIRED_RX_OBSTRUCTED);
                } else {
                    setState(partnerNodePos, NodeState.PAIRED_RX);
                }
            } else {
                setState(NodeState.PAIRED_IDLE);
                setState(partnerNodePos, NodeState.PAIRED_IDLE);
            }
        }
    }

    private void unpair() {
        if (!world.isRemote) {
            if (partnerNodePos != null) {
                setState(partnerNodePos, NodeState.IDLE);
                getTileEntityFromPos(partnerNodePos).setPartnerNode(null);
            }
        }
    }

    private LaserNodeTileEntity getTileEntityFromPos(BlockPos pos) {
        if (world != null && pos != null) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof LaserNodeTileEntity) {
                return (LaserNodeTileEntity) tileEntity;
            }
        }
        return null;
    }

    public void UpdateNeighborConnections() {
        if (world == null)
            return;

        TileEntity north = world.getTileEntity(pos.add(0, 0, -1));
        TileEntity south = world.getTileEntity(pos.add(0, 0, 1));
        TileEntity east = world.getTileEntity(pos.add(1, 0, 0));
        TileEntity west = world.getTileEntity(pos.add(-1, 0, 0));

        if (north != null && north instanceof IOptical) {
            northOptical = true;
            ((IOptical) north).SetNeighborConnection(Direction.SOUTH, true);
        } else {
            northOptical = false;
        }
        if (south != null && south instanceof IOptical) {
            southOptical = true;
            ((IOptical) south).SetNeighborConnection(Direction.NORTH, true);
        } else {
            southOptical = false;
        }
        if (east != null && east instanceof IOptical) {
            eastOptical = true;
            ((IOptical) east).SetNeighborConnection(Direction.WEST, true);
        } else {
            eastOptical = false;
        }
        if (west != null && west instanceof IOptical) {
            westOptical = true;
            ((IOptical) west).SetNeighborConnection(Direction.EAST, true);
        } else {
            westOptical = false;
        }
    }

    public void SetNeighborConnection(Direction direction, boolean enabled) {
        switch (direction) {
            case WEST:
                westOptical = enabled;
                break;
            case EAST:
                eastOptical = enabled;
                break;
            case NORTH:
                northOptical = enabled;
                break;
            case SOUTH:
                southOptical = enabled;
                break;
            case DOWN:
            case UP:
            default:
                OttselTurrets.LOGGER.error(pos + " Could not " + (enabled ? "set" : "unset")
                        + " neighbor connection on side: " + direction);
                break;

        }
    }

    private RayTraceResult rayTraceToTarget(Vector3d origin, Vector3d target) {
        return world.rayTraceBlocks(new RayTraceContext(origin, target, RayTraceContext.BlockMode.VISUAL,
                RayTraceContext.FluidMode.NONE, null));
    }

    private float calculateYaw(Vector3d target) {
        Vector3d diffPos = getHeadOffset(pos).subtract(target);
        return (float) MathHelper
                .wrapDegrees(MathHelper.atan2(-diffPos.z, -diffPos.x) * (double) (180F / (float) Math.PI) + 90);
    }

    private float calculatePitch(Vector3d target) {
        Vector3d diffPos = getHeadOffset(pos).subtract(target);
        double horizComponent = MathHelper.sqrt((diffPos.z * diffPos.z) + (diffPos.x * diffPos.x));
        float pitch = (float) MathHelper
                .wrapDegrees((MathHelper.atan2(-horizComponent, diffPos.y) * (double) (180F / (float) Math.PI) + 90));
        return pitch;
    }

    private static Vector3d getHeadOffset(BlockPos pos) {
        return new Vector3d(pos.getX() + .5, pos.getY() + .6875, pos.getZ() + .5);
    }

    private boolean isLoadedOnClient(BlockPos pos) {
        if (world != null && pos != null) {
            if (world.getBlockState(pos).getBlock().getRegistryName() != Blocks.VOID_AIR.getRegistryName()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return Double.POSITIVE_INFINITY;
    }

    private <E extends AnimatedTileEntity> PlayState predicate(AnimationEvent<E> event) {
        AnimationBuilder animationBuilder = new AnimationBuilder();
        switch (getState()) {
            case IDLE:
            default:
                animationBuilder.addAnimation(IDLE_ANIMATION, true);
                break;
            case PAIRED_IDLE:
                animationBuilder.addAnimation(ROTATE_ANIMATION, true);
                break;
            case PAIRED_RX:
                if (partnerNodePos != null && !isLoadedOnClient(partnerNodePos)) {
                    animationBuilder.addAnimation(FIRE_BEAM_REMOTE_ANIMATION, false);
                    animationBuilder.addAnimation(RECEIVE_BEAM_ANIMATION, true);
                } else {
                    animationBuilder.addAnimation(ROTATE_ANIMATION, true);
                }
                break;
            case PAIRED_RX_OBSTRUCTED:
                if (partnerNodePos != null && !isLoadedOnClient(partnerNodePos) && obstruction == null
                        || (obstruction != null && isLoadedOnClient(obstruction))) {
                    animationBuilder.addAnimation(FIRE_BEAM_REMOTE_ANIMATION, false);
                    animationBuilder.addAnimation(RECEIVE_BEAM_ANIMATION, true);
                } else {
                    animationBuilder.addAnimation(ROTATE_ANIMATION, true);
                }
                break;
            case PAIRED_TX:
                if (partnerNodePos != null)
                    checkForObstruction(getHeadOffset(pos), getHeadOffset(partnerNodePos));
                animationBuilder.addAnimation(FIRE_BEAM_ANIMATION, false);
                animationBuilder.addAnimation(BEAM_ANIMATION, true);
                break;
            case PAIRING:
                animationBuilder.addAnimation(PAIR_ANIMATION, true);
                break;

        }
        event.getController().setAnimation(animationBuilder);
        return PlayState.CONTINUE;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void registerControllers(AnimationData animationData) {
        AnimationController<?> controller = new AnimationController(this, "controller", 0, this::predicate);
        animationData.addAnimationController(controller);
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}