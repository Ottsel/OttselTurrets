package com.hunterpnw.mc.ottselturrets.client.renderers.item;

import com.hunterpnw.mc.ottselturrets.client.models.item.TurretItemModel;
import com.hunterpnw.mc.ottselturrets.items.TurretBlockItem;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class TurretItemRenderer extends GeoItemRenderer<TurretBlockItem> {
    public TurretItemRenderer() {
        super(new TurretItemModel());
    }
}