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
import pl.betoncraft.betonquest.QuestManager;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.core.id.ObjectiveID;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Starts an objective for the player
 *
 * @author Jakub Sapalski
 */
public class ObjectiveEvent extends QuestEvent {

    private final List<ObjectiveID> objectives;
    private final String action;

    public ObjectiveEvent(Instruction instruction) throws InstructionParseException {
        super(instruction);
        action = instruction.next();
        objectives = instruction.getObjectives();
        if (!Arrays.asList(new String[]{"start", "add", "delete", "remove", "complete", "finish"})
                .contains(action)) {
            throw new InstructionParseException("Unknown action: " + action);
        }
        persistent = !action.equalsIgnoreCase("complete");
    }

    @Override
    public void run(final UUID uuid) throws QuestRuntimeException {
        switch (action.toLowerCase()) {
            case "start":
            case "add":
                objectives.forEach(elm -> QuestManager.newObjective(uuid, elm));
                break;
            case "delete":
            case "remove":
                objectives.forEach(elm -> {
                    BetonQuest.getQuestManager().getObjective(elm).removePlayer(uuid);
                    BetonQuest.getInstance().getPlayerData(uuid).removeRawObjective(elm);
                });
                break;
            case "complete":
            case "finish":
                objectives.forEach(elm -> {
                    BetonQuest.getQuestManager().getObjective(elm).completeObjective(uuid);
                });
                break;
        }
    }
}
