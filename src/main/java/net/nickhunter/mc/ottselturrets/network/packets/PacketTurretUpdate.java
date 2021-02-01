package net.nickhunter.mc.ottselturrets.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.blocks.tile.TurretTileEntity;
import net.nickhunter.mc.ottselturrets.blocks.tile.TurretTileEntity.TurretState;
import net.nickhunter.mc.ottselturrets.network.OttselPacket;

public class PacketTurretUpdate extends OttselPacket {
    public PacketTurretUpdate(PacketBuffer buffer) {
        readPayload(buffer);
    }

    public PacketTurretUpdate(final TurretState turretState, final BlockPos pos) {
        this.turretState = turretState;
        this.pos = pos;
    }

    private TurretState turretState;
    private BlockPos pos;

    @Override
    public void client() {
        OttselTurrets.LOGGER.debug("Packet received for turret at: " + pos + " State: " + turretState);
        World world = Minecraft.getInstance().world;
        if (world != null) {
            TileEntity tileEntity = Minecraft.getInstance().world.getChunkAt(pos).getTileEntity(pos);
            if (tileEntity != null) {
                if (tileEntity instanceof TurretTileEntity) {
                    TurretTileEntity turret = (TurretTileEntity) tileEntity;
                    turret.setTurretState(turretState);
                }
            }
        }
    }

    @Override
    public void getPayload(PacketBuffer buffer) {
        buffer.writeEnumValue(turretState);
        buffer.writeBlockPos(pos);
    }

    @Override
    public void readPayload(PacketBuffer buffer) {
        turretState = buffer.readEnumValue(TurretState.class);
        pos = buffer.readBlockPos();
    }
}
