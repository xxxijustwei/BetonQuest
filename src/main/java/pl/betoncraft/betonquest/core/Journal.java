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

import me.clip.placeholderapi.PlaceholderAPI;
import net.sakuragame.eternal.cargo.CargoAPI;
import net.sakuragame.eternal.cargo.value.ValueType;
import net.sakuragame.eternal.dragoncore.network.PacketSender;
import net.sakuragame.eternal.justmessage.api.MessageAPI;
import net.sakuragame.eternal.justmessage.screen.ui.quest.JournalParams;
import net.sakuragame.eternal.justmessage.screen.ui.quest.JournalScreen;
import org.bukkit.entity.Player;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.QuestManager;
import pl.betoncraft.betonquest.config.ConfigPackage;
import pl.betoncraft.betonquest.config.FileManager;
import pl.betoncraft.betonquest.config.JournalProfile;
import pl.betoncraft.betonquest.utils.*;

import java.util.*;
import java.util.logging.Level;

public class Journal {

    private final UUID uuid;
    private String stick;
    private final LinkedList<String> pointers;
    private boolean change;

    public Journal(UUID uuid, LinkedList<String> list) {
        this.uuid = uuid;
        this.stick = CargoAPI.getValuesManager().getUserValue(uuid, ValueType.STORAGE, "QUEST_JOURNAL_STICK");
        this.pointers = list;
        this.change = true;
    }

    public String getStick() {
        return stick;
    }

    public List<String> getPointers() {
        return pointers;
    }

    public void addPointer(String pointer) {
        if (pointers.contains(pointer)) return;

        pointers.add(pointer);
        Scheduler.runAsync(() -> BetonQuest.getStorageManager().insertJournal(uuid, pointer));

        change = true;
    }

    public void removePointer(String pointerName) {
        for (String s : new ArrayList<>(pointers)) {
            if (s.equals(pointerName)) {
                pointers.remove(s);
                Scheduler.runAsync(() -> BetonQuest.getStorageManager().deleteJournal(uuid, pointerName));

                change = true;
            }
        }
    }

    public void openUI() {
        Player player = PlayerConverter.getPlayer(uuid);
        if (change) {
            List<JournalProfile> profiles = new ArrayList<>();
            pointers.forEach(s -> profiles.add(FileManager.getPackages().getJournal().get(s)));
            profiles.sort(Comparator.comparing(JournalProfile::getPriority));

            List<JournalParams> data = new LinkedList<>();
            profiles.forEach(elm -> data.add(new JournalParams(elm.getId(), PlaceholderAPI.setPlaceholders(player, elm.getTitle()), elm.getStatus().getDisplay())));

            JournalScreen ui = new JournalScreen(data, stick);
            ui.open(PlayerConverter.getPlayer(uuid));
            change = false;
            return;
        }

        PacketSender.sendOpenGui(player, "quest");
    }

    public void setStick(String stick) {
        if (!pointers.contains(stick)) return;

        String oldStick = this.stick;
        this.stick = stick;
        JournalScreen.stickQuest(PlayerConverter.getPlayer(uuid), this.stick, oldStick);
        Scheduler.runAsync(() -> CargoAPI.getValuesManager().setUserValue(uuid, ValueType.STORAGE, "QUEST_JOURNAL_STICK", stick));

        this.update();
        this.change = true;
    }

    private LinkedList<String> generatePointer(JournalProfile profile) {
        LinkedList<String> texts = new LinkedList<>();

        texts.add(profile.getTitle());
        texts.addAll(generateContent(profile));

        return texts;
    }

    public LinkedList<String> generateContent(JournalProfile profile) {
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
            MessageAPI.setQuestBar(PlayerConverter.getPlayer(uuid), "&7&o暂无待完成任务&7&l(F)", new ArrayList<>());
            return;
        }

        if (stick == null || stick.isEmpty() || !pointers.contains(stick)) {
            stick = pointers.getLast();
            Scheduler.runAsync(() -> CargoAPI.getValuesManager().setUserValue(uuid, ValueType.STORAGE, "QUEST_JOURNAL_STICK", stick));
        }

        JournalProfile profile = FileManager.getPackages().getJournal().get(stick);
        if (profile == null) return;

        Player player = PlayerConverter.getPlayer(uuid);
        String title = PlaceholderAPI.setPlaceholders(player, profile.getTitle()) + "&f" + profile.getStatus().getSymbol() + "&7&l(F)";

        MessageAPI.setQuestBar(PlayerConverter.getPlayer(uuid), title, generateContent(profile));
    }

    public void clear() {
        pointers.clear();
    }

    public enum Status {
        UNFINISHED(0, "⊋", "&a进行中.."),
        FINISHED(1, "⊊", "&e已完成");

        private final int id;
        private final String symbol;
        private final String display;

        Status(int id, String symbol, String display) {
            this.id = id;
            this.symbol = symbol;
            this.display = display;
        }

        public int getID() {
            return id;
        }

        public String getSymbol() {
            return symbol;
        }

        public String getDisplay() {
            return display;
        }

        public static Status match(int id) {
            for (Status status : values()) {
                if (status.getID() == id) return status;
            }

            return null;
        }
    }
}
