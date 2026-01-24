package ml.mypals.lucidity.config.modmenu;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import ml.mypals.lucidity.gui.LucidityGuiConfigs;

public class LucidityModMenuImpl implements ModMenuApi
{
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory()
    {
        return (screen) -> {
            LucidityGuiConfigs gui = new LucidityGuiConfigs();
            gui.setParent(screen);
            return gui;
        };
    }
}