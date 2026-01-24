package ml.mypals.lucidity.mixin.features.tisMicroTimingHighlight;

import ml.mypals.lucidity.features.tisMicroTiminghighlight.MicroTimingAnalyzer;
import ml.mypals.lucidity.features.tisMicroTiminghighlight.MicrotimingKeyWorldTracker;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    protected ChatScreenMixin(Component component) {
        super(component);
    }
    @Inject(method = "init",at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        MicrotimingKeyWorldTracker.start();
    }
    @Override
    public void onClose() {
        MicrotimingKeyWorldTracker.stop();
        MicroTimingAnalyzer.reset();
        super.onClose();
    }
}
