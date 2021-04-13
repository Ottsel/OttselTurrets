package net.nickhunter.mc.ottselturrets.blocks.tile;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.nickhunter.mc.ottselturrets.client.sounds.BeamSound;
import net.nickhunter.mc.ottselturrets.registry.SoundRegistry;
import net.nickhunter.mc.ottselturrets.registry.TileRegistry;
import net.nickhunter.mc.ottselturrets.util.DamageSources;
import net.nickhunter.mc.ottselturrets.util.IBeamEmitter;
import net.nickhunter.mc.ottselturrets.util.TurretState;

public class LaserTurretTileEntity extends TiltingTurretTileEntity implements IBeamEmitter {

    public static final String IDLE_ANIMATION = "animation.turret_horizontal.scan";
    public static final String AIMING_ANIMATION = "animation.turret_horizontal.rotate_head";
    public static final String FIRING_ANIMATION = "animation.turret_horizontal.fire_beam";
    public static final String RESET_ANIMATION = "animation.turret_horizontal.reset_rotation";

    public static final SoundEvent CHARGE_SOUND = SoundRegistry.LASER_CHARGE.getSound();
    public static final SoundEvent FIRING_SOUND = SoundRegistry.LASER_BOLT.getSound();

    public static final DamageSource DAMAGE_SOURCE = DamageSources.LASER_TURRET;

    public static final int RANGE = 10;
    public static final int DAMAGE = 20;
    public static final double CHARGE_TIME = 1.7;
    public static final double COOLDOWN_TIME = 2;
    public static final float PITCH_MAX = 45;
    public static final float HEAD_PITCH_MAX = 15;

    public static final float TILT_PITCH_AMOUNT = 25;

    private float beamLength;

    private BeamState beamState = BeamState.INACTIVE;
    private BeamSound currentSound;

    public LaserTurretTileEntity() {
        super(TileRegistry.LASER_TURRET.get(), IDLE_ANIMATION, AIMING_ANIMATION, FIRING_ANIMATION, RESET_ANIMATION,
                CHARGE_SOUND, FIRING_SOUND, DAMAGE_SOURCE, RANGE, DAMAGE, CHARGE_TIME, COOLDOWN_TIME, PITCH_MAX,
                HEAD_PITCH_MAX, TILT_PITCH_AMOUNT);
    }

    public float getBeamLength() {
        return beamLength;
    }

    @Override
    protected void clientTrackTarget() {
        calculateBeamLength(getTarget().getPositionVec());
        super.clientTrackTarget();
    }

    @Override
    public void clientTick() {
        switch (getState()) {
        case AIMING:
        case FIRING:
            if (currentSound == null || currentSound.isDonePlaying())
                currentSound = playSoundEffect(Minecraft.getInstance().player);
            break;
        case SCANNING:
        default:
            break;
        }
        super.clientTick();
    }

    @Override
    public TurretState getState() {
        TurretState turretState = super.getState();
        switch (turretState) {
        case AIMING:
            if (getTarget() == null || !getTarget().isAlive()) {
                beamState = BeamState.FIRING;
                break;
            }
            beamState = BeamState.CHARGING;
            break;
        case FIRING:
            beamState = BeamState.FIRING;
            break;
        case SCANNING:
        default:
            beamState = BeamState.INACTIVE;
            break;
        }
        return turretState;
    }

    @Override
    protected void playSoundEffect(SoundEvent soundEvent) {
        
    }

    protected BeamSound playSoundEffect(PlayerEntity player) {
        BeamSound sound = new BeamSound(SoundCategory.BLOCKS, this, player, true);
        Minecraft.getInstance().getSoundHandler().play(sound);
        return sound;
    }

    private void calculateBeamLength(Vector3d targetPos) {
        if (world == null)
            return;

        Vector3d posOffset = getPosOffset();
        Vector3d posVec = new Vector3d(this.pos.getX() + posOffset.x, this.pos.getY() + posOffset.y,
                this.pos.getZ() + posOffset.z);
        Vector3d posDiff = (targetPos.add(targetOffset)).subtract(posVec);
        RayTraceResult result = world.rayTraceBlocks(new RayTraceContext(posVec, posVec.add(posDiff.scale(RANGE)),
                RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, null));
        if (result.getType() == RayTraceResult.Type.MISS) {
            beamLength = 256;
        } else if (result.getType() == RayTraceResult.Type.BLOCK) {
            beamLength = (float) posVec.distanceTo(result.getHitVec()) - .9f;
        }
    }

    @Override
    public BeamState getBeamState() {
        return beamState;
    }
}