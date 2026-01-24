package ml.mypals.lucidity.features.imageRender;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MediaEntry {
    public MediaTypeDetector.MediaType type;
    private boolean selected;
    private List<Integer> delays;
    public int index;
    public ResourceLocation[] textureIDs;
    public boolean ready;
    public String path;
    public String name;
    public double[] pos;
    public double[] rotation;
    public double[] scale;

    public MediaEntry(boolean ready, int index, @Nullable String name,
                      @Nullable String orgPath, @Nullable ResourceLocation[] texturePath,
                      double[] pos, double[] rotation, double[] scale,
                      @NotNull MediaTypeDetector.MediaType type) {
        this.ready = ready;
        this.index = index;
        this.name = name;
        this.path = orgPath;
        this.textureIDs = texturePath;
        this.pos = pos;
        if(rotation.length<4) throw new IllegalStateException("?");
        this.rotation = rotation;
        this.scale = scale;
        this.type = type;
    }

    public List<Integer> getDelays() {
        return delays;
    }

    public void setDelays(List<Integer> delays) {
        this.delays = delays;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getName() {
        return name;
    }

    public MediaTypeDetector.MediaType getType() {
        return type;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean isReady() {
        return ready;
    }

    public int getIndex() {
        return index;
    }

    public double[] getPos() {
        return pos;
    }

    public double[] getRotation() {
        return rotation;
    }

    public double[] getScale() {
        return scale;
    }

    public String getPath() {
        return path;
    }

    public ResourceLocation[] getTexture() {
        return textureIDs;
    }

}
