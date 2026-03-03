package ml.mypals.lucidity.mixin.features.selectiveRendering.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
//? if >=1.21.9 {
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.SubmitNodeCollection;

@Mixin(SubmitNodeCollection.class)
public interface SubmitNodeCollectionAccessor {
    @Accessor("submitNodeStorage")
    SubmitNodeStorage getSubmitNodeStorage();
}
//?} else {

/*import net.minecraft.client.Minecraft;
@Mixin(Minecraft.class)
public interface SubmitNodeCollectionAccessor {

}

*///?}
