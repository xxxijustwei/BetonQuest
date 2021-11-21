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
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.metadata.MetadataValue;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.core.Instruction;
import pl.betoncraft.betonquest.api.MobKillNotifier.MobKilledEvent;
import pl.betoncraft.betonquest.api.Objective;
import pl.betoncraft.betonquest.exceptions.InstructionParseException;
import pl.betoncraft.betonquest.utils.MessageUtils;
import pl.betoncraft.betonquest.utils.PlayerConverter;
import pl.betoncraft.betonquest.utils.Utils;

import java.util.List;
import java.util.UUID;

/**
 * Player has to kill specified amount of specified mobs. It can also require
 * the player to kill specifically named mobs and notify them about the required
 * amount.
 *
 * @author Jakub Sapalski
 */
public class MobKillObjective extends Objective implements Listener {

    private final int notifyInterval;
    protected EntityType mobType;
    protected int amount;
    protected String name;
    protected String marked;
    protected boolean notify;

    public MobKillObjective(Instruction instruction) throws InstructionParseException {
        super(instruction);
        template = MobData.class;
        mobType = instruction.getEnum(EntityType.class);
        amount = instruction.getPositive();
        name = instruction.getOptional("name");
        if (name != null) {
            name = Utils.format(name, true, false).replace('_', ' ');
        }
        marked = instruction.getOptional("marked");
        notifyInterval = instruction.getInt(instruction.getOptional("notify"), 1);
        notify = instruction.hasArgument("notify") || notifyInterval > 1;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMobKill(MobKilledEvent event) {
        // check if it's the right entity type
        if (!event.getEntity().getType().equals(mobType)) {
            return;
        }
        // if the entity should have a name and it does not match, return
        if (name != null && (event.getEntity().getCustomName() == null ||
                !event.getEntity().getCustomName().equals(name))) {
            return;
        }
        // check if the entity is correctly marked
        if (marked != null) {
            if (!event.getEntity().hasMetadata("betonquest-marked")) {
                return;
            }
            List<MetadataValue> meta = event.getEntity().getMetadata("betonquest-marked");
            for (MetadataValue m : meta) {
                if (!m.asString().equals(marked)) {
                    return;
                }
            }
        }
        // check if the player has this objective
        UUID uuid = event.getPlayer().getUniqueId();
        if (containsPlayer(uuid) && checkConditions(uuid)) {
            // the right mob was killed, handle data update
            MobData playerData = (MobData) dataMap.get(uuid);
            playerData.subtract();
            if (playerData.isZero()) {
                completeObjective(uuid);
            } else if (notify && playerData.getAmount() % notifyInterval == 0) {
                // send a notification
                MessageUtils.sendNotify(uuid, "mobs_to_kill", new String[]{String.valueOf(playerData.getAmount())},
                        "mobs_to_kill,info");
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
            return Integer.toString(((MobData) dataMap.get(uuid)).getAmount());
        } else if (name.equalsIgnoreCase("amount")) {
            return Integer.toString(amount - ((MobData) dataMap.get(uuid)).getAmount());
        }
        return "";
    }

    public static class MobData extends ObjectiveData {

        private int amount;

        public MobData(String instruction, UUID uuid, String objID) {
            super(instruction, uuid, objID);
            amount = Integer.parseInt(instruction);
        }

        public int getAmount() {
            return amount;
        }

        public void subtract() {
            amount--;
            update();
        }

        public boolean isZero() {
            return amount <= 0;
        }

        @Override
        public String toString() {
            return Integer.toString(amount);
        }

    }
}
