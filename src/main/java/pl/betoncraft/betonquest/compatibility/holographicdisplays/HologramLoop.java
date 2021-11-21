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
package pl.betoncraft.betonquest.compatibility.holographicdisplays;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.QuestManager;
import pl.betoncraft.betonquest.config.FileManager;
import pl.betoncraft.betonquest.core.id.ConditionID;
import pl.betoncraft.betonquest.config.ConfigPackage;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.ObjectNotFoundException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.utils.LocationData;
import pl.betoncraft.betonquest.utils.LogUtils;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;

/**
 * Hides and shows holograms to players, based on conditions.
 *
 * @author Jakub Sapalski
 */
public class HologramLoop {

    private HashMap<Hologram, ConditionID[]> holograms = new HashMap<>();
    private BukkitRunnable runnable;

    /**
     * Starts a loop, which checks hologram conditions and shows them to players.
     */
    public HologramLoop() {
        ConfigPackage pack = FileManager.getPackages();
        ConfigurationSection section = pack.getCustom().getYaml().getConfigurationSection("holograms");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                if (!Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
                    LogUtils.getLogger().log(Level.WARNING, "Holograms won't be able to hide from players without ProtocolLib plugin! "
                            + "Install it to use conditioned holograms.");
                    return;
                }
                List<String> lines = section.getStringList(key + ".lines");
                String rawConditions = section.getString(key + ".conditions");
                String rawLocation = section.getString(key + ".location");
                if (rawLocation == null) {
                    LogUtils.getLogger().log(Level.WARNING, "Location is not specified in " + key + " hologram");
                    continue;
                }
                ConditionID[] conditions = new ConditionID[]{};
                if (rawConditions != null) {
                    String[] parts = rawConditions.split(",");
                    conditions = new ConditionID[parts.length];
                    for (int i = 0; i < conditions.length; i++) {
                        try {
                            conditions[i] = new ConditionID(parts[i]);
                        } catch (ObjectNotFoundException e) {
                            LogUtils.getLogger().log(Level.WARNING, "Error while loading " + parts[i] + " condition for hologram " + key + ": " + e.getMessage());
                            LogUtils.logThrowable(e);
                        }
                    }
                }
                Location location = null;
                try {
                    location = new LocationData(rawLocation).getLocation(null);
                } catch (QuestRuntimeException | InstructionParseException e) {
                    LogUtils.getLogger().log(Level.WARNING, "Could not parse location in " + key + " hologram: " + e.getMessage());
                    LogUtils.logThrowable(e);
                    continue;
                }
                Hologram hologram = HologramsAPI.createHologram(BetonQuest.getInstance(), location);
                hologram.getVisibilityManager().setVisibleByDefault(false);
                for (String line : lines) {
                    // If line begins with 'item:', then we will assume its a floating item
                    if (line.startsWith("item:")) {
                        hologram.appendItemLine(new ItemStack(Material.matchMaterial(line.substring(5))));
                    } else {
                        hologram.appendTextLine(line.replace('&', '§'));
                    }
                }
                holograms.put(hologram, conditions);
            }
        }
        // loop the holograms to show/hide them
        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    holograms:
                    for (Entry<Hologram, ConditionID[]> entry : holograms.entrySet()) {
                        for (ConditionID condition : entry.getValue()) {
                            if (!QuestManager.condition(player.getUniqueId(), condition)) {
                                entry.getKey().getVisibilityManager().hideTo(player);
                                continue holograms;
                            }
                        }
                        entry.getKey().getVisibilityManager().showTo(player);
                    }
                }
            }
        };
        runnable.runTaskTimer(BetonQuest.getInstance(), 20, BetonQuest.getInstance().getConfig()
                .getInt("hologram_update_interval", 20 * 10));
    }

    /**
     * Cancels hologram updating loop and removes all BetonQuest-registered holograms.
     */
    public void cancel() {
        if (runnable == null)
            return;
        runnable.cancel();
        for (Hologram hologram : holograms.keySet()) {
            hologram.delete();
        }
    }

}
