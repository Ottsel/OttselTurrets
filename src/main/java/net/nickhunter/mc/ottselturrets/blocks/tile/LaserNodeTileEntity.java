package net.nickhunter.mc.ottselturrets.blocks.tile;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
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
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

@SuppressWarnings("rawtypes")
public class LaserNodeTileEntity extends TileEntity implements ITickableTileEntity, IAnimatable, IOptical {

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

    private static LaserNodeTileEntity pairingNode;

    private BlockPos partnerNode;

    private int lastPlayerTicks;

    private float yawPrev, pitchPrev;
    private float yaw, pitch;

    private float beamLength;

    public boolean northOptical, eastOptical, southOptical, westOptical;

    public LaserNodeTileEntity() {
        super(TileRegistry.LASER_NODE.get());
    }

    public static LaserNodeTileEntity getPairingNode() {
        return pairingNode;
    }

    public static void setPairingNode(LaserNodeTileEntity pairingNode) {
        LaserNodeTileEntity.pairingNode = pairingNode;
    }

    public BlockPos getPartnerNode() {
        return partnerNode;
    }

    public void setPartnerNode(BlockPos partnerNode) {
        if (partnerNode != this.partnerNode) {
            this.partnerNode = partnerNode;
            this.markDirty();
        }
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
    public CompoundNBT write(CompoundNBT nbt) {
        OttselTurrets.LOGGER.debug(pos + " write");
        if (partnerNode != null) {
            nbt.putBoolean("has_partner", true);
            nbt.putInt("partner_x", partnerNode.getX());
            nbt.putInt("partner_y", partnerNode.getY());
            nbt.putInt("partner_z", partnerNode.getZ());
        }
        return super.write(nbt);
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);
        OttselTurrets.LOGGER.info(pos + " read");
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
    public void loadNBT(CompoundNBT nbt) {
        if (nbt.getBoolean("has_partner")) {
            setPartnerNode(new BlockPos(nbt.getInt("partner_x"), nbt.getInt("partner_y"), nbt.getInt("partner_z")));
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

    private void serverTick() {
        if (partnerNode != null) {
            switch (world.getBlockState(pos).get(LaserNodeBlock.NODE_STATE)) {
                case PAIRED_RX:
                    if (checkForObstruction(getHeadOffset(partnerNode), getHeadOffset(pos))) {
                        updateServer();
                    }
                    break;
                case PAIRED_RX_OBSTRUCTED:
                    OttselTurrets.LOGGER.info(checkForObstruction(getHeadOffset(partnerNode), getHeadOffset(pos)));
                    if (!checkForObstruction(getHeadOffset(partnerNode), getHeadOffset(pos))) {
                        updateServer();
                    }
                    break;
                case IDLE:
                case PAIRED_IDLE:
                case PAIRED_TX:
                case PAIRING:
                default:
                    break;
            }
        }
    }

    private void clientTick() {
        if (partnerNode != null) {
            switch (world.getBlockState(pos).get(LaserNodeBlock.NODE_STATE)) {
                case PAIRED_RX:
                case PAIRED_RX_OBSTRUCTED:
                    checkForObstruction(getHeadOffset(partnerNode), getHeadOffset(pos));
                    break;
                case PAIRED_TX:
                    checkForObstruction(getHeadOffset(pos), getHeadOffset(partnerNode));
                    break;
                case PAIRED_IDLE:
                case PAIRING:
                case IDLE:
                default:
                    break;

            }
        }
    }

    private void updateServer() {
        OttselTurrets.LOGGER.info(pos + " updateServer: " + getState());
        switch (getState()) {
            case PAIRED_IDLE:
                if (world.isBlockPowered(pos)) {
                    setState(NodeState.PAIRED_TX);
                    if (checkForObstruction(getHeadOffset(pos), getHeadOffset(partnerNode))) {
                        setState(partnerNode, NodeState.PAIRED_RX_OBSTRUCTED);
                    } else {
                        setState(partnerNode, NodeState.PAIRED_RX);
                    }
                } else {
                    setState(partnerNode, NodeState.PAIRED_IDLE);
                }
                break;
            case PAIRED_RX:
                if (world.isBlockPowered(partnerNode)) {
                    if (checkForObstruction(getHeadOffset(partnerNode), getHeadOffset(pos))) {
                        setState(NodeState.PAIRED_RX_OBSTRUCTED);
                    }
                } else {
                    setState(NodeState.PAIRED_IDLE);
                }
                break;
            case PAIRED_RX_OBSTRUCTED:
                if (world.isBlockPowered(partnerNode)) {
                    if (!checkForObstruction(getHeadOffset(partnerNode), getHeadOffset(pos))) {
                        setState(NodeState.PAIRED_RX);
                    }
                } else {
                    setState(NodeState.PAIRED_IDLE);
                }
                break;
            case PAIRED_TX:
                if (world.isBlockPowered(pos)) {
                    if (checkForObstruction(getHeadOffset(pos), getHeadOffset(partnerNode))) {
                        setState(partnerNode, NodeState.PAIRED_RX_OBSTRUCTED);
                    } else {
                        setState(partnerNode, NodeState.PAIRED_RX);
                    }
                } else {
                    setState(NodeState.PAIRED_IDLE);
                    setState(partnerNode, NodeState.PAIRED_IDLE);
                    if (world.isBlockLoaded(partnerNode)) {
                        LaserNodeTileEntity laserNode = getTileEntityFromPos(partnerNode);
                        if (laserNode != null) {
                            OttselTurrets.LOGGER.info("hello");
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
        OttselTurrets.LOGGER.info(pos + "updateClient: " + getState());
        switch (getState()) {
            case PAIRED_IDLE:
            case PAIRED_RX:
                pointTo(partnerNode);
                pointTo(partnerNode);
                break;
            case PAIRED_RX_OBSTRUCTED:
                pointTo(partnerNode);
                checkForObstruction(getHeadOffset(partnerNode), getHeadOffset(pos));
                break;
            case PAIRED_TX:
                pointTo(partnerNode);
                checkForObstruction(getHeadOffset(pos), getHeadOffset(partnerNode));
                break;
            case PAIRING:
            case IDLE:
            default:
                break;
        }
    }

    private void pointTo(BlockPos targetPos) {
        OttselTurrets.LOGGER.info(pos + "pointTo: " + targetPos);
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
            world.setBlockState(pos, world.getBlockState(pos).with(LaserNodeBlock.NODE_STATE, nodeState)
                    .with(BlockStateProperties.POWERED, (nodeState == NodeState.PAIRED_RX)), 2);
            world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock());
        }
    }

    public void onBlockDestroyed(PlayerEntity player) {
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
        if (player.ticksExisted - lastPlayerTicks < 15)
            return;
        lastPlayerTicks = player.ticksExisted;

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
                    onBlockDestroyed(player);
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
                        setPartnerNode(pairingNode.pos);
                        pairingNode = null;
                    }
                    pair();
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
            return true;
        } else {
            beamLength = (float) origin.distanceTo(target) - .4f;
            return false;
        }
    }

    private void pair() {
        if (partnerNode != null) {
            LaserNodeTileEntity partnerTileEntity = getTileEntityFromPos(partnerNode);
            partnerTileEntity.setPartnerNode(this.pos);
            if (world.isBlockPowered(partnerNode)) {
                if (checkForObstruction(getHeadOffset(partnerNode), getHeadOffset(pos))) {
                    setState(NodeState.PAIRED_RX_OBSTRUCTED);
                } else {
                    setState(NodeState.PAIRED_RX);
                }
                setState(partnerNode, NodeState.PAIRED_TX);
            } else {
                setState(partnerNode, NodeState.PAIRED_IDLE);
                if (world.getBlockState(pos).get(LaserNodeBlock.RECEIVING_POWER)) {
                    setState(NodeState.PAIRED_TX);
                } else {
                    setState(NodeState.PAIRED_IDLE);
                }
            }
        } else {
            OttselTurrets.LOGGER.debug(pos + "Failed to pair with partner");
        }
    }

    private void unpair() {
        if (!world.isRemote) {
            if (partnerNode != null) {
                setState(partnerNode, NodeState.IDLE);
                getTileEntityFromPos(partnerNode).setPartnerNode(null);
            }
            setState(NodeState.IDLE);
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

    public void neighborConnections() {
        if (world == null)
            return;

        TileEntity north = world.getTileEntity(pos.add(0, 0, -1));
        TileEntity south = world.getTileEntity(pos.add(0, 0, 1));
        TileEntity east = world.getTileEntity(pos.add(1, 0, 0));
        TileEntity west = world.getTileEntity(pos.add(-1, 0, 0));

        if (north != null && north instanceof IOptical) {
            northOptical = true;
        } else {
            northOptical = false;
        }
        if (south != null && south instanceof IOptical) {
            southOptical = true;
        } else {
            southOptical = false;
        }
        if (east != null && east instanceof IOptical) {
            eastOptical = true;
        } else {
            eastOptical = false;
        }
        if (west != null && west instanceof IOptical) {
            westOptical = true;
        } else {
            westOptical = false;
        }
    }

    private final RayTraceResult rayTraceToTarget(Vector3d origin, Vector3d target) {
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

    public static Vector3d getHeadOffset(BlockPos pos) {
        return new Vector3d(pos.getX() + .5, pos.getY() + .8125, pos.getZ() + .5);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    private <E extends TileEntity & IAnimatable> PlayState predicate(AnimationEvent<E> event) {
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
            case PAIRED_RX_OBSTRUCTED:
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
    public void registerControllers(AnimationData animationData) {
        AnimationController controller = new AnimationController(this, "controller", 0, this::predicate);
        animationData.addAnimationController(controller);
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}