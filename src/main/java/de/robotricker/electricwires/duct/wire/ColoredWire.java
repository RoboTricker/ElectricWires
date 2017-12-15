package de.robotricker.electricwires.duct.wire;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import de.robotricker.electricwires.duct.wire.utils.WireColor;
import de.robotricker.electricwires.duct.wire.utils.WireType;
import de.robotricker.electricwires.utils.ductdetails.WireDetails;
import de.robotricker.transportpipes.utils.ductdetails.DuctDetails;
import de.robotricker.transportpipes.utils.staticutils.DuctItemUtils;

public class ColoredWire extends Wire {

	private WireColor wireColor;
	
	public ColoredWire(Location blockLoc, WireColor wireColor) {
		super(blockLoc);
		this.wireColor = wireColor;
	}
	
	
	public WireColor getWireColor() {
		return wireColor;
	}
	
	@Override
	public int[] getBreakParticleData() {
		return new int[] { getWireColor().getDyeItem().getTypeId(), getWireColor().getDyeItem().getDurability() };
	}
	
	@Override
	public WireType getWireType() {
		return WireType.COLORED;
	}
	
	@Override
	public List<ItemStack> getDroppedItems() {
		List<ItemStack> is = new ArrayList<>();
		is.add(DuctItemUtils.getClonedDuctItem(new WireDetails(getWireColor())));
		return is;
	}
	
	@Override
	public DuctDetails getDuctDetails() {
		return new WireDetails(getWireColor());
	}
	
}
