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
package pl.betoncraft.betonquest.core;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.QuestManager;
import pl.betoncraft.betonquest.config.ConfigPackage;
import pl.betoncraft.betonquest.config.FileManager;
import pl.betoncraft.betonquest.core.id.ConditionID;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.ObjectNotFoundException;
import pl.betoncraft.betonquest.utils.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * Represents player's journal.
 *
 * @author Jakub Sapalski
 */
public class Journal {

    private final UUID uuid;
    private final List<Pointer> pointers;
    private final List<String> texts = new ArrayList<>();

    /**
     * Creates new Journal instance from List of Pointers.
     *
     * @param uuid ID of the player whose journal is created
     * @param list list of pointers to journal entries
     */
    public Journal(UUID uuid, List<Pointer> list) {
        // generate texts from list of pointers
        this.uuid = uuid;
        pointers = list;
    }

    /**
     * Retrieves the list of pointers in this journal.
     *
     * @return this Journal's list of pointers to journal entries
     */
    public List<Pointer> getPointers() {
        return pointers;
    }

    /**
     * Adds pointer to the journal. It needs to be updated now.
     *
     * @param pointer the pointer to be added
     */
    public void addPointer(Pointer pointer) {
        pointers.add(pointer);

        Scheduler.runAsync(() -> {
            String date = Long.toString(pointer.getTimestamp());
            BetonQuest.getStorageManager().insertJournal(uuid, pointer.getPointer(), date);
        });
    }

    /**
     * Removes the pointer from journal. It needs to be updated now.
     *
     * @param pointerName the name of the pointer to remove
     */
    public void removePointer(String pointerName) {
        for (Iterator<Pointer> iterator = pointers.iterator(); iterator.hasNext(); ) {
            Pointer pointer = iterator.next();
            if (pointer.getPointer().equalsIgnoreCase(pointerName)) {
                /*String date = Long.toString(pointer.getTimestamp());*/
                Scheduler.runAsync(() -> BetonQuest.getStorageManager().deleteJournal(uuid, pointer.getPointer()));
                iterator.remove();
                break;
            }
        }
    }

    /**
     * Retrieves the list of generated texts.
     *
     * @return list of Strings - texts for every journal entry
     */
    public List<String> getText() {
        List<String> list;
        if (FileManager.getConfig("journal.reversed_order").equalsIgnoreCase("true")) {
            list = Lists.reverse(texts);
        } else {
            list = new ArrayList<>(texts);
        }
        return list;
    }

    /**
     * Generates texts for every pointer and places them inside a List
     *
     */
    public void generateTexts() {
        // remove previous texts
        texts.clear();
        for (Pointer pointer : pointers) {
            // if date should not be hidden, generate the date prefix
            String datePrefix = "";
            if (FileManager.getConfig("journal.hide_date").equalsIgnoreCase("false")) {
                String date = new SimpleDateFormat(FileManager.getConfig("date_format"))
                        .format(pointer.getTimestamp());
                String[] dateParts = date.split(" ");
                String day = "§" + FileManager.getConfig("journal_colors.date.day") + dateParts[0];
                String hour = "";
                if (dateParts.length > 1) {
                    hour = "§" + FileManager.getConfig("journal_colors.date.hour") + dateParts[1];
                }
                datePrefix = day + " " + hour + "\n";
            }
            // get package and name of the pointer
            String[] parts = pointer.getPointer().split("\\.");
            ConfigPackage pack = FileManager.getPackages();
            if (pack == null) {
                continue;
            }
            String pointerName = parts[1];
            // resolve the text in player's language
            String text;
            if (!pack.getJournal().containsKey(pointerName)) {
                LogUtils.getLogger().log(Level.WARNING, "No defined journal entry " + pointerName);
                text = "error";
            } else {
                text = Utils.format(pack.getJournal().get(pointerName));
            }
            // handle case when the text isn't defined
            if (text == null) {
                LogUtils.getLogger().log(Level.WARNING, "No text defined for journal entry " + pointerName);
                text = "error";
            }

            // resolve variables
            for (String variable : QuestManager.resolveVariables(text)) {
                try {
                    QuestManager.createVariable(variable);
                } catch (InstructionParseException e) {
                    LogUtils.getLogger().log(Level.WARNING, "Error while creating variable '" + variable + "' on journal page '" + pointerName + "' in "
                            + PlayerConverter.getName(uuid) + "'s journal: " + e.getMessage());
                    LogUtils.logThrowable(e);
                }
                text = text.replace(variable,
                        BetonQuest.getQuestManager().getVariableValue(variable, uuid));
            }

            // add the entry to the list
            texts.add(datePrefix + "§" + FileManager.getConfig("journal_colors.text") + text);
        }
    }

    public void update() {

    }

    /**
     * Clears the Journal completely but doesn't touch the database.
     */
    public void clear() {
        texts.clear();
        pointers.clear();
    }
}
