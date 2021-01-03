package com.tfc.droneassembly;

import com.tfc.assortedutils.API.entities.IVoxelShapeEntity;
import com.tfc.assortedutils.API.entities.VoxelShapeEntityRaytraceResult;
import com.tfc.droneassembly.parts.DronePart;
import com.tfc.droneassembly.registries.DroneParts;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.MonsterEntity;
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
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class DroneEntity extends LivingEntity implements IVoxelShapeEntity {
	public static final DataParameter<CompoundNBT> dronePartParameter = EntityDataManager.createKey(DroneEntity.class, DataSerializers.COMPOUND_NBT);
	
	private Vector3d lastMotion = Vector3d.ZERO;
	
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
		AxisAlignedBB newBB = this.getBoundingBox();
		if (oldBox == null) {
			oldBox = newBB;
			return;
		}
		Vector3d oldCenter = oldBox.getCenter();
		Vector3d newCenter = newBB.getCenter();
		this.setPosition(
				this.getPositionVec().x,
				newBB.minY,
				this.getPositionVec().z
		);
		oldBox = newBB;
	}
	
	public CompoundNBT getPartData() {
		return this.getDataManager().get(dronePartParameter);
	}
	
	public void setPartData(CompoundNBT nbt) {
		this.getDataManager().set(dronePartParameter, nbt);
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
		if (!prevNBT.equals(this.getPartData()) || prevShape == null) {
			ListNBT parts = this.getPartData().getList("parts", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < parts.size(); i++) {
				CompoundNBT partNBT = parts.getCompound(i);
				DronePart part = DroneParts.get(partNBT.getString("name"));
				part.load(partNBT);
				if (shape == null) shape = part.getShape();
				else shape = VoxelShapes.or(shape, part.getShape().withOffset(part.x / 4f, part.y / 4f, part.z / 4f));
			}
			prevNBT = this.getPartData();
		}
		return shape;
	}
	
	@Override
	protected void doBlockCollisions() {
		super.doBlockCollisions();
	}
	
	@Override
	public void applyEntityCollision(@NotNull Entity entityIn) {
//		super.applyEntityCollision(entityIn);
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
		if (
				super.getBoundingBox().maxX - super.getBoundingBox().minX != bb.maxX - bb.minX ||
						super.getBoundingBox().maxY - super.getBoundingBox().minY != bb.maxY - bb.minY ||
						super.getBoundingBox().maxZ - super.getBoundingBox().minZ != bb.maxZ - bb.minZ
		)
			this.setBoundingBox(bb);
		return super.getBoundingBox();
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
		
		Vector3d oldPos = this.getPositionVec();
		
//		if (!lastMotion.equals(this.getMotion()) &&
//				(lastMotion.y <= 0 && lastMotion.z <= 0 && lastMotion.x <= 0)
//		) {
//			CompoundNBT partData = this.getPartData();
//			ListNBT partList = partData.getList("parts", Constants.NBT.TAG_COMPOUND);
//
//			for (int i = 0; i < partList.size(); i++) {
//				CompoundNBT partNBT = partList.getCompound(i);
//				DronePart part = DroneParts.get(partNBT.getString("name"));
//				part.load(partNBT);
//				BlockPos pos = new BlockPos(
//						part.x / 4f + this.getPositionVec().x,
//						part.y / 4f + this.getPositionVec().y,
//						part.z / 4f + this.getPositionVec().z
//				);
//				for (AxisAlignedBB bb : part.getShape().withOffset(this.getPositionVec().getX(),this.getPositionVec().y,this.getPositionVec().z).withOffset(part.x / 4f, part.y / 4f, part.z / 4f).toBoundingBoxList()) {
//					for (AxisAlignedBB bb1 : world.getBlockState(pos).getShape(world,pos).withOffset(pos.getX(),pos.getY(),pos.getZ()).toBoundingBoxList()) {
//						if (bb.intersects(bb1)) {
//							this.removePart(new BlockPos(part.x,part.y,part.z));
//							break;
//						}
//					}
//				}
//			}
//
//			Vector3d newPos = this.getPositionVec().add(this.getMotion());
//			this.setPosition(newPos.x,newPos.y,newPos.z);
//
//			for (int i = 0; i < partList.size(); i++) {
//				CompoundNBT partNBT = partList.getCompound(i);
//				DronePart part = DroneParts.get(partNBT.getString("name"));
//				part.load(partNBT);
//				BlockPos pos = new BlockPos(
//						part.x / 4f + this.getPositionVec().x,
//						part.y / 4f + this.getPositionVec().y,
//						part.z / 4f + this.getPositionVec().z
//				);
//				for (AxisAlignedBB bb : part.getShape().withOffset(this.getPositionVec().getX(),this.getPositionVec().y,this.getPositionVec().z).withOffset(part.x / 4f, part.y / 4f, part.z / 4f).toBoundingBoxList()) {
//					for (AxisAlignedBB bb1 : world.getBlockState(pos).getShape(world,pos).withOffset(pos.getX(),pos.getY(),pos.getZ()).toBoundingBoxList()) {
//						if (bb.intersects(bb1)) {
//							this.setPosition(oldPos.x,oldPos.y+0,oldPos.z);
//							this.setMotion(0,0,0);
//							break;
//						}
//					}
//				}
//			}
//		}
		
		lastMotion = this.getMotion();
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
		if (compound.contains("parts")) this.setPartData(compound.getCompound("parts"));
		else this.setPartData(getDefaultNBT());
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
		System.out.println(result.getDir());
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
		
		System.out.println(hitPos);

		if (!containsPart(hitPos))
			addPart("drone_assembly:basic_part", 0.5f, 0.5f, 0.5f, hitPos.getX(), hitPos.getY(), hitPos.getZ());
		
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
		setPartData(partData);
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
}
