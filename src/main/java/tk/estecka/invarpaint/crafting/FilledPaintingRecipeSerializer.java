package tk.estecka.invarpaint.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.util.dynamic.Range;

public class FilledPaintingRecipeSerializer
implements RecipeSerializer<FilledPaintingRecipe>
{
	public record Ingredients(Range<Integer> dyes, boolean blank, boolean filled) {
		static public Ingredients ofRecipe(FilledPaintingRecipe r){
			return new Ingredients(new Range<Integer>(r.dyesMin, r.dyesMax), r.canCreate, r.canDerive);
		}
	}

	static public final Codec<Ingredients> INGREDIENT_CODEC = RecordCodecBuilder.create(builder -> 
		builder.group(Range.CODEC.fieldOf("dyeCount").forGetter(Ingredients::dyes))
		       .and(Codec.BOOL.fieldOf("acceptsFilled").orElse(false).forGetter(Ingredients::filled))
		       .and(Codec.BOOL.fieldOf("acceptsBlank" ).orElse(true ).forGetter(Ingredients::blank ))
		       .apply(builder, Ingredients::new)
	);

	static private final MapCodec<FilledPaintingRecipe> CODEC = RecordCodecBuilder.mapCodec(builder ->
		builder.group(CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter(FilledPaintingRecipe::getCategory))
		       .and(Codec.BOOL.fieldOf("obfuscated").orElse(false).forGetter(FilledPaintingRecipe::IsObfuscated))
		       .and(INGREDIENT_CODEC.fieldOf("ingredients").forGetter(Ingredients::ofRecipe))
		       .apply(builder, FilledPaintingRecipe::new)
	);


	public FilledPaintingRecipe read(PacketByteBuf packet){
		InvarpaintCraftingAddon.LOGGER.warn("A serverside recipe was read from a packet. This should not have occurred.");
		Range<Integer> range = new Range<Integer>(
			packet.readInt(),
			packet.readInt()
		);

		return new FilledPaintingRecipe(
			packet.readEnumConstant(CraftingRecipeCategory.class),
			packet.readBoolean(),
			range,
			packet.readBoolean(),
			packet.readBoolean()
		);
	}

	public void	write(PacketByteBuf packet, FilledPaintingRecipe recipe){
		packet.writeInt(recipe.dyesMin);
		packet.writeInt(recipe.dyesMax);
		packet.writeEnumConstant(recipe.getCategory());
		packet.writeBoolean(recipe.isObfuscated);
		packet.writeBoolean(recipe.canCreate);
		packet.writeBoolean(recipe.canDerive);
	}

	@Override
	public MapCodec<FilledPaintingRecipe> codec(){
		return CODEC;
	}

	@Override
	public PacketCodec<RegistryByteBuf, FilledPaintingRecipe> packetCodec(){
		return PacketCodec.ofStatic(this::write, this::read);
	}
}
