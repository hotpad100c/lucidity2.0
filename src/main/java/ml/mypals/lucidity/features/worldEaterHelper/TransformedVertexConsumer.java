package ml.mypals.lucidity.features.worldEaterHelper;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class TransformedVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;
    private final Matrix4f transformMatrix;
    private float x, y, z;

    public TransformedVertexConsumer(VertexConsumer delegate, Matrix4f transformMatrix) {
        this.delegate = delegate;
        this.transformMatrix = transformMatrix;
    }

    @Override
    public @NotNull VertexConsumer addVertex(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;

        Vector4f pos = new Vector4f(x, y, z, 1.0f);
        pos.mul(transformMatrix);
        return delegate.addVertex(pos.x, pos.y, pos.z);
    }

    @Override
    public @NotNull VertexConsumer setColor(int red, int green, int blue, int alpha) {
        return delegate.setColor(red, green, blue, alpha);
    }

    @Override
    public @NotNull VertexConsumer setUv(float u, float v) {
        return delegate.setUv(u, v);
    }

    @Override
    public @NotNull VertexConsumer setUv1(int u, int v) {
        return delegate.setUv1(u, v);
    }

    @Override
    public @NotNull VertexConsumer setUv2(int u, int v) {
        return delegate.setUv2(u, v);
    }

    @Override
    public @NotNull VertexConsumer setNormal(float x, float y, float z) {
        Vector3f normal = new Vector3f(x, y, z);
        normal.mulPosition(transformMatrix);
        normal.normalize();
        return delegate.setNormal(normal.x, normal.y, normal.z);
    }
}