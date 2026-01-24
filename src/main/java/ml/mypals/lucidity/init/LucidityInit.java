package ml.mypals.lucidity.init;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import ml.mypals.lucidity.Lucidity;
import ml.mypals.lucidity.LucidityModInfo;
import ml.mypals.lucidity.config.LucidityConfigs;
import ml.mypals.lucidity.gui.LucidityGuiConfigs;
import ml.mypals.lucidity.hotkeys.HotkeyCallbacks;
import ml.mypals.lucidity.input.InputHandler;

public class LucidityInit implements IInitializationHandler {
    @Override
    public void registerModHandlers()
    {
        ConfigManager.getInstance().registerConfigHandler(LucidityModInfo.MOD_ID,
                new LucidityConfigs());
        Registry.CONFIG_SCREEN.registerConfigScreenFactory(
                new ModInfo(LucidityModInfo.MOD_ID, LucidityModInfo.MOD_NAME, LucidityGuiConfigs::new)
        );
        InputEventHandler.getKeybindManager().registerKeybindProvider(InputHandler.getInstance());
        InputEventHandler.getInputManager().registerMouseInputHandler(InputHandler.getInstance());

        HotkeyCallbacks.init();
        Lucidity.lateInit();
    }
}
