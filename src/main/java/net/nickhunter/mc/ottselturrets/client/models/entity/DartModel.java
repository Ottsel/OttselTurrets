package net.nickhunter.mc.ottselturrets.client.models.entity;

import net.minecraft.util.ResourceLocation;
import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.entities.DartEntity;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class DartModel extends AnimatedGeoModel<DartEntity> {
    @Override
    public ResourceLocation getModelLocation(DartEntity entity) {
        return new ResourceLocation(OttselTurrets.MOD_ID, "geo/entity/laser_dart.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(DartEntity entity) {
        return new ResourceLocation(OttselTurrets.MOD_ID, "textures/model/entity/laser_dart.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(DartEntity entity) {
        return new ResourceLocation(OttselTurrets.MOD_ID, "animations/entity/dart.animation.json");
    }
}