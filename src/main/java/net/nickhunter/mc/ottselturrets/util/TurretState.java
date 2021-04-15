package net.nickhunter.mc.ottselturrets.util;

import net.minecraft.util.IStringSerializable;

public enum TurretState implements IStringSerializable {
    SCANNING("scanning"), AIMING("aiming"), FIRING("firing");

    private final String name;

    private TurretState(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}