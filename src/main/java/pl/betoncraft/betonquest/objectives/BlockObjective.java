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
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.api.Objective;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.utils.BlockSelector;
import pl.betoncraft.betonquest.utils.MessageUtils;

import java.util.UUID;

/**
 * Player has to break/place specified amount of blocks. Doing opposite thing
 * (breaking when should be placing) will reverse the progress.
 *
 * @author Jakub Sapalski
 */
public class BlockObjective extends Objective implements Listener {

    private final int neededAmount;
    private final boolean notify;
    private final int notifyInterval;
    private final BlockSelector selector;

    public BlockObjective(Instruction instruction) throws InstructionParseException {
        super(instruction);
        template = BlockData.class;
        selector = instruction.getBlockSelector();
        neededAmount = instruction.getInt();
        notifyInterval = instruction.getInt(instruction.getOptional("notify"), 1);
        notify = instruction.hasArgument("notify") || notifyInterval > 1;

        if (selector != null && !selector.isValid()) {
            throw new InstructionParseException("Invalid selector: " + selector.toString());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        // if the player has this objective, the event isn't canceled,
        // the block is correct and conditions are met
        if (containsPlayer(uuid) && selector.match(event.getBlock()) && checkConditions(uuid)) {
            // add the block to the total amount
            BlockData playerData = (BlockData) dataMap.get(uuid);
            playerData.add();
            // complete the objective
            if (playerData.getAmount() == neededAmount) {
                completeObjective(uuid);
            } else if (notify && playerData.getAmount() % notifyInterval == 0) {
                // or maybe display a notification
                if (playerData.getAmount() > neededAmount) {
                    MessageUtils.sendNotify(uuid, "blocks_to_break",
                            new String[]{String.valueOf(playerData.getAmount() - neededAmount)},
                            "blocks_to_break,info");
                } else {
                    MessageUtils.sendNotify(uuid, "blocks_to_place",
                            new String[]{String.valueOf(neededAmount - playerData.getAmount())},
                            "blocks_to_place,info");
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        // if the player has this objective, the event isn't canceled,
        // the block is correct and conditions are met
        if (containsPlayer(uuid) && selector.match(event.getBlock()) && checkConditions(uuid)) {
            // remove the block from the total amount
            BlockData playerData = (BlockData) dataMap.get(uuid);
            playerData.remove();
            // complete the objective
            if (playerData.getAmount() == neededAmount) {
                completeObjective(uuid);
            } else if (notify && playerData.getAmount() % notifyInterval == 0) {
                // or maybe display a notification
                if (playerData.getAmount() > neededAmount) {
                    MessageUtils.sendNotify(uuid, "blocks_to_break",
                            new String[]{String.valueOf(playerData.getAmount() - neededAmount)},
                            "blocks_to_break,info");
                } else {
                    MessageUtils.sendNotify(uuid, "blocks_to_place",
                            new String[]{String.valueOf(neededAmount - playerData.getAmount())},
                            "blocks_to_place,info");
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
        return "0";
    }

    @Override
    public String getProperty(String name, UUID uuid) {
        if (name.equalsIgnoreCase("left")) {
            return Integer.toString(neededAmount - ((BlockData) dataMap.get(uuid)).getAmount());
        } else if (name.equalsIgnoreCase("amount")) {
            return Integer.toString(((BlockData) dataMap.get(uuid)).getAmount());
        }
        return "";
    }

    public static class BlockData extends ObjectiveData {

        private int amount;

        public BlockData(String instruction, UUID uuid, String objID) {
            super(instruction, uuid, objID);
            amount = Integer.parseInt(instruction);
        }

        private void add() {
            amount++;
            update();
        }

        private void remove() {
            amount--;
            update();
        }

        private int getAmount() {
            return amount;
        }

        @Override
        public String toString() {
            return String.valueOf(amount);
        }
    }
}
