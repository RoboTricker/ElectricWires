package de.robotricker.electricwires;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import de.robotricker.electricwires.api.ElectricWiresContainer;
import de.robotricker.electricwires.config.GeneralConf;
import de.robotricker.electricwires.config.LocConf;
import de.robotricker.electricwires.config.RecipesConf;
import de.robotricker.electricwires.rendersystem.modelled.utils.ModelledWireRenderSystem;
import de.robotricker.electricwires.wires.types.Wire;
import de.robotricker.electricwires.wireutils.CraftUtils;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.api.TransportPipesContainer;
import de.robotricker.transportpipes.pipes.BlockLoc;
import de.robotricker.transportpipes.pipes.DuctType;
import de.robotricker.transportpipes.pipes.types.Pipe;
import de.robotricker.transportpipes.rendersystem.RenderSystem;
import de.robotricker.transportpipes.rendersystem.modelled.utils.ModelledPipeRenderSystem;
import de.robotricker.transportpipes.rendersystem.vanilla.utils.VanillaPipeRenderSystem;
import io.sentry.Sentry;
import io.sentry.event.UserBuilder;

public class ElectricWires extends JavaPlugin {

	public static ElectricWires instance;

	private Map<World, Map<BlockLoc, Wire>> registeredWires;
	private Map<World, Map<BlockLoc, ElectricWiresContainer>> registeredContainers;

	// configs
	public LocConf locConf;
	public GeneralConf generalConf;
	public RecipesConf recipesConf;

	@Override
	public void onEnable() {
		instance = this;
		initSentryOnCurrentThread();

		// Prepare collections
		registeredWires = Collections.synchronizedMap(new HashMap<World, Map<BlockLoc, Wire>>());
		registeredContainers = Collections.synchronizedMap(new HashMap<World, Map<BlockLoc, ElectricWiresContainer>>());

		locConf = new LocConf();
		generalConf = new GeneralConf();
		recipesConf = new RecipesConf();

		DuctType.WIRE.addRenderSystem(new ModelledWireRenderSystem(TransportPipes.instance.armorStandProtocol));

		Bukkit.getPluginManager().registerEvents(new CraftUtils(), this);
		for (RenderSystem rs : DuctType.WIRE.getRenderSystems())
			Bukkit.getPluginManager().registerEvents(rs, this);

		CraftUtils.initRecipes();

	}

	public Map<BlockLoc, Wire> getWireMap(World world) {
		if (registeredWires.containsKey(world)) {
			return registeredWires.get(world);
		}
		return null;
	}

	public Map<World, Map<BlockLoc, Wire>> getFullWireMap() {
		return registeredWires;
	}

	public Map<BlockLoc, ElectricWiresContainer> getContainerMap(World world) {
		if (registeredContainers.containsKey(world)) {
			return registeredContainers.get(world);
		}
		return null;
	}

	public Map<World, Map<BlockLoc, ElectricWiresContainer>> getFullContainerMap() {
		return registeredContainers;
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
