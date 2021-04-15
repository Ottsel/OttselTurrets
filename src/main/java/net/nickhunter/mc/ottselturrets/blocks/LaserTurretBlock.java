package net.nickhunter.mc.ottselturrets.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.nickhunter.mc.ottselturrets.blocks.tile.LaserTurretTileEntity;
import net.nickhunter.mc.ottselturrets.blocks.tile.TurretTileEntity;
import net.nickhunter.mc.ottselturrets.client.models.item.LaserTurretItemModel;
import net.nickhunter.mc.ottselturrets.client.models.tile.LaserTurretTileModel;
import net.nickhunter.mc.ottselturrets.items.AnimatedBlockItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class LaserTurretBlock extends TurretBlock {

    public static final String RESOURCE_NAME = "laser_turret";
    public static final AnimatedGeoModel<AnimatedBlockItem> ITEM_MODEL = new LaserTurretItemModel();
    public static final AnimatedGeoModel<TurretTileEntity> TILE_MODEL = new LaserTurretTileModel();
    public static final VoxelShape HITBOX_AABB = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 10.0D, 14.0D);

    public LaserTurretBlock() {
        super(Material.METAL, RESOURCE_NAME, ITEM_MODEL, TILE_MODEL, HITBOX_AABB);
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new LaserTurretTileEntity();
    }
}