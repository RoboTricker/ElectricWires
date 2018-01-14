package de.robotricker.electricwires;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.logisticscraft.logisticsapi.energy.EnergyStorage;

import de.robotricker.electricwires.config.GeneralConf;
import de.robotricker.electricwires.config.LocConf;
import de.robotricker.electricwires.config.RecipesConf;
import de.robotricker.electricwires.duct.wire.Wire;
import de.robotricker.electricwires.duct.wire.utils.WireColor;
import de.robotricker.electricwires.duct.wire.utils.WireType;
import de.robotricker.electricwires.rendersystem.modelled.utils.ModelledWireRenderSystem;
import de.robotricker.electricwires.tick.WireTickData;
import de.robotricker.electricwires.utils.ductdetails.WireDetails;
import de.robotricker.electricwires.utils.staticutils.CraftUtils;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.api.DuctRegistrationEvent;
import de.robotricker.transportpipes.api.DuctUnregistrationEvent;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctType;
import de.robotricker.transportpipes.utils.BlockLoc;
import de.robotricker.transportpipes.utils.WrappedDirection;
import de.robotricker.transportpipes.utils.staticutils.DuctItemUtils;
import de.robotricker.transportpipes.utils.staticutils.InventoryUtils;
import de.robotricker.transportpipes.utils.tick.TickRunnable;
import io.sentry.Sentry;
import io.sentry.event.UserBuilder;

public class ElectricWires extends JavaPlugin {

	public static ElectricWires instance;

	// configs
	public LocConf locConf;
	public GeneralConf generalConf;
	public RecipesConf recipesConf;

	public Set<WireNetwork> wireNetworks;

	@Override
	public void onEnable() {
		instance = this;
		initSentryOnCurrentThread();
		DuctType.WIRE.setDuctDetailsClass(WireDetails.class);
		DuctType.WIRE.setTickRunnable(new TickRunnable() {

			@Override
			public void run(long numberOfTicksSinceStart) {

				// update wires
				for (World world : Bukkit.getWorlds()) {
					Map<BlockLoc, Duct> ductMap = TransportPipes.instance.getDuctMap(world);
					if (ductMap != null) {
						synchronized (ductMap) {
							for (Duct duct : ductMap.values()) {
								if (duct.getDuctType() != DuctType.WIRE) {
									continue;
								}
								if (!duct.isInLoadedChunk()) {
									continue;
								}
								duct.tick(new WireTickData());
							}
						}
					}
				}
			}
		});

		Bukkit.getScheduler().runTaskTimer(this, new Runnable() {

			@Override
			public void run() {
				synchronized (wireNetworks) {
					for (WireNetwork network : wireNetworks) {
						network.update();
					}
				}
			}
		}, 10, 10);

		locConf = new LocConf();
		generalConf = new GeneralConf();
		recipesConf = new RecipesConf();
		wireNetworks = Collections.synchronizedSet(new HashSet<WireNetwork>());

		// register duct items
		ItemStack ITEM_WIRE_WHITE = InventoryUtils.createSkullItemStack("9f38586a-2ec7-33be-a472-13939b855430", "eyJ0aW1lc3RhbXAiOjE1MDAwMzc4NDM1MTgsInByb2ZpbGVJZCI6ImE5MGI4MmIwNzE4NTQ0ZjU5YmE1MTZkMGY2Nzk2NDkwIiwicHJvZmlsZU5hbWUiOiJJbUZhdFRCSCIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmViYjZmNmU1NjRjM2E2MDdkZjk3OGE2ZjZmY2VkZGJkZmViOTdiOWU1YmMzZGQ4MzZkMDJiZTdjOTFlNiJ9fX0=", "blgkeU5W6OAuF3A8BSJVXaR8X2OK/YjGITjx3UTDr+Ij0qsFbXnV7AuskN2lw/KnCgqOW7xWWDaeRpRIwkXwg6IkTl2w8ZFLxvoje/GlWxuG5X2hA6/hTdfEV9rU+4hwliSnU4zABVfA0hm9uxmVjYYH0GKshPPyQnbG1DI6vBaY+qUMwvZao26qhCQeDi/HLx3X+xIxLXjmlOtAFV+pce7WJWL1VjSpRejtpyreCqc/TVanCGTTqDknJOmTKiBrUBFk6NGfPPq2sr0fwR0Aj+jdysaCujeCuSvsXoKwMEHWwTDU+GYl+Ez7bj+fPOabW1wuYzLWk7E5HlBnL74zNFBzH2GVvQFDAhpgSyMxOh4d65S1gbWgi9D03FZ+tEWdRQgGTNNnX5IVK6OCZLhwQW4YF4GbiFhst6M2YfVrJLu6j3WVWvHmBhD5OE3ytJTqTmNXWFJ46U9WOjtZFYqBqWdXBdF6Xc/Z+sRgGgUDCyN4QGchVkFp1DUt6Fq07eMvsQ6rxWeGzGq0dw7m9u56mcVyMR+JlGHNQzR76C8FEMMF/+pZG0qy1XKlNWsCLR9ePe7kURdYISbUYljSkWVhfJ5iFWfhpaquvmXW7erN6FXIc7XuhW6ZxvczQ546l5Q5Ncqzl8qnU61bdd87uxUrQHoD8G5i3iE1NmLw8FWmAaM=");
		InventoryUtils.changeDisplayName(ITEM_WIRE_WHITE, WireColor.WHITE.getColorCode() + WireType.COLORED.getFormattedWireName());

		DuctItemUtils.registerDuctItem(new WireDetails(WireColor.WHITE), ITEM_WIRE_WHITE);

		DuctType.WIRE.addRenderSystem(new ModelledWireRenderSystem(TransportPipes.instance.ductManager));

		Bukkit.getPluginManager().registerEvents(new CraftUtils(), this);

		CraftUtils.initRecipes();

		Bukkit.getPluginManager().registerEvents(new Listener() {

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
						wireNetworks.add(network);
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
								wireNetworks.remove(network);
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
					System.out.println("networks: " + wireNetworks.size());
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
						wireNetworks.remove(wire.getWireNetwork());
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
						wireNetworks.remove(oldNetwork);
						for (WrappedDirection wd : wireConnections) {
							Location newLoc = wire.getBlockLoc().clone().add(wd.getX(), wd.getY(), wd.getZ());
							Wire neighborWire = (Wire) ductMap.get(BlockLoc.convertBlockLoc(newLoc));
							if (neighborWire.getWireNetwork() == null) {
								WireNetwork newNetwork = new WireNetwork(neighborWire.getBlockLoc().getWorld());
								wireNetworks.add(newNetwork);
								newNetwork.addWireSilent(neighborWire);
								neighborWire.setWireNetwork(newNetwork);
								updateConnectedWiresNetworkRekursive(neighborWire, ductMap);

								newNetwork.updateEnergyStorages();
								newNetwork.calcMaxCharge();
							}
						}
					}
					System.out.println("networks: " + wireNetworks.size());
				}
			}

			@EventHandler
			public void onRegister(final com.logisticscraft.logisticsapi.event.LogisticBlockLoadEvent e) {
				if (e.getLogisticBlock() instanceof EnergyStorage) {
					for (final WireNetwork network : wireNetworks) {
						if (network.getWorld().equals(e.getLocation().getWorld())) {
							Bukkit.getScheduler().runTask(ElectricWires.instance, new Runnable() {
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
					for (final WireNetwork network : wireNetworks) {
						if (network.getWorld().equals(e.getLocation().getWorld())) {
							Bukkit.getScheduler().runTask(ElectricWires.instance, new Runnable() {
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

		}, this);

	}

	private void updateConnectedWiresNetworkRekursive(Wire wire, Map<BlockLoc, Duct> ductMap) {
		Collection<WrappedDirection> wireConnections = wire.getOnlyConnectableDuctConnections();
		for (WrappedDirection wd : wireConnections) {
			Location newLoc = wire.getBlockLoc().clone().add(wd.getX(), wd.getY(), wd.getZ());
			Wire neighborWire = (Wire) ductMap.get(BlockLoc.convertBlockLoc(newLoc));
			if (neighborWire.getWireNetwork() == null) {
				WireNetwork network = wire.getWireNetwork();

				neighborWire.setWireNetwork(network);
				network.addWireSilent(neighborWire);

				updateConnectedWiresNetworkRekursive(neighborWire, ductMap);
			}
		}
	}

	public static void initSentryOnCurrentThread() {
		Sentry.init("https://6e99a13e8f654066b8cd00927079db36:3d1a8cf711444b80bda2a6dae0b2ac9e@sentry.io/256964");
		Sentry.getContext().setUser(new UserBuilder().setUsername("RoboTricker").build());
		Sentry.getContext().addTag("thread", Thread.currentThread().getName());
		Sentry.getContext().addTag("version", ElectricWires.instance.getDescription().getVersion());

		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				Sentry.capture(e);
			}
		});
	}

}
