package ml.mypals.lucidity.mixin.features.yaclLikeConfig;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.interfaces.IMessageConsumer;
import fi.dy.masa.malilib.interfaces.IStringConsumer;
import ml.mypals.lucidity.features.yaclLikeConfig.YaclLikeConfigTab;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.network.chat.Component;

import static ml.mypals.lucidity.config.LucidityConfigs.Other.YACL_STYLE;

@Mixin(value = GuiBase.class,remap = false)
public abstract class GuiBaseMixin extends Screen implements IMessageConsumer, IStringConsumer {
    protected GuiBaseMixin(Component component) {
        super(component);
    }

    @WrapMethod(method = "addButton")
    private ButtonBase addButton(ButtonBase button, IButtonActionListener listener, Operation<ButtonBase> original) {
        if(YACL_STYLE.getBooleanValue()){
            YaclLikeConfigTab tab = new YaclLikeConfigTab(button.getX(), button.getY(),
                    button.getWidth(), button.getHeight(), ((ButtonBaseAccessor)button).getDisplayString(), button.getHoverStrings().toArray(new String[0]));
            tab.setEnabled(((ButtonBaseAccessor)button).getEnabled());
            return original.call(tab, listener);
        }else {
            return original.call(button, listener);
        }
    }
}
