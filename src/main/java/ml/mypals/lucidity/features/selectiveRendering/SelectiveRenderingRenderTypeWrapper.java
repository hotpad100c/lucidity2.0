package ml.mypals.lucidity.features.selectiveRendering;

//? if <1.21.9 {
/*public class SelectiveRenderingRenderTypeWrapper{

}
*///?} else if >=1.21.11 {
// 1.21.11 起 RenderType 是私有构造的具体类，没法再继承出一个包装类来当标记。
// 取而代之的是 SelectiveRenderingRenderTypes：给隐藏几何铸造独立的 RenderType 实例，
// 透明度在绘制时由 RenderTypeDrawMixin 通过 uniform 施加。
public class SelectiveRenderingRenderTypeWrapper {
}
//?} else {
/*import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.rendertype.*;

public class SelectiveRenderingRenderTypeWrapper extends RenderType {
    public RenderType base;
    public SelectiveRenderingRenderTypeWrapper(RenderType renderType) {
        super(renderType.getName(), renderType.bufferSize(), renderType.affectsCrumbling, renderType.affectsCrumbling(), renderType.setupState, renderType.clearState);
        base = renderType;
    }

    @Override
    public void draw(MeshData meshData) {
        base.draw(meshData);
    }

    @Override
    public VertexFormat format() {
        return base.format();
    }

    @Override
    public VertexFormat.Mode mode() {
        return base.mode();
    }

    @Override
    public RenderPipeline pipeline() {
        return base.pipeline();
    }
}
*///?}
