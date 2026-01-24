package ml.mypals.lucidity.mixin.features.tisMicroTimingHighlight;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import ml.mypals.lucidity.features.tisMicroTiminghighlight.MicrotimingKeyWorldTracker;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.*;
import org.spongepowered.asm.mixin.Mixin;
import static ml.mypals.lucidity.config.FeatureToggle.MICROTIMING_MARKER_VISUALIZER;


@Mixin(ChatListener.class)
public class ClientPacketListenerMixin {

    @WrapMethod(method = "handleSystemMessage")
    public void handleSystemMessage(Component component, boolean bl, Operation<Void> original) {
        if(MICROTIMING_MARKER_VISUALIZER.getBooleanValue() && !bl){
            original.call(MicrotimingKeyWorldTracker.track(component),false);

        }else{
            original.call(component,bl);

        }
    }
}
