package ml.mypals.lucidity.features.selectiveRendering;

//? if <1.21.9 {
/*public class SelectiveRenderingRenderTypeWrapper{

}
*///?} else {
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

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
//?}