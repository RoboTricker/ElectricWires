package de.robotricker.electricwires.utils.ductdetails;

import org.bukkit.Location;

import de.robotricker.electricwires.duct.wire.ColoredWire;
import de.robotricker.electricwires.duct.wire.utils.WireColor;
import de.robotricker.electricwires.duct.wire.utils.WireType;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctType;
import de.robotricker.transportpipes.duct.pipe.ColoredPipe;
import de.robotricker.transportpipes.duct.pipe.ExtractionPipe;
import de.robotricker.transportpipes.duct.pipe.GoldenPipe;
import de.robotricker.transportpipes.duct.pipe.IcePipe;
import de.robotricker.transportpipes.duct.pipe.IronPipe;
import de.robotricker.transportpipes.duct.pipe.VoidPipe;
import de.robotricker.transportpipes.duct.pipe.utils.PipeColor;
import de.robotricker.transportpipes.duct.pipe.utils.PipeType;
import de.robotricker.transportpipes.utils.ductdetails.DuctDetails;
import de.robotricker.transportpipes.utils.ductdetails.PipeDetails;
import io.sentry.Sentry;

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
		setCraftPermission(wireType.getCraftPermission());
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
		if (wireType != other.wireType)
			return false;
		if (wireType == WireType.COLORED && wireColor != other.wireColor)
			return false;
		return true;
	}

	@Override
	public String toString() {
		String pipeTypeString = "WireType:" + wireType.name() + ";";
		String pipeColorString = wireColor != null ? "WireColor:" + wireColor.name() + ";" : "";
		return super.toString() + pipeTypeString + pipeColorString;
	}

	@Override
	public void fromString(String serialization) {
		try {
			for (String element : serialization.split(";")) {
				if (element.startsWith("WireType:")) {
					setWireType(WireType.valueOf(element.substring(9)));
				} else if (element.startsWith("WireColor:")) {
					setWireColor(WireColor.valueOf(element.substring(10)));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Sentry.capture(e);
		}
	}

}
