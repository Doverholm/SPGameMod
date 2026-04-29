package net.doverholm;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.doverholm.rank.Rank;
import net.doverholm.rank.RankManager;
import net.doverholm.util.CountdownManager;
import net.doverholm.util.NameTagUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SPGameMod implements ModInitializer {
    public static final String MOD_ID = "spgamemod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final String COUNTDOWN_ENTRY = "\u00A77\u00A7k\u00A70";
    private int tick;

    @Override
    public void onInitialize() {
        RankManager.load();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Scoreboard scoreboard = server.getScoreboard();

            Objective existing = scoreboard.getObjective("board");
            if (existing != null) {
                scoreboard.removeObjective(existing);
            }

            Objective board = scoreboard.addObjective(
                    "board",
                    ObjectiveCriteria.DUMMY,
                    Component.literal("\u00A7eSeason 1 Java"),
                    ObjectiveCriteria.RenderType.INTEGER,
                    true,
                    null
            );
            scoreboard.setDisplayObjective(DisplaySlot.SIDEBAR, board);

            scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly("\u00A70"), board).set(10);
            scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly(" Purge slutar om:"), board).set(9);

            PlayerTeam countdownTeam = scoreboard.getPlayerTeam("countdownTeam");
            if (countdownTeam == null) {
                countdownTeam = scoreboard.addPlayerTeam("countdownTeam");
                countdownTeam.setDisplayName(Component.literal(" "));
            }

            scoreboard.addPlayerToTeam(COUNTDOWN_ENTRY, countdownTeam);
            scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly(COUNTDOWN_ENTRY), board).set(8);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tick++;

            if (tick < 20) {
                return;
            }

            Scoreboard scoreboard = server.getScoreboard();
            PlayerTeam countdownTeam = scoreboard.getPlayerTeam("countdownTeam");
            if (countdownTeam != null) {
                countdownTeam.setPlayerPrefix(Component.literal(" "));
                countdownTeam.setPlayerSuffix(Component.literal(CountdownManager.getFormattedTimeLeft()));
            }

            tick = 0;
        });

        ServerPlayerEvents.JOIN.register(player -> {
            showJoinTitle(player);
            NameTagUtil.updateName(player);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                NameTagUtil.updateName(handler.getPlayer()));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("setrank")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("rank", StringArgumentType.word())
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            String rankName = StringArgumentType.getString(context, "rank");

                                            Rank rank;
                                            try {
                                                rank = Rank.valueOf(rankName.toUpperCase());
                                            } catch (IllegalArgumentException exception) {
                                                context.getSource().sendFailure(Component.literal(
                                                        "Unknown rank. Use PLAYER, TESTER, MODERATOR, ADMIN, DRAGONSLAYER or DEVELOPER."
                                                ));
                                                return 0;
                                            }

                                            RankManager.setRank(target.getUUID(), rank);
                                            NameTagUtil.updateName(target);

                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("Rank set for " + target.getName().getString()),
                                                    true
                                            );
                                            return 1;
                                        })))));
    }

    private static void showJoinTitle(ServerPlayer player) {
        String playerName = player.getName().getString();
        String message = "\u00A77Valkommen \u00A76" + playerName + "\u00A77!";

        player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("Season 1 Java")));
        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(message)));
        player.connection.send(new ClientboundSetTitlesAnimationPacket(30, 70, 20));
    }

    public static Component getRankPrefix(Rank rank) {
        return switch (rank) {
            case ADMIN -> Component.literal("[ADMIN] ").withStyle(ChatFormatting.RED);
            case MODERATOR -> Component.literal("[MOD] ").withStyle(ChatFormatting.BLUE);
            case DEVELOPER -> Component.literal("[DEV] ").withStyle(ChatFormatting.DARK_BLUE);
            case TESTER -> Component.literal("[TESTER] ").withStyle(ChatFormatting.GREEN);
            case OWNER -> Component.literal("[OWNER] ").withStyle(ChatFormatting.GOLD);
            case DRAGONSLAYER -> Component.literal("[Dragon Slayer] ").withStyle(ChatFormatting.DARK_PURPLE);
            default -> Component.empty();
        };
    }
}
