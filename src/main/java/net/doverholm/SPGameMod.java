package net.doverholm;

import net.doverholm.util.CountdownManager;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.*;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SPGameMod implements ModInitializer {
	public static final String MOD_ID = "spgamemod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private int tick = 0;

	private static final String COUNTDOWN_ENTRY = "§7§k§0";

	@Override
	public void onInitialize() {

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			Scoreboard scoreboard = server.getScoreboard();

			Objective existing = scoreboard.getObjective("board");
			if (existing != null) scoreboard.removeObjective(existing);
			Objective board = scoreboard.addObjective(
					"board",
					ObjectiveCriteria.DUMMY,
					Component.literal("§eSeason 1 Java"),
					ObjectiveCriteria.RenderType.INTEGER,
					true,
					null
			);
			scoreboard.setDisplayObjective(DisplaySlot.SIDEBAR, board);

			scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly("§0"), board).set(10);
			scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly(" Purge slutar om:"), board).set(9);

			PlayerTeam team = scoreboard.getPlayerTeam("countdownTeam");
			if (team == null) {
				team = scoreboard.addPlayerTeam("countdownTeam");
				team.setDisplayName(Component.literal(" "));
			}

			scoreboard.addPlayerToTeam(COUNTDOWN_ENTRY, team);
			scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly(COUNTDOWN_ENTRY), board).set(8);
		});

		ServerTickEvents.END_SERVER_TICK.register((server -> {
			tick++;

			if (tick >= 20) {
				Scoreboard scoreboard = server.getScoreboard();
				PlayerTeam team = scoreboard.getPlayerTeam("countdownTeam");

				if (team != null) {
					team.setPlayerPrefix(Component.literal(" "));
					team.setPlayerSuffix(Component.literal(CountdownManager.getFormattedTimeLeft()));
				}

				tick = 0;
			}
		}));

		ServerPlayerEvents.JOIN.register(player -> {
			String playerName = player.getName().getString();
			String message = "§7Välkommen §6" + playerName + "!";

			player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("Season 1 Java")));

			player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(message)));

			player.connection.send(
				new ClientboundSetTitlesAnimationPacket(
						30, 70, 20
				)
			);
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("setrank")
					.requires(src -> src.hasPermissionLevel(2))
					.then(CommandManager.argument("player", EntityArgumentType.player())
							.then(CommandManager.argument("rank", StringArgumentType.word())
									.executes(context -> {

										ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
										String rankStr = StringArgumentType.getString(context, "rank");

										Rank rank = Rank.valueOf(rankStr.toUpperCase());

										RankManager.setRank(target.getUuid(), rank);
										NameTagUtil.updateName(target);

										context.getSource().sendFeedback(
												() -> Text.literal("Rank satt!"), false
										);

										return 1;
			}))));
		});


		ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {

			Rank rank = RankManager.getRank(sender.getUuid());

			Text prefix = switch (rank) {
				case ADMIN -> Text.literal("[ADMIN] ").formatted(Formatting.RED);
				case MODERATOR -> Text.literal("[MOD] ").formatted(Formatting.BLUE);
				default -> Text.literal("");
			};

			Text newMessage = Text.literal("")
					.append(prefix)
					.append(sender.getName())
					.append(Text.literal(": "))
					.append(message.getContent());

			sender.getServer().getPlayerManager().broadcast(newMessage, false);

			return false;
		});


		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			NameTagUtil.updateName(handler.getPlayer());
		});
	}
}