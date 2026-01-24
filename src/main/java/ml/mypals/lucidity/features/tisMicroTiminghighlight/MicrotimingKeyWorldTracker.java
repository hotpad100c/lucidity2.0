package ml.mypals.lucidity.features.tisMicroTiminghighlight;

import ml.mypals.ryansrenderingkit.shape.Shape;
import ml.mypals.ryansrenderingkit.shape.box.BoxFaceShape;
import ml.mypals.ryansrenderingkit.shape.box.BoxShape;
import ml.mypals.ryansrenderingkit.shape.box.BoxWireframeShape;
import ml.mypals.ryansrenderingkit.shapeManagers.ShapeManagers;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ml.mypals.lucidity.LucidityModInfo.MOD_ID;
import static ml.mypals.lucidity.features.tisMicroTiminghighlight.MicroTimingAnalyzer.appendParentMarker;

public class MicrotimingKeyWorldTracker {
    private static boolean registered = false;
    private static BoxFaceShape actorPosMarker = new BoxFaceShape(Shape.RenderingType.BATCH, boxTransformer -> {},Vec3.ZERO.add(0.5,0.5,0.5),Vec3.ZERO.subtract(0.5,0.5,0.5), Color.WHITE,true,BoxShape.BoxConstructionType.CORNERS);
    private static BoxWireframeShape actorParentPosMarker = new BoxWireframeShape(Shape.RenderingType.BATCH, boxTransformer -> {},Vec3.ZERO.add(0.5,0.5,0.5),Vec3.ZERO.subtract(0.5,0.5,0.5), Color.WHITE,true,4,BoxShape.BoxConstructionType.CORNERS);
    public static final Pattern MARKER_POSITION_PATTERN = Pattern.compile(
            "\\[\\s*(-?\\d+(?:\\.\\d+)?)\\s*,\\s*(-?\\d+(?:\\.\\d+)?)\\s*,\\s*(-?\\d+(?:\\.\\d+)?)\\s*\\]"
    );
    public static final Pattern INDEX_GETTER_PATTERN = Pattern.compile(":\\s*(\\d+)");
    public static boolean isMarkerLog(String input){
        return MARKER_POSITION_PATTERN.matcher(input).find();
    }
    public static Vec3 parseMarkerPos(String input) {
        Matcher matcher = MARKER_POSITION_PATTERN.matcher(input);

        if (!matcher.find()) {
            return null;
        }

        double x = Double.parseDouble(matcher.group(1));
        double y = Double.parseDouble(matcher.group(2));
        double z = Double.parseDouble(matcher.group(3));

        return new Vec3(x, y, z);
    }
    public static int parseMarkerIndent(String input) {
        String upper = input.toUpperCase(Locale.ROOT);
        Matcher m = INDEX_GETTER_PATTERN.matcher(upper);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return -1;
    }
    public static DyeColor parseMarkerColor(String input) {
        String upper = input.toUpperCase(Locale.ROOT);

        for (DyeColor color : DyeColor.values()) {
            if (upper.contains(color.getName().toUpperCase(Locale.ROOT))) {
                return color;
            }
        }
        return null;
    }
    public static void updateActorPos(Vec3 vec3, DyeColor dyeColor, MicroTimingAnalyzer.ParentInfo parentInfo){
        if(!registered){
            ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID,"microtiming_cator"),actorPosMarker);
        }
        if(!actorPosMarker.enabled()){
            actorPosMarker.enable();
        }
        actorPosMarker.setWorldPosition(vec3.add(0.5,0.5,0.5));

        int currentRGB = actorPosMarker.getBaseColor().getRGB() & 0x00FFFFFF;
        int targetRGB  = dyeColor.getTextColor() & 0x00FFFFFF;

        if (currentRGB != targetRGB) {
            Color color = new Color(targetRGB);
            actorPosMarker.setBaseColor(
                    new Color(color.getRed(),color.getGreen(),color.getBlue(),40)
            );
        }

        if(parentInfo != null){
            if(!actorParentPosMarker.enabled()){
                actorParentPosMarker.enable();
            }
            actorParentPosMarker.setWorldPosition(parentInfo.vec3().add(0.5,0.5,0.5));

            int currentRGBP = actorParentPosMarker.getBaseColor().getRGB() & 0x00FFFFFF;
            int targetRGBP  = parentInfo.color().getTextColor() & 0x00FFFFFF;
            if (currentRGBP != targetRGBP) {
                Color color = new Color(targetRGBP);
                actorParentPosMarker.setBaseColor(
                        color
                );
            }
        }else{
            if(actorParentPosMarker.enabled()){
                actorParentPosMarker.disable();
            }
        }


    }
    public static Component track(Component component) {
        List<Component> contents = component.toFlatList();

        for (int i = 0;i<contents.size();i++) {
            Component component1 = contents.get(i);
            String content = component1.getString();

            if (content.contains("#")) {
                HoverEvent hoverEvent = component1.getStyle().getHoverEvent();
                //? if >=1.21.5 {
                if (hoverEvent != null && hoverEvent.action() == HoverEvent.Action.SHOW_TEXT) {
                    HoverEvent.ShowText showText = (HoverEvent.ShowText)hoverEvent;
                    String tooltip = showText.value().getString();
                //?} else {
                /*if (hoverEvent != null && hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT) != null) {
                    String tooltip = hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT).getString();
                *///?}
                    Vec3 vec3 = parseMarkerPos(tooltip);
                    DyeColor dyeColor = parseMarkerColor(tooltip);
                    int indent = parseMarkerIndent(tooltip);

                    if (vec3 != null && dyeColor != null && indent > 0) {

                        MicroEventNode parent = MicroTimingAnalyzer.accept(
                                indent-1,
                                vec3,
                                dyeColor,
                                content.trim(),
                                component1
                        );

                        if (parent != null) {
                            ClickEvent data = appendParentMarker(parent);

                            component1 = component1.copy().withStyle(
                                    component1.getStyle().withClickEvent(data)
                            );
                            contents.set(i,component1);
                            break;
                        }
                    }
                }
            }

            if (content.contains("------------")) {
                MicroTimingAnalyzer.reset();
            }
        }
        MutableComponent mutableComponent = Component.empty();
        for (Component c : contents) {
            mutableComponent.append(c);
        }
        return mutableComponent;
    }

    public static void start(){
        ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID,"microtiming_cator"),actorPosMarker);
        ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID,"microtiming_cator_parent"),actorParentPosMarker);
        registered = true;
    }
    public static void stop(){
        actorPosMarker.disable();
        actorParentPosMarker.disable();
        actorPosMarker.discard();
        actorParentPosMarker.discard();
        registered = false;
    }
    public static final class MicroEventNode {
        public final int depth;
        public final Vec3 pos;
        public final DyeColor color;
        public final String message;

        public final Component component; // ⭐ 原始消息组件
        public MicroEventNode parent;
        public final List<MicroEventNode> children = new ArrayList<>();

        public MicroEventNode(
                int depth,
                Vec3 pos,
                DyeColor color,
                String message,
                Component component
        ) {
            this.depth = depth;
            this.pos = pos;
            this.color = color;
            this.message = message;
            this.component = component;
        }
    }



}
