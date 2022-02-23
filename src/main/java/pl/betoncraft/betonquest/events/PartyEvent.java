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

import pl.betoncraft.betonquest.QuestManager;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.core.VariableNumber;
import pl.betoncraft.betonquest.core.id.ConditionID;
import pl.betoncraft.betonquest.core.id.EventID;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.utils.Utils;

import java.util.List;
import java.util.UUID;

/**
 * Fires specified events for every player in the party
 *
 * @author Jakub Sapalski
 */
public class PartyEvent extends QuestEvent {

    private final ConditionID[] conditions;
    private final EventID[] events;
    private final VariableNumber range;

    public PartyEvent(Instruction instruction) throws InstructionParseException {
        super(instruction);
        range = instruction.getVarNum();
        conditions = instruction.getList(instruction::getCondition).toArray(new ConditionID[0]);
        events = instruction.getList(instruction::getEvent).toArray(new EventID[0]);
    }

    @Override
    public void run(UUID uuid) throws QuestRuntimeException {
        List<UUID> members = Utils.getParty(uuid, range.getDouble(uuid), conditions);
        for (UUID memberID : members) {
            for (EventID event : events) {
                QuestManager.event(memberID, event);
            }
        }
    }

}
