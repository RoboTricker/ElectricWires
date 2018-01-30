package de.robotricker.electricwires.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.logisticscraft.logisticsapi.energy.EnergyStorage;

import de.robotricker.electricwires.ElectricWires;
import de.robotricker.electricwires.WireNetwork;
import de.robotricker.electricwires.duct.wire.Wire;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.api.DuctRegistrationEvent;
import de.robotricker.transportpipes.api.DuctUnregistrationEvent;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctType;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.WrappedDirection;

public class RegistrationListener implements Listener {

	private ElectricWires electricWires;
	
	public RegistrationListener(ElectricWires electricWires) {
		this.electricWires = electricWires;
	}
	
	@EventHandler
	public void onDuctRegistration(DuctRegistrationEvent e) {
		if (e.getDuct().getDuctType() == DuctType.WIRE) {
			Wire wire = (Wire) e.getDuct();
			Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(e.getDuct().getBlockLoc().getWorld());

			Set<WireNetwork> neighborNetworks = new HashSet<>();
			Collection<WrappedDirection> wireConnections = wire.getOnlyConnectableDuctConnections();
			for (WrappedDirection wd : wireConnections) {
				Location newLoc = wire.getBlockLoc().clone().add(wd.getX(), wd.getY(), wd.getZ());
				Wire neighborWire = (Wire) ductMap.get(BlockLoc.convertBlockLoc(newLoc));
				neighborNetworks.add(neighborWire.getWireNetwork());
			}
			if (neighborNetworks.size() == 0) {
				// wire is completely lonely
				WireNetwork network = new WireNetwork(e.getDuct().getBlockLoc().getWorld());
				electricWires.wireNetworks.add(network);
				network.addWire(wire);
				wire.setWireNetwork(network);
			} else if (neighborNetworks.size() == 1) {
				// wire connects to an existing network
				WireNetwork network = neighborNetworks.iterator().next();
				network.addWire(wire);
				wire.setWireNetwork(network);
			} else {
				// wire connects 2 or more networks and combines these to one big network
				WireNetwork bigNetwork = null;
				List<WireNetwork> otherNetworks = new ArrayList<>();
				Iterator<WireNetwork> it = neighborNetworks.iterator();
				while (it.hasNext()) {
					WireNetwork network = it.next();
					if (bigNetwork == null) {
						bigNetwork = network;
					} else {
						otherNetworks.add(network);
						electricWires.wireNetworks.remove(network);
					}
				}
				for (WireNetwork otherNetwork : otherNetworks) {
					bigNetwork.getWires().addAll(otherNetwork.getWires());
					for (Wire otherNetworkWire : otherNetwork.getWires()) {
						otherNetworkWire.setWireNetwork(bigNetwork);
					}
				}
				bigNetwork.addWire(wire);
				wire.setWireNetwork(bigNetwork);
			}
		}
	}

	@EventHandler
	public void onDuctUnregistration(DuctUnregistrationEvent e) {
		if (e.getDuct().getDuctType() == DuctType.WIRE) {
			Wire wire = (Wire) e.getDuct();
			Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(e.getDuct().getBlockLoc().getWorld());

			Collection<WrappedDirection> wireConnections = wire.getOnlyConnectableDuctConnections();
			if (wireConnections.size() == 0) {
				// wire is completely lonely
				electricWires.wireNetworks.remove(wire.getWireNetwork());
				wire.setWireNetwork(null);
			} else if (wireConnections.size() == 1) {
				// wire just connects to one other wire and therefore this wire can be removed
				// easily
				wire.getWireNetwork().removeWire(wire);
				wire.setWireNetwork(null);
			} else {
				// wire connects at least 2 other wires and therefore after removal of this
				// wire, there is a chance that one network system splits into 2 or more
				WireNetwork oldNetwork = wire.getWireNetwork();
				oldNetwork.removeWire(wire);
				wire.setWireNetwork(null);
				for (Wire networkWire : oldNetwork.getWires()) {
					networkWire.setWireNetwork(null);
				}
				oldNetwork.getWires().clear();
				electricWires.wireNetworks.remove(oldNetwork);
				for (WrappedDirection wd : wireConnections) {
					Location newLoc = wire.getBlockLoc().clone().add(wd.getX(), wd.getY(), wd.getZ());
					Wire neighborWire = (Wire) ductMap.get(BlockLoc.convertBlockLoc(newLoc));
					if (neighborWire.getWireNetwork() == null) {
						WireNetwork newNetwork = new WireNetwork(neighborWire.getBlockLoc().getWorld());
						electricWires.wireNetworks.add(newNetwork);
						newNetwork.addWireSilent(neighborWire);
						neighborWire.setWireNetwork(newNetwork);
						electricWires.updateConnectedWiresNetworkRekursive(neighborWire, ductMap);

						newNetwork.updateEnergyStorages();
					}
				}
			}
		}
	}

	@EventHandler
	public void onRegister(final com.logisticscraft.logisticsapi.event.LogisticBlockLoadEvent e) {
		if (e.getLogisticBlock() instanceof EnergyStorage) {
			for (final WireNetwork network : electricWires.wireNetworks) {
				if (network.getWorld().equals(e.getLocation().getWorld())) {
					ElectricWires.runTask(new Runnable() {
						@Override
						public void run() {
							network.updateEnergyStorages();
						}
					});
				}
			}
			Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(e.getLocation().getWorld());
			if (ductMap != null) {
				for (WrappedDirection pd : WrappedDirection.values()) {
					BlockLoc bl = BlockLoc.convertBlockLoc(e.getLocation().clone().add(pd.getX(), pd.getY(), pd.getZ()));
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

	@EventHandler
	public void onUnregister(com.logisticscraft.logisticsapi.event.LogisticBlockUnloadEvent e) {
		if (e.getLogisticBlock() instanceof EnergyStorage) {
			for (final WireNetwork network : electricWires.wireNetworks) {
				if (network.getWorld().equals(e.getLocation().getWorld())) {
					ElectricWires.runTask(new Runnable() {
						@Override
						public void run() {
							network.updateEnergyStorages();
						}
					});
				}
			}
			Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(e.getLocation().getWorld());
			if (ductMap != null) {
				for (WrappedDirection pd : WrappedDirection.values()) {
					BlockLoc bl = BlockLoc.convertBlockLoc(e.getLocation().clone().add(pd.getX(), pd.getY(), pd.getZ()));
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
