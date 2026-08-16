package ml.mypals.lucidity.utils;

//? if <1.21.9 {
/*public final class DeferredGeometry {
    private DeferredGeometry() {}
}
*///?} else {
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
//? if >=1.21.11 {
import net.minecraft.client.renderer.rendertype.RenderType;
//?} else {
/*import net.minecraft.client.renderer.RenderType;
*///?}

import java.util.function.BiConsumer;

/**
 * 1.21.9 起世界渲染被拆成 extract / submit / draw 三段：
 * extract 收集渲染状态，submit 把节点推进 SubmitNodeCollector，draw 才真正绘制。
 *
 * 1.21.9 之前的写法是在渲染方法里直接往 {@code renderBuffers().bufferSource()} 里写顶点。
 * 那套代码移植到 submit 阶段之后就失效了 —— submit 只负责收集，此时往 buffer source 里
 * 塞进去的几何不参与这一帧的绘制，表现就是"功能完全不显示、也不报错"。
 *
 * 这个工具把即时绘制包装成一个提交节点，让它在 draw 阶段以正确的时机、正确的 buffer 执行。
 */
public final class DeferredGeometry {

    private DeferredGeometry() {}

    /**
     * @param poseStack 提交时的变换（会被快照下来）
     * @param draw      在 draw 阶段回调；给回的 PoseStack 由快照重建，可直接交给 tesselateBlock 之类的 API
     */
    public static void submit(SubmitNodeCollector collector, PoseStack poseStack, RenderType renderType,
                              BiConsumer<PoseStack, VertexConsumer> draw) {
        collector.order(0).submitCustomGeometry(poseStack, renderType, (pose, consumer) -> {
            PoseStack local = new PoseStack();
            local.last().set(pose);
            draw.accept(local, consumer);
        });
    }
}
//?}
