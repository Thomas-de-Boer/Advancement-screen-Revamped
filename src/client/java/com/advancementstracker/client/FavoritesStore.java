package com.advancementstracker.client;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

/** Keeps the favorite advancement ids in memory and saves them to a text file in the config folder. */
public final class FavoritesStore {

    private static final Set<String> favorites = new LinkedHashSet<>();
    private static boolean loaded = false;

    private FavoritesStore() {
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("advancementstracker-favorites.txt");
    }

    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        Path path = file();
        if (!Files.exists(path)) return;
        try {
            for (String line : Files.readAllLines(path)) {
                String id = line.trim();
                if (!id.isEmpty()) favorites.add(id);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void save() {
        try {
            Files.write(file(), favorites);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean contains(String advancementId) {
        ensureLoaded();
        return favorites.contains(advancementId);
    }

    public static void toggle(String advancementId) {
        ensureLoaded();
        if (!favorites.remove(advancementId)) {
            favorites.add(advancementId);
        }
        save();
    }
}