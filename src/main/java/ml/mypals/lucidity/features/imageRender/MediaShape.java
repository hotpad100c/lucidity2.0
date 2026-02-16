package ml.mypals.lucidity.features.imageRender;

//? if >=1.21.5 {
/*import com.mojang.blaze3d.opengl.GlStateManager;
*///?} else {
import com.mojang.blaze3d.platform.GlStateManager;
//?}
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import ml.mypals.lucidity.utils.LucidityConfigHelper;
import ml.mypals.lucidity.utils.axis.AxisObject;
import ml.mypals.ryansrenderingkit.builders.vertexBuilders.VertexBuilder;
import ml.mypals.ryansrenderingkit.shape.Shape;
import ml.mypals.ryansrenderingkit.shape.basics.tags.EmptyMesh;
import ml.mypals.ryansrenderingkit.transform.shapeTransformers.DefaultTransformer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
//? if >=1.21.5 {
/*import net.minecraft.util.TriState;
*///?}
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static ml.mypals.lucidity.LucidityModInfo.MOD_ID;
import static ml.mypals.lucidity.config.ImageRendererConfigs.*;
//? if >=1.21.5 {
/*import com.mojang.blaze3d.pipeline.RenderPipeline;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED_SNIPPET;
*///?}
public class MediaShape extends Shape implements EmptyMesh {

    //? >=1.21.5 {
    /*public static final RenderPipeline MEDIA_SHAPE_TEXTURE;
    private static final Function<ResourceLocation, RenderType> MEDIA_SHAPE_RENDER_TYPE;
    static {
        MEDIA_SHAPE_TEXTURE = RenderPipelines.register(RenderPipeline.builder(GUI_TEXTURED_SNIPPET).withCull(false).withLocation("pipeline/gui_textured").build());
        MEDIA_SHAPE_RENDER_TYPE = Util.memoize((resourceLocation) -> RenderType.create("media_shape_texture", 1536, RenderPipelines.GUI_TEXTURED, RenderType.CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation/^? if <1.21.6 {^/, TriState.DEFAULT/^?}^/, false)).createCompositeState(false)));
    }
    *///?}


    private MediaData mediaData;

    private ResourceLocation currentTextureId;

    private int currentFrame = 0;
    private long lastFrameTime = 0;

    public static final ResourceLocation LOST = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/lost-file.png");
    public static final ResourceLocation LOADING = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/loading.png");

    public MediaShape(Color color, Vec3 center, boolean seeThrough, MediaData mediaData) {
        super(RenderingType.IMMEDIATE,
                (defaultTransformer) ->{
                    Shape shape = defaultTransformer.getShape();
                    if(mediaData != null && mediaData.isReady()){
                        defaultTransformer.setShapeWorldScale(mediaData.getScale());
                        defaultTransformer.setShapeWorldRotation(mediaData.getRotation());
                    }
                    handleMouseGrab(Minecraft.getInstance().player,
                            (MediaShape) defaultTransformer.getShape(),
                            defaultTransformer);
                },
                color, center, seeThrough);
        this.mediaData = mediaData;

        if (mediaData.textureIDs != null && mediaData.textureIDs.length > 0) {
            this.currentTextureId = mediaData.textureIDs[0];
        } else {
            this.currentTextureId = ImageRenderManager.createTexture(mediaData.path, mediaData.name);
        }
    }

    public MediaShape(Color color, Vec3 center, boolean seeThrough) {
        this(color, center, seeThrough,
                new MediaData(-1, LOST.getPath(), "lost", null,
                        new double[3], new double[4], new double[3],
                        MediaTypeDetector.MediaType.IMAGE, false));
    }
    public void updateFromEntry(MediaEntry entry) {
        this.mediaData.textureIDs = entry.getTexture();
        this.mediaData.type = entry.getType();
        this.mediaData.setDelays(entry.getDelays());
        if(this.getCustomData("ownedAxis",null) == null){
            this.mediaData.pos = new Vec3(
                    entry.pos.length > 0 ? entry.pos[0] : 0,
                    entry.pos.length > 1 ? entry.pos[1] : 0,
                    entry.pos.length > 2 ? entry.pos[2] : 0
            );

            this.mediaData.rotation = new Quaternionf(
                    entry.rotation.length > 0 ? entry.rotation[0] : 0,
                    entry.rotation.length > 1 ? entry.rotation[1] : 0,
                    entry.rotation.length > 2 ? entry.rotation[2] : 0,
                    entry.rotation.length > 3 ? entry.rotation[3] : 1  // w 默认为 1
            );

            this.mediaData.scale = new Vec3(
                    entry.scale.length > 0 ? entry.scale[0] : 1,
                    entry.scale.length > 1 ? entry.scale[1] : 1,
                    1
            );
        }
        if (entry.isReady()) {
            if(!mediaData.ready){
                this.currentTextureId = entry.getTexture()[0];
                this.mediaData.ready = entry.isReady();

            }
            updateAnimationFrame();
        }
    }
    public static MediaShape fromMediaEntry(MediaEntry entry, Color color, Vec3 center, boolean seeThrough) {
        MediaData data = new MediaData(
                entry.index,
                entry.path,
                entry.name,
                entry.textureIDs,
                entry.pos,
                entry.rotation,
                entry.scale,
                entry.type,
                entry.ready
        );
        data.setDelays(entry.getDelays());
        data.setSelected(entry.isSelected());

        return new MediaShape(color, center, seeThrough, data);
    }

    @Override
    protected void generateRawGeometry(boolean b) {
        this.model_vertexes.clear();
        this.model_vertexes.add(new Vec3(-1, -1, 0));
        this.model_vertexes.add(new Vec3(1, -1, 0));
        this.model_vertexes.add(new Vec3(1, 1, 0));
        this.model_vertexes.add(new Vec3(-1, 1, 0));

        this.indexBuffer = new int[]{
                0, 1, 3,
                3, 2, 1,
                3, 1, 0,
                1, 2, 3,
        };
    }

    @Override
    protected void drawInternal(VertexBuilder builder) {
        if (!IMAGER_RENDERING.getBooleanValue()) return;
        if (indexBuffer.length < 4) {
            generateRawGeometry(false);
        }


        Minecraft client = Minecraft.getInstance();
        AtomicReference<Float> textureWidth = new AtomicReference<>((float) 16);
        AtomicReference<Float> textureHeight = new AtomicReference<>((float) 16);

        try {
            AbstractTexture texture = client.getTextureManager().getTexture(currentTextureId);

            if (texture instanceof DynamicTexture dynamicTexture) {
                NativeImage image = dynamicTexture.getPixels();
                if (image != null) {
                    textureWidth.set((float) image.getWidth() / PIXELS_PER_BLOCK.getFloatValue());
                    textureHeight.set((float) image.getHeight() / PIXELS_PER_BLOCK.getFloatValue());
                }
            } else {
                Optional<NativeImage> imageOptional = client.getResourceManager()
                        .getResource(currentTextureId)
                        .map(resource -> {
                            try (InputStream inputStream = resource.open()) {
                                return NativeImage.read(inputStream);
                            } catch (IOException e) {
                                return null;
                            }
                        });

                if (imageOptional.isPresent()) {
                    NativeImage image = imageOptional.get();
                    textureWidth.set((float) image.getWidth() / PIXELS_PER_BLOCK.getFloatValue());
                    textureHeight.set((float) image.getHeight() / PIXELS_PER_BLOCK.getFloatValue());
                    image.close();
                }
            }
        } catch (Exception e) {
            loadFallbackTexture(client, ref -> {
                textureWidth.set(ref[0]);
                textureHeight.set(ref[1]);
            });
        }

        float scaledWidth = (float) (textureWidth.get() * mediaData.scale.x);
        float scaledHeight = (float) (textureHeight.get() * mediaData.scale.y);

        this.model_vertexes.clear();
        this.model_vertexes.add(new Vec3(-scaledWidth, -scaledHeight, 0));
        this.model_vertexes.add(new Vec3(scaledWidth, -scaledHeight, 0));
        this.model_vertexes.add(new Vec3(scaledWidth, scaledHeight, 0));
        this.model_vertexes.add(new Vec3(-scaledWidth, scaledHeight, 0));

        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_TEX_COLOR
        );



        bufferBuilder.addVertex(builder.getPositionMatrix(),
                        (float) model_vertexes.get(0).x, (float) model_vertexes.get(0).y, (float) model_vertexes.get(0).z)
                .setColor(this.baseColor.getRed(), this.baseColor.getGreen(), this.baseColor.getBlue(), this.baseColor.getAlpha()).setUv(0.0f, 1.0f).setLight(LightTexture.FULL_BRIGHT);
        bufferBuilder.addVertex(builder.getPositionMatrix(),
                        (float) model_vertexes.get(1).x, (float) model_vertexes.get(1).y, (float) model_vertexes.get(1).z)
                .setColor(this.baseColor.getRed(), this.baseColor.getGreen(), this.baseColor.getBlue(), this.baseColor.getAlpha()).setUv(1.0f, 1.0f).setLight(LightTexture.FULL_BRIGHT);
        bufferBuilder.addVertex(builder.getPositionMatrix(),
                        (float) model_vertexes.get(2).x, (float) model_vertexes.get(2).y, (float) model_vertexes.get(2).z)
                .setColor(this.baseColor.getRed(), this.baseColor.getGreen(), this.baseColor.getBlue(), this.baseColor.getAlpha()).setUv(1.0f, 0.0f).setLight(LightTexture.FULL_BRIGHT);
        bufferBuilder.addVertex(builder.getPositionMatrix(),
                        (float) model_vertexes.get(3).x, (float) model_vertexes.get(3).y, (float) model_vertexes.get(3).z)
                .setColor(this.baseColor.getRed(), this.baseColor.getGreen(), this.baseColor.getBlue(), this.baseColor.getAlpha()).setUv(0.0f, 0.0f).setLight(LightTexture.FULL_BRIGHT);


        //? if >=1.21.6 {
        /*RenderType renderType = MEDIA_SHAPE_RENDER_TYPE.apply(currentTextureId);
        *///?} else if >=1.21.5 {
        /*RenderType renderType = RenderType.guiTextured(currentTextureId);
        *///?} else {
        
        RenderSystem.setShaderTexture(0, currentTextureId);
        //? if >=1.21.3 {
        /*RenderSystem.setShader(CoreShaders.POSITION_TEX_COLOR);
        *///?} else {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        //?}
        //?}
        GlStateManager._enableDepthTest();
        if (this.seeThrough) GlStateManager._disableDepthTest();
        GlStateManager._disableCull();

        //? if >=1.21.5 {
        /*renderType.draw(bufferBuilder.buildOrThrow());
        *///?} else {
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        //?}
        GlStateManager._enableCull();
        if (this.seeThrough) GlStateManager._enableDepthTest();
    }

    private void updateAnimationFrame() {
        if (mediaData.type != MediaTypeDetector.MediaType.GIF) return;
        if (mediaData.textureIDs == null || mediaData.textureIDs.length <= 1) return;
        if (mediaData.delays == null || mediaData.delays.isEmpty()) return;

        long currentTime = System.currentTimeMillis();
        int frameDelay = mediaData.delays.get(currentFrame);

        if (currentTime - lastFrameTime >= frameDelay) {
            currentFrame = (currentFrame + 1) % mediaData.textureIDs.length;
            currentTextureId = mediaData.textureIDs[currentFrame];
            lastFrameTime = currentTime;
        }
    }

    private void loadFallbackTexture(Minecraft client, java.util.function.Consumer<float[]> callback) {
        ResourceManager resourceManager = client.getResourceManager();
        Optional<Resource> resourceOptional = resourceManager.getResource(LOST);

        if (resourceOptional.isPresent()) {
            try (InputStream inputStream = resourceOptional.get().open()) {
                NativeImage image = NativeImage.read(inputStream);
                float width = (float) image.getWidth() / PIXELS_PER_BLOCK.getFloatValue();
                float height = (float) image.getHeight() / PIXELS_PER_BLOCK.getFloatValue();
                image.close();
                callback.accept(new float[]{width, height});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void handleMouseGrab(Player player, MediaShape shape, DefaultTransformer transformer) {
        Minecraft mc = Minecraft.getInstance();

        boolean pressed = mc.mouseHandler.isMiddlePressed();
        boolean wasHolding = shape.getCustomData("isHolding", false);
        AxisObject axisObject = shape.getCustomData("ownedAxis", null);
        boolean isLookingAt = shape.isPlayerLookingAt().hit;
        if (pressed && !wasHolding && isLookingAt) {
            if (axisObject == null) {
                axisObject = new AxisObject(transformer.getShapeWorldPivot(true),transformer.getShapeWorldRotation(true),transformer.getShapeWorldScale(true) ,shape.mediaData.name.toLowerCase());
                axisObject.submit();
                shape.putCustomData("ownedAxis", axisObject);
                shape.putCustomData("color", shape.baseColor);
                shape.setBaseColor(new Color(255,255,255,50));
                player.swing(player.getUsedItemHand());
                Vec3 pos = transformer.getShapeWorldPivot(true);
                for(int i=0;i<20;i++){
                    player.level().addParticle(
                            ParticleTypes.TOTEM_OF_UNDYING,
                            pos.x(), pos.y(), pos.z(),
                            ThreadLocalRandom.current().nextDouble(-0.5,0.5),
                            ThreadLocalRandom.current().nextDouble(-0.5,0.5),
                            ThreadLocalRandom.current().nextDouble(-0.5,0.5));

                }
            } else {
                axisObject.destroy();
                shape.putCustomData("ownedAxis", null);
                shape.setBaseColor(shape.getCustomData("color", Color.WHITE));
                player.swing(player.getUsedItemHand());

                for(Map.Entry<String, MediaEntry> entry: ImageRenderManager.images.entrySet()){
                    MediaEntry mediaEntry = entry.getValue();
                    MediaData mediaData = shape.getMediaData();
                    if(mediaEntry.name.equals(mediaData.name)){
                        mediaEntry.pos = new double[]{mediaData.pos.x(),mediaData.pos.y(),mediaData.pos.z()};
                        mediaEntry.rotation = new double[]{mediaData.rotation.x(),mediaData.rotation.y(),mediaData.rotation.z(),mediaData.rotation.w()};
                        mediaEntry.scale = new double[]{mediaData.scale.x(),mediaData.scale.y(),mediaData.scale.z()};

                        int id = -1;
                        List<String> images = IMAGES.getStrings();
                        for(String string : images){
                            if(string.split(";")[1].equals(mediaEntry.name)){
                                id = images.indexOf(string);
                            }
                        }

                        LucidityConfigHelper.setInConfigList(IMAGES,id,mediaData.toString(),false);
                    }
                }

            }
        }
        if(axisObject != null){
            shape.forceSetWorldPosition(axisObject.basePosition);
            shape.mediaData.pos = axisObject.basePosition;
            Quaternionf quaternionf = axisObject.getCurrentRotation();
            transformer.setShapeWorldRotation(quaternionf);
            shape.mediaData.rotation = quaternionf;
            shape.forceSetLocalScale(axisObject.getCurrentScale());
            shape.mediaData.scale = axisObject.getCurrentScale();
        }
        shape.putCustomData("isHolding", pressed);
    }

    public MediaData getMediaData() {
        return mediaData;
    }

    public ResourceLocation getCurrentTextureId() {
        return currentTextureId;
    }

    public int getCurrentFrame() {
        return currentFrame;
    }
    @Override
    public void discard(){
        super.discard();
        AxisObject axisObject = this.getCustomData("ownedAxis", null);
        if(axisObject != null){
            axisObject.destroy();
        }
    }

    public static class MediaData {
        public int index;
        public String path;
        public String name;
        public ResourceLocation[] textureIDs;
        public Vec3 pos;
        public Quaternionf rotation;
        public Vec3 scale;
        public MediaTypeDetector.MediaType type;
        public boolean ready;

        private List<Integer> delays;
        private boolean selected;

        public MediaData(int index, String path, String name,
                         @Nullable ResourceLocation[] textureIDs,
                         double[] pos, double[] rotation, double[] scale,
                         @NotNull MediaTypeDetector.MediaType type, boolean ready) {
            this.index = index;
            this.path = path;
            this.name = name;
            this.textureIDs = textureIDs;
            this.pos = new Vec3(
                    pos.length > 0 ? pos[0] : 0,
                    pos.length > 1 ? pos[1] : 0,
                    pos.length > 2 ? pos[2] : 0
            );

            this.rotation = new Quaternionf(
                    (float) (rotation.length > 0 ? rotation[0] : 0),
                    (float) (rotation.length > 1 ? rotation[1] : 0),
                    (float) (rotation.length > 2 ? rotation[2] : 0),
                    (float) (rotation.length > 3 ? rotation[3] : 1)
            );

            this.scale = new Vec3(
                    scale.length > 0 ? scale[0] : 1,
                    scale.length > 1 ? scale[1] : 1,
                    1
            );
            this.type = type;
            this.ready = ready;
        }

        // Getters and Setters
        public List<Integer> getDelays() {
            return delays;
        }

        public void setDelays(List<Integer> delays) {
            this.delays = delays;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public String getPath() {
            return path;
        }

        public String getName() {
            return name;
        }

        public int getIndex() {
            return index;
        }

        public Vec3 getPos() {
            return pos;
        }

        public Quaternionf getRotation() {
            return rotation;
        }

        public Vec3 getScale() {
            return scale;
        }

        public MediaTypeDetector.MediaType getType() {
            return type;
        }

        public boolean isReady() {
            return ready;
        }

        public ResourceLocation[] getTextureIDs() {
            return textureIDs;
        }

        @Override
        public String toString() {
            return String.format("%s;%s;%s;%s;%s;%s;%s",
                    path,
                    name,
                    Arrays.toString(new double[]{pos.x,pos.y,pos.z}),
                    Arrays.toString(new double[]{rotation.x,rotation.y,rotation.z,rotation.w}),
                    Arrays.toString(new double[]{scale.x,scale.y,scale.z}),
                    type,
                    ready
            );
        }
    }
}