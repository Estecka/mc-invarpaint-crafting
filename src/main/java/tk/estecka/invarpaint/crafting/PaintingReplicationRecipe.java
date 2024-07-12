package tk.estecka.invarpaint.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import tk.estecka.invarpaint.core.PaintStackUtil;

public class PaintingReplicationRecipe
extends SpecialCraftingRecipe
implements IUnsyncRecipe
{
	static public final Identifier ID = Identifier.of("invarpaint", "crafting_special_painting_replication");
	static public final SpecialRecipeSerializer<PaintingReplicationRecipe> SERIALIZER = new SpecialRecipeSerializer<PaintingReplicationRecipe>(PaintingReplicationRecipe::new);

	static public void Register(){
		Registry.register(Registries.RECIPE_SERIALIZER, ID, SERIALIZER);
	}

	public PaintingReplicationRecipe(CraftingRecipeCategory category){
		super(category);
	}

	@Override
	public boolean matches(CraftingRecipeInput ingredients, World world){
		boolean hasCanvas = false;
		boolean hasTemplate = false;

		for (int i=0; i<ingredients.getSize(); ++i){
			ItemStack stack = ingredients.getStackInSlot(i);
			if (stack.isEmpty())
				continue;
			else if (!stack.isOf(Items.PAINTING))
				return false;

			boolean isBlank = !PaintStackUtil.HasVariantId(stack);
			if (!hasCanvas && isBlank)
				hasCanvas = true;
			else if (!hasTemplate && !isBlank)
				hasTemplate = true;
			else
				return false;
		}

		return hasCanvas && hasTemplate;
	}

	@Override
	public ItemStack craft(CraftingRecipeInput ingredients, RegistryWrapper.WrapperLookup manager){
		for (int i=0; i<ingredients.getSize(); ++i)
			if (PaintStackUtil.HasVariantId(ingredients.getStackInSlot(i)))
				return ingredients.getStackInSlot(i).copyWithCount(1);

		return ItemStack.EMPTY;
	}

	@Override
	public DefaultedList<ItemStack> getRemainder(CraftingRecipeInput ingredients) {
		DefaultedList<ItemStack> remainder = DefaultedList.ofSize(ingredients.getSize(), ItemStack.EMPTY);
		for (int i=0; i<ingredients.getSize(); ++i)
			if (PaintStackUtil.HasVariantId(ingredients.getStackInSlot(i)))
				remainder.set(i, ingredients.getStackInSlot(i).copyWithCount(1));

		return remainder;
	}

	@Override
	public boolean fits(int width, int height){
		return (width*height) >= 2;
	}

	@Override
	public SpecialRecipeSerializer<PaintingReplicationRecipe> getSerializer(){
		return SERIALIZER;
	}
}
