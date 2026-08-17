package ml.mypals.lucidity.features.worldEaterHelper;

import org.joml.Matrix4f;
import org.jetbrains.annotations.Nullable;

/**
 * 世界吞噬者辅助线在 sodium 下渲染"增生模型"时的线程局部状态。
 *
 * 区块构建是多线程的，所以必须用 ThreadLocal。
 * 之前这两个字段私有在 BlockRendererMixin 里，但 sodium 的面剔除发生在
 * AbstractBlockRenderContext.renderQuad 里（renderQuad -> isFaceCulled -> shouldDrawSide），
 * 那是另一个类，需要同一份标记才能在渲染增生模型时关掉剔除。
 */
public final class WorldEaterTransformState {

    private static final ThreadLocal<Boolean> RENDERING_TRANSFORMED = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Matrix4f> TRANSFORM = new ThreadLocal<>();

    private WorldEaterTransformState() {}

    public static boolean isRenderingTransformed() {
        return RENDERING_TRANSFORMED.get();
    }

    public static void begin(Matrix4f matrix) {
        RENDERING_TRANSFORMED.set(true);
        TRANSFORM.set(matrix);
    }

    public static void end() {
        RENDERING_TRANSFORMED.set(false);
        TRANSFORM.remove();
    }

    @Nullable
    public static Matrix4f transform() {
        return TRANSFORM.get();
    }
}
