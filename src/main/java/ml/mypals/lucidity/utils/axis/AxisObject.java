package ml.mypals.lucidity.utils.axis;

import ml.mypals.ryansrenderingkit.collision.RayModelIntersection;
import ml.mypals.ryansrenderingkit.shape.Shape;
import ml.mypals.ryansrenderingkit.shape.model.ObjModelShape;
import ml.mypals.ryansrenderingkit.shapeManagers.ShapeManagers;
import ml.mypals.ryansrenderingkit.transform.shapeTransformers.DefaultTransformer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.awt.Color;
import java.util.function.Consumer;

import static ml.mypals.lucidity.LucidityModInfo.MOD_ID;
import static ml.mypals.lucidity.features.netherPosCaculator.NetherPosCaculatorManager.CoordinatesEntry.scaleByDistance;

public class AxisObject {

    private static final ResourceLocation MOVE_AXIS_MODEL = ResourceLocation.fromNamespaceAndPath(MOD_ID, "models/obj/move.obj");
    private static final ResourceLocation ROTATION_AXIS_MODEL = ResourceLocation.fromNamespaceAndPath(MOD_ID, "models/obj/rotation.obj");
    private static final ResourceLocation SCALE_AXIS_MODEL = ResourceLocation.fromNamespaceAndPath(MOD_ID, "models/obj/scale.obj");

    private static final Color COLOR_X = new Color(255, 50, 50, 220);
    private static final Color COLOR_Y = new Color(50, 255, 50, 220);
    private static final Color COLOR_Z = new Color(50, 50, 255, 220);

    private static final Color COLOR_RX = new Color(255, 80, 80, 180);
    private static final Color COLOR_RY = new Color(80, 255, 80, 180);
    private static final Color COLOR_RZ = new Color(80, 80, 255, 180);

    private static final Color COLOR_SX = new Color(200, 40, 40, 220);
    private static final Color COLOR_SY = new Color(40, 200, 40, 220);
    private static final Color COLOR_SZ = new Color(40, 40, 200, 220);

    private static final Color COLOR_HIGHLIGHT = new Color(255, 255, 200, 255);

    public ObjModelShape mxAxis, myAxis, mzAxis;
    public ObjModelShape rxAxis, ryAxis, rzAxis;
    public ObjModelShape sxAxis, syAxis, szAxis;

    private double grabDistance;
    public ObjModelShape grabbingAxis;

    private float initialGrabAngle;
    private Quaternionf initialObjectRotation;  // 改用四元数
    private Quaternionf currentRotation;        // 改用四元数

    private Vec3 initialObjectScale;
    private Vec3 currentScale;

    private double initialGrabProjection;

    public Vec3 basePosition;
    public String name;
    private int signX = 1, signY = 1, signZ = 1;

    public AxisObject(Vec3 center, Quaternionf currentRotation, Vec3 initialScale, String name) {
        this.basePosition = center;
        this.grabbingAxis = null;
        this.grabDistance = 0.0;
        this.name = "axis_" + name;
        this.currentRotation = new Quaternionf(currentRotation).normalize();  // 直接存储四元数
        this.currentScale = initialScale;

        this.myAxis = new ObjModelShape(Shape.RenderingType.BATCH, createTransformLogic(Direction.Y, InteractionMode.MOVE), MOVE_AXIS_MODEL, basePosition, COLOR_Y, true);
        this.mxAxis = new ObjModelShape(Shape.RenderingType.BATCH, createTransformLogic(Direction.X, InteractionMode.MOVE), MOVE_AXIS_MODEL, basePosition, COLOR_X, true);
        this.mzAxis = new ObjModelShape(Shape.RenderingType.BATCH, createTransformLogic(Direction.Z, InteractionMode.MOVE), MOVE_AXIS_MODEL, basePosition, COLOR_Z, true);

        this.ryAxis = new ObjModelShape(Shape.RenderingType.BATCH, createTransformLogic(Direction.Y, InteractionMode.ROTATE), ROTATION_AXIS_MODEL, basePosition, COLOR_RY, true);
        this.rxAxis = new ObjModelShape(Shape.RenderingType.BATCH, createTransformLogic(Direction.X, InteractionMode.ROTATE), ROTATION_AXIS_MODEL, basePosition, COLOR_RX, true);
        this.rzAxis = new ObjModelShape(Shape.RenderingType.BATCH, createTransformLogic(Direction.Z, InteractionMode.ROTATE), ROTATION_AXIS_MODEL, basePosition, COLOR_RZ, true);

        this.syAxis = new ObjModelShape(Shape.RenderingType.BATCH, createTransformLogic(Direction.Y, InteractionMode.SCALE), SCALE_AXIS_MODEL, basePosition, COLOR_SY, true);
        this.sxAxis = new ObjModelShape(Shape.RenderingType.BATCH, createTransformLogic(Direction.X, InteractionMode.SCALE), SCALE_AXIS_MODEL, basePosition, COLOR_SX, true);
        this.szAxis = new ObjModelShape(Shape.RenderingType.BATCH, createTransformLogic(Direction.Z, InteractionMode.SCALE), SCALE_AXIS_MODEL, basePosition, COLOR_SZ, true);
        updateAllRotations();
    }

    private void updateAllRotations() {
        if (mxAxis != null) mxAxis.forceSetWorldRotation(new Vector3f(0, 0, -90));
        if (mzAxis != null) mzAxis.forceSetWorldRotation(new Vector3f(90, 0, 0));
        if (ryAxis != null) ryAxis.forceSetWorldRotation(new Vector3f(0, 0, -90));
        if (rzAxis != null) rzAxis.forceSetWorldRotation(new Vector3f(90, 0, 90));
        if (sxAxis != null) sxAxis.forceSetWorldRotation(new Vector3f(0, 0, -90));
        if (szAxis != null) szAxis.forceSetWorldRotation(new Vector3f(90, 0, 0));
    }

    public void submit() {
        ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID, name + "_move_x"), mxAxis);
        ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID, name + "_move_y"), myAxis);
        ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID, name + "_move_z"), mzAxis);

        ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID, name + "_rot_x"), rxAxis);
        ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID, name + "_rot_y"), ryAxis);
        ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID, name + "_rot_z"), rzAxis);

        ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID, name + "_scale_x"), sxAxis);
        ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID, name + "_scale_y"), syAxis);
        ShapeManagers.addShape(ResourceLocation.fromNamespaceAndPath(MOD_ID, name + "_scale_z"), szAxis);
    }

    public void destroy() {
        grabbingAxis = null;
        mxAxis.discard();
        myAxis.discard();
        mzAxis.discard();
        rxAxis.discard();
        ryAxis.discard();
        rzAxis.discard();
        sxAxis.discard();
        syAxis.discard();
        szAxis.discard();
    }

    public void setBasePosition(Vec3 pos) {
        this.basePosition = pos;
        updateAllPositions();
    }

    private void updateAllPositions() {
        if (mxAxis != null) mxAxis.forceSetWorldPosition(basePosition);
        if (myAxis != null) myAxis.forceSetWorldPosition(basePosition);
        if (mzAxis != null) mzAxis.forceSetWorldPosition(basePosition);
        if (rxAxis != null) rxAxis.forceSetWorldPosition(basePosition);
        if (ryAxis != null) ryAxis.forceSetWorldPosition(basePosition);
        if (rzAxis != null) rzAxis.forceSetWorldPosition(basePosition);
        if (sxAxis != null) sxAxis.forceSetWorldPosition(basePosition);
        if (syAxis != null) syAxis.forceSetWorldPosition(basePosition);
        if (szAxis != null) szAxis.forceSetWorldPosition(basePosition);
    }

    public Quaternionf getCurrentRotation() {
        return new Quaternionf(currentRotation);
    }
    public Vec3 getCurrentScale() {
        return currentScale;
    }

    private Consumer<DefaultTransformer> createTransformLogic(Direction direction, InteractionMode mode) {
        return (transformer) -> {
            ObjModelShape shape = (ObjModelShape) transformer.getShape();
            RayModelIntersection.HitResult hitResult = shape.isPlayerLookingAt();
            boolean isHit = hitResult.hit;
            Minecraft mc = Minecraft.getInstance();
            boolean isMouseDown = mc.options.keyUse.isDown();

            float partialTicks = transformer.getTickDelta();
            assert mc.player != null;
            Vec3 eyePos = mc.player.getEyePosition(partialTicks);

            if(mode == InteractionMode.MOVE || mode == InteractionMode.SCALE) updateOrientation(eyePos);

            if (isHit && isMouseDown && grabbingAxis == null) {
                grabbingAxis = shape;
                if (mc.player != null) {
                    this.grabDistance = basePosition.distanceTo(mc.player.getEyePosition(transformer.getTickDelta()));
                }

                if (mode == InteractionMode.ROTATE) {
                    handleRotationGrabStart(direction, mc, transformer.getTickDelta());
                }

                if (mode == InteractionMode.SCALE || mode == InteractionMode.MOVE) {
                    this.initialGrabProjection = calculateProjectionLength(direction, mc, transformer.getTickDelta());
                    this.initialObjectScale = currentScale;
                }
            }

            if (!isMouseDown) {
                grabbingAxis = null;
            }

            Color originalColor = getOriginalColor(shape, mode, direction);
            if (grabbingAxis == shape) {
                shape.setBaseColor(COLOR_HIGHLIGHT);
            } else if (isHit && grabbingAxis == null) {
                shape.setBaseColor(COLOR_HIGHLIGHT);
            } else {
                shape.setBaseColor(originalColor);
            }

            if (grabbingAxis == shape) {
                if (mode == InteractionMode.MOVE) {
                    handleDragging(transformer, direction, mc);
                } else if (mode == InteractionMode.ROTATE) {
                    handleRotationDragging(direction, mc, transformer.getTickDelta());
                } else if (mode == InteractionMode.SCALE) {
                    handleScaleDragging(transformer, direction, mc);
                }
            }
        };
    }

    private Color getOriginalColor(ObjModelShape shape, InteractionMode mode, Direction direction) {
        if (mode == InteractionMode.MOVE) {
            return switch (direction) {
                case X -> COLOR_X;
                case Y -> COLOR_Y;
                case Z -> COLOR_Z;
            };
        } else if (mode == InteractionMode.ROTATE) {
            return switch (direction) {
                case X -> COLOR_RX;
                case Y -> COLOR_RY;
                case Z -> COLOR_RZ;
            };
        } else if (mode == InteractionMode.SCALE) {
            return switch (direction) {
                case X -> COLOR_SX;
                case Y -> COLOR_SY;
                case Z -> COLOR_SZ;
            };
        }
        return COLOR_HIGHLIGHT;
    }

    private void updateOrientation(Vec3 eyePos) {
        if (eyePos == null) return;

        float s = scaleByDistance(basePosition,0.1f,1f);
        Vec3 vec3 = new Vec3(s,s,s);
        if (myAxis != null) myAxis.setWorldScale(vec3);
        if (mxAxis != null) mxAxis.setWorldScale(vec3);
        if (mzAxis != null) mzAxis.setWorldScale(vec3);

        if (rxAxis != null) rxAxis.setWorldScale(vec3);
        if (ryAxis != null) ryAxis.setWorldScale(vec3);
        if (rzAxis != null) rzAxis.setWorldScale(vec3);

        if (sxAxis != null) sxAxis.setWorldScale(vec3);
        if (syAxis != null) syAxis.setWorldScale(vec3);
        if (szAxis != null) szAxis.setWorldScale(vec3);

        Vec3 rel = eyePos.subtract(basePosition);
        int newSignX = rel.x >= 0 ? 1 : -1;
        int newSignY = rel.y >= 0 ? 1 : -1;
        int newSignZ = rel.z >= 0 ? 1 : -1;

        if (newSignX == signX && newSignY == signY && newSignZ == signZ) {
            return;
        }

        signX = newSignX;
        signY = newSignY;
        signZ = newSignZ;
        if (myAxis != null) myAxis.setWorldRotation(new Vector3f().add(signY == 1 ? 0 : 180, 0, 0));
        if (mxAxis != null) mxAxis.setWorldRotation(new Vector3f().add(0, 0, signX == 1 ? -90 : 90));
        if (mzAxis != null) mzAxis.setWorldRotation(new Vector3f().add(signZ == 1 ? 90 : -90, 0, 0));

        if (syAxis != null) syAxis.setWorldRotation(new Vector3f().add(signY == 1 ? 0 : 180, 0, 0));
        if (sxAxis != null) sxAxis.setWorldRotation(new Vector3f().add(0, 0, signX == 1 ? -90 : 90));
        if (szAxis != null) szAxis.setWorldRotation(new Vector3f().add(signZ == 1 ? 90 : -90, 0, 0));
    }

    private double calculateProjectionLength(Direction direction, Minecraft mc, float delta) {
        assert mc.player != null;
        Vec3 eyePos = mc.player.getEyePosition(delta);
        Vec3 lookDir = mc.player.getLookAngle();
        Vec3 targetPos = eyePos.add(lookDir.scale(this.grabDistance));

        Vec3 axisVector = switch (direction) {
            case X -> new Vec3(signX, 0, 0);
            case Y -> new Vec3(0, signY, 0);
            case Z -> new Vec3(0, 0, signZ);
        };

        Vec3 relativeVec = targetPos.subtract(basePosition);
        return relativeVec.dot(axisVector);
    }

    private void handleDragging(DefaultTransformer transformer, Direction direction, Minecraft mc) {
        if (mc.player == null) return;

        double currentProjection = calculateProjectionLength(direction, mc, transformer.getTickDelta());
        double deltaProjection = currentProjection - initialGrabProjection;

        if (Math.abs(deltaProjection) > 0.0001) {
            Vec3 axisVector = switch (direction) {
                case X -> new Vec3(signX, 0, 0);
                case Y -> new Vec3(0, signY, 0);
                case Z -> new Vec3(0, 0, signZ);
            };

            Vec3 moveStep = axisVector.scale(deltaProjection);
            setBasePosition(basePosition.add(moveStep));

            this.initialGrabProjection = calculateProjectionLength(direction, mc, transformer.getTickDelta());
        }
    }

    private void handleScaleDragging(DefaultTransformer transformer, Direction direction, Minecraft mc) {
        if (mc.player == null) return;

        double currentProjection = calculateProjectionLength(direction, mc, transformer.getTickDelta());
        double deltaProjection = currentProjection - initialGrabProjection;

        float sensitivity = 0.2f;
        float deltaScale = (float) deltaProjection * sensitivity;

        switch (direction) {
            case X -> currentScale = new Vec3(Math.max(0.01, initialObjectScale.x() + deltaScale), currentScale.y(), currentScale.z());
            case Y -> currentScale = new Vec3(currentScale.x(), Math.max(0.01, initialObjectScale.y() + deltaScale), currentScale.z());
            case Z -> currentScale = new Vec3(currentScale.x(), currentScale.y(), Math.max(0.01, initialObjectScale.z() + deltaScale));
        }
    }

    private void handleRotationGrabStart(Direction direction, Minecraft mc, float delta) {
        if (mc.player == null) return;
        this.initialObjectRotation = new Quaternionf(currentRotation);
        this.initialGrabAngle = calculateMouseAngle(direction, mc, delta);
    }

    private void handleRotationDragging(Direction direction, Minecraft mc, float delta) {
        float currentAngle = calculateMouseAngle(direction, mc, delta);
        float deltaAngle = currentAngle - initialGrabAngle;

        Vector3f axis = switch (direction) {
            case X -> new Vector3f(1, 0, 0);
            case Y -> new Vector3f(0, -1, 0);
            case Z -> new Vector3f(0, 0, 1);
        };

        Quaternionf deltaRotation = new Quaternionf().rotateAxis(deltaAngle, axis);

        currentRotation = new Quaternionf(deltaRotation).mul(initialObjectRotation).normalize();
    }

    private float calculateMouseAngle(Direction direction, Minecraft mc, float delta) {
        assert mc.player != null;
        Vec3 eyePos = mc.player.getEyePosition(delta);
        Vec3 lookDir = mc.player.getLookAngle();

        Vec3 planeNormal = switch (direction) {
            case X -> new Vec3(1, 0, 0);
            case Y -> new Vec3(0, 1, 0);
            case Z -> new Vec3(0, 0, 1);
        };

        double denominator = lookDir.dot(planeNormal);
        if (Math.abs(denominator) < 0.000001) {
            return 0;
        }

        double t = (basePosition.subtract(eyePos)).dot(planeNormal) / denominator;
        Vec3 intersection = eyePos.add(lookDir.scale(t));
        Vec3 relative = intersection.subtract(basePosition);

        return switch (direction) {
            case X -> (float) Math.atan2(relative.z, relative.y);
            case Y -> (float) Math.atan2(relative.z, relative.x);
            case Z -> (float) Math.atan2(relative.y, relative.x);
        };
    }
    private enum InteractionMode { MOVE, ROTATE, SCALE }
    private enum Direction { X, Y, Z }
}