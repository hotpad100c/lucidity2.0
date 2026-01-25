package ml.mypals.lucidity.gui;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.BooleanHotkeyGuiWrapper;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import ml.mypals.lucidity.LucidityModInfo;
import ml.mypals.lucidity.config.*;
import ml.mypals.lucidity.features.yacaLikeConfig.YaclLikeConfigTab;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LucidityGuiConfigs extends GuiConfigsBase {
    public static LucidityGuiConfigs CONFIG_INSTANCE = null;
    // If you have an add-on mod, you can append stuff to these GUI lists by re-assigning a new list to it.
    // I'd recommend using your own config handler for the config serialization to/from config files.
    // Although the config dirty marking stuff probably is a mess in this old malilib code base for that stuff...
    public LucidityGuiConfigs()
    {
        super(
                10, 50, LucidityModInfo.MOD_ID, null,
                LucidityModInfo.MOD_ID+".title.configs",
                String.format("%s", LucidityModInfo.VERSION)
        );
        CONFIG_INSTANCE = this;
    }

    public static LucidityGuiTab tab = LucidityGuiTab.GENERIC;

    public LucidityGuiConfigs(int listX, int listY, String modId, Screen parent, String titleKey, Object... args) {
        super(listX, listY, modId, parent, titleKey, args);
    }

    @Override
    protected boolean useKeybindSearch()
    {
        return true;
    }
    @Override
    public void render(GuiGraphics drawContext, int mouseX, int mouseY, float partialTicks) {
        super.render(drawContext,mouseX,mouseY,partialTicks);
    }
    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics, int i, int j, float f) {
        if (this.minecraft.level == null) {
            this.renderPanorama(guiGraphics, f);
        }
        //? if <1.21.6 {
        this.renderBlurredBackground(/*? if <=1.21.1 {*//*f*//*?}*/);
        //?}
        this.renderMenuBackground(guiGraphics);
        guiGraphics.blit(/*? if >= 1.21.6 {*/ /*RenderPipelines.GUI_TEXTURED,*//*?} else if >=1.21.3 {*/RenderType::guiTextured, /*?}*/Screen.HEADER_SEPARATOR, 0, this.getListY() - 6, 0.0F, 0.0F, this.width, 2, 32, 2);

        guiGraphics.blit(/*? if >= 1.21.6 {*/ /*RenderPipelines.GUI_TEXTURED,*//*?} else if >=1.21.3 {*/RenderType::guiTextured, /*?}*/Screen.HEADER_SEPARATOR, 0, this.getListY()+this.getBrowserHeight(), 0.0F, 0.0F, this.width, 2, 32, 2);
    }
    @Override
    public void initGui()
    {
        super.initGui();
        this.clearOptions();

        int x = 10;
        int y = 26;
        int rows = 1;

        for (LucidityGuiTab tab : LucidityGuiTab.values())
        {
            int width = this.getStringWidth(tab.getDisplayName()) + 10;

            if (x >= this.getScreenWidth() - width - 10)
            {
                x = 10;
                y += 22;
                rows++;
            }

            x += this.createTab(x, y, width, tab);
        }

        if (rows > 1)
        {
            int scrollbarPosition = this.getListWidget().getScrollbar().getValue();
            this.setListPosition(this.getListX(), 50 + (rows - 1) * 22);
            this.reCreateListWidget();
            this.getListWidget().getScrollbar().setValue(scrollbarPosition);
            this.getListWidget().refreshEntries();
        }
    }
    @Override
    public List<ConfigOptionWrapper> getConfigs()
    {
        LucidityGuiTab tab = LucidityGuiConfigs.tab;
        if (tab == LucidityGuiTab.GENERIC)
        {
            return ConfigOptionWrapper.createFor(LucidityConfigs.Generic.OPTIONS);
        }
        if (tab == LucidityGuiTab.FEATURES)
        {
            List<IConfigBase> list = new ArrayList<>(FeatureToggle.VALUES.stream().map(this::wrapConfig).toList());
            return ConfigOptionWrapper.createFor(list);
        }
        if(tab == LucidityGuiTab.EXPLOSION_VISUALIZER){
            return ConfigOptionWrapper.createFor(ExplosionVisualizerConfigs.VALUES);
        }
        if(tab == LucidityGuiTab.COLORS){
            return ConfigOptionWrapper.createFor(VisualizerColors.VALUES);
        }
        else if (tab == LucidityGuiTab.SELECTIVE_RENDERING)
        {
            return ConfigOptionWrapper.createFor(SelectiveRenderingConfigs.VALUES);
        }
        else if (tab == LucidityGuiTab.IMAGE_RENDERING)
        {
            return ConfigOptionWrapper.createFor(ImageRendererConfigs.VALUES);
        }
        else if (tab == LucidityGuiTab.OTHER)
        {
            return ConfigOptionWrapper.createFor(LucidityConfigs.Other.OPTIONS);
        }
        return Collections.emptyList();
    }
    protected BooleanHotkeyGuiWrapper wrapConfig(FeatureToggle config)
    {
        return new BooleanHotkeyGuiWrapper(config.getName(), config, config.getKeybind());
    }
    private int createTab(int x, int y, int width, LucidityGuiTab tab)
    {
        ButtonGeneric button = new LucidityTab(x, y, width, 20, tab.getDisplayName());
        button.setEnabled(LucidityGuiConfigs.tab != tab);
        this.addButton(button, new LucidityConfigTab(tab, this));

        return button.getWidth() + 2;
    }

    public static class LucidityTab extends YaclLikeConfigTab {
        public LucidityTab(int x, int y, int width, int height, String string) {
            super(x, y, width, height,string);
        }
    }
    public static class LucidityConfigTab implements IButtonActionListener
    {
        private final LucidityGuiConfigs parent;
        private final LucidityGuiTab tab;

        public LucidityConfigTab(LucidityGuiTab tab, LucidityGuiConfigs parent)
        {
            this.tab = tab;
            this.parent = parent;
        }

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton)
        {
            LucidityGuiConfigs.tab = this.tab;
            this.parent.reCreateListWidget();
            this.parent.getListWidget().resetScrollbarPosition();
            this.parent.initGui();
        }
    }
}
