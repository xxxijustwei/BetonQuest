package pl.betoncraft.betonquest.utils;

import org.bukkit.Bukkit;
import pl.betoncraft.betonquest.BetonQuest;

public class Scheduler {
    public static void run(Runnable runnable) {
        Bukkit.getScheduler().runTask(BetonQuest.getInstance(), runnable);
    }

    public static void runAsync(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(BetonQuest.getInstance(), runnable);
    }

    public static void runLater(Runnable runnable, long tick) {
        Bukkit.getScheduler().runTaskLater(BetonQuest.getInstance(), runnable, tick);
    }

    public static void runLaterAsync(Runnable runnable, long tick) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(BetonQuest.getInstance(), runnable, tick);
    }
}