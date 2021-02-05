package com.tfc.assortedutils.API.raytracing;

import com.tfc.assortedutils.API.transformations.matrix.Matrix4fDistSafe;
import com.tfc.assortedutils.API.transformations.quaternion.QuaternionHelper;
import com.tfc.assortedutils.mixins.VoxelShapeAccessor;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.List;

public class RotatedVoxelShape extends VoxelShape {
	public Quaternion rotation;
	private final VoxelShape shape;
	
	public RotatedVoxelShape(VoxelShape shape) {
		super(((VoxelShapeAccessor) shape).getPart());
		this.shape = shape;
		this.rotation = QuaternionHelper.fromAngles(0, 0, 0, false);
	}
	
	public RotatedVoxelShape(VoxelShape shape, Quaternion quaternion) {
		super(((VoxelShapeAccessor) shape).getPart());
		this.shape = shape;
		this.rotation = quaternion;
	}
	
	@Override
	protected DoubleList getValues(Direction.Axis axis) {
		return ((VoxelShapeAccessor) shape).doGetValues(axis);
	}
	
	@Nullable
	@Override
	public BlockRayTraceResult rayTrace(Vector3d startVec, Vector3d endVec, BlockPos pos) {
		Vector3d middle = startVec.add(endVec).scale(0.5f);
		startVec = startVec.subtract(middle);
		endVec = endVec.subtract(middle);
		
		Matrix4fDistSafe matrix = new Matrix4fDistSafe(rotation);
		startVec = matrix.transformS(startVec);
		endVec = matrix.transformS(endVec);
		
		startVec.add(middle);
		endVec.add(middle);
		
		if (shape != null) return shape.rayTrace(startVec, endVec, pos);
		return super.rayTrace(startVec, endVec, pos);
	}
	
	@Override
	public AxisAlignedBB getBoundingBox() {
		if (shape != null) return shape.getBoundingBox();
		return super.getBoundingBox();
	}
	
	@Override
	public void forEachBox(VoxelShapes.ILineConsumer action) {
		if (shape != null) shape.forEachBox(action);
		else super.forEachBox(action);
	}
	
	@Override
	public void forEachEdge(VoxelShapes.ILineConsumer action) {
		if (shape != null) shape.forEachEdge(action);
		else super.forEachEdge(action);
	}
	
	@Override
	public List<AxisAlignedBB> toBoundingBoxList() {
		if (shape != null) return shape.toBoundingBoxList();
		return super.toBoundingBoxList();
	}
}
