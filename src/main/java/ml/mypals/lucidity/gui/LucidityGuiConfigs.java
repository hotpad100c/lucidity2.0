package ml.mypals.lucidity.gui;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.mojang.datafixers.types.Func;
import fi.dy.masa.malilib.config.*;
import fi.dy.masa.malilib.config.options.BooleanHotkeyGuiWrapper;
import fi.dy.masa.malilib.config.options.ConfigBase;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptions;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import ml.mypals.lucidity.LucidityModInfo;
import ml.mypals.lucidity.config.*;
import ml.mypals.lucidity.features.yaclLikeConfig.YaclLikeConfigTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
//? if >=1.21.6 {
import net.minecraft.client.renderer.RenderPipelines;
//?}
//?if>=1.21.9{

import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

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
        /*this.renderBlurredBackground(/^? if <=1.21.1 {^//^f^//^?}^/);
        *///?}
        this.renderMenuBackground(guiGraphics);
        guiGraphics.blit(/*? if >= 1.21.6 {*/ RenderPipelines.GUI_TEXTURED,/*?} else if >=1.21.3 {*//*RenderType::guiTextured, *//*?}*/Screen.HEADER_SEPARATOR, 0, this.getListY() - 6, 0.0F, 0.0F, this.width, 2, 32, 2);

        guiGraphics.blit(/*? if >= 1.21.6 {*/ RenderPipelines.GUI_TEXTURED,/*?} else if >=1.21.3 {*//*RenderType::guiTextured, *//*?}*/Screen.HEADER_SEPARATOR, 0, this.getListY()+this.getBrowserHeight(), 0.0F, 0.0F, this.width, 2, 32, 2);
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
            List<ConfigOptionWrapper> wrappers = new ArrayList<>();

            for (IConfigBase config : SelectiveRenderingConfigs.VALUES) {
                wrappers.add(new ConfigOptionWrapper(config));

                if (config instanceof ExportableConfigStringList) {
                    wrappers.add(new ConfigOptionWrapper(
                            new ConfigImportExportButtons((ExportableConfigStringList) config)
                    ));
                }
            }

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

    @Override
    protected WidgetListConfigOptions createListWidget(int listX, int listY) {
        return new WidgetListConfigOptions(listX, listY,
                this.getBrowserWidth(), this.getBrowserHeight(),
                this.getConfigWidth(), 0.f, this.useKeybindSearch(), this) {

            @Override
            protected WidgetConfigOption createListEntryWidget(int x, int y, int listIndex,
                                                               boolean isOdd, ConfigOptionWrapper entry) {

                if (entry.getType() == ConfigOptionWrapper.Type.CONFIG
                        && entry.getConfig() instanceof ConfigImportExportButtons ioButtons) {

                    return new WidgetImportExportButtons(
                            x, y,
                            this.width, 22,
                            this.maxLabelWidth,
                            this.configWidth,
                            entry, listIndex,
                            this.parent, this,
                            ioButtons::exportToClipboard,
                            ioButtons::importFromClipboard
                    );
                }

                return super.createListEntryWidget(x, y, listIndex, isOdd, entry);
            }
        };
    }

    public static class ConfigImportExportButtons extends ConfigBase<ConfigImportExportButtons> implements IConfigBoolean {

        private final IConfigStringList  target;
        public ConfigImportExportButtons(IConfigStringList target) {
            //super(ConfigType.BOOLEAN, target.getName() + "_io", "", "", "");
            super(ConfigType.BOOLEAN, "", "", "", "");
            this.target = target;
        }
        public void exportToClipboard() {
            JsonArray arr = new JsonArray();
            for (String s : this.target.getStrings()) {
                arr.add(new JsonPrimitive(s));
            }
            Minecraft.getInstance().keyboardHandler.setClipboard(arr.toString());
        }

        public boolean importFromClipboard(Void v) {
            String clipboard = Minecraft.getInstance().keyboardHandler.getClipboard().trim();
            try {
                JsonElement element = JsonParser.parseString(clipboard);
                if (!element.isJsonArray()) return false;

                List<String> values = new ArrayList<>();
                for (JsonElement e : element.getAsJsonArray()) {
                    values.add(e.getAsString());
                }
                this.target.setStrings(values);
                return true;
            } catch (JsonParseException e) {
                return false;
            }
        }
        public IConfigStringList  getTarget() {
            return this.target;
        }
        @Override public boolean isModified() { return false; }
        @Override public void resetToDefault() {}
        @Override public void setValueFromJsonElement(JsonElement element) {}
        @Override public JsonElement getAsJsonElement() { return new JsonObject(); }

        @Override
        public boolean getBooleanValue() {
            return false;
        }

        @Override
        public boolean getDefaultBooleanValue() {
            return false;
        }

        @Override
        public void setBooleanValue(boolean b) {

        }

        @Override
        public String getDefaultStringValue() {
            return "";
        }

        @Override
        public void setValueFromString(String s) {

        }

        @Override
        public boolean isModified(String s) {
            return false;
        }

        @Override
        public String getStringValue() {
            return "";
        }
    }
    public static class ExportableConfigStringList extends ConfigStringList {

        public ExportableConfigStringList(String name, ImmutableList<String> defaultValue) {
            super(name, defaultValue);
        }

        public void importFrom(List<String> newValues) {
            this.setStrings(newValues);
        }

        public void exportToClipboard() {
            String json = this.getAsJsonElement().toString();
            Minecraft.getInstance().keyboardHandler.setClipboard(json);
        }
        @Override
        public ExportableConfigStringList apply(String key) {
            super.apply(key);
            return this;
        }
        public boolean importFromClipboard() {
            String clipboard = Minecraft.getInstance().keyboardHandler.getClipboard().trim();
            try {
                JsonElement element = JsonParser.parseString(clipboard);
                if (!element.isJsonArray()) {
                    return false;
                }
                this.setValueFromJsonElement(element);
                this.setStrings(this.getStrings());
                ConfigManager.getInstance().onConfigsChanged(LucidityModInfo.MOD_ID);
                return true;
            } catch (JsonParseException e) {
                return false;
            }
        }
    }
    public static class WidgetImportExportButtons extends WidgetConfigOption {

        private final Runnable exportTask;
        private final Function<Void,Boolean> importTask;
        private ButtonGeneric exportBtn;
        private ButtonGeneric importBtn;

        public WidgetImportExportButtons(int x, int y, int width, int height,
                                         int labelWidth, int configWidth,
                                         ConfigOptionWrapper wrapper, int listIndex,
                                         IKeybindConfigGui host,
                                         WidgetListConfigOptionsBase<?, ?> parent,
                                         Runnable exportTask, Function<Void,Boolean> importTask) {
            super(x, y, width, height, labelWidth, configWidth, wrapper, listIndex, host, parent);
            this.exportTask = exportTask;
            this.importTask = importTask;
        }

        //?if>=1.21.9{

        protected void addConfigOption(int x, int y, int labelWidth, int configWidth, IConfigBase config) {
        //?}else{
        /*@Override
        protected void addConfigOption(int x, int y, float zLevel, int labelWidth, int configWidth, IConfigBase config) {
        *///?}
            int btnX = x + labelWidth + 10;
            int btnY = y + 1;
            int btnWidth = (configWidth - 10) / 2;

            exportBtn = new ButtonGeneric(btnX, btnY, btnWidth, 20, Component.translatable("lucidity.config.selective_rendering.export").getString());
            importBtn = new ButtonGeneric(btnX + btnWidth + 10, btnY, btnWidth, 20, Component.translatable("lucidity.config.selective_rendering.import").getString());
            this.addWidget(exportBtn);
            this.addWidget(importBtn);
        }
        @Override
        public boolean isMouseOver(int mouseX, int mouseY) {
            return exportBtn.isMouseOver(mouseX, mouseY) || importBtn.isMouseOver(mouseX, mouseY) || super.isMouseOver(mouseX, mouseY);
        }


        //?if>=1.21.9{
        @Override
        public boolean onMouseClicked(MouseButtonEvent mouseButtonEvent, boolean db) {
            int mouseY = (int) mouseButtonEvent.y();
            int mouseX = (int) mouseButtonEvent.x();
        //?}else{
        /*@Override
        public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
        *///?}
            if (this.exportBtn != null && this.exportBtn.isMouseOver(mouseX, mouseY)) {
                Screen screen = Minecraft.getInstance().screen;
                if (screen instanceof GuiBase guiBase) {
                    exportTask.run();
                    guiBase.addMessage(Message.MessageType.SUCCESS, 400,
                    Component.translatable("lucidity.config.selective_rendering.export_success").getString());
                }
                return true;
            }
            if (this.importBtn != null && this.importBtn.isMouseOver(mouseX, mouseY)) {
                boolean s = importTask.apply(null);
                Screen screen = Minecraft.getInstance().screen;
                if (screen instanceof GuiBase guiBase) {
                    if(s){
                        guiBase.initGui();
                        guiBase.addMessage(Message.MessageType.SUCCESS, 400,
                                Component.translatable("lucidity.config.selective_rendering.import_success").getString());
                    } else {
                        guiBase.addMessage(Message.MessageType.WARNING, 400,
                                Component.translatable("lucidity.config.selective_rendering.import_fail").getString());
                    }
                }
                return true;
            }
            //?if>=1.21.9{
            return super.onMouseClicked(mouseButtonEvent, db);
            //?}else{
            /*    return super.onMouseClicked(mouseX, mouseY, mouseButton);
            *///?}
        }
    }
}
