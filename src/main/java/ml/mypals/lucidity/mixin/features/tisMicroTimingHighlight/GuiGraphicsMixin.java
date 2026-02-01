package ml.mypals.lucidity.mixin.features.tisMicroTimingHighlight;

import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static ml.mypals.lucidity.config.FeatureToggle.MICROTIMING_MARKER_VISUALIZER;
import static ml.mypals.lucidity.features.tisMicroTiminghighlight.MicroTimingAnalyzer.decodeParentHidden;
import static ml.mypals.lucidity.features.tisMicroTiminghighlight.MicrotimingKeyWorldTracker.*;


@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {

    @Inject(method = "renderComponentHoverEffect",at =@At("HEAD"))
    public void renderComponentHoverEffect(Font font, Style style, int i, int j, CallbackInfo ci) {
        if (MICROTIMING_MARKER_VISUALIZER.getBooleanValue() && style != null && style.getHoverEvent() != null) {
            HoverEvent hoverEvent = style.getHoverEvent();
            //? if >=1.21.5 {
            /*if (hoverEvent != null && hoverEvent.action() == HoverEvent.Action.SHOW_TEXT) {
                HoverEvent.ShowText showText = (HoverEvent.ShowText)hoverEvent;
                Component component = showText.value();
            *///?} else {
            
            if (hoverEvent != null && hoverEvent.getAction() == HoverEvent.Action.SHOW_TEXT) {
                Component component = hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT);
             
            //?}
                if (component != null) {
                    String content = component.getString();

                    if (isMarkerLog(content)) {
                        Vec3 vec3 = parseMarkerPos(content);
                        DyeColor dyeColor = parseMarkerColor(content);
                        if(vec3 != null && dyeColor != null){
                            ClickEvent clickEvent = style.getClickEvent();
                            if(clickEvent != null){
                                //? if >=1.21.5 {
                                /*ClickEvent.CopyToClipboard copyToClipboard = (ClickEvent.CopyToClipboard)clickEvent;
                                updateActorPos(vec3,dyeColor,decodeParentHidden(copyToClipboard.value()));
                                *///?} else {
                                    updateActorPos(vec3,dyeColor,decodeParentHidden(clickEvent.getValue()));
                                //?}
                            } else {
                                    updateActorPos(vec3, dyeColor, null);
                            }
                        }
                    }
                }
            }

        }
    }
}
