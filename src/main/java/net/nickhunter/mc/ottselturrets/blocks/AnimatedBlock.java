package net.nickhunter.mc.ottselturrets.blocks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.nickhunter.mc.ottselturrets.blocks.tile.AnimatedTileEntity;
import net.nickhunter.mc.ottselturrets.items.AnimatedBlockItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class AnimatedBlock extends Block {

    private final VoxelShape hitboxAABB;
    private final String resourceName;
    private final AnimatedGeoModel<AnimatedBlockItem> itemModel;
    private final AnimatedGeoModel<? extends AnimatedTileEntity> tileModel;

    public AnimatedBlock(Material material, String resourceName, AnimatedGeoModel<AnimatedBlockItem> itemModel,
            AnimatedGeoModel<? extends AnimatedTileEntity> tileModel, VoxelShape hitboxAABB) {
        super(Properties.create(material).notSolid());
        this.resourceName = resourceName;
        this.itemModel = itemModel;
        this.tileModel = tileModel;
        this.hitboxAABB = hitboxAABB;
    }

    public String getResourceName() {
        return resourceName;
    }

    public AnimatedGeoModel<AnimatedBlockItem> getItemModel() {
        return itemModel;
    }

    public AnimatedGeoModel<? extends AnimatedTileEntity> getTileModel() {
        return tileModel;
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

    @Nonnull
    public VoxelShape getShape(BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos,
            @Nonnull ISelectionContext context) {
        return hitboxAABB;
    }
}
