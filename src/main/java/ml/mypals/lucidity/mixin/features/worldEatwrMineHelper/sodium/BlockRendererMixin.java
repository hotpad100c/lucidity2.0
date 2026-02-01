package ml.mypals.lucidity.mixin.features.worldEatwrMineHelper.sodium;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ml.mypals.lucidity.features.worldEaterHelper.UnculledBlockBakedModel;
import ml.mypals.lucidity.features.worldEaterHelper.WorldEaterHelperManager;
import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadOrientation;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.render.frapi.render.AbstractBlockRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
//? if >=1.21.5 {
/*import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.resources.model.QuadCollection;
*///?} else {
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
//?}
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ml.mypals.lucidity.config.FeatureToggle.WORLD_EATER_MINE_HELPER;
import static ml.mypals.lucidity.config.LucidityConfigs.Generic.WORLD_EATER_MINE_HELPER_HEIGHT;

@Mixin(value = BlockRenderer.class,remap = false)
public abstract class BlockRendererMixin extends AbstractBlockRenderContext {

    @Shadow @Final private ChunkVertexEncoder.Vertex[] vertices;
    @Unique
    private static final ThreadLocal<Boolean> isRenderingTransformed = ThreadLocal.withInitial(() -> false);
    @Unique
    private static final ThreadLocal<Matrix4f> transformMatrix = new ThreadLocal<>();

    @Inject(method = "processQuad", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;bufferQuad(Lnet/caffeinemc/mods/sodium/client/render/frapi/mesh/MutableQuadViewImpl;[FLnet/caffeinemc/mods/sodium/client/render/chunk/terrain/material/Material;)V"))
    private void redirectBrightnessAndLight(MutableQuadViewImpl quad, CallbackInfo ci) {
        if (isRenderingTransformed.get()) {
            for (int i = 0; i < 4; i++) {
                this.quadLightData.br[i] = 1.0f;
            }
            for (int i = 0; i < 4; i++) {
                this.quadLightData.lm[i] = LightTexture.FULL_BRIGHT;
            }
        }
    }

    @Inject(method = "bufferQuad",
    at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/frapi/mesh/MutableQuadViewImpl;sprite(Lnet/fabricmc/fabric/api/renderer/v1/model/SpriteFinder;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"))
    private void beforeSprite(MutableQuadViewImpl quad, float[] brightnesses, Material material, CallbackInfo ci) {
        if (isRenderingTransformed.get()) {
            ChunkVertexEncoder.Vertex[] vertices = this.vertices;
            for (int dstIndex = 0; dstIndex < 4; ++dstIndex) {
                ChunkVertexEncoder.Vertex out = vertices[dstIndex];
                out.light = LightTexture.FULL_BRIGHT;
            }
        }
    }
        @WrapOperation(
            method = "bufferQuad",
            at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/vertex/builder/ChunkMeshBufferBuilder;push([Lnet/caffeinemc/mods/sodium/client/render/chunk/vertex/format/ChunkVertexEncoder$Vertex;I)V")
    )
    private void transformVertices(ChunkMeshBufferBuilder instance, ChunkVertexEncoder.Vertex[] vertices, int materialBits, Operation<Void> original) {
        if (isRenderingTransformed.get()) {
            Matrix4f matrix = transformMatrix.get();
            if (matrix != null) {
                Vector4f pos = new Vector4f();
                for (ChunkVertexEncoder.Vertex vertex : vertices) {
                    pos.set(vertex.x, vertex.y, vertex.z, 1.0f);
                    pos.mul(matrix);
                    vertex.x = pos.x();
                    vertex.y = pos.y();
                    vertex.z = pos.z();
                }
            }
        }
        original.call(instance, vertices, materialBits);
    }

    @WrapMethod(method = "renderModel")
    //? if >=1.21.5 {
    /*private void onRenderModel(BlockStateModel model, BlockState state, BlockPos pos, BlockPos origin, Operation<Void> original) {

        original.call(model, state, pos, origin);

    *///?} else {
    private void onRenderModel(BakedModel model, BlockState state, BlockPos pos, BlockPos origin, Operation<Void> original) {

        original.call(model, state, pos, origin);
    //?}
        if (!isRenderingTransformed.get() &&
                WORLD_EATER_MINE_HELPER.getBooleanValue() &&
                WorldEaterHelperManager.shouldRender(state, pos)) {

            isRenderingTransformed.set(true);
            try {
                PoseStack poseStack = new PoseStack();
                float relX = (float) (pos.getX() & 15);
                float relY = (float) (pos.getY() & 15);
                float relZ = (float) (pos.getZ() & 15);

                poseStack.translate(relX + 0.5f, relY + 0.5f + WORLD_EATER_MINE_HELPER_HEIGHT.getFloatValue(), relZ + 0.5f);


                Quaternionf rotation = new Quaternionf().rotationTo(
                        new Vector3f(1, 1, 1).normalize(),
                        new Vector3f(0, 1, 0)
                );
                poseStack.mulPose(rotation);
                poseStack.mulPose(Axis.YP.rotationDegrees(45f));

                poseStack.translate(-(relX + 0.5f), -(relY + 0.5f), -(relZ + 0.5f));

                transformMatrix.set(poseStack.last().pose());

                Minecraft mc = Minecraft.getInstance();
                BlockRenderDispatcher dispatcher = mc.getBlockRenderer();

                UnculledBlockBakedModel unculledBlockBakedModel = new UnculledBlockBakedModel(dispatcher.getBlockModel(state),state,random);

                original.call(unculledBlockBakedModel, state, pos, origin);
            } finally {
                isRenderingTransformed.set(false);
                transformMatrix.remove();
            }
        }
    }
}