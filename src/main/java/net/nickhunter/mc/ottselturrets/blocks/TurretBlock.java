package net.nickhunter.mc.ottselturrets.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.nickhunter.mc.ottselturrets.TurretType;
import net.nickhunter.mc.ottselturrets.blocks.tile.TurretTileEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class TurretBlock extends HorizontalBlock {

    //Collision boxes for each orientation of the turret.
    protected static final VoxelShape TURRET_UP_AABB = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 8.0D, 10.0D, 8.0D);

    //Damage type dealt by this turret.
    public final TurretType turretType;

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
        builder.add(HORIZONTAL_FACING);
    }

    //Set collision box according to orientation.
    @Nonnull
    public VoxelShape getShape(BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos, @Nonnull ISelectionContext context) {
        return TURRET_UP_AABB;
    }

    @Nonnull
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction direction = context.getPlayer() != null ? context.getPlayer().getHorizontalFacing() : Direction.NORTH;
        return this.getDefaultState().with(HORIZONTAL_FACING, direction);
    }

}
