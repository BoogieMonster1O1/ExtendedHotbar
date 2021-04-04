package io.github.boogiemonster1o1.extendedhotbar.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import io.github.boogiemonster1o1.extendedhotbar.ExtendedHotbar;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.util.Window;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import static io.github.boogiemonster1o1.extendedhotbar.ExtendedHotbar.DISTANCE;
import static io.github.boogiemonster1o1.extendedhotbar.ExtendedHotbar.moveUp;
import static io.github.boogiemonster1o1.extendedhotbar.ExtendedHotbar.reset;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_ZERO;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends DrawableHelper {
	@Shadow @Final private MinecraftClient client;

	@Shadow @Final private static Identifier WIDGETS;

	@Shadow protected abstract void method_2418(int i, int j, int k, float f, PlayerEntity playerEntity);

	@Inject(method = "method_2421", at = @At("RETURN"))
	private void drawTopHotbar(final Window window, final float tickDelta, final CallbackInfo info) {
		if (!ExtendedHotbar.getInstance().isEnabled()) {
			return;
		}

		final PlayerEntity playerEntity = (PlayerEntity) this.client.getCameraEntity();
		if (playerEntity == null) {
			return;
		}

		final int i = window.getScaledWidth() / 2;

		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		client.getTextureManager().bindTexture(WIDGETS);
		GlStateManager.enableBlend();
		drawTexture(i - 91, window.getScaledHeight() - 22 + DISTANCE, 0, 0, 182, 22);

		GlStateManager.enableRescaleNormal();
		GlStateManager.blendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
		GuiLighting.enable();

		for (int l = 0; l < 9; ++l) {
			// Anyone like magic numbers?
			final int i1 = i - 90 + l * 20 + 2;
			final int j1 = window.getScaledHeight() - 16 - 3 + DISTANCE;
			this.method_2418(l + 27, i1, j1, tickDelta, playerEntity);
		}

		GuiLighting.disable();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableBlend();
	}

	@Inject(
			id = "move",
			method = {
					"renderStatusBars",
					"renderExperienceBar",
					"renderHeldItemName"
			},
			at  = {
					@At(value = "HEAD", id = "head"),
					@At(value = "RETURN", id = "return")
			}
	)
	private void moveGui(final CallbackInfo info) {
		if ("move:head".equals(info.getId())) {
			moveUp();
		} else {
			reset();
		}
	}

	@ModifyArg(method = "render", index = 2, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Ljava/lang/String;III)I"))
	private int moveActionBarText(final int y) {
		if (ExtendedHotbar.getInstance().isEnabled()) {
			return y + DISTANCE;
		} else {
			return y;
		}
	}
}
