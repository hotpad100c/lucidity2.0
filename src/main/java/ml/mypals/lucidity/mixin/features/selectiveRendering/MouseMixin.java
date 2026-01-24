package ml.mypals.lucidity.mixin.features.selectiveRendering;

import ml.mypals.lucidity.features.selectiveRendering.WandActionsManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager.wand;
import static ml.mypals.lucidity.hotkeys.HotkeyCallbacks.switchRenderMode;

@Mixin(MouseHandler.class)
public class MouseMixin {

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void injectOnMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {

        if (window == this.minecraft.getWindow().getWindow()) {
            if (this.minecraft.player != null) {
                ItemStack mainHand = this.minecraft.player.getMainHandItem();
                if (((mainHand.is(wand) || (this.minecraft.player.isSpectator())) && switchRenderMode.isDown())) {
                    double sensitivity = this.minecraft.options.mouseWheelSensitivity().get();
                    double scrollAmount = (this.minecraft.options.discreteMouseScroll().get() ?
                            Math.signum(vertical) : vertical) * sensitivity;

                    if (scrollAmount > 0) {
                        WandActionsManager.switchRenderMod(true);
                    } else if (scrollAmount < 0) {
                        WandActionsManager.switchRenderMod(false);
                    }
                    ci.cancel();
                }
            }
        }
    }

}
