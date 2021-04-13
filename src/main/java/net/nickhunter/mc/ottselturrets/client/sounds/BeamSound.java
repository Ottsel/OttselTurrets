package net.nickhunter.mc.ottselturrets.client.sounds;

import net.minecraft.client.audio.TickableSound;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.nickhunter.mc.ottselturrets.registry.SoundRegistry;
import net.nickhunter.mc.ottselturrets.util.IBeamEmitter;

public class BeamSound extends TickableSound {

    private static final float MAX_VOLUME = 0.1f;

    private final IBeamEmitter emitter;
    private final PlayerEntity player;
    private final Vector3d posWithOffset;
    private final boolean needsToCharge;

    private boolean chargeStarted;

    private static BeamSound currentItemSound;
    
    public BeamSound(SoundCategory category, IBeamEmitter emitter, PlayerEntity player, boolean needsToCharge) {
        super(SoundRegistry.LASER_SUSTAIN.getSound(), category);
        this.emitter = emitter;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.00000001f;
        this.pitch = 1;
        this.player = player;
        this.needsToCharge = needsToCharge;

        if (category == SoundCategory.BLOCKS && emitter instanceof TileEntity) {
            BlockPos pos = ((TileEntity) emitter).getPos();
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
        }
        this.posWithOffset = new Vector3d(this.x + 0.5, this.y + 1, this.z + 0.5);

        if (emitter instanceof Item) {
            if (currentItemSound != null && player == currentItemSound.player && !currentItemSound.isDonePlaying()) {
                currentItemSound.finishPlaying();
            }
            currentItemSound = this;
        }
    }

    @Override
    public void tick() {
        if (emitter instanceof TileEntity) {
            if (((TileEntity) emitter).isRemoved()) {
                this.finishPlaying();
                return;
            }
            double distance = player.getPositionVec().distanceTo(posWithOffset);
            this.volume = (float) (MAX_VOLUME / (distance + 1) - 0.006);
            if (volume < 0)
                volume = 0;
        } else {
            volume = MAX_VOLUME;
        }
        switch (emitter.getBeamState()) {
        case CHARGING:
            chargeStarted = true;
            this.pitch += .030f;
            break;
        case FIRING:
            if (needsToCharge && !chargeStarted) {
                this.volume = 0;
            } else {
                this.pitch = 0;
            }
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
