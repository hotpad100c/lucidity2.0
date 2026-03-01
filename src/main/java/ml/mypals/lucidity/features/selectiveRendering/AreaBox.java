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
import net.minecraft.resources.ResourceLocation;

import java.awt.*;

import static ml.mypals.lucidity.LucidityModInfo.MOD_ID;
import static ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager.isInsideArea;
import static ml.mypals.lucidity.features.selectiveRendering.SelectiveRenderingManager.wand;

public class AreaBox{
    private static final Color DELETE_COLOR = new Color(1f,0f,0f,0.5f);
    private static final Color DEFAULT_COLOR = new Color(1f,1f,1f,0.2f);
    public BlockPos minPos;
    public BlockPos maxPos;
    public Color color;
    public float alpha = 0.2f;
    public boolean seeThrough = false;
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
    public void setShapeEnabled(boolean b){
        if(boxShape != null) {
            boxShape.enabled = b;
        }
        if(boxFrame != null) {
            boxFrame.enabled = b;
        }
    }
    public void submit(){
        ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID,"area_box_"+minPos.hashCode()+maxPos.hashCode()),this.boxShape);
        ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID,"area_box_frame_"+minPos.hashCode()+maxPos.hashCode()),this.boxFrame);
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
    public String asString(){
        return this.minPos.getX() + "," + this.minPos.getY() + "," + this.minPos.getZ() + ":"
                + this.maxPos.getX() + "," + this.maxPos.getY() + "," + this.maxPos.getZ();
    }
}