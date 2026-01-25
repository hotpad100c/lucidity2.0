package ml.mypals.lucidity.features.yacaLikeConfig;

import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import static ml.mypals.lucidity.config.LucidityConfigs.Other.YACL_STYLE;

public class YaclLikeConfigTab extends ButtonGeneric {
    public static final WidgetSprites SPRITES = new WidgetSprites(ResourceLocation.withDefaultNamespace("widget/tab_selected"), ResourceLocation.withDefaultNamespace("widget/tab"), ResourceLocation.withDefaultNamespace("widget/tab_selected_highlighted"), ResourceLocation.withDefaultNamespace("widget/tab_highlighted"));

    public YaclLikeConfigTab(int x, int y, int width, int height, String text, String... hoverStrings) {
        super(x, y, width, height, text, hoverStrings);
    }

    @Override
    //? if >=1.21.6 {
    /*public void render(GuiGraphics drawContext, int mouseX, int mouseY, boolean selected) {
    *///?} else {
    public void render(int mouseX, int mouseY, boolean selected, GuiGraphics drawContext) {
    //?}
        if (this.visible) {
            boolean yacaStyle = YACL_STYLE.getBooleanValue();
            this.renderDefaultBackground = !yacaStyle;
            if(yacaStyle) {
                drawContext.blitSprite(/*? if >= 1.21.6 {*/ /*RenderPipelines.GUI_TEXTURED,*//*?} else if >=1.21.3 {*/RenderType::guiTextured, /*?}*/
                        SPRITES.get(!this.enabled, this.hovered), this.getX(),
                        this.getY() - 2, this.width, this.height);
                if (!this.enabled) {
                    this.renderFocusUnderline(drawContext, mc.font, Color.WHITE.getRGB());
                }
            }
            //? if >=1.21.6 {
            /*super.render(drawContext, mouseX, mouseY, selected);
            *///?} else {
            super.render(mouseX, mouseY, selected, drawContext);
            //?}
        }
    }


    private void renderFocusUnderline(GuiGraphics guiGraphics, Font font, int i) {
        int j = Math.min(font.width(this.displayString), this.getWidth() - 4);
        int k = this.getX() + (this.getWidth() - j) / 2;
        int l = this.getY() + this.getHeight() - 2;
        guiGraphics.fill(k, l, k + j, l + 1, i);
    }

}
