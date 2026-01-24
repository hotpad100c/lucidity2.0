package ml.mypals.lucidity.features.imageRender;

import ml.mypals.lucidity.Lucidity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;

public class MediaTypeDetector {
    public enum MediaType {
        IMAGE("IMAGE"),
        GIF("GIF"),
        VIDEO("VIDEO"),
        UNKNOWN("UNKNOWN");

        private final String key;
        MediaType(String value) {
            this.key = value;
        }
        public String getKey() {
            return key;
        }
    }

    public static MediaType detectMediaType(String source) {
        MediaType typeByExtension = guessMediaTypeByExtension(source);
        Lucidity.LOGGER.debug("Guessed media type by extension for {}: {}", source, typeByExtension);

        try {
            if (source.startsWith("http://") || source.startsWith("https://")) {
                URI url = new URI(source);
                HttpURLConnection connection = (HttpURLConnection) url.toURL().openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setConnectTimeout(5000);

                String contentType = connection.getContentType();
                MediaType typeByContentType = guessMediaTypeByContentType(contentType);

                if (typeByContentType == MediaType.VIDEO || typeByContentType == MediaType.GIF) {
                    connection.disconnect();
                    return typeByContentType;
                }

                try (InputStream inputStream = connection.getInputStream()) {
                    return detectMediaTypeByHeader(inputStream);
                } finally {
                    connection.disconnect();
                }
            } else {
                File file = new File(source);
                if (!file.exists() || !file.isFile()) {
                    return MediaType.UNKNOWN;
                }
                try (InputStream inputStream = Files.newInputStream(file.toPath())) {
                    return detectMediaTypeByHeader(inputStream);
                }
            }
        } catch (Throwable e) {
            return MediaType.UNKNOWN;
        }
    }

    private static MediaType guessMediaTypeByExtension(String source) {
        String lowerSource = source.toLowerCase();
        if (lowerSource.endsWith(".png") || lowerSource.endsWith(".jpg") || lowerSource.endsWith(".jpeg") ||
                lowerSource.endsWith(".webp") || lowerSource.endsWith(".bmp")) {
            return MediaType.IMAGE;
        } else if (lowerSource.endsWith(".gif")) {
            return MediaType.GIF;
        } else if (lowerSource.endsWith(".mp4") || lowerSource.endsWith(".avi") || lowerSource.endsWith(".mov")) {
            return MediaType.VIDEO;
        } else {
            return MediaType.UNKNOWN;
        }
    }

    private static MediaType guessMediaTypeByContentType(String contentType) {
        if (contentType == null) {
            return MediaType.UNKNOWN;
        }
        contentType = contentType.toLowerCase();
        if (contentType.contains("image/gif")) {
            return MediaType.GIF;
        } else if (contentType.contains("image/")) {
            return MediaType.IMAGE;
        } else if (contentType.contains("video/")) {
            return MediaType.VIDEO;
        } else {
            return MediaType.UNKNOWN;
        }
    }

    private static MediaType detectMediaTypeByHeader(InputStream inputStream) throws IOException {
        if (!inputStream.markSupported()) {
            inputStream = new BufferedInputStream(inputStream);
        }
        inputStream.mark(32);
        byte[] header = new byte[32];
        int bytesRead = inputStream.read(header);
        inputStream.reset();

        if (bytesRead < 8) {
            return MediaType.UNKNOWN;
        }

        // GIF: 47 49 46 38 (GIF8)
        if (header[0] == (byte) 0x47 && header[1] == (byte) 0x49 && header[2] == (byte) 0x46 && header[3] == (byte) 0x38) {
            return MediaType.GIF;
        }

        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (header[0] == (byte) 0x89 && header[1] == (byte) 0x50 && header[2] == (byte) 0x4E && header[3] == (byte) 0x47) {
            return MediaType.IMAGE;
        }

        // JPEG: FF D8
        if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8) {
            return MediaType.IMAGE;
        }

        // WebP
        if (header[0] == (byte) 0x52 && header[1] == (byte) 0x49 && header[2] == (byte) 0x46 && header[3] == (byte) 0x46 &&
                header[8] == (byte) 0x57 && header[9] == (byte) 0x45 && header[10] == (byte) 0x42 && header[11] == (byte) 0x50) {
            return MediaType.IMAGE;
        }

        // BMP: 42 4D (BM)
        if (header[0] == (byte) 0x42 && header[1] == (byte) 0x4D) {
            return MediaType.IMAGE;
        }

        // MP4/MOV
        if (header[4] == (byte) 0x66 && header[5] == (byte) 0x74 && header[6] == (byte) 0x79 && header[7] == (byte) 0x70) {
            return MediaType.VIDEO;
        }

        // AVI
        if (header[0] == (byte) 0x52 && header[1] == (byte) 0x49 && header[2] == (byte) 0x46 && header[3] == (byte) 0x46 &&
                header[8] == (byte) 0x41 && header[9] == (byte) 0x56 && header[10] == (byte) 0x49) {
            return MediaType.VIDEO;
        }

        return MediaType.UNKNOWN;
    }
}