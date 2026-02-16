package ml.mypals.lucidity.mixin.features.selectiveRendering.vanilla;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
//? if >=1.21.9 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import ml.mypals.lucidity.features.selectiveRendering.ControllableTransparentBuffersWrapper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.*;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(value = FeatureRenderDispatcher.class)
public class FeatureRendererDispatcher {

    /^ modelFeatureRenderer ^/

    @WrapOperation(
            method = "renderAllFeatures",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer;render(Lnet/minecraft/client/renderer/SubmitNodeCollection;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/OutlineBufferSource;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)V"
            )
    )
    private void wrapModel(
            ModelFeatureRenderer instance, SubmitNodeCollection submitNodeCollection, MultiBufferSource.BufferSource bufferSource, OutlineBufferSource outlineBufferSource, MultiBufferSource.BufferSource bufferSource2, Operation<Void> original
    ) {
       original.call(instance, submitNodeCollection, new ControllableTransparentBuffersWrapper(bufferSource), outlineBufferSource, new ControllableTransparentBuffersWrapper(bufferSource2));
    }

    /^ modelPartFeatureRenderer ^/

    @WrapOperation(
            method = "renderAllFeatures",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/feature/ModelPartFeatureRenderer;render(Lnet/minecraft/client/renderer/SubmitNodeCollection;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/OutlineBufferSource;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)V"
            )
    )
    private void wrapModelPart(
            ModelPartFeatureRenderer instance, SubmitNodeCollection submitNodeCollection, MultiBufferSource.BufferSource bufferSource, OutlineBufferSource outlineBufferSource, MultiBufferSource.BufferSource bufferSource2, Operation<Void> original
    ) {
        original.call(instance, submitNodeCollection, new ControllableTransparentBuffersWrapper(bufferSource), outlineBufferSource, new ControllableTransparentBuffersWrapper(bufferSource2));
    }




    /^ itemFeatureRenderer ^/

    @WrapOperation(
            method = "renderAllFeatures",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/feature/ItemFeatureRenderer;render(Lnet/minecraft/client/renderer/SubmitNodeCollection;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/OutlineBufferSource;)V"
            )
    )
    private void wrapItem(
            ItemFeatureRenderer instance, SubmitNodeCollection submitNodeCollection, MultiBufferSource.BufferSource bufferSource, OutlineBufferSource outlineBufferSource, Operation<Void> original
    ) {
        original.call(instance, submitNodeCollection, new ControllableTransparentBuffersWrapper(bufferSource), outlineBufferSource);
    }

    /^ customFeatureRenderer ^/

    @WrapOperation(
            method = "renderAllFeatures",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/feature/CustomFeatureRenderer;render(Lnet/minecraft/client/renderer/SubmitNodeCollection;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)V"
            )
    )
    private void wrapCustom(
            CustomFeatureRenderer instance,
            SubmitNodeCollection submitNodeCollection,
            MultiBufferSource.BufferSource bufferSource,
            Operation<Void> original
    ) {
        original.call(instance, submitNodeCollection, new ControllableTransparentBuffersWrapper(bufferSource));
    }
}
*///?} else {
@Mixin(Minecraft.class)
public class FeatureRendererDispatcher {
}
//?}
