package ml.mypals.lucidity.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class LucidityMiscHelpers {
    public static boolean shouldProcessEntityIfSinglePlayerOrOnlyClientSide(Entity entity){
        Minecraft minecraft = Minecraft.getInstance();
        Level level = entity.level();
        boolean isSinglePlayerAndServerSide = minecraft.hasSingleplayerServer() && entity.level().getServer() != null;
        boolean notSinglePlayerButClientSide = !minecraft.hasSingleplayerServer() && level.isClientSide();
        return isSinglePlayerAndServerSide || notSinglePlayerButClientSide;
    }
    public static Vec3 rotate(double t) {
        double angle = (t % 360 + 360) % 360;

        return new Vec3(
                angle,
                (angle + 120) % 360,
                (angle + 240) % 360
        );
    }
    public static Vector3f quaternionToEuler(Quaternionf q) {
        q = new Quaternionf(q).normalize();

        float sinp = -2.0f * (q.x * q.z - q.w * q.y);

        float pitch;
        if (Math.abs(sinp) >= 1) {
            pitch = (float) Math.copySign(Math.PI / 2, sinp);
        } else {
            pitch = (float) Math.asin(sinp);
        }

        float yaw = (float) Math.atan2(2.0f * (q.y * q.z + q.w * q.x),
                q.w * q.w - q.x * q.x - q.y * q.y + q.z * q.z);
        float roll = (float) Math.atan2(2.0f * (q.x * q.y + q.w * q.z),
                q.w * q.w + q.x * q.x - q.y * q.y - q.z * q.z);

        roll = (float) Math.toDegrees(roll);
        pitch = (float) Math.toDegrees(pitch);
        yaw = (float) Math.toDegrees(yaw);

        if (roll < 0) roll += 360;
        if (pitch < 0) pitch += 360;
        if (yaw < 0) yaw += 360;

        return new Vector3f(roll, pitch, yaw);
    }

}
