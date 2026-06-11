package minigame.tnt_rocket_leauge.mixin;

import minigame.tnt_rocket_leauge.game.GameManager;
import minigame.tnt_rocket_leauge.game.PlayerRole;
import minigame.tnt_rocket_leauge.game.Team;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(Player.class)
public class PlayerMovementMixin {
    
    private static final Random RANDOM = new Random();
    private long tntrl$lastBannerCheck = 0;
    
    @Inject(method = "tick", at = @At("TAIL"))
    private void onPlayerTick(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        
        // Only check every 20 ticks (1 second) to avoid spam
        long currentTime = player.level().getGameTime();
        if (currentTime - tntrl$lastBannerCheck < 20) {
            return;
        }
        tntrl$lastBannerCheck = currentTime;
        
        // Check if player is near a banner
        GameManager gameManager = GameManager.getInstance();
        if (gameManager.getLobbyCenter() == null) {
            return; // No lobby set up
        }
        
        // Check if player already in a team
        var game = gameManager.getCurrentGame();
        if (game != null && game.getPlayerTeam(serverPlayer.getUUID()) != null) {
            return; // Already in a team
        }
        
        // Check blocks around player for banners
        BlockPos playerPos = serverPlayer.blockPosition();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 2; dy++) { // Check head level too
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos checkPos = playerPos.offset(dx, dy, dz);
                    BlockState state = serverPlayer.level().getBlockState(checkPos);
                    
                    // Check for red or blue wall banners
                    if (state.is(Blocks.RED_WALL_BANNER) || state.is(Blocks.RED_BANNER)) {
                        // Join RED team!
                        joinTeamFromBanner(serverPlayer, Team.RED);
                        return;
                    } else if (state.is(Blocks.BLUE_WALL_BANNER) || state.is(Blocks.BLUE_BANNER)) {
                        // Join BLUE team!
                        joinTeamFromBanner(serverPlayer, Team.BLUE);
                        return;
                    }
                }
            }
        }
    }
    
    private void joinTeamFromBanner(ServerPlayer player, Team team) {
        var game = GameManager.getInstance().getCurrentGame();
        if (game == null || !game.isActive()) {
            return;
        }
        
        // Random role assignment
        PlayerRole role = RANDOM.nextBoolean() ? PlayerRole.ATTACKER : PlayerRole.GOALIE;
        
        // Add to team
        game.addPlayerToTeam(player, team, role);
        
        // Send feedback
        player.sendSystemMessage(
            net.minecraft.network.chat.Component.literal("✓ You joined the " + team.name() + " team as a " + role.getDisplayName() + "!")
                .withStyle(team.getColor())
        );
    }
}

