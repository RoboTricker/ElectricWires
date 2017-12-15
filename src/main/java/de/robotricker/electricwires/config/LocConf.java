package de.robotricker.electricwires.config;

import java.io.File;
import java.util.List;

import org.bukkit.ChatColor;

import de.robotricker.electricwires.ElectricWires;
import de.robotricker.transportpipes.utils.config.Conf;

public class LocConf extends Conf {

	public static final String WIRES_COLORED = "wires.colored";

	public LocConf() {
		super(new File(ElectricWires.instance.getDataFolder().getAbsolutePath() + File.separator + "localization.yml"), ElectricWires.instance);
		saveAsDefault(WIRES_COLORED, "Electric Wire");
		finishDefault();
	}

	public String get(String key) {
		return ChatColor.translateAlternateColorCodes('&', (String) read(key));
	}

	@SuppressWarnings("unchecked")
	public List<String> getStringList(String key) {
		List<String> list = (List<String>) read(key);
		for (int i = 0; i < list.size(); i++) {
			list.set(i, ChatColor.translateAlternateColorCodes('&', list.get(i)));
		}
		return list;
	}

	public static String load(String key) {
		return ElectricWires.instance.locConf.get(key);
	}

	public static List<String> loadStringList(String key) {
		return ElectricWires.instance.locConf.getStringList(key);
	}

}