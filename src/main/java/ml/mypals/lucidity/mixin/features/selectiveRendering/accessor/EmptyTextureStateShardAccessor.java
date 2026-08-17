package ml.mypals.lucidity.mixin.features.selectiveRendering.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
// 1.21.11 起 RenderStateShard 整个不存在了；纹理 Identifier 现在从
// RenderSetup.textures 的 TextureBinding.location() 取，见 SelectiveRenderingRenderTypes。
import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public interface EmptyTextureStateShardAccessor {
}
