package ml.mypals.lucidity.mixin.features.visualizers.containerOutputPreview;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import static ml.mypals.lucidity.config.FeatureToggle.CONTAINER_SIGNAL_PREVIEW;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin<T extends AbstractContainerMenu> extends Screen implements MenuAccess<T> {

    @Shadow
    @Final
    protected T menu;


    @Shadow @Final protected Component playerInventoryTitle;

    @Shadow protected int inventoryLabelX;

    @Shadow protected int inventoryLabelY;

    @Shadow protected int titleLabelX;

    @Shadow protected int titleLabelY;

    protected HandledScreenMixin(Component title) {
        super(title);
    }

    @Unique
    private void renderComparatorOutput(GuiGraphics context, int mouseX, int mouseY){
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        if (this.menu instanceof ChestMenu ||
                this.menu instanceof FurnaceMenu ||
                this.menu instanceof HopperMenu ||
                this.menu instanceof DispenserMenu ||
                this.menu instanceof BrewingStandMenu ||
                this.menu instanceof ShulkerBoxMenu ||
                this.menu instanceof BlastFurnaceMenu ||
                this.menu instanceof SmokerMenu) {

            AbstractContainerMenu screenHandler = this.menu;

            float totalFullness = 0.0F;
            int totalSlots = 0;

            for (Slot slot : screenHandler.slots) {
                if (slot.container instanceof Inventory) {
                    continue;
                }
                totalSlots++;
                ItemStack itemStack = slot.getItem();
                if (!itemStack.isEmpty()) {

                    totalFullness += (float) itemStack.getCount() / (float) itemStack.getMaxStackSize();
                }
            }


            float averageFullness = totalFullness > 0 ? totalFullness / totalSlots : 0.0F;
            int comparatorOutput = totalFullness > 0 ? (int) Math.floor(1 + averageFullness * 14) : 0;

            Component outputText = Component.literal(this.title.getString()).append(Component.literal("(C: " + comparatorOutput + ")").withStyle(ChatFormatting.GOLD));
            context.drawString(this.font, outputText, this.titleLabelX, this.titleLabelY, 4210752, false);
        } else if(this.menu instanceof CrafterMenu crafterScreenHandler){
            int affectSlots = 0;
            for (Slot slot : crafterScreenHandler.slots) {
                if (slot.container instanceof Inventory) {
                    continue;
                }
                if(crafterScreenHandler.isSlotDisabled(slot.index) || !slot.getItem().isEmpty()) {
                    affectSlots++;
                }
            }
            Component outputText = Component.literal(this.title.getString()).append(Component.literal("(C: " + affectSlots + ")").withStyle(ChatFormatting.GOLD));
            context.drawString(this.font, outputText, this.titleLabelX, this.titleLabelY, 4210752, false);

        }else {
            context.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        }
        context.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
    }

    @WrapMethod(method = "renderLabels")
    private void drawForeground(GuiGraphics guiGraphics, int i, int j, Operation<Void> original) {
        if (!CONTAINER_SIGNAL_PREVIEW.getBooleanValue()) {
            original.call(guiGraphics, i, j);
        }else {
            renderComparatorOutput(guiGraphics, i, j);
        }
    }

}
