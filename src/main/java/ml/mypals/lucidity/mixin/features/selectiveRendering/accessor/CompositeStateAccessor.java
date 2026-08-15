package ml.mypals.lucidity.mixin.features.selectiveRendering.accessor;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.rendertype.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderType.CompositeState.class)
public interface CompositeStateAccessor {
    @Accessor("textureState")
    RenderStateShard.EmptyTextureStateShard getTextureState();
}
