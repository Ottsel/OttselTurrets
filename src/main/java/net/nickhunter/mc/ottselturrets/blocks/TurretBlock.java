package net.nickhunter.mc.ottselturrets.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.math.shapes.VoxelShape;
import net.nickhunter.mc.ottselturrets.blocks.tile.AnimatedTileEntity;
import net.nickhunter.mc.ottselturrets.items.AnimatedBlockItem;
import net.nickhunter.mc.ottselturrets.util.TurretState;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class TurretBlock extends AnimatedHorizontalBlock {

    public static final EnumProperty<TurretState> TURRET_STATE = EnumProperty.create("turret_state", TurretState.class);

    public TurretBlock(Material material, String resourceName, AnimatedGeoModel<AnimatedBlockItem> itemModel,
            AnimatedGeoModel<? extends AnimatedTileEntity> tileModel, VoxelShape hitboxAABB) {
        super(material, resourceName, itemModel, tileModel, hitboxAABB);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(TURRET_STATE);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return super.getStateForPlacement(context).setValue(TURRET_STATE, TurretState.SCANNING);
    }
}