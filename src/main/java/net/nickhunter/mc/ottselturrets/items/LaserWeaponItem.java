package net.nickhunter.mc.ottselturrets.items;

import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.nickhunter.mc.ottselturrets.OttselTurrets;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.resource.GeckoLibCache;

public class LaserWeaponItem extends BowItem implements IAnimatable, ITickable{

    public static final String RESOURCE_NAME = "laser_weapon";

    public final AnimationFactory factory = new AnimationFactory(this);

    private float beamLength;
    private boolean firing;

    public LaserWeaponItem(Properties properties) {
        super(properties);
    }

    public float getBeamLength() {
        return beamLength;
    }

    public boolean getFiring() {
        return firing;
    }

    public void setFiring(boolean firing) {
        this.firing = firing;
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof PlayerEntity) {
            if (this.getUseDuration(stack) - timeLeft > 15) {
                beamLength = calculateBeamLength(entityLiving);
                firing = true;
            }
        }
        GeckoLibCache.getInstance().parser.setValue("beam_length", getBeamLength());
        OttselTurrets.LOGGER.info("beam length: "+ getBeamLength());
    }
    @Override
    public void tick() {
        
    }

    private float calculateBeamLength(LivingEntity entity) {
        Vector3d posVec = entity.getPositionVec().add(new Vector3d(0, 1, 0));
        Vector3d direction = entity.getLookVec();

        RayTraceResult result = entity.world.rayTraceBlocks(
                new RayTraceContext(posVec, posVec.add(direction.scale(256)), BlockMode.COLLIDER, FluidMode.NONE, null));
        if (result.getType() == RayTraceResult.Type.BLOCK) {
            return (float) entity.getPositionVec().distanceTo(result.getHitVec());
        } else {
            return 256;
        }
    }

    private <P extends Item & IAnimatable> PlayState predicate(AnimationEvent<P> event) {
        AnimationController controller = event.getController();
        if (getFiring()) {
            controller.setAnimation(new AnimationBuilder().addAnimation("animation.weapon_laser.fire", true));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller", 20, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

}
