package tk.estecka.invarpaint.crafting.mixin;

import java.util.Collection;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.RecipeEntry;
import tk.estecka.invarpaint.crafting.IUnsyncRecipe;

@Mixin(SynchronizeRecipesS2CPacket.class)
public abstract class SynchronizeRecipesS2CPacketMixin{
	@Shadow @Mutable private List<RecipeEntry<?>> recipes;

	/**
	 * Prevents custom recipes from being synched with the client, allowing the 
	 * mod to remain optional there.
	 */
	@Inject(method="<init>(Ljava/util/Collection;)V", at=@At("TAIL"))
	private void SkipServerOnly(Collection<RecipeEntry<?>> recipes, CallbackInfo info){
		this.recipes = this.recipes.stream().filter(r -> !(r.value() instanceof IUnsyncRecipe unsync && unsync.DontSync())).toList();
	}
}
