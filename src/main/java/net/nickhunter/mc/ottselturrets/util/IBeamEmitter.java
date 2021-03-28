package net.nickhunter.mc.ottselturrets.util;

public interface IBeamEmitter {
    public enum BeamState{
        CHARGING,
        FIRING,
        PAUSED,
        INACTIVE
    }

    public BeamState getBeamState();
}
