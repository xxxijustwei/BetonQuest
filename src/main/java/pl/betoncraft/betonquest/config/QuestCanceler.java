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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.QuestManager;
import pl.betoncraft.betonquest.core.id.ConditionID;
import pl.betoncraft.betonquest.core.id.EventID;
import pl.betoncraft.betonquest.core.id.ItemID;
import pl.betoncraft.betonquest.core.Journal;
import pl.betoncraft.betonquest.core.id.ObjectiveID;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.ObjectNotFoundException;
import pl.betoncraft.betonquest.core.QuestItem;
import pl.betoncraft.betonquest.core.PlayerData;
import pl.betoncraft.betonquest.utils.LogUtils;
import pl.betoncraft.betonquest.utils.MessageUtils;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Represents a quest canceler, which cancels quests for players.
 *
 * @author Jakub Sapalski
 */
public class QuestCanceler {

    private final String id;
    private final String name;
    private final String item;
    private List<String> tags;
    private List<String> points;
    private List<String> journal;
    private List<ConditionID> conditions;
    private List<EventID> events;
    private List<ObjectiveID> objectives;
    private Location loc;

    /**
     * Creates a new canceler with given name.
     *
     * @param id ID of the canceler (package.name)
     * @throws InstructionParseException when parsing the canceler fails for some reason
     */
    public QuestCanceler(String id) throws InstructionParseException, ObjectNotFoundException {
        this.id = id;
        this.name = FileManager.getPackages().getMainString("cancel" + id + ".name");
        item = FileManager.getPackages().getMainString("cancel." + id + ".item");
        this.loadEvents();
        this.loadConditions();
        this.loadObjectives();
        this.loadTags();
        this.loadPoints();
        this.loadJournal();
        this.loadLoc();
    }

    private void loadLoc() {
        String content = FileManager.getPackages().getMainString("cancel." + id + ".loc");
        if (content == null) return;
        String[] parts = content.split(";");

        if (parts.length != 4 && parts.length != 6) {
            LogUtils.getLogger().log(Level.WARNING, "Wrong location format in quest canceler " + id);
            return;
        }

        double x, y, z;
        try {
            x = Double.parseDouble(parts[0]);
            y = Double.parseDouble(parts[1]);
            z = Double.parseDouble(parts[2]);
        } catch (NumberFormatException e) {
            LogUtils.getLogger().log(Level.WARNING, "Could not parse location in quest canceler " + id);
            LogUtils.logThrowable(e);
            return;
        }
        World world = Bukkit.getWorld(parts[3]);
        if (world == null) {
            LogUtils.getLogger().log(Level.WARNING, "The world doesn't exist in quest canceler " + id);
            return;
        }
        float yaw = 0, pitch = 0;
        if (parts.length == 6) {
            try {
                yaw = Float.parseFloat(parts[4]);
                pitch = Float.parseFloat(parts[5]);
            } catch (NumberFormatException e) {
                LogUtils.getLogger().log(Level.WARNING, "Could not parse yaw/pitch in quest canceler " + id + ", setting to 0");
                LogUtils.logThrowable(e);
                yaw = 0;
                pitch = 0;
            }
        }

        loc = new Location(world, x, y, z, yaw, pitch);
    }

    private void loadEvents() throws ObjectNotFoundException {
        events = new ArrayList<>();
        String content = FileManager.getPackages().getMainString("cancel." + id + ".events");
        if (content == null) return;

        for (String s : content.split(",")) {
            events.add(new EventID(s));
        }
    }

    private void loadConditions() throws ObjectNotFoundException {
        conditions = new ArrayList<>();
        String content = FileManager.getPackages().getMainString("cancel." + id + ".conditions");
        if (content == null) return;

        for (String s : content.split(",")) {
            conditions.add(new ConditionID(s));
        }
    }

    private void loadObjectives() throws ObjectNotFoundException {
        objectives = new ArrayList<>();
        String content = FileManager.getPackages().getMainString("cancel." + id + ".objectives");
        if (content == null) return;

        for (String s : content.split(",")) {
            objectives.add(new ObjectiveID(s));
        }
    }

    private void loadTags() {
        tags = new ArrayList<>();
        String content = FileManager.getPackages().getMainString("cancel." + id + ".tags");
        if (content == null) return;

        tags.addAll(Arrays.asList(content.split(",")));
    }

    private void loadPoints() {
        points = new ArrayList<>();
        String content = FileManager.getPackages().getMainString("cancel." + id + ".points");
        if (content == null) return;

        points.addAll(Arrays.asList(content.split(",")));
    }

    private void loadJournal() {
        journal = new ArrayList<>();
        String content = FileManager.getPackages().getMainString("cancel." + id + ".journal");
        if (content == null) return;

        journal.addAll(Arrays.asList(content.split(",")));
    }


    /**
     * Checks conditions of this canceler to decide if it should be shown to the
     * player or not.
     *
     * @param uuid ID of the player
     * @return true if all conditions are met, false otherwise
     */
    public boolean show(UUID uuid) {
        if (conditions == null)
            return true;
        for (ConditionID condition : conditions) {
            if (!QuestManager.condition(uuid, condition)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Cancels the quest for specified player.
     *
     * @param uuid ID of the player
     */
    public void cancel(UUID uuid) {
        LogUtils.getLogger().log(Level.FINE, "Canceling the quest " + id + " for player " + PlayerConverter.getName(uuid));
        PlayerData playerData = BetonQuest.getInstance().getPlayerData(uuid);
        // remove tags, points, objectives and journals
        if (tags != null) {
            for (String tag : tags) {
                LogUtils.getLogger().log(Level.FINE, "  Removing tag " + tag);
                if (!tag.contains(".")) {
                    playerData.removeTag(tag);
                } else {
                    playerData.removeTag(tag);
                }
            }
        }
        if (points != null) {
            for (String point : points) {
                LogUtils.getLogger().log(Level.FINE, "  Removing points " + point);
                if (!point.contains(".")) {
                    playerData.removePointsCategory(point);
                } else {
                    playerData.removePointsCategory(point);
                }
            }
        }
        if (objectives != null) {
            for (ObjectiveID objectiveID : objectives) {
                LogUtils.getLogger().log(Level.FINE, "  Removing objective " + objectiveID);
                BetonQuest.getQuestManager().getObjective(objectiveID).removePlayer(uuid);
                playerData.removeRawObjective(objectiveID);
            }
        }
        if (journal != null) {
            Journal j = playerData.getJournal();
            for (String entry : journal) {
                LogUtils.getLogger().log(Level.FINE, "  Removing entry " + entry);
                if (entry.contains(".")) {
                    j.removePointer(entry);
                } else {
                    j.removePointer(entry);
                }
            }
            j.update();
        }
        // teleport player to the location
        if (loc != null) {
            LogUtils.getLogger().log(Level.FINE, "  Teleporting to new location");
            PlayerConverter.getPlayer(uuid).teleport(loc);
        }
        // fire all events
        if (events != null) {
            for (EventID event : events) {
                QuestManager.event(uuid, event);
            }
        }
        // done
        LogUtils.getLogger().log(Level.FINE, "Quest removed!");

        MessageUtils.sendNotify(uuid, "quest_canceled", new String[]{getName()}, "quest_cancelled,quest_canceled,info");
    }

    public String getName() {
        return name.replace("_", " ").replace("&", "§");
    }

    public ItemStack getItem() {
        ItemStack stack = new ItemStack(Material.BONE);
        if (item != null) {
            try {
                ItemID itemID = new ItemID(item);
                stack = new QuestItem(itemID).generate(1);
            } catch (InstructionParseException | ObjectNotFoundException e) {
                LogUtils.getLogger().log(Level.WARNING, "Could not load cancel button: " + e.getMessage());
                LogUtils.logThrowable(e);
            }
        }
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(getName());
        stack.setItemMeta(meta);
        return stack;
    }

}
