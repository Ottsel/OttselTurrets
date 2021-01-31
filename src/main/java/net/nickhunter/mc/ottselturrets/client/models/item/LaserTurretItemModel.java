package net.nickhunter.mc.ottselturrets.client.models.item;

import net.minecraft.util.ResourceLocation;
import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.items.TurretBlockItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class LaserTurretItemModel extends AnimatedGeoModel<TurretBlockItem> {

    @Override
    public ResourceLocation getModelLocation(TurretBlockItem turretBlockItem) {
        return new ResourceLocation(OttselTurrets.MOD_ID, "geo/block/turret_horizontal.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(TurretBlockItem turretBlockItem) {
        return new ResourceLocation(OttselTurrets.MOD_ID, "textures/blocks/turret_horizontal.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(TurretBlockItem turretBlockItem) {
        return new ResourceLocation(OttselTurrets.MOD_ID, "animations/block/turret.animation.json");
    }
}
