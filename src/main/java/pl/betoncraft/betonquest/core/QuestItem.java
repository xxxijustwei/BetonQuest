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
package pl.betoncraft.betonquest.core;

import com.taylorswiftcn.justwei.util.MegumiUtil;
import ink.ptms.zaphkiel.ZaphkielAPI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.core.id.ItemID;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.utils.LogUtils;

import java.util.logging.Level;

public class QuestItem {

    private final String itemID;

    public QuestItem(ItemID itemID) throws InstructionParseException {
        this(itemID.generateInstruction());
    }

    public QuestItem(Instruction instruction) throws InstructionParseException {
        this(instruction.getInstruction());
    }

    public QuestItem(String instruction) throws InstructionParseException {
        this.itemID = instruction;
    }

    public boolean compare(Player player, ItemStack item) {
        // basic item checks
        if (MegumiUtil.isEmpty(item)) return false;

        return generate(player, 1).isSimilar(item);
    }

    public ItemStack generate(int amount) {
        return generate(null, amount);
    }

    public ItemStack generate(Player player, int amount)  {
        ItemStack generate = ZaphkielAPI.INSTANCE.getItemStack(itemID, player);
        if (MegumiUtil.isEmpty(generate)) {
            LogUtils.getLogger().log(Level.WARNING, "Item '" + itemID + "' is not defined (Zaphkiel)");
            return new ItemStack(Material.AIR);
        }
        generate.setAmount(amount);
        return generate;
    }
}
