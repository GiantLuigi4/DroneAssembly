package com.tfc.droneassembly.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.tfc.assortedutils.API.rendering.RenderHelper;
import com.tfc.assortedutils.API.transformations.matrix.Matrix4fDistSafe;
import com.tfc.droneassembly.DroneEntity;
import com.tfc.droneassembly.parts.DronePart;
import com.tfc.droneassembly.registries.DroneParts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.util.Constants;

public class DroneRenderer extends EntityRenderer<DroneEntity> {
	public DroneRenderer(EntityRendererManager renderManager) {
		super(renderManager);
	}
	
	@Override
	public void render(DroneEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		if (!entityIn.isAlive()) return;
		
		ListNBT parts = entityIn.getPartData().getList("parts", Constants.NBT.TAG_COMPOUND);
		matrixStackIn.push();
		matrixStackIn.rotate(entityIn.getRotation());
		
		matrixStackIn.translate(-1.5 / 4f, 0, -1.5 / 4f);
		
		for (int i = 0; i < parts.size(); i++) {
			matrixStackIn.push();
			CompoundNBT partNBT = parts.getCompound(i);
			DronePart part = DroneParts.get(partNBT.getString("name"));
			part.load(partNBT);
			matrixStackIn.translate(part.x / 4f, part.y / 4f, part.z / 4f);
			part.getRenderer().render(matrixStackIn, bufferIn, partialTicks, partNBT, packedLightIn);
			matrixStackIn.pop();
		}
		matrixStackIn.pop();
		
		matrixStackIn.push();
		matrixStackIn.translate(-(0.25f) / 2, 0, -(0.25f) / 2);
		
		Vector3d startVec = Minecraft.getInstance().player.getPositionVec();
		Vector3d endVec = Minecraft.getInstance().player.getLookVec();
		
		endVec = startVec.add(endVec.scale(8));
		
		Vector3d middle = startVec.add(endVec).scale(0.5f);
		startVec = startVec.subtract(middle);
		endVec = endVec.subtract(middle);
		
		Matrix4fDistSafe matrix = new Matrix4fDistSafe(entityIn.getRotation());
		startVec = matrix.transformS(startVec);
		endVec = matrix.transformS(endVec);
		
		startVec.add(middle);
		endVec.add(middle);
		
		RenderHelper.drawBoxOutline(
				matrixStackIn,
				new AxisAlignedBB(
						startVec.x - 0.1f, startVec.y - 0.1f, startVec.z - 0.1f,
						startVec.x + 0.1f, startVec.y + 0.1f, startVec.z + 0.1f
				), 255, 0, 0, 1
		);
		RenderHelper.drawBoxOutline(
				matrixStackIn,
				new AxisAlignedBB(
						endVec.x - 0.1f, endVec.y - 0.1f, endVec.z - 0.1f,
						endVec.x + 0.1f, endVec.y + 0.1f, endVec.z + 0.1f
				), 0, 255, 0, 1
		);
		RenderHelper.drawLine(
				matrixStackIn,
				startVec.x, startVec.y, startVec.z,
				endVec.x, endVec.y, endVec.z,
				0, 255, 0, 1
		);
		
		matrixStackIn.rotate(entityIn.getRotation());
		
		if (Minecraft.getInstance().gameSettings.showDebugInfo) {
			if (entityIn.getRaytraceShape() != null) {
				for (AxisAlignedBB bb : entityIn.getRaytraceShape().toBoundingBoxList()) {
					RenderSystem.enableDepthTest();
//					RenderHelper.drawBoxOutline(
//							matrixStackIn,
//							bb, 1, 1, 1, 1
//					);
					RenderHelper.drawBoxOutline(
							matrixStackIn, bb, 1, 1, 1, 1
					);
				}
			}
		}
		matrixStackIn.pop();
	}
	
	@Override
	public ResourceLocation getEntityTexture(DroneEntity entity) {
		return null;
	}
}
