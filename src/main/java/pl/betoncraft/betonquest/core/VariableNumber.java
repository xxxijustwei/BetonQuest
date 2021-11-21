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

import pl.betoncraft.betonquest.QuestManager;
import pl.betoncraft.betonquest.api.Variable;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.utils.LogUtils;

import java.util.UUID;
import java.util.logging.Level;

/**
 * Represents a number which might also be a variable.
 *
 * @author Jakub Sapalski
 */
public class VariableNumber {

    private double number;
    private Variable variable;

    /**
     * Parses the string as a number or saves it as a variable if it's not a
     * number.
     *
     * @param variable the string to parse
     */
    public VariableNumber(String variable) throws NumberFormatException {
        if (variable.length() > 2 && variable.startsWith("%") && variable.endsWith("%")) {
            try {
                this.variable = QuestManager.createVariable(variable);
            } catch (InstructionParseException e) {
                throw new NumberFormatException("Could not create variable: " + e.getMessage());
            }
            if (this.variable == null) {
                throw new NumberFormatException("Could not create variable");
            }
        } else {
            number = Double.parseDouble(variable);
        }
    }

    /**
     * Creates the VariableNumber using specified number.
     *
     * @param number the number to use
     */
    public VariableNumber(int number) {
        this.number = number;
    }

    /**
     * Creates the VariableNumber using specified number.
     *
     * @param number the number to use
     */
    public VariableNumber(double number) {
        this.number = number;
    }

    /**
     * Returns an integer represented by this variable. If it's a double, this
     * method will return the floor of it.
     *
     * @param uuid ID of the player for whom the variable should be resolved
     * @return the integer represented by this variable number
     */
    public int getInt(UUID uuid) {
        return (int) Math.floor(resolveVariable(uuid));
    }

    /**
     * Returns a double represented by this variable.
     *
     * @param uuid ID of the player for whom the variable should be resolved
     * @return the double represented by this variable number
     * @throws QuestRuntimeException when the variable does not resolve to a number
     */
    public double getDouble(UUID uuid) throws QuestRuntimeException {
        return resolveVariable(uuid);
    }

    private double resolveVariable(UUID uuid) {
        if (variable == null) {
            return number;
        } else if (uuid == null) {
            return 0;
        } else {
            String resolved = variable.getValue(uuid);
            double parsed = 0;
            try {
                parsed = Double.parseDouble(resolved);
            } catch (NumberFormatException e) {
                LogUtils.getLogger().log(Level.WARNING, "Could not parse the as a number, it's value is: '" + resolved + "'; returning 0.");
                LogUtils.logThrowable(e);
            }
            return parsed;
        }
    }

    @Override
    public String toString() {
        return (variable == null ? String.valueOf(number) : variable.toString());
    }
}
