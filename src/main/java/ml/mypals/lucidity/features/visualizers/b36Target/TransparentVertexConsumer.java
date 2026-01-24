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
}
