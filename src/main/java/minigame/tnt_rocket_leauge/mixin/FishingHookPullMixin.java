package minigame.tnt_rocket_leauge.mixin;

import minigame.tnt_rocket_leauge.game.GameManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public class FishingHookPullMixin {
    
    // Reduce pull force on TNT ball
    @Inject(method = "pullEntity", at = @At("HEAD"), cancellable = true)
    private void onPullEntity(Entity entity, CallbackInfo ci) {
        FishingHook hook = (FishingHook) (Object) this;
        
        // Check if pulling a game ball
        if (entity instanceof PrimedTnt tnt && GameManager.getInstance().isGameBall(tnt)) {
            Entity owner = hook.getOwner();
            if (owner == null) {
                ci.cancel();
                return;
            }
            
            // Calculate gentle pull force
            double pullDistance = owner.position().distanceTo(entity.position());
            if (pullDistance < 0.01) {
                ci.cancel();
                return;
            }
            
            // Direction from TNT to player
            Vec3 pullDirection = owner.position().subtract(entity.position()).normalize();
            
            // MODERATE PULL - decent control (0.25 blocks/tick = 5 blocks/sec)
            double pullStrength = 0.25;
            Vec3 pullForce = pullDirection.scale(pullStrength);
            
            // Apply the gentle pull
            Vec3 currentVel = entity.getDeltaMovement();
            entity.setDeltaMovement(currentVel.add(pullForce));
            
            // Cancel vanilla pull (which is much stronger)
            ci.cancel();
        }
    }
}

