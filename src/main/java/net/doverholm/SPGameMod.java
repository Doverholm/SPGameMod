package net.doverholm;

import net.doverholm.util.CountdownManager;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SPGameMod implements ModInitializer {
	public static final String MOD_ID = "spgamemod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ServerTickEvents.END_SERVER_TICK.register((server -> {
			Scoreboard scoreboard = server.getScoreboard();

			Objective board = scoreboard.getObjective("board");
			if(board == null) {
				board = scoreboard.addObjective(
						"board",
						ObjectiveCriteria.DUMMY,
						Component.literal("§eVälkommen till servern"),
						ObjectiveCriteria.RenderType.INTEGER,
						true,
						null
				);
				scoreboard.setDisplayObjective(DisplaySlot.SIDEBAR, board);
			} else {
				for (ScoreHolder holder : scoreboard.getTrackedPlayers()) {
					scoreboard.resetSinglePlayerScore(holder, board);
				}
			}


			scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly("§0"), board).set(10);
			scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly("Purge slutar om:"), board).set(9);
			scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly(CountdownManager.getFormattedTimeLeft()), board).set(8);
		}));
	}
}