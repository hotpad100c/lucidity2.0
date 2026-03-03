package ml.mypals.lucidity.mixin.features.selectiveRendering.fabric;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ml.mypals.lucidity.config.SelectiveRenderingConfigs;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager;

import net.fabricmc.fabric.impl.client.indigo.renderer.render.*;

//? if <=1.21.4 {
/*import com.mojang.blaze3d.vertex.VertexConsumer;
*///?} else {
//?}

//? if = 1.21.5 {
/*import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
*///?}
//? if >1.21.5 {
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

//?}
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@SuppressWarnings("UnstableApiUsage")
//? if <=1.21.4 {
/*@Mixin(value = AbstractBlockRenderContext.class, remap = false)
*///?} else {
@Mixin(value = AbstractTerrainRenderContext.class, remap = false)
//?}
public abstract class AbstractTerrainRenderContextMixin
        //? if >=1.21.4 {
        extends AbstractRenderContext
        //?}
{
    @Final @Shadow(remap = false)
    protected BlockRenderInfo blockInfo;
    //? if <=1.21.5 {
    /*@Shadow protected abstract VertexConsumer getVertexConsumer(RenderType renderType);
    *///?} else {
    @Shadow protected abstract VertexConsumer getVertexConsumer(ChunkSectionLayer par1);
    //?}

    //? if <=1.21.4 {

    /*//? if <=1.21.3 {
    /^@Inject(method = "renderQuad", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/client/indigo/renderer/render/AbstractBlockRenderContext;bufferQuad(Lnet/fabricmc/fabric/impl/client/indigo/renderer/mesh/MutableQuadViewImpl;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"))
    ^///?} else {
    @Inject(method = "bufferQuad", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/client/indigo/renderer/render/AbstractBlockRenderContext;bufferQuad(Lnet/fabricmc/fabric/impl/client/indigo/renderer/mesh/MutableQuadViewImpl;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"))
    //?}
    private void onBufferQuad(MutableQuadViewImpl quad, CallbackInfo ci) {
        if(!SelectiveRenderingManager.shouldRenderBlock(blockInfo.blockState,blockInfo.blockPos) && !SelectiveRenderingConfigs.isBlockFullyHidden()){
            for (int i = 0; i < 4; i++) {
                quad.color(i, rewriteQuadAlpha(quad.color(i), SelectiveRenderingConfigs.HIDDEN_BLOCK_TRANSPARENCY.getIntegerValue()));
            }
        }
    }
    //? if <=1.21.3 {
    /^@WrapOperation(method = "renderQuad", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/client/indigo/renderer/render/AbstractBlockRenderContext;bufferQuad(Lnet/fabricmc/fabric/impl/client/indigo/renderer/mesh/MutableQuadViewImpl;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"))
    ^///?} else {
    @WrapOperation(method = "bufferQuad", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/client/indigo/renderer/render/AbstractBlockRenderContext;bufferQuad(Lnet/fabricmc/fabric/impl/client/indigo/renderer/mesh/MutableQuadViewImpl;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"))
    //?}
    private void onBufferQuad(AbstractBlockRenderContext instance, MutableQuadViewImpl mutableQuadView, VertexConsumer vertexConsumer, Operation<Void> original) {
        if(!SelectiveRenderingManager.shouldRenderBlock(blockInfo.blockState,blockInfo.blockPos) && !SelectiveRenderingConfigs.isBlockFullyHidden()){
            original.call(instance,mutableQuadView,getVertexConsumer(RenderType.translucent()));
        }else{
            original.call(instance,mutableQuadView,vertexConsumer);
        }
    }
    *///?} else {

    @Override
    protected void bufferQuad(MutableQuadViewImpl quad, VertexConsumer vertexConsumer) {
        if (!SelectiveRenderingManager.shouldRenderBlock(blockInfo.blockState, blockInfo.blockPos) && !SelectiveRenderingConfigs.isBlockFullyHidden()) {
            for (int i = 0; i < 4; i++) {
                quad.color(i, rewriteQuadAlpha(quad.color(i), SelectiveRenderingConfigs.HIDDEN_BLOCK_TRANSPARENCY.getIntegerValue()));
            }
            //? if <=1.21.5 {
            /*super.bufferQuad(quad, getVertexConsumer(RenderType.translucent()));
            *///?} else {
            super.bufferQuad(quad, getVertexConsumer(ChunkSectionLayer.TRANSLUCENT));
            //?}
        } else {
            super.bufferQuad(quad, vertexConsumer);
        }
    }

    //?}

    @Unique
    private int rewriteQuadAlpha(int color, int alpha) {
        return ((alpha & 0xFF) << 24) | (color & 0x00FFFFFF);
    }
}
