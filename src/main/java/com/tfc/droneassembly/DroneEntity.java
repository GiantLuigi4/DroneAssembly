package com.tfc.droneassembly;

import com.tfc.assortedutils.API.entities.IVoxelShapeEntity;
import com.tfc.assortedutils.API.entities.VoxelShapeEntityRaytraceResult;
import com.tfc.assortedutils.API.nbt.ExtendedCompoundNBT;
import com.tfc.assortedutils.API.raytracing.RotatedVoxelShape;
import com.tfc.assortedutils.API.transformations.quaternion.QuaternionHelper;
import com.tfc.droneassembly.parts.DronePart;
import com.tfc.droneassembly.registries.DroneParts;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;

public class DroneEntity extends LivingEntity implements IVoxelShapeEntity {
	public static final DataParameter<CompoundNBT> dronePartParameter = EntityDataManager.createKey(DroneEntity.class, DataSerializers.COMPOUND_NBT);
	
	public DroneEntity(EntityType<? extends LivingEntity> type, World worldIn) {
		super(type, worldIn);
	}
	
	public static AttributeModifierMap createModifiers() {
		return MonsterEntity.func_234295_eP_()
				.createMutableAttribute(Attributes.FLYING_SPEED, 0)
				.createMutableAttribute(Attributes.MOVEMENT_SPEED, 0)
				.createMutableAttribute(Attributes.ATTACK_DAMAGE, 0)
				.createMutableAttribute(Attributes.ARMOR, 0)
				.createMutableAttribute(Attributes.MAX_HEALTH, 1).create();
	}
	
	private static final CompoundNBT defaultNBT = new CompoundNBT();
	
	public static CompoundNBT getDefaultNBT() {
		return defaultNBT.copy();
	}
	
	static {
		{
			ListNBT partList = new ListNBT();
			{
				CompoundNBT part = new CompoundNBT();
				{
					part.putString("name", "drone_assembly:drone_core");
					part.putFloat("red", 0.5f);
					part.putFloat("green", 0.5f);
					part.putFloat("blue", 0.5f);
					part.putDouble("x", 0.0);
					part.putDouble("y", 0.0);
					part.putDouble("z", 0.0);
				}
				partList.add(part);
			}
			defaultNBT.put("parts", partList);
		}
	}
	
	@Override
	protected void registerData() {
		super.registerData();
		this.dataManager.register(dronePartParameter, defaultNBT.copy());
	}
	
	private AxisAlignedBB oldBox = null;
	
	@Override
	public void resetPositionToBB() {
//		AxisAlignedBB newBB = this.getBoundingBox();
//		if (oldBox == null) {
//			oldBox = newBB;
//			return;
//		}
//		Vector3d oldCenter = oldBox.getCenter();
//		Vector3d newCenter = newBB.getCenter();
//		this.setPosition(
//				this.getPositionVec().x,
//				newBB.minY,
//				this.getPositionVec().z
//		);
//		oldBox = newBB;
	}
	
	public CompoundNBT getPartData() {
		return dataManager.get(dronePartParameter);
	}
	
	public void setPartData(CompoundNBT nbt) {
		this.dataManager.set(dronePartParameter, nbt);
		try {
 			Field field = ObfuscationReflectionHelper.findField(EntityDataManager.class,"field_187237_f");
			Field field2 = ObfuscationReflectionHelper.findField(EntityDataManager.class,"field_187234_c");
			field.setAccessible(true);
			field2.setAccessible(true);
			field.set(this.dataManager,true);
			
			((Map<Integer, EntityDataManager.DataEntry<?>>)field2.get(this.dataManager)).forEach((id,entry)->{
				if (entry.getKey().equals(dronePartParameter)) {
					entry.setDirty(true);
				}
			});
		} catch (Throwable ignored) {
			ignored.printStackTrace();
		}
	}
	
	public void addPart(CompoundNBT nbt) {
		CompoundNBT partData = getPartData();
		ListNBT partList = partData.getList("parts", Constants.NBT.TAG_COMPOUND);
		partList.add(nbt);
		partData.put("parts", partList);
		setPartData(partData);
	}
	
	public void addPart(String type, float r, float g, float b, double x, double y, double z) {
		CompoundNBT part = new CompoundNBT();
		{
			part.putString("name", type);
			part.putFloat("red", r);
			part.putFloat("green", g);
			part.putFloat("blue", b);
			part.putDouble("x", x);
			part.putDouble("y", y);
			part.putDouble("z", z);
		}
		addPart(part);
	}
	
	public void addPart(@NotNull DronePart part) {
		addPart(part.serializeNBT());
	}
	
	@Override
	@NotNull
	public Iterable<ItemStack> getArmorInventoryList() {
		return new ArrayList<>();
	}
	
	@Override
	@NotNull
	public ItemStack getItemStackFromSlot(@NotNull EquipmentSlotType slotIn) {
		return ItemStack.EMPTY;
	}
	
	@Override
	public void setItemStackToSlot(@NotNull EquipmentSlotType slotIn, @NotNull ItemStack stack) {
	}
	
	@Override
	@NotNull
	public HandSide getPrimaryHand() {
		return HandSide.LEFT;
	}
	
	@Override
	protected void playHurtSound(@NotNull DamageSource source) {
		this.playSound(Blocks.IRON_BLOCK.getSoundType(Blocks.IRON_BLOCK.getDefaultState()).getHitSound(), this.getSoundVolume(), this.getSoundPitch());
	}
	
	@Nullable
	@Override
	protected SoundEvent getDeathSound() {
		return Blocks.IRON_BLOCK.getSoundType(Blocks.IRON_BLOCK.getDefaultState()).getBreakSound();
	}
	
	@Nullable
	@Override
	protected SoundEvent getHurtSound(@NotNull DamageSource damageSourceIn) {
		return null;
	}
	
	public CompoundNBT prevNBT = new CompoundNBT();
	public VoxelShape prevShape = null;
	
	@Override
	public VoxelShape getRaytraceShape() {
		VoxelShape shape = prevShape;
		if (prevNBT != null && !prevNBT.equals(this.getPartData())) {
			prevShape = null;
			shape = null;
			ListNBT parts = this.getPartData().getList("parts", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < parts.size(); i++) {
				CompoundNBT partNBT = parts.getCompound(i);
				DronePart part = DroneParts.get(partNBT.getString("name"));
				part.load(partNBT);
				if (shape == null) shape = part.getShape().withOffset(part.x / 4f, part.y / 4f, part.z / 4f);
				else shape = VoxelShapes.or(shape, part.getShape().withOffset(part.x / 4f, part.y / 4f, part.z / 4f));
			}
			prevNBT = this.getPartData().copy();
			prevShape = shape;
			if (prevShape != null) prevShape = new RotatedVoxelShape(prevShape);
			BulletPhysicsWorldCache.INSTANCE.remove(this);
		}
		if (prevShape instanceof RotatedVoxelShape) {
			if (BulletPhysicsWorldCache.INSTANCE.has(this)) ((RotatedVoxelShape)prevShape).rotation = BulletPhysicsWorldCache.INSTANCE.getRotation(this);
			else ((RotatedVoxelShape)prevShape).rotation = this.getRotation();
		}
		return shape;
	}
	
	//TODO: figure out some way to cache this
	public VoxelShape getTerrainShape() {
		AxisAlignedBB bb = this.getBoundingBox();
		VoxelShape shape = null;
		ArrayList<BlockPos> donePoses = new ArrayList<>();
		for (int x = (int) (bb.minX - 2); x <= (int) (bb.maxX + 2); x += 1) {
			for (int y = (int) (bb.minY - 2); y <= (int) (bb.maxY + 2); y += 1) {
				for (int z = (int) (bb.minZ - 2); z <= (int) (bb.maxZ + 2); z += 1) {
					BlockPos pos = new BlockPos(x, y, z);
					if (!donePoses.contains(pos)) {
						donePoses.add(pos);
						BlockState state = world.getBlockState(pos);
						if (!state.isAir() && state.isSolid()) {
							if (shape == null) shape = state.getCollisionShape(world, pos).withOffset(x, y, z);
							else
								shape = VoxelShapes.combine(
										shape,
										state.getCollisionShape(world, pos).withOffset(x, y, z),
										IBooleanFunction.OR
								);
						}
					}
				}
			}
		}
		if (shape != null) return shape;
		else return null;
	}
	
	@Override
	protected void doBlockCollisions() {
//		super.doBlockCollisions();
	}
	
	@Override
	public void applyEntityCollision(@NotNull Entity entityIn) {
	}
	
	@Override
	@NotNull
	public AxisAlignedBB getBoundingBox() {
		AxisAlignedBB bb = new AxisAlignedBB(0, 0, 0, 0.1f, 0.1f, 0.1f);
		try {
			bb = getRaytraceShape().getBoundingBox();
		} catch (Throwable ignored) {
		}
		bb = bb.offset(this.getPositionVec().subtract(0.125f, 0, 0).x, this.getPositionVec().y, this.getPositionVec().subtract(0, 0, 0.125f).z);
//		if (
//				super.getBoundingBox().maxX - super.getBoundingBox().minX != bb.maxX - bb.minX ||
//						super.getBoundingBox().maxY - super.getBoundingBox().minY != bb.maxY - bb.minY ||
//						super.getBoundingBox().maxZ - super.getBoundingBox().minZ != bb.maxZ - bb.minZ
//		)
//			return bb;
		return bb;
	}
	
	@Override
	@NotNull
	protected AxisAlignedBB getBoundingBox(@NotNull Pose pose) {
		return getBoundingBox();
	}
	
	@Override
	public void livingTick() {
		super.livingTick();
		this.fallDistance = 0;
		
		if (this.world.isRemote) return;
		
		VoxelShape terrainShape = getTerrainShape();
//		VoxelShape terrainShape = null;
		if (terrainShape == null) {
			BulletPhysicsWorldCache.INSTANCE.remove(this);
			return;
		}
		
		if (BulletPhysicsWorldCache.INSTANCE.has(this)) {
			setPosition(BulletPhysicsWorldCache.INSTANCE.getPosition(this));
			Quaternion quaternion = BulletPhysicsWorldCache.INSTANCE.getRotation(this);
			quaternion.normalize();
			this.setRotation(quaternion);
		} else
			BulletPhysicsWorldCache.INSTANCE.add(this);
		
		if (terrainShape != null)
			BulletPhysicsWorldCache.INSTANCE.add(this.world.getDimensionKey(),terrainShape);
	}
	
	public void setPosition(Vector3d newPos) {
		this.setPosition(newPos.x,newPos.y,newPos.z);
	}
	
	@Override
	protected void recenterBoundingBox() {
		super.recenterBoundingBox();
	}
	
	@Override
	public void writeAdditional(@NotNull CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.put("parts", this.getPartData());
	}
	
	@Override
	public void readAdditional(@NotNull CompoundNBT compound) {
		super.readAdditional(compound);
		if (!this.world.isRemote) {
			if (compound.contains("parts")) this.setPartData(compound.getCompound("parts"));
			else this.setPartData(getDefaultNBT());
		}
	}
	
	public float calcMass() {
		float mass = 0;
		
		CompoundNBT partData = this.getPartData();
		ListNBT partList = partData.getList("parts", Constants.NBT.TAG_COMPOUND);
		for (int i = partList.size() - 1; i > 0; i--) {
			CompoundNBT partNBT = partList.getCompound(i);
			DronePart part = DroneParts.get(partNBT.getString("name"));
			part.load(partNBT);
			mass += part.calcMass();
		}
		
		return mass;
	}
	
	@Override
	@NotNull
	public ActionResultType applyPlayerInteraction(@NotNull PlayerEntity player, @NotNull Vector3d vec, @NotNull Hand hand) {
		if (FMLEnvironment.dist.isClient() && hand.equals(Hand.MAIN_HAND))
			if (Minecraft.getInstance().objectMouseOver instanceof VoxelShapeEntityRaytraceResult)
				return this.onInteract(player, (VoxelShapeEntityRaytraceResult) Minecraft.getInstance().objectMouseOver);
		
		super.applyPlayerInteraction(player, vec, hand);
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public ActionResultType onInteract(PlayerEntity player, @NotNull VoxelShapeEntityRaytraceResult result) {
//		System.out.println(result.getDir());
		Vector3d pos = result.getHitVec().subtract(result.getEntity().getPositionVec()).scale(4);
		BlockPos hitPos = new BlockPos(
				Math.round(pos.x),
				Math.floor(pos.y),
				Math.round(pos.z)
		);
		
		if (result.getDir() == Direction.EAST) hitPos = hitPos.west();
		else if (result.getDir() == Direction.SOUTH) hitPos = hitPos.north();
		else if (result.getDir() == Direction.UP) hitPos = hitPos.down();
		hitPos = hitPos.offset(result.getDir());
		
//		System.out.println(hitPos);

		if (!containsPart(hitPos) && !world.isRemote)
			addPart("drone_assembly:basic_part", 0.5f, 0.5f, 0.5f, hitPos.getX(), hitPos.getY(), hitPos.getZ());
		System.out.println(dataManager.isDirty());
		if (dataManager.getAll() != null) {
			for (EntityDataManager.DataEntry<?> entry : dataManager.getAll()) {
				if (entry.getKey().getId() == dronePartParameter.getId()) {
//					entry.setDirty(true);
					System.out.println(entry.isDirty());
				}
			}
		}
		System.out.println(dataManager.isDirty());
		
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		ListNBT list = ((ListNBT) this.getPartData().get("parts"));
		if (source.getTrueSource() != null) {
			BlockRayTraceResult result;
			{
				Vector3d pos = source.getTrueSource().getPositionVec().add(0, source.getTrueSource().getEyeHeight(), 0).subtract(this.getPositionVec());
				Vector3d reach = pos.add(source.getTrueSource().getLookVec().scale(8));
				
				System.out.println(pos + "," + reach);
				
				result = ((IVoxelShapeEntity) this).getRaytraceShape().withOffset(
						-(0.25f) / 2, 0, -(0.25f) / 2
				).withOffset(0, 0, 0).rayTrace(
						pos, reach, new BlockPos(0, 0, 0)
				);
			}
			
			if (result != null) {
				Vector3d pos = result.getHitVec().scale(4);
				BlockPos hitPos = new BlockPos(
						Math.round(pos.x),
						Math.floor(pos.y),
						Math.round(pos.z)
				);
				
				if (result.getFace() == Direction.EAST) hitPos = hitPos.west();
				else if (result.getFace() == Direction.SOUTH) hitPos = hitPos.north();
				else if (result.getFace() == Direction.UP) hitPos = hitPos.down();
				
				System.out.println(hitPos);
				
				if (list != null && list.size() <= 1)
					return super.attackEntityFrom(source, amount);
				else return removePart(hitPos);
			}
		}
		
		System.out.println(source.damageType);
		
		if (source.damageType.equals("outOfWorld")) return super.attackEntityFrom(source, amount);
		
		if (list != null && list.size() <= 1) return super.attackEntityFrom(source, amount);
		else return false;
	}
	
	public boolean removePart(BlockPos hitPos) {
		CompoundNBT partData = this.getPartData();
		ListNBT partList = partData.getList("parts", Constants.NBT.TAG_COMPOUND);
		ArrayList<Integer> toRemove = new ArrayList<>();
		for (int i = partList.size() - 1; i > 0; i--) {
			CompoundNBT partNBT = partList.getCompound(i);
			if (
					((int) partNBT.getDouble("x")) == hitPos.getX() &&
							((int) partNBT.getDouble("y")) == hitPos.getY() &&
							((int) partNBT.getDouble("z")) == hitPos.getZ()
			) {
				toRemove.add(i);
			}
		}
		for (int i : toRemove)
			partList.remove(i);
		partData.put("parts", partList);
		if (!world.isRemote) setPartData(partData);
		return !toRemove.isEmpty();
	}
	
	public boolean containsPart(BlockPos hitPos) {
		CompoundNBT partData = this.getPartData();
		ListNBT partList = partData.getList("parts", Constants.NBT.TAG_COMPOUND);
		if (hitPos.getX() == 0 && hitPos.getY() == 0 && hitPos.getZ() == 0)
			return true;
		for (int i = partList.size() - 1; i > 0; i--) {
			CompoundNBT partNBT = partList.getCompound(i);
			if (
					((int) partNBT.getDouble("x")) == hitPos.getX() &&
							((int) partNBT.getDouble("y")) == hitPos.getY() &&
							((int) partNBT.getDouble("z")) == hitPos.getZ()
			) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	@NotNull
	public EntitySize getSize(@NotNull Pose poseIn) {
		AxisAlignedBB bb = new AxisAlignedBB(0, 0, 0, 0.1f, 0.1f, 0.1f);
		try {
			bb = getRaytraceShape().getBoundingBox();
		} catch (Throwable ignored) {
		}
		return new EntitySize((float) Math.min(bb.maxX - bb.minX, bb.maxZ - bb.minZ), (float) (bb.maxY - bb.minY), false);
	}
	
	public Vector3f getPos() {
		return new Vector3f(
				(float) this.getPosX(),
				(float) this.getPosY(),
				(float) this.getPosZ()
		);
	}
	
	public void setRotation(Quaternion quaternion) {
		ExtendedCompoundNBT partData = new ExtendedCompoundNBT(this.getPartData(),true);
		partData.putQuaternion("rotation",quaternion);
		setPartData(this.getPartData());
	}
	
	public void setRotation(Quat4f quaternion) {
		setRotation(new Quaternion(quaternion.x, quaternion.y, quaternion.z, quaternion.w));
	}
	
	public Quaternion getRotation() {
		CompoundNBT partData = this.getPartData();
		if (partData.contains("rotation")) {
			Quaternion quaternion = new ExtendedCompoundNBT(partData,true).getQuaternion("rotation");
			return quaternion;
		} else {
			return QuaternionHelper.fromAngles(0,0,0,false);
		}
	}
	
	public Quat4f getRotationAsQuat4f() {
		Quaternion quaternion = getRotation();
		return new Quat4f(
				quaternion.getX(),
				quaternion.getY(),
				quaternion.getZ(),
				quaternion.getW()
		);
	}
	
	/**
	 * Returns true if other Entities should be prevented from moving through this Entity.
	 */
	@Override
	public boolean canBeCollidedWith() {
		return true;
	}
	
	/**
	 * Returns true if this entity should push and be pushed by other entities when colliding.
	 */
	@Override
	public boolean canBePushed() {
		return false;
	}
}
