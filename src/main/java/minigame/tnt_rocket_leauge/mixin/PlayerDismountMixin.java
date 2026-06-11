package minigame.tnt_rocket_leauge.mixin;

import minigame.tnt_rocket_leauge.game.GameManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class PlayerDismountMixin {
    
    @Inject(method = "stopRiding", at = @At("HEAD"), cancellable = true)
    private void preventDismount(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        
        // Prevent players from dismounting Allays during active game
        if (entity instanceof Player && GameManager.getInstance().isGameActive()) {
            if (entity.getVehicle() instanceof Allay) {
                var game = GameManager.getInstance().getCurrentGame();
                if (game != null && game.getPlayerTeam(entity.getUUID()) != null) {
                    // Cancel dismount
                    ci.cancel();
                }
            }
        }
    }
}


