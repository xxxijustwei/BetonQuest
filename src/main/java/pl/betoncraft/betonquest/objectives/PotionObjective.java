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
import org.bukkit.Location;
import org.bukkit.block.BrewingStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.api.Objective;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.core.QuestItem;
import pl.betoncraft.betonquest.utils.MessageUtils;

import java.util.HashMap;
import java.util.UUID;

/**
 * Requires the player to manually brew a potion.
 *
 * @author Jakub Sapalski
 */
public class PotionObjective extends Objective implements Listener {

    private final QuestItem potion;
    private final int amount;
    private final boolean notify;
    private final int notifyInterval;
    private final HashMap<Location, UUID> locations = new HashMap<>();

    public PotionObjective(Instruction instruction) throws InstructionParseException {
        super(instruction);
        template = PotionData.class;
        potion = instruction.getQuestItem();
        amount = instruction.getInt();
        notifyInterval = instruction.getInt(instruction.getOptional("notify"), 1);
        notify = instruction.hasArgument("notify") || notifyInterval > 1;
    }

    @EventHandler(ignoreCancelled = true)
    public void onIngredientPut(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.BREWING)
            return;
        if (event.getRawSlot() == 3 || event.getClick().equals(ClickType.SHIFT_LEFT)) {
            UUID uuid = event.getWhoClicked().getUniqueId();
            if (!containsPlayer(uuid))
                return;
            locations.put(((BrewingStand) event.getInventory().getHolder()).getLocation(), uuid);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBrew(final BrewEvent event) {
        UUID uuid = locations.remove(event.getBlock().getLocation());
        if (uuid == null)
            return;
        final PotionData data = ((PotionData) dataMap.get(uuid));
        // this tracks how many potions there are in the stand before brewing
        int alreadyExistingTemp = 0;
        for (int i = 0; i < 3; i++)
            if (checkPotion(event.getContents().getItem(i)))
                alreadyExistingTemp++;
        // making it final for the runnable
        final int alreadyExisting = alreadyExistingTemp;
        new BukkitRunnable() {
            @Override
            public void run() {
                // unfinaling it for modifications
                boolean brewed = false;
                int alreadyExistingFinal = alreadyExisting;
                for (int i = 0; i < 3; i++) {
                    // if there were any potions before, don't count them to
                    // prevent cheating
                    if (checkPotion(event.getContents().getItem(i))) {
                        if (alreadyExistingFinal <= 0 && checkConditions(uuid)) {
                            data.brew();
                        }
                        alreadyExistingFinal--;
                        brewed = true;
                    }
                }
                // check if the objective has been completed
                if (data.getAmount() >= amount) {
                    completeObjective(uuid);
                } else if (brewed && notify && data.getAmount() % notifyInterval == 0) {
                    MessageUtils.sendNotify(uuid, "potions_to_brew",
                            new String[]{String.valueOf(amount - data.getAmount())},
                            "potions_to_brew,info");
                }
            }
        }.runTask(BetonQuest.getInstance());
    }

    /**
     * Checks if this ItemStack matches a potion defined in "effects" HashMap.
     */
    private boolean checkPotion(ItemStack item) {
        if (item == null)
            return false;
        return potion.compare(null, item);
    }

    @Override
    public String getProperty(String name, UUID uuid) {
        if (name.equalsIgnoreCase("left")) {
            return Integer.toString(amount - ((PotionData) dataMap.get(uuid)).getAmount());
        } else if (name.equalsIgnoreCase("amount")) {
            return Integer.toString(((PotionData) dataMap.get(uuid)).getAmount());
        }
        return "";
    }

    @Override
    public void start() {
        Bukkit.getPluginManager().registerEvents(this, BetonQuest.getInstance());
    }

    @Override
    public void stop() {
        locations.clear();
        HandlerList.unregisterAll(this);
    }

    @Override
    public String getDefaultDataInstruction() {
        return "0";
    }

    public static class PotionData extends ObjectiveData {

        private int amount;

        public PotionData(String instruction, UUID uuid, String objID) {
            super(instruction, uuid, objID);
            amount = Integer.parseInt(instruction);
        }

        public void brew() {
            amount++;
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
