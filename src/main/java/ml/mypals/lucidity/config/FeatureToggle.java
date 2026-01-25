package ml.mypals.lucidity.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.config.ConfigType;
import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.config.IConfigNotifiable;
import fi.dy.masa.malilib.config.IHotkeyTogglable;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.*;
import fi.dy.masa.malilib.interfaces.IValueChangeCallback;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.util.StringUtils;
import ml.mypals.lucidity.Lucidity;
import ml.mypals.lucidity.LucidityModInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum FeatureToggle implements IHotkeyTogglable, IConfigNotifiable<IConfigBoolean>
{
    SCULK_SENSOR_VISUALIZE("sculk_sensor_range",false,false),
    VIBRATION_TRACE("vibration_trace",false,true),
    BOAT_VIEW_RESTRICTION("boat_restriction", false,false),
    BODY_YAW("body_yaw",false,true),
    FLUID_TRANSPARENCY_OVERRIDE("fluid_transparency_override",false),
    /*? if >=1.21.4 {*/CREAKING_HEART_INDICATOR("creaking_heart_indicator",false),/*?}*/
    ITEM_MERG_RANGE_VISUALIZE("item_merg_range_visualize",false),
    ENDER_DRAGON_WAYPOINTS_VISUALIZE("ender_dragon_waypoints_visualize",false,true),
    ENDER_DRAGON_DESTRUCTION_VISUALIZE("ender_dragon_destruction_visualize",false,true),
    WITHER_DESTRUCTION_VISUALIZE("wither_destruction_visualize",false,true),
    NETHER_COORDINATE_CACULATOR("coordinate_caculator",false),
    WORLD_EATER_MINE_HELPER("world_eater_mine_helper",false),
    /*? if >=1.21 {*/VAULT_ITEM_DISPLAY("vault_item_display",false),/*?}*/
    B36_TARGET_PREVIEW("b36_preview",false),
    FLUID_SOURCE_HIGHLIGHT("fluid_source_highlight",false),
    ACCURATE_ENTITY_SHADOW("accurate_entity_shadow",false),
    CONTAINER_SIGNAL_PREVIEW("container_signal_preview",false),
    EXTENDED_HITBOX("extended_hitbox_visualize",false),
    BLOCK_NO_RANDOM_OFFSET("block_no_random_offset",false,true),
    //
    ZALGO_TEXT_DEOBF("zalgo_text_deobfuscate",false),
    FALLING_BLOCK_PREVIEW("falling_block_preview",false),
    SOUND_VISUALIZE("sound_visualize",false),
    BLOCK_EVENT_VISUALIZE("block_event_visualize",false),
    MOB_SPAWN_VISUALIZE("mob_spawn_visualize",false,true),
    INVISIBLE_ENTITY_OVERRIDE("invisible_entity_transparency_override",false),
    MICROTIMING_MARKER_VISUALIZER("carpet_tis_microtiming_visual_enhance",false);


    public static final ImmutableList<@NotNull FeatureToggle> VALUES = ImmutableList.copyOf(values());
    private static final String INFO_KEY = LucidityModInfo.MOD_ID+".config.features";

    private final String name;
    private String description;
    private String translatedName;
    private final IKeybind keybind;
    private boolean singlePlayerOnly = false;
    @Nullable
    private IValueChangeCallback<IConfigBoolean> callback;

    private boolean enabled = false;

    FeatureToggle(String name, boolean enabled, boolean singlePlayerOnly)
    {
        this(name,enabled,singlePlayerOnly,"",(iConfigBoolean)->{});
    }

    FeatureToggle(String name, boolean enabled)
    {
        this(name,enabled,false,"",(iConfigBoolean)->{});
    }
    FeatureToggle(String name, boolean enabled, boolean singlePlayerOnly, IValueChangeCallback<IConfigBoolean> iValueChangeCallback)
    {
        this(name,enabled,singlePlayerOnly,"",iValueChangeCallback);
    }
    FeatureToggle(String name, boolean enabled, boolean singlePlayerOnly, String defaultHotKey, @Nullable IValueChangeCallback<IConfigBoolean> iValueChangeCallback)
    {
        this.enabled = enabled;
        this.name = buildTranslateName("name."+name);
        this.description = buildTranslateName("comment."+name);
        this.singlePlayerOnly = singlePlayerOnly;
        this.keybind = KeybindMulti.fromStorageString(defaultHotKey, KeybindSettings.DEFAULT);
        this.keybind.setCallback(this::toggleValueWithMessage);
        getTranslatedName();
    }
    private static String buildTranslateName(String name)
    {
        return INFO_KEY + "." + name;
    }


    @Override
    public ConfigType getType() {
        return ConfigType.HOTKEY;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String getComment() {
        String comment = StringUtils.getTranslatedOrFallback(this.description, this.description);

        if (comment != null && this.singlePlayerOnly)
        {
            return comment + "\n" + StringUtils.translate(LucidityModInfo.MOD_ID + ".info.singleplayer_only");
        }

        return comment;
    }
    private boolean toggleValueWithMessage(KeyAction action, IKeybind key)
    {
        InfoUtils.printBooleanConfigToggleMessage(this.getPrettyName(), ! this.enabled);
        this.toggleBooleanValue();
        return true;
    }
    @Override
    public String getTranslatedName() {
        String name = StringUtils.getTranslatedOrFallback(this.name, this.name);
        this.translatedName = name;
        if (this.singlePlayerOnly)
        {
            return GuiBase.TXT_GOLD + name + GuiBase.TXT_RST;
        }
        return translatedName;
    }
    @Override
    public String getPrettyName() {
        return this.getTranslatedName();
    }
    @Override
    public void setPrettyName(String s) {
        this.translatedName = s;
    }

    @Override
    public void setTranslatedName(String s) {
        this.translatedName = s;
    }

    @Override
    public void setComment(String s) {
        this.description = s;
    }

    @Override
    public void setValueFromJsonElement(JsonElement jsonElement) {
        try
        {
            if (jsonElement.isJsonPrimitive())
            {
                this.enabled = jsonElement.getAsBoolean();
            }
            else
            {
                Lucidity.LOGGER.warn("Failed to read config value for {} from the JSON config", this.getName());
            }
        }
        catch (Exception e)
        {
            Lucidity.LOGGER.warn("Failed to read config value for {} from the JSON config", this.getName(), e);
        }
    }

    @Override
    public JsonElement getAsJsonElement() {
        return new JsonPrimitive(this.enabled);
    }


    public IKeybind getKeybind()
    {
        return keybind;
    }
    @Override
    public void toggleBooleanValue()
    {
        IHotkeyTogglable.super.toggleBooleanValue();
        onValueChanged();
    }
    @Override
    public boolean getBooleanValue() {
        return enabled;
    }

    @Override
    public boolean getDefaultBooleanValue() {
        return false;
    }

    @Override
    public void setBooleanValue(boolean b) {
        enabled = b;
    }

    @Override
    public void onValueChanged()
    {
        if (this.callback != null)
        {
            this.callback.onValueChanged(this);
        }
    }
    @Override
    public void setValueChangeCallback(IValueChangeCallback<IConfigBoolean> iValueChangeCallback) {
        this.callback = iValueChangeCallback;
    }
}


