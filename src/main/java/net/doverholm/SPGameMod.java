package net.doverholm;

import net.doverholm.util.CountdownManager;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
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
					Component.literal("§eVälkommen till servern"),
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

			//scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly(CountdownManager.getFormattedTimeLeft()), board).set(8);
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
	}
}