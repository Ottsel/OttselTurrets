package net.nickhunter.mc.ottselturrets.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.PacketDistributor.TargetPoint;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.nickhunter.mc.ottselturrets.OttselTurrets;

import java.util.function.Function;

public class NetworkChannel {
    private static final String PROTOCOL_VERSION = OttselTurrets.VERSION;
    private final SimpleChannel INSTANCE;

    public NetworkChannel(final String channelName) {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(OttselTurrets.MOD_ID, channelName),
                () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    }

    public void registerCommonMessages() {
        PacketTypes.init(this);
    }

    public <MSG extends OttselPacket> void registerMessage(final int id, final Class<MSG> msgClazz,
            final Function<PacketBuffer, MSG> msgCreator) {
        INSTANCE.registerMessage(id, msgClazz, OttselPacket::getPayload, msgCreator, (msg, ctxIn) -> {
            final Context ctx = ctxIn.get();
            final LogicalSide packetOrigin = ctx.getDirection().getOriginationSide();
            ctx.setPacketHandled(true);
            ctx.enqueueWork(() -> msg.processPacket(ctx, packetOrigin.equals(LogicalSide.CLIENT)));
        });
    }

    public void sendToServer(final OttselPacket msg) {
        INSTANCE.sendToServer(msg);
    }

    public void sendToPlayer(final OttselPacket msg, final ServerPlayerEntity player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public void sendToOrigin(final OttselPacket msg, final Context ctx) {
        final ServerPlayerEntity player = ctx.getSender();
        if (player != null) {
            sendToPlayer(msg, player);
        } else {
            sendToServer(msg);
        }
    }

    public void sendToPosition(final OttselPacket msg, final TargetPoint pos) {
        INSTANCE.send(PacketDistributor.NEAR.with(() -> pos), msg);
    }

    public void sendToEveryone(final OttselPacket msg) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), msg);
    }

    public void sendToTrackingChunk(final OttselPacket msg, final Chunk chunk) {
        INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), msg);
    }

}
