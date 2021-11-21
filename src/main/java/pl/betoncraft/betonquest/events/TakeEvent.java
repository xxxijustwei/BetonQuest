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

import java.util.*;

/**
 * Removes items from player's inventory
 *
 * @author Jakub Sapalski
 */
public class TakeEvent extends QuestEvent {

    private final Item[] questItems;
    private final boolean notify;

    private int counter;

    public TakeEvent(Instruction instruction) throws InstructionParseException {
        super(instruction);
        questItems = instruction.getItemList();
        notify = instruction.hasArgument("notify");
    }

    @Override
    public void run(UUID uuid) throws QuestRuntimeException {
        Player player = PlayerConverter.getPlayer(uuid);
        for (Item item : questItems) {
            QuestItem questItem = item.getItem();
            VariableNumber amount = item.getAmount();

            // cache the amount
            counter = amount.getInt(uuid);

            // notify the player
            if (notify) {
                ItemStack qItem = questItem.generate(player, 1);
                String name = qItem.getType().name();
                if (qItem.hasItemMeta() && qItem.getItemMeta().hasDisplayName()) {
                    name = qItem.getItemMeta().getDisplayName();
                }
                MessageUtils.sendNotify(uuid, "items_taken",
                        new String[]{name, String.valueOf(counter)},
                        "items_taken,info");
            }

            // Remove Quest items from player's inventory
            player.getInventory().setContents(removeItems(player, questItem));

            // Remove Quest items from player's armor slots
            if (counter > 0) {
                player.getInventory()
                        .setArmorContents(removeItems(player, questItem));
            }
        }
    }

    private ItemStack[] removeItems(Player player, QuestItem questItem) {
        ItemStack[] items = player.getInventory().getContents();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (questItem.compare(player, item)) {
                if (item.getAmount() - counter <= 0) {
                    counter -= item.getAmount();
                    items[i] = null;
                } else {
                    item.setAmount(item.getAmount() - counter);
                    counter = 0;
                }
                if (counter <= 0) {
                    break;
                }
            }
        }
        return items;
    }
}
