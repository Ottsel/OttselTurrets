package net.nickhunter.mc.ottselturrets.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.blocks.tile.TurretTileEntity;
import net.nickhunter.mc.ottselturrets.network.OttselPacket;
import net.minecraft.network.PacketBuffer;
import net.nickhunter.mc.ottselturrets.registry.SoundRegistry;
import net.nickhunter.mc.ottselturrets.registry.TileRegistry;

import static net.nickhunter.mc.ottselturrets.blocks.tile.TurretTileEntity.TurretAnimations.*;

public class PacketTurretUpdate extends OttselPacket {
    public PacketTurretUpdate(PacketBuffer buffer) {
        readPayload(buffer);
    }

    public PacketTurretUpdate(
            final String currentAnimation,
            final BlockPos pos) {
        this.currentAnimation = currentAnimation;
        this.pos = pos;
    }

    private String currentAnimation;
    private BlockPos pos;

    @Override
    public void client() {
        OttselTurrets.LOGGER.debug("Packet received for turret at: " + pos + " Animation: " + currentAnimation);
        World world = Minecraft.getInstance().world;
        if (world != null) {
            TileEntity tileEntity = Minecraft.getInstance().world.getChunkAt(pos).getTileEntity(pos);
            if (tileEntity != null) {
                if (tileEntity.getType() == TileRegistry.TURRET.get()) {
                    TurretTileEntity turret = (TurretTileEntity) tileEntity;
                    turret.queuedAnimation = currentAnimation;
                    if(currentAnimation.equals(AIM_AT_TARGET)){
                        turret.playSoundEffect(SoundRegistry.LASER_CHARGE.getSound());
                        return;
                    }
                    if(currentAnimation.equals(SHOOT)){
                        turret.playSoundEffect(SoundRegistry.LASER_BOLT.getSound());
                    }
                }
            }
        }
    }

    @Override
    public void getPayload(PacketBuffer buffer) {
        buffer.writeString(currentAnimation);
        buffer.writeBlockPos(pos);
    }

    @Override
    public void readPayload(PacketBuffer buffer) {
        currentAnimation = buffer.readString();
        pos = buffer.readBlockPos();
    }
}
