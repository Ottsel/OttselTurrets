package net.nickhunter.mc.ottselturrets.client.renderers.item;

import net.nickhunter.mc.ottselturrets.items.AnimatedBlockItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class BlockItemRenderer extends GeoItemRenderer<AnimatedBlockItem> {
    public BlockItemRenderer(AnimatedGeoModel<AnimatedBlockItem> blockItemModel) {
        super(blockItemModel);
    }
}