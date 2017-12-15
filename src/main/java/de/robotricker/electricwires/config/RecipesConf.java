package de.robotricker.electricwires.config;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.MaterialData;

import de.robotricker.electricwires.ElectricWires;
import de.robotricker.electricwires.duct.wire.utils.WireColor;
import de.robotricker.electricwires.duct.wire.utils.WireType;
import de.robotricker.electricwires.utils.ductdetails.WireDetails;
import de.robotricker.transportpipes.utils.config.Conf;
import de.robotricker.transportpipes.utils.staticutils.DuctItemUtils;

public class RecipesConf extends Conf {

	public RecipesConf() {
		super(new File(ElectricWires.instance.getDataFolder().getAbsolutePath() + File.separator + "recipes.yml"), ElectricWires.instance);
		saveShapedRecipeAsDefault("colored", 4, Arrays.asList("ggx", "gbg", "xgg"), "g", "20:0", "b", "280:0");
		saveShapelessRecipeAsDefault("colored.white", 1, "wire", "351:15");
		finishDefault();
	}

	private void saveShapedRecipeAsDefault(String name, int amount, List<String> shape, String... ingredients) {
		String fullKey = "recipe." + name;
		if (!getYamlConf().contains(fullKey + ".type")) {
			saveAsDefault(fullKey + ".type", "shaped");
			saveAsDefault(fullKey + ".amount", amount);
			saveAsDefault(fullKey + ".shape", shape);
			for (int i = 0; i < ingredients.length; i++) {
				char ingredientChar = ingredients[i].charAt(0);
				String ingredientValue = ingredients[++i];
				saveAsDefault(fullKey + ".ingredients." + ingredientChar, ingredientValue);
			}
		}
	}

	private void saveShapelessRecipeAsDefault(String name, int amount, String... ingredients) {
		String fullKey = "recipe." + name;
		if (!getYamlConf().contains(fullKey + ".type")) {
			saveAsDefault(fullKey + ".type", "shapeless");
			saveAsDefault(fullKey + ".amount", amount);
			saveAsDefault(fullKey + ".ingredients", Arrays.asList(ingredients));
		}
	}

	/**
	 * prevent unused key from removing only inside RecipesConf
	 */
	@Override
	protected void finishDefault() {
		saveToFile();
	}

	public Recipe createWireRecipe(WireType wt, WireColor wc) {
		String basePath = "recipe." + wt.name().toLowerCase();
		if (wc != null) {
			basePath += "." + wc.name().toLowerCase();
		}
		ItemStack resultItem;
		if (wt == WireType.COLORED) {
			resultItem = DuctItemUtils.getClonedDuctItem(new WireDetails(wc == null ? WireColor.WHITE : wc));
		} else {
			resultItem = DuctItemUtils.getClonedDuctItem(new WireDetails(wt));
		}
		resultItem.setAmount((int) read(basePath + ".amount"));
		if (((String) read(basePath + ".type")).equalsIgnoreCase("shaped")) {
			ShapedRecipe recipe = new ShapedRecipe(resultItem);
			recipe.shape(((List<String>) read(basePath + ".shape")).toArray(new String[0]));
			Collection<String> subKeys = readSubKeys(basePath + ".ingredients");
			for (String key : subKeys) {
				String itemString = (String) read(basePath + ".ingredients." + key);
				int typeId = -1;
				byte typeData = -1;
				if (itemString.equalsIgnoreCase("wire")) {
					typeId = Material.SKULL_ITEM.getId();
					typeData = (byte) SkullType.PLAYER.ordinal();
				} else if (itemString.contains(":")) {
					typeId = Integer.parseInt(itemString.split(":")[0]);
					typeData = Byte.parseByte(itemString.split(":")[1]);
				} else {
					typeId = Integer.parseInt(itemString);
					typeData = 0;
				}
				if (typeId != -1 && typeData != -1) {
					recipe.setIngredient(key.charAt(0), new MaterialData(typeId, typeData));
				}
			}
			return recipe;
		} else if (((String) read(basePath + ".type")).equalsIgnoreCase("shapeless")) {
			ShapelessRecipe recipe = new ShapelessRecipe(resultItem);
			List<String> ingredients = (List<String>) read(basePath + ".ingredients");
			for (String itemString : ingredients) {
				int typeId = -1;
				byte typeData = -1;
				if (itemString.equalsIgnoreCase("wire")) {
					typeId = Material.SKULL_ITEM.getId();
					typeData = (byte) SkullType.PLAYER.ordinal();
				} else if (itemString.contains(":")) {
					typeId = Integer.parseInt(itemString.split(":")[0]);
					typeData = Byte.parseByte(itemString.split(":")[1]);
				} else {
					typeId = Integer.parseInt(itemString);
					typeData = 0;
				}
				if (typeId != -1 && typeData != -1) {
					recipe.addIngredient(new MaterialData(typeId, typeData));
				}
			}
			return recipe;
		}
		return null;
	}

}