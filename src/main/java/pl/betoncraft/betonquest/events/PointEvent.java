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
import pl.betoncraft.betonquest.core.Point;
import pl.betoncraft.betonquest.core.VariableNumber;
import pl.betoncraft.betonquest.api.event.QuestEvent;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.core.PlayerData;

import java.util.UUID;

/**
 * Modifies player's points
 *
 * @author Jakub Sapalski
 */
public class PointEvent extends QuestEvent {

    protected final VariableNumber count;
    protected final boolean multi;
    protected final String category;

    public PointEvent(Instruction instruction) throws InstructionParseException {
        super(instruction);
        persistent = true;
        category = instruction.next();
        String number = instruction.next();
        if (number.startsWith("*")) {
            multi = true;
            number = number.replace("*", "");
        } else {
            multi = false;
        }
        try {
            count = new VariableNumber(number);
        } catch (NumberFormatException e) {
            throw new InstructionParseException("Could not parse point count", e);
        }
    }

    @Override
    public void run(UUID uuid) throws QuestRuntimeException {
        PlayerData playerData = BetonQuest.getInstance().getPlayerData(uuid);
        addPoints(uuid, playerData);
    }

    private void addPoints(UUID uuid, PlayerData playerData) throws QuestRuntimeException {
        if (multi) {
            for (Point p : playerData.getPoints()) {
                if (p.getCategory().equalsIgnoreCase(category)) {
                    playerData.modifyPoints(category,
                            (int) Math.floor((p.getCount() * count.getDouble(uuid)) - p.getCount()));
                }
            }
        } else {
            playerData.modifyPoints(category, (int) Math.floor(count.getDouble(uuid)));
        }
    }
}
