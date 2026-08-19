package ml.mypals.lucidity.mixin.features.selectiveRendering.fabric;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager;

import net.fabricmc.fabric.impl.client.indigo.renderer.render.*;


import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

import net.minecraft.client.renderer.rendertype.*;
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
@Mixin(value = AbstractTerrainRenderContext.class, remap = false)
public abstract class AbstractTerrainRenderContextMixin
        extends AbstractRenderContext
{
    @Final @Shadow(remap = false)
    protected BlockRenderInfo blockInfo;
    @Shadow protected abstract VertexConsumer getVertexConsumer(ChunkSectionLayer par1);


    @Override
    protected void bufferQuad(MutableQuadViewImpl quad, VertexConsumer vertexConsumer) {
        if (!SelectiveRenderingManager.shouldRenderBlock(blockInfo.blockState, blockInfo.blockPos)) {
            int alpha = SelectiveRenderingManager.hiddenTransparencyAt(blockInfo.blockPos);
            for (int i = 0; i < 4; i++) {
                quad.color(i, rewriteQuadAlpha(quad.color(i), alpha));
            }
            super.bufferQuad(quad, getVertexConsumer(ChunkSectionLayer.TRANSLUCENT));
        } else {
            super.bufferQuad(quad, vertexConsumer);
        }
    }


    @Unique
    private int rewriteQuadAlpha(int color, int alpha) {
        return ((alpha & 0xFF) << 24) | (color & 0x00FFFFFF);
    }
}
