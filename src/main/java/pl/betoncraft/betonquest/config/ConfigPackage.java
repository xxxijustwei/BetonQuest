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
package pl.betoncraft.betonquest.config;

import com.taylorswiftcn.justwei.util.MegumiUtil;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.betoncraft.betonquest.core.Journal;

import java.io.File;
import java.util.*;

/**
 * Holds configuration files of the package
 *
 * @author Jakub Sapalski
 */
public class ConfigPackage {

    private File folder;
    private boolean enabled;

    private ConfigAccessor main;
    @Getter private HashMap<String, String> events;
    @Getter private HashMap<String, String> conditions;
    @Getter private HashMap<String, String> objectives;
    @Getter private HashMap<String, JournalProfile> journal;
    @Getter private HashMap<Integer, String> npc;
    private ConfigAccessor custom;
    private HashMap<String, ConfigAccessor> conversations = new HashMap<>();

    /**
     * Loads a package from specified directory. It doesn't have to be valid
     * package directory.
     *
     * @param path the directory containing this package
     */
    public ConfigPackage(File path) {
        if (!path.isDirectory()) return;
        this.folder = path;
        this.main = new ConfigAccessor(new File(path, "main.yml"), AccessorType.MAIN);
        this.custom = new ConfigAccessor(new File(path, "custom.yml"), AccessorType.CUSTOM);
        this.events = loadSetting(AccessorType.EVENTS);
        this.conditions = loadSetting(AccessorType.CONDITIONS);
        this.objectives = loadSetting(AccessorType.OBJECTIVES);
        this.journal = loadJournals();
        this.npc = loadNpc();
        this.conversations = loadConversations();

        enabled = main.getYaml().getBoolean("enabled", true);
    }

    private HashMap<Integer, String> loadNpc() {
        HashMap<Integer, String> npc = new HashMap<>();
        ConfigurationSection section = main.getYaml().getConfigurationSection("npcs");
        if (section == null) return npc;

        for (String s : section.getKeys(false)) {
            if (!MegumiUtil.isInteger(s)) continue;

            String name = section.getString(s);
            npc.put(Integer.parseInt(s), name);
        }

        return npc;
    }

    private HashMap<String, String> loadSetting(AccessorType type) {
        HashMap<String, String> setting = new HashMap<>();

        File dir = new File(folder, type.getFolder());
        if (!(dir.exists() && dir.isDirectory())) return setting;

        List<File> files = getYamlFiles(dir, new ArrayList<>());
        if (files == null || files.size() == 0) return setting;

        files.forEach(sub -> {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(sub);
            for (String key : yaml.getKeys(false)) {
                String value = yaml.getString(key);
                setting.put(key, value);
            }
        });

        return setting;
    }

    private HashMap<String, JournalProfile> loadJournals() {
        HashMap<String, JournalProfile> profiles = new HashMap<>();

        File dir = new File(folder, AccessorType.JOURNAL.getFolder());
        if (!(dir.exists() && dir.isDirectory())) return profiles;

        List<File> files = getYamlFiles(dir, new ArrayList<>());
        if (files == null || files.size() == 0) return profiles;

        files.forEach(sub -> {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(sub);
            for (String key : yaml.getKeys(false)) {
                String title = MegumiUtil.onReplace(yaml.getString(key + ".title"));
                int priority = yaml.getInt(key + ".priority");
                Journal.Status status = Journal.Status.match(yaml.getInt(key + ".status"));
                List<String> desc = MegumiUtil.onReplace(yaml.getStringList(key + ".desc"));

                if (status == null) continue;

                profiles.put(key, new JournalProfile(key, title, priority, status, desc));
            }
        });

        return profiles;
    }

    private HashMap<String, ConfigAccessor> loadConversations() {
        HashMap<String, ConfigAccessor> conv = new HashMap<>();

        File dir = new File(folder, AccessorType.CONVERSATION.getFolder());
        if (!(dir.exists() && dir.isDirectory())) return conv;

        List<File> files = getYamlFiles(dir, new ArrayList<>());
        if (files == null || files.size() == 0) return conv;

        files.forEach(sub -> {
            String fileName = sub.getName();
            ConfigAccessor accessor = new ConfigAccessor(sub, AccessorType.CONVERSATION);
            conv.put(fileName.substring(0, fileName.length() - 4), accessor);
        });

        return conv;
    }

    public List<File> getYamlFiles(File folder, List<File> list) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.isDirectory()) {
                    getYamlFiles(file, list);
                }
                else if (file.isFile() && file.getName().endsWith(".yml")) {
                    list.add(file);
                }
            }
        }
        return list;
    }

    public void addEvents(String id, String statement) {
        events.put(id, statement);
    }

    public void addConversations(String id, ConfigAccessor accessor) {
        conversations.put(id, accessor);
    }

    /**
     * @return if the package is enabled (true) or disabled (false)
     */
    public boolean isEnabled() {
        return enabled;
    }

    public String getMainString(String address) {
        return main.getYaml().getString(address);
    }

    public String getConvString(String id, String address) {
        return getConvString(id, address, null);
    }

    public String getConvString(String id, String address, String def) {
        String s = conversations.get(id).getYaml().getString(address);
        if (s == null) return def;

        return s;
    }

    /**
     * @return the main configuration of the package
     */
    public ConfigAccessor getMain() {
        return main;
    }

    /**
     * @return the config with custom settings
     */
    public ConfigAccessor getCustom() {
        return custom;
    }

    /**
     * @param name name of the conversation to search for
     * @return the conversation config
     */
    public ConfigAccessor getConversation(String name) {
        return conversations.get(name);
    }

    /**
     * @return the set of conversation names
     */
    public Set<String> getConversationNames() {
        return conversations.keySet();
    }

    /**
     * @return the folder which contains this package
     */
    public File getFolder() {
        return folder;
    }

}
