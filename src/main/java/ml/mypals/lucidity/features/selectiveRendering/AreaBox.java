package ml.mypals.lucidity.features.selectiveRendering;



import ml.mypals.ryansrenderingkit.builders.shapeBuilders.ShapeGenerator;
import ml.mypals.lucidity.hotkeys.HotkeyCallbacks;
import ml.mypals.ryansrenderingkit.shape.Shape;
import ml.mypals.ryansrenderingkit.shape.box.BoxFaceShape;
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
                .transform(boxTransformer -> {
                    Shape shape = (Shape) boxTransformer.getShape();
                    shape.enabled = Minecraft.getInstance().player.getMainHandItem().is(wand);
                    if(shape.getCustomData("color",null) == null){
                        shape.putCustomData("color", this.color);
                    }
                    if(WandActionsManager.pointingPos != null && isInsideArea(WandActionsManager.pointingPos.getCenter(),this,false)){
                        shape.setBaseColor(HotkeyCallbacks.deleteArea.isDown()?DELETE_COLOR:DEFAULT_COLOR);
                    }else{
                        shape.setBaseColor(shape.getCustomData("color",DEFAULT_COLOR));
                    }
                })
                .seeThrough(true)
                .build(Shape.RenderingType.BATCH);
    }
    public BoxFaceShape submit(){
        ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID,"area_box"+minPos.hashCode()+maxPos.hashCode()),this.boxShape);
        return this.boxShape;
    }
    public void destroy() {
        if (boxShape != null) {
            boxShape.discard();
            boxShape = null;
        }
    }
    public String asString(){
        return this.minPos.getX() + "," + this.minPos.getY() + "," + this.minPos.getZ() + ":"
                + this.maxPos.getX() + "," + this.maxPos.getY() + "," + this.maxPos.getZ();
    }
}