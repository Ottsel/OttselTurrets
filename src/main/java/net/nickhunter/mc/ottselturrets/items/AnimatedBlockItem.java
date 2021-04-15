package net.nickhunter.mc.ottselturrets.items;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.nickhunter.mc.ottselturrets.blocks.AnimatedBlock;
import net.nickhunter.mc.ottselturrets.blocks.AnimatedHorizontalBlock;
import net.nickhunter.mc.ottselturrets.client.renderers.item.BlockItemRenderer;
import net.nickhunter.mc.ottselturrets.registry.ItemGroupRegistry;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class AnimatedBlockItem extends BlockItem implements IAnimatable {
    public final AnimationFactory factory = new AnimationFactory(this);

    public AnimatedBlockItem(AnimatedBlock animatedBlock) {
        super(animatedBlock, new Item.Properties().tab(ItemGroupRegistry.MAIN)
                .setISTER(() -> () -> new BlockItemRenderer(animatedBlock.getItemModel())));
    }

    public AnimatedBlockItem(AnimatedHorizontalBlock animatedBlock) {
        super(animatedBlock, new Item.Properties().tab(ItemGroupRegistry.MAIN)
                .setISTER(() -> () -> new BlockItemRenderer(animatedBlock.getItemModel())));
    }

    private <P extends Item & IAnimatable> PlayState predicate(AnimationEvent<P> event) {
        return PlayState.CONTINUE;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller", 20, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

}
