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
package pl.betoncraft.betonquest.compatibility.vault;

import org.bukkit.entity.Player;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.core.VariableNumber;
import pl.betoncraft.betonquest.api.event.QuestEvent;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.exceptions.QuestRuntimeException;
import pl.betoncraft.betonquest.utils.MessageUtils;
import pl.betoncraft.betonquest.utils.PlayerConverter;

import java.text.DecimalFormat;
import java.util.UUID;

/**
 * Modifies player's balance
 *
 * @author Jakub Sapalski
 */
public class MoneyEvent extends QuestEvent {

    private final VariableNumber amount;
    private final boolean notify;
    private boolean multi;

    public MoneyEvent(Instruction instruction) throws InstructionParseException {
        super(instruction);
        String string = instruction.next();
        if (string.startsWith("*")) {
            multi = true;
            string = string.replace("*", "");
        }
        try {
            amount = new VariableNumber(string);
        } catch (NumberFormatException e) {
            throw new InstructionParseException("Could not parse money amount", e);
        }
        notify = instruction.hasArgument("notify");
    }

    @SuppressWarnings("deprecation")
    @Override
    public void run(UUID uuid) throws QuestRuntimeException {
        Player player = PlayerConverter.getPlayer(uuid);
        // get the difference between target money and current money
        double current = VaultIntegrator.getEconomy().getBalance(player);
        double target;
        if (multi)
            target = current * amount.getDouble(uuid);
        else
            target = current + amount.getDouble(uuid);

        double difference = target - current;
        DecimalFormat df = new DecimalFormat("#.00");
        String currencyName = VaultIntegrator.getEconomy().currencyNamePlural();

        if (difference > 0) {
            VaultIntegrator.getEconomy().depositPlayer(player.getName(), difference);
            if (notify) {
                MessageUtils.sendNotify(uuid, "money_given",
                        new String[]{df.format(difference), currencyName}, "money_given,info");
            }
        } else if (difference < 0) {
            VaultIntegrator.getEconomy().withdrawPlayer(player.getName(), -difference);
            if (notify) {
                MessageUtils.sendNotify(uuid, "money_taken",
                        new String[]{df.format(difference), currencyName}, "money_taken,info");
            }
        }
    }
}
