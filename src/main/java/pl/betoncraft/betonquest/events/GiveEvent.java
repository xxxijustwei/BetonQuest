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

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.core.Instruction.Item;
import pl.betoncraft.betonquest.core.VariableNumber;
import pl.betoncraft.betonquest.api.event.QuestEvent;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.core.QuestItem;
import pl.betoncraft.betonquest.utils.MessageUtils;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.util.HashMap;
import java.util.UUID;

/**
 * Gives the player specified items
 *
 * @author Jakub Sapalski
 */
public class GiveEvent extends QuestEvent {

    private final Item[] questItems;
    private final boolean notify;

    public GiveEvent(Instruction instruction) throws InstructionParseException {
        super(instruction);
        questItems = instruction.getItemList();
        notify = instruction.hasArgument("notify");
    }

    @Override
    public void run(UUID uuid) throws QuestRuntimeException {
        Player player = PlayerConverter.getPlayer(uuid);
        for (Item theItem : questItems) {
            QuestItem questItem = theItem.getItem();
            ItemStack item = questItem.generate(player, 1);
            VariableNumber amount = theItem.getAmount();
            int amountInt = amount.getInt(uuid);
            while (amountInt > 0) {
                int stackSize = Math.min(amountInt, 64);
                item.setAmount(stackSize);
                HashMap<Integer, ItemStack> left = player.getInventory().addItem(item);
                for (Integer leftNumber : left.keySet()) {
                    ItemStack itemStack = left.get(leftNumber);
                    player.getWorld().dropItem(player.getLocation(), itemStack);
                }
                amountInt = amountInt - stackSize;
            }

            if (notify) {
                String name = item.getType().name();
                if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                    name = item.getItemMeta().getDisplayName();
                }
                MessageUtils.sendNotify(uuid, "common.items_given",
                        new String[]{name, String.valueOf(amountInt)},
                        "items_given,info");
            }
        }
    }
}
