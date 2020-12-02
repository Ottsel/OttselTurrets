// Made with Blockbench 3.7.4
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package net.nickhunter.mc.ottselturrets.client.models.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.nickhunter.mc.ottselturrets.entities.DartEntity;

public class DartModel extends EntityModel<DartEntity> {
	private final ModelRenderer modelRenderer;

	public DartModel() {
		textureWidth = 16;
		textureHeight = 16;

		modelRenderer = new ModelRenderer(this);
		modelRenderer.setRotationPoint(0.0F, 24.0F, 0.0F);
		modelRenderer.setTextureOffset(0, 0).addBox(-0.5F, -0.5F, -6.0F, 1.0F, 1.0F, 12.0F, 0.0F, false);
	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
		modelRenderer.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	@Override
	public void setRotationAngles(DartEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}
}