package tk.estecka.invarpaint.crafting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class InvarpaintCraftingAddon
implements ModInitializer
{
	static public final String MODID  = "invarpaint-crafting";
	static public final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public void onInitialize(){
		PaintingReplicationRecipe.Register();
		FilledPaintingRecipe.Register();

		RegisterPack("cloning"  , "Painting Cloning"          , false);
		RegisterPack("unbound"  , "All Painting Recipes"      , false);
		RegisterPack("expensive", "Painting Recipe: Expensive", false);
		RegisterPack("iterative", "Painting Recipe: Iterative", false);
		RegisterPack("recycling", "Painting Recipe: Recycling", false);
	}

	public static void RegisterPack(String id, String displayName, boolean defaultEnabled) {
		ModContainer mod = (ModContainer)FabricLoader.getInstance().getModContainer(MODID).get();
		ResourceManagerHelper.registerBuiltinResourcePack(
			new Identifier(MODID, id),
			mod,
			Text.literal(displayName),
			defaultEnabled ? ResourcePackActivationType.DEFAULT_ENABLED : ResourcePackActivationType.NORMAL
		);
	 }
}
