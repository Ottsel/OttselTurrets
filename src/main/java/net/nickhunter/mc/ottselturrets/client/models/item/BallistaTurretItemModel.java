package net.nickhunter.mc.ottselturrets.client.models.item;

import net.minecraft.util.ResourceLocation;
import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.items.TurretBlockItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class BallistaTurretItemModel extends AnimatedGeoModel<TurretBlockItem> {

    @Override
    public ResourceLocation getModelLocation(TurretBlockItem tileEntity) {
        return new ResourceLocation(OttselTurrets.MOD_ID, "geo/block/turret_ballista.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(TurretBlockItem tileEntity) {
        return new ResourceLocation(OttselTurrets.MOD_ID, "textures/blocks/turret_ballista.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(TurretBlockItem tileEntity) {
        return new ResourceLocation(OttselTurrets.MOD_ID, "animations/block/ballista_turret.animation.json");
    }
}
