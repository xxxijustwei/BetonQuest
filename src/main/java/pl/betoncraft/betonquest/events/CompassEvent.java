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

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import pl.betoncraft.betonquest.config.FileManager;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.config.ConfigPackage;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.utils.LocationData;
import pl.betoncraft.betonquest.utils.LogUtils;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.util.UUID;
import java.util.logging.Level;

/**
 * Adds a compass specific tag to the player.
 *
 * @author Jakub Sapalski
 */
public class CompassEvent extends QuestEvent {

    private Action action;
    private String compass;
    private ConfigurationSection compassSection;
    private ConfigPackage compassPackage;

    public CompassEvent(Instruction instruction) throws InstructionParseException {
        super(instruction);
        persistent = true;

        action = instruction.getEnum(Action.class);
        compass = instruction.next();

        // Check if compass is valid
        ConfigurationSection s = FileManager.getPackages().getMain().getYaml().getConfigurationSection("compass");
        if (s != null) {
            if (s.contains(compass)) {
                compassSection = s.getConfigurationSection(compass);
                compassPackage = FileManager.getPackages();
            }
        }

        if (compassSection == null) {
            throw new InstructionParseException("Invalid compass location: " + compass);
        }
    }

    @Override
    public void run(UUID uuid) {
        switch (action) {
            case ADD:
            case DEL:
                // Add Tag to player
                try {
                    new TagEvent(new Instruction(null, "tag " + action.toString().toLowerCase() + " compass-" + compass)).run(uuid);
                } catch (InstructionParseException e) {
                    LogUtils.getLogger().log(Level.WARNING, "Failed to tag player with compass point: " + compass);
                    LogUtils.logThrowable(e);
                }
                return;
            case SET:
                Location location;
                try {
                    location = new LocationData(compassSection.getString("location")).getLocation(uuid);
                } catch (QuestRuntimeException | InstructionParseException e) {
                    LogUtils.getLogger().log(Level.WARNING, "Failed to set compass: " + compass);
                    LogUtils.logThrowable(e);
                    return;
                }

                Player player = PlayerConverter.getPlayer(uuid);
                if (player != null) {
                    player.setCompassTarget(location);
                }
        }
    }

    public enum Action {
        ADD,
        DEL,
        SET
    }
}
