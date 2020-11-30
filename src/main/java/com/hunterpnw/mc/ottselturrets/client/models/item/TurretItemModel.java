package com.hunterpnw.mc.ottselturrets.client.models.item;

import com.hunterpnw.mc.ottselturrets.OttselTurrets;
import com.hunterpnw.mc.ottselturrets.items.TurretBlockItem;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

import static com.hunterpnw.mc.ottselturrets.TurretType.getTextureFromType;

public class TurretItemModel extends AnimatedGeoModel<TurretBlockItem>
{
    @Override
    public ResourceLocation getModelLocation(TurretBlockItem turretBlockItem)
    {
        return new ResourceLocation(OttselTurrets.MOD_ID, "geo/block/turret.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(TurretBlockItem turretBlockItem)
    {
        return getTextureFromType(turretBlockItem.turretType);
    }

    @Override
    public ResourceLocation getAnimationFileLocation(TurretBlockItem turretBlockItem)
    {
        return new ResourceLocation(OttselTurrets.MOD_ID, "animations/block/turret.animation.json");
    }


}
