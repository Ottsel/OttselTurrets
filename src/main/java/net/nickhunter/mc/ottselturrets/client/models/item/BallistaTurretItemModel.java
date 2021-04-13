package net.nickhunter.mc.ottselturrets.client.models.item;

import net.minecraft.util.ResourceLocation;
import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.items.AnimatedBlockItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class BallistaTurretItemModel extends AnimatedGeoModel<AnimatedBlockItem> {

    @Override
    public ResourceLocation getModelLocation(AnimatedBlockItem tileEntity) {
        return new ResourceLocation(OttselTurrets.MOD_ID, "geo/block/turret_ballista.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(AnimatedBlockItem tileEntity) {
        return new ResourceLocation(OttselTurrets.MOD_ID, "textures/block/turret_ballista.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(AnimatedBlockItem tileEntity) {
        return new ResourceLocation(OttselTurrets.MOD_ID, "animations/block/ballista_turret.animation.json");
    }
}
