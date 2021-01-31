package net.nickhunter.mc.ottselturrets.client.models.tile;

import net.minecraft.util.ResourceLocation;
import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.blocks.tile.LaserTurretTileEntity;
import net.nickhunter.mc.ottselturrets.blocks.tile.TurretTileEntity;
import software.bernie.geckolib3.resource.GeckoLibCache;

public class LaserTurretTileModel extends TurretTileModel {

    @Override
    public void setLivingAnimations(TurretTileEntity entity, Integer uniqueID) {
        super.setLivingAnimations(entity, uniqueID);
        GeckoLibCache.getInstance().parser.setValue("beam_length", ((LaserTurretTileEntity) entity).beamLength);
    }

    @Override
    public ResourceLocation getModelLocation(TurretTileEntity tileEntity) {
        return new ResourceLocation(OttselTurrets.MOD_ID, "geo/block/turret_horizontal.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(TurretTileEntity tileEntity) {
        return new ResourceLocation(OttselTurrets.MOD_ID, "textures/blocks/turret_horizontal.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(TurretTileEntity tileEntity) {
        return new ResourceLocation(OttselTurrets.MOD_ID, "animations/block/turret.animation.json");
    }
}