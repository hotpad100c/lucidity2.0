package ml.mypals.lucidity.features.imageRender;
import com.mojang.blaze3d.platform.NativeImage;
import ml.mypals.lucidity.Lucidity;
import ml.mypals.lucidity.utils.LucidityConfigHelper;
import ml.mypals.ryansrenderingkit.shapeManagers.ShapeManagers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;
import org.joml.Vector2i;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


import static ml.mypals.lucidity.LucidityModInfo.MOD_ID;
import static ml.mypals.lucidity.config.ImageRendererConfigs.IMAGES;
import static ml.mypals.lucidity.features.imageRender.GIFHandler.createGifTextures;

public class ImageRenderManager {

    public static final ConcurrentHashMap<String, MediaShape> activeShapes = new ConcurrentHashMap<>();
    public static final String TEMP_TEXTURE_PATH = "textures/temp/";
    private static final String GENERATED_PATH = "assets/" + MOD_ID + "/textures/generated/";
    public static final ResourceLocation LOST = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/lost-file.png");
    public static final ResourceLocation LOADING = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/loading.png");
    public static ResourceLocation TEST = null;
    public static void uploadTextures(){
        TEST = createTexture("null/1/1","m");
    }

    public static ArrayList<MediaEntry> readyToMerge = new ArrayList<>();
    public static ConcurrentHashMap<String, MediaEntry> images = new ConcurrentHashMap<>();


    public static void onClientTick() {
        mergeImages();
        syncShapesWithEntries();
    }

    public static void prepareImages() {
        Minecraft client = Minecraft.getInstance();
        TextureManager textureManager = client.getTextureManager();

        ((ITextureManager) textureManager).lucidity$destroyAll(ResourceLocation.fromNamespaceAndPath(MOD_ID, TEMP_TEXTURE_PATH));
        images.clear();
        activeShapes.entrySet().removeIf((set)->{
            set.getValue().discard();
            return true;
        });

        for (int i = 0; i < IMAGES.getStrings().size(); i++) {
            String pic = IMAGES.getStrings().get(i);
            if(pic.isEmpty()) continue;
            try {
                resolveRepeatedName(i);
                putToImages(parse(pic, i));

            }catch (Throwable throwable){
                Lucidity.LOGGER.info("Failed to parse picture : {}",pic);
            }
        }
    }

    public static void putToImages(MediaEntry mediaEntry) {
        if (mediaEntry != null) {
            images.put(mediaEntry.getName(), mediaEntry);
        }
    }
    public static void syncShapesWithEntries() {
        Iterator<Map.Entry<String, MediaShape>> iterator = activeShapes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, MediaShape> entry = iterator.next();
            if (!images.containsKey(entry.getKey())) {
                entry.getValue().discard();
                iterator.remove();
            }
        }

        for (MediaEntry mediaEntry : images.values()) {
            if (!activeShapes.containsKey(mediaEntry.getName())) {
                MediaShape newShape = MediaShape.fromMediaEntry(
                        mediaEntry,
                        java.awt.Color.WHITE,
                        new Vec3(mediaEntry.pos[0], mediaEntry.pos[1], mediaEntry.pos[2]),
                        false
                );

                ResourceLocation shapeId = ResourceLocation.fromNamespaceAndPath(MOD_ID, "media_" + mediaEntry.getName());
                ShapeManagers.addShape(shapeId, newShape);
                activeShapes.put(mediaEntry.getName(), newShape);
            } else {
                activeShapes.get(mediaEntry.getName()).updateFromEntry(mediaEntry);
            }
        }
    }
    public static void mergeImages() {
        for (MediaEntry image : readyToMerge) {
            images.put(image.getName(), image);
        }
        readyToMerge.clear();
    }

    public static ResourceLocation prepareImageMedia(String path, String name) {
        return createTexture(path, name);
    }

    public static Map.Entry<ResourceLocation[], List<Integer>> prepareGIFMedia(String path, String name) {
        GIFHandler.GifFrameData data = createGifTextures(path, name);
        return Map.entry(data.identifiers.toArray(ResourceLocation[]::new), data.delays);
    }

    public static void resolveRepeatedName(int index) {
        String picture = IMAGES.getStrings().get(index);
        String[] parts = picture.split(";");
        String oldName = parts[1];
        if (images.get(oldName) != null) {
            String newName = oldName + "_";
            String newPath = parts[0] + ";" + newName + ";" + parts[2] + ";" + parts[3] + ";" + parts[4];
            changeMapKey(images, oldName, newName);

            LucidityConfigHelper.setInConfigList(IMAGES,index,newPath);
        }
    }

    public static <K, V> void changeMapKey(Map<K, V> map, K oldKey, K newKey) {
        if (map.containsKey(oldKey)) {
            V value = map.remove(oldKey);
            map.put(newKey, value);
        }
    }

    public static ResourceLocation createTexture(String source, String name) {
        Minecraft client = Minecraft.getInstance();
        TextureManager textureManager = client.getTextureManager();
        ResourceLocation generatedPath = LOST;
        NativeImage image = null;
        try {
            if (source.startsWith("http://") || source.startsWith("https://")) {
                URI imageUrl = new URI(source);
                HttpURLConnection connection = (HttpURLConnection) imageUrl.toURL().openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setConnectTimeout(5000);
                String contentType = connection.getContentType();
                if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
                    Lucidity.LOGGER.warn("URL {} does not point to an image (Content-Type: {})", source, contentType);
                    connection.disconnect();
                    return generatedPath;
                }

                try (InputStream inputStream = connection.getInputStream()) {
                    image = convertToNativeImage(inputStream);
                } finally {
                    connection.disconnect();
                }
            } else {
                File file = new File(source);
                if (!file.exists() || !file.isFile()) {
                    Lucidity.LOGGER.warn("File {} does not exist or is not a file", source);
                    return generatedPath;
                }
                try (InputStream inputStream = Files.newInputStream(file.toPath())) {
                    image = convertToNativeImage(inputStream);
                }
            }

            generatedPath = ResourceLocation.fromNamespaceAndPath(MOD_ID, TEMP_TEXTURE_PATH + name);
            ResourceLocation finalGeneratedPath = generatedPath;
            NativeImage finalImage = image;

            //? if >=1.21.5 {
            ResourceLocation finalGeneratedPath1 = generatedPath;
            //?}
            Minecraft.getInstance().execute(() -> textureManager.register(finalGeneratedPath, new DynamicTexture(/*? if >=1.21.5 {*/finalGeneratedPath1::toLanguageKey,/*?}*/finalImage)));

        } catch (Throwable e) {
            Lucidity.LOGGER.error("Failed to create texture from {}: {}", source, e.getMessage());
            e.printStackTrace();
        }

        return generatedPath;
    }

    private static NativeImage convertToNativeImage(InputStream inputStream) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(inputStream);
        if (bufferedImage == null) throw new IOException("Unable to decode image");

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", pngOutputStream);
        byte[] pngBytes = pngOutputStream.toByteArray();

        try (ByteArrayInputStream pngInputStream = new ByteArrayInputStream(pngBytes)) {
            return NativeImage.read(pngInputStream);
        }
    }

    private static double[] parseArray(String arrayString, int expectCount) {
        String[] elements = arrayString.replaceAll("[\\[\\]]", "").split(",");
        return Arrays.copyOf(Arrays.stream(elements).mapToDouble(Double::parseDouble).toArray(), expectCount);
    }

    public static MediaEntry parse(String picture, int index) {
        String[] parts = picture.split(";");
        if (parts.length < 5) return null;

        String path = parts[0];
        String name = parts[1];
        double[] pos = parseArray(parts[2], 3);
        double[] rotation = parseArray(parts[3], 4);
        double[] scale = parseArray(parts[4], 2);

        MediaEntry initialEntry = createEntry(index, name, path, new ResourceLocation[]{LOADING}, pos, rotation, scale, MediaTypeDetector.MediaType.UNKNOWN, false);

        new Thread(() -> {
            try {
                ResourceLocation[] image = new ResourceLocation[]{LOST};
                List<Integer> delays = new ArrayList<>();
                MediaTypeDetector.MediaType type = MediaTypeDetector.detectMediaType(path);
                if (type.equals(MediaTypeDetector.MediaType.IMAGE)) {
                    image = new ResourceLocation[]{prepareImageMedia(path, name)};
                } else if (type.equals(MediaTypeDetector.MediaType.GIF)) {
                    Map.Entry<ResourceLocation[], List<Integer>> gifEntry = prepareGIFMedia(path, name);
                    image = gifEntry.getKey();
                    delays = gifEntry.getValue();
                }

                MediaEntry finalEntry = createEntry(index, name, path, image, pos, rotation, scale, type, true);
                if (!delays.isEmpty()) finalEntry.setDelays(delays);
                Lucidity.LOGGER.info("Finished loading : {}",finalEntry);
                images.put(finalEntry.getName(), finalEntry);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        return initialEntry;
    }

    private static MediaEntry createEntry(int index, String name, String path, ResourceLocation[] textures, double[] pos, double[] rotation, double[] scale, MediaTypeDetector.MediaType type, boolean ready) {
        return new MediaEntry(ready, index, name, path, textures, pos, rotation, scale, type);
    }

}
