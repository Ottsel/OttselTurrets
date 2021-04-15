package net.nickhunter.mc.ottselturrets.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.nickhunter.mc.ottselturrets.blocks.tile.LaserNodeTileEntity;
import net.nickhunter.mc.ottselturrets.client.models.item.LaserNodeItemModel;
import net.nickhunter.mc.ottselturrets.client.models.tile.LaserNodeTileModel;
import net.nickhunter.mc.ottselturrets.items.AnimatedBlockItem;
import net.nickhunter.mc.ottselturrets.util.NodeState;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class LaserNodeBlock extends AnimatedBlock {

    public static final String RESOURCE_NAME = "laser_node";
    public static final AnimatedGeoModel<AnimatedBlockItem> ITEM_MODEL = new LaserNodeItemModel();
    public static final AnimatedGeoModel<LaserNodeTileEntity> TILE_MODEL = new LaserNodeTileModel();
    public static final VoxelShape HITBOX_AABB = Block.box(4D, 0D, 4D, 12D, 5D, 12D);
    public static final BooleanProperty RECEIVING_POWER = BooleanProperty.create("receiving_power");
    public static final EnumProperty<NodeState> NODE_STATE = EnumProperty.create("node_state", NodeState.class);
    public static final BooleanProperty OBSTRUCTED= BooleanProperty.create("obstructed");

    public LaserNodeBlock() {
        super(Material.METAL, RESOURCE_NAME, ITEM_MODEL, TILE_MODEL, HITBOX_AABB);
    }

    private LaserNodeTileEntity getTileEntity(World world, BlockPos pos) {
        TileEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof LaserNodeTileEntity) {
            return (LaserNodeTileEntity) tileEntity;
        }
        return null;
    }

    @SuppressWarnings({ "deprecation" })
    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
            boolean isMoving) {
        boolean isPowered = worldIn.hasNeighborSignal(pos);
        switch (worldIn.getBlockState(pos).getValue(NODE_STATE)) {
            case PAIRED_IDLE:
            case PAIRED_TX:
                break;
            case PAIRED_RX:
            case PAIRED_RX_OBSTRUCTED:
            case IDLE:
            case PAIRING:
            default:
                isPowered = false;
                break;
        }
        worldIn.setBlock(pos, worldIn.getBlockState(pos).setValue(RECEIVING_POWER, isPowered), 2);
        LaserNodeTileEntity tileEntity = getTileEntity(worldIn, pos);
        if (tileEntity != null) {
            tileEntity.neighborUpdate();
        }
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return super.getStateForPlacement(context)
                .setValue(RECEIVING_POWER, context.getLevel().hasNeighborSignal(context.getClickedPos()))
                .setValue(NODE_STATE, NodeState.IDLE)
                .setValue(OBSTRUCTED, false);
    }

    @Override
    public void onBlockExploded(BlockState state, World world, BlockPos pos, Explosion explosion) {
        LaserNodeTileEntity tileEntity = getTileEntity(world, pos);
        if (tileEntity != null)
            tileEntity.onBlockDestroyed();
        super.onBlockExploded(state, world, pos, explosion);

    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
            Hand handIn, BlockRayTraceResult hit) {
        LaserNodeTileEntity tileEntity = getTileEntity(worldIn, pos);
        if (tileEntity != null)
            tileEntity.onBlockActivated(player, hit, handIn);
        return ActionResultType.SUCCESS;
    }

    @Override
    public void playerWillDestroy(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        super.playerWillDestroy(worldIn, pos, state, player);
        LaserNodeTileEntity tileEntity = getTileEntity(worldIn, pos);
        if (tileEntity != null)
            tileEntity.onBlockDestroyed();
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(RECEIVING_POWER);
        builder.add(NODE_STATE);
        builder.add(OBSTRUCTED);
    }

    @Override
    public int getSignal(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        boolean isPowered = false;
        switch (blockState.getValue(NODE_STATE)) {
            case PAIRED_RX:
                isPowered = true;
                break;
            case IDLE:
            case PAIRED_IDLE:
            case PAIRED_RX_OBSTRUCTED:
            case PAIRED_TX:
            case PAIRING:
            default:
                break;

        }
        return isPowered ? 15 : 0;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new LaserNodeTileEntity();
    }
}
