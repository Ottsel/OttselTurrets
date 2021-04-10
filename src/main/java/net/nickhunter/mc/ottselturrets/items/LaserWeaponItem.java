package net.nickhunter.mc.ottselturrets.items;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.nickhunter.mc.ottselturrets.client.renderers.item.LaserWeaponRenderer;
import net.nickhunter.mc.ottselturrets.client.sounds.BeamSound;
import net.nickhunter.mc.ottselturrets.registry.ParticleRegistry;
import net.nickhunter.mc.ottselturrets.util.DamageSources;
import net.nickhunter.mc.ottselturrets.util.IBeamEmitter;
import net.nickhunter.mc.ottselturrets.util.TrigHelper;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.resource.GeckoLibCache;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class LaserWeaponItem extends Item implements IBeamEmitter, IAnimatable {

    public static final String RESOURCE_NAME = "laser_weapon";

    private static final String CONTROLLER_NAME = "controller";

    private static final String IDLE_ANIM = "animation.weapon_laser.idle";
    private static final String BEAM_FIRE_ANIM = "animation.weapon_laser.beam_fire";
    private static final String BEAM_HOLD_ANIM = "animation.weapon_laser.beam_hold";
    private static final String BEAM_RETRACT_ANIM = "animation.weapon_laser.beam_retract";

    private static final float BEAM_LENGTH_MAX = 512;

    private AnimationFactory factory = new AnimationFactory(this);

    private float beamLength = BEAM_LENGTH_MAX;
    private BeamState beamState = BeamState.INACTIVE;
    private static Vector3d localSpawnOffset = new Vector3d(0.5, -0.35, 0.1);

    public LaserWeaponItem(Properties properties) {
        super(properties.maxStackSize(1).setISTER(() -> LaserWeaponRenderer::new));
    }
    public void setBeamState(BeamState beamState){
        this.beamState = beamState;
    }

    public float getBeamLength() {
        return beamLength;
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (worldIn.isRemote) {
            if (entityIn instanceof PlayerEntity) {
                if (((PlayerEntity) entityIn).getHeldItemMainhand() != stack) {
                    setBeamState(BeamState.INACTIVE);
                    AnimationController<?> controller = GeckoLibUtil.getControllerForStack(this.factory, stack,
                            CONTROLLER_NAME);
                    controller.setAnimation(new AnimationBuilder().addAnimation(IDLE_ANIM, true));
                }
            }
        }
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, PlayerEntity player) {
        setBeamState(BeamState.INACTIVE);
        return super.onDroppedByPlayer(item, player);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        if (hand == Hand.OFF_HAND)
            return super.onItemRightClick(world, player, hand);
        player.setActiveHand(hand);

        setBeamState(BeamState.FIRING);
        if (world.isRemote) {
            Minecraft.getInstance().getSoundHandler().play(new BeamSound(SoundCategory.PLAYERS, this));
            AnimationController<?> controller = GeckoLibUtil.getControllerForStack(this.factory,
                    player.getHeldItem(player.getActiveHand()), CONTROLLER_NAME);
            controller.markNeedsReload();
            controller.setAnimation(new AnimationBuilder().addAnimation(BEAM_FIRE_ANIM, false));
        }

        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity entity, int count) {
        if (!(entity instanceof PlayerEntity))
            return;

        Vector3d spawnPos = getSpawnPos(entity);
        Vector3d hitVec = getTarget(entity);

        Vector3d localTargetVec = new Vector3d(0, 0, beamLength);
        float yawAdjust = -(TrigHelper.calculateYaw(localSpawnOffset, localTargetVec) - 90);
        float pitchAdjust = -(TrigHelper.calculatePitch(localSpawnOffset, localTargetVec) + 90);

        if (entity.world.isRemote) {
            GeckoLibCache geckoCache = GeckoLibCache.getInstance();
            geckoCache.parser.setValue("rotation_x", pitchAdjust);
            geckoCache.parser.setValue("rotation_y", yawAdjust);

            AnimationController<?> controller = GeckoLibUtil.getControllerForStack(this.factory,
                    entity.getHeldItem(entity.getActiveHand()), CONTROLLER_NAME);
            controller.setAnimation(new AnimationBuilder().addAnimation(BEAM_HOLD_ANIM, true));

        }

        Vector3d aimVec = Vector3d.fromPitchYaw(entity.rotationPitch + pitchAdjust, entity.rotationYaw + yawAdjust)
                .normalize();

        for (int i = 1; i < MathHelper.ceil(beamLength); i++) {

            Vector3d iSpawnPos = spawnPos.add(aimVec.scale(i + (Math.random()) - .3));
            entity.world.addParticle(ParticleRegistry.BEAM_PARTICLE.get(), iSpawnPos.x + (0.3 * (Math.random() - .5f)),
                    iSpawnPos.y + (0.3 * (Math.random() - .5f)) - .01, iSpawnPos.z + (0.3 * (Math.random() - .5f)),
                    (0.01 * (Math.random() - .5f)), (0.01 * (Math.random() - .5f)), (0.01 * (Math.random() - .5f)));
            if (hitVec != null) {
                entity.world.addParticle(ParticleTypes.ASH, hitVec.x + (0.2 * (Math.random() - .5f)),
                        hitVec.y + (0.2 * (Math.random() - .5f)), hitVec.z + (0.2 * (Math.random() - .5f)), 0, 0, 0);
            }

            if (!entity.world.isRemote) {
                AxisAlignedBB boundingBox = new AxisAlignedBB(iSpawnPos.subtract(.5, .5, .5),
                        iSpawnPos.add(.5, .5, .5));

                List<LivingEntity> entities = entity.world.getEntitiesWithinAABB(LivingEntity.class, boundingBox);
                if (entities != null && !entities.isEmpty()) {
                    for (LivingEntity livingEntity : entities) {
                        if (livingEntity != entity)
                            livingEntity.attackEntityFrom(DamageSources.causeArmCannonDamage(entity), 6);
                    }
                }
            }
        }
        super.onUsingTick(stack, entity, count);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
        if (worldIn.isRemote) {
            setBeamState(BeamState.INACTIVE);
            AnimationController<?> controller = GeckoLibUtil.getControllerForStack(this.factory, stack,
                    CONTROLLER_NAME);
            controller.setAnimation(new AnimationBuilder().addAnimation(BEAM_RETRACT_ANIM, false));
            controller.setAnimation(new AnimationBuilder().addAnimation(IDLE_ANIM, true));
        }
    }

    private Vector3d getTarget(LivingEntity entity) {

        Vector3d posVec = entity.getPositionVec().add(0, entity.getEyeHeight(), 0);
        Vector3d direction = entity.getLookVec();

        RayTraceResult result = entity.world.rayTraceBlocks(
                new RayTraceContext(posVec, posVec.add(direction.scale(256)), BlockMode.VISUAL, FluidMode.NONE, null));
        if (result.getType() == RayTraceResult.Type.BLOCK) {
            beamLength = (float) getSpawnPos(entity).distanceTo(result.getHitVec());
            return result.getHitVec();
        } else {
            beamLength = BEAM_LENGTH_MAX;
            return null;
        }
    }

    private static Vector3d getSpawnPos(LivingEntity entity) {

        Vector3d spawnPos = entity.getPositionVec().add(0, entity.getEyeHeight(), 0);

        spawnPos = spawnPos
                .add(Vector3d.fromPitchYaw(entity.rotationPitch - 90, entity.rotationYaw).scale(localSpawnOffset.y));
        spawnPos = spawnPos.add(Vector3d.fromPitchYaw(0, entity.rotationYaw + 90).scale(localSpawnOffset.x));
        spawnPos = spawnPos.add(entity.getLookVec().scale(localSpawnOffset.z));

        return spawnPos;
    }

    private <P extends Item & IAnimatable> PlayState predicate(AnimationEvent<P> event) {
        GeckoLibCache.getInstance().parser.setValue("beam_length", getBeamLength());
        return PlayState.CONTINUE;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, CONTROLLER_NAME, 1, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return false;
    }

    @Override
    public BeamState getBeamState() {
        return beamState;
    }
}
