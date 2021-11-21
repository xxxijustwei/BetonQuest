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
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.api.Objective;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.utils.MessageUtils;

import java.util.UUID;

/**
 * Requires the player to catch the fish.
 *
 * @author Jakub Sapalski
 */
public class FishObjective extends Objective implements Listener {

    private final Material fish;
    private final byte data;
    private final int amount;
    private final boolean notify;
    private final int notifyInterval;

    public FishObjective(Instruction instruction) throws InstructionParseException {
        super(instruction);
        template = FishData.class;
        String[] fishParts = instruction.next().split(":");
        fish = Material.matchMaterial(fishParts[0]);
        if (fish == null)
            throw new InstructionParseException("Unknown fish type");
        if (fishParts.length > 1) {
            try {
                data = Byte.parseByte(fishParts[1]);
            } catch (NumberFormatException e) {
                throw new InstructionParseException("Could not parse fish data value", e);
            }
        } else {
            data = -1;
        }
        amount = instruction.getInt();
        if (amount < 1) {
            throw new InstructionParseException("Fish amount cannot be less than 0");
        }
        notifyInterval = instruction.getInt(instruction.getOptional("notify"), 1);
        notify = instruction.hasArgument("notify") || notifyInterval > 1;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true)
    public void onFishCatch(PlayerFishEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (event.getState() != State.CAUGHT_FISH)
            return;
        if (!containsPlayer(uuid))
            return;
        if (event.getCaught() == null)
            return;
        if (event.getCaught().getType() != EntityType.DROPPED_ITEM)
            return;
        ItemStack item = ((Item) event.getCaught()).getItemStack();
        if (item.getType() != fish)
            return;
        if (data >= 0 && item.getData().getData() != data)
            return;
        FishData data = (FishData) dataMap.get(uuid);
        if (checkConditions(uuid))
            data.catchFish();
        if (data.getAmount() <= 0)
            completeObjective(uuid);
        else if (notify && data.getAmount() % notifyInterval == 0)
            MessageUtils.sendMessage(uuid, "fish_to_catch", new String[]{String.valueOf(data.getAmount())});
    }

    @Override
    public String getProperty(String name, UUID uuid) {
        if (name.equalsIgnoreCase("left")) {
            return Integer.toString(((FishData) dataMap.get(uuid)).getAmount());
        } else if (name.equalsIgnoreCase("amount")) {
            return Integer.toString(amount - ((FishData) dataMap.get(uuid)).getAmount());
        }
        return "";
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

    public static class FishData extends ObjectiveData {

        private int amount;

        public FishData(String instruction, UUID uuid, String objID) {
            super(instruction, uuid, objID);
            amount = Integer.parseInt(instruction);
        }

        public void catchFish() {
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
