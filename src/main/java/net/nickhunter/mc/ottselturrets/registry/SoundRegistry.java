package net.nickhunter.mc.ottselturrets.registry;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nickhunter.mc.ottselturrets.OttselTurrets;

@Mod.EventBusSubscriber(modid = OttselTurrets.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum SoundRegistry {
    LASER_CHARGE("laser_charge"), LASER_BOLT("laser_bolt");

    private SoundEvent sound;

    SoundRegistry(String name) {
        ResourceLocation location = new ResourceLocation(OttselTurrets.MOD_ID, name);
        sound = new SoundEvent(location).setRegistryName(name);
    }

    public SoundEvent getSound() {
        return sound;
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        for (SoundRegistry sound : values()) {
            event.getRegistry().register(sound.getSound());
        }
    }
}