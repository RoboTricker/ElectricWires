package de.robotricker.electricwires.utils.staticutils;

import org.bukkit.event.Listener;
import de.robotricker.electricwires.ElectricWires;
import de.robotricker.electricwires.duct.wire.utils.WireColor;
import de.robotricker.electricwires.duct.wire.utils.WireType;

public class CraftUtils implements Listener {

	public static void initRecipes() {
		if (!ElectricWires.instance.generalConf.isCraftingEnabled()) {
			return;
		}
		for (WireType wt : WireType.values()) {
			ElectricWires.instance.recipesConf.createWireRecipe(wt, null);
			if (wt == WireType.COLORED) {
				for (WireColor wc : WireColor.values()) {
					ElectricWires.instance.recipesConf.createWireRecipe(wt, wc);
				}
			}
		}
	}

}
