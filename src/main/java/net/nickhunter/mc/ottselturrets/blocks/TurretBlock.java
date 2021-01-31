package net.nickhunter.mc.ottselturrets.blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.nickhunter.mc.ottselturrets.blocks.tile.TurretTileEntity;
import net.nickhunter.mc.ottselturrets.items.TurretBlockItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class TurretBlock extends HorizontalBlock {

    public final VoxelShape hitboxAABB;
    public final String resourceName;
    public final AnimatedGeoModel<TurretBlockItem> itemModel;
    public final AnimatedGeoModel<TurretTileEntity> tileModel;

    public TurretBlock(Material material, String resourceName, AnimatedGeoModel<TurretBlockItem> itemModel,
            AnimatedGeoModel<TurretTileEntity> tileModel, VoxelShape hitboxAABB) {
        super(Properties.create(material).notSolid());
        this.resourceName = resourceName;
        this.itemModel = itemModel;
        this.tileModel = tileModel;
        this.hitboxAABB = hitboxAABB;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nonnull
    @Override
    public BlockRenderType getRenderType(@Nullable BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING);
    }

    @Nonnull
    public VoxelShape getShape(BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos,
            @Nonnull ISelectionContext context) {
        return hitboxAABB;
    }

    @Nonnull
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction direction = context.getPlayer() != null ? context.getPlayer().getHorizontalFacing() : Direction.NORTH;
        return this.getDefaultState().with(HORIZONTAL_FACING, direction);
    }

}
