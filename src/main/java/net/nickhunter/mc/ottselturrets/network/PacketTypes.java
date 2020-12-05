package net.nickhunter.mc.ottselturrets.network;

import java.util.HashMap;
import java.util.function.BiConsumer;

import net.nickhunter.mc.ottselturrets.network.packets.PacketTurretUpdate;

public enum PacketTypes {
    TURRET_UPDATE((channel, integer) -> {
        channel.registerMessage(integer, PacketTurretUpdate.class, PacketTurretUpdate::new);
    });
    private final BiConsumer<NetworkChannel, Integer> registrationHandler;

    private static HashMap<Class<? extends OttselPacket>, Integer> fromClassToId = new HashMap<Class<? extends OttselPacket>, Integer>();
    private static HashMap<Integer, Class<? extends OttselPacket>> fromIdToClass = new HashMap<Integer, Class<? extends OttselPacket>>();

    PacketTypes(final BiConsumer<NetworkChannel, Integer> registrationHandler) {
        this.registrationHandler = registrationHandler;
    }

    public static void init(NetworkChannel channel) {
        int idx = 0;

        for (final PacketTypes p : PacketTypes.values()) {
            p.registrationHandler.accept(channel, ++idx);
        }
    }

    public static int getID(
            final Class<? extends OttselPacket> clz) {
        return fromClassToId.get(clz);
    }

    public static OttselPacket constructByID(
            final int id) throws InstantiationException, IllegalAccessException {
        return fromIdToClass.get(id).newInstance();
    }

}