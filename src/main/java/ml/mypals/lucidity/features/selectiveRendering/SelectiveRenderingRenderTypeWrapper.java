package ml.mypals.lucidity.features.selectiveRendering;

// 1.21.11 起 RenderType 是私有构造的具体类，没法再继承出一个包装类来当标记。
// 取而代之的是 SelectiveRenderingRenderTypes：给隐藏几何铸造独立的 RenderType 实例，
// 透明度在绘制时由 RenderTypeDrawMixin 通过 uniform 施加。
public class SelectiveRenderingRenderTypeWrapper {
}
