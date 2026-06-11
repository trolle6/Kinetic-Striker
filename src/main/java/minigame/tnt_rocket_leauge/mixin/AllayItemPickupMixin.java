package minigame.tnt_rocket_leauge.mixin;

import minigame.tnt_rocket_leauge.game.GameManager;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Allay.class)
public class AllayItemPickupMixin {
    
    // Prevent Allay from picking up items during game
    @Inject(method = "pickUpItem", at = @At("HEAD"), cancellable = true)
    private void preventItemPickup(ItemEntity itemEntity, CallbackInfo ci) {
        if (GameManager.getInstance().isGameActive()) {
            ci.cancel();
        }
    }
}


