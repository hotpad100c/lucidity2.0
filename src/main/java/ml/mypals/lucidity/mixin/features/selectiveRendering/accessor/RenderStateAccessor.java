package ml.mypals.lucidity.mixin.features.selectiveRendering.accessor;

import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderType.CompositeRenderType.class)
public interface RenderStateAccessor {
    @Accessor("state")
    RenderType.CompositeState getState();
}
