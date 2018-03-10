package de.robotricker.electricwires.utils.ductdetails;

import java.util.Locale;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import de.robotricker.electricwires.duct.wire.ColoredWire;
import de.robotricker.electricwires.duct.wire.utils.WireColor;
import de.robotricker.electricwires.duct.wire.utils.WireType;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctType;
import de.robotricker.transportpipes.utils.ductdetails.DuctDetails;
import de.robotricker.transportpipes.utils.staticutils.DuctItemUtils;

public class WireDetails extends DuctDetails {

	private WireType wireType;
	private WireColor wireColor;

	/**
	 * creates new PipeDetails with PipeType pipeType (use other constructor for
	 * COLORED pipeType)
	 */
	public WireDetails(WireType wireType) {
		super(DuctType.WIRE, wireType.getCraftPermission());
		this.wireType = wireType;
	}

	public WireDetails() {
		super(DuctType.WIRE, null);
	}

	/**
	 * automatically sets WireType to COLORED
	 */
	public WireDetails(WireColor wireColor) {
		super(DuctType.WIRE, WireType.COLORED.getCraftPermission());
		this.wireType = WireType.COLORED;
		this.wireColor = wireColor;
	}

	public WireType getWireType() {
		return wireType;
	}

	public WireColor getWireColor() {
		return wireColor;
	}

	public void setWireType(WireType wireType) {
		this.wireType = wireType;
		if (wireType != null) {
			setCraftPermission(wireType.getCraftPermission());
		}
	}

	public void setWireColor(WireColor wireColor) {
		this.wireColor = wireColor;
	}

	@Override
	public Duct createDuct(Location blockLoc) {
		if (getWireType() == WireType.COLORED) {
			return new ColoredWire(blockLoc, getWireColor());
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ductType == null) ? 0 : ductType.hashCode());
		result = prime * result + ((wireColor == null) ? 0 : wireColor.hashCode());
		result = prime * result + ((wireType == null) ? 0 : wireType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WireDetails other = (WireDetails) obj;
		if (wireType == null || other.wireType == null)
			return true;
		if (wireType != other.wireType)
			return false;
		if (wireType == WireType.COLORED && wireColor != null && other.wireColor != null && wireColor != other.wireColor)
			return false;
		return true;
	}

	@Override
	public String toString() {
		String wireTypeString = wireType != null ? ":" + wireType.name().toLowerCase(Locale.ENGLISH) : "";
		String wireColorString = wireColor != null ? ":" + wireColor.name().toLowerCase(Locale.ENGLISH) : "";
		return super.toString() + wireTypeString + wireColorString;
	}

	@Override
	public void deserialize(String serialization) {
		try {
			String wireTypeName = null;
			if (serialization.contains("WireType:")) {
				wireTypeName = serialization.split(";")[1].split(":")[1];
			} else if (serialization.split(":").length >= 2) {
				wireTypeName = serialization.split(":")[1];
			}
			setWireType(wireTypeName != null ? WireType.valueOf(wireTypeName.toUpperCase(Locale.ENGLISH)) : null);
			if (getWireType() == WireType.COLORED) {
				String wireColorName = null;
				if (serialization.contains("WireColor:")) {
					wireColorName = serialization.split(";")[2].split(":")[1];
				} else if (serialization.split(":").length >= 3) {
					wireColorName = serialization.split(":")[2];
				}
				setWireColor(wireColorName != null ? WireColor.valueOf(wireColorName.toUpperCase(Locale.ENGLISH)) : null);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(serialization + " does not fit the serialization format");
		}

	}

	@Override
	public boolean doesItemStackMatchesDuctDetails(ItemStack itemStack) {
		DuctDetails dd = DuctItemUtils.getDuctDetailsOfItem(itemStack);
		if (dd != null && ductType == dd.getDuctType()) {
			WireDetails wd = (WireDetails) dd;
			if (wireType == null) {
				return true;
			} else if (wireColor == null) {
				return wireType == wd.wireType;
			} else {
				return wireType == wd.wireType && wireColor == wd.wireColor;
			}
		}
		return false;
	}

}
