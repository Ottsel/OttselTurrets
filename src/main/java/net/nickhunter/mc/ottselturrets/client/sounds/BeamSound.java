package net.nickhunter.mc.ottselturrets.client.sounds;

import net.minecraft.client.audio.TickableSound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.nickhunter.mc.ottselturrets.registry.SoundRegistry;
import net.nickhunter.mc.ottselturrets.util.IBeamEmitter;

public class BeamSound extends TickableSound {

    private final IBeamEmitter emitter;

    public BeamSound(SoundCategory category, IBeamEmitter emitter) {
        super(SoundRegistry.LASER_SUSTAIN.getSound(), category);
        this.emitter = emitter;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.01f;
        this.pitch = 1;
        if (category == SoundCategory.BLOCKS && emitter instanceof TileEntity) {
            BlockPos pos = ((TileEntity) emitter).getPos();
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
        }
    }

    @Override
    public void tick() {
        if (volume < .1f)
            this.volume += .01f;
        switch (emitter.getBeamState()) {
            case CHARGING:
                this.pitch += .015f;
                break;
            case FIRING:
                this.pitch = 0;
                break;
            case PAUSED:
                this.volume = 0;
                break;
            case INACTIVE:
            default:
                this.finishPlaying();
                break;
        }
    }
}
