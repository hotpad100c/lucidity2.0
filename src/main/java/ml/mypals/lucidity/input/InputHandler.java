package ml.mypals.lucidity.input;


import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybindManager;
import fi.dy.masa.malilib.hotkeys.IKeybindProvider;
import fi.dy.masa.malilib.hotkeys.IMouseInputHandler;
import ml.mypals.lucidity.LucidityModInfo;
import ml.mypals.lucidity.config.FeatureToggle;
import ml.mypals.lucidity.config.LucidityConfigs;

public class InputHandler implements IKeybindProvider, IMouseInputHandler {
    private static final InputHandler INSTANCE = new InputHandler();

    private InputHandler() {
        super();
    }
    public static InputHandler getInstance() {
        return INSTANCE;
    }
    @Override
    public void addKeysToMap(IKeybindManager manager) {
        for (IHotkey hotkey : FeatureToggle.VALUES) {
            manager.addKeybindToMap(hotkey.getKeybind());
        }
        for (IHotkey hotkey : LucidityConfigs.Generic.HOTKEY_LIST) {
            manager.addKeybindToMap(hotkey.getKeybind());
        }
    }
    @Override
    public void addHotkeys(IKeybindManager manager) {
        manager.addHotkeysForCategory(LucidityModInfo.MOD_NAME, LucidityModInfo.MOD_NAME+".hotkeys.category.generic_hotkeys", LucidityConfigs.Generic.HOTKEY_LIST);
        manager.addHotkeysForCategory(LucidityModInfo.MOD_NAME, LucidityModInfo.MOD_NAME+".hotkeys.category.visualizer_hotkeys", FeatureToggle.VALUES);
    }
}