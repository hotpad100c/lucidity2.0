package ml.mypals.lucidity.utils;
//? if >=1.21.5 {
/*import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.opengl.GlStateManager;
*///?} else {
import com.mojang.blaze3d.platform.GlStateManager;
//? if >=1.21.3 {
/*import net.minecraft.client.renderer.CoreShaders;
*///?}
import com.mojang.blaze3d.systems.RenderSystem;
//?}
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.AABB;

public class LucidityRenderUtils {
    public static void renderBox(PoseStack poseStack,VertexConsumer vertexConsumer, AABB aABB, float f, float g, float h, float i) {
        renderBox(poseStack, vertexConsumer, aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ, f, g, h, i, f, g, h);
    }
    public static void renderBox(PoseStack poseStack, AABB aABB, float f, float g, float h, float i) {

        //? if >= 1.21.6 {

        /*RenderType renderType = RenderType.translucentMovingBlock();
        *///?} else if >=1.21.5 {
        /*RenderType renderType = RenderType.translucent();
        *///?} else if >=1.21.3 {
        /*RenderSystem.setShader(CoreShaders.POSITION_COLOR);
        *///?} else {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        //?}
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder vertexConsumer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        renderBox(poseStack, vertexConsumer, aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ, f, g, h, i, f, g, h);
        try {
            MeshData builtBuffer = vertexConsumer.buildOrThrow();
            GlStateManager._enableBlend();
            GlStateManager._enablePolygonOffset();
            GlStateManager._polygonOffset(1,1);

            //? if >=1.21.5 {
            /*renderType.draw(builtBuffer);
            *///?} else {
            BufferUploader.drawWithShader(builtBuffer);
            //?}
            GlStateManager._disableBlend();
            builtBuffer.close();
        }catch (Exception ignored) {
        }
    }
    public static void renderBox(PoseStack poseStack,VertexConsumer vertexConsumer, double d, double e, double f, double g, double h, double i, float j, float k, float l, float m, float n, float o, float p) {

        PoseStack.Pose pose = poseStack.last();
        float minX = (float)d+0.001f;
        float minY = (float)e+0.001f;
        float minZ = (float)f+0.001f;
        float maxX = (float)g-0.001f;
        float maxY = (float)h-0.001f;
        float maxZ = (float)i-0.001f;

        vertexConsumer.addVertex(pose, minX, minY, minZ).setColor(n, o, p, m);
        vertexConsumer.addVertex(pose, maxX, minY, minZ).setColor(n, o, p, m);
        vertexConsumer.addVertex(pose, maxX, minY, maxZ).setColor(n, o, p, m);
        vertexConsumer.addVertex(pose, minX, minY, maxZ).setColor(n, o, p, m);

        vertexConsumer.addVertex(pose, minX, maxY, minZ).setColor(j, k, l, m);
        vertexConsumer.addVertex(pose, minX, maxY, maxZ).setColor(j, k, l, m);
        vertexConsumer.addVertex(pose, maxX, maxY, maxZ).setColor(j, k, l, m);
        vertexConsumer.addVertex(pose, maxX, maxY, minZ).setColor(j, k, l, m);

        vertexConsumer.addVertex(pose, minX, minY, minZ).setColor(n, k, p, m);
        vertexConsumer.addVertex(pose, minX, maxY, minZ).setColor(n, k, p, m);
        vertexConsumer.addVertex(pose, maxX, maxY, minZ).setColor(n, k, p, m);
        vertexConsumer.addVertex(pose, maxX, minY, minZ).setColor(n, k, p, m);

        vertexConsumer.addVertex(pose, minX, minY, maxZ).setColor(j, o, l, m);
        vertexConsumer.addVertex(pose, maxX, minY, maxZ).setColor(j, o, l, m);
        vertexConsumer.addVertex(pose, maxX, maxY, maxZ).setColor(j, o, l, m);
        vertexConsumer.addVertex(pose, minX, maxY, maxZ).setColor(j, o, l, m);

        vertexConsumer.addVertex(pose, minX, minY, minZ).setColor(n, o, l, m);
        vertexConsumer.addVertex(pose, minX, minY, maxZ).setColor(n, o, l, m);
        vertexConsumer.addVertex(pose, minX, maxY, maxZ).setColor(n, o, l, m);
        vertexConsumer.addVertex(pose, minX, maxY, minZ).setColor(n, o, l, m);

        vertexConsumer.addVertex(pose, maxX, minY, minZ).setColor(j, o, p, m);
        vertexConsumer.addVertex(pose, maxX, maxY, minZ).setColor(j, o, p, m);
        vertexConsumer.addVertex(pose, maxX, maxY, maxZ).setColor(j, o, p, m);
        vertexConsumer.addVertex(pose, maxX, minY, maxZ).setColor(j, o, p, m);

    }
}
