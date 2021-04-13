package net.nickhunter.mc.ottselturrets.client.models.item;

import net.minecraft.util.ResourceLocation;
import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.items.LaserWeaponItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class LaserWeaponItemModel extends AnimatedGeoModel<LaserWeaponItem> {

    @Override
    public ResourceLocation getModelLocation(LaserWeaponItem laserWeaponItem) {
        return new ResourceLocation(OttselTurrets.MOD_ID, "geo/item/weapon_laser.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(LaserWeaponItem laserWeaponItem) {
        return new ResourceLocation(OttselTurrets.MOD_ID, "textures/block/turret_horizontal.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(LaserWeaponItem laserWeaponItem) {
        return new ResourceLocation(OttselTurrets.MOD_ID, "animations/item/weapon_laser.animation.json");
    }
}
