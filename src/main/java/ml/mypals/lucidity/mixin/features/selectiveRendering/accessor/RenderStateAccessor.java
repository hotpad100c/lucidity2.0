package ml.mypals.lucidity.mixin.features.selectiveRendering.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
//? if >=1.21.11 {
// 1.21.11 起 RenderType.CompositeRenderType 不存在了，配置挪到了 RenderSetup。
// accesswidener 已经把 RenderType.state 放开，直接取字段即可，不再需要 accessor。
import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public interface RenderStateAccessor {
}
//?} else {
/*import net.minecraft.client.renderer.rendertype.*;

@Mixin(RenderType.CompositeRenderType.class)
public interface RenderStateAccessor {
    @Accessor("state")
    RenderType.CompositeState getState();
}
*///?}
