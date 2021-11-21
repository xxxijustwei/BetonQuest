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
package pl.betoncraft.betonquest.objectives;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.api.Objective;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.utils.MessageUtils;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.util.UUID;

public class BreedObjective extends Objective implements Listener {

    private final EntityType type;
    private final int amount;
    private final boolean notify;

    public BreedObjective(Instruction instruction) throws InstructionParseException {
        super(instruction);
        template = BreedData.class;
        type = instruction.getEntity();
        amount = instruction.getPositive();
        notify = instruction.hasArgument("notify");
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreeding(EntityBreedEvent event) {
        if (event.getEntityType() == type && event.getBreeder() instanceof Player) {
            UUID uuid = event.getBreeder().getUniqueId();
            if (!containsPlayer(uuid)) {
                return;
            }
            if (checkConditions(uuid)) {
                BreedData data = (BreedData) dataMap.get(uuid);
                data.breed();
                if (data.getAmount() == 0) {
                    completeObjective(uuid);
                } else if (notify) {
                    MessageUtils.sendNotify(uuid, "animals_to_breed", new String[]{String.valueOf(data.getAmount())},
                            "animals_to_breed,info");
                }
            }
        }
    }

    @Override
    public void start() {
        Bukkit.getPluginManager().registerEvents(this, BetonQuest.getInstance());
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public String getDefaultDataInstruction() {
        return Integer.toString(amount);
    }

    @Override
    public String getProperty(String name, UUID uuid) {
        if (name.equalsIgnoreCase("left")) {
            return Integer.toString(amount - ((BreedData) dataMap.get(uuid)).getAmount());
        } else if (name.equalsIgnoreCase("amount")) {
            return Integer.toString(((BreedData) dataMap.get(uuid)).getAmount());
        }
        return "";
    }

    public static class BreedData extends ObjectiveData {

        private int amount;

        public BreedData(String instruction, UUID uuid, String objID) {
            super(instruction, uuid, objID);
            amount = Integer.parseInt(instruction);
        }

        public void breed() {
            amount--;
            update();
        }

        public int getAmount() {
            return amount;
        }

        @Override
        public String toString() {
            return String.valueOf(amount);
        }

    }

}
