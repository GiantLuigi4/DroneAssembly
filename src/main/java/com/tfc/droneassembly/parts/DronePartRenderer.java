package com.tfc.droneassembly.parts;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.tfc.assortedutils.API.rendering.RenderHelper;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

public class DronePartRenderer {
	private static final ModelRenderer cube = new ModelRenderer(16, 16, 0, 0);
	
	private final String texture;
	
	static {
		//Generated with block bench
		cube.setRotationPoint(0.0F, 0.0F, 0.0F);
		cube.setTextureOffset(0, 0).addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 4.0F, 0.0F, false);
	}
	
	boolean emissive = false;
	
	public DronePartRenderer() {
		this.texture = "basic_block";
	}
	
	public DronePartRenderer(boolean useEmissive) {
		this.texture = "basic_block";
		this.emissive = useEmissive;
	}
	
	public DronePartRenderer(String texture) {
		this.texture = texture;
	}
	
	public DronePartRenderer(String texture, boolean useEmissive) {
		this.texture = texture;
		this.emissive = useEmissive;
	}
	
	public void render(MatrixStack stack, IRenderTypeBuffer bufferIn, float partialTicks, CompoundNBT data, int packedLightIn) {
		renderCube(
				stack, bufferIn, packedLightIn,
				0, 0, 0,
				16, 16, 16,
				data.getFloat("red"), data.getFloat("green"), data.getFloat("blue"),
				new ResourceLocation("drone_assembly:textures/entity/" + texture + ".png")
		);
		if (emissive) {
			renderCube(
					stack, bufferIn, LightTexture.packLight(15, 15),
					0, 0, 0,
					16, 16, 16,
					1, 1, 1,
					new ResourceLocation("drone_assembly:textures/entity/" + texture + "_emissive" + ".png")
			);
		}
	}
	
	public void renderCube(
			MatrixStack stack, IRenderTypeBuffer buffer, int packedLightIn,
			float x, float y, float z,
			float width, float height, float depth,
			float r, float g, float b,
			ResourceLocation texture
	) {
		stack.push();
		stack.translate(x, y, z);
		stack.translate(1f / 4, 0, 1f / 4);
		stack.scale(width, height, depth);
		stack.scale(1f / 16, 1f / 16, 1f / 16);
		if (texture == (null))
			cube.render(stack, buffer.getBuffer(RenderType.getLines()), packedLightIn, OverlayTexture.NO_OVERLAY, r, g, b, 1);
		else
			cube.render(stack, buffer.getBuffer(RenderType.getEntityCutout(texture)), packedLightIn, OverlayTexture.NO_OVERLAY, r, g, b, 1);
		stack.pop();

//		Matrix4f matrix4f = stack.getLast().getMatrix();
//		IVertexBuilder bufferIn = buffer.getBuffer(
//				RenderType.getEntityCutoutNoCull(texture)
//		);
//		int[] offX = {0,128,128,0};
//		int[] offY = {0,0,128,128};
//		for (int i=0;i<4;i++) {
//			Vector4f vector4f = new Vector4f(offX[i],offY[i],0,0);
//			vector4f.transform(matrix4f);
//			bufferIn.addVertex(
//					vector4f.getX(), vector4f.getY(), vector4f.getZ(),
//					r, g, b, 1,
//					0,0,
//					OverlayTexture.NO_OVERLAY, packedLightIn,
//					0,0,0
//			);
//		}
	}
}
