package ml.mypals.lucidity.mixin.features.yaclLikeConfig;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.datafixers.types.Func;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptions;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
//? if >=1.21.6 {
/*import net.minecraft.client.renderer.RenderPipelines;
*///?}
//? if >=1.21.9 {
/*import net.minecraft.client.input.MouseButtonEvent;
*///?}
import net.minecraft.client.renderer.RenderType;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.function.Function;

import static ml.mypals.lucidity.config.LucidityConfigs.Other.YACL_STYLE;

@Mixin(value = GuiConfigsBase.class,remap = false)
public abstract class GuiConfigBaseMixin extends GuiListBase<GuiConfigsBase.ConfigOptionWrapper, WidgetConfigOption, WidgetListConfigOptions> {

    @Unique
    private int lucidity$scrollOffset = 0;

    @Unique
    private int lucidity$maxScrollOffset = 0;

    @Unique
    private static final int NAVBAR_MARGIN = 28;

    @Unique
    private static final int BUTTON_TOP_Y = 26;




    protected GuiConfigBaseMixin(int listX, int listY) {
        super(listX, listY);
    }

    @Override
    public void render(GuiGraphics drawContext, int mouseX, int mouseY, float partialTicks) {
        if(YACL_STYLE.getBooleanValue()){
            if (this.drawContext == null || !this.drawContext.equals(drawContext)) {
                this.drawContext = drawContext;
            }

            lucidity$offsetAndRun((ignored)->{
                this.renderBackground(drawContext, mouseX, mouseY,partialTicks);
                this.drawTitle(drawContext, mouseX, mouseY, partialTicks);
                //? if >=1.21.6 {
                /*this.drawWidgets(drawContext, mouseX, mouseY);
                this.drawTextFields(drawContext, mouseX, mouseY);
                this.drawButtons(drawContext, mouseX, mouseY, partialTicks);
                *///?} else {
                this.drawWidgets(mouseX, mouseY, drawContext);
                this.drawTextFields(mouseX, mouseY, drawContext);
                this.drawButtons(mouseX, mouseY, partialTicks, drawContext);
                //?}
                this.drawContents(drawContext, mouseX, mouseY, partialTicks);
                //? if >=1.21.6 {
                /*this.drawHoveredWidget(drawContext, mouseX, mouseY);
                this.drawButtonHoverTexts(drawContext, mouseX, mouseY, partialTicks);
                *///?} else {
                this.drawHoveredWidget(mouseX, mouseY, drawContext);
                this.drawButtonHoverTexts(mouseX, mouseY, partialTicks, drawContext);
                //?}
                this.drawGuiMessages(drawContext);
                return false;
            });

        }else{
            super.render(drawContext, mouseX, mouseY, partialTicks);
        }
    }
    @Unique
    private boolean lucidity$offsetAndRun(Function<Boolean,Boolean> runnable){
        int oldX = this.getListX();
        int oldY = this.getListY();
        int oldScroll = Objects.requireNonNull(this.getListWidget()).getScrollbar().getValue();
        this.setListPosition(this.getListX(), 50);
        this.reCreateListWidget();
        this.getListWidget().getScrollbar().setValue(oldScroll);
        this.getListWidget().refreshEntries();

        boolean r = runnable.apply(false);

        this.setListPosition(oldX, oldY);
        this.reCreateListWidget();
        this.getListWidget().getScrollbar().setValue(oldScroll);
        this.getListWidget().refreshEntries();
        return r;
    }
    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if(YACL_STYLE.getBooleanValue()){
            if (this.minecraft.level == null) {
                this.renderPanorama(guiGraphics, partialTicks);
            }
            //? if <1.21.6 {
            this.renderBlurredBackground(/*? if <=1.21.1 {*//*partialTicks*//*?}*/);
             //?}
            this.renderMenuBackground(guiGraphics);
            guiGraphics.blit(/*? if >= 1.21.6 {*/ /*RenderPipelines.GUI_TEXTURED,*//*?} else if >=1.21.3 {*/RenderType::guiTextured, /*?}*/Screen.HEADER_SEPARATOR, 0, this.getListY() - 6, 0.0F, 0.0F, this.width, 2, 32, 2);
            guiGraphics.blit(/*? if >= 1.21.6 {*/ /*RenderPipelines.GUI_TEXTURED,*//*?} else if >=1.21.3 {*/RenderType::guiTextured, /*?}*/Screen.FOOTER_SEPARATOR, 0, this.getListY()+this.getBrowserHeight(), 0.0F, 0.0F, this.width, 2, 32, 2);

        }else{
            super.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }
    @Override
    //? if >=1.21.6 {
    /*public void drawButtons( GuiGraphics drawContext, int mouseX, int mouseY, float partialTicks) {
    *///?} else {
    public void drawButtons(int mouseX, int mouseY, float partialTicks, GuiGraphics drawContext) {
    //?}
        if(!YACL_STYLE.getBooleanValue()) {
            //? if >=1.21.6 {
            /*super.drawButtons(drawContext,mouseX,mouseY,partialTicks);
            *///?} else {
            super.drawButtons(mouseX,mouseY,partialTicks,drawContext);
            //?}
            return;
        }
        int screenWidth = this.getScreenWidth();

        int totalWidth = 0;
        for (ButtonBase button : ((GuiBaseAccessor)this).getButtons()) {
            totalWidth += button.getWidth();
        }

        int availableWidth = screenWidth - NAVBAR_MARGIN * 2;
        this.lucidity$maxScrollOffset = Math.max(0, totalWidth - availableWidth);

        int xOffset = NAVBAR_MARGIN - this.lucidity$scrollOffset;

        for (ButtonBase button : ((GuiBaseAccessor)this).getButtons()) {
            int originalX = button.getX();
            button.setPosition(xOffset, 26);

            if (xOffset + button.getWidth() > 0 && xOffset < screenWidth) {
                //? if >=1.21.6 {
                /*button.render(drawContext,mouseX,mouseY,button.isMouseOver());
                *///?} else {
                button.render(mouseX, mouseY, button.isMouseOver(), drawContext);
                 //?}

            }

            button.setPosition(originalX, button.getY());

            xOffset += button.getWidth();
        }

        this.lucidity$drawScrollIndicators(drawContext, screenWidth);

    }
    @Override
    //? if >=1.21.9 {
    /*public boolean onMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
    *///?} else {
    public boolean onMouseScrolled(int mouseX, int mouseY, double horizontalAmount, double verticalAmount) {
    //?}
        if(!YACL_STYLE.getBooleanValue()) return super.onMouseScrolled(mouseX,mouseY,horizontalAmount,verticalAmount);
        if (mouseY <= 50) {
            this.lucidity$setScrollOffset(this.lucidity$scrollOffset - (int)(verticalAmount * 15) - (int)(horizontalAmount * 15));
            return true;
        }else {
            super.onMouseScrolled(mouseX,mouseY,horizontalAmount,verticalAmount);
        }
        return false;
    }

    @WrapMethod(method = "onMouseClicked")
    //? if >=1.21.9 {
    /*private boolean lucidity$onMouseClickedScrollableTabs(MouseButtonEvent click, boolean doubleClick, Operation<Boolean> original) {
        if (!YACL_STYLE.getBooleanValue()) return original.call(click, doubleClick);
        double mouseY = click.y();
        double mouseX = click.x();
    *///?} else {
    private boolean lucidity$onMouseClickedScrollableTabs(int mouseX, int mouseY, int mouseButton, Operation<Boolean> original) {
        if (!YACL_STYLE.getBooleanValue()) return original.call(mouseX, mouseY, mouseButton);
    //?}


        return lucidity$offsetAndRun((ignored)->{
            if (mouseY >= 20 && mouseY <= 50) {
                int xOffset = NAVBAR_MARGIN - this.lucidity$scrollOffset;

                for (ButtonBase button : ((GuiBaseAccessor)this).getButtons()) {
                    int originalX = button.getX();
                    int originalY = button.getY();

                    button.setPosition(xOffset, BUTTON_TOP_Y);

                    //? if >=1.21.9 {
                    /*if (button.onMouseClicked(click,doubleClick)) {
                    *///?} else {
                    if (button.onMouseClicked(mouseX, mouseY, mouseButton)) {
                    //?}
                        button.setPosition(originalX, originalY);
                        return true;
                    }

                    button.setPosition(originalX, originalY);
                    xOffset += button.getWidth();
                }

                return false;
            }
            //? if >=1.21.9 {
            /*return original.call(click,doubleClick);
            *///?} else {
            return original.call(mouseX, mouseY, mouseButton);
            //?}
        });
    }
    @Unique
    private void lucidity$setScrollOffset(int offset) {
        this.lucidity$scrollOffset = Math.max(0, Math.min(offset, this.lucidity$maxScrollOffset));
    }

    @Unique
    private void lucidity$drawScrollIndicators(GuiGraphics drawContext, int screenWidth) {
        if (this.lucidity$scrollOffset < this.lucidity$maxScrollOffset - NAVBAR_MARGIN) {
            this.drawString(drawContext, "→", screenWidth - 15, BUTTON_TOP_Y - 6, 0xFFFFFFFF);
        }
        if (this.lucidity$scrollOffset > NAVBAR_MARGIN) {
            this.drawString(drawContext, "←", 5, BUTTON_TOP_Y - 6, 0xFFFFFFFF);
        }
    }
}
