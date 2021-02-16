package net.nickhunter.mc.ottselturrets.blocks;

import net.minecraft.util.Direction;

public interface IOptical {
    public void UpdateNeighborConnections();
    public void SetNeighborConnection(Direction direction, boolean enabled);
}