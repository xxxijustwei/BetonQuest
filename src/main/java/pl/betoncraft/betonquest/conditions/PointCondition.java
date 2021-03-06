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
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.core.Point;
import pl.betoncraft.betonquest.core.VariableNumber;
import pl.betoncraft.betonquest.api.Condition;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.utils.Utils;

import java.util.List;
import java.util.UUID;

/**
 * Requires the player to have specified amount of points (or more) in specified
 * category
 *
 * @author Jakub Sapalski
 */
public class PointCondition extends Condition {

    protected final String category;
    protected final VariableNumber count;
    protected final boolean equal;

    public PointCondition(Instruction instruction) throws InstructionParseException {
        super(instruction);
        category = instruction.next();
        count = instruction.getVarNum();
        equal = instruction.hasArgument("equal");
    }

    @Override
    public boolean check(UUID uuid) throws QuestRuntimeException {
        return check(uuid, BetonQuest.getInstance().getPlayerData(uuid).getPoints());
    }

    protected boolean check(UUID uuid, List<Point> points) throws QuestRuntimeException {
        int c = count.getInt(uuid);
        for (Point point : points) {
            if (point.getCategory().equalsIgnoreCase(category)) {
                if (equal) {
                    return point.getCount() == c;
                } else {
                    return point.getCount() >= c;
                }
            }
        }
        return false;
    }

}
