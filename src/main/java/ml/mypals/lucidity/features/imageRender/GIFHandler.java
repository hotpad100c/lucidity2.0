package ml.mypals.lucidity.features.imageRender;

import com.mojang.blaze3d.platform.NativeImage;
import ml.mypals.lucidity.Lucidity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static ml.mypals.lucidity.LucidityModInfo.MOD_ID;
import static ml.mypals.lucidity.features.imageRender.MediaShape.LOST;
import static ml.mypals.lucidity.features.imageRender.ImageRenderManager.TEMP_TEXTURE_PATH;

public class GIFHandler {
    public static class GifFrameData {
        public List<ResourceLocation> identifiers; // Identifier -> ResourceLocation
        public List<Integer> delays;

        public GifFrameData(List<ResourceLocation> identifiers, List<Integer> delays) {
            this.identifiers = identifiers;
            this.delays = delays;
        }
    }

    public static GifFrameData createGifTextures(String source, String baseName) {
        List<ResourceLocation> identifiers = new ArrayList<>();
        List<Integer> delays = new ArrayList<>();
        TextureManager textureManager = Minecraft.getInstance().getTextureManager(); // MinecraftClient -> Minecraft

        ImageInputStream imageInputStream = null;
        ImageReader reader = null;
        InputStream inputStream = null;

        try {
            byte[] data;
            if (source.startsWith("http://") || source.startsWith("https://")) {
                URI imageUrl = new URI(source);
                HttpURLConnection connection = (HttpURLConnection) imageUrl.toURL().openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setConnectTimeout(5000);

                String contentType = connection.getContentType();
                if (contentType == null || !contentType.toLowerCase().contains("gif")) {
                    Lucidity.LOGGER.warn("URL {} was not a GIF file (Content-Type: {})", source, contentType);
                    identifiers.add(LOST);
                    connection.disconnect();
                    return new GifFrameData(identifiers, delays);
                }

                inputStream = new BufferedInputStream(connection.getInputStream());
                data = inputStream.readAllBytes();
                connection.disconnect();
            } else {
                File file = new File(source);
                if (!file.exists() || !file.isFile()) {
                    Lucidity.LOGGER.warn("File {} does not exist", source);
                    identifiers.add(LOST);
                    return new GifFrameData(identifiers, delays);
                }
                inputStream = new BufferedInputStream(Files.newInputStream(file.toPath()));
                data = inputStream.readAllBytes();
            }

            imageInputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(data));
            reader = getGifReader(imageInputStream);
            if (reader == null) {
                identifiers.add(LOST);
                return new GifFrameData(identifiers, delays);
            }

            int frameCount = reader.getNumImages(true);

            // 获取画布尺寸
            IIOMetadataNode globalRoot = (IIOMetadataNode) reader.getStreamMetadata()
                    .getAsTree("javax_imageio_gif_stream_1.0");
            int canvasWidth = Integer.parseInt(globalRoot.getElementsByTagName("LogicalScreenDescriptor")
                    .item(0).getAttributes().getNamedItem("logicalScreenWidth").getNodeValue());
            int canvasHeight = Integer.parseInt(globalRoot.getElementsByTagName("LogicalScreenDescriptor")
                    .item(0).getAttributes().getNamedItem("logicalScreenHeight").getNodeValue());

            BufferedImage canvas = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gCanvas = canvas.createGraphics();
            gCanvas.setComposite(AlphaComposite.Clear);
            gCanvas.fillRect(0, 0, canvasWidth, canvasHeight);
            gCanvas.dispose();

            BufferedImage savedPrevious = null;
            String prevDisposal = "none";
            int prevX = 0, prevY = 0, prevW = canvasWidth, prevH = canvasHeight;

            for (int i = 0; i < frameCount; i++) {
                BufferedImage frame = reader.read(i);
                IIOMetadata metadata = reader.getImageMetadata(i);
                String metaFormat = metadata.getNativeMetadataFormatName();
                IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormat);

                IIOMetadataNode imgDescr = (IIOMetadataNode) root.getElementsByTagName("ImageDescriptor").item(0);
                int x = Integer.parseInt(imgDescr.getAttribute("imageLeftPosition"));
                int y = Integer.parseInt(imgDescr.getAttribute("imageTopPosition"));
                int w = Integer.parseInt(imgDescr.getAttribute("imageWidth"));
                int h = Integer.parseInt(imgDescr.getAttribute("imageHeight"));

                IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
                String disposalMethod = gce.getAttribute("disposalMethod");
                int delay = Integer.parseInt(gce.getAttribute("delayTime")) * 10;
                delays.add(delay);

                if (prevDisposal.equalsIgnoreCase("restoreToBackgroundColor")) {
                    Graphics2D g = canvas.createGraphics();
                    g.setComposite(AlphaComposite.Clear);
                    g.fillRect(prevX, prevY, prevW, prevH);
                    g.dispose();
                } else if (prevDisposal.equalsIgnoreCase("restoreToPrevious") && savedPrevious != null) {
                    canvas = deepCopy(savedPrevious);
                }

                if (disposalMethod.equalsIgnoreCase("restoreToPrevious")) {
                    savedPrevious = deepCopy(canvas);
                } else {
                    savedPrevious = null;
                }

                Graphics2D gFrame = canvas.createGraphics();
                gFrame.setComposite(AlphaComposite.SrcOver);
                gFrame.drawImage(frame, x, y, null);
                gFrame.dispose();

                NativeImage nativeImage = convertBufferedImageToNativeImage(canvas);
                ResourceLocation frameIdentifier = ResourceLocation.fromNamespaceAndPath(MOD_ID, TEMP_TEXTURE_PATH + baseName + "_frame_" + i);
                identifiers.add(frameIdentifier);

                Minecraft.getInstance().execute(() -> {
                    textureManager.register(frameIdentifier, new DynamicTexture(/*? if >=1.21.5 {*//*frameIdentifier::toLanguageKey,*//*?}*/nativeImage));
                });

                prevDisposal = disposalMethod;
                prevX = x;
                prevY = y;
                prevW = w;
                prevH = h;
            }

        } catch (Throwable e) {
            Lucidity.LOGGER.error("Can't create GIF texture from {} : {}", source, e.getMessage());
            e.printStackTrace();
            identifiers.add(LOST);
        } finally {
            try {
                if (reader != null) reader.dispose();
                if (imageInputStream != null) imageInputStream.close();
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                Lucidity.LOGGER.error("Error while closing source: {}", e.getMessage());
            }
        }

        if (identifiers.isEmpty()) {
            identifiers.add(LOST);
        }
        return new GifFrameData(identifiers, delays);
    }

    private static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    private static ImageReader getGifReader(ImageInputStream imageInputStream) {
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
        if (!readers.hasNext()) {
            Lucidity.LOGGER.error("Unable to find the ImageReader");
            return null;
        }
        ImageReader reader = readers.next();
        reader.setInput(imageInputStream);
        return reader;
    }

    private static NativeImage convertBufferedImageToNativeImage(BufferedImage bufferedImage) {
        // NativeImage.Format.RGBA -> NativeImage.Format.RGBA
        NativeImage nativeImage = new NativeImage(
                NativeImage.Format.RGBA,
                bufferedImage.getWidth(),
                bufferedImage.getHeight(),
                false
        );

        for (int x = 0; x < bufferedImage.getWidth(); x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                int argb = bufferedImage.getRGB(x, y);

                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8)  & 0xFF;
                int b =  argb        & 0xFF;

                //? if >=1.21.3 {
                /*int color = (a << 24) | (r << 16) | (g << 8) | b;
                nativeImage.setPixel(x, y, color);
                *///?} else {
                int color = (a << 24) | (b << 16) | (g << 8) | r;
                nativeImage.setPixelRGBA(x, y, color);//Idk too...
                //?}
            }
        }

        return nativeImage;
    }
}