package io.github.boogiemonster1o1.extendedhotbar;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.boogiemonster1o1.extendedhotbar.mixin.CreativeInventoryScreenAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.SurvivalInventoryScreen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.item.ItemGroup;

import net.fabricmc.api.ClientModInitializer;

import net.legacyfabric.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.legacyfabric.fabric.api.client.keybinding.v1.KeyBindingHelper;

public class ExtendedHotbar implements ClientModInitializer, ClientTickEvents.EndTick {
	public static final Logger LOGGER = LogManager.getLogger();
	public static final int DISTANCE = -22;
	private static ExtendedHotbar INSTANCE;
	private static final KeyBinding SWAP_KEY_BINDING = new KeyBinding("key.extendedHotbar.switch", Keyboard.KEY_R, "key.categories.extendedHotbar");
	private static final KeyBinding TOGGLE_KEY_BINDING = new KeyBinding("key.extendedHotbar.toggle", Keyboard.KEY_P, "key.categories.extendedHotbar");
	private static final int INVENTORY_TAB_INDEX = ItemGroup.INVENTORY.getIndex();
	private static final int LEFT_BOTTOM_ROW_SLOT_INDEX = 27;
	private static final int LEFT_HOTBAR_SLOT_INDEX = 36;
	private static final int BOTTOM_RIGHT_CRAFTING_SLOT_INDEX = 4;
	private boolean enabled = true;

	@Override
	public void onInitializeClient() {
		INSTANCE = this;
		LOGGER.info("Initializing ExtendedHotbar");
		KeyBindingHelper.registerKeyBinding(SWAP_KEY_BINDING);
		KeyBindingHelper.registerKeyBinding(TOGGLE_KEY_BINDING);
		ClientTickEvents.END_CLIENT_TICK.register(this);
	}

	@Override
	public void onEndTick(MinecraftClient client) {
		if (TOGGLE_KEY_BINDING.wasPressed()) {
			enabled = !enabled;
			return;
		}

		if (!enabled) {
			return;
		}

		if (client.world == null || client.currentScreen != null || !MinecraftClient.isHudEnabled()) {
			return;
		}

		if (SWAP_KEY_BINDING.wasPressed()) {
			performSwap(!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT));
		}
	}

	private void performSwap(final boolean fullRow) {
		final MinecraftClient minecraft = MinecraftClient.getInstance();

		final SurvivalInventoryScreen screen = new SurvivalInventoryScreen(minecraft.player);
		minecraft.openScreen(screen);

		final Screen currentScreen = minecraft.currentScreen;
		if (currentScreen == null) {
			return;
		}

		// For the switcheroo to work, we need to be in the inventory window
		final int index;
		if (currentScreen instanceof CreativeInventoryScreen) {
			index = ((CreativeInventoryScreen) currentScreen).getSelectedTab();

			if (index != INVENTORY_TAB_INDEX) {
				((CreativeInventoryScreenAccessor) currentScreen).callSetSelectedTab(ItemGroup.INVENTORY);
			}
		} else {
			index = -1;
		}

		final int syncId = screen.screenHandler.syncId;

		if (fullRow) {
			swapRows(minecraft, syncId);
		} else {
			final int selectedSlot = minecraft.player.inventory.selectedSlot;
			swapItem(minecraft, syncId, selectedSlot);
		}

		// If index == -1 then it's not a creative inventory, if it's INVENTORY_TAB_INDEX then there's no need to change it back to itself
		if (index != -1 && index != INVENTORY_TAB_INDEX) {
			((CreativeInventoryScreenAccessor) currentScreen).callSetSelectedTab(ItemGroup.itemGroups[index]);
		}

		minecraft.openScreen(null);
	}

	private void swapRows(MinecraftClient minecraft, int syncId) {
		for (int i = 0; i < 9; i++) {
			swapItem(minecraft, syncId, i);
		}
	}

	/**
	 * Swaps two items in the hotbar and the bottom row of the player's inventory, 0 being the far left column, 8 being the far right.
	 */
	private void swapItem(final MinecraftClient minecraft, final int syncId, final int slotId) {
		/*
		 * Implementation note:
		 * There are fancy click mechanisms to swap item stacks without using a temporary slot, but when swapping between two identical item
		 * stacks, things can get messed up. Using a temporary slot that we know is guaranteed to be empty is the safest option.
		 */

		// Move hotbar item to crafting slot
		minecraft.interactionManager.clickSlot(syncId, slotId + LEFT_HOTBAR_SLOT_INDEX, 0, 0, minecraft.player);
		minecraft.interactionManager.clickSlot(syncId, BOTTOM_RIGHT_CRAFTING_SLOT_INDEX, 0, 0, minecraft.player);
		// Move bottom row item to hotbar
		minecraft.interactionManager.clickSlot(syncId, slotId + LEFT_BOTTOM_ROW_SLOT_INDEX, 0, 0, minecraft.player);
		minecraft.interactionManager.clickSlot(syncId, slotId + LEFT_HOTBAR_SLOT_INDEX, 0, 0, minecraft.player);
		// Move crafting slot item to bottom row
		minecraft.interactionManager.clickSlot(syncId, BOTTOM_RIGHT_CRAFTING_SLOT_INDEX, 0, 0, minecraft.player);
		minecraft.interactionManager.clickSlot(syncId, slotId + LEFT_BOTTOM_ROW_SLOT_INDEX, 0, 0, minecraft.player);
	}

	public static ExtendedHotbar getInstance() {
		return INSTANCE;
	}

	public static void moveUp() {
		if (getInstance().isEnabled()) {
			GlStateManager.pushMatrix();
			GlStateManager.translated(0, DISTANCE, 0);
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public static void reset() {
		if (getInstance().isEnabled()) {
			GlStateManager.popMatrix();
		}
	}
}
