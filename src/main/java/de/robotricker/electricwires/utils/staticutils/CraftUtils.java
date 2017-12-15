package de.robotricker.electricwires.utils.staticutils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import de.robotricker.electricwires.ElectricWires;
import de.robotricker.electricwires.duct.wire.utils.WireColor;
import de.robotricker.electricwires.duct.wire.utils.WireType;
import de.robotricker.transportpipes.duct.DuctType;
import de.robotricker.transportpipes.utils.ductdetails.DuctDetails;
import de.robotricker.transportpipes.utils.staticutils.DuctItemUtils;

public class CraftUtils implements Listener {

	public static void initRecipes() {
		if (!ElectricWires.instance.generalConf.isCraftingEnabled()) {
			return;
		}
		for (WireType wt : WireType.values()) {
			Bukkit.addRecipe(ElectricWires.instance.recipesConf.createWireRecipe(wt, null));
			if (wt == WireType.COLORED) {
				for (WireColor wc : WireColor.values()) {
					Bukkit.addRecipe(ElectricWires.instance.recipesConf.createWireRecipe(wt, wc));
				}
			}
		}
	}

	@EventHandler
	public void onPrepareCraft(PrepareItemCraftEvent e) {
		Recipe r = e.getInventory().getRecipe();
		if (r == null || e.getViewers().size() != 1) {
			return;
		}

		Player viewer = (Player) e.getViewers().get(0);

		if (r.getResult() != null) {
			DuctDetails ductDetails = DuctItemUtils.getDuctDetailsOfItem(r.getResult());
			if (ductDetails != null) {
				if (!viewer.hasPermission(ductDetails.getCraftPermission())) {
					e.getInventory().setResult(null);
					return;
				}
			} else if (DuctItemUtils.getWrenchItem().isSimilar(r.getResult())) {
				if (!viewer.hasPermission("transportpipes.craft.wrench")) {
					e.getInventory().setResult(null);
					return;
				}
			}

			if (ductDetails != null && ductDetails.getDuctType() == DuctType.WIRE) {
				boolean prevent = false;
				for (int i = 1; i < 10; i++) {
					ItemStack is = e.getInventory().getItem(i);
					if (is != null && is.getType() == Material.SKULL_ITEM && is.getDurability() == SkullType.PLAYER.ordinal()) {
						DuctDetails isDuctDetails = DuctItemUtils.getDuctDetailsOfItem(is);
						prevent |= isDuctDetails == null;
					}
				}
				if (prevent) {
					e.getInventory().setResult(null);
				}
			}
		}
	}

}
