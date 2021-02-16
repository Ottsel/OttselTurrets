package net.nickhunter.mc.ottselturrets.blocks.tile;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import software.bernie.geckolib3.core.IAnimatable;

public abstract class AnimatedTileEntity extends TileEntity implements IAnimatable {

    public AnimatedTileEntity(TileEntityType<? extends AnimatedTileEntity> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }
}