package com.tfc.droneassembly.parts;

public class CorePart extends DronePart {
	private static final DronePartRenderer renderer = new DronePartRenderer("core_block", true);
	@Override
	public DronePartRenderer getRenderer() {
		return renderer;
	}
	
	@Override
	public float calcMass() {
		return super.calcMass()*2;
	}
}
