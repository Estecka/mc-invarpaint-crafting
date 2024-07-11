package tk.estecka.invarpaint.crafting;

import java.util.HashSet;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Range;
import net.minecraft.world.World;
import tk.estecka.invarpaint.core.PaintStackUtil;
import tk.estecka.invarpaint.crafting.FilledPaintingRecipeSerializer.Ingredients;

public class FilledPaintingRecipe
extends SpecialCraftingRecipe
implements IUnsyncRecipe, IObfuscatedRecipe
{
	static public final Identifier ID = Identifier.of("invarpaint", "crafting_special_painting_creation");
	static public final RecipeSerializer<FilledPaintingRecipe> SERIALIZER = new FilledPaintingRecipeSerializer();

	public final int dyesMin, dyesMax;
	public final boolean canCreate, canDerive;
	public final boolean isObfuscated;

	// Hacky way to gain direct access to the painting registry. Might break easily.
	// Registry wrappers are way too dumbed down to be of any use.
	private Registry<PaintingVariant> paintingRegistry;

	static public void Register(){
		Registry.register(Registries.RECIPE_SERIALIZER, ID, SERIALIZER);
	}

	public FilledPaintingRecipe(CraftingRecipeCategory category, boolean isObfuscated, Ingredients ingredients){
		this(category, isObfuscated, ingredients.dyes(), ingredients.blank(), ingredients.filled());
	}

	public FilledPaintingRecipe(CraftingRecipeCategory category, boolean isObfuscated, Range<Integer> dyeCount, boolean canCreate, boolean canDerive){
		super(category);

		this.dyesMin = Math.min(8, Math.max(1, dyeCount.minInclusive().intValue()));
		this.dyesMax = Math.min(8, Math.max(1, dyeCount.maxInclusive().intValue()));
		this.canCreate = canCreate;
		this.canDerive = canDerive;
		this.isObfuscated = isObfuscated;
	}

	@Override
	public boolean IsObfuscated(){
		return this.isObfuscated;
	}

	private boolean ValidatePainting(ItemStack painting, World world){
		boolean hasVariant = PaintStackUtil.HasVariantId(painting);

		return (canCreate && !hasVariant) || (canDerive && hasVariant);
	}

	@Override
	public boolean matches(CraftingRecipeInput ingredients, World world){
		boolean hasPainting = false;
		var dyeSet = new HashSet<DyeItem>(8);
		this.paintingRegistry = world.getRegistryManager().get(RegistryKeys.PAINTING_VARIANT);

		for (int i=0; i<ingredients.getSize(); ++i){
			ItemStack stack = ingredients.getStackInSlot(i);
			if (stack.getItem() instanceof DyeItem dye){
				if(dyeSet.contains(dye))
					return false;
				dyeSet.add(dye);
			}
			else if (!hasPainting && stack.isOf(Items.PAINTING)){
				if(!ValidatePainting(stack, world))
					return false;
				hasPainting = true;
			}
			else if (!stack.isEmpty())
				return false;
		}

		return hasPainting && (dyesMin <= dyeSet.size()) && (dyeSet.size() <= dyesMax);
	}

	@Override
	public ItemStack craft(CraftingRecipeInput ingredients, RegistryWrapper.WrapperLookup wrapper){
		String canvasVariant = null;
		short dyeMask = 0;
		for (int i=0; i<ingredients.getSize(); ++i){
			ItemStack item = ingredients.getStackInSlot(i);
			if (item.isOf(Items.PAINTING))
				canvasVariant = PaintStackUtil.GetVariantName(item);
			if (item.getItem() instanceof DyeItem dye)
				dyeMask |= 1 << dye.getColor().getId();
		}

		int dyeCount = DyeCodeUtil.MaskSize(dyeMask);
		var entry = CraftVariant(canvasVariant, dyeMask, dyeCount);
		if (entry.isPresent()){
			return PaintStackUtil.CreateVariant(entry.get().getKey().get().getValue().toString());
		}
		else {
			long dyeCode = DyeCodeUtil.MaskToCode(dyeMask);
			InvarpaintCraftingAddon.LOGGER.error("Unable to find a valid painting: [{}] 0x{}", dyeCount, String.format("%0"+dyeCount+"X", dyeCode));
			return ItemStack.EMPTY;
		}
	}

	public Optional<?extends RegistryEntry<PaintingVariant>>	CraftVariant(@Nullable String inputVariant, short dyeMask, int dyeCount){
		if (paintingRegistry.size() <= DyeCodeUtil.COMBINATION_MAX[dyeCount])
			return DyeCodeUtil.DyemaskToVariant(paintingRegistry, dyeMask);
		else {
			int rank = DyeCodeUtil.MaskToRank(dyeMask);
			return Partition.FromIngredients(paintingRegistry, inputVariant, DyeCodeUtil.COMBINATION_MAX[dyeCount], rank).GetVariant(rank);
		}
	}

	@Override
	public boolean fits(int width, int height){
		return (width*height) > dyesMin;
	}

	@Override
	public RecipeSerializer<FilledPaintingRecipe> getSerializer(){
		return SERIALIZER;
	}
}
