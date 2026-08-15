package ml.mypals.lucidity.mixin.features.selectiveRendering.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
//? if >=1.21.11 {
// 1.21.11 起 CompositeState/RenderStateShard 都不存在了，纹理挂在 RenderSetup.textures 上，
// 由 accesswidener 放开，见 SelectiveRenderingRenderTypes.textureOf。
import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public interface CompositeStateAccessor {
}
//?} else {
/*import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.rendertype.*;

@Mixin(RenderType.CompositeState.class)
public interface CompositeStateAccessor {
    @Accessor("textureState")
    RenderStateShard.EmptyTextureStateShard getTextureState();
}
*///?}
