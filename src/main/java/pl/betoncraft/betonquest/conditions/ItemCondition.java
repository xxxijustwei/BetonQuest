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
import org.bukkit.inventory.ItemStack;
import pl.betoncraft.betonquest.api.Condition;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.core.Instruction.Item;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.util.UUID;

/**
 * Requires the player to have specified amount of items in the inventory
 *
 * @author Jakub Sapalski
 */
public class ItemCondition extends Condition {

    private final Item[] questItems;

    public ItemCondition(Instruction instruction) throws InstructionParseException {
        super(instruction);
        questItems = instruction.getItemList();
    }

    @Override
    public boolean check(UUID uuid) throws QuestRuntimeException {
        Player player = PlayerConverter.getPlayer(uuid);

        int successfulChecks = 0; // Count of successful checks

        for (Item questItem : questItems) {
            int counter = 0; // Reset counter for each item
            int amount = questItem.getAmount().getInt(uuid);

            ItemStack[] inventoryItems = player.getInventory().getContents();
            for (ItemStack item : inventoryItems) {
                if (item == null || !questItem.isItemEqual(player, item)) {
                    continue;
                }
                counter += item.getAmount();
            }

            if (counter >= amount) {
                successfulChecks++;
            }
        }
        return successfulChecks == questItems.length;
    }
}
