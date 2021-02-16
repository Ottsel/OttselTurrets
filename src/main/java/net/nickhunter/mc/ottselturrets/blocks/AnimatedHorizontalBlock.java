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
import net.nickhunter.mc.ottselturrets.blocks.tile.AnimatedTileEntity;
import net.nickhunter.mc.ottselturrets.items.AnimatedBlockItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class AnimatedHorizontalBlock extends HorizontalBlock {

    private final VoxelShape HITBOX_AABB;
    private final String RESOURCE_NAME;
    private final AnimatedGeoModel<AnimatedBlockItem> ITEM_MODEL;
    private final AnimatedGeoModel<? extends AnimatedTileEntity> TILE_MODEL;

    public AnimatedHorizontalBlock(Material material, String resourceName,
            AnimatedGeoModel<AnimatedBlockItem> itemModel, AnimatedGeoModel<? extends AnimatedTileEntity> tileModel,
            VoxelShape hitboxAABB) {
        super(Properties.create(material).notSolid());
        this.RESOURCE_NAME = resourceName;
        this.ITEM_MODEL = itemModel;
        this.TILE_MODEL = tileModel;
        this.HITBOX_AABB = hitboxAABB;
    }

    public String getResourceName() {
        return RESOURCE_NAME;
    }

    public AnimatedGeoModel<AnimatedBlockItem> getItemModel() {
        return ITEM_MODEL;
    }

    public AnimatedGeoModel<? extends AnimatedTileEntity> getTileModel() {
        return TILE_MODEL;
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

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING);
    }

    @Nonnull
    public VoxelShape getShape(BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos,
            @Nonnull ISelectionContext context) {
        return HITBOX_AABB;
    }

    @Nonnull
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction direction = context.getPlayer() != null ? context.getPlayer().getHorizontalFacing() : Direction.NORTH;
        return this.getDefaultState().with(HORIZONTAL_FACING, direction);
    }

}
