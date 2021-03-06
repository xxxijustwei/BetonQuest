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

import org.bukkit.Location;
import org.bukkit.entity.Player;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.core.VariableNumber;
import pl.betoncraft.betonquest.api.Condition;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.utils.LocationData;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.util.UUID;

/**
 * Requires the player to be in specified distance from a location
 *
 * @author Jakub Sapalski
 */
public class LocationCondition extends Condition {

    private final LocationData loc;
    private final VariableNumber range;

    public LocationCondition(Instruction instruction) throws InstructionParseException {
        super(instruction);
        loc = instruction.getLocation();
        range = instruction.getVarNum();
    }

    @Override
    public boolean check(UUID uuid) throws QuestRuntimeException {
        Location location = loc.getLocation(uuid);
        Player player = PlayerConverter.getPlayer(uuid);
        if (!location.getWorld().equals(player.getWorld())) {
            return false;
        }
        double r = range.getDouble(uuid);
        return player.getLocation().distanceSquared(location) <= r * r;
    }

}
