package ml.mypals.lucidity.mixin.features.enhancedMalilib;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fi.dy.masa.malilib.config.IConfigStringList;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.interfaces.ISelectionListener;
import fi.dy.masa.malilib.gui.widgets.*;
import ml.mypals.lucidity.config.LucidityConfigs;
import ml.mypals.lucidity.gui.LucidityGuiConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(WidgetListConfigOptions.class)
public abstract class GuiConfigBaseMixin extends WidgetListConfigOptionsBase<GuiConfigsBase.ConfigOptionWrapper, WidgetConfigOption> {


    @Shadow @Final protected GuiConfigsBase parent;

    public GuiConfigBaseMixin(int x, int y, int width, int height, int configWidth) {
        super(x, y, width, height, configWidth);
    }


    @WrapOperation(
            method = "getAllEntries",
            at = @At(
                    value = "INVOKE",
                    target = "Lfi/dy/masa/malilib/gui/GuiConfigsBase;getConfigs()Ljava/util/List;"
            )
    )
    private List injectImportExportButtons(GuiConfigsBase instance, Operation<List> org) {
        if(LucidityConfigs.Other.EXPORTABLE_LIST.getBooleanValue()){
            List<GuiConfigsBase.ConfigOptionWrapper> original = instance.getConfigs();
            List<GuiConfigsBase.ConfigOptionWrapper> result = new ArrayList<>();

            for (GuiConfigsBase.ConfigOptionWrapper wrapper : original) {
                result.add(wrapper);

                if (wrapper.getType() == GuiConfigsBase.ConfigOptionWrapper.Type.CONFIG
                        && wrapper.getConfig() instanceof IConfigStringList list
                        && !(wrapper.getConfig() instanceof LucidityGuiConfigs.ConfigImportExportButtons)
                        && !(wrapper.getConfig() instanceof LucidityGuiConfigs.ExportableConfigStringList)) {
                    result.add(new GuiConfigsBase.ConfigOptionWrapper(new LucidityGuiConfigs.ConfigImportExportButtons(list)));
                }
            }

            return result;
        }
        return org.call(instance);
    }
    @Inject(
            method = "createListEntryWidget(IIIZLfi/dy/masa/malilib/gui/GuiConfigsBase$ConfigOptionWrapper;)Lfi/dy/masa/malilib/gui/widgets/WidgetConfigOption;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void interceptImportExportWidget(int x, int y, int listIndex,
                                             boolean isOdd, GuiConfigsBase.ConfigOptionWrapper entry,
                                             CallbackInfoReturnable<WidgetConfigOption> cir) {

        if (LucidityConfigs.Other.EXPORTABLE_LIST.getBooleanValue() &&
                entry.getType() == GuiConfigsBase.ConfigOptionWrapper.Type.CONFIG
                && entry.getConfig() instanceof LucidityGuiConfigs.ConfigImportExportButtons ioButtons) {
            cir.setReturnValue(new LucidityGuiConfigs.WidgetImportExportButtons(
                    x, y,
                    this.width, 22,
                    this.maxLabelWidth,
                    this.configWidth,
                    entry, listIndex,
                    this.parent, (WidgetListConfigOptionsBase<?, ?>) (Object) this,
                    ioButtons::exportToClipboard,
                    ioButtons::importFromClipboard
            ));
        }
    }
}
