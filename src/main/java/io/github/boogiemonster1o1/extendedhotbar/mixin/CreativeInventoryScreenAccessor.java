package io.github.boogiemonster1o1.extendedhotbar.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroup;

@Mixin(CreativeInventoryScreen.class)
public interface CreativeInventoryScreenAccessor {
	@Invoker
	void callSetSelectedTab(ItemGroup group);
}
