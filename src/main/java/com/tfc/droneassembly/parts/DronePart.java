package com.tfc.droneassembly.parts;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;

public class DronePart implements IForgeRegistryEntry<DronePart> {
	public ResourceLocation key;
	public static final DronePartRenderer renderer = new DronePartRenderer("basic_block");
	public double x;
	public double y;
	public double z;
	public float r,g,b;
	public String type;
	
	@Override
	public DronePart setRegistryName(ResourceLocation name) {
		key = name;
		return this;
	}
	
	public void load(CompoundNBT nbt) {
		if (nbt.contains("x")) {
			this.x = nbt.getDouble("x");
			this.y = nbt.getDouble("y");
			this.z = nbt.getDouble("z");
		} else {
			this.x=0;
			this.y=0;
			this.z=0;
		}
		this.type = nbt.getString("name");
		this.r = nbt.getFloat("r");
		this.b = nbt.getFloat("g");
		this.g = nbt.getFloat("b");
	}
	
	@Nullable
	@Override
	public ResourceLocation getRegistryName() {
		return key;
	}
	
	@Override
	public Class<DronePart> getRegistryType() {
		return DronePart.class;
	}
	
	public DronePartRenderer getRenderer() {
		return renderer;
	}
	
	private static final VoxelShape shape = VoxelShapes.create(0, 0, 0, 0.25f, 0.25f, 0.25f);
	
	public VoxelShape getShape() {
		return shape;
	}
	
	public CompoundNBT serializeNBT() {
		CompoundNBT part = new CompoundNBT();
		part.putString("name", type);
		part.putFloat("red", r);
		part.putFloat("green", g);
		part.putFloat("blue", b);
		part.putDouble("x", x);
		part.putDouble("y", y);
		part.putDouble("z", z);
		return part;
	}
}
