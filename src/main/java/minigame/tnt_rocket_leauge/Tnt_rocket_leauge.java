package minigame.tnt_rocket_leauge;

import minigame.tnt_rocket_leauge.command.TNTRLCommand;
import minigame.tnt_rocket_leauge.command.StructureCommand;
import minigame.tnt_rocket_leauge.structure.StructureManager;
import minigame.tnt_rocket_leauge.game.GameManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tnt_rocket_leauge implements ModInitializer {
    public static final String MOD_ID = "tnt_rocket_leauge";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing TNT Rocket League!");
        
        // Register items
        ModItems.initialize();
        
        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            TNTRLCommand.register(dispatcher);
            StructureCommand.register(dispatcher);
        });
        
        // Set up server reference for structure manager
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            StructureManager.getInstance().setServer(server);
        });
        
        // Tick game manager every server tick (for countdown without ball)
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            GameManager.getInstance().tickGame();
        });
        
        // Handle player death and respawn
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof net.minecraft.server.level.ServerPlayer player) {
                // Check if player died and respawn them in the game
                if (GameManager.getInstance().isGameActive()) {
                    var game = GameManager.getInstance().getCurrentGame();
                    if (game != null && game.getPlayerTeam(player.getUUID()) != null) {
                        // Player is in the game, respawn them
                        game.respawnPlayer(player);
                    }
                }
            }
        });
        
        LOGGER.info("TNT Rocket League initialized successfully!");
    }
}
