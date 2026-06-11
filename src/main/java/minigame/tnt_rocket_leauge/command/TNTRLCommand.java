package minigame.tnt_rocket_leauge.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import minigame.tnt_rocket_leauge.game.*;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public class TNTRLCommand {
    
    private static BlockPos arenaCorner1;
    private static BlockPos arenaCorner2;
    private static AABB redGoal;
    private static AABB blueGoal;
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tntrl")
            .then(Commands.literal("quickstart")
                .requires(source -> Commands.LEVEL_GAMEMASTERS.check(source.permissions()))
                .then(Commands.literal("2v2")
                    .executes(ctx -> quickStart(ctx, GameMode.MODE_2V2)))
                .then(Commands.literal("3v3")
                    .executes(ctx -> quickStart(ctx, GameMode.MODE_3V3)))
                .executes(ctx -> quickStart(ctx, GameMode.MODE_3V3))) // Default to 3v3
            
            .then(Commands.literal("start")
                .requires(source -> Commands.LEVEL_GAMEMASTERS.check(source.permissions()))
                .executes(TNTRLCommand::startGame))
            
            .then(Commands.literal("stop")
                .requires(source -> Commands.LEVEL_GAMEMASTERS.check(source.permissions()))
                .executes(TNTRLCommand::stopGame))
            
            .then(Commands.literal("join")
                .then(Commands.literal("red")
                    .then(Commands.literal("attacker")
                        .executes(ctx -> joinTeam(ctx, Team.RED, minigame.tnt_rocket_leauge.game.PlayerRole.ATTACKER)))
                    .then(Commands.literal("goalie")
                        .executes(ctx -> joinTeam(ctx, Team.RED, minigame.tnt_rocket_leauge.game.PlayerRole.GOALIE)))
                    .executes(ctx -> joinTeam(ctx, Team.RED, minigame.tnt_rocket_leauge.game.PlayerRole.ATTACKER)))
                .then(Commands.literal("blue")
                    .then(Commands.literal("attacker")
                        .executes(ctx -> joinTeam(ctx, Team.BLUE, minigame.tnt_rocket_leauge.game.PlayerRole.ATTACKER)))
                    .then(Commands.literal("goalie")
                        .executes(ctx -> joinTeam(ctx, Team.BLUE, minigame.tnt_rocket_leauge.game.PlayerRole.GOALIE)))
                    .executes(ctx -> joinTeam(ctx, Team.BLUE, minigame.tnt_rocket_leauge.game.PlayerRole.ATTACKER))))
            
            .then(Commands.literal("leave")
                .executes(TNTRLCommand::leaveGame))
            
            .then(Commands.literal("setarena")
                .requires(source -> Commands.LEVEL_GAMEMASTERS.check(source.permissions()))
                .then(Commands.argument("corner1", BlockPosArgument.blockPos())
                    .then(Commands.argument("corner2", BlockPosArgument.blockPos())
                        .executes(TNTRLCommand::setArena))))
            
            .then(Commands.literal("setgoal")
                .requires(source -> Commands.LEVEL_GAMEMASTERS.check(source.permissions()))
                .then(Commands.literal("red")
                    .then(Commands.argument("corner1", BlockPosArgument.blockPos())
                        .then(Commands.argument("corner2", BlockPosArgument.blockPos())
                            .executes(ctx -> setGoal(ctx, Team.RED)))))
                .then(Commands.literal("blue")
                    .then(Commands.argument("corner1", BlockPosArgument.blockPos())
                        .then(Commands.argument("corner2", BlockPosArgument.blockPos())
                            .executes(ctx -> setGoal(ctx, Team.BLUE))))))
            
            .then(Commands.literal("visualize")
                .requires(source -> Commands.LEVEL_GAMEMASTERS.check(source.permissions()))
                .executes(TNTRLCommand::visualizeGoals))
            
            .then(Commands.literal("score")
                .executes(TNTRLCommand::showScore))
        );
    }
    
    private static int startGame(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerLevel level = ctx.getSource().getLevel();
        
        if (arenaCorner1 == null || arenaCorner2 == null) {
            ctx.getSource().sendFailure(Component.literal("Arena not set! Use /tntrl setarena first"));
            return 0;
        }
        
        if (redGoal == null || blueGoal == null) {
            ctx.getSource().sendFailure(Component.literal("Goals not set! Use /tntrl setgoal first"));
            return 0;
        }
        
        if (GameManager.getInstance().isGameActive()) {
            ctx.getSource().sendFailure(Component.literal("A game is already active!"));
            return 0;
        }
        
        Arena arena = new Arena(arenaCorner1, arenaCorner2, redGoal, blueGoal);
        GameManager.getInstance().startGame(level, arena, GameMode.MODE_3V3);
        
        ctx.getSource().sendSuccess(() -> Component.literal("Game started!").withStyle(ChatFormatting.GREEN), true);
        return 1;
    }
    
    private static int stopGame(CommandContext<CommandSourceStack> ctx) {
        if (!GameManager.getInstance().isGameActive()) {
            ctx.getSource().sendFailure(Component.literal("No active game!"));
            return 0;
        }
        
        GameManager.getInstance().endGame();
        ctx.getSource().sendSuccess(() -> Component.literal("Game stopped!").withStyle(ChatFormatting.RED), true);
        return 1;
    }
    
    private static int joinTeam(CommandContext<CommandSourceStack> ctx, Team team, minigame.tnt_rocket_leauge.game.PlayerRole role) throws CommandSyntaxException {
        if (!GameManager.getInstance().isGameActive()) {
            ctx.getSource().sendFailure(Component.literal("No active game!"));
            return 0;
        }
        
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        GameManager.getInstance().addPlayerToTeam(player, team, role);
        
        return 1;
    }
    
    private static int leaveGame(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        GameManager.getInstance().removePlayer(player);
        
        ctx.getSource().sendSuccess(() -> Component.literal("You left the game!"), false);
        return 1;
    }
    
    private static int setArena(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        arenaCorner1 = BlockPosArgument.getBlockPos(ctx, "corner1");
        arenaCorner2 = BlockPosArgument.getBlockPos(ctx, "corner2");
        
        ctx.getSource().sendSuccess(() -> Component.literal("Arena set from " + 
            arenaCorner1.toShortString() + " to " + arenaCorner2.toShortString())
            .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }
    
    private static int setGoal(CommandContext<CommandSourceStack> ctx, Team team) throws CommandSyntaxException {
        BlockPos corner1 = BlockPosArgument.getBlockPos(ctx, "corner1");
        BlockPos corner2 = BlockPosArgument.getBlockPos(ctx, "corner2");
        
        AABB goal = AABB.encapsulatingFullBlocks(corner1, corner2);
        
        if (team == Team.RED) {
            redGoal = goal;
        } else {
            blueGoal = goal;
        }
        
        ctx.getSource().sendSuccess(() -> Component.literal(team.name() + " goal set from " + 
            corner1.toShortString() + " to " + corner2.toShortString())
            .withStyle(team.getColor()), false);
        return 1;
    }
    
    private static int showScore(CommandContext<CommandSourceStack> ctx) {
        if (!GameManager.getInstance().isGameActive()) {
            ctx.getSource().sendFailure(Component.literal("No active game!"));
            return 0;
        }
        
        Game game = GameManager.getInstance().getCurrentGame();
        int redScore = game.getScores().get(Team.RED);
        int blueScore = game.getScores().get(Team.BLUE);
        
        ctx.getSource().sendSuccess(() -> Component.literal("Score: ")
            .append(Component.literal("RED " + redScore).withStyle(ChatFormatting.RED))
            .append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("BLUE " + blueScore).withStyle(ChatFormatting.BLUE)), 
            false);
        return 1;
    }
    
    private static int quickStart(CommandContext<CommandSourceStack> ctx, GameMode mode) throws CommandSyntaxException {
        ServerLevel level = ctx.getSource().getLevel();
        BlockPos playerPos = BlockPos.containing(ctx.getSource().getPosition());
        
        // Get arena dimensions from mode
        int halfWidth = mode.getArenaWidth() / 2;
        int halfLength = mode.getArenaLength() / 2;
        int height = mode.getArenaHeight();
        int minY = 10;
        int maxY = minY + height;
        
        // Create arena with mode-specific dimensions
        BlockPos corner1 = playerPos.offset(-halfWidth, minY, -halfLength);
        BlockPos corner2 = playerPos.offset(halfWidth, maxY, halfLength);
        
        // Goals (8 blocks wide, 6 blocks tall, 3 blocks deep) - centered in arena height
        int goalMinY = minY + (height / 2) - 3;
        int goalMaxY = goalMinY + 6;
        
        BlockPos redGoalCorner1 = playerPos.offset(-halfWidth, goalMinY, -4);
        BlockPos redGoalCorner2 = playerPos.offset(-halfWidth + 3, goalMaxY, 4);
        
        BlockPos blueGoalCorner1 = playerPos.offset(halfWidth - 3, goalMinY, -4);
        BlockPos blueGoalCorner2 = playerPos.offset(halfWidth, goalMaxY, 4);
        
        // Set arena and goals - expand AABB by 2 blocks in all directions to include powder snow
        arenaCorner1 = corner1;
        arenaCorner2 = corner2;
        redGoal = AABB.encapsulatingFullBlocks(redGoalCorner1, redGoalCorner2).inflate(2.0);
        blueGoal = AABB.encapsulatingFullBlocks(blueGoalCorner1, blueGoalCorner2).inflate(2.0);
        
        // Build goal markers
        buildGoalMarkers(level, redGoalCorner1, redGoalCorner2, true);
        buildGoalMarkers(level, blueGoalCorner1, blueGoalCorner2, false);
        
        // Build platform under spawn point - size varies with mode
        int platformSize = mode == GameMode.MODE_3V3 ? 10 : 7;
        BlockPos centerGround = BlockPos.containing(
            (corner1.getX() + corner2.getX()) / 2.0,
            Math.min(corner1.getY(), corner2.getY()),
            (corner1.getZ() + corner2.getZ()) / 2.0
        );
        for (int x = -platformSize; x <= platformSize; x++) {
            for (int z = -platformSize; z <= platformSize; z++) {
                level.setBlock(centerGround.offset(x, 0, z), Blocks.SMOOTH_STONE.defaultBlockState(), 3);
            }
        }
        
        // BUILD LOBBY WITH PISTON HEADS!
        // Lobby is 10 blocks away from arena, at ground level
        BlockPos lobbyCenter = centerGround.offset(0, 0, halfLength + 15);
        
        // Build lobby platform (15x15 smooth stone)
        for (int x = -7; x <= 7; x++) {
            for (int z = -7; z <= 7; z++) {
                level.setBlock(lobbyCenter.offset(x, -1, z), Blocks.SMOOTH_STONE.defaultBlockState(), 3);
            }
        }
        
        // Place TEAM ENTRANCE BANNERS!
        // RED team entrance (on left side)
        BlockPos redEntranceBase = lobbyCenter.offset(-5, 0, 0);
        level.setBlock(redEntranceBase, Blocks.RED_WOOL.defaultBlockState(), 3);
        level.setBlock(redEntranceBase.offset(0, 1, 0), Blocks.RED_BANNER.defaultBlockState(), 3);
        level.setBlock(redEntranceBase.offset(0, 2, 0), Blocks.RED_BANNER.defaultBlockState(), 3);
        
        // Add red wall banners on the sides for easier detection
        level.setBlock(redEntranceBase.offset(0, 1, -1), Blocks.RED_WALL_BANNER.defaultBlockState()
            .setValue(net.minecraft.world.level.block.WallBannerBlock.FACING, Direction.SOUTH), 3);
        level.setBlock(redEntranceBase.offset(0, 1, 1), Blocks.RED_WALL_BANNER.defaultBlockState()
            .setValue(net.minecraft.world.level.block.WallBannerBlock.FACING, Direction.NORTH), 3);
        
        // BLUE team entrance (on right side)
        BlockPos blueEntranceBase = lobbyCenter.offset(5, 0, 0);
        level.setBlock(blueEntranceBase, Blocks.BLUE_WOOL.defaultBlockState(), 3);
        level.setBlock(blueEntranceBase.offset(0, 1, 0), Blocks.BLUE_BANNER.defaultBlockState(), 3);
        level.setBlock(blueEntranceBase.offset(0, 2, 0), Blocks.BLUE_BANNER.defaultBlockState(), 3);
        
        // Add blue wall banners on the sides
        level.setBlock(blueEntranceBase.offset(0, 1, -1), Blocks.BLUE_WALL_BANNER.defaultBlockState()
            .setValue(net.minecraft.world.level.block.WallBannerBlock.FACING, Direction.SOUTH), 3);
        level.setBlock(blueEntranceBase.offset(0, 1, 1), Blocks.BLUE_WALL_BANNER.defaultBlockState()
            .setValue(net.minecraft.world.level.block.WallBannerBlock.FACING, Direction.NORTH), 3);
        
        // Place DISPENSER 15 blocks above ground (for ball spawning)
        int groundY = Math.min(corner1.getY(), corner2.getY()) + 1;
        BlockPos dispenserPos = BlockPos.containing(
            (corner1.getX() + corner2.getX()) / 2.0,
            groundY + 15, // 15 blocks above ground
            (corner1.getZ() + corner2.getZ()) / 2.0
        );
        level.setBlock(dispenserPos, Blocks.DISPENSER.defaultBlockState()
            .setValue(net.minecraft.world.level.block.DispenserBlock.FACING, Direction.DOWN), 3);
        
        // Fill dispenser with TNT
        if (level.getBlockEntity(dispenserPos) instanceof net.minecraft.world.level.block.entity.DispenserBlockEntity dispenser) {
            dispenser.setItem(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.TNT, 64));
        }
        
        // Store lobby info in GameManager (for piston trigger later)
        GameManager.getInstance().setLobby(lobbyCenter, mode, corner1, corner2, redGoal, blueGoal);
        
        // START THE GAME IMMEDIATELY (manual join system)
        Arena arena = new Arena(corner1, corner2, redGoal, blueGoal);
        GameManager.getInstance().startGame(level, arena, mode);
        
        ctx.getSource().sendSuccess(() -> 
            Component.literal("⚽ Game lobby created! [" + mode.getDisplayName() + "]\n")
                .append(Component.literal("Arena: " + mode.getArenaWidth() + "x" + mode.getArenaLength() + "x" + mode.getArenaHeight() + "\n").withStyle(ChatFormatting.GREEN))
                .append(Component.literal("🚪 Walk through RED or BLUE banners to join!\n").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .append(Component.literal("(Random class assigned automatically)\n").withStyle(ChatFormatting.GRAY))
                .append(Component.literal("Teams: " + mode.getMaxPlayersPerTeam() + "v" + mode.getMaxPlayersPerTeam()).withStyle(ChatFormatting.AQUA)),
            true);
        return 1;
    }
    
    private static void buildGoalMarkers(ServerLevel level, BlockPos corner1, BlockPos corner2, boolean isRed) {
        var wool = isRed ? Blocks.RED_WOOL : Blocks.BLUE_WOOL;
        
        int minX = Math.min(corner1.getX(), corner2.getX());
        int maxX = Math.max(corner1.getX(), corner2.getX());
        int minY = Math.min(corner1.getY(), corner2.getY());
        int maxY = Math.max(corner1.getY(), corner2.getY());
        int minZ = Math.min(corner1.getZ(), corner2.getZ());
        int maxZ = Math.max(corner1.getZ(), corner2.getZ());
        
        // Build goal: Wool box with powder snow layer in front
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    
                    // Fill entire box with wool first
                    level.setBlock(pos, wool.defaultBlockState(), 3);
                }
            }
        }
        
        // Add powder snow layer in front (on the X-axis edge that's in the middle of the field)
        // Determine which side is "front" based on position
        int frontX = (minX + maxX) / 2 > 0 ? minX : maxX; // Side closer to center
        
        for (int y = minY + 1; y < maxY; y++) {
            for (int z = minZ + 1; z < maxZ; z++) {
                // Add powder snow in front of the wool
                BlockPos snowPos = new BlockPos(frontX, y, z).relative(
                    frontX == minX ? net.minecraft.core.Direction.WEST : net.minecraft.core.Direction.EAST);
                level.setBlock(snowPos, Blocks.POWDER_SNOW.defaultBlockState(), 3);
            }
        }
    }
    
    private static int visualizeGoals(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        
        if (redGoal == null || blueGoal == null) {
            ctx.getSource().sendFailure(Component.literal("Goals not set! Use /tntrl setgoal first"));
            return 0;
        }
        
        // Build markers for existing goals
        BlockPos redCorner1 = BlockPos.containing(redGoal.minX, redGoal.minY, redGoal.minZ);
        BlockPos redCorner2 = BlockPos.containing(redGoal.maxX, redGoal.maxY, redGoal.maxZ);
        buildGoalMarkers(level, redCorner1, redCorner2, true);
        
        BlockPos blueCorner1 = BlockPos.containing(blueGoal.minX, blueGoal.minY, blueGoal.minZ);
        BlockPos blueCorner2 = BlockPos.containing(blueGoal.maxX, blueGoal.maxY, blueGoal.maxZ);
        buildGoalMarkers(level, blueCorner1, blueCorner2, false);
        
        ctx.getSource().sendSuccess(() -> Component.literal("Goals visualized with colored wool!").withStyle(ChatFormatting.GREEN), false);
        return 1;
    }
}

