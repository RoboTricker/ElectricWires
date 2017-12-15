package de.robotricker.electricwires.duct.wire;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import de.robotricker.electricwires.ElectricWires;
import de.robotricker.electricwires.api.ElectricWiresContainer;
import de.robotricker.electricwires.duct.wire.utils.WireType;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctType;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.WrappedDirection;
import de.robotricker.transportpipes.utils.tick.TickData;

public abstract class Wire extends Duct {

	public Wire(Location blockLoc) {
		super(blockLoc);
	}

	@Override
	public void tick(TickData tickData) {

	}
	
	public abstract WireType getWireType();

	@Override
	public boolean canConnectToDuct(Duct duct) {
		if (duct instanceof Wire) {
			Wire neighborWire = (Wire) duct;
			if (neighborWire.getWireType() == WireType.COLORED && getWireType() == WireType.COLORED) {
				if (!((ColoredWire) neighborWire).getWireColor().equals(((ColoredWire) this).getWireColor())) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public List<WrappedDirection> getOnlyBlockConnections() {
		List<WrappedDirection> dirs = new ArrayList<>();

		Map<BlockLoc, ElectricWiresContainer> containerMap = ElectricWires.instance.getContainerMap(getBlockLoc().getWorld());

		if (containerMap != null) {
			for (WrappedDirection dir : WrappedDirection.values()) {
				Location blockLoc = getBlockLoc().clone().add(dir.getX(), dir.getY(), dir.getZ());
				BlockLoc bl = BlockLoc.convertBlockLoc(blockLoc);
				if (containerMap.containsKey(bl)) {
					dirs.add(dir);
				}
			}
		}

		return dirs;
	}

	@Override
	public DuctType getDuctType() {
		return DuctType.WIRE;
	}

}
