package ml.mypals.lucidity.mixin.features.selectiveRendering.vanilla;

//? if <1.21.11 {
/*import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Minecraft.class)
public class RenderTypeDrawMixin {
}
*///?} else {
import ml.mypals.lucidity.config.SelectiveRenderingConfigs;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingRenderTypes;
import net.minecraft.client.renderer.rendertype.*;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * RenderType.draw 每绘制一批就会往 DynamicUniforms 写一次 transform，其中
 * colorModulator 是写死的 (1,1,1,1)。隐藏几何有自己独立的 RenderType 实例
 * （见 SelectiveRenderingRenderTypes），所以在这里把那一批的 alpha 换掉，
 * 就能在<b>不碰任何顶点数据</b>的前提下调整透明度 —— 改透明度不再需要重建网格。
 */
@Mixin(RenderType.class)
public class RenderTypeDrawMixin {

    @ModifyArg(
            method = "draw(Lcom/mojang/blaze3d/vertex/MeshData;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/DynamicUniforms;writeTransform(Lorg/joml/Matrix4fc;Lorg/joml/Vector4fc;Lorg/joml/Vector3fc;Lorg/joml/Matrix4fc;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"
            ),
            index = 1
    )
    private Vector4fc lucidity$modulateHiddenAlpha(Vector4fc colorModulator) {
        RenderType self = (RenderType) (Object) this;
        if (!SelectiveRenderingRenderTypes.isHiddenVariant(self)) {
            return colorModulator;
        }
        float alpha = SelectiveRenderingConfigs.HIDDEN_BLOCK_TRANSPARENCY.getIntegerValue() / 255.0F;
        return new Vector4f(colorModulator.x(), colorModulator.y(), colorModulator.z(), colorModulator.w() * alpha);
    }
}
//?}
