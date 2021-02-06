package net.nickhunter.mc.ottselturrets.client.renderers.item;

import net.nickhunter.mc.ottselturrets.client.models.item.LaserWeaponItemModel;
import net.nickhunter.mc.ottselturrets.items.LaserWeaponItem;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class ItemRenderer extends GeoItemRenderer<LaserWeaponItem> {
    public ItemRenderer() {
        super(new LaserWeaponItemModel());
    }
    
}
