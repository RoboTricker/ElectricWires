package de.robotricker.electricwires.duct.wire;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.logisticscraft.logisticsapi.LogisticsApi;
import com.logisticscraft.logisticsapi.block.LogisticBlock;
import com.logisticscraft.logisticsapi.energy.EnergyInput;
import com.logisticscraft.logisticsapi.energy.EnergyOutput;
import com.logisticscraft.logisticsapi.energy.EnergyStorage;

import de.robotricker.electricwires.ElectricWires;
import de.robotricker.electricwires.WireNetwork;
import de.robotricker.electricwires.duct.wire.utils.WireType;
import de.robotricker.transportpipes.duct.ClickableDuct;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctType;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.WrappedDirection;
import de.robotricker.transportpipes.utils.tick.TickData;

public abstract class Wire extends Duct implements ClickableDuct{

	private WireNetwork network;

	public Wire(Location blockLoc) {
		super(blockLoc);
	}

	public void setWireNetwork(WireNetwork network) {
		this.network = network;
	}

	public WireNetwork getWireNetwork() {
		return network;
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

		Map<Chunk, Map<Location, LogisticBlock>> lbMap = LogisticsApi.getInstance().getBlockManager().getPlacedBlocks();
		synchronized (lbMap) {
			for (WrappedDirection dir : WrappedDirection.values()) {
				Location newLoc = getBlockLoc().clone().add(dir.getX(), dir.getY(), dir.getZ());
				for (Chunk lbChunk : lbMap.keySet()) {
					if (!lbChunk.getWorld().equals(getBlockLoc().getWorld())) {
						continue;
					}
					for (Location lbLoc : lbMap.get(lbChunk).keySet()) {
						LogisticBlock lb = lbMap.get(lbChunk).get(lbLoc);
						if (lb instanceof EnergyStorage && newLoc.equals(lb.getLocation().getLocation().get())) {
							dirs.add(dir);
						}
					}
				}
			}
		}

		return dirs;
	}

	@Override
	public DuctType getDuctType() {
		return DuctType.WIRE;
	}
	
	@Override
	public void click(Player p, WrappedDirection side) {
		p.sendMessage("Energy: " + getWireNetwork().getNetworkCharge());
	}

}
