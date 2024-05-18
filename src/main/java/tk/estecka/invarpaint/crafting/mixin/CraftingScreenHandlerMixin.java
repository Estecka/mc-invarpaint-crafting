package tk.estecka.invarpaint.crafting.mixin;

import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.text.MutableText;
import net.minecraft.util.Rarity;
import tk.estecka.invarpaint.InvarpaintMod;
import tk.estecka.invarpaint.crafting.IObfuscatedRecipe;

@Mixin(CraftingScreenHandler.class)
public class CraftingScreenHandlerMixin
{
	@WrapOperation( method="updateResult", at=@At( value="INVOKE", target="net/minecraft/recipe/CraftingRecipe.craft (Lnet/minecraft/inventory/Inventory;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/item/ItemStack;") )
	static private ItemStack	ShouldObfuscate(CraftingRecipe recipe, Inventory input, RegistryWrapper.WrapperLookup wrapper, Operation<ItemStack> original, @Share("obf") LocalBooleanRef doObfuscate){
		if (recipe instanceof IObfuscatedRecipe obfRecipe && obfRecipe.IsObfuscated())
			doObfuscate.set(true);
		return original.call(recipe, input, wrapper);
	}


	@ModifyArg( method="updateResult", index=3, at=@At(value="INVOKE", target="net/minecraft/network/packet/s2c/play/ScreenHandlerSlotUpdateS2CPacket.<init> (IIILnet/minecraft/item/ItemStack;)V") )
	static private ItemStack ObfuscateResult(int syncId, int revision, int slot, ItemStack stack, @Share("obf") LocalBooleanRef doObfuscate){
		if (doObfuscate.get())
			stack = ObfuscateStack(stack);
		return stack;
	}

	@Unique
	static private ItemStack ObfuscateStack(ItemStack stack){
		stack = new ItemStack(stack.getItem());
		stack.set(DataComponentTypes.CUSTOM_DATA, stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).apply(nbt -> nbt.putBoolean("invarpaint:obfuscated", true)));

		MutableText name = stack.getName().copy()
			.append(" (")
			.append(InvarpaintMod.ServersideTranslatable("invarpaint.painting.obfuscated"))
			.append(")")
			;

		stack.set(DataComponentTypes.RARITY, Rarity.EPIC);
		stack.set(DataComponentTypes.ITEM_NAME, name);
		stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(InvarpaintMod.ServersideTranslatable("invarpaint.painting.obfuscated.lore"))));
		return stack;
	}

}
