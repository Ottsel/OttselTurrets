package net.nickhunter.mc.ottselturrets.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.nickhunter.mc.ottselturrets.blocks.tile.BallistaTurretTileEntity;
import net.nickhunter.mc.ottselturrets.blocks.tile.TurretTileEntity;
import net.nickhunter.mc.ottselturrets.client.models.item.BallistaTurretItemModel;
import net.nickhunter.mc.ottselturrets.client.models.tile.BallistaTurretTileModel;
import net.nickhunter.mc.ottselturrets.items.TurretBlockItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class BallistaTurretBlock extends TurretBlock {

    public static final String RESOURCE_NAME = "ballista_turret";
    public static final AnimatedGeoModel<TurretBlockItem> ITEM_MODEL = new BallistaTurretItemModel();
    public static final AnimatedGeoModel<TurretTileEntity> TILE_MODEL = new BallistaTurretTileModel();
    public static final VoxelShape HITBOX_AABB = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D);

    public BallistaTurretBlock() {
        super(Material.WOOD, RESOURCE_NAME, ITEM_MODEL, TILE_MODEL, HITBOX_AABB);
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new BallistaTurretTileEntity();
    }
}