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

import org.bukkit.entity.Player;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.core.VariableNumber;
import pl.betoncraft.betonquest.api.Condition;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.util.UUID;

/**
 * This condition checks the players moon cycle (1 is full moon, 8 is Waxing Gibbous) and returns if the player is
 * under that moon.
 *
 * @author Caleb Britannia (James Thacker)
 */
public class MooncycleCondition extends Condition {

    private VariableNumber thisCycle;

    public MooncycleCondition(Instruction instruction) throws InstructionParseException {
        super(instruction);
        this.thisCycle = instruction.getVarNum();
    }


    @Override
    public boolean check(UUID uuid) throws QuestRuntimeException {
        Player player = PlayerConverter.getPlayer(uuid);
        int days = (int) player.getWorld().getFullTime() / 24000;
        int phaseInt = days % 8;
        phaseInt += 1;
        return (phaseInt == thisCycle.getInt(uuid));
    }

}



