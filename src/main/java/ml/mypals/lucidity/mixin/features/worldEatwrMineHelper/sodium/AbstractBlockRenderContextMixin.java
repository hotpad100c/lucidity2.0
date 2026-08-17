package ml.mypals.lucidity.mixin.features.worldEatwrMineHelper.sodium;

//? if <1.21.11 {
/*import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Minecraft.class)
public class AbstractBlockRenderContextMixin {
}
*///?} else {
import ml.mypals.lucidity.features.worldEaterHelper.WorldEaterTransformState;
import net.caffeinemc.mods.sodium.client.render.model.AbstractBlockRenderContext;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * sodium 的面剔除发生在 AbstractBlockRenderContext.renderQuad 里，链路是
 * renderQuad -> isFaceCulled(Direction) -> shouldDrawSide(Direction)，
 * 判定依据是<b>原方块位置</b>的邻居。
 *
 * 这一步和模型无关：UnculledBlockBakedModel 传给 emitQuads 的 cullTest 只能决定
 * 模型"发出哪些面"，发出来之后 sodium 还会再剔一遍。所以被方块包围的原方块一旦
 * 六面全剔，悬浮的增生模型也跟着一起消失 —— 这就是 sodium 下增生模型看不见的原因。
 *
 * 渲染增生模型期间直接让剔除判定返回 false。原方块的正常渲染不受影响，
 * 因为那时候标记是关的。
 */
@Mixin(value = AbstractBlockRenderContext.class, remap = false)
public class AbstractBlockRenderContextMixin {

    @Inject(method = "isFaceCulled", at = @At("HEAD"), cancellable = true, remap = false)
    private void lucidity$dontCullExtrudedModel(Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (WorldEaterTransformState.isRenderingTransformed()) {
            cir.setReturnValue(false);
        }
    }
}
//?}
