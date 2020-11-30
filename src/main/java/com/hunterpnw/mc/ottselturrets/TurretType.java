package com.hunterpnw.mc.ottselturrets;

import net.minecraft.util.ResourceLocation;

public enum TurretType {
    PHYSICAL,
    FIRE,
    WATER,
    ICE,
    POISON,
    LASER,
    WITHER;

    public static TurretType getTurretTypeFromInt(int num) {
        switch (num) {
            case 0:
            default:
                return PHYSICAL;
            case 1:
                return FIRE;
            case 2:
                return WATER;
            case 3:
                return ICE;
            case 4:
                return POISON;
            case 5:
                return LASER;
            case 6:
                return WITHER;
        }
    }

    public static ResourceLocation getTextureFromType(TurretType turretType) {
        switch (turretType) {
            case PHYSICAL:
                return new ResourceLocation(OttselTurrets.MOD_ID, "textures/blocks/physical_turret.png");
            case FIRE:
                return new ResourceLocation(OttselTurrets.MOD_ID, "textures/blocks/fire_turret.png");
            case WATER:
                return new ResourceLocation(OttselTurrets.MOD_ID, "textures/blocks/water_turret.png");
            case ICE:
                return new ResourceLocation(OttselTurrets.MOD_ID, "textures/blocks/ice_turret.png");
            case POISON:
                return new ResourceLocation(OttselTurrets.MOD_ID, "textures/blocks/poison_turret.png");
            case LASER:
            default:
                return new ResourceLocation(OttselTurrets.MOD_ID, "textures/blocks/laser_turret.png");
            case WITHER:
                return new ResourceLocation(OttselTurrets.MOD_ID, "textures/blocks/wither_turret.png");
        }
    }
}
