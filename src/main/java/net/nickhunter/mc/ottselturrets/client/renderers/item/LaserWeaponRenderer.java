package net.nickhunter.mc.ottselturrets.client.renderers.item;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.nickhunter.mc.ottselturrets.client.models.item.LaserWeaponItemModel;
import net.nickhunter.mc.ottselturrets.items.LaserWeaponItem;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class LaserWeaponRenderer extends GeoItemRenderer<LaserWeaponItem> {
    public LaserWeaponRenderer() {
        super(new LaserWeaponItemModel());
    }
    
    private TransformType currentTransform;

    @Override
    public void renderByItem(ItemStack itemStack, TransformType p_239207_2_, MatrixStack matrixStack,
            IRenderTypeBuffer bufferIn, int combinedLightIn, int p_239207_6_) {
        currentTransform = p_239207_2_;
        super.renderByItem(itemStack, p_239207_2_, matrixStack, bufferIn, combinedLightIn, p_239207_6_);
    }

    @Override
    public Integer getUniqueID(LaserWeaponItem animatable) {
        if (currentTransform == TransformType.GUI) {
            return -1;
        }
        return super.getUniqueID(animatable);
    }
}
