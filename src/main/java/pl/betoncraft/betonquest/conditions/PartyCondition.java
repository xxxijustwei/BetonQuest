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
package pl.betoncraft.betonquest.conditions;

import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.QuestManager;
import pl.betoncraft.betonquest.core.id.ConditionID;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.core.VariableNumber;
import pl.betoncraft.betonquest.api.Condition;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Checks the conditions for the whole party (including the player that started
 * the checking)
 *
 * @author Jakub Sapalski
 */
public class PartyCondition extends Condition {

    private VariableNumber range;
    private ConditionID[] conditions;
    private ConditionID[] everyone;
    private ConditionID[] anyone;
    private VariableNumber count;

    public PartyCondition(Instruction instruction) throws InstructionParseException {
        super(instruction);
        range = instruction.getVarNum();
        conditions = instruction.getList(instruction::getCondition).toArray(new ConditionID[0]);
        everyone = instruction.getList(instruction.getOptional("every"), instruction::getCondition).toArray(new ConditionID[0]);
        anyone = instruction.getList(instruction.getOptional("any"), instruction::getCondition).toArray(new ConditionID[0]);
        count = instruction.getVarNum(instruction.getOptional("count"));
    }

    @Override
    public boolean check(UUID uuid) throws QuestRuntimeException {
        // get the party
        List<UUID> members = Utils.getParty(uuid, range.getDouble(uuid), conditions);
        // check every condition against every player - all of them must meet those conditions
        for (ConditionID condition : everyone) {
            for (UUID memberID : members) {
                // if this condition wasn't met by someone, return false
                if (!QuestManager.condition(memberID, condition)) {
                    return false;
                }
            }
        }
        // check every condition against every player - at least one of them must meet each of those
        for (ConditionID condition : anyone) {
            boolean met = false;
            for (UUID memberID : members) {
                if (QuestManager.condition(memberID, condition)) {
                    met = true;
                    break;
                }
            }
            // if this condition wasn't met by anyone, return false
            if (!met) {
                return false;
            }
        }
        // if the count is more than 0, we need to check if there are more
        // players in the party than required minimum
        int c = (count != null) ? count.getInt(uuid) : 0;
        return c <= 0 || members.size() >= c;
    }

}
