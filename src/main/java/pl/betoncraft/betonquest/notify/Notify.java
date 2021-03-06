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

package pl.betoncraft.betonquest.notify;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.QuestManager;
import pl.betoncraft.betonquest.config.ConfigPackage;
import pl.betoncraft.betonquest.config.FileManager;
import pl.betoncraft.betonquest.utils.LogUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;

/**
 * Create a short message
 */
public class Notify {

    public static NotifyIO get(String category) {
        return get(category, new HashMap<>());
    }

    public static NotifyIO get(Map<String, String> data) {
        return get(null, data);
    }

    /**
     * Get a NotifyIO instance
     *
     * @param category comma separated predefined categories
     * @param data     Data for IO
     */
    public static NotifyIO get(String category, Map<String, String> data) {

        SortedSet<String> categories = new TreeSet<>();
        if (category != null) {
            categories.addAll(Arrays.asList(category.split(",")));
        }

        // Add default category at end
        categories.add("setting");


        // Load from all packages
        ConfigurationSection selectedConfig = null;
        ConfigPackage pack = FileManager.getPackages();

        if (pack.getCustom().getYaml().contains("notifications")) {
            ConfigurationSection section = pack.getCustom().getYaml().getConfigurationSection("notifications");

            SortedSet<String> intersect = new TreeSet<>(categories);
            intersect.retainAll(section.getKeys(false));

            // If we match on categories, find the first entry and prune away uninteresting in categories
            if (intersect.size() > 0) {
                selectedConfig = section.getConfigurationSection(intersect.first());

                // Found first category, short circuit
                if (!intersect.first().equals(categories.first())) {
                    categories = categories.subSet(categories.first(), intersect.first());
                }
            }
        }

        // Load settings from config if available
        Map<String, String> ioData = new HashMap<>();
        if (selectedConfig != null) {
            for (String key : selectedConfig.getKeys(false)) {
                ioData.put(key.toLowerCase(), selectedConfig.getString(key));
            }
        }

        // Add data over the top
        if (data != null) {
            for (String key : data.keySet()) {
                ioData.put(key.toLowerCase(), data.get(key));
            }
        }

        // NotifyIO's to use
        List<String> ios = new ArrayList<>();

        // If data contains the key 'io' then we parse it as a comma separated list of io's to use.
        if (ioData.containsKey("io")) {
            ios.addAll(Arrays.asList(
                    Arrays.stream(ioData.get("io").split(","))
                            .map(String::trim)
                            .toArray(String[]::new)));
        }

        // Add default IO, if one
        String configuredIO = BetonQuest.getInstance().getConfig().getString("default_notify_IO");
        if (configuredIO != null) {
            ios.add(configuredIO);
        }

        // Add fallbacks
        ios.add("chat");

        // Load IO
        NotifyIO tio = null;
        for (String name : ios) {
            Class<? extends NotifyIO> c = QuestManager.getNotifyIO(name);
            if (c != null) {
                try {
                    tio = c.getConstructor(Map.class).newInstance(ioData);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    LogUtils.getLogger().log(Level.WARNING, "Error when loading notify IO");
                    LogUtils.logThrowable(e);
                    return new DummyIO(ioData);
                }
                break;
            }
        }

        if (tio == null) {
            LogUtils.getLogger().log(Level.WARNING, "Error when loading notify IO");
            return new DummyIO(ioData);
        }

        return tio;
    }

    public static NotifyIO get() {
        return get(new HashMap<>());
    }

    // Fallback dummy IO
    public static class DummyIO extends NotifyIO {

        public DummyIO(Map<String, String> data) {
            super(data);
        }

        @Override
        public void sendNotify(String message, Collection<? extends Player> players) {
        }
    }

}
