package com.tfc.droneassembly.client;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.CompoundShapeChild;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.tfc.assortedutils.API.rendering.RenderHelper;
import com.tfc.droneassembly.BulletPhysicsWorldCache;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Quaternion;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import net.minecraftforge.client.event.RenderWorldLastEvent;

public class CollisionDebugRenderer {
	public static void renderWorldLast(RenderWorldLastEvent event) {
		MatrixStack stack = event.getMatrixStack();
//		Quaternion rotation = Minecraft.getInstance().getRenderManager().info.getRotation().copy();
//		rotation.multiply(-1);
		stack.translate(
				-Minecraft.getInstance().getRenderManager().info.getProjectedView().x,
				-Minecraft.getInstance().getRenderManager().info.getProjectedView().y,
				-Minecraft.getInstance().getRenderManager().info.getProjectedView().z
		);
//		stack.rotate(rotation);
		
		RenderSystem.pushMatrix();
		
//		RenderSystem.rotatef(Minecraft.getInstance().getRenderManager().info.getPitch(), 1, 0, 0);
//		RenderSystem.rotatef(Minecraft.getInstance().getRenderManager().info.getYaw() + 180, 0, 1, 0);
		
//		RenderSystem.translated(
//				-Minecraft.getInstance().getRenderManager().info.getProjectedView().x,
//				-Minecraft.getInstance().getRenderManager().info.getProjectedView().y,
//				-Minecraft.getInstance().getRenderManager().info.getProjectedView().z
//		);
		
		try {
			BulletPhysicsWorldCache.INSTANCE.allBodies.forEach((rigidBody) -> {
				RenderSystem.disableDepthTest();
				drawShape(rigidBody.getCollisionShape(),rigidBody,stack);
			});
		} catch (Throwable ignored) {
		}
		
		RenderSystem.popMatrix();
	}
	
	public static void drawShape(CollisionShape shape, RigidBody rigidBody, MatrixStack stack) {
		if (shape instanceof CompoundShape) {
			CompoundShape shape1 = (CompoundShape)shape;
			for (CompoundShapeChild compoundShapeChild : shape1.getChildList()) {
				stack.push();
				stack.translate(compoundShapeChild.transform.origin.x,compoundShapeChild.transform.origin.y,compoundShapeChild.transform.origin.z);
				Quat4f rot = compoundShapeChild.transform.getRotation(new Quat4f());
				stack.rotate(new Quaternion(rot.x,rot.y,rot.z,rot.w));
				drawShape(compoundShapeChild.childShape, rigidBody, stack);
				stack.pop();
			}
		} else if (shape instanceof BoxShape) {
			BoxShape shape1 = ((BoxShape) shape);
			Vector3f min = new Vector3f();
			Vector3f max = new Vector3f();
			shape1.getAabb(rigidBody.getWorldTransform(new Transform()), min, max);
			AxisAlignedBB bb = new AxisAlignedBB(min.x, min.y, min.z, max.x, max.y, max.z);
			RenderHelper.drawBoxOutline(stack, bb, 1, 1, 1, 1);
		}
	}
}
