package ml.mypals.lucidity.features.selectiveRendering;

import com.mojang.blaze3d.vertex.VertexConsumer;

import org.jetbrains.annotations.NotNull;

import static ml.mypals.lucidity.config.SelectiveRenderingConfigs.HIDDEN_BLOCK_TRANSPARENCY;

public class ControllableTransparentVertexConsumer implements VertexConsumer {
    private final VertexConsumer base;

    public ControllableTransparentVertexConsumer(VertexConsumer base) {
        this.base = base;
    }

    @Override
    public @NotNull VertexConsumer setColor(int i, int j, int k, int l) {
        base.setColor(i,j,k, HIDDEN_BLOCK_TRANSPARENCY.getIntegerValue());
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
}
