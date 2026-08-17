package ml.mypals.lucidity.mixin.features.selectiveRendering.sodium;

import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(value = SodiumWorldRenderer.class,remap = false)
public class SodiumWorldRendererMixin {
}
