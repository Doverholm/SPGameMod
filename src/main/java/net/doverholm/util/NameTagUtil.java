package net.doverholm.util;

import net.doverholm.rank.Rank;
import net.doverholm.rank.RankManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

public class NameTagUtil {

    public static void updateName(ServerPlayer player) {
        if (player.getServer() == null) {
            return;
        }

        Scoreboard scoreboard = player.getServer().getScoreboard();
        Rank rank = RankManager.getRank(player.getUUID());
        String teamName = rank.name().toLowerCase();

        PlayerTeam team = scoreboard.getPlayerTeam(teamName);
        if (team == null) {
            team = scoreboard.addPlayerTeam(teamName);
        }

        team.setPlayerPrefix(getPrefix(rank));
        scoreboard.addPlayerToTeam(player.getScoreboardName(), team);
    }

    private static Component getPrefix(Rank rank) {
        return switch (rank) {
            case ADMIN -> Component.literal("[ADMIN] ").withStyle(ChatFormatting.RED);
            case MODERATOR -> Component.literal("[MOD] ").withStyle(ChatFormatting.BLUE);
            case DEVELOPER -> Component.literal("[DEV] ").withStyle(ChatFormatting.GOLD);
            case TESTER -> Component.literal("[TESTER] ").withStyle(ChatFormatting.GREEN);
            default -> Component.empty();
        };
    }
}
