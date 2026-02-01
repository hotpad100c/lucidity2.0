package ml.mypals.lucidity.mixin.features.visualizers.containerOutputPreview;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import static ml.mypals.lucidity.config.FeatureToggle.CONTAINER_SIGNAL_PREVIEW;

@Mixin(BookViewScreen.class)
public abstract class BookViewScreenMixin extends Screen {

    @Shadow private int currentPage;

    @Shadow private int cachedPage;

    protected BookViewScreenMixin(Component component) {
        super(component);
    }


    @Shadow protected abstract int getNumPages();

    @Shadow private Component pageMsg;

    @Unique

    private int renderComparatorOutput(GuiGraphics context, Font font, Component component, int i, int j, int k, boolean bl){

        int currentPage = this.currentPage;
        int pageCount = this.getNumPages();
        float f = pageCount > 1 ? (float)currentPage / ((float)pageCount - 1.0F) : 1.0F;
        int result = Mth.floor(f * 14.0F) + 1;
        int width = (this.width - 192) / 2;
        Component outputText = Component.literal(component.getString()).append(Component.literal("(C: " + result + ")").withStyle(ChatFormatting.GOLD));
        int length = this.font.width(outputText);
        //? if >=1.21.6 {
        /*context.drawString(font, outputText, width - length + 192 - 44, j, k, bl);
        return 1;
        *///?} else {
        return context.drawString(font, outputText, width - length + 192 - 44, j, k, bl);
        //?}
    }

    //? if >=1.21.6 {
    /*@WrapOperation(method = "render",at = @At(target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V",value = "INVOKE"))
    private void render(GuiGraphics instance, Font font, Component component, int i, int j, int k, boolean bl, Operation<Void> original) {
        if (!CONTAINER_SIGNAL_PREVIEW.getBooleanValue()) {
            original.call(instance, font, component, i, j, k, bl);
        }else {
            renderComparatorOutput(instance,font, component,i, j,k,bl);
        }
    }
    *///?} else {
    @WrapOperation(method = "render",at = @At(target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I",value = "INVOKE"))
    private int render(GuiGraphics instance, Font font, Component component, int i, int j, int k, boolean bl, Operation<Integer> original) {
        if (!CONTAINER_SIGNAL_PREVIEW.getBooleanValue()) {
            return original.call(instance, font, component, i, j, k, bl);
        }else {
            return renderComparatorOutput(instance,font, component,i, j,k,bl);
        }
    }
    //?}

}
