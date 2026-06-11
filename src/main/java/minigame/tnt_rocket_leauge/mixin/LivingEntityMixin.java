package minigame.tnt_rocket_leauge.mixin;

import minigame.tnt_rocket_leauge.game.GameManager;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    
    @Unique
    private int elytraBoostCooldown = 0;
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        if (elytraBoostCooldown > 0) {
            elytraBoostCooldown--;
        }
        
        // Enhanced elytra controls during game
        if (GameManager.getInstance().isGameActive() && entity.isFallFlying()) {
            // Elytra flight mechanics are enhanced during gameplay
            // Boost is handled through the boost item
        }
    }
}

