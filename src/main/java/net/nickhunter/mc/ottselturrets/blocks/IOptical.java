package net.nickhunter.mc.ottselturrets.blocks;

public interface IOptical {

    public enum PowerType {
        ENDPOINT, PASSTHROUGH, SOURCE, OFF
    }

    PowerType getPowerType();
}