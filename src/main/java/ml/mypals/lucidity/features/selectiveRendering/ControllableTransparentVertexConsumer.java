package ml.mypals.lucidity.features.selectiveRendering;

import com.mojang.blaze3d.vertex.VertexConsumer;
import ml.mypals.lucidity.features.visualizers.b36Target.TransparentVertexConsumer;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.NotNull;

import static ml.mypals.lucidity.config.SelectiveRenderingConfigs.HIDDEN_BLOCK_TRANSPARENCY;

public class ControllableTransparentVertexConsumer implements VertexConsumer {
    private final VertexConsumer base;

    public ControllableTransparentVertexConsumer(VertexConsumer base) {
        this.base = base;
    }

    @Override
    public @NotNull VertexConsumer setColor(int i, int j, int k, int l) {
        return base.setColor(i,j,k, HIDDEN_BLOCK_TRANSPARENCY.getIntegerValue());
    }
    @Override
    public @NotNull VertexConsumer addVertex(float f, float g, float h) {
        base.addVertex(f,g,h);
        return this.setColor(255,255,255,255);
    }
    @Override
    public @NotNull VertexConsumer setColor(int i) {
        return this.setColor(ARGB.red(i), ARGB.green(i), ARGB.blue(i), 0);
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
