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
package pl.betoncraft.betonquest.events;

import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.core.Journal;
import pl.betoncraft.betonquest.api.event.QuestEvent;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.utils.MessageUtils;

import java.util.Date;
import java.util.UUID;

/**
 * Adds the entry to player's journal
 *
 * @author Jakub Sapalski
 */
public class JournalEvent extends QuestEvent {

    private final String name;
    private final boolean add;

    public JournalEvent(Instruction instruction) throws InstructionParseException {
        super(instruction);
        String first = instruction.next();
        if (first.equalsIgnoreCase("update")) {
            name = null;
            add = false;
        } else {
            add = first.equalsIgnoreCase("add");
            name = instruction.next();
        }
    }

    @Override
    public void run(UUID uuid) {
        Journal journal = BetonQuest.getInstance().getPlayerData(uuid).getJournal();
        if (add) {
            journal.addPointer(name);
            journal.update();
            MessageUtils.sendNotify(uuid, "new_journal_entry", null, "new_journal_entry,info");
            return;
        }

        if (name != null) {
            journal.removePointer(name);
            journal.update();
            return;
        }

        journal.update();
    }

}
