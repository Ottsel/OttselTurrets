package net.nickhunter.mc.ottselturrets.util;

import net.minecraft.util.IStringSerializable;

public enum NodeState implements IStringSerializable {
    IDLE("idle"), PAIRING("pairing"), PAIRED_IDLE("paired_idle"), PAIRED_TX("paired_tx"), PAIRED_RX("paired_rx"),
    PAIRED_RX_OBSTRUCTED("paired_rx_obstructed");

    private final String name;

    private NodeState(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}