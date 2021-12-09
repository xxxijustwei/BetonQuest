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
import com.taylorswiftcn.justwei.util.MegumiUtil;
import net.sakuragame.eternal.justmessage.api.MessageAPI;
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
import pl.betoncraft.betonquest.config.JournalProfile;
import pl.betoncraft.betonquest.core.id.ConditionID;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.ObjectNotFoundException;
import pl.betoncraft.betonquest.utils.*;

import java.sql.Timestamp;
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
    private final LinkedList<String> pointers;

    /**
     * Creates new Journal instance from List of Pointers.
     *
     * @param uuid ID of the player whose journal is created
     * @param list list of pointers to journal entries
     */
    public Journal(UUID uuid, LinkedList<String> list) {
        // generate texts from list of pointers
        this.uuid = uuid;
        this.pointers = list;
    }

    /**
     * Retrieves the list of pointers in this journal.
     *
     * @return this Journal's list of pointers to journal entries
     */
    public List<String> getPointers() {
        return pointers;
    }

    /**
     * Adds pointer to the journal. It needs to be updated now.
     *
     * @param pointer the pointer to be added
     */
    public void addPointer(String pointer) {
        if (pointer == null) return;
        if (pointers.contains(pointer)) return;

        pointers.add(pointer);

        Scheduler.runAsync(() -> BetonQuest.getStorageManager().insertJournal(uuid, pointer));
    }

    /**
     * Removes the pointer from journal. It needs to be updated now.
     *
     * @param pointerName the name of the pointer to remove
     */
    public void removePointer(String pointerName) {
        for (String s : new ArrayList<>(pointers)) {
            if (s.equals(pointerName)) {
                pointers.remove(s);
                Scheduler.runAsync(() -> BetonQuest.getStorageManager().deleteJournal(uuid, pointerName));
            }
        }
    }

    /**
     * Generates texts for every pointer and places them inside a List
     *
     */
    public LinkedList<String> generatePointer() {
        LinkedList<String> texts = new LinkedList<>();

        ConfigPackage pack = FileManager.getPackages();

        for (String pointer : pointers) {
            List<String> contents = pack.getJournal().containsKey(pointer) ? generatePointer(pack.getJournal().get(pointer)) : new ArrayList<>();
            if (contents.isEmpty()) {
                LogUtils.getLogger().log(Level.WARNING, "No text defined for journal entry " + pointer);
            }

            texts.addAll(contents);
        }

        return texts;
    }

    private LinkedList<String> generatePointer(JournalProfile profile) {
        LinkedList<String> texts = new LinkedList<>();

        texts.add(profile.getTitle());
        texts.addAll(generatePointerContent(profile));

        return texts;
    }

    public LinkedList<String> generatePointerContent(JournalProfile profile) {
        LinkedList<String> contents = new LinkedList<>();

        for (String s : profile.getContents()) {
            for (String variable : QuestManager.resolveVariables(s)) {
                s = s.replace(variable, BetonQuest.getQuestManager().getVariableValue(variable, uuid));
            }
            contents.add(s);
        }

        return contents;
    }

    public void update() {
        if (pointers.size() == 0) {
            MessageAPI.setQuestBar(PlayerConverter.getPlayer(uuid), "&7&o暂无待完成任务", new ArrayList<>());
            return;
        }

        String pointer = pointers.getLast();
        JournalProfile profile = FileManager.getPackages().getJournal().get(pointer);
        if (profile == null) return;

        MessageAPI.setQuestBar(PlayerConverter.getPlayer(uuid), profile.getTitle(), generatePointerContent(profile));
    }

    /**
     * Clears the Journal completely but doesn't touch the database.
     */
    public void clear() {
        pointers.clear();
    }
}
