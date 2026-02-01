package ml.mypals.lucidity.features.tisMicroTiminghighlight;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public final class MicroTimingAnalyzer {
    private static final Deque<MicrotimingKeyWorldTracker.MicroEventNode> stack = new ArrayDeque<>();

    public static MicrotimingKeyWorldTracker.MicroEventNode accept(
            int depth,
            Vec3 pos,
            DyeColor color,
            String message,
            Component component
    ) {
        MicrotimingKeyWorldTracker.MicroEventNode node =
                new MicrotimingKeyWorldTracker.MicroEventNode(depth, pos, color, message, component);

        while (!stack.isEmpty() && stack.peek().depth >= depth) {
            stack.pop();
        }

        if (stack.isEmpty()) {
            node.parent = null;
        } else {
            MicrotimingKeyWorldTracker.MicroEventNode parent = stack.peek();
            node.parent = parent;
            parent.children.add(node);
        }

        stack.push(node);

        return depth == 0 ? node : node.parent;
    }

    public static void reset() {
        stack.clear();
    }
    static ClickEvent appendParentMarker(
            MicrotimingKeyWorldTracker.MicroEventNode parent
    ) {
        if (parent == null) return null;
        //? if >=1.21.5 {
        /*return new ClickEvent.CopyToClipboard(encodeParentHidden(parent));
        *///?} else {
        return new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, encodeParentHidden(parent));
        //?}
    }

    private static final char BIT_0 = '\u00A0';
    private static final char BIT_1 = '\u2000';
    private static final char SEP   = '\u2002';
    private static final char END   = '\u2001';

    private static void appendBits(StringBuilder sb, int value, int bits) {
        for (int i = bits - 1; i >= 0; i--) {
            sb.append(((value >> i) & 1) == 1 ? BIT_1 : BIT_0);
        }
    }
    private static String encodeParentHidden(MicrotimingKeyWorldTracker.MicroEventNode parent) {
        StringBuilder sb = new StringBuilder();

        sb.append(SEP);
        BlockPos pos = BlockPos.containing(parent.pos);
        appendBits(sb, pos.getX(), 32);
        appendBits(sb, pos.getY(), 32);
        appendBits(sb, pos.getZ(), 32);

        appendBits(sb, parent.color.getId(), 4);

        sb.append(END);

        return sb.toString();
    }
    public static ParentInfo decodeParentHidden(String s) {
        int i = s.indexOf(SEP);
        if (i == -1) return null;
        i++;

        int x = readBits(s, i, 32); i += 32;
        int y = readBits(s, i, 32); i += 32;
        int z = readBits(s, i, 32); i += 32;
        int color = readBits(s, i, 4);

        return new ParentInfo(new Vec3(x, y, z), DyeColor.byId(color));
    }

    private static int readBits(String s, int offset, int bits) {
        int v = 0;
        for (int i = 0; i < bits; i++) {
            v <<= 1;
            if (s.charAt(offset + i) == BIT_1) v |= 1;
        }
        return v;
    }
    public static record ParentInfo(Vec3 vec3,DyeColor color){

    }

}

