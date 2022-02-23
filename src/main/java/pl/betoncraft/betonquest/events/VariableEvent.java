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
import pl.betoncraft.betonquest.api.Objective;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.objectives.VariableObjective;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.util.ArrayList;
import java.util.UUID;

public class VariableEvent extends QuestEvent {

    private ObjectiveID id;
    private String key;
    private ArrayList<String> keyVariables;
    private String value;
    private ArrayList<String> valueVariables;

    public VariableEvent(Instruction instruction) throws InstructionParseException {
        super(instruction);
        id = instruction.getObjective();
        key = instruction.next();
        keyVariables = QuestManager.resolveVariables(key);
        value = instruction.next();
        valueVariables = QuestManager.resolveVariables(value);
    }

    @Override
    public void run(UUID uuid) throws QuestRuntimeException {
        Objective obj = BetonQuest.getQuestManager().getObjective(id);
        if (!(obj instanceof VariableObjective)) {
            throw new QuestRuntimeException(id.getFullID() + " is not a variable objective");
        }
        VariableObjective objective = (VariableObjective) obj;
        String keyReplaced = key;
        for (String v : keyVariables) {
            keyReplaced = keyReplaced.replace(v, BetonQuest.getQuestManager().getVariableValue(v, uuid));
        }
        String valueReplaced = value;
        for (String v : valueVariables) {
            valueReplaced = valueReplaced.replace(v, BetonQuest.getQuestManager().getVariableValue(v, uuid));
        }
        if (!objective.store(uuid, keyReplaced.replace('_', ' '), valueReplaced.replace('_', ' '))) {
            throw new QuestRuntimeException("Player " + PlayerConverter.getName(uuid) + " does not have '" +
                    id.getFullID() + "' objective, cannot store a variable.");
        }
    }

}
