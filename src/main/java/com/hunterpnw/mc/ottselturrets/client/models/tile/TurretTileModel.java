package com.hunterpnw.mc.ottselturrets.client.models.tile;

import com.hunterpnw.mc.ottselturrets.OttselTurrets;
import com.hunterpnw.mc.ottselturrets.blocks.tile.TurretTileEntity;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

import static com.hunterpnw.mc.ottselturrets.TurretType.getTextureFromType;

public class TurretTileModel extends AnimatedGeoModel<TurretTileEntity>
{
    @Override
    public ResourceLocation getModelLocation(TurretTileEntity tileEntity)
    {
        return new ResourceLocation(OttselTurrets.MOD_ID, "geo/block/turret.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(TurretTileEntity tileEntity)
    {
        //TODO Network the turretType
        //OttselTurrets.LOGGER.debug("Got turret type: "+tileEntity);
        //return getTextureFromType(tileEntity.turretType);
        return new ResourceLocation(OttselTurrets.MOD_ID,"textures/blocks/laser_turret.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(TurretTileEntity tileEntity)
    {
        return new ResourceLocation(OttselTurrets.MOD_ID, "animations/block/turret.animation.json");
    }


}
