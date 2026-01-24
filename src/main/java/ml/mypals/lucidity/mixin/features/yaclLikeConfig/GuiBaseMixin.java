package ml.mypals.lucidity.mixin.features.yaclLikeConfig;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.interfaces.IMessageConsumer;
import fi.dy.masa.malilib.interfaces.IStringConsumer;
import ml.mypals.lucidity.features.yacaLikeConfig.YaclLikeConfigTab;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.network.chat.Component;

import java.util.List;

import static ml.mypals.lucidity.config.LucidityConfigs.Other.YACL_STYLE;

@Mixin(value = GuiBase.class,remap = false)
public abstract class GuiBaseMixin extends Screen implements IMessageConsumer, IStringConsumer {
    @Shadow
    private List<ButtonBase> buttons;

    @Shadow
    public abstract int getStringWidth(String text);

    @Shadow
    protected abstract void drawString(GuiGraphics drawContext, String text, int x, int y, int color);

    @Unique
    private int lucidity$scrollOffset = 0;

    @Unique
    private int lucidity$maxScrollOffset = 0;

    @Unique
    private static final int NAVBAR_MARGIN = 28;

    @Unique
    private static final int BUTTON_TOP_Y = 26; // 按钮的 Y 坐标

    @Unique
    private int lucidity$originalButtonX = -1; // 存储按钮的原始 X 坐标

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
    /*@Inject(method = "drawButtons", at = @At("HEAD"), cancellable = true, remap = false)
    private void lucidity$drawScrollableButtons(int mouseX, int mouseY, float partialTicks, GuiGraphics drawContext, CallbackInfo ci) {
        if(!YACL_STYLE.getBooleanValue()) return;
        if (this.buttons.isEmpty()) {
            ci.cancel();
            return;
        }

        // 获取屏幕宽度
        int screenWidth = ((GuiBase)(Object)this).getScreenWidth();

        // 计算所有按钮的总宽度
        int totalWidth = 0;
        for (ButtonBase button : this.buttons) {
            totalWidth += button.getWidth();
        }

        // 计算最大滚动偏移量
        int availableWidth = screenWidth - NAVBAR_MARGIN * 2;
        this.lucidity$maxScrollOffset = Math.max(0, totalWidth - availableWidth);

        // 应用滚动偏移渲染按钮
        int xOffset = NAVBAR_MARGIN - this.lucidity$scrollOffset;

        for (ButtonBase button : this.buttons) {
            // 临时修改按钮的 X 坐标
            int originalX = button.getX();
            button.setPosition(xOffset, 26);

            // 只渲染在可见区域内的按钮
            if (xOffset + button.getWidth() > 0 && xOffset < screenWidth) {
                button.render(mouseX, mouseY, button.isMouseOver(), drawContext);
            }

            // 恢复原始坐标（避免影响点击检测）
            button.setPosition(originalX, button.getY());

            xOffset += button.getWidth();
        }

        // 绘制滚动提示箭头
        this.lucidity$drawScrollIndicators(drawContext, screenWidth);

        ci.cancel(); // 取消原始方法执行
    }*/

    /**
     * 拦截鼠标滚轮事件
     */
    /*@Inject(method = "onMouseScrolled", at = @At("HEAD"), cancellable = true, remap = false)
    private void lucidity$handleMouseScroll(int mouseX, int mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        if(!YACL_STYLE.getBooleanValue()) return;
        if (mouseY <= 50) {
            this.lucidity$setScrollOffset(this.lucidity$scrollOffset - (int)(verticalAmount * 15) - (int)(horizontalAmount * 15));
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "initGui", at = @At("TAIL"), remap = false)
    private void lucidity$resetScrollOnInit(CallbackInfo ci) {
        this.lucidity$scrollOffset = 0;
        this.lucidity$maxScrollOffset = 0;
    }
    @Inject(
            method = "onMouseClicked",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void lucidity$onMouseClickedScrollableTabs(
            int mouseX, int mouseY, int mouseButton,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!YACL_STYLE.getBooleanValue()) return;

        // 只在 tab 区域处理
        if (mouseY > 50) return;

        // 把“屏幕坐标”映射回“按钮逻辑坐标”
        int adjustedMouseX = mouseX + this.lucidity$scrollOffset - NAVBAR_MARGIN;

        for (ButtonBase button : this.buttons) {
            if (button.onMouseClicked(adjustedMouseX, mouseY, mouseButton)) {
                cir.setReturnValue(true);
                return;
            }
        }

        cir.setReturnValue(false);
    }*/


    /**
     * 设置滚动偏移量
     */
    @Unique
    private void lucidity$setScrollOffset(int offset) {
        this.lucidity$scrollOffset = Math.max(0, Math.min(offset, this.lucidity$maxScrollOffset));
    }

    /**
     * 绘制滚动指示箭头
     */
    @Unique
    private void lucidity$drawScrollIndicators(GuiGraphics drawContext, int screenWidth) {
        // 右侧箭头
        if (this.lucidity$scrollOffset < this.lucidity$maxScrollOffset - NAVBAR_MARGIN) {
            this.drawString(drawContext, "→", screenWidth - 15, BUTTON_TOP_Y - 6, 0xFFFFFFFF);
        }

        // 左侧箭头
        if (this.lucidity$scrollOffset > NAVBAR_MARGIN) {
            this.drawString(drawContext, "←", 5, BUTTON_TOP_Y - 6, 0xFFFFFFFF);
        }
    }
}
