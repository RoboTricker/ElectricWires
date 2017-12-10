package de.robotricker.electricwires.config;

import java.io.File;

import de.robotricker.electricwires.ElectricWires;
import de.robotricker.transportpipes.pipeutils.config.Conf;

public class GeneralConf extends Conf {

	public GeneralConf() {
		super(new File(ElectricWires.instance.getDataFolder().getAbsolutePath() + File.separator + "config.yml"), ElectricWires.instance);
		saveAsDefault("crafting_enabled", true);
		saveAsDefault("destroy_pipe_on_explosion", true);
		finishDefault();
	}

	public boolean isCraftingEnabled() {
		return (boolean) read("crafting_enabled");
	}

	public boolean isDestroyPipeOnExplosion() {
		return (boolean) read("destroy_pipe_on_explosion");
	}

}