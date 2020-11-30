package com.hunterpnw.mc.ottselturrets.blocks;

import com.hunterpnw.mc.ottselturrets.TurretType;
import com.hunterpnw.mc.ottselturrets.blocks.tile.TurretTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TurretBlock extends DirectionalBlock {

    //Collision boxes for each orientation of the turret.
    protected static final VoxelShape TURRET_UP_AABB = Block.makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 8.0D, 14.0D);
    protected static final VoxelShape TURRET_DOWN_AABB = Block.makeCuboidShape(2.0D, 8.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    protected static final VoxelShape TURRET_E_AABB = Block.makeCuboidShape(0.0D, 2.0D, 2.0D, 8.0D, 14.0D, 14.0D);
    protected static final VoxelShape TURRET_W_AABB = Block.makeCuboidShape(8.0D, 2.0D, 2.0D, 16.0D, 14.0D, 14.0D);
    protected static final VoxelShape TURRET_N_AABB = Block.makeCuboidShape(2.0D, 2.0D, 8.0D, 14.0D, 14.0D, 16.0D);
    protected static final VoxelShape TURRET_S_AABB = Block.makeCuboidShape(2.0D, 2.0D, 0.0D, 14.0D, 14.0D, 8.0D);

    //Damage type dealt by this turret.
    public TurretType turretType;

    public TurretBlock(TurretType turretType) {
        super(Properties.create(Material.IRON)
                .notSolid()
        );
        this.turretType = turretType;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {

        return new TurretTileEntity(turretType);
    }

    @Nonnull
    @Override
    public BlockRenderType getRenderType(@Nullable BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    //Set collision box according to orientation.
    @Nonnull
    public VoxelShape getShape(BlockState state,@Nonnull IBlockReader worldIn,@Nonnull BlockPos pos,@Nonnull ISelectionContext context) {
        switch (state.get(FACING)) {
            case UP:
            default:
                return TURRET_UP_AABB;
            case DOWN:
                return TURRET_DOWN_AABB;
            case EAST:
                return TURRET_E_AABB;
            case WEST:
                return TURRET_W_AABB;
            case NORTH:
                return TURRET_N_AABB;
            case SOUTH:
                return TURRET_S_AABB;
        }
    }

    @Nonnull
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction direction = context.getFace();
        BlockState blockstate = context.getWorld().getBlockState(context.getPos().offset(direction.getOpposite()));
        return blockstate.getBlock() == this && blockstate.get(FACING) == direction ? this.getDefaultState().with(FACING, direction.getOpposite()) : this.getDefaultState().with(FACING, direction);
    }
}
