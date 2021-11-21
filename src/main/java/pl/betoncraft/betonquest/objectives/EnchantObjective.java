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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.api.Objective;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.core.QuestItem;

import java.util.List;
import java.util.UUID;

/**
 * Requires the player to enchant an item.
 *
 * @author Jakub Sapalski
 */
public class EnchantObjective extends Objective implements Listener {

    private QuestItem item;
    private List<EnchantmentData> enchantments;

    public EnchantObjective(Instruction instruction) throws InstructionParseException {
        super(instruction);
        template = ObjectiveData.class;
        item = instruction.getQuestItem();
        enchantments = instruction.getList(EnchantmentData::convert);
        if (enchantments.isEmpty()) throw new InstructionParseException("Not enough arguments");
    }

    @EventHandler(ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        UUID uuid = player.getUniqueId();
        if (!containsPlayer(uuid))
            return;
        if (!item.compare(player, event.getItem()))
            return;
        for (EnchantmentData enchant : enchantments) {
            if (!event.getEnchantsToAdd().containsKey(enchant.getEnchantment())
                    || event.getEnchantsToAdd().get(enchant.getEnchantment()) < enchant.getLevel()) {
                return;
            }
        }
        if (checkConditions(uuid)) {
            completeObjective(uuid);
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
        return "";
    }

    public static class EnchantmentData {

        private final Enchantment enchantment;
        private final int level;

        public EnchantmentData(Enchantment enchantment, int level) {
            this.enchantment = enchantment;
            this.level = level;
        }

        public static EnchantmentData convert(String string) throws InstructionParseException {
            String[] parts = string.split(":");
            if (parts.length != 2)
                throw new InstructionParseException("Could not parse enchantment: " + string);
            Enchantment enchantment = Enchantment.getByName(parts[0].toUpperCase());
            if (enchantment == null)
                throw new InstructionParseException("Enchantment type '" + parts[0] + "' does not exist");
            int level;
            try {
                level = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                throw new InstructionParseException("Could not parse enchantment level: " + string, e);
            }
            return new EnchantmentData(enchantment, level);
        }

        public Enchantment getEnchantment() {
            return enchantment;
        }

        public int getLevel() {
            return level;
        }
    }

}
