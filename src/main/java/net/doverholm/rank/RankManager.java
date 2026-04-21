package net.doverholm.spmod.rank;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RankManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File("config/ranks.json");

    private static Map<String, String> ranks = new HashMap<>();

    public static void load() {
        try {
            if (!FILE.exists()) {
                FILE.getParentFile().mkdirs();
                save();
                return;
            }

            FileReader reader = new FileReader(FILE);
            Map<String, String> data = GSON.fromJson(reader, Map.class);
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
        return Rank.valueOf(ranks.getOrDefault(uuid.toString(), "PLAYER"));
    }

    public static void setRank(UUID uuid, Rank rank) {
        ranks.put(uuid.toString(), rank.name());
        save();
    }
}