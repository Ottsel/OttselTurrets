package net.nickhunter.mc.ottselturrets.client.models.tile;

import net.minecraft.util.ResourceLocation;
import net.nickhunter.mc.ottselturrets.OttselTurrets;
import net.nickhunter.mc.ottselturrets.blocks.tile.TurretTileEntity;
import net.nickhunter.mc.ottselturrets.blocks.tile.TurretTileEntity.TurretState;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.resource.GeckoLibCache;

public abstract class TurretTileModel extends AnimatedGeoModel<TurretTileEntity> {

    public final String modelLocation;
    public final String textureLocation;
    public final String animationLocation;

    public final String yawBoneName;
    public final String pitchBoneName;

    TurretTileModel(String modelLocation, String textureLocation, String animationLocation, String yawBoneName, String pitchBoneName){

        this.modelLocation = modelLocation;
        this.textureLocation = textureLocation;
        this.animationLocation = animationLocation;

        this.yawBoneName = yawBoneName;
        this.pitchBoneName = pitchBoneName;
    }

    @Override
    public void setLivingAnimations(TurretTileEntity entity, Integer uniqueID) {

        GeckoLibCache.getInstance().parser.setValue("head_rotation_x_max", entity.headPitchMax);

        super.setLivingAnimations(entity, uniqueID);

        IBone yawBone = this.getAnimationProcessor().getBone(yawBoneName);
        IBone pitchBone = this.getAnimationProcessor().getBone(pitchBoneName);


        if (entity.turretState != TurretState.SCANNING) {
            yawBone.setRotationY((float) (entity.yawToTarget * Math.PI / 180));
            pitchBone.setRotationX((float) (entity.pitchToTarget * Math.PI / 180));
        }
    }
    
    @Override
    public ResourceLocation getModelLocation(TurretTileEntity tileEntity) {
        return new ResourceLocation(OttselTurrets.MOD_ID, modelLocation);
    }

    @Override
    public ResourceLocation getTextureLocation(TurretTileEntity tileEntity) {
        return new ResourceLocation(OttselTurrets.MOD_ID, textureLocation);
    }
 
    @Override
    public ResourceLocation getAnimationFileLocation(TurretTileEntity tileEntity) {
        return new ResourceLocation(OttselTurrets.MOD_ID, animationLocation);
    }
}
