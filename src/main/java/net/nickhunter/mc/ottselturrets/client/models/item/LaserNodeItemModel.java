package net.nickhunter.mc.ottselturrets.client.models.item;

import net.minecraft.util.ResourceLocation;
import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.items.AnimatedBlockItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class LaserNodeItemModel extends AnimatedGeoModel<AnimatedBlockItem> {

    @Override
    public ResourceLocation getModelLocation(AnimatedBlockItem turretBlockItem) {
        return new ResourceLocation(OttselTurrets.MOD_ID, "geo/block/laser_projector.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(AnimatedBlockItem turretBlockItem) {
        return new ResourceLocation(OttselTurrets.MOD_ID, "textures/blocks/turret_horizontal.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(AnimatedBlockItem turretBlockItem) {
        return new ResourceLocation(OttselTurrets.MOD_ID, "animations/block/laser_projector.json");
    }
}
