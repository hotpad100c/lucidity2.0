package ml.mypals.lucidity.features.selectiveRendering;

import com.mojang.blaze3d.vertex.VertexConsumer;

import org.jetbrains.annotations.NotNull;

public class ControllableTransparentVertexConsumer implements VertexConsumer {
    private final VertexConsumer base;
    /**
     * 这一批几何要用的隐藏透明度。由调用方按位置解析好传进来 —— 现在透明度可以逐选区
     * 不同，消费者自己无从知道正在写的是哪个方块。
     */
    private final int alpha;

    public ControllableTransparentVertexConsumer(VertexConsumer base, int alpha) {
        this.base = base;
        this.alpha = alpha;
    }

    @Override
    public @NotNull VertexConsumer setColor(int i, int j, int k, int l) {
        base.setColor(i,j,k, this.alpha);
        return this;
    }
    @Override
    public @NotNull VertexConsumer addVertex(float x, float y, float z) {
        base.addVertex(x,y,z);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setUv(float f, float g) {
        base.setUv(f,g);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setUv1(int i, int j) {
        base.setUv1(i,j);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setUv2(int i, int j) {
        base.setUv2(i,j);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setNormal(float f, float g, float h) {
        base.setNormal(f,g,h);
        return this;
    }

    // 1.21.11 把 setLineWidth 和 setColor(int) 提成了抽象方法
    @Override
    public @NotNull VertexConsumer setLineWidth(float width) {
        base.setLineWidth(width);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setColor(int argb) {
        // 走本类的四参重载，透明度照样会被本批次的 alpha 覆盖
        return this.setColor((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, (argb >>> 24) & 0xFF);
    }
}
