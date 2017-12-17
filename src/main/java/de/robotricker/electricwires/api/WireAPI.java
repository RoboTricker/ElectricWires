package de.robotricker.electricwires.api;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Location;

import de.robotricker.electricwires.ElectricWires;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctType;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.WrappedDirection;

public class WireAPI {

	/**
	 * Registers a custom machine block at the given location. Every wire around
	 * this block will try to interact with this block.<br>
	 * Create your own implementation of the ElectricWiresContainer interface in
	 * order to specify how to interact with this block.
	 * 
	 * @param ewc
	 *            your own implementation of the ElectricWiresContainer interface
	 */
	public static void registerTransportPipesContainer(Location blockLoc, ElectricWiresContainer ewc) {
		BlockLoc bl = BlockLoc.convertBlockLoc(blockLoc);

		Map<BlockLoc, ElectricWiresContainer> containerMap = ElectricWires.instance.getContainerMap(blockLoc.getWorld());
		if (containerMap == null) {
			containerMap = Collections.synchronizedMap(new TreeMap<BlockLoc, ElectricWiresContainer>());
			ElectricWires.instance.getFullContainerMap().put(blockLoc.getWorld(), containerMap);
		}
		if (containerMap.containsKey(bl)) {
			throw new IllegalArgumentException("There is already a ElectricWiresContainer object registered at this location");
		}
		containerMap.put(bl, ewc);

		Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(blockLoc.getWorld());
		if (ductMap != null) {
			for (WrappedDirection pd : WrappedDirection.values()) {
				bl = BlockLoc.convertBlockLoc(blockLoc.clone().add(pd.getX(), pd.getY(), pd.getZ()));
				if (ductMap.containsKey(bl)) {
					final Duct duct = ductMap.get(bl);
					if (duct.getDuctType() == DuctType.WIRE) {
						TransportPipes.instance.pipeThread.runTask(new Runnable() {

							@Override
							public void run() {
								TransportPipes.instance.ductManager.updateDuct(duct);
							}
						}, 0);
					}
				}
			}
		}

	}

	/**
	 * Unregisters a custom machine block. See
	 * {@link WireAPI#registerElectricWiresContainer(Location, ElectricWiresContainer)}
	 */
	public static void unregisterElectricWiresContainer(Location blockLoc) {
		BlockLoc bl = BlockLoc.convertBlockLoc(blockLoc);

		Map<BlockLoc, ElectricWiresContainer> containerMap = ElectricWires.instance.getContainerMap(blockLoc.getWorld());
		if (containerMap != null) {
			if (containerMap.containsKey(bl)) {
				containerMap.remove(bl);

				Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(blockLoc.getWorld());
				if (ductMap != null) {
					for (WrappedDirection pd : WrappedDirection.values()) {
						bl = BlockLoc.convertBlockLoc(blockLoc.clone().add(pd.getX(), pd.getY(), pd.getZ()));
						if (ductMap.containsKey(bl)) {
							final Duct duct = ductMap.get(bl);
							if (duct.getDuctType() == DuctType.WIRE) {
								TransportPipes.instance.pipeThread.runTask(new Runnable() {

									@Override
									public void run() {
										TransportPipes.instance.ductManager.updateDuct(duct);
									}
								}, 0);
							}
						}
					}
				}
			}
		}

	}

}
