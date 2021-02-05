package com.tfc.droneassembly;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.dynamics.*;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;

import net.minecraft.world.World;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.HashMap;

public class BulletPhysicsWorldCache {
	public static BulletPhysicsWorldCache INSTANCE = new BulletPhysicsWorldCache();
	
	private final BroadphaseInterface broadphase = new DbvtBroadphase();
	private final CollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
	private final CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);
	private final ConstraintSolver solver = new SequentialImpulseConstraintSolver();
	private final HashMap<RegistryKey<World>, DynamicsWorld> worldCache = new HashMap<>();
	public final HashMap<Integer, RigidBody> bodyCache = new HashMap<>();
	public final ArrayList<RigidBody> allBodies = new ArrayList<>();
	
	public DynamicsWorld getWorld(RegistryKey<World> dimensionRegistryKey) {
		if (!worldCache.containsKey(dimensionRegistryKey)) {
			DynamicsWorld dynamicsWorld = new SimpleDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
			worldCache.put(dimensionRegistryKey, dynamicsWorld);
		}
		return worldCache.get(dimensionRegistryKey);
	}
	
	public void clear() {
		worldCache.clear();
		bodyCache.clear();
	}
	
	public RigidBody add(DroneEntity entity) {
		float mass = entity.calcMass();
		
		CollisionShape shape = from(entity.getRaytraceShape());
		
		RigidBody collisionBody = createBody(shape, mass);
		
		Transform transform = collisionBody.getMotionState().getWorldTransform(new Transform());
		transform.setIdentity();
		transform.origin.set(entity.getPos());
		transform.setRotation(entity.getRotationAsQuat4f());
		collisionBody.getMotionState().setWorldTransform(transform);
		transform = collisionBody.getWorldTransform(new Transform());
		transform.setIdentity();
		transform.origin.set(entity.getPos());
		transform.setRotation(entity.getRotationAsQuat4f());
		collisionBody.setWorldTransform(transform);
		
		collisionBody.setFriction(0.25f);
		
		getWorld(entity.world.getDimensionKey()).addRigidBody(collisionBody);
		
		if (mass != 0) collisionBody.activate();
		else collisionBody.setDeactivationTime(1000000000);
		
		this.bodyCache.put(entity.getEntityId(), collisionBody);
		allBodies.add(collisionBody);
		
		return collisionBody;
	}
	
	private RigidBody createBody(CollisionShape shape, float mass) {
		Vector3f inertia = new Vector3f(0, 0, 0);
		shape.calculateLocalInertia(mass, inertia);
		
		Transform startingTransform = new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), new Vector3f(0, 0, 0), 1.0f));
		MotionState motionState = new DefaultMotionState(new Transform(startingTransform));
		RigidBodyConstructionInfo bodyConstructionInfo = new RigidBodyConstructionInfo(mass, motionState, shape, inertia);
		bodyConstructionInfo.angularDamping = 0.95f;
		
		bodyConstructionInfo.restitution = 1;
		
		RigidBody body = new RigidBody(bodyConstructionInfo);
		body.setActivationState(CollisionObject.DISABLE_DEACTIVATION);
		return body;
	}
	
//	public void add(RegistryKey<World> dim, VoxelShape shape, Vector3d pos) {
	public void add(RegistryKey<World> dim, VoxelShape shape) {
		if (shape != null) {
			CollisionShape shape1 = from(shape);
			RigidBody body = createBody(shape1, 0);
			body.setFriction(0.75f);
			getWorld(dim).addRigidBody(body);
			allBodies.add(body);
		}
	}
	
	public void removeGroundBodies() {
		worldCache.forEach((key, world) -> {
			ArrayList<CollisionObject> collisionObjectsToRemove = new ArrayList<>();
			world.getCollisionObjectArray().forEach(collisionObject -> {
				if (collisionObject.getFriction() == 0.75f) {
					collisionObject.setFriction(0.7f);
				} else if (collisionObject.getFriction() == 0.7f) {
					collisionObjectsToRemove.add(collisionObject);
				}
			});
			collisionObjectsToRemove.forEach((body) -> {
				if (body instanceof RigidBody) {
					try {
						world.removeRigidBody((RigidBody) body);
						allBodies.remove(body);
					} catch (Throwable ignored) {
					}
				} else world.removeCollisionObject(body);
			});
		});
	}
	
	public static void tick() {
		INSTANCE.worldCache.forEach((key, world) -> {
			try {
				world.setGravity(new Vector3f(0,-5,0));
				world.stepSimulation(1 / 30.0f, 64, 1f/60);
			} catch (Throwable err) {
				err.printStackTrace();
				ArrayList<CollisionObject> objects = new ArrayList<>(world.getCollisionObjectArray());
				objects.forEach((object)->{
					if (object instanceof RigidBody) {
						try {
							world.removeRigidBody((RigidBody) object);
							INSTANCE.allBodies.remove(object);
							for (int i = 0; i < INSTANCE.bodyCache.keySet().size(); i++) {
								if (INSTANCE.bodyCache.get(i).equals(object)) {
									INSTANCE.bodyCache.remove(i);
									break;
								}
							}
						} catch (Throwable err1) {
							err1.printStackTrace();
						}
					} else {
						world.removeCollisionObject(object);
					}
				});
			}
		});
		
//		try {
//			INSTANCE.worldCache.values().toArray(new DiscreteDynamicsWorld[0])[0].stepSimulation(1f/60);
//		} catch (Throwable err) {
//			StringBuilder exception = new StringBuilder(err.getLocalizedMessage()+"\n");
//			for (StackTraceElement element : err.getStackTrace())
//				exception.append(element.toString()).append("\n");
//			if (!exception.toString().equals("null\n"))
//			System.out.println(exception.toString());
//		}
		
		INSTANCE.removeGroundBodies();
	}
	
	private static CollisionShape from(VoxelShape shape) {
//		if (shape.toBoundingBoxList().size() == 1 || true) {
//			AxisAlignedBB bb = shape.getBoundingBox();
//			return new BoxShape(new Vector3f(
//					(float) (bb.maxX - bb.minX) / 2f,
//					(float) (bb.maxY - bb.minY) / 2f,
//					(float) (bb.maxZ - bb.minZ) / 2f
//			));
//		}
		CompoundShape shape1 = new CompoundShape();
		for (AxisAlignedBB bb : shape.toBoundingBoxList()) {
			Transform transform = new Transform();
			transform.setIdentity();
			transform.setRotation(new Quat4f(0,0,0,1));
			transform.origin.set((float)(bb.minX + bb.maxX)/2f,(float)(bb.minY + bb.maxY)/2f,(float)(bb.minZ + bb.maxZ)/2f);
			shape1.addChildShape(
					transform,
					new BoxShape(new Vector3f(
							(float) (bb.maxX - bb.minX) / 2f,
							(float) (bb.maxY - bb.minY) / 2f,
							(float) (bb.maxZ - bb.minZ) / 2f
					))
			);
//			ObjectArrayList<Vector3f> shapeArray = new ObjectArrayList<>();
//			shapeArray.addAll(
//					Arrays.asList(
//							new Vector3f((float) bb.minX, (float) bb.minY, (float) bb.minZ),
//							new Vector3f((float) bb.minX, (float) bb.maxY, (float) bb.minZ),
//							new Vector3f((float) bb.minX, (float) bb.maxY, (float) bb.maxZ),
//							new Vector3f((float) bb.minX, (float) bb.minY, (float) bb.maxZ),
//							new Vector3f((float) bb.minX, (float) bb.minY, (float) bb.minZ),
//
//
//							new Vector3f((float) bb.minX, (float) bb.minY, (float) bb.minZ),
//							new Vector3f((float) bb.minX, (float) bb.maxY, (float) bb.minZ),
//							new Vector3f((float) bb.maxX, (float) bb.maxY, (float) bb.minZ),
//							new Vector3f((float) bb.maxX, (float) bb.minY, (float) bb.minZ),
//							new Vector3f((float) bb.minX, (float) bb.minY, (float) bb.minZ)
//					)
//			);
//			shape1.addChildShape(
//					new Transform(),
//					new ConvexHullShape(shapeArray)
//			);
		}
		return shape1;
	}
	
	public boolean has(DroneEntity droneEntity) {
		return bodyCache.containsKey(droneEntity.getEntityId());
	}
	
	public Quaternion getRotation(DroneEntity droneEntity) {
		RigidBody body = bodyCache.get(droneEntity.getEntityId());
		Transform transform = body.getMotionState().getWorldTransform(new Transform());
		Quaternion quaternion = toQuaternion(transform.getRotation(new Quat4f()));
		quaternion.normalize();
//		transform.setRotation(new Quat4f(quaternion.getX(),quaternion.getY(),quaternion.getZ(),quaternion.getW()));
//		body.getMotionState().setWorldTransform(transform);
		
		return quaternion;
	}
	
	public Vector3d getPosition(DroneEntity droneEntity) {
		Vector3f pos = new Vector3f(0,0,0);
		if (bodyCache.containsKey(droneEntity.getEntityId())) {
			Transform transform = bodyCache.get(droneEntity.getEntityId()).getMotionState().getWorldTransform(new Transform());
			transform.transform(pos);
			return toPosition(pos);
		} else {
			return droneEntity.getPositionVec();
		}
	}
	
	private static Quaternion toQuaternion(Quat4f quat4f) {
		return new Quaternion(quat4f.x, quat4f.y, quat4f.z, quat4f.w);
	}
	
	private static Vector3d toPosition(Vector3f vec) {
		return new Vector3d(vec.x, vec.y, vec.z);
	}
	
	public void remove(DroneEntity entity) {
		worldCache.forEach((key, world) -> {
			if (world.getCollisionObjectArray().contains(bodyCache.get(entity.getEntityId()))) {
				RigidBody body = bodyCache.get(entity.getEntityId());
				world.removeRigidBody(body);
				allBodies.remove(body);
				bodyCache.remove(entity.getEntityId());
			}
		});
	}
}
