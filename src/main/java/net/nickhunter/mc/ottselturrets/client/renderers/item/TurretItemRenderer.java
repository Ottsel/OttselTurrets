package net.nickhunter.mc.ottselturrets.client.renderers.item;

import net.nickhunter.mc.ottselturrets.client.models.item.TurretItemModel;
import net.nickhunter.mc.ottselturrets.items.TurretBlockItem;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class TurretItemRenderer extends GeoItemRenderer<TurretBlockItem> {
    public TurretItemRenderer() {
        super(new TurretItemModel());
    }
}