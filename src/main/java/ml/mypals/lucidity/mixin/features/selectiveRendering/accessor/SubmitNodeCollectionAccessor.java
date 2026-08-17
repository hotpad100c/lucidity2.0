package ml.mypals.lucidity.mixin.features.selectiveRendering.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.SubmitNodeCollection;

@Mixin(SubmitNodeCollection.class)
public interface SubmitNodeCollectionAccessor {
    @Accessor("submitNodeStorage")
    SubmitNodeStorage getSubmitNodeStorage();
}
