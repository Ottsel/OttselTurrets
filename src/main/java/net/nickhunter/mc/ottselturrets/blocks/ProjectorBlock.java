package net.nickhunter.mc.ottselturrets.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.nickhunter.mc.ottselturrets.blocks.tile.ProjectorTileEntity;
import net.nickhunter.mc.ottselturrets.client.models.item.ProjectorItemModel;
import net.nickhunter.mc.ottselturrets.client.models.tile.ProjectorTileModel;
import net.nickhunter.mc.ottselturrets.items.AnimatedBlockItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class ProjectorBlock extends AnimatedHorizontalBlock {

    public static final String RESOURCE_NAME = "laser_projector";
    public static final AnimatedGeoModel<AnimatedBlockItem> ITEM_MODEL = new ProjectorItemModel();
    public static final AnimatedGeoModel<ProjectorTileEntity> TILE_MODEL = new ProjectorTileModel();
    public static final VoxelShape HITBOX_AABB = Block.makeCuboidShape(2D, 0D, 2D, 14D, 14D, 14D);

    public ProjectorBlock() {
        super(Material.IRON, RESOURCE_NAME, ITEM_MODEL, TILE_MODEL, HITBOX_AABB);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
            Hand handIn, BlockRayTraceResult hit) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof ProjectorTileEntity) {
            ((ProjectorTileEntity) tileEntity).onBlockActivated(player, hit, handIn);
        }
        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }
    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof ProjectorTileEntity) {
            ((ProjectorTileEntity) tileEntity).onBlockDestroyed(player);
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ProjectorTileEntity();
    }
}
