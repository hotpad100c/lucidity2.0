package ml.mypals.lucidity.features.visualizers.b36Target;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.NotNull;

public class TransparentVertexConsumer implements VertexConsumer {
    private final VertexConsumer base;
    public TransparentVertexConsumer(VertexConsumer consumer){
        this.base = consumer;
    }
    @Override
    public @NotNull VertexConsumer addVertex(float f, float g, float h) {
        return base.addVertex(f,g,h);
    }

    @Override
    public @NotNull VertexConsumer setColor(int i, int j, int k, int l) {
        return base.setColor(i,j,k,100);
    }

    @Override
    public @NotNull VertexConsumer setUv(float f, float g) {
        return base.setUv(f,g);
    }

    @Override
    public @NotNull VertexConsumer setUv1(int i, int j) {
        return base.setUv1(i,j);
    }

    @Override
    public @NotNull VertexConsumer setUv2(int i, int j) {
        return base.setUv2(i,j);
    }

    @Override
    public @NotNull VertexConsumer setNormal(float f, float g, float h) {
        return base.setNormal(f,g,h);
    }

    // 1.21.11 把 setLineWidth 和 setColor(int) 提成了抽象方法
    @Override
    public @NotNull VertexConsumer setLineWidth(float width) {
        return base.setLineWidth(width);
    }

    @Override
    public @NotNull VertexConsumer setColor(int argb) {
        // 走本类的四参重载，好让固定的半透明 alpha 依旧生效
        return this.setColor((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, (argb >>> 24) & 0xFF);
    }
}
