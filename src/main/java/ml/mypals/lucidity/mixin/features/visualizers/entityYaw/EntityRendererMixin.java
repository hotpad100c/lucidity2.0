package ml.mypals.lucidity.mixin.features.visualizers.entityYaw;

// 1.21.11 把调试碰撞箱渲染整体换成了 Gizmos 体系：EntityRenderDispatcher.renderHitboxes
// 和 HitboxesRenderState 都不存在了，ShapeRenderer 的绘制辅助也一并移除。
// 这个功能（实体朝向可视化）需要重写到 EntityHitboxDebugRenderer / Gizmos 上，
// 在那之前先在 1.21.11 上停用，避免拖住整个构建。
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Minecraft.class)
public class EntityRendererMixin {
}
