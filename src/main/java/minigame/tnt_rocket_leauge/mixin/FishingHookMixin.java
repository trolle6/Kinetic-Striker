package minigame.tnt_rocket_leauge.mixin;

import minigame.tnt_rocket_leauge.game.Game;
import minigame.tnt_rocket_leauge.game.GameManager;
import minigame.tnt_rocket_leauge.game.Team;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public class FishingHookMixin {
    
    // Simple approach: fishing hook hits TNT like a projectile!
    @Inject(method = "onHitEntity", at = @At("HEAD"))
    private void onHitEntity(EntityHitResult result, CallbackInfo ci) {
        FishingHook hook = (FishingHook) (Object) this;
        
        if (result.getEntity() instanceof PrimedTnt tnt) {
            if (GameManager.getInstance().isGameBall(tnt)) {
                
                // Record who hit it
                if (hook.getOwner() instanceof ServerPlayer player) {
                    Game game = GameManager.getInstance().getCurrentGame();
                    if (game != null) {
                        Team team = game.getPlayerTeam(player.getUUID());
                        if (team != null) {
                            GameManager.getInstance().recordBallHit(tnt, player.getUUID(), team);
                        }
                    }
                }
                
                // Hit it like a projectile!
                Vec3 hookVel = hook.getDeltaMovement();
                Vec3 currentVel = tnt.getDeltaMovement();
                
                // Apply hook velocity × 2.0 (like other projectiles)
                double amplification = 2.0;
                tnt.setDeltaMovement(
                    currentVel.x + hookVel.x * amplification,
                    currentVel.y + hookVel.y * amplification * 0.5,
                    currentVel.z + hookVel.z * amplification
                );
            }
        }
    }
}

