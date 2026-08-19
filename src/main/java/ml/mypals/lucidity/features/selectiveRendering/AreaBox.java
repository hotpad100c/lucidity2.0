package ml.mypals.lucidity.features.selectiveRendering;



import ml.mypals.ryansrenderingkit.builders.shapeBuilders.ShapeGenerator;
import ml.mypals.lucidity.hotkeys.HotkeyCallbacks;
import ml.mypals.ryansrenderingkit.shape.Shape;
import ml.mypals.ryansrenderingkit.shape.basics.BoxLikeShape;
import ml.mypals.ryansrenderingkit.shape.box.BoxFaceShape;
import ml.mypals.ryansrenderingkit.shape.box.BoxWireframeShape;
import ml.mypals.ryansrenderingkit.shape.box.WireframedBoxShape;
import ml.mypals.ryansrenderingkit.shapeManagers.ShapeManagers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

import java.awt.*;

import static ml.mypals.lucidity.LucidityModInfo.MOD_ID;
import static ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager.isInsideArea;
import static ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager.wand;

public class AreaBox{
    private static final Color DELETE_COLOR = new Color(1f,0f,0f,0.5f);
    private static final Color DEFAULT_COLOR = new Color(1f,1f,1f,0.2f);
    /** {@link #hiddenTransparency} 取这个值时该选区不覆盖全局透明度。 */
    public static final int FOLLOW_GLOBAL = -1;
    public BlockPos minPos;
    public BlockPos maxPos;
    public Color color;
    public float alpha = 0.2f;
    public boolean seeThrough = false;
    /**
     * 该选区自己的隐藏透明度（0..255），{@link #FOLLOW_GLOBAL} 表示跟随
     * {@code HIDDEN_BLOCK_TRANSPARENCY}。注意这跟上面的 {@link #alpha} 无关，
     * 后者是选区可视化边框自身的不透明度。
     */
    private int hiddenTransparency = FOLLOW_GLOBAL;
    public BoxWireframeShape boxFrame = null;
    public BoxFaceShape boxShape = null;
    public AreaBox(BlockPos a, BlockPos b,Color color,boolean seeThrough){
        this(a,b,color,0.2f,seeThrough);
    }
    public AreaBox(BlockPos a, BlockPos b,Color color,float alpha, boolean seeThrough){
        this.minPos = new BlockPos(
                Math.min(a.getX(), b.getX()),
                Math.min(a.getY(), b.getY()),
                Math.min(a.getZ(), b.getZ())
        );
        this.maxPos = new BlockPos(
                Math.max(a.getX(), b.getX()),
                Math.max(a.getY(), b.getY()),
                Math.max(a.getZ(), b.getZ())
        );
        this.color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 255));
        this.alpha = alpha;
        this.seeThrough = seeThrough;
        this.boxShape = ShapeGenerator.generateBoxFace().
                aabb(minPos.getCenter().subtract(0.5,0.5,0.5), maxPos.getCenter().add(0.5,0.5,0.5))
                .color(color)
                .transform(this::updateBox)
                .seeThrough(true)
                .build(Shape.RenderingType.BATCH);
        this.boxFrame = ShapeGenerator.generateBoxWireframe().
                aabb(minPos.getCenter().subtract(0.5,0.5,0.5), maxPos.getCenter().add(0.5,0.5,0.5))
                .color(new Color(color.getRed(),color.getGreen(),color.getBlue(),255))
                .edgeWidth(3f)
                .transform(this::updateBox)
                .seeThrough(true)
                .build(Shape.RenderingType.BATCH);
    }
    public void updateBox(BoxLikeShape.BoxTransformer boxTransformer){

        Shape shape = (Shape) boxTransformer.getShape();


        assert Minecraft.getInstance().player != null;
        boolean holdingWand = Minecraft.getInstance().player.getMainHandItem().is(wand);
        if (!holdingWand) shape.disable();

        if(shape.getCustomData("color",null) == null){
            shape.putCustomData("color", this.color);
        }
        if(WandActionsManager.pointingPos != null && isInsideArea(WandActionsManager.pointingPos.getCenter(),this,false)){
            shape.setBaseColor(HotkeyCallbacks.deleteArea.isDown()?DELETE_COLOR:DEFAULT_COLOR);
        }else{
            shape.setBaseColor(shape.getCustomData("color",DEFAULT_COLOR));
        }
    }
    /**
     * 构造期/解析期赋值：不走 {@link #setOwnTransparency} 的重建调度，
     * 因为调用方（选区列表解析、选区切割）随后会统一安排一次重建。
     */
    public AreaBox withOwnTransparency(int value){
        this.hiddenTransparency = value < 0 ? FOLLOW_GLOBAL : Math.min(value, 255);
        return this;
    }

    /** 这个选区是否有自己的透明度，而不是跟随全局。 */
    public boolean hasOwnTransparency(){
        return this.hiddenTransparency >= 0;
    }

    /** 自己的透明度，没有覆盖时返回 {@link #FOLLOW_GLOBAL}。 */
    public int getOwnTransparency(){
        return this.hiddenTransparency;
    }

    /**
     * 设置这个选区自己的透明度，传入负数表示改回跟随全局。
     *
     * <p>只改内存里的选区对象，不写 SELECTED_AREAS —— 写配置会触发整份选区列表的
     * 销毁重建，Flashback 逐帧应用关键帧时承受不起。用魔杖/配置界面改选区时本来就
     * 是先改字符串再解析回来，那条路径不经过这里。
     *
     * @return 值真的变了返回 true
     */
    public boolean setOwnTransparency(int value){
        int normalized = value < 0 ? FOLLOW_GLOBAL : Math.min(value, 255);
        if (normalized == this.hiddenTransparency) {
            return false;
        }
        this.hiddenTransparency = normalized;
        SelectiveRenderingManager.onAreaTransparencyChanged(this);
        return true;
    }

    public void setShapeEnabled(boolean b){
        if(boxShape != null) {
            boxShape.enabled = b;
        }
        if(boxFrame != null) {
            boxFrame.enabled = b;
        }
    }
    public void submit(){
        ShapeManagers.addShape(Identifier.fromNamespaceAndPath(MOD_ID,"area_box_"+minPos.hashCode()+maxPos.hashCode()),this.boxShape);
        ShapeManagers.addShape(Identifier.fromNamespaceAndPath(MOD_ID,"area_box_frame_"+minPos.hashCode()+maxPos.hashCode()),this.boxFrame);
    }
    public void destroy() {
        if (boxShape != null) {
            boxShape.discard();
            boxShape = null;
        }
        if (boxFrame != null) {
            boxFrame.discard();
            boxFrame = null;
        }
    }
    /**
     * 选区的名字，就是它的对角线：{@code x1,y1,z1:x2,y2,z2}。
     *
     * <p>用对角而不是列表下标当标识，是为了让 Flashback 关键帧能稳定地点名某个选区 ——
     * 下标会随着增删选区整体错位，对角只要选区本身不动就不会变。
     */
    public String getKey(){
        return this.minPos.getX() + "," + this.minPos.getY() + "," + this.minPos.getZ() + ":"
                + this.maxPos.getX() + "," + this.maxPos.getY() + "," + this.maxPos.getZ();
    }

    /**
     * 序列化格式：{@code x1,y1,z1:x2,y2,z2}，选区有自己的透明度时在末尾追加
     * {@code :alpha}。旧配置只有前两段，解析时按"跟随全局"处理。
     */
    public String asString(){
        String box = getKey();
        return this.hiddenTransparency >= 0 ? box + ":" + this.hiddenTransparency : box;
    }
}