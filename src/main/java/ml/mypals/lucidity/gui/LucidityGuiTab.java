package ml.mypals.lucidity.gui;

import fi.dy.masa.malilib.util.StringUtils;

import static ml.mypals.lucidity.LucidityModInfo.MOD_ID;

public enum LucidityGuiTab
{
    GENERIC                 (MOD_ID + ".config_gui.generic"),
    FEATURES                (MOD_ID + ".config_gui.features"),
    EXPLOSION_VISUALIZER    (MOD_ID + ".config_gui.explosion_visualizer"),
    COLORS                  (MOD_ID + ".config_gui.colors"),
    SELECTIVE_RENDERING     (MOD_ID + ".config_gui.selective_rendering"),
    IMAGE_RENDERING         (MOD_ID + ".config_gui.image_rendering"),
    OTHER                   (MOD_ID + ".config_gui.other");

    private final String translationKey;

    LucidityGuiTab(String translationKey)
    {
        this.translationKey = translationKey;
    }

    public String getDisplayName()
    {
        return StringUtils.translate(this.translationKey);
    }
}
