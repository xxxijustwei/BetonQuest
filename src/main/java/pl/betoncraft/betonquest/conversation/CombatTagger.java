/*
 * BetonQuest - advanced quests for Bukkit
 * Copyright (C) 2016  Jakub "Co0sh" Sapalski
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.betoncraft.betonquest.conversation;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.config.FileManager;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Tags players that are in combat to prevent them from starting the
 * conversation
 *
 * @author Jakub Sapalski
 */
public class CombatTagger implements Listener {

    private static HashMap<UUID, Boolean> tagged = new HashMap<>();
    private static HashMap<UUID, Integer> untaggers = new HashMap<>();
    private int delay = 10;

    /**
     * Starts the combat listener
     */
    public CombatTagger() {
        delay = Integer.parseInt(FileManager.getConfig("combat_delay"));
    }

    /**
     * Checks if the player is combat-tagged
     *
     * @param uuid ID of the player
     * @return true if the player is tagged, false otherwise
     */
    public static boolean isTagged(UUID uuid) {
        boolean result = false;
        Boolean state = tagged.get(uuid);
        if (state != null) {
            result = state;
        }
        return result;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        List<UUID> uuidList = new ArrayList<>();
        if (event.getEntity() instanceof Player) {
            uuidList.add(event.getEntity().getUniqueId());
        }
        if (event.getDamager() instanceof Player) {
            uuidList.add(event.getDamager().getUniqueId());
        }
        for (UUID uuid : uuidList) {
            tagged.put(uuid, true);
            Integer tid = untaggers.get(uuid);
            if (tid != null) {
                Bukkit.getScheduler().cancelTask(tid);
            }

            BukkitTask task = Bukkit.getScheduler().runTaskLater(BetonQuest.getInstance(), () -> tagged.put(uuid, false), delay * 20L);
            untaggers.put(uuid, task.getTaskId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        UUID uuid = event.getEntity().getUniqueId();
        tagged.remove(uuid);
        Integer tid = untaggers.remove(uuid);
        if (tid != null) {
            Bukkit.getScheduler().cancelTask(tid);
        }
    }
}
