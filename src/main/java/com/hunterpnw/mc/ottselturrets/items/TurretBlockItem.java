package com.hunterpnw.mc.ottselturrets.items;

import com.hunterpnw.mc.ottselturrets.TurretType;
import com.hunterpnw.mc.ottselturrets.client.renderers.item.TurretItemRenderer;
import com.hunterpnw.mc.ottselturrets.registry.ItemGroupRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class TurretBlockItem extends BlockItem implements IAnimatable {
    public AnimationFactory factory = new AnimationFactory(this);
    public TurretType turretType;

    public TurretBlockItem(Block blockTurret, TurretType turretType) {
        super(blockTurret, new Item.Properties().group(ItemGroupRegistry.MAIN).setISTER(() -> TurretItemRenderer::new));
        this.turretType = turretType;
    }

    private <P extends Item & IAnimatable> PlayState predicate(AnimationEvent<P> event) {
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
