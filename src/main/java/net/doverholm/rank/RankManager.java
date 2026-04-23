package net.doverholm.rank;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RankManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type RANK_MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();
    private static final File FILE = new File("config/ranks.json");

    private static Map<String, String> ranks = new HashMap<>();

    public static void load() {
        try {
            if (!FILE.exists()) {
                File parent = FILE.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }
                save();
                return;
            }

            FileReader reader = new FileReader(FILE);
            Map<String, String> data = GSON.fromJson(reader, RANK_MAP_TYPE);
            reader.close();

            if (data != null) {
                ranks.putAll(data);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            FileWriter writer = new FileWriter(FILE);
            GSON.toJson(ranks, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Rank getRank(UUID uuid) {
        String storedRank = ranks.getOrDefault(uuid.toString(), Rank.PLAYER.name());
        try {
            return Rank.valueOf(storedRank);
        } catch (IllegalArgumentException ignored) {
            return Rank.PLAYER;
        }
    }

    public static void setRank(UUID uuid, Rank rank) {
        ranks.put(uuid.toString(), rank.name());
        save();
    }
}
